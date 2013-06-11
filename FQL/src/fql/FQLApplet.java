package fql;

import javax.swing.JApplet;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import fql.gui.GUI;

/**
 * @author ryan Entry point for the applet.
 */
public class FQLApplet extends JApplet {

	private static final long serialVersionUID = 1L;
	public static boolean isapplet = false;

	public void init() {
		isapplet = true;

		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					JPanel lbl = GUI.makeGUI().first;
					add(lbl);
				}
			});
		} catch (Exception e) {
			System.err.println("Couldn't create GUI " + e.getMessage());
		}
	}

}
