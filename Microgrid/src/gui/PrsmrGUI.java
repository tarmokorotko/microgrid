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

import mg.prosumerAgent;

import jade.gui.GuiEvent;

public class PrsmrGUI {
	Object[] actions = {"","New Account","Deposit","Withdrawal", "Balance","Operations"};
	private prosumerAgent myAgent;
	private Button btnPrintName ;
	private Text inSetpoint;
	static final String TESTING = "Testing 1 2 3";
    Double Sp = 0.0;
    
	public PrsmrGUI(prosumerAgent a, String name) { 
		myAgent = a;
		
		Display display = Display.getDefault();
		final Shell shell = new Shell(display, SWT.CLOSE); 
              
		shell.setText(name); 
		shell.setLayout(new GridLayout(1, false));

       Label lblPrsmrName = new Label(shell, SWT.NONE); 
       lblPrsmrName.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, true, false, 1, 1));
       lblPrsmrName.setText(name);

       shell.addListener(SWT.Close, new Listener() 
       { 
          @Override 
          public void handleEvent(Event event) 
          { 
             System.out.println(name +" handling Close event"); 
             shell.dispose(); 
          } 
       }); 
       
       shell.setSize(200, 200); 
       
       Label lblSetpointKw = new Label(shell, SWT.NONE);
       lblSetpointKw.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
       lblSetpointKw.setText("Setpoint: 0.0 kW");
       
       inSetpoint = new Text(shell, SWT.BORDER);
       inSetpoint.addKeyListener(new KeyAdapter() {
       	@Override
       	public void keyPressed(KeyEvent e) {
       		if (e.keyCode == 13 || e.keyCode == 16777296){
                System.out.println("Field entry:"+inSetpoint.getText());
                Sp = Double.parseDouble(inSetpoint.getText());
                lblSetpointKw.setText(String.format("Setpoint: %s kW", Sp));
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
       shell.pack();
       

    } 
	
	public void actionPerformed(ActionEvent ae) {
		if (ae.getSource() == btnPrintName) {
			System.out.println("Button pressed 1 2 3");
		}
	}
	
	
	public void abc(ActionEvent ae) {
		//Process the event according to it's source
		if (ae.getSource() == btnPrintName) {
			System.out.println("Button pressed 1 2 3");
			GuiEvent ge = new GuiEvent(this, 1);
			myAgent.postGuiEvent(ge);
		}		
	}
}
