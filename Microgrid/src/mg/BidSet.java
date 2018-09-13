package mg;

import java.util.ArrayList;
import java.util.List;

public class BidSet {
	
	List<Bid> bids = new ArrayList<Bid>();
	
	public class Bid {
		Double V;
		Double C;
		
		public Bid(Double inV, Double inC) {
			V = inV;
			C = inC;
		}
	}
}

