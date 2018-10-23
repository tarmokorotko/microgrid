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

import mg.Util;
import mg.ProsumerAgent;
import mg.BidSet;
import mg.BidSet.Bid;
import jade.gui.GuiEvent;

import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.swt.widgets.Composite;


public class PrsmrGUI {
	private ProsumerAgent prosumer;
    Double Sp = 0.0;
    
    private Shell shell;
    private Table table;
    /* 
    private TableItem tableItem0;
    private TableItem tableItem1;
    private TableItem tableItem2;
    private TableItem tableItem3;
    private TableItem tableItem4;
    private TableItem tableItem5;
    private TableItem tableItem6;
    private TableItem tableItem7;  
    */
    private Label lblSetpointKw;
    private Label lblRoundInfo;
    private Label lblNegotiationInfo;
    private Label lblPresentedOffer;
    
    private Button btnSendBid;
    private Button btnNewRound;
    
    
    private Composite compositeBid;
    private Composite compositeDistributor;
    
    private GridData compositeBidGridData;
    private GridData compositeDistributorGridData;
    
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
	
	@SuppressWarnings("unused")
	protected void createContents(Shell s, ProsumerAgent prosumer) {
		/**
		 *  General GUI functionality
		 */
	    s.addListener(SWT.Close, new Listener() { 
	    	@Override 
	    	public void handleEvent(Event event) 
		        {  
		           prosumer.killAgent();
		           s.dispose(); 
		        } 
		}); 
		       
	    s.setSize(219, 283);
		Composite compositePrsmr = new Composite(shell, SWT.BORDER);
		compositePrsmr.setLayout(new GridLayout(1, true));
		compositePrsmr.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
		
		Label lblPrsmrName = new Label(compositePrsmr, SWT.NONE);
		lblPrsmrName.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		lblPrsmrName.setAlignment(SWT.CENTER);
		lblPrsmrName.setText(prosumer.getLocalName());
		   
		Combo combo = new Combo(compositePrsmr, SWT.NONE);
		combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		combo.addSelectionListener(new SelectionAdapter() {
	       	@Override
	       	public void widgetSelected(SelectionEvent e) {
	       		String selRole = combo.getText();
	       		GuiEvent ge = new GuiEvent(this, Util.GUI_MSG_ROLE);
                ge.addParameter(selRole);
                prosumer.postGuiEvent(ge);	       		
	       	}
	     });
	     combo.setItems(Util.prosumerRoles);
	       
	     /**
	      * Round section of the GUI
	      */
	       
	       Composite compositeRound = new Composite(shell, SWT.BORDER);
	       compositeRound.setLayout(new GridLayout(1, true));
	       compositeRound.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 2));
	       
	       lblSetpointKw = new Label(compositeRound, SWT.NONE);
	       lblSetpointKw.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
	       lblSetpointKw.setAlignment(SWT.CENTER);
	       lblSetpointKw.setText(String.format("PCC Setpoint for next round: %.1f kW", 0.0));
	       
	       lblRoundInfo = new Label(compositeRound, SWT.NONE);
	       lblRoundInfo.setAlignment(SWT.CENTER);
	       lblRoundInfo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
	       lblRoundInfo.setText("New round info");
	       
	       /**
	        * Bid section of the GUI
	        */
	       compositeBid = new Composite(shell, SWT.BORDER);
	       compositeBid.setLayout(new GridLayout(2, true));
		   compositeBidGridData = new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1);
		   compositeBidGridData.exclude = true;
	       compositeBid.setLayoutData(compositeBidGridData);
	       
	       table = new Table(compositeBid, SWT.BORDER | SWT.FULL_SELECTION);
	       table.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
	       table.setHeaderBackground(SWTResourceManager.getColor(SWT.COLOR_TITLE_BACKGROUND));
	       table.setHeaderVisible(true);
	       table.setLinesVisible(true);
	       
	       TableColumn tblclmnV = new TableColumn(table, SWT.NONE);
	       tblclmnV.setResizable(false);
	       tblclmnV.setWidth(89);
	       tblclmnV.setText("V");
	       
	       TableColumn tblclmnC = new TableColumn(table, SWT.NONE);
	       tblclmnC.setResizable(false);
	       tblclmnC.setWidth(100);
	       tblclmnC.setText("C");
	       
	       TableItem tableItem0 = new TableItem(table, SWT.NONE);	
	       TableItem tableItem1 = new TableItem(table, SWT.NONE);	
	       TableItem tableItem2 = new TableItem(table, SWT.NONE);	
	       TableItem tableItem3 = new TableItem(table, SWT.NONE);	
	       TableItem tableItem4 = new TableItem(table, SWT.NONE);	
	       TableItem tableItem5 = new TableItem(table, SWT.NONE);	
	       TableItem tableItem6 = new TableItem(table, SWT.NONE);	
	       TableItem tableItem7 = new TableItem(table, SWT.NONE);	       
	       
	       Button btnManual = new Button(compositeBid, SWT.RADIO);
	       btnManual.setAlignment(SWT.CENTER);
	       btnManual.setText("Manual");
	       btnManual.addSelectionListener(new SelectionAdapter() {	    	   
		       public void widgetSelected(SelectionEvent e) {
		    	   Button src = (Button)e.getSource();
		    	   GuiEvent ge = new GuiEvent(this, Util.GUI_MSG_BID_ORIGIN);
		    	   if(src.getSelection()) {
		    		   String origin = "Manual";
		                ge.addParameter(origin);
		                prosumer.postGuiEvent(ge);		    		   
		    	   }
		       }	    	   
	       });
	       	       	       
	       Button btnFromFile = new Button(compositeBid, SWT.RADIO);
	       btnFromFile.setAlignment(SWT.CENTER);
	       btnFromFile.setText("From File");
	       btnFromFile.setSelection(true);
	       btnFromFile.addSelectionListener(new SelectionAdapter() {	    	   
		       public void widgetSelected(SelectionEvent e) {
		    	   Button src = (Button)e.getSource();
		    	   GuiEvent ge = new GuiEvent(this, Util.GUI_MSG_BID_ORIGIN);
		    	   if(src.getSelection()) {
		    		   String origin = "From file";
		                ge.addParameter(origin);
		                prosumer.postGuiEvent(ge);		    		   
		    	   }
		       }	    	   
	       });
	       
	       btnSendBid = new Button(compositeBid, SWT.CENTER);
	       btnSendBid.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
	       btnSendBid.setText("SEND BID");
	       btnSendBid.addSelectionListener(new SelectionAdapter() {	    	   
	    	   public void widgetSelected(SelectionEvent e) {
	    		   GuiEvent ge = new GuiEvent(this, Util.GUI_MSG_SEND_BID); 	
	    		   BidSet bs = getBidSet();   
	    				   
	    		   ge.addParameter(bs);
	    		   prosumer.postGuiEvent(ge);    		   
	    	   }
	       });
	       compositeBid.setVisible(false);
	       compositeBidGridData.exclude = true;
	       
	       /**
	        * Distributor section of the GUI
	        */
	       compositeDistributor = new Composite(shell, SWT.BORDER);
	       compositeDistributor.setLayout(new GridLayout(2, true));
		   compositeDistributorGridData = new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 2);
		   compositeDistributor.setLayoutData(compositeDistributorGridData);
	       
	       btnNewRound = new Button(compositeDistributor, SWT.CENTER);
	       btnNewRound.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
	       btnNewRound.setText("NEW ROUND");
	       btnNewRound.addSelectionListener(new SelectionAdapter() {	  
	    	   public void widgetSelected(SelectionEvent e) {
	    		   GuiEvent ge = new GuiEvent(this, Util.GUI_MSG_NEW_ROUND);  
	    		   
	    		   ge.addParameter("New round");
	    		   prosumer.postGuiEvent(ge);    		   
	    	   }
	       });
	       compositeDistributor.setVisible(false);
	       compositeDistributorGridData.exclude = true;
	       
	       
	       Composite compositeOffer = new Composite(shell, SWT.BORDER);
	       compositeOffer.setLayout(new GridLayout(2, true));
	       compositeOffer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
	       
	       lblNegotiationInfo = new Label(compositeOffer, SWT.CENTER);
	       lblNegotiationInfo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
	       lblNegotiationInfo.setText("Negotiation info");
	       
	       lblPresentedOffer = new Label(compositeOffer, SWT.CENTER);
	       lblPresentedOffer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
	       lblPresentedOffer.setText("Presented offer");
	       
	       Button btnReject = new Button(compositeOffer, SWT.NONE);
	       btnReject.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
	       btnReject.setText("Reject");
	       btnReject.addSelectionListener(new SelectionAdapter() {	    	   
	    	   public void widgetSelected(SelectionEvent e) {
	    		   //TableItem selected = table.getSelection()[0];
	    		   GuiEvent ge = new GuiEvent(this, Util.GUI_MSG_REJECT);
	    		   //String bid = selected.getText(0)+":"+selected.getText(1);
	    		   ge.addParameter("Reject");
	    		   prosumer.postGuiEvent(ge);    		   
	    	   }
	       });
	       
	       Button btnAccept = new Button(compositeOffer, SWT.NONE);
	       btnAccept.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
	       btnAccept.setText("Accept");
	       btnAccept.addSelectionListener(new SelectionAdapter() {	    	   
	    	   public void widgetSelected(SelectionEvent e) {
	    		   GuiEvent ge = new GuiEvent(this, Util.GUI_MSG_ACCEPT);
	    		   ge.addParameter("Accept");
	    		   prosumer.postGuiEvent(ge);;    		   
	    	   }
	       });
	}
	
	/**********************************************************************************************
	 * PUBLIC METHODS FOR GUI INTERACTION *********************************************************
	 **********************************************************************************************/

	
	/**
	 * Compose Bid Set from GUI table
	 * @return
	 */
	public BidSet getBidSet() {
		TableItem[] ti = table.getItems();				
		BidSet bs = new BidSet();
		
		for(int j=0;j<ti.length;j++) {
			if(ti[j].getText(0) != "" && ti[j].getText(1) != "") {
				try {
					Double V = Double.parseDouble(ti[j].getText(0).replace(",",".").replace(" kW",""));
					Double C = Double.parseDouble(ti[j].getText(1).replace(",",".").replace(" €",""));
					bs.bids.add(new Bid(V, C));
				} catch (NumberFormatException e) {
					Util.logString("Inserted value not in Double format!", 20);
				}
			}
		}		
					
		return bs;
	}
	
	/**
	 * Method for updating table
	 * @param bs
	 */
	public void updateBids(BidSet bs) {
		TableItem[] ti = table.getItems();
		
		for(int i=0;i<ti.length;i++) {
			ti[i].setText(0, "");	
			ti[i].setText(1, "");			
		}
		
		if (bs !=null) {
			for(int j=0;j<bs.bids.size();j++) {
				ti[j].setText(0, String.format("%.2f kW", bs.bids.get(j).V).replace(".", ","));   
				ti[j].setText(1, String.format("%.3f €", bs.bids.get(j).C).replace(".", ",")); 
			}
		}	
		
	   compositeBidGridData.exclude = false;
       compositeBid.setVisible(true);
       compositeBid.getParent().pack();
	}
	
	/**
	 * Method for updating PCC setpoint
	 * @param pccSp
	 */
	public void updatePCCsetpoint(String nextRoundSp) {
	       lblSetpointKw.setText(nextRoundSp);
	}
	
	/**
	 * Method for updating round info message
	 * @param roundInfo
	 */
	public void updateRoundInfo(String roundInfo) {
	       lblRoundInfo.setText(roundInfo);		
	}
	
	/**
	 * Method for updating negotiation info message
	 * @param negotiationInfo
	 */
	public void updateNegotiationInfo(String negotiationInfo) {
	       lblNegotiationInfo.setText(negotiationInfo);		
	}
	
	/**
	 * Method for updating presented offer 
	 * @param presentedOffer
	 */
	public void updatePresentedOffer(String presentedOffer) {
	       lblPresentedOffer.setText(presentedOffer);		
	}

	/**
	 * Method for displaying participant container
	 */
	public void displayParticipant(boolean visible) {
		compositeBidGridData.exclude = !visible;
		compositeBid.setVisible(visible);
		compositeBid.setEnabled(visible);
		btnSendBid.setEnabled(visible);
		compositeBid.getParent().pack();
		
	}

	/**
	 * Method for displaying distributor container
	 */
	public void displayDistributor(boolean visible) {
	       compositeDistributorGridData.exclude = !visible;
	       compositeDistributor.setVisible(visible);
	       compositeDistributor.setEnabled(visible);
	       btnNewRound.setEnabled(visible);
	       compositeDistributor.getParent().pack();
	}
	
}