package mg;

import java.util.Vector;

import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.SubscriptionResponder;

public class AuctionResponder extends SubscriptionResponder {

	private static final long serialVersionUID = 9044256482649608950L;
	private ProsumerAgent myAgent;
			
	AuctionResponder(ProsumerAgent a) {
        super(a, MessageTemplate.and(                                       
        		MessageTemplate.or(
        				MessageTemplate.MatchPerformative(ACLMessage.SUBSCRIBE), 
        				MessageTemplate.MatchPerformative(ACLMessage.CANCEL)),                                       
        		MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_SUBSCRIBE))
        		); 
        myAgent = a;
    }		
   
    protected ACLMessage handleSubscription(ACLMessage subscription_msg) throws NotUnderstoodException, RefuseException {
    	// Instantiate reply message
    	ACLMessage reply;
        
    	// Decision whether to accept or deny subscription request
    	if (subscription_msg.getPerformative() == ACLMessage.SUBSCRIBE) {
            createSubscription(subscription_msg);
            Util.logString(String.format("%s: %s successfully subscribed", myAgent.getLocalName(), subscription_msg.getSender().getLocalName()), 20);
            reply = new ACLMessage(ACLMessage.AGREE);
    	} else {
    		Util.logString(String.format("%s: %s subscription refused", myAgent.getLocalName(), subscription_msg.getSender().getLocalName()), 20);
    		reply = new ACLMessage(ACLMessage.REFUSE);
    	}
        
		return reply;
    }
  
    protected void initiateNextAuctionRound(ACLMessage inform) {
    	// Set auction initialization message ontology
    	inform.setOntology("AUCTION_INIT");
    	
    	Util.logString(myAgent.getLocalName()+": NEW AUCTION ROUND: "+ inform.getContent(), 20);    
    	
        // send notification to all subscribers
        Vector<?> subs = getSubscriptions();
        for(int i=0; i<subs.size(); i++) {
            ((SubscriptionResponder.Subscription)subs.elementAt(i)).notify(inform);
        }
    }       
  
}   