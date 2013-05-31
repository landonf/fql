package fql;

import java.awt.MenuBar;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;

import fql.gui.GUI;

/**
 * 
 * @author ryan
 *
 * Program entry point.
 */
public class FQL  {

	public static void main(String[] args) {
	//	System.setProperty("awt.useSystemAAFontSettings","on");
		//  System.setProperty("swing.aatext", "true");
		
		Pair<JPanel, MenuBar> gui = GUI.makeGUI();
		JFrame f = new JFrame("FQL IDE");
		f.setContentPane(gui.first);
		f.setMenuBar(gui.second);
		f.pack();
		f.setSize(840, 630);
		
		f.setLocationRelativeTo(null);
		f.setVisible(true);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	static {
		try {
		    UIManager.setLookAndFeel(
		            UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ee) { }
	}
	

}
