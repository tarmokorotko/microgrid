package mg;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.DataStore;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class NegotiationClientFSM extends FSMBehaviour {

	private static final long serialVersionUID = 5498686208672522466L;
	
	private ACLMessage initialMessage;
	private AID server;
	private String currentOffer;
	private int negotiationRounds;
	
	public boolean acceptOffer = false;
	public boolean rejectOffer = false;

	protected static final String COMPOSE_BIDS = "Compose-Bid-Set";
	protected static final String CHECK_SEND = "Check-Send";
	protected static final String SEND_BIDS = "Send-Proposal";
	protected static final String RECEIVE_OFFER = "Receive-Offer";
	protected static final String DECISION = "Accept-Or-Reject";
	protected static final String SEND_REPLY = "Send-Reply";
	protected static final String FINALIZE = "Finalize-negotiation";
	
	public NegotiationClientFSM(Agent a, ACLMessage initMsg) {
		super(a);
		
		negotiationRounds = 0;
		initialMessage = initMsg;
		server = (AID) initMsg.getSender();
		
		// FSM state transitions
		registerDefaultTransition(COMPOSE_BIDS, CHECK_SEND);
		registerTransition(CHECK_SEND, CHECK_SEND,0);
		registerTransition(CHECK_SEND, SEND_BIDS,1);
		registerDefaultTransition(SEND_BIDS, RECEIVE_OFFER);
		registerTransition(RECEIVE_OFFER, RECEIVE_OFFER,0);
		registerTransition(RECEIVE_OFFER, DECISION,1);
		registerTransition(DECISION, DECISION,0);
		registerTransition(DECISION, SEND_REPLY,1);
		registerTransition(SEND_REPLY,RECEIVE_OFFER,1);
		registerTransition(SEND_REPLY,FINALIZE,-1);
		
		Behaviour b;
		DataStore ds = getDataStore();
		
		// COMPOSE BID
		b = new ComposeBid(myAgent);
		b.setDataStore(ds);
		registerFirstState(b, COMPOSE_BIDS);	
		
		// CHECK SEND
		b = new CheckSend(myAgent);
		b.setDataStore(ds);
		registerState(b, CHECK_SEND);	
		
		// SEND BID
		b = new SendProposal(myAgent);
		b.setDataStore(ds);
		registerState(b, SEND_BIDS);		
		
		// RECEIVE OFFER
		b = new OfferHandler(myAgent);
		b.setDataStore(ds);
		registerState(b, RECEIVE_OFFER);
		
		// RECEIVE OFFER
		b = new OfferDecision(myAgent);
		b.setDataStore(ds);
		registerState(b, DECISION);
		
		// SEND REPLY IN RESPONSE TO OFFER
		b = new SendReply(myAgent);
		b.setDataStore(ds);
		registerState(b, SEND_REPLY);
		
		// FINALIZE NEGOTIATION AND TERMINATE BEHAVIOUR
		b = new FinalizeNegotiation(myAgent);
		b.setDataStore(ds);
		registerLastState(b, FINALIZE);
	}
	
	/**
	 * Inner method for accepting or rejecting offer
	 * @param offer
	 * @return
	 */
	private void acceptOffer(ProsumerAgent ag, String offer) {
		acceptOffer = ag.getAcceptStatus();
		rejectOffer = ag.getRejectStatus();
	}
	
	/**
	* Inner class ComposeBid
	*/
	private class ComposeBid extends OneShotBehaviour {
		private static final long     serialVersionUID = 4762407563773002L;
		
		private ProsumerAgent ag;
		
		public ComposeBid(Agent a) {
			super(a);
		}
		
		public void action() {
			ag = (ProsumerAgent) myAgent;
			
			// Output initialization info to GUI
			ag.updateRoundInfo(initialMessage.getContent());
			
			// Check bid composition origin
			if(ag.getBidOrigin()) {	
				// Get initial bid data from manual source
				ag.setInitialBidManually(initialMessage.getContent());
			} else {
				// Get initial bid data from Excel file
				ag.setInitialBidFromFile(initialMessage.getContent());				
			}
		}
	} // End of inner class ComposeBid 
	
	/**
	* Inner class CheckSend
	*/
	private class CheckSend extends OneShotBehaviour {
		private static final long     serialVersionUID = 4762407563773002L;
		
		private int ret;		
		private ProsumerAgent ag;
		
		public CheckSend(Agent a) {
			super(a);
		}
		
		public void action() {
			ret = 0;
			ag = (ProsumerAgent) myAgent;
			
			// Check if send command received
			if(ag.getSendStatus()) {	
				ag.resetSendStatus();
				
				// Continue to next state
				ret=1;
			}
		}
		
		public int onEnd() {
			return ret;
		}	
	} // End of inner class CheckSend 
	
	/**
	* Inner class SendProposal
	*/
	private class SendProposal extends OneShotBehaviour {
		private static final long     serialVersionUID = 4762407563773002L;
		
		private ProsumerAgent ag;
		private ACLMessage initialBids;
		
		public SendProposal(Agent a) {
			super(a);
		}	

		public void action() {			
			ag = (ProsumerAgent) myAgent;
			
			// Compose initial bid proposal message
			initialBids = new ACLMessage(ACLMessage.CFP);			
			initialBids.addReceiver(initialMessage.getSender());
			initialBids.setContent(ag.getBidSetFromGui());
			initialBids.setProtocol(FIPANames.InteractionProtocol.FIPA_PROPOSE);
			
			// Send initial bid proposal
			myAgent.send(initialBids);
		}
	} // End of inner class SendProposal 
	
	/**
	* Inner class OfferHandler
	*/
	private class OfferHandler extends OneShotBehaviour {
		private static final long     serialVersionUID = 4762407563773002L;
		
		private int ret;
		private ProsumerAgent ag;
		
		public OfferHandler(Agent a) {
			super(a);
		}
		
		public void action() {		
			ret = 0;
			ag = (ProsumerAgent) myAgent;
			
			ACLMessage msg = myAgent.receive(MessageTemplate.and(
					MessageTemplate.MatchPerformative(ACLMessage.PROPOSE), 
					MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_PROPOSE)
					));
			if(msg != null) {	
				// Store received offer
				currentOffer = msg.getContent();
				
				// Log
				Util.logString(myAgent.getLocalName()+": Received offer: "+currentOffer);
				
				// Update received offer to GUI
				ag.updateCurrentOffer(currentOffer, negotiationRounds, Util.maxNegotiationRounds);
				
				// Continue to next state
				ret = 1;
			} 			
		}
		
		public int onEnd() {
			return ret;
		}		
	} // End of inner class OfferHandler 
	
	/**
	* Inner class Decision
	**/
	private class OfferDecision extends OneShotBehaviour {
		private static final long     serialVersionUID = 4762407563773002L;
		
		private int ret;
		private ProsumerAgent ag;
		
		public OfferDecision(Agent a) {
			super(a);
		}
		
		public void action() {
			ret = 0;			
			ag = (ProsumerAgent) myAgent;
			
			// Check verdict for presented offer
			acceptOffer(ag, currentOffer);
			
			// If offer verdict received, advance to next state
			if(acceptOffer || rejectOffer) {
				ret = 1;
			}
		}
		
		public int onEnd() {
			return ret;
		}		
	} // End of inner class Decision 
		
	/**
	* Inner class SendReply
	*/
	private class SendReply extends OneShotBehaviour {
		private static final long     serialVersionUID = 4762407563773002L;

		private int ret;
		private ProsumerAgent ag;
		
		public SendReply(Agent a) {
			super(a);
		}
		
		public void action() {	
			ret = -1;			
			ag = (ProsumerAgent) myAgent;
			
			// Compose reply message
			ACLMessage reply = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);			
			reply.setProtocol(FIPANames.InteractionProtocol.FIPA_PROPOSE);
			reply.addReceiver(server);
			
			if(acceptOffer) {
				// If offer accepted, reply with accept message
				reply.setContent("Accept offer;"+currentOffer);
				ag.resetAcceptStatus();
				acceptOffer = false;
				
				// Move to next, termination state
				ret = -1;
			} else if(rejectOffer) {
				// If offer rejected, increase negotiation round counter
				negotiationRounds++;				
				reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
				
				if(negotiationRounds <= Util.maxNegotiationRounds) {
					// If negotiation rounds less than allowed, correct bids and send them with reply
					String correctedBids = (ag.getBidSetFromGui());
					reply.setContent("Reject offer;Correct bids;" + correctedBids);
					ret = 1;
				} else {
					// If negotiation rounds more than allowed, reply with nil offer and terminate
					currentOffer = "0.00 0.000";
					reply.setContent("Reject offer;" + currentOffer);
					ret = -1;
				}
				ag.resetRejectStatus();
				rejectOffer = false;
			} else {
				Util.logString("ACCEPT REJECT LOGIC ERROR!", 40);
			}
			
			myAgent.send(reply);
		}
		
		public int onEnd() {
			return ret;
		}
	} // End of inner class SendReply 
	
	/**
	Inner class FinalizeNegotiation
	*/
	private class FinalizeNegotiation extends OneShotBehaviour {
		private static final long     serialVersionUID = 4762407563773002L;
		private ProsumerAgent ag;
		
		public FinalizeNegotiation(Agent a) {
			super(a);
		}
		
		public void action() {
			ag = (ProsumerAgent) myAgent;
			
			// Finalize auction and update GUI output
			ag.updateDealInfo(currentOffer);
			Util.logString(myAgent.getLocalName()+": DEAL FOR NEXT OPERATION ROUND :"+currentOffer, 20);
		}
	} // End of inner class FinalizeNegotiation 
}