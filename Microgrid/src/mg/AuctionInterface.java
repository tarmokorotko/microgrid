package mg;

import jade.lang.acl.ACLMessage;

public interface AuctionInterface {
	public ACLMessage startNewRound(); // method for initiating new auction round
	public void presentBid(String agentName, BidSet b); // method for setting auction participant data
}
