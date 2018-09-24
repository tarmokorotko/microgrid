package mg;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetResponder;
import jade.proto.SSContractNetResponder;

public class CNETResponder extends ContractNetResponder {

	private static final long serialVersionUID = 1901427993791946926L;
	
	public static final String MANAGE_CFP = "Manage-Cfp";
	
	public CNETResponder(Agent a) {
		super(a, MessageTemplate.or(
				MessageTemplate.MatchPerformative(ACLMessage.CFP), 
				MessageTemplate.MatchPerformative(ACLMessage.INFORM)));
		
		deregisterDefaultTransition(HANDLE_CFP);
		registerDefaultTransition(HANDLE_CFP, MANAGE_CFP);
		registerTransition(MANAGE_CFP, SEND_REPLY, 1);
		registerTransition(MANAGE_CFP, HANDLE_CFP, 0);
		
		Behaviour b;
		b = new CfpManager(myAgent);
		registerDSState(b, MANAGE_CFP);
	
		b.setDataStore(getDataStore());
	}
	
	protected ACLMessage handleCfp(ACLMessage cfp) throws RefuseException, FailureException, NotUnderstoodException {			
		// Set up proposal message
		ACLMessage reply = new ACLMessage(ACLMessage.PROPOSE);			
		reply.addReceiver(cfp.getSender());
		reply.setConversationId(cfp.getConversationId());
		reply.setContent(cfp.getContent());		
		
		return reply;
	}
	
	/**
	 Inner class CfpManager
	 */
	private class CfpManager extends OneShotBehaviour {
		private static final long     serialVersionUID = 4766406563773001L;
		int ret = 0;
		
		public CfpManager(Agent a) {
			super(a);
		}
		
		@SuppressWarnings("unused")
		public void action() {
			SSContractNetResponder parent = (SSContractNetResponder) getParent();
			ACLMessage bid = (ACLMessage) getDataStore().get(parent.CFP_KEY);
			ACLMessage reply = (ACLMessage) getDataStore().get(parent.REPLY_KEY);
			/*
			// Get offer and send it to participant
			sendBids(bid.getContent());	
			
			if(ac.negotiationDone) {
				String offer = getOffer(bid.getContent());
				reply.setContent(offer);
				
				//Log presented offer
				Util.logString(String.format("%s: Offer %s presented to %s", getLocalName(), offer, ((AID)reply.getAllReceiver().next()).getLocalName()), 20);
				getDataStore().put(parent.REPLY_KEY, reply);
				ret = 1;
			}
			
			System.out.println(getLocalName()+": Waiting for calculation..."+getPrevious());
			 */
		}
		

		public int onEnd() {
			
			return ret;
		}
	} // End of inner class CfpManager

	protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) throws FailureException {
		ACLMessage reply = accept.createReply();
		reply.setContent("CONFIRMED");
		
		return reply;
	}
	
	protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
					
	}
	
	protected void handleOutOfSequence(ACLMessage cfp,ACLMessage propose, ACLMessage msg){
		System.out.println(": Out of Sequence message received: " + msg.getContent()+" with performative "+ msg.getPerformative());
	}

}
