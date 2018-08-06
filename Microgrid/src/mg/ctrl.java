package mg;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.ControllerException;
import jade.wrapper.StaleProxyException;
import bin.MgGUI;
import bin.PlaygroundPart;


public class ctrl {
	
	static ContainerController cController;

	static String COMAGENT_NAME = "simComAgent"; 
	static String COMAGENT_CLASS = "mg.simComAgent";

	public static void main(String[] args) {
		try {
			PlaygroundPart window = new PlaygroundPart();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}		
		/*
		try 
		{
			runJade();
		} 
		catch (StaleProxyException e) {} 
		catch (ControllerException e) {}	
		*/
	}

	/**
	 * Runs JADE and starts the initial agents
	 * @throws ControllerException
	 */
	/*
	public static void runJade() throws ControllerException
	{
		// Launch JADE platform
		Runtime rt = Runtime.instance();
		Profile p;
		p = new ProfileImpl();
		cController = rt.createMainContainer(p);			
		rt.setCloseVM(true);
		
		
		
		// Launch simulation communication agent
		// addAgent(COMAGENT_NAME, COMAGENT_CLASS, null);
	}
*/
	/**
	 * Creates and starts an agent
	 * @param name
	 * @param type
	 * @throws ControllerException
	 */
	/*
	private static void addAgent(String name, String type, String arg) throws ControllerException 
	{		
		Object[] argsObj = {arg};
		AgentController ac = cController.createNewAgent(name, type, argsObj);
		ac.start();
	}	
	*/
}
