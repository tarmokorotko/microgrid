package mg;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import jade.core.AID;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.proto.SubscriptionInitiator;

public class AuctionInitiator extends SubscriptionInitiator {
	
	private static final long serialVersionUID = -7111020906826495420L;
	private ProsumerAgent myAgent; 
	
	AuctionInitiator(ProsumerAgent agent) {
        super(agent, new ACLMessage(ACLMessage.SUBSCRIBE)); 
        
        myAgent = agent;
    }
		
	protected Vector<ACLMessage> prepareSubscriptions(ACLMessage subscription) {
		// Search for agents in the role of Distributor
		List<AID> agents;			
		AID server = null;
		try {
			agents = getAgentAIDs(myAgent, "Distributor");
			if(agents.size() > 0) {
				server = agents.get(0);
			}
		} catch (FIPAException e) {
			e.printStackTrace();
		}
		
    	// Set up subscription message
    	subscription.setProtocol(FIPANames.InteractionProtocol.FIPA_SUBSCRIBE);
        subscription.addReceiver(server);   // the agent supplying a subscription service (has a responder role)
        subscription.setContent(String.format("Subscribe agent %s", this.getAgent().getLocalName()));   // the subscription content
        subscription.setConversationId( String.format("SUBSCRIPTION of %s", this.getAgent().getLocalName()));            
        Vector<ACLMessage> v = new Vector<ACLMessage>();
        v.addElement(subscription);
        
        return v;
    }
	
    protected void handleInform(ACLMessage inform) {
        // Extract data from inform message
    	String id = inform.getOntology();
    	
    	switch(id) {
    	case "AUCTION_INIT":
    		myAgent.addNCBehaviour(inform);
    		
    		break;
    	}
    }
    
    protected void handleRefuse(ACLMessage refuse) {
        // handle a refusal from the subscription service
    	Util.logString(myAgent.getLocalName()+": Refusal handling from subscription service", 20);
    }

    protected void handleAccept(ACLMessage accept) {
        // handle an accept from the subscription service
    	Util.logString(myAgent.getLocalName()+": Acceptance handling from subscription service", 20);
    }
    
    /* Output list of agent AIDs
	 * @param agentType
	 * @return
	 * @throws FIPAException
	 */
    private List<AID> getAgentAIDs(ProsumerAgent prsmrAgent, String agentType) throws FIPAException {
		List<AID> agents = new ArrayList<AID>();		
		DFAgentDescription dfd = new DFAgentDescription();
		ServiceDescription sd  = new ServiceDescription();
		sd.setType( agentType );
		dfd.addServices(sd);
       
		DFAgentDescription[] result = DFService.search(prsmrAgent, dfd);
       
	    for (int i=0; i<result.length;i++){
	        agents.add(result[i].getName());
	    }
       
	    return agents;
	}	
}