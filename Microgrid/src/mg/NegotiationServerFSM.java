package mg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.DataStore;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class NegotiationServerFSM extends FSMBehaviour {

	private static final long serialVersionUID = -1172139205022014883L;
	private static final int delay = 500;
	
	protected static final String RECEIVE_PROPOSALS = "Receive-Proposal";
	protected static final String CHECK_ALL_PROPOSALS_RECEIVED = "All-Proposals-Received-Check";
	protected static final String INSERT_BIDS = "Insert-Bids-To-Auction";
	protected static final String CHECK_OFFERS_AVAILABLE = "Offer-Available-Check";
	protected static final String SEND_OFFERS = "Send-offers";
	protected static final String RECEIVE_REPLIES = "Receive-Accept-Refuse";
	protected static final String CHECK_ALL_REPLIES_RECEIVED = "All-Replies-Received-Check";
	protected static final String FINALIZE = "Finalize-negotiation";
	
	private static HashMap<AID, String> bids;
	private static HashMap<AID, String> offers;
	private static List<ACLMessage> replies;
	private static HashMap<AID, String> auctionLedger;
	
	private int subCnt;
	private int auctionRound;
	
	public NegotiationServerFSM(Agent a, int subscriptionCnt, int currentAuctionRound) {
		super(a);
		subCnt = subscriptionCnt;
		bids = new HashMap<AID, String>();
		auctionLedger = new HashMap<AID, String>();
		auctionRound = currentAuctionRound;
		
		// FSM state transitions
		registerDefaultTransition(RECEIVE_PROPOSALS, CHECK_ALL_PROPOSALS_RECEIVED);
		registerTransition(CHECK_ALL_PROPOSALS_RECEIVED,RECEIVE_PROPOSALS,0);
		registerTransition(CHECK_ALL_PROPOSALS_RECEIVED,INSERT_BIDS,1);
		registerTransition(CHECK_ALL_PROPOSALS_RECEIVED,FINALIZE,-1);
		registerDefaultTransition(INSERT_BIDS, CHECK_OFFERS_AVAILABLE);
		registerTransition(CHECK_OFFERS_AVAILABLE, SEND_OFFERS, 1);
		registerTransition(CHECK_OFFERS_AVAILABLE, CHECK_OFFERS_AVAILABLE, 0);
		registerDefaultTransition(SEND_OFFERS, RECEIVE_REPLIES);
		registerDefaultTransition(RECEIVE_REPLIES, CHECK_ALL_REPLIES_RECEIVED);
		registerTransition(CHECK_ALL_REPLIES_RECEIVED,RECEIVE_REPLIES,0);
		registerTransition(CHECK_ALL_REPLIES_RECEIVED,INSERT_BIDS,1);
		registerTransition(CHECK_ALL_REPLIES_RECEIVED,FINALIZE,-1);
		
		Behaviour b;
		DataStore ds = getDataStore();
		
		// RECEIVE PROPOSALS
		b = new ReceiveProposal(myAgent);
		b.setDataStore(ds);
		registerFirstState(b, RECEIVE_PROPOSALS);		
		
		// CHECK IF ALL PROPOSALS RECEIVED
		b = new CheckProposals(myAgent, bids, subCnt);
		b.setDataStore(ds);
		registerState(b, CHECK_ALL_PROPOSALS_RECEIVED);
		
		// PRESENT BIDS TO AUCTION
		b = new ComposeOffers(myAgent, bids);
		b.setDataStore(ds);
		registerState(b, INSERT_BIDS);
		
		// CHECK IF AUCTION COMPLETE
		b = new CheckAuctionComplete(myAgent, bids);
		b.setDataStore(ds);
		registerState(b, CHECK_OFFERS_AVAILABLE);
		
		// SEND OFFERS IN RESPONSE TO PROPOSALS
		b = new SendOffers(myAgent);
		b.setDataStore(ds);
		registerState(b, SEND_OFFERS);
		
		// RECIEVE ACCEPT OR REJECT REPLIES TO OFFERS
		b = new ReplyHandler(myAgent);
		b.setDataStore(ds);
		registerState(b, RECEIVE_REPLIES);
		
		// CHECK IF ALL REPLIES RECEIVED
		b = new CheckReplies(myAgent);
		b.setDataStore(ds);
		registerState(b, CHECK_ALL_REPLIES_RECEIVED);
		
		// FINALIZE NEGOTIATION AND TERMINATE BEHAVIOUR
		b = new FinalizeNegotiation(myAgent);
		b.setDataStore(ds);
		registerLastState(b, FINALIZE);
	}
	
	/**
	* Inner class ProposalHandler
	*/
	private class ReceiveProposal extends OneShotBehaviour {
		private static final long serialVersionUID = 4762407563773002L;
		
		public ReceiveProposal(Agent a) {
			super(a);
		}		

		public void action() {
			ACLMessage msg = myAgent.receive(MessageTemplate.and(
												MessageTemplate.MatchPerformative(ACLMessage.CFP), 
												MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_PROPOSE)
												));
			if(msg != null) {
				// if bid is received, insert it to the bids hashmap
				bids.put(msg.getSender(), msg.getContent());
			} 
		}
	} // End of inner class ProposalHandler 
	
	/**
	Inner class CheckProposals
	*/
	private class CheckProposals extends OneShotBehaviour {
		private static final long     serialVersionUID = 4762407563773002L;

		private HashMap<AID, String> locProposals;
		private int subCnt;		
		private int ret;
		
		public CheckProposals(Agent a, HashMap<AID, String> prpsls, int subscribedCnt) {
			super(a);
			locProposals = prpsls;
			subCnt = subscribedCnt;
		}
		
		public void action() {	
			ret = 0;
			
			if(subCnt == 0) {
				// If number of subscribers is 0, terminate
				ret = -1;
			} else {
				// If number all proposals received, move to the next state
				ret = (locProposals.size() == subCnt) ? 1 : 0; 
			} 						
		}		
		
		public int onEnd() {
			return ret;
		}
	} // End of inner class CheckProposals 
	
	/**
	Inner class ComposeOffers
	*/
	private class ComposeOffers extends OneShotBehaviour {
		private static final long     serialVersionUID = 4762407563773002L;

		private HashMap<AID, String> locProposals;
		
		public ComposeOffers(Agent a, HashMap<AID, String> prpsls) {
			super(a);
			locProposals = prpsls;
			offers = new HashMap<AID, String>();
		}
		
		public void action() {		
			// iterate through all bid sets and insert them into the auction
			for(Map.Entry<AID, String> kvPair : locProposals.entrySet()) {
				((ProsumerAgent) getAgent()).sendBids(kvPair.getKey().getLocalName(), kvPair.getValue());
			}
		}		
	} // End of inner class ComposeOffers

	/**
	Inner class CheckAuctionComplete
	*/
	private class CheckAuctionComplete extends OneShotBehaviour {
		private static final long     serialVersionUID = 4762407563773002L;

		private HashMap<AID, String> locProposals;
		
		public CheckAuctionComplete(Agent a, HashMap<AID, String> prpsls) {
			super(a);
			locProposals = prpsls;
		}
		
		public void action() {
			// Check if auction negotiation round is complete
			if(!((ProsumerAgent) getAgent()).negotiationReady() ) {
				System.out.println("Calculating...");
				myAgent.doWait(delay);
			} else {	
				// If negotiation round is complete, insert all offers into hashmap
				for(Map.Entry<AID, String> kvPair : locProposals.entrySet()) {
					String offer = (((ProsumerAgent) getAgent()).getOffer(kvPair.getKey().getLocalName(), String.valueOf(auctionRound)));
					offers.put(kvPair.getKey(), offer);
				}
			}	
		}

		public int onEnd() {
			return ((ProsumerAgent) getAgent()).negotiationReady() ? 1 : 0;
		}
	} // End of inner class CheckAuctionComplete
	
	/**
	Inner class SendOffers
	*/
	private class SendOffers extends OneShotBehaviour {
		private static final long     serialVersionUID = 4762407563773002L;
		
		public SendOffers(Agent a) {
			super(a);
		}
		
		public void action() {	
			// Compose offer message
			ACLMessage offer = new ACLMessage(ACLMessage.PROPOSE);
			offer.setProtocol(FIPANames.InteractionProtocol.FIPA_PROPOSE);
			
			// Send offers
			for(Map.Entry<AID, String> kvPair : offers.entrySet()) {
				offer.setContent(kvPair.getValue());
				offer.addReceiver(kvPair.getKey());
				myAgent.send(offer);
			}
		}
	} // End of inner class SendOffers
		
	/**
	Inner class ReplyHandler
	*/
	private class ReplyHandler extends OneShotBehaviour {
		private static final long serialVersionUID = 4762407563773002L;
		
		public ReplyHandler(Agent a) {
			super(a);
			replies = new ArrayList<ACLMessage>();
		}
		
		public void action() {			
			ACLMessage msg = myAgent.receive(MessageTemplate.and(
					MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL),
										MessageTemplate.MatchPerformative(ACLMessage.REJECT_PROPOSAL)),
					MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_PROPOSE)
					));
			if(msg != null) {	
				// Add received reply to list
				replies.add(msg);
			} 						
		}
	} // End of inner class ReplyHandler
	
	/**
	Inner class CheckReplies
	*/
	private class CheckReplies extends OneShotBehaviour {
		private static final long serialVersionUID = 4762407563773002L;
		
		private int ret;
		
		public CheckReplies(Agent a) {
			super(a);
		}
		
		public void action() {
			ret = 0;
			
			// Send replies
			for(ACLMessage msg : replies) {
				if(msg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
					// If reply is accept, then store offer in ledger
					auctionLedger.put(msg.getSender(), msg.getContent().split(";")[1]);
				} else if(msg.getContent().split(";").length == 2) {
					// If reply is xx;xx, store into ledger
					auctionLedger.put(msg.getSender(), msg.getContent().split(";")[1]);
				} 
			}
			
			// If auction ledger size equals subscribed user count, terminate auction
			//System.out.println(String.format("Ledger size %s ?= %s subscribed count", auctionLedger.size(), subCnt));
			if(auctionLedger.size() == subCnt) {
				ret = -1;
			}			
		}
		
		public int onEnd() {
			return ret;
		}
	} // End of inner class CheckReplies 
		
	/**
	Inner class FinalizeNegotiation
	*/
	private class FinalizeNegotiation extends OneShotBehaviour {
		private static final long     serialVersionUID = 4762407563773002L;
		
		public FinalizeNegotiation(Agent a) {
			super(a);
		}
		
		public void action() {
			if(auctionLedger.size() != 0) {
				// Compose string of auction results
				String logString = "AUCTION ROUND RESULTS ";
				for(Map.Entry<AID, String> deals : auctionLedger.entrySet()) {
					logString = logString + deals.getKey().getLocalName() + ": " + deals.getValue()+"; ";
				}
				// Log
				Util.logString(logString);	
			}
		}
	} // End of inner class FinalizeNegotiation 
} 