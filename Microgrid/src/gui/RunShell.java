package gui;

import org.eclipse.swt.widgets.Display;

import mg.ProsumerAgent;

public class RunShell implements Runnable {
	
	private Display display;
    public Display getDisplay(){
        return display;
    }	
   
	public void run() {	
		display = new Display();		
		
		while (!display.isDisposed()) { 
		       try { 
		           if (!display.readAndDispatch()) { 
		              display.sleep(); 
		           } 
		        } 
		        catch (Exception e) { 
		           e.printStackTrace(); 
		        } 
		     }		
	}
	
	private void openShell(Display display, ProsumerAgent pa, String name) {
		//new ShellTestGUI(display, name);
		new PrsmrGUI(display, pa ,name);
	}
	
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
}