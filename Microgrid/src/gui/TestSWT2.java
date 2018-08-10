package gui;

import java.awt.EventQueue;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class TestSWT2 {
	
	public static void openShell() {
		Display display = new Display();
		Shell shell = new Shell(display);
		 
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		gridLayout.makeColumnsEqualWidth = true;
 
		shell.setLayout(gridLayout);
 
		Button b1 = new Button(shell, SWT.PUSH);
		b1.setText("button 1");
		b1.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
 
		final Label l2 = new Label(shell, SWT.PUSH);
		l2.setText("button 2");
		GridData gridData = new GridData(GridData.VERTICAL_ALIGN_END);
		l2.setLayoutData(gridData);
 
		Button button3 = new Button(shell, SWT.PUSH);
		button3.setText("button 3");
 
		button3.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub
				l2.setText("clicked");
			}
 
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub
				l2.setText("clicked");
			}
		});
 
		button3.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL));
 
		shell.pack();
		shell.open();
		while (!shell.isDisposed()) {
		      if (!display.readAndDispatch())
		        display.sleep();
		    }
		    display.dispose();
	}

	public static void main(String[] args) {
		
		//Display display = new Display();
		EventQueue.invokeLater(new Runnable( ) {			
			@Override
			public void run() {
				openShell();
			}
		});
		
		EventQueue.invokeLater(new Runnable( ) {			
			@Override
			public void run() {
				openShell();
			}
		});
		
	}
}
