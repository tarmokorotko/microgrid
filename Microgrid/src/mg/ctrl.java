package mg;

import java.util.TimerTask;

import gui.RunShell;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.ControllerException;
import jade.wrapper.StaleProxyException;

public class ctrl {	
	
	static ContainerController cController;

	static String COMAGENT_NAME = "simComAgent"; 
	static String COMAGENT_CLASS = "mg.simComAgent";
	static String PRSMRAGENT_CLASS = "mg.prosumerAgent";

	public static Prsmr[] prsmr = {new Prsmr("PRSMR_0"), new Prsmr("PRSMR_1"), new Prsmr("PRSMR_2"), new Prsmr("PRSMR_2_0"), new Prsmr("PRSMR_2_1")}; 
	
	public static void main(String[] args) throws Exception {	
		// Initialize environment
		Util.initialize();
		//String experimentDataFilePath = "C:/Users/Tarmo/OneDrive - TTU 2/Doktoritöö/Simulatsiooni andmed/Experiment case 4.xlsx";
		
		/*
		List<int[]> sheetData = new ArrayList<int[]>();
		int[] cellData1 = {12, 7};
		int[] cellData2 = {12, 8};
		sheetData.add(cellData1);
		sheetData.add(cellData2);
		List<String> test = Util.readFromExcel(experimentDataFilePath, "PCC", sheetData);
		
		for(int i=0;i<test.size();i++) {
			System.out.println(test.get(i));
		}
		*/
		
		// Set up Display thread
		final RunShell gui = new RunShell();
        Thread t = new Thread(gui);
        t.start();
        Thread.sleep(1000); // Delay for Display thread initiation
        
        // Run JADE and start initial agents
		try {
			runJade(gui);
		} 
		catch (StaleProxyException e) {} 
		catch (ControllerException e) {}		
		
		// Periodic task
		//Timer timer = new Timer();
		//timer.schedule(new ScheduledTask(), 0, 1000);		
		
	}
	
	/**
	 * Runs JADE and starts the initial agents
	 * @param gui - RunShell instance for Prosumer gui management
	 * @throws ControllerException
	 */
	private static void runJade(RunShell gui) throws ControllerException
	{
		// Launch JADE platform
		Runtime rt = Runtime.instance();
		Profile p;
		p = new ProfileImpl();
		cController = rt.createMainContainer(p);			
		rt.setCloseVM(true);		
		
		// Launch simulation communication agent
		addAgent(COMAGENT_NAME, COMAGENT_CLASS, null);
		// Launch prosumer agents
		for(int i=0;i<prsmr.length;i++) {
			addAgent(prsmr[i].ID, PRSMRAGENT_CLASS, gui);			
		}

	}

	/**
	 * Creates and starts an agent
	 * @param name
	 * @param type
	 * @param gui - RunShell instance for Prosumer gui management
	 * @throws ControllerException
	 */	
	private static void addAgent(String name, String type, RunShell gui) throws ControllerException 
	{		
		Object[] argsObj = {gui};
		AgentController ac = cController.createNewAgent(name, type, argsObj);
		ac.start();
	}	
	
	/**
	 * Periodically executed task
	 * @author Tarmo
	 *
	 */
	public static class ScheduledTask extends TimerTask {
			public void run() {
				System.out.println("Periodical task executed");
			}
	}
	
}
