package mg;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
		
	private String role = Util.prosumerRoles[0];
	private Auction ac;
	private boolean bidOriginManual = false;
	private boolean sendBid = false;
	private boolean acceptOffer = false;
	private boolean rejectOffer = false;
	private BidSet currentBidSet;
	private int currentAuctionRound;
	
	@SuppressWarnings("unused")
	private Bid currentOffer;
	
	private RunShell rsGui;
	transient protected PrsmrGUI myGui;
	
	Distributor db = new Distributor(this, Util.auctionCycle);
	AuctionInitiator msi;
	NegotiationClientFSM ncf;
	AuctionResponder msr;
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
		rsGui = (RunShell)args[0];
		rsGui.createShell(getName(), this);
		
		// Run behavior for registering agents at Simulation Communication agent
		Register rg = new Register();
		addBehaviour(rg);		
				
	} // End setup
	
	/**
	 * Behaviour for registering agents at Simulation Communication agent
	 * @author Tarmo
	 */
	private class Register extends OneShotBehaviour {
		
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
	private class Distributor extends CyclicBehaviour {	
		private static final long serialVersionUID = 7387677922296356597L;
		
		private long 	timeout, wakeupTime;
		   
		public Distributor(Agent a, long timeout) {
			super(a);
			this.timeout = timeout;
		}
		   
		public void onStart() {
			wakeupTime = System.currentTimeMillis() + timeout;
			handleElapsedTimeout();
		}
		
		public void action() {
			long dt = wakeupTime - System.currentTimeMillis();

			if (dt <= 0) {
				handleElapsedTimeout();
			}   
	   }
		   
		protected void handleElapsedTimeout() { 
			if (role.equals("Distributor")) {
				executeNextAuctionRound();	
				   wakeupTime = System.currentTimeMillis() + timeout;			
			}
	   } 
		
		public void next() {
			executeNextAuctionRound();
			wakeupTime = System.currentTimeMillis() + timeout;
		}
		
		void executeNextAuctionRound() {
			// Start next auction round and send notification of new auction round to all subscribed participants
			ACLMessage initMsg = ac.startNewRound();
			msr.initiateNextAuctionRound(initMsg);
			String msg[] = initMsg.getContent().split(";");
			String a = msg[0].substring(msg[0].indexOf("round ")+6);
			currentAuctionRound = Integer.parseInt(a);
			updateGuiRoundInfo("Auction round "+a);
			addNSBehaviour(msr.getSubscriptions().size(), currentAuctionRound);
			ac.setNrOfSubscribers(msr.getSubscriptions().size());			
		}
	}
	
	/********************************************************************************
     * AGENT COMMUNICATION INNER CLASSES AND METHODS ********************************
     ********************************************************************************/
	
	/**
	 * Initiate Negotiation Client behaviour
	 */
	void addNCBehaviour(ACLMessage msg) {
		// Instantiate and set up Contract Net Subscription Initiator behaviour
		ncf = new NegotiationClientFSM(this, msg);
		this.addBehaviour(ncf);
	}
	
	/**
	 * Initiate Negotiation Server behaviour
	 */
	private void addNSBehaviour(int subCnt, int auctionRound) {
		// Instantiate and set up Notification Server FSM behaviour
		nsf = new NegotiationServerFSM(this, subCnt, auctionRound);
		this.addBehaviour(nsf);
	}
	
	/**
	 * Set up auction responder - SERVER
	 */
	private void setUpSubscriptionServer() {
		// Instantiate and set up Subscription responder
		msr = new AuctionResponder(this);		
		this.addBehaviour(msr);
		
		// Log
    	Util.logString(String.format("%s: Subscritption server initialized", this.getLocalName()), 20); 
	}
	
	/**
	 * Subscribe to auction responder -> SERVER
	 */
	private void subscribeToDistributor() {
		// Instantiate and set up Subscription Initiator behaviour
		msi = new AuctionInitiator(this);		
		this.addBehaviour(msi);
		
		Util.logString(String.format("%s: Initiated request to subscribe with a Distributor", this.getLocalName()), 20);	
	}
	
    /********************************************************************************
     * AGENT COMMUNICATION INNER CLASSES AND METHODS END ****************************
     ********************************************************************************/
        
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
    	initialData[1] = Double.parseDouble(b.replace(",", "."));
    	initialData[2] = Double.parseDouble(c.replace(",", "."));    	
    	
    	return initialData;
    }
    
    /**
     * Method for composing prosumer initial bid
     * @param initialData
     * @return
     */
    private BidSet composeInitialBid(Double[] initialData) {
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
    		
		// Update GUI with initial bids and round info
		//updateGuiRoundInfo(String.format("Auction round %s", roundNr));
    	//updateGuiTable(bs);    
		
    	// Log
    	//Util.logString(logString, 20); 	
    	
    	return bs;    	
    }
    
    /**
     * Method for correcting prosumer bid
     * @param initialData
     * @return
     */
    public String composeCorrectedBid(String initialBids, String presentedOffer) {
    	String[] initialBidsSplit = initialBids.split(";");
    	int roundNr = Integer.parseInt(initialBidsSplit[0]);
    	BidSet initialBidSet = new BidSet();
    	for(int i=0;i<(initialBidsSplit.length-1)/2;i++) {
    		initialBidSet.bids.add(new Bid(Double.parseDouble(initialBidsSplit[i*2 + 1]), Double.parseDouble(initialBidsSplit[i*2 + 2])));
    	}
    	BidSet bs = new BidSet();  
    	
		// TODO correct bids
    	bs = initialBidSet;
		
		String logString = (String.format("%s: Corrected bids [" , getLocalName()));
		String result = Integer.toString(roundNr)+";";
		for(int k=0;k<bs.bids.size();k++) {
			logString = logString + String.format("V=%.2f kW, C=%.3f €;" , bs.bids.get(k).V, bs.bids.get(k).C );
			result = result + String.format("%.2f %.3f;" , bs.bids.get(k).V, bs.bids.get(k).C );
		}
		logString = logString + "]";
		
    	Util.logString(logString, 20);  
    	
    	return result;    	
    }
  
    /**
     * Public method to send bids for running auction
     * @param sender
     * @param content
     */
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
    
    /**
     * Public method to get prosumer offers from auction results
     * @param sender
     * @param content
     * @return
     */
    public String getOffer(String sender, String content) {
    	int rndNr;
    	String[] rawBids = content.split(";");
    	
    	rndNr = Integer.parseInt(rawBids[0]);
    	
    	Bid res = ac.getOffer(sender, rndNr);
    	String result = String.format("%.2f %.3f",res.V, res.C);
    	
    	return result;    	
    }
    
    /**
     * Public method to check if auction negotiation round is finished
     * @return
     */
    public boolean negotiationReady() {
    	return ac.negotiationDone;
    }

    /**
     * Public method to check if auction round is finished
     * @return
     */
    public boolean auctionRoundReady() {
    	return ac.auctionRoundDone;
    }
    
    public void outputAgentName() {
    	System.out.println("this is my agent name:"+getLocalName());
    }
    
    public boolean getBidOrigin() {
    	return bidOriginManual;
    }
    
    public void setInitialBidFromFile(String initMsg) {
    	Double[] initialAuctionData;
		initialAuctionData = extractInitMessage(initMsg);
		BidSet bs = composeInitialBid(initialAuctionData);	
    	updateGuiTable(bs);
    }
    
    public void setInitialBidManually(String initMsg) {
    	Double[] temp = {0.0, 0.0};
    	updateGuiTable(new BidSet(temp));
    }
        
    public String getBidSetFromGui() {
    	//String result = "Test 1 2 3";
		String result = Integer.toString(currentAuctionRound)+";";
		String logString = (String.format("%s: Presented bids [" , getLocalName()));
    	for(int k=0;k<currentBidSet.bids.size();k++) {
			result = result + String.format("%.2f %.3f;" , currentBidSet.bids.get(k).V, currentBidSet.bids.get(k).C );
			logString = logString + String.format("V=%.2f kW, C=%.3f €;" , currentBidSet.bids.get(k).V, currentBidSet.bids.get(k).C );
		}
		logString = logString + "]"; 
    	
    	Util.logString(logString, 20); 	
    	
    	return result;
    }
    
    public void updateCurrentOffer(String offer, int currentNegotiationRound, int maxNegotiationRound) {
    	String[] s = offer.split(" ");
    	Double V = 0.0;
    	Double C = 0.0;
    	
    	try {
    		V = Double.parseDouble(s[0].replace(",","."));
    		C = Double.parseDouble(s[1].replace(",","."));
    	} catch (Exception e) {
    		System.out.println("Incorrect number format for Double");
    	}
    	
    	Bid b = new Bid(V, C);
    	currentOffer = b;
    	
    	String a = String.format("OFFER: %.2f kW @ %.3f €", b.V, b.C);
    	updateGuiPresentedOffer(a);
    	updateGuiNegotiationInfo(String.format("Negotiation round %s/%s", currentNegotiationRound, maxNegotiationRound));
    }
    
    public void updateRoundInfo(String newRoundInfo) {
    	Double[] initData = extractInitMessage(newRoundInfo);
    	String text = String.format("R: %d; G: buys @ %.3f €; sells @ %.3f €", initData[0].intValue(), initData[1], initData[2]);
    	updateGuiRoundInfo(text);
    }
    
    public void updateDealInfo(String dealInfo) {
    	String[] deal = dealInfo.split(" ");
    	String text = String.format("Next round: %s kW @ %s", deal[0], deal[1]);
    	updateGuiPccSP(text);
    }
    
    /********************************************************************************
     * GUI INTERACTION METHODS ******************************************************
     ********************************************************************************/

	/**
	 * GUI event handlers
	 */
	@Override
	protected void onGuiEvent(GuiEvent ge) {
		int event = ge.getType();
		//TODO - GUI handlers
		switch(event) {
		case Util.GUI_MSG_ROLE: // change role
			String rl = (String)ge.getParameter(0);
			setRole(rl);
			break;
		case Util.GUI_MSG_BID_ORIGIN: // change bid origin
			String origin = (String)ge.getParameter(0);
			if (origin.equals("Manual")) {
				bidOriginManual = true;
			} else {
				bidOriginManual = false;
			}			
			break;
		case Util.GUI_MSG_SEND_BID: // handle send bid command
			currentBidSet = (BidSet)ge.getParameter(0);
			sendBid = true;
			break;
		case Util.GUI_MSG_REJECT: // handle reject message command
			rejectOffer = true;
			break;
		case Util.GUI_MSG_ACCEPT: // handle accept message command
			acceptOffer = true;
			break;
		case Util.GUI_MSG_NEW_ROUND: // handle new auction round command
			db.next();
			break;
		}		
	}
	
    /**
     * Internal method for updating GUI table
     * @param bs
     */    
    private void updateGuiTable(BidSet bs) {
    	rsGui.getDisplay().asyncExec(new Runnable() {
            public void run() {
        		rsGui.updateTable(getName(),bs);            	
            }
    	});
    }
    
    /**
     * Internal method for updating GUI PCC SP display
     * @param pccSP
     */
    private void updateGuiPccSP(String nextRoundSp) {
    	rsGui.getDisplay().asyncExec(new Runnable() {
            public void run() {
        		rsGui.updatePCCsetpoint(getName(), nextRoundSp);            	
            }
    	});
    }
    
    /**
     * Internal method for updating GUI round info display
     * @param roundInfo
     */
    private void updateGuiRoundInfo(String roundInfo) {
    	rsGui.getDisplay().asyncExec(new Runnable() {
            public void run() {
        		rsGui.updateRoundInfo(getName(), roundInfo);           	
            }
    	});
    }
    
    /**
     * Internal method for updating GUI negotiation info display
     * @param negotiationInfo
     */
    private void updateGuiNegotiationInfo(String negotiationInfo) {
    	rsGui.getDisplay().asyncExec(new Runnable() {
            public void run() {
        		rsGui.updateNegotiationInfo(getName(), negotiationInfo);           	
            }
    	});
    }
    
    /**
     * Internal method for updating GUI presented offer display
     * @param presentedOffer
     */
    private void updateGuiPresentedOffer(String presentedOffer) {
    	rsGui.getDisplay().asyncExec(new Runnable() {
            public void run() {
        		rsGui.updatePresentedOffer(getName(), presentedOffer);           	
            }
    	});
    }

    /**
     * Internal method for updating GUI distributor container display
     * @param visible
     */
    private void updateGuiDisplayDistributor(boolean visible) {
    	rsGui.getDisplay().asyncExec(new Runnable() {
            public void run() {
        		rsGui.updateDisplayDistributor(getName(), visible);           	
            }
    	});
    }
    
    /**
     * Internal method for updating GUI participant container display
     * @param visible
     */
    private void updateGuiDisplayParticipant(boolean visible) {
    	rsGui.getDisplay().asyncExec(new Runnable() {
            public void run() {
        		rsGui.updateDisplayParticipant(getName(), visible);           	
            }
    	});
    }
    
    /********************************************************************************
     * GUI INTERACTION METHODS END **************************************************
     ********************************************************************************/

    public boolean getSendStatus() {
    	return sendBid; 
    }
    
    public void resetSendStatus() {
    	sendBid = false;
    }
    
    public boolean getAcceptStatus() {
    	return acceptOffer;
    }
    
    public void resetAcceptStatus() {
    	acceptOffer = false;
    }
    
    public boolean getRejectStatus() {
    	return rejectOffer;
    }
    
    public void resetRejectStatus() {
    	rejectOffer = false;
    }        	

	/********************************************************************************
	 * AUXILIARY METHODS ************************************************************
	 ********************************************************************************/
    
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
	 * Terminate agent
	 */
	public void killAgent( ) {
		String name = this.getName();
		this.takeDown();
		this.doDelete();
		Util.logString(name+": Agent terminated.", 20);
	}
	
	/**
	 * Random bid generator
	 * @return
	 */
	@SuppressWarnings("unused")
	private List<Double> composeRandomBid() {
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
			
			// Display distributor container and hide participant container in GUI
			updateGuiDisplayDistributor(true);
			updateGuiDisplayParticipant(false);
			
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

			// Hide distributor container in GUI
			updateGuiDisplayDistributor(false);
			
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

	/********************************************************************************
	 * AUXILIARY METHODS END ********************************************************
	 ********************************************************************************/
}