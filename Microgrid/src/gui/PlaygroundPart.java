package gui;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;

public class PlaygroundPart {

	protected Shell shell;
	//protected Shell sh2;
	private Text txtTxt;
	final static Display display = Display.getDefault();
	/**
	 * Launch the application.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			PlaygroundPart window = new PlaygroundPart();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Open the window.
	 */
	public void open() {
		//Shell sh1;
		
		final Shell mainShell = new Shell(display, SWT.CLOSE);
		mainShell.setText("Main");
		
		
		createContents(mainShell);
		mainShell.open();
		mainShell.layout();
		
		new ChildShell("Child 1");
		
		mainShell.addListener(SWT.Close, new Listener() 
				{
					public void handleEvent(Event event) {
						display.dispose();						
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
	 */
	protected void createContents(Shell shell) {
		shell.setSize(450, 300);
		shell.setText(" SWT Application");
		shell.setLayout(new GridLayout(2, false));

		Label lblLbl = new Label(shell, SWT.NONE);
		lblLbl.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblLbl.setText("lbl1");

		txtTxt = new Text(shell, SWT.BORDER);
		txtTxt.setText("txt1");
		txtTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Button btnBtn1 = new Button(shell, SWT.NONE);
		btnBtn1.setText("bttn");

		Button btnBtn2 = new Button(shell, SWT.NONE);
		btnBtn2.setText("btn2");

	}
	
	/**
	 * Create child shell
	 */
	private class ChildShell { 
	     public ChildShell( String name) 
	     { 
	        System.out.println("Creating new child Shell"); 
	        
	        final Shell shell = new Shell(display, SWT.CLOSE); 
	        
	        shell.setText("Child Shell"); 
 
	        Label label = new Label(shell, SWT.NONE); 
	        label.setText(name); 
	        label.setBounds(0, 0, 100, 20); 
	        shell.setSize(200, 200); 
	        shell.open(); 

	        shell.addListener(SWT.Close, new Listener() 
	        { 
	           @Override 
	           public void handleEvent(Event event) 
	           { 
	              System.out.println("Child Shell handling Close event, about to dispose this Shell"); 
	              shell.dispose(); 
	           } 
	        }); 
	     } 
	  } 

}
