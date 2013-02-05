package fql;

import javax.swing.JApplet;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * @author ryan
 * Entry point for the applet.
 */
public class FQLApplet extends JApplet {
	
	private static final long serialVersionUID = 1L;
	static boolean isapplet = false;

	public void init() {
		isapplet = true;
        //Execute a job on the event-dispatching thread; creating this applet's GUI.
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    JPanel lbl = GUI.makeGUI();
                    add(lbl);
                }
            });
        } catch (Exception e) {
            System.err.println("Couldn't create GUI " + e.getMessage());
        }
    }
	
}
