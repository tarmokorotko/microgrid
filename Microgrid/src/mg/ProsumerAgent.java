package mg;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.SubscriptionInitiator;
import jade.proto.SubscriptionResponder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import gui.PrsmrGUI;
import gui.RunShell;

import mg.BidSet.Bid;

/**
 * This class is a Prosumer intelligent agent. 
 * @author tarmokorotko
 * forked from gitHub user rroche
 */
public class ProsumerAgent extends GuiAgent
{
	private static final long serialVersionUID = 195263862L;
	
	transient protected PrsmrGUI myGui;
	
	private String role = Util.prosumerRoles[0];
	private Auction ac;
	
	Distributor db = new Distributor(this, Util.auctionCycle);
	MySubscriptionInit msi;
	NegotiationClientFSM ncf;
	MySubscriptionResp msr;
	NegotiationServerFSM nsf;
	
	/**
	 * Initialization of agent
	 */
	protected void setup() 
	{
		// Log
		Util.logString(getLocalName() + ": successfully started", 20);
		
		// Get arguments
		Object[] args = getArguments();
		
		// Instantiate and open GUI
		RunShell gui = (RunShell)args[0];
		gui.createShell(getName(), this);		
				
		// Run behavior for registering agents at Simulation Communication agent
		Register rg = new Register();
		addBehaviour(rg);		
				
	} // End setup
	
	/**
	 * Behaviour for registering agents at Simulation Communication agent
	 * @author Tarmo
	 */
	class Register extends OneShotBehaviour {
		
		private static final long serialVersionUID = 9288461912L;
		
		public void action()
		{
			sendMessage("simComAgent", "Register me","INIT",ACLMessage.SUBSCRIBE);
			Util.logString(getLocalName()+": Agent registered", 20);
		}
	}
	
	/**
	 * Behaviour of Distributor role
	 * @author Tarmo
	 *
	 */
	class Distributor extends TickerBehaviour {
		
		private static final long serialVersionUID = 7387677922296356597L;

		public Distributor(Agent a, long period) {
			super(a, period);
		}

		@Override
		protected void onTick() {
			if (role.equals("Distributor")) {
				// Start next auction round and send notification of new auction round to all subscribed participants
				msr.initiateNextAuctionRound(ac.startNewRound());	
				addNSBehaviour(msr.getSubscriptions().size());
				ac.setNrOfSubscribers(msr.getSubscriptions().size());
			} else {
				//removeBehaviour(this.getBehaviourName());
			}
		}
	}
		
	/**
	 * Send message to another agent
	 * @param targetName
	 * @param content
	 * @param conversation
	 * @param type
	 */
	private void sendMessage(String targetName, String content, String conversation, int type)
	{
		ACLMessage message = new ACLMessage(type);
		message.addReceiver(new AID (targetName, AID.ISLOCALNAME));
		message.setContent(content);
		message.setConversationId(conversation);
		this.send(message);
	}
	
	/**
	 * Output list of agent AIDs
	 * @param agentType
	 * @return
	 * @throws FIPAException
	 */
	private List<AID> getAgentAIDs(String agentType) throws FIPAException {
		List<AID> agents = new ArrayList<AID>();		
		DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd  = new ServiceDescription();
        sd.setType( agentType );
        dfd.addServices(sd);
        
        DFAgentDescription[] result = DFService.search(this, dfd);
        
	    for (int i=0; i<result.length;i++){
	        agents.add(result[i].getName());
	   }
        
	    //Util.logString(String.format("All registered agents: %s", agents), 20);
	    return agents;
	}
	
	/**
	 * Terminate agent
	 */
	public void killAgent( ) {
		String name = this.getName();
		this.takeDown();
		this.doDelete();
		Util.logString(name+": Agent terminated.", 20);
	}

	/**
	 * Handle GUI events
	 */
	@Override
	protected void onGuiEvent(GuiEvent ge) {
		int event = ge.getType();
		
		switch(event) {
		case 1: // change role
			String rl = (String)ge.getParameter(0);
			setRole(rl);
			break;
		case 4: // update manual setpoint
			sendMessage("simComAgent", String.format("%s", ge.getParameter(0)) ,"UPDATE" ,ACLMessage.INFORM);
			Util.logString((this.getName()+String.format(": Value changed to %s", ge.getParameter(0))), 20);
			break;
		}
		
	}
	
	/**
	 * Compose bid for agent
	 * @return
	 */
	@SuppressWarnings("unused")
	private List<Double> composeBid() {
		List<Double> bids = new ArrayList<Double>();
		
		// Random bid composition
		Random r = new Random();
		Double[] blim = {-50.0, 50.0}; 
		
		for(int i=0;i<8;i++) {
			Double b = blim[0] + (blim[1] - blim[0]) * r.nextDouble();
			bids.add(b);
		}
		
		// Log output
		Util.logString(String.format("bids: %s", bids), 20);
		
		return bids;		
	}
	
	/**
	 * Set role for agent
	 * @param rl
	 */
	private void setRole(String rl) {
		// if previous role was Distributor, take down agent
		if (role.equals("Distributor")) {
			this.takeDown();
		}
		
		role = rl;
		
		switch(role) {
		case "Distributor": 
			// Register agent as distributor
			this.register(role);
			
			// Try to remove pre-existing behaviours
			try { removeBehaviour(msi);		} catch (NullPointerException npe) {}
			try { removeBehaviour(ncf);	} catch (NullPointerException npe) {}
			try { removeBehaviour(msr);		} catch (NullPointerException npe) {}
			try { removeBehaviour(nsf);	} catch (NullPointerException npe) {}
			
			// Initialize auction
			 ac = new Auction();
			
			// Add role-specific behaviours
			setUpSubscriptionServer();
			addBehaviour(db);
			
			break;			
		default:
			// Try to remove pre-existing behaviours
			try { removeBehaviour(msi);		} catch (NullPointerException npe) {}
			try { removeBehaviour(ncf);	} catch (NullPointerException npe) {}
			try { removeBehaviour(msr);		} catch (NullPointerException npe) {}
			try { removeBehaviour(nsf);	} catch (NullPointerException npe) {}
			try { removeBehaviour(db);		} catch (NullPointerException npe) {}
			
			// Add role-specific behaviours
			subscribeToDistributor();
			
			break;
		}			
				
		Util.logString(String.format(this.getLocalName()+": Assumed role of %s", role),20);
	}

	/**
	 * Deregister agent from DF
	 */
	protected void takeDown() 
    {
       try { DFService.deregister(this); }
       catch (Exception e) {}
    }
	
	/**
	 * Register agent at DF
	 * @param serviceName
	 */
	protected void register(String serviceName) {
		DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd  = new ServiceDescription();
        dfd.setName( getAID() );  
        sd.setType( serviceName );
        sd.setName( getLocalName() );
        dfd.addServices(sd);

        try {  
            DFService.register(this, dfd );  
        } catch (FIPAException fe) { 
        	Util.logString(String.format("%s", fe.toString()), 30);
        }
	}
	
	/**
	 * Subscribe to subscription server
	 */
	private void subscribeToDistributor() {
		// Instantiate and set up Subscription Initiator behaviour
		msi = new MySubscriptionInit(this);		
		this.addBehaviour(msi);
		
		Util.logString(String.format("%s: Initiated request to subscribe with a Distributor", this.getLocalName()), 20);	
	}
	
	/**
	 * Initiate Contract NET behaviour
	 */
	private void addNCIBehaviour(ACLMessage msg) {
		// Instantiate and set up Contract Net Subscription Initiator behaviour
		ncf = new NegotiationClientFSM(this, msg);
		this.addBehaviour(ncf);
	}
	
	/**
	 * Initiate Notification Server behaviour
	 */
	private void addNSBehaviour(int subCnt) {
		// Instantiate and set up Notification Server FSM behaviour
		nsf = new NegotiationServerFSM(this, subCnt);
		this.addBehaviour(nsf);
	}
	
	/**
	 * Set up subscription server
	 */
	private void setUpSubscriptionServer() {
		// Instantiate and set up Subscription responder
		msr = new MySubscriptionResp(this);		
		this.addBehaviour(msr);
		
		// Log
    	Util.logString(String.format("%s: Subscritption server initialized", this.getLocalName()), 20); 
	}
	
    /**
	 * Subscription initiator - CLIENT
	 */
    class MySubscriptionInit extends SubscriptionInitiator {
    	
    	private static final long serialVersionUID = -7111020906826495420L;	
		
		MySubscriptionInit(Agent agent) {
            super(agent, new ACLMessage(ACLMessage.SUBSCRIBE));            
        }
			
		protected Vector<ACLMessage> prepareSubscriptions(ACLMessage subscription) {
			// Search for agents in the role of Distributor
			List<AID> agents;			
			AID server = null;
			try {
				agents = getAgentAIDs("Distributor");
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
        	AID sender = inform.getSender();
        	
        	switch(id) {
        	case "AUCTION_INIT":
        		// Compose initial bid data for CFP message
        		Double[] initialAuctionData;
        		initialAuctionData = extractInitMessage(inform.getContent());
        		String initialBids = composeInitialBid(initialAuctionData);
        		
        		// Compose CFP message
        		ACLMessage reply = new ACLMessage(ACLMessage.CFP);
        		reply.setContent(initialBids);
        		reply.addReceiver(sender);
        		
        		// Call method to set up behaviour for communicating CFP message
        		addNCIBehaviour(reply);
        		
        		break;
        	}
        }
        
        protected void handleRefuse(ACLMessage refuse) {
            // handle a refusal from the subscription service
        	Util.logString(getLocalName()+": Refusal handling from subscription service", 20);
        }

        protected void handleAccept(ACLMessage accept) {
            // handle an accept from the subscription service
        	Util.logString(getLocalName()+": Acceptance handling from subscription service", 20);
        }
    }
      
    /**
	 * Subscription responder - SERVER
	 */
    public class MySubscriptionResp extends SubscriptionResponder {

		private static final long serialVersionUID = 9044256482649608950L;
				
		MySubscriptionResp(Agent a) {
            super(a, MessageTemplate.and(                                       
            		MessageTemplate.or(
            				MessageTemplate.MatchPerformative(ACLMessage.SUBSCRIBE), 
            				MessageTemplate.MatchPerformative(ACLMessage.CANCEL)),                                       
            		MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_SUBSCRIBE))
            		);            
        }		
       
        protected ACLMessage handleSubscription(ACLMessage subscription_msg) throws NotUnderstoodException, RefuseException {
        	// Instantiate reply message
        	ACLMessage reply;
            
        	// Decision whether to accept or deny subscription request
        	if (subscription_msg.getPerformative() == ACLMessage.SUBSCRIBE) {
	            createSubscription(subscription_msg);
	            Util.logString(String.format("%s: %s successfully subscribed", getLocalName(), subscription_msg.getSender().getLocalName()), 20);
	            reply = new ACLMessage(ACLMessage.AGREE);
        	} else {
        		Util.logString(String.format("%s: %s subscription refused", getLocalName(), subscription_msg.getSender().getLocalName()), 20);
        		reply = new ACLMessage(ACLMessage.REFUSE);
        	}
            
			return reply;
        }
      
        protected void initiateNextAuctionRound(ACLMessage inform) {
        	// Set auction initialization message ontology
        	inform.setOntology("AUCTION_INIT");
        	
        	Util.logString(getLocalName()+": NEW AUCTION ROUND: "+ inform.getContent(), 20);    
        	
            // send notification to all subscribers
            Vector<?> subs = getSubscriptions();
            for(int i=0; i<subs.size(); i++) {
                ((SubscriptionResponder.Subscription)subs.elementAt(i)).notify(inform);
            }
        }        
      
    }   
    
    /**
     * Method for extracting initial data from Auction initiation message
     * @param initMsg
     * @return
     */
    private Double[] extractInitMessage(String initMsg) {    	
    	Double[] initialData = new Double[3];
    	String[] initialMessage = initMsg.split(";");
    	
    	String a = initialMessage[0].substring(initialMessage[0].indexOf("round ")+6);
    	String b = initialMessage[1].substring(initialMessage[1].indexOf("GCsP = ")+7);
    	String c = initialMessage[2].substring(initialMessage[2].indexOf("GCpP = ")+7);
    	
    	initialData[0] = Double.parseDouble(a);
    	initialData[1] = Double.parseDouble(b);
    	initialData[2] = Double.parseDouble(c);    	
    	
    	return initialData;
    }
    
    /**
     * Method for composing prosumer initial bid
     * @param initialData
     * @return
     */
    private String composeInitialBid(Double[] initialData) {
    	int roundNr = initialData[0].intValue();
    	BidSet bs = new BidSet();  
    	
		List<int[]> sheetData = new ArrayList<int[]>();
		List<String> readData = new ArrayList<String>();
		int index = roundNr % Util.nrOfDataRows;
		int colIndex = Util.getCharPosition(Util.firstDataCol.toCharArray()[0]);
		int[][] cellData = new int[Util.maxBidsInSet*2][2];
		
		for(int h=0;h<Util.maxBidsInSet;h++) {			
			cellData[h*2][0] = index+Util.firstDataRow-1;
			cellData[h*2][1] = colIndex+h*3;
			cellData[h*2+1][0] = index+Util.firstDataRow-1;
			cellData[h*2+1][1] = colIndex+1+h*3;
		}

		for(int i=0;i<cellData.length;i++) {
			sheetData.add(cellData[i]);
		}
		
		String sheetName = getLocalName();		
		
		try {
			readData = Util.readFromExcel(Util.experimentDataFilePath, sheetName, sheetData);
		} catch (IOException e) {
			e.printStackTrace();
		}			
		
		
		for(int j=0;j<(readData.size())/2;j++) {
			Double V = (readData.get(j*2) == "") ? 0.0 : Double.parseDouble(readData.get(j*2));
			Double C = (readData.get(j*2+1) == "") ? 0.0 : Double.parseDouble(readData.get(j*2+1));
			if (V != 0.0) {
				Double[] readValues = {V , C};
				Bid b = new Bid(readValues[0], readValues[1]);
				bs.bids.add(b);
				}
		}
		
		String logString = (String.format("%s: Presented bids [" , getLocalName()));
		String result = Integer.toString(roundNr)+";";
		for(int k=0;k<bs.bids.size();k++) {
			logString = logString + String.format("V=%.2f kW, C=%.3f €;" , bs.bids.get(k).V, bs.bids.get(k).C );
			result = result + String.format("%.2f %.3f;" , bs.bids.get(k).V, bs.bids.get(k).C );
		}
		logString = logString + "]";
		
    	Util.logString(logString, 20);  
    	
    	return result;    	
    }
  
    public void sendBids(String sender, String content) {
    	BidSet bs = new BidSet();
    	Bid temp;
    	String[] rawBids = content.split(";");
    	
    	int index = 0;
    	
    	for (String s: rawBids) {
    		if(index != 0) {
	    		try { 
	    			String[] t = s.replace(",", ".").split(" ");
	    			temp = new Bid(Double.parseDouble(t[0]), Double.parseDouble(t[1]));
	    			bs.bids.add(temp);	
	    		} catch (Exception e) {
	    			System.out.println(e);
	    		}
    		}
    		index++;
    	}
    	ac.presentBid(sender, bs);    	
    }
    
    public String getOffer(String sender, String content) {
    	int rndNr;
    	String[] rawBids = content.split(";");
    	
    	rndNr = Integer.parseInt(rawBids[0]);
    	
    	Bid res = ac.getOffer(sender, rndNr);
    	String result = String.format("%.2f %.3f",res.V, res.C);
    	
    	return result;
    	
    }
    
    public boolean negotiationReady() {
    	return ac.negotiationDone;
    }

    public boolean auctionRoundReady() {
    	return ac.auctionRoundDone;
    }
}