package mg;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jade.lang.acl.ACLMessage;
import mg.BidSet.Bid;

public class Auction implements AuctionInterface {
	public boolean negotiationDone = false;
	public boolean auctionRoundDone = false;
	public boolean offersReady = false;
	
	private int roundNr = 0;
	private int R = 0;
	
	private int subscriptionClients;
	
	private Map<Integer, Map<String, BidSet.Bid>> offerSet = new HashMap<Integer, Map<String, BidSet.Bid>>();
	
	private Map<Integer, Map<Integer, Map<String, BidSet>>> ledger = new HashMap<Integer, Map<Integer, Map<String, BidSet>>>();
	private Map<Integer, Map<String, BidSet>> roundLedger = new HashMap<Integer, Map<String, BidSet>>();
	private Map<String, BidSet> bidsetLedger = new HashMap<String, BidSet>();
	
	public ACLMessage startNewRound() {
		// Get initialization values for current auction round
		Double[] initValues = getAuctionInitValues(roundNr);
		
		// Compose Auction initialization message
		ACLMessage initMsg = new ACLMessage(ACLMessage.INFORM);
		
		initMsg.setContent(String.format("Start of round %s; GCsP = %s; GCpP = %s", roundNr, initValues[0], initValues[1]));
		initMsg.setOntology("AUCTION_INIT");
		
		// Increase round iterator count
		roundNr++;
		
		return initMsg;
	}

	public void presentBid(String agentName, BidSet b) {
		// Calculate current round number
		int rn = roundNr - 1;
		
		// Each time a new bid is presented, the done flags will be reset
		negotiationDone = false;
		auctionRoundDone = false;
		
		// Insert bid set to dedicated ledger for presented bid sets
		bidsetLedger.put(agentName, b);
		
		// Check if all bid sets are presented
		checkAllBidsPresented(rn);
		
		/*
		System.out.print(a.getLocalName()+": [");
		for(int i = 0;i<b.bids.size();i++) {
			System.out.print(String.format("%.2f, %.3f; ", b.bids.get(i).V, b.bids.get(i).C));
		}
		System.out.println("]");
		*/
		//Util.logString(a.getLocalName()+": presented bid!", 20);
		
	}
	
	private void checkAllBidsPresented(int rn) {
		boolean negotiationComplete = false;
		negotiationComplete = (subscriptionClients == bidsetLedger.size());
		
		if(negotiationComplete) {			
			// Insert negotiation round ledger into dedicated auction round ledger
			roundLedger.put(R, bidsetLedger);
			
			// Call method for searching for the optimal solution
			offerSet.put(rn, search(bidsetLedger));
			
			// Clear bidset ledger
			bidsetLedger.clear();			

			// If negotiation is complete, corresponding Done flag will be set
			negotiationDone = true;
			
			// Check if auction round is complete
			checkRoundComplete(rn);
			
			// Increase negotiaton round iterator count
			R++;
		}
	}
	private void checkRoundComplete(int rn) {
		boolean auctionComplete = false;
		
		//TODO logic for checking if auction round is complete
		
		if(auctionComplete) {
			// If auction is complete, corresponding Done flag will be set
			auctionRoundDone = true;
			
			// Insert auction round ledger into dedicated general ledger
			ledger.put(rn, roundLedger);
			
			// Clear negotiation round ledger
			roundLedger.clear();
		}
	}
	
	public Bid getOffer(String a, int r) {
		Bid offer = new Bid(0.0, 0.0);
		
		try {
			offer = offerSet.get(r).get(a);
		} catch (Exception e) { }
		
		return offer;
	}
	
	public void setNrOfSubscribers(int nr) {
		subscriptionClients = nr;
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
		
		return otherValues;
	}

 	/**
 	 * Method to search for the optimal combination of presented bids 
 	 * @param bidSets
 	 * @return
 	 */
	private Map<String, BidSet.Bid> search(Map<String, BidSet> bidSets) { 		
 		Map<String, BidSet.Bid> offers = new HashMap<String, BidSet.Bid>();
 		
 		//TODO search algorithm! Currently first bid from each set is used
 		for (Map.Entry<String, BidSet> entry : bidSets.entrySet()) {
 			String tempString = entry.getKey();
 			Bid tempBid = entry.getValue().bids.get(0);
 			
 			offers.put(tempString, tempBid);
 		}
 		
 		return offers;
 	}
 	
}