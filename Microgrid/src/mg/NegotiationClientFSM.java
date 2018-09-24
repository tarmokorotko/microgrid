package mg;

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
	private static final int delay = 500;
	
	private ACLMessage initialMessage;

	protected static final String SEND_BIDS = "Send-Proposal";
	protected static final String RECEIVE_OFFER = "Receive-Offer";
	protected static final String COMPOSE_REPLY = "Compose-Reply";
	protected static final String SEND_REPLY = "Send-Reply";
	protected static final String FINALIZE = "Finalize-negotiation";
	
	public NegotiationClientFSM(Agent a, ACLMessage initMsg) {
		super(a);
		
		initialMessage = initMsg;
		
		// FSM state transitions
		registerDefaultTransition(SEND_BIDS, RECEIVE_OFFER);
		registerTransition(RECEIVE_OFFER, RECEIVE_OFFER,0);
		registerTransition(RECEIVE_OFFER, COMPOSE_REPLY,1);
		registerDefaultTransition(COMPOSE_REPLY, SEND_REPLY);
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
		
		// COMPOSE ACCEPT OR REJECT REPLY
		b = new ComposeReply(myAgent);
		b.setDataStore(ds);
		registerState(b, COMPOSE_REPLY);
		
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
				Util.logString(myAgent.getLocalName()+": Received message content: "+msg.getContent());
				ret = 1;
			} 			
		}
		
		public int onEnd() {
			return ret;
		}		
	} // End of inner class OfferHandler 
		
	/**
	Inner class ComposeReply
	*/
	private class ComposeReply extends OneShotBehaviour {
		private static final long     serialVersionUID = 4762407563773002L;
		
		public ComposeReply(Agent a) {
			super(a);
		}
		
		public void action() {
			Util.logString(myAgent.getLocalName()+": launched FSM state :"+getCurrent(), 20);
			myAgent.doWait(delay);
		}
	} // End of inner class ComposeReply 
	
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
			Util.logString(myAgent.getLocalName()+": launched FSM state :"+getCurrent(), 20);
			myAgent.doWait(delay);
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
			Util.logString(myAgent.getLocalName()+": launched FSM state :"+getCurrent(), 20);
			myAgent.doWait(delay);			
		}
	} // End of inner class FinalizeNegotiation 
}