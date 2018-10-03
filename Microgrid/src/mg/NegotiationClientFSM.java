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

	protected static final String SEND_BIDS = "Send-Proposal";
	protected static final String RECEIVE_OFFER = "Receive-Offer";
	protected static final String SEND_REPLY = "Send-Reply";
	protected static final String FINALIZE = "Finalize-negotiation";
	
	public NegotiationClientFSM(Agent a, ACLMessage initMsg) {
		super(a);
		
		negotiationRounds = 0;
		initialMessage = initMsg;
		server = (AID) initMsg.getAllReceiver().next();
		
		// FSM state transitions
		registerDefaultTransition(SEND_BIDS, RECEIVE_OFFER);
		registerTransition(RECEIVE_OFFER, RECEIVE_OFFER,0);
		registerTransition(RECEIVE_OFFER, SEND_REPLY,1);
		registerTransition(SEND_REPLY,RECEIVE_OFFER,1);
		registerTransition(SEND_REPLY,FINALIZE,-1);
		
		Behaviour b;
		DataStore ds = getDataStore();
		
		// SEND PROPOSALS
		b = new SendProposal(myAgent);
		b.setDataStore(ds);
		registerFirstState(b, SEND_BIDS);		
		
		// RECEIVE OFFERS
		b = new OfferHandler(myAgent);
		b.setDataStore(ds);
		registerState(b, RECEIVE_OFFER);
		
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
	private boolean acceptOffer(String offer) {
		boolean accept = true;
		
		return accept;
	}
	
	/**
	Inner class SendProposal
	*/
	private class SendProposal extends OneShotBehaviour {
		private static final long     serialVersionUID = 4762407563773002L;
		//private String perf;
		
		public SendProposal(Agent a) {
			super(a);
		}	

		public void action() {
			initialMessage.setPerformative(ACLMessage.CFP);
			initialMessage.setProtocol(FIPANames.InteractionProtocol.FIPA_PROPOSE);
			myAgent.send(initialMessage);
		}
	} // End of inner class SendProposal 
	
	/**
	Inner class OfferHandler
	*/
	private class OfferHandler extends OneShotBehaviour {
		private static final long     serialVersionUID = 4762407563773002L;
		
		private int ret = 0;
		
		public OfferHandler(Agent a) {
			super(a);
		}
		
		public void action() {			
			ACLMessage msg = myAgent.receive(MessageTemplate.and(
					MessageTemplate.MatchPerformative(ACLMessage.PROPOSE), 
					MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_PROPOSE)
					));
			if(msg != null) {	
				currentOffer = msg.getContent();
				Util.logString(myAgent.getLocalName()+": Received offer: "+currentOffer);
				ret = 1;
			} 			
		}
		
		public int onEnd() {
			return ret;
		}		
	} // End of inner class OfferHandler 
	
	/**
	Inner class SendReply
	*/
	private class SendReply extends OneShotBehaviour {
		private static final long     serialVersionUID = 4762407563773002L;

		private int ret = -1;
		
		public SendReply(Agent a) {
			super(a);
		}
		
		public void action() {
			ACLMessage reply = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);			
			reply.setProtocol(FIPANames.InteractionProtocol.FIPA_PROPOSE);
			reply.addReceiver(server);
			if(acceptOffer(currentOffer)) {
				reply.setContent("Accept offer;"+currentOffer);
				ret = -1;
			} else {
				negotiationRounds++;
				reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
				if(negotiationRounds <= Util.maxNegotiationRounds) {
					String correctedBids = ((ProsumerAgent) getAgent()).composeCorrectedBid(initialMessage.getContent(), currentOffer);
					reply.setContent("Reject offer;Correct bids;" + correctedBids);
					ret = 1;
				} else {
					currentOffer = "0.00 0.000";
					reply.setContent("Reject offer;" + currentOffer);
					ret = -1;
				}
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
		
		public FinalizeNegotiation(Agent a) {
			super(a);
		}
		
		public void action() {
			Util.logString(myAgent.getLocalName()+": DEAL FOR NEXT OPERATION ROUND :"+currentOffer, 20);
		}
	} // End of inner class FinalizeNegotiation 
}