package mg;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;

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

		// Run behavior		
		sendPeriodical sp = new sendPeriodical(this, 1000);
		addBehaviour(sp);

	} // End setup

	/**
	 * Agent behaviour periodiacally sending TCP messages
	 * @param Agent
	 * @param period in seconds
	 */
	class sendPeriodical extends TickerBehaviour
	{

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
	
	//TODO Agent behaviour for checking received messages buffer
	
}