package gui;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;

import java.awt.EventQueue;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import mg.prosumerAgent;

import org.eclipse.swt.layout.GridData;

public class MgGUI {
	private Text txtTxt;
	final static Display display = Display.getDefault();
	/**
	 * Open the window.
	 * @wbp.parser.entryPoint
	 */	
	public void open() {		
		
		final Shell mainShell = new Shell(display, SWT.CLOSE);
		
		createContents(mainShell);
		mainShell.setText("Main");
		mainShell.open();
		mainShell.layout();
		
		mainShell.addListener(SWT.Close, new Listener() 
				{
					public void handleEvent(Event event) {
						display.dispose();						
					}
				});
		/*
		mainShell.addListener(SWT.Close, new Listener() 
		{
			public void handleEvent(Event event) {
				System.out.println("Create child shell"); 						
			}
		});
		*/
		//display.addListener(eventType, listener);
		display.asyncExec(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				System.out.println("Test"); 
			}			
		});
		
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
	
	/**
	 * Create contents of the window.
	 * @wbp.parser.entryPoint
	 */
	protected void createContents(Shell s) {
		s.setSize(450, 300);
		s.setText("SWT Application");
		s.setLayout(new GridLayout(2, false));

		Label lblLbl = new Label(s, SWT.NONE);
		lblLbl.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblLbl.setText("lbl1");

		txtTxt = new Text(s, SWT.BORDER);
		txtTxt.setText("txt1");
		txtTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Button btnBtn1 = new Button(s, SWT.NONE);
		btnBtn1.setText("bttn");

		Button btnBtn2 = new Button(s, SWT.NONE);
		btnBtn2.setText("btn2");
	}
	

	
	public class ChildShell { 
	     public ChildShell(prosumerAgent a, String name) 
	     { 
	    	System.out.println("Creating child Shell "+name); 
	        new PrsmrGUI(a, name);	
	        display.wake();
	     } 	     
	  } 
	
	public void createChildShell(prosumerAgent a, String name) {
		final MgGUI gui = this;
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					gui.new ChildShell(a, name);
				} catch (Exception e) {
					e.printStackTrace();
				}
				//display.wake();
			}
		});	
	}
	
	public void wakeDisplay() {
		display.wake();
	}
}