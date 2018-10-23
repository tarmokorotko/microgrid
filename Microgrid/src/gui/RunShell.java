package gui;

import java.util.HashMap;

import org.eclipse.swt.widgets.Display;

import mg.BidSet;
import mg.ProsumerAgent;

public class RunShell implements Runnable {
	private Display display;
	private HashMap<String, PrsmrGUI> myGuis;		
   
	public void run() {	
		display = new Display();
		myGuis = new HashMap<String, PrsmrGUI>();
		
		while (!display.isDisposed()) { 
			try { 
				if (!display.readAndDispatch()) { 
					display.sleep(); 
				} 
			} catch (Exception e) { 
				e.printStackTrace(); 
			} 
		}		
	}

	/**
	 * Method for geting display
	 * @return
	 */
    public Display getDisplay(){
        return display;
    }
	
	/**
	 * Inner method for opening GUI shell
	 * @param display
	 * @param pa
	 * @param name
	 */
	private void openShell(Display display, ProsumerAgent pa, String name) {
		PrsmrGUI myGui = new PrsmrGUI(display, pa ,name);
		myGuis.put(name, myGui);
	}	

	/**
	 * Method for creating GUI shell
	 * @param name
	 * @param pa
	 */
	public synchronized void createShell(final String name, ProsumerAgent pa)
    {
        if (display == null || display.isDisposed()) 
            return;
        display.asyncExec(new Runnable() {
            public void run() {
        		openShell(display, pa, name);
            }
        });
    }
	
	/**
	 * Method for updating GUI table
	 * @param name
	 * @param bs
	 */
	public synchronized void updateTable(String name,BidSet bs) {
		PrsmrGUI selectedGui = myGuis.get(name);
		selectedGui.updateBids(bs);
	}
	
	/**
	 * Method for updating next round setpoint display
	 * @param name
	 * @param pccSP
	 */
	public synchronized void updatePCCsetpoint(String name, String nextRoundSp) {
		PrsmrGUI selectedGui = myGuis.get(name);
		selectedGui.updatePCCsetpoint(nextRoundSp);
	}
	
	/**
	 * Method for updating round info display
	 * @param name
	 * @param roundInfo
	 */
	public synchronized void updateRoundInfo(String name,String roundInfo) {
		PrsmrGUI selectedGui = myGuis.get(name);
		selectedGui.updateRoundInfo(roundInfo);
	}
	
	/**
	 * Method for updating negotiation info display
	 * @param name
	 * @param negotiationInfo
	 */
	public synchronized void updateNegotiationInfo(String name,String negotiationInfo) {
		PrsmrGUI selectedGui = myGuis.get(name);
		selectedGui.updateNegotiationInfo(negotiationInfo);
	}
	
	/**
	 * Method for updating presented offer
	 * @param name
	 * @param presentedOffer
	 */
	public synchronized void updatePresentedOffer(String name,String presentedOffer) {
		PrsmrGUI selectedGui = myGuis.get(name);
		selectedGui.updatePresentedOffer(presentedOffer);
	}	

	/**
	 * Method for displaying participant container
	 * @param name
	 * @param visible
	 */
	public synchronized void updateDisplayParticipant(String name,boolean visible) {
		PrsmrGUI selectedGui = myGuis.get(name);
		selectedGui.displayParticipant(visible);		
	}

	/**
	 * Method for displaying distributor container
	 * @param name
	 * @param visible
	 */
	public synchronized void updateDisplayDistributor(String name,boolean visible) {
		PrsmrGUI selectedGui = myGuis.get(name);
		selectedGui.displayDistributor(visible);		
	}
}