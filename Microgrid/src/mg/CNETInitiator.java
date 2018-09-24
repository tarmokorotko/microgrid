package mg;

import java.util.Enumeration;
import java.util.Vector;

import jade.core.Agent;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetInitiator;

public class CNETInitiator extends ContractNetInitiator {

    	private static final long serialVersionUID = 8014843226916342564L;
    	
		public CNETInitiator(Agent a, ACLMessage cfp) {
			super(a, cfp);
		}
		
		@SuppressWarnings( { "rawtypes", "unchecked" } )
		protected Vector prepareCfps(ACLMessage cfp) {	
			// Set up CFP message
        	cfp.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
        	Vector v = new Vector(1);
			v.addElement(cfp);
			return v;
		}
		
		@SuppressWarnings({ "unchecked", "rawtypes" })
		protected void handlePropose(ACLMessage propose, Vector acceptances) {
			// Set up reply message
			ACLMessage reply = propose.createReply();
			/*
			// Send accept or reject messages
			if(acceptProposal(propose.getContent())) {
				// Prepare acceptance message
				reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
				
				// Log
				Util.logString(getLocalName()+": Proposal from "+propose.getSender().getLocalName()+" accepted", 20);				
			} else {
				// Prepare rejectance message
				reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
				
				// Log
				Util.logString(getLocalName()+": Proposal from "+propose.getSender().getLocalName()+" rejected", 20);
				
				// Recalculate bids 
				recalculateBids(propose);
			}
			*/
			// add reply to acceptances vector
			reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL); // always accept
			acceptances.addElement(reply);
		}
		
		protected void handleRefuse(ACLMessage refuse) {
			System.out.println("Refusal "+ refuse.getContent() +" received from "+refuse.getSender().getLocalName());
		}
		
		protected void handleFailure(ACLMessage failure) {
			System.out.println("Failure "+ failure.getContent() +" received from "+failure.getSender().getLocalName());
		}
		
		protected void handleInform(ACLMessage inform) {
			System.out.println("Inform "+ inform.getContent() +" received from "+inform.getSender().getLocalName());			
		}
		
		@SuppressWarnings("rawtypes")
		protected void handleAllResponses(Vector responses, Vector acceptances) {
			Enumeration e = responses.elements();
			while (e.hasMoreElements()) {
				ACLMessage msg = (ACLMessage) e.nextElement();
				if(msg.getPerformative() != ACLMessage.PROPOSE &&
						msg.getPerformative() != ACLMessage.CFP &&
						msg.getPerformative() != ACLMessage.REFUSE &&
						msg.getPerformative() != ACLMessage.FAILURE &&
						msg.getPerformative() != ACLMessage.INFORM) {
					System.out.println(/*getLocalName()+*/": Received unhandled response "+ msg.getContent() +";"+ msg.getPerformative() +"  from "+msg.getSender().getLocalName());
				}
			}
		}
	
}
