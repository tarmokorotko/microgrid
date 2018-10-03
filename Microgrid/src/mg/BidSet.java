package mg;

import java.util.ArrayList;
import java.util.List;

public class BidSet {
	
	public List<Bid> bids = new ArrayList<Bid>();
	
	public static class Bid {
		public Double V;
		public Double C;
		
		public Bid(Double inV, Double inC) {
			V = inV;
			C = inC;
		}
	}
	
	public Bid getBid(int index) throws IndexOutOfBoundsException {
		return bids.get(index);
	}
	
	public void setBid(int index, Bid b) throws IndexOutOfBoundsException {
		bids.add(index, b);
	}
}

