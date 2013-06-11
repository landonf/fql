package fql;

import java.awt.MenuBar;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import fql.gui.GUI;

/**
 * 
 * @author ryan
 * 
 *         Program entry point.
 */
public class FQL {

	public static void main(String[] args) {
		// System.setProperty("awt.useSystemAAFontSettings","on");
		// System.setProperty("swing.aatext", "true");
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {

				final Pair<JPanel, MenuBar> gui = GUI.makeGUI();
				final JFrame f = new JFrame("FQL IDE");
				f.setContentPane(gui.first);
				f.setMenuBar(gui.second);
				f.pack();
				f.setSize(840, 630);

				f.setLocationRelativeTo(null);
				f.setVisible(true);

				f.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
				f.addWindowListener(new java.awt.event.WindowAdapter() {
					@Override
					public void windowClosing(
							java.awt.event.WindowEvent windowEvent) {
						GUI.exitAction();

					}
				});
			}
		});
	}

	static {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ee) {
		}
	}

}
