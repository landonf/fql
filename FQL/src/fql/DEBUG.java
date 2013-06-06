package fql;

import java.awt.GridLayout;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

/**
 * 
 * @author ryan
 *
 * Contains global constants for debugging.
 */
public class DEBUG {

	public static  Intermediate INTERMEDIATE = Intermediate.NONE;

	public static  boolean VALIDATE = true;
	
	public static  int MAX_PATH_LENGTH = 8;

	public static  int MAX_DENOTE_ITERATIONS = 64;
	
	//public static int MAX_JOIN_SIZE = 1024;
	
	public static  boolean ALLOW_INFINITES = true; 
	
	//public static boolean CHECK_MAPPINGS = false;
	
	public static boolean DO_NOT_GUIDIFY = false;
	
	public enum Intermediate { SOME, NONE, ALL };
	
	
	public static void showOptions() {
		JPanel p = new JPanel(new GridLayout(6, 2));
		
	//	JCheckBox jcb1 = new JCheckBox("", CHECK_MAPPINGS);
	//	p.add(new JLabel("Require mapping well-formedness:"));
	//	p.add(jcb1);
		JRadioButton noneb = new JRadioButton("none");
		JRadioButton somb = new JRadioButton("some");
		JRadioButton allb = new JRadioButton("all");
		ButtonGroup bg = new ButtonGroup();
		bg.add(noneb);
		bg.add(somb);
		bg.add(allb);
		if (INTERMEDIATE == Intermediate.SOME) {
			somb.setSelected(true);
		} else if (INTERMEDIATE == Intermediate.NONE) {
			noneb.setSelected(true);
		} else if (INTERMEDIATE == Intermediate.ALL) {
			allb.setSelected(true);
		} else {
			throw new RuntimeException();
		}
		
		//	JPanel p0 = new JPanel();
			p.add(new JLabel("Show intermediate schemas:"));
			JPanel xxx = new JPanel();
			xxx.add(noneb);
			xxx.add(somb);
			xxx.add(allb);
			p.add(xxx);
		
		JCheckBox jcbX = new JCheckBox("", DO_NOT_GUIDIFY);
		//	JPanel p0 = new JPanel();
			p.add(new JLabel("Do not GUID-ify (dangerous):"));
			p.add(jcbX);
		
		JCheckBox jcb0 = new JCheckBox("", ALLOW_INFINITES);
	//	JPanel p0 = new JPanel();
		p.add(new JLabel("Do not validate mappings:"));
		p.add(jcb0);
		
		JCheckBox jcb = new JCheckBox("", VALIDATE);
	//	JPanel p0 = new JPanel();
		p.add(new JLabel("Validate all categories:"));
		p.add(jcb);
		//p.add(jcb);
		
		//JPanel p1 = new JPanel();
		JTextField plen = new JTextField(Integer.toString(MAX_PATH_LENGTH));
		p.add(new JLabel("Maximum path length:"));
		p.add(plen);
		//p.add(p1);
		
		//JPanel p2 = new JPanel();
		JTextField iter = new JTextField(Integer.toString(MAX_DENOTE_ITERATIONS));
		p.add(new JLabel("Maximum category size:"));
		p.add(iter);
		//p.add(p2);
		
//		JTextField sz = new JTextField(Integer.toString(MAX_JOIN_SIZE));
//		p.add(new JLabel("Maximum potential join size:"));
//		p.add(iter);
		
		int ret = JOptionPane.showConfirmDialog(null, p, "Options", JOptionPane.OK_CANCEL_OPTION);
		if (ret == JOptionPane.YES_OPTION) {
			int a = MAX_PATH_LENGTH;
			int b = MAX_DENOTE_ITERATIONS;
			//int c = MAX_JOIN_SIZE;
			try {
				a = Integer.parseInt(plen.getText());
				b = Integer.parseInt(iter.getText());
		//		c = Integer.parseInt(sz.getText());
			} catch (NumberFormatException nfe) {
				return;
			}
			ALLOW_INFINITES = jcb0.isSelected();
			VALIDATE = jcb.isSelected();
			DO_NOT_GUIDIFY = jcbX.isSelected();
			if (somb.isSelected()) {
				INTERMEDIATE = Intermediate.SOME;
			} else if (noneb.isSelected()) {
				INTERMEDIATE = Intermediate.NONE;
			} else if (allb.isSelected()) {
				INTERMEDIATE = Intermediate.ALL;
			} else {
				throw new RuntimeException();
			}
			
	//		CHECK_MAPPINGS = jcb1.isSelected();
			MAX_PATH_LENGTH = a;
			MAX_DENOTE_ITERATIONS = b;
			//MAX_JOIN_SIZE = c;
		}
	}

}
