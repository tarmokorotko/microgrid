package gui;

import java.awt.event.ActionEvent;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;

public class ShellTestGUI {
	private Button btnPrintName ;
	private Text inSetpoint;
	static final String TESTING = "Testing 1 2 3";
    
	public ShellTestGUI(Display display, String name) { 
		//Display display = Display.getDefault();
		final Shell shell = new Shell(display, SWT.CLOSE); 
              
		shell.setText(name); 
		shell.setLayout(new GridLayout(1, false));
		shell.setSize(200, 200);
       
		Label lblPrsmrName = new Label(shell, SWT.NONE); 
		lblPrsmrName.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, true, false, 1, 1));
		lblPrsmrName.setText(name);
		
       Label lblSetpointKw = new Label(shell, SWT.NONE);
       lblSetpointKw.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
       lblSetpointKw.setText("Setpoint: 0.0 kW");
       
       shell.addListener(SWT.Close, new Listener() { 
    	   
    	   @Override 
    	   public void handleEvent(Event event) { 
    		   System.out.println(name +" handling Close event"); 
    		   shell.dispose(); 
    	   } 
       }); 
               
       inSetpoint = new Text(shell, SWT.BORDER);
       inSetpoint.addKeyListener(new KeyAdapter() {
    	   @Override
    	   public void keyPressed(KeyEvent e) {
    		   if (e.keyCode == 13 || e.keyCode == 16777296){
    			   System.out.println("Field entry:"+inSetpoint.getText());
    			   //Sp = Double.parseDouble(inSetpoint.getText());
    			   //lblSetpointKw.setText(String.format("Setpoint: %s kW", Sp));
           	}
       	}
       });
       inSetpoint.setText("0.0");
       inSetpoint.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
              
       btnPrintName = new Button(shell, SWT.CENTER);
       GridData gd_btnPrintName = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
       gd_btnPrintName.widthHint = 154;
       btnPrintName.setLayoutData(gd_btnPrintName);
       btnPrintName.setText("Print name");
       btnPrintName.addSelectionListener(new SelectionListener() 
	     { 
	        @Override 
	        public void widgetSelected(SelectionEvent e) 
	        {  
	           System.out.println(name);		           
	        } 

	        @Override 
	        public void widgetDefaultSelected(SelectionEvent e) 
	        { 
	           widgetSelected(e); 
	        } 
	     });
       shell.open(); 
       

    } 
	
	public void actionPerformed(ActionEvent ae) {
		if (ae.getSource() == btnPrintName) {
			System.out.println("Button pressed 1 2 3");
		}
	}
	
}
