package mg;

import java.util.ArrayList;
import java.util.List;

public class BidSet {
	
	public List<Bid> bids = new ArrayList<Bid>();
	
	public BidSet(Double[]... varArg) {
		int j = varArg.length;
		Bid[] b = new Bid[j];
		
		Double[][] t = new Double[j][2];
		
		// Handle inputs		
		for(int i=0;i<j;i++) {
			if (!(varArg[i] instanceof Double[])) {
				t[i] = (Double[])varArg[i];
					if(t[i].length != 2) {
						throw new IllegalArgumentException("Passed argument "+String.valueOf(i+1)+" not type of Double");
					}
				}
			b[i] = new Bid(t[i][0], t[i][1]);
		}
	}
	
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

