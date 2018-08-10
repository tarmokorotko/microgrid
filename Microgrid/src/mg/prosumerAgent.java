package mg;

import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;

import gui.PrsmrGUI;
import gui.RunShell;

/**
 * This class is a Prosumer intelligent agent. 
 * @author tarmokorotko
 * forked from gitHub user rroche
 */
public class prosumerAgent extends GuiAgent
{
	transient protected PrsmrGUI myGui;
	private static final long serialVersionUID = 195263862L;
	public static Double Sp = 0.0;
	
	// Setup method
	protected void setup() 
	{
		System.out.println(getName() + " successfully started");
		
		// Get arguments
		Object[] args = getArguments();
		
		// Instantiate and open GUI
		RunShell gui = (RunShell)args[0];
		gui.createShell(getName());		
				
		// Run behavior
		Register rg = new Register();
		addBehaviour(rg);
		
	} // End setup
	
	class Register extends OneShotBehaviour {
		
		private static final long serialVersionUID = 9288461912L;
		
		public void action()
		{
			System.out.println("Agent registered");
		}
	}
		
	
	/**
	 * Sends a message to another agent
	 * @param targetName
	 * @param content
	 * @param conversation
	 * @param type
	 */
	public void sendMessage(String targetName, String content, String conversation, int type)
	{
		ACLMessage message = new ACLMessage(type);
		message.addReceiver(new AID (targetName, AID.ISLOCALNAME));
		message.setContent(content);
		message.setConversationId(conversation);
		this.send(message);
	}


	@Override
	protected void onGuiEvent(GuiEvent arg0) {
		// TODO Auto-generated method stub
		
	}

}
