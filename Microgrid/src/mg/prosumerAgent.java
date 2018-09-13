package mg;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetInitiator;
import jade.proto.ContractNetResponder;
import jade.proto.SubscriptionInitiator;
import jade.proto.SubscriptionResponder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import gui.PrsmrGUI;
import gui.RunShell;

import mg.BidSet.Bid;

/**
 * This class is a Prosumer intelligent agent. 
 * @author tarmokorotko
 * forked from gitHub user rroche
 */
public class prosumerAgent extends GuiAgent
{
	transient protected PrsmrGUI myGui;
	private static final long serialVersionUID = 195263862L;
	private String role = Util.prosumerRoles[0];
	//private Double Sp = 0.0;
	//private Auction ac = new Auction();
	
	/**
	 * Initialization of agent
	 */
	protected void setup() 
	{
		
		Util.logString(getName() + " successfully started", 20);
		
		//List<Double> bids = composeBid();
		
		// Get arguments
		Object[] args = getArguments();
		
		// Instantiate and open GUI
		RunShell gui = (RunShell)args[0];
		gui.createShell(getName(), this);		
				
		// Run behavior
		Register rg = new Register();
		addBehaviour(rg);
		
		Distributor db = new Distributor(this, 1000);
		addBehaviour(db);
		
	} // End setup
	
	/**
	 * Behaviour for registering agents at Simulation Communication agent
	 * @author Tarmo
	 *
	 */
	class Register extends OneShotBehaviour {
		
		private static final long serialVersionUID = 9288461912L;
		
		public void action()
		{
			sendMessage("simComAgent", "Register me","INIT",ACLMessage.SUBSCRIBE);
			Util.logString("Agent registered", 20);
		}
	}
	
	/**
	 * Behaviour of Distributor role
	 * @author Tarmo
	 *
	 */
	class Distributor extends TickerBehaviour {

		public Distributor(Agent a, long period) {
			super(a, period);
		}

		private static final long serialVersionUID = 7387677922296356597L;

		@Override
		protected void onTick() {
			if (role.equals("Distributor")) {
				
				//ac.startNewRound();
			} else {
				
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
        
	    Util.logString(String.format("All registered agents: %s", agents), 20);
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
			Util.logString((this.getName()+String.format(": value changed to %s", ge.getParameter(0))), 20);
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
		case "Distributor": // if new role is Distributor, register agent as distributor
			this.register(role);
			setUpSubscriptionServer();
			break;
			
		default: // default action is to subscribe to a distributor
			subscribeToDistributor();
			break;
		}			
				
		Util.logString(String.format(this.getLocalName()+" role changed to %s", role),20);
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
		MySubscriptionInit msi = new MySubscriptionInit(this);		
		this.addBehaviour(msi);
		
		Util.logString(String.format("%s subscribed to server", this.getLocalName()), 20);	
	}
	
	/**
	 * Initiate Contract NET behaviour
	 */
	private void addCNETiBehaviour(ACLMessage msg) {
		// Instantiate and set up Contract Net Subscription Initiator behaviour
		MyCNETInitiator mCNETi = new MyCNETInitiator(this, msg);
		this.addBehaviour(mCNETi);
	}
	
	/**
	 * Set up subscription server
	 */
	private void setUpSubscriptionServer() {
		int auctionRound = 1;
		Agent agent = this;
		
		// Instantiate and set up Subscription responder and Contract Net Subscription responder
		MySubscriptionResp msr = new MySubscriptionResp(agent);
		MyCNETResponder mCNETr = new MyCNETResponder(agent);
		
		this.addBehaviour(msr);
		this.addBehaviour(mCNETr);
		
		// Set up periodically executed task for Auction initialization
		Timer timer = new Timer();
		timer.schedule(new InitAuctionRound(msr, auctionRound), 0, Util.auctionCycle);
		
    	Util.logString(String.format("%s set up subscritption server", this.getLocalName()), 20); 
	}
	
	/**
	 * Periodic task for initiating auction round
	 */
	class InitAuctionRound extends TimerTask {
		MySubscriptionResp msr;		
		int i;
		
		InitAuctionRound(MySubscriptionResp initMsr, int roundNr) {
			msr = initMsr;
			i = roundNr;
		}
		
		public void run() {			
			// Get values for current auction round initialization
			Double[] initValues = getAuctionInitValues(i);
			
			// Compose Auction initialization message
			ACLMessage initMsg = new ACLMessage(ACLMessage.INFORM);
			initMsg.setContent(String.format("Start of round %s; GCsP = %s; GCpP = %s", i, initValues[0], initValues[1]));
			initMsg.setOntology("AUCTION_INIT");
			
			// Send notification of new auction round to all subscribed participants
			msr.initiateNextAuctionRound(initMsg);
			
			// Increase auction round counter
			i++;
		}
		
		/**
		 * Method for composing initial values for auction round
		 * @return
		 */
		private Double[] getAuctionInitValues(int roundNr) {			
			int index;			
			index = roundNr % Util.nrOfDataRows;
			
			List<int[]> sheetData = new ArrayList<int[]>();
			List<String> readData = new ArrayList<String>();
			int[] cellData1 = {index+1, 7};
			int[] cellData2 = {index+1, 8};
			sheetData.add(cellData1);
			sheetData.add(cellData2);
			try {
				readData = Util.readFromExcel(Util.experimentDataFilePath, "PCC", sheetData);
			} catch (IOException e) {
				e.printStackTrace();
			}			
			
			Double[] otherValues = {Double.parseDouble(readData.get(0)), Double.parseDouble(readData.get(1))};		
			
			//return gridValues[index];
			return otherValues;
		}
		
		
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
        		addCNETiBehaviour(reply);
        		
        		break;
        	}
        }
        
        protected void handleRefuse(ACLMessage refuse) {
            // handle a refusal from the subscription service
        	Util.logString("Refusal handling from subscription service", 20);
        }

        protected void handleAccept(ACLMessage accept) {
            // handle an accept from the subscription service
        	Util.logString("Acceptance handling from subscription service", 20);
        }
    }
    
    /** 
     * ContractNET initiator - CLIENT
     */
    public class MyCNETInitiator extends ContractNetInitiator {

    	private static final long serialVersionUID = 8014843266916342564L;
    	
		public MyCNETInitiator(Agent a, ACLMessage cfp) {
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
			
			// Send accept or reject messages
			if(acceptProposal(propose.getContent())) {
				reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
			} else {
				reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
			}
			
			// add reply to acceptances vector
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
				if(msg.getPerformative() != ACLMessage.PROPOSE ||
						msg.getPerformative() != ACLMessage.CFP ||
						msg.getPerformative() != ACLMessage.REFUSE ||
						msg.getPerformative() != ACLMessage.FAILURE ||
						msg.getPerformative() != ACLMessage.INFORM) {
					System.out.println("Received unhandled response "+ msg.getContent() +";"+ msg.getPerformative() +"  from "+msg.getSender().getLocalName());
				}
			}
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
	            Util.logString(String.format("Agent %s successfully subscribed", subscription_msg.getSender().getLocalName()), 20);
	            reply = new ACLMessage(ACLMessage.AGREE);
        	} else {
        		Util.logString(String.format("Agent %s subscription refused", subscription_msg.getSender().getLocalName()), 20);
        		reply = new ACLMessage(ACLMessage.REFUSE);
        	}
            
			return reply;
        }
      
        protected void initiateNextAuctionRound(ACLMessage inform) {
        	// Set auction initialization message ontology
        	inform.setOntology("AUCTION_INIT");
        	
        	Util.logString("---- NEW AUCTION ROUND: "+ inform.getContent() +" ----", 20);    
        	
            // send notification to all subscribers
            Vector<?> subs = getSubscriptions();
            for(int i=0; i<subs.size(); i++) {
                ((SubscriptionResponder.Subscription)subs.elementAt(i)).notify(inform);
            }
        }        
      
    }    
    
    /**
     * ContractNET responder - SERVER
     */
    public class MyCNETResponder extends ContractNetResponder {
    	private Agent myAgent;
    	private static final long serialVersionUID = -1211425255707453021L;
		
		public MyCNETResponder(Agent a) {
			super(a, MessageTemplate.or(
					MessageTemplate.MatchPerformative(ACLMessage.CFP), 
					MessageTemplate.MatchPerformative(ACLMessage.INFORM)));
			myAgent = a;
		}
		
		protected ACLMessage handleCfp(ACLMessage cfp) throws RefuseException, FailureException, NotUnderstoodException {
			// Set up proposal message
			ACLMessage reply = new ACLMessage(ACLMessage.PROPOSE);
			reply.addReceiver(cfp.getSender());
			reply.setConversationId(cfp.getConversationId());
			
			// Get offer and send it to participant
			String offer = getOffer(myAgent);
			reply.setContent(offer);					
			
			return reply;
		}

		protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) throws FailureException {
			Util.logString(getLocalName()+": Agent "+propose.getSender()+" proposal accepted", 20);
			ACLMessage reply = accept.createReply();
			reply.setContent("CONFIRMED");
			
			return reply;
		}
		
		protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
			Util.logString(getLocalName()+": Agent "+propose.getSender()+" proposal rejected", 20);
			
			// If proposal gets rejected, bids are recalculated
			recalculateBids(propose);			
		}
		
		protected void handleOutOfSequence(ACLMessage cfp,ACLMessage propose, ACLMessage msg){
			System.out.println("Agent "+getLocalName()+" received Out of Sequence message: " + msg.getContent()+" with performative "+ msg.getPerformative());
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
				Bid b = bs.new Bid(readValues[0], readValues[1]);
				bs.bids.add(b);
				}
		}
		
		String logString = (String.format("%s presents bids [" , getLocalName()));
		String result = "";
		for(int k=0;k<bs.bids.size();k++) {
			logString = logString + String.format("V=%.2f kW, C=%.3f €;" , bs.bids.get(k).V, bs.bids.get(k).C );
			result = result + String.format("%.2f,%.3f;" , bs.bids.get(k).V, bs.bids.get(k).C );
		}
		logString = logString + "]";
		
    	Util.logString(logString, 20);  
    	
    	return result;    	
    }
    
    //TODO - bid recalculation logic
    private void recalculateBids(ACLMessage msg) {
    	Util.logString("Recalculating bids", 20);
    	
    	ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
    	cfp.setContent("Corrected bids: ");
    	cfp.addReceiver(msg.getSender());
    	addCNETiBehaviour(cfp);
    }
    
    //TODO - Proposal acceptance logic
    private boolean acceptProposal(String s) {
		long millis = System.currentTimeMillis();
		
    	return (millis % 2) == 0;
    }
    
    //TODO - synchronous offer presentation logic
    private String getOffer(Agent a) {
    	String result = "1 2 3";
    	
    	return result;
    }
}
