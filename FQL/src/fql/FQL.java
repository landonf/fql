package fql;

import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * 
 * @author ryan
 *
 * Program entry point.
 */
public class FQL  {

	public static void main(String[] args) {
		JPanel gui = GUI.makeGUI();
		JFrame f = new JFrame("FQL IDE");
		f.setContentPane(gui);
		f.pack();
		f.setSize(800, 600);
		
		f.setLocationRelativeTo(null);
		f.setVisible(true);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
//	static {
//	try {
//		for (Object o : UIManager.getInstalledLookAndFeels()) {
//			System.out.println(o.toString());
//		}
//      UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
//	    } catch(Exception e) {
//	      System.out.println("Error setting native LAF: " + e);
//	    }
//	}

}
