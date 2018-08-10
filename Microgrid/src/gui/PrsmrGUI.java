package gui;

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
	private prosumerAgent prosumer;
	private Button btnQuit ;
	private Text inSetpoint;
    Double Sp = 0.0;
    
	public PrsmrGUI(Display display, prosumerAgent a, String name) { 
		prosumer = a;
		
		final Shell shell = new Shell(display, SWT.CLOSE); 
              
		shell.setText(name); 
		shell.setLayout(new GridLayout(1, false));

		createContents(shell, prosumer);
		
       
       shell.open(); 
       shell.pack();
       

    } 
	
	protected void createContents(Shell s, prosumerAgent prosumer) {
		Label lblPrsmrName = new Label(s, SWT.NONE); 
	       lblPrsmrName.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, true, false, 1, 1));
	       lblPrsmrName.setText(prosumer.getLocalName());
	       
	       s.addListener(SWT.Close, new Listener() 
	       { 
	          @Override 
	          public void handleEvent(Event event) 
	          {  
	             prosumer.killAgent();
	             s.dispose(); 
	          } 
	       }); 
	       
	       s.setSize(200, 200); 
	       
	       Label lblSetpointKw = new Label(s, SWT.NONE);
	       lblSetpointKw.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
	       lblSetpointKw.setText("Setpoint: 0.0 kW");
	       
	       inSetpoint = new Text(s, SWT.BORDER);
	       inSetpoint.addKeyListener(new KeyAdapter() {
	       	@Override
	       	public void keyPressed(KeyEvent e) {
	       		if (e.keyCode == 13 || e.keyCode == 16777296){
	                System.out.println("Field entry:"+inSetpoint.getText());
	                Sp = Double.parseDouble(inSetpoint.getText());
	                lblSetpointKw.setText(String.format("Setpoint: %s kW", Sp));
	                
	                GuiEvent ge = new GuiEvent(this, 4);
	                ge.addParameter(Sp);
	                prosumer.postGuiEvent(ge);
	           	}
	       	}
	       });
	       inSetpoint.setText("0.0");
	       inSetpoint.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
	       	       
	       btnQuit = new Button(s, SWT.CENTER);
	       GridData gd_btnQuit = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
	       gd_btnQuit.widthHint = 154;
	       btnQuit.setLayoutData(gd_btnQuit);
	       btnQuit.setText("QUIT");
	       btnQuit.addSelectionListener(new SelectionListener() 
		     { 
		        @Override 
		        public void widgetSelected(SelectionEvent e) 
		        {  
		        	Display.getCurrent().dispose();		           
		        } 

		        @Override 
		        public void widgetDefaultSelected(SelectionEvent e) 
		        { 
		           widgetSelected(e); 
		        } 
		     });
	}
	
	

}
