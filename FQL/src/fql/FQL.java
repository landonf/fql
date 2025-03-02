package fql;

import java.awt.MenuBar;
import java.util.Arrays;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import fql.decl.Driver;
import fql.decl.Environment;
import fql.decl.FQLProgram;
import fql.examples.Examples;
import fql.gui.CodeEditor;
import fql.gui.GUI;
import fql.parse.FQLParser;

/**
 * 
 * @author ryan
 * 
 *         Program entry point.
 */
public class FQL {
	

	public static void main(String[] args) {
		if (args.length == 1) {
			try {
				FQLProgram init = FQLParser.program(args[0]);
				Triple<Environment, String, List<Throwable>> envX = Driver
						.makeEnv(init);
				if (envX.third.size() > 0) {
					throw new RuntimeException("Errors: " + envX.third);
				}
				System.out.println("OK");
				System.out.println(envX.second);
				return;
			} catch (Throwable err) {
				err.printStackTrace(System.err);
				System.out.println(err.getLocalizedMessage());
				return;
			}
		} else if (args.length != 0) {
			System.out
					.println("The FQL IDE expects zero arguments for the gui and one argument, a string, for the compiler.");
			return;
		}

		//TODO check surjective pis still compose
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					Arrays.sort(Examples.examples);
					Arrays.sort(Examples.key_examples);

					DEBUG.load(true);
					//f = new JFrame("FQL IDE");
					
					UIManager.setLookAndFeel(DEBUG.debug.look_and_feel);			

					final JFrame f = new JFrame("FQL IDE");
					final Pair<JPanel, MenuBar> gui = GUI.makeGUI(f);

					f.setContentPane(gui.first);
					f.setMenuBar(gui.second);
					f.pack();
					f.setSize(840, 630);
					((CodeEditor) GUI.editors.getComponentAt(0)).topArea
							.requestFocusInWindow();
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
				} catch (Throwable e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(
							null,
							"Unrecoverable error, restart FQL: "
									+ e.getMessage());
				}
			}
		});
	}


}
