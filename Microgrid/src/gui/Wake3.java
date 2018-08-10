package gui;

import org.eclipse.swt.widgets.*;
 
public class Wake3 {

	public static void main(String[] args) {
    final Display display = new Display();
    final Shell shell = new Shell(display);
    shell.setSize(500, 64);
    shell.open();
    final boolean[] done = new boolean[1];
    final boolean[] reporting=new boolean[1];
    final StringBuffer title=new StringBuffer("Running ");
	  new Thread() {
	      public void run() {
	          for (int i = 0; i < 10; i++) {
	              try {
	            Thread.sleep(500);
	        } catch (Throwable th) {
	        }
	        title.append(".");
	    if (reporting[0]) continue;
	    reporting[0] = true;
	          display.asyncExec(new Runnable() {
	              public void run() {
	            if (shell.isDisposed()) return;
	            shell.setText(title.toString());
	            reporting[0] = false;
	        }
	    });
	}
	done[0] = true;
	// wake the user-interface thread from sleep
	    display.wake();
	      }
	  }
    .start();
    shell.setText(title.toString());
	      while (!done[0]) {
        if (!display.readAndDispatch()) display.sleep();
    }
	      if (!shell.isDisposed()) {
        title.append(" done.");
        shell.setText(title.toString());
	          try {
            Thread.sleep(500);
        } catch (Throwable th) {
        }
    }
    display.dispose();
}

}
