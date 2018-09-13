package mg;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * This class acts as a communication server which handles communication with Matlab. 
 * @author tarmokorotko
 * forked from gitHub user rroche
 */
public class simComAgent extends Agent
{

	private static final long serialVersionUID = 195263862L;
	
	Double[] ag = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};		

	// TCP connection variables
	ServerSocket srvr = null;
	Socket skt = null;
	BufferedReader in;
	PrintWriter out;
	String ip = "localhost";
	String filePath;
	int port = 1234;

	// Agent setup method
	protected void setup() 
	{
		System.out.println(getName() + " successfully started");
				
		// Get arguments
		Object[] args = getArguments();
		filePath = (String) args[0];
		
		// Create TCP connection
		try 
		{
			// Create server and socket
			srvr = new ServerSocket(port);
			skt = srvr.accept();
			System.out.println(getLocalName() + ": Server connection initiated");

			// Create writer and reader to send and receive data
			out = new PrintWriter(skt.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(skt.getInputStream()));
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}		
		
		// Run behaviors	
		// Periodical behaviour
		sendPeriodical sp = new sendPeriodical(this, 1000);
		addBehaviour(sp);

		// Receive messages behaviour
		receiveMsg rm = new receiveMsg();
		addBehaviour(rm);

		
	} // End setup

	/**
	 * Agent behaviour periodiacally sending TCP messages
	 * @param Agent
	 * @param period in seconds
	 */
	private class sendPeriodical extends TickerBehaviour {

		public sendPeriodical(Agent a, long period) {
			super(a, period);
		}

		private static final long serialVersionUID = 985730375L;
		
		@Override
		protected void onTick() {
			// compose message
			//Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));			
			String msgContent = composeMsg(ag);
					
			// send message	
			out.print(msgContent);
			out.flush();
			
			// output diagnostics to console			
			System.out.println(getLocalName() + ": Message sent to Matlab: " + msgContent);

		}// end action
		
	} // end behaviour

	
	/**
	 * Composes TCP message to be sent
	 * @param data - A 16 element array of Double
	 */
	private String composeMsg(Double[] data) 
	{
		String msg = String.format("%s ", data[0]);
		for(int i=1; i<16; i++) {
			msg = msg.concat(String.format("%s ", data[i]));
		}
		msg = msg.concat("\n");
		
		return msg;		
	} // end of composeMsg
	
	
	/**
	 * Simulation communication agent received message buffer check behaviour
	 * @author Tarmo
	 *
	 */
	private class receiveMsg extends CyclicBehaviour {

		private static final long serialVersionUID = 7623834276113064445L;

		public void action() {
			System.out.println("Receive messages check activated");
			
			ACLMessage msg = receive();				
			if (msg != null) {
				String ontology = msg.getOntology();
				String conversation = msg.getConversationId();
				String content = msg.getContent();
				AID sender = msg.getSender();
				
				System.out.println(String.format("Ontology: %s; Conversation ID: %s; Content: %s; Sender: %s", ontology, conversation, content, sender.getLocalName()));
			
				switch (conversation) {
				case "UPDATE":
					for(int i=0;i<ctrl.prsmr.length;i++) {
						if(ctrl.prsmr[i].ID.equals(sender.getLocalName())) {
							ag[i] = Double.parseDouble(content);
						}
					}
				}
				
			
			}
			else {
				block();
				}	
			} //end action
	} //end behaviour
}