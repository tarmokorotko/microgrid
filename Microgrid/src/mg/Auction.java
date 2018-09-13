package mg;

public class Auction implements AuctionInterface {
	private int R = 0;
	@Override
	public void startNewRound() {
		// TODO Auto-generated method stub
		runAuction();
		
	}

	@Override
	public void setParticipantData() {
		// TODO Auto-generated method stub
		
	}
	
	@SuppressWarnings("unused")
	private void sendInitMessages() {
		// TODO Auto-generated method stub
		
	}
	
	private void runAuction() {
		R++;
		Util.logString(String.format("Start of auction Round: %s", R), 20);
	}
	
	@SuppressWarnings("unused")
	private void initAuction() {
		// TODO Auto-generated method stub
		
	}

}