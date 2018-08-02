package mg;
import jade.core.Agent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.ControllerException;
import jade.wrapper.StaleProxyException;

public class ctrl {
	
	static ContainerController cController;

	static String AGENT_NAME = "matlabComAgent"; 
	static String AGENT_CLASS = "mg.MatlabComAgent";
	//static String PWRWORLD_TESTER_NAME = "Tester"; 
	//static String PWRWORLD_TESTER_CLASS = "pwrworld.MatlabComAgentTest"; 

	public static void main(String[] args) {
		System.out.print("Hello World!");
		try 
		{
			runJade();
		} 
		catch (StaleProxyException e) {} 
		catch (ControllerException e) {}	
	}

	/**
	 * Runs JADE and starts the initial agents
	 * @throws ControllerException
	 */
	public static void runJade() throws ControllerException
	{
		// Launch JADE platform
		Runtime rt = Runtime.instance();
		Profile p;
		p = new ProfileImpl();
		cController = rt.createMainContainer(p);			
		rt.setCloseVM(true);
	
		// Launch Powerworld interface agent
		addAgent(AGENT_NAME, AGENT_CLASS, null);
		// addAgent(PWRWORLD_TESTER_NAME, PWRWORLD_TESTER_CLASS, null);
	}

	/**
	 * Creates and starts an agent
	 * @param name
	 * @param type
	 * @throws ControllerException
	 */
	private static void addAgent(String name, String type, String arg) throws ControllerException 
	{		
		Object[] argsObj = {arg};
		AgentController ac = cController.createNewAgent(name, type, argsObj);
		ac.start();
	}	
	
	
}
