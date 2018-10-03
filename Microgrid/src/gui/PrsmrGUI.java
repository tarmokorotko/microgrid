package gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
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

import mg.Util;
import mg.ProsumerAgent;
import mg.BidSet;

import jade.gui.GuiEvent;

import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.wb.swt.SWTResourceManager;


public class PrsmrGUI {
	private ProsumerAgent prosumer;
	private Text inSetpoint;
    Double Sp = 0.0;
    
    private Shell shell;
    private Table table;
     
    private TableItem tableItem0;
    private TableItem tableItem1;
    private TableItem tableItem2;
    private TableItem tableItem3;
    private TableItem tableItem4;
    private TableItem tableItem5;
    private TableItem tableItem6;
    private TableItem tableItem7;    
    
	public PrsmrGUI(Display display, ProsumerAgent a, String name) { 
		prosumer = a;

		GridLayout gl_shell = new GridLayout(2, true);
		shell = new Shell(display, SWT.CLOSE | SWT.TITLE);         		
		shell.setText(name); 
		shell.setLayout(gl_shell);

		createContents(shell, prosumer);
		
		shell.open(); 
       	shell.pack();       
    } 
	
	protected void createContents(Shell s, ProsumerAgent prosumer) {
		Label lblPrsmrName = new Label(s, SWT.NONE); 
		lblPrsmrName.setAlignment(SWT.CENTER);
	       lblPrsmrName.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 2, 1));
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
	       
	       s.setSize(303, 221);
	       
	       Label lblSetpointKw = new Label(s, SWT.NONE);
	       lblSetpointKw.setAlignment(SWT.CENTER);
	       lblSetpointKw.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 2, 1));
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
	       inSetpoint.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 2, 1));
	       
	       table = new Table(shell, SWT.BORDER | SWT.FULL_SELECTION);
	       table.setHeaderBackground(SWTResourceManager.getColor(SWT.COLOR_TITLE_BACKGROUND));
	       table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 2, 1));
	       table.setHeaderVisible(true);
	       table.setLinesVisible(true);
	       
	       TableColumn tblclmnV = new TableColumn(table, SWT.NONE);
	       tblclmnV.setResizable(false);
	       tblclmnV.setWidth(100);
	       tblclmnV.setText("V");
	       
	       TableColumn tblclmnC = new TableColumn(table, SWT.NONE);
	       tblclmnC.setResizable(false);
	       tblclmnC.setWidth(100);
	       tblclmnC.setText("C");
	       
	       tableItem0 = new TableItem(table, SWT.NONE);	
	       tableItem1 = new TableItem(table, SWT.NONE);	
	       tableItem2 = new TableItem(table, SWT.NONE);	
	       tableItem3 = new TableItem(table, SWT.NONE);	
	       tableItem4 = new TableItem(table, SWT.NONE);	
	       tableItem5 = new TableItem(table, SWT.NONE);	
	       tableItem6 = new TableItem(table, SWT.NONE);	
	       tableItem7 = new TableItem(table, SWT.NONE);	   
	       
	       Label lblProsumerRole = new Label(shell, SWT.NONE);
	       lblProsumerRole.setAlignment(SWT.CENTER);
	       lblProsumerRole.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
	       lblProsumerRole.setText("Prosumer Role");
	       
	       Combo combo = new Combo(shell, SWT.NONE);
	       combo.addSelectionListener(new SelectionAdapter() {
	       	@Override
	       	public void widgetSelected(SelectionEvent e) {
	       		String selRole = combo.getText();
	       		GuiEvent ge = new GuiEvent(this, 1);
                ge.addParameter(selRole);
                prosumer.postGuiEvent(ge);	       		
	       	}
	       });
	       combo.setItems(Util.prosumerRoles);
	       GridData gd_combo = new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1);
	       combo.setLayoutData(gd_combo);
	       
	       Button btnReject = new Button(shell, SWT.NONE);
	       btnReject.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
	       btnReject.setText("Reject");
	       
	       Button btnAccept = new Button(shell, SWT.NONE);
	       btnAccept.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
	       btnAccept.setText("Accept");	  
	}
	
	/**
	 * Method for updating table
	 * @param bs
	 */
	public void updateBids(BidSet bs) {
		tableItem0.setText(0, "");
		tableItem0.setText(1, "");
		tableItem1.setText(0, "");
		tableItem1.setText(1, "");
		tableItem2.setText(0, "");
		tableItem2.setText(1, "");
		tableItem3.setText(0, "");
		tableItem3.setText(1, "");
		tableItem4.setText(0, "");
		tableItem4.setText(1, "");
		tableItem5.setText(0, "");
		tableItem5.setText(1, "");
		tableItem6.setText(0, "");
		tableItem6.setText(1, "");
		tableItem7.setText(0, "");
		tableItem7.setText(1, "");
		
		if (bs !=null) {
			if (bs.bids.size() > 0) {
			tableItem0.setText(0, String.format("%.2f kW", bs.bids.get(0).V).replace(".", ","));   
			tableItem0.setText(1, String.format("%.3f €", bs.bids.get(0).C).replace(".", ","));
			}
			if (bs.bids.size() > 1) {
			tableItem1.setText(0, String.format("%.2f kW", bs.bids.get(1).V).replace(".", ","));   
			tableItem1.setText(1, String.format("%.3f €", bs.bids.get(1).C).replace(".", ","));
			}
			if (bs.bids.size() > 2) {
			tableItem2.setText(0, String.format("%.2f kW", bs.bids.get(2).V).replace(".", ","));   
			tableItem2.setText(1, String.format("%.3f €", bs.bids.get(2).C).replace(".", ","));
			}
			if (bs.bids.size() > 3) {
			tableItem3.setText(0, String.format("%.2f kW", bs.bids.get(3).V).replace(".", ","));   
			tableItem3.setText(1, String.format("%.3f €", bs.bids.get(3).C).replace(".", ","));
			}
			if (bs.bids.size() > 4) {
			tableItem4.setText(0, String.format("%.2f kW", bs.bids.get(4).V).replace(".", ","));   
			tableItem4.setText(1, String.format("%.3f €", bs.bids.get(4).C).replace(".", ","));
			}
			if (bs.bids.size() > 5) {
			tableItem5.setText(0, String.format("%.2f kW", bs.bids.get(5).V).replace(".", ","));   
			tableItem5.setText(1, String.format("%.3f €", bs.bids.get(5).C).replace(".", ","));
			}
			if (bs.bids.size() > 6) {
			tableItem6.setText(0, String.format("%.2f kW", bs.bids.get(6).V).replace(".", ","));   
			tableItem6.setText(1, String.format("%.3f €", bs.bids.get(6).C).replace(".", ","));
			}
			if (bs.bids.size() > 7) {
			tableItem7.setText(0, String.format("%.2f kW", bs.bids.get(7).V).replace(".", ","));   
			tableItem7.setText(1, String.format("%.3f €", bs.bids.get(7).C).replace(".", ","));
			}
		}
	}
}