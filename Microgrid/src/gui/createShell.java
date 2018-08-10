package gui;

import org.eclipse.swt.widgets.Display;

public class createShell {	
		
	public static void main(String[] args) throws Exception {

		System.out.println("Hello World!");
		
		final RunShell gui = new RunShell();
        Thread t = new Thread(gui);
        t.start();

        Thread.sleep(1000); // POINT OF FOCUS
        Display d = gui.getDisplay();

        for(int i = 0; i<5; i++) {           
            System.out.println(i + "  " + d);
            //gui.createShell(String.format("PRSMR_%s", i));  
            Thread.sleep(500);
        }

	}		
}