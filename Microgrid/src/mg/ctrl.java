package mg;
import jade.core.Agent;

public class ctrl {

	private static BookByerAgent a1;

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.print("Hello World!");
		a1 = null;
		a1.setup();
	}
	
	public class BookByerAgent extends Agent { 
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		protected void setup() {
		 // Printout a welcome message
		 System.out.println("Hello! Buyer-agent "+getAID().getName()+" is ready.");
		}
	} 

}
