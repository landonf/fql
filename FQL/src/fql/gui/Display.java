package fql.gui;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import fql.DEBUG;
import fql.FQLException;
import fql.Pair;
import fql.decl.Environment;
import fql.decl.Instance;
import fql.decl.Mapping;
import fql.decl.NewestFQLProgram;
import fql.decl.Signature;

/**
 * 
 * @author ryan
 * 
 *         Class for showing all the viewers.
 */
public class Display {

	List<Pair<String, JComponent>> frames = new LinkedList<>();
	
	public JPanel showInst(Environment environment, String c, Instance view) throws FQLException{
		JTabbedPane px = new JTabbedPane();

		if (DEBUG.inst_graphical) {
			JPanel gp = view.pretty(environment);
			JPanel gp0 = new JPanel(new GridLayout(1, 1));
			gp0.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
			gp0.add(gp);
			px.add("Graphical", new JScrollPane(gp0));
			gp0.setSize(600, 600);
		}

		if (DEBUG.inst_textual) {
			JPanel ta = view.text();
			px.add("Textual", ta);
		}
		
		if (DEBUG.inst_tabular) {
			JPanel tp = view.view();
			px.add("Tabular", tp);
		}
		
		if (DEBUG.inst_joined) {
			JPanel joined = view.join(); 
			px.add("Joined", joined);
		}
		
		if (DEBUG.inst_gr) {
			 JPanel groth = view.groth();
			 px.add("Grothendieck", new JScrollPane(groth));
		}
		
		if (DEBUG.inst_obs) {
			JPanel rel = view.observables();
			px.add("Observables", new JScrollPane(rel));
		}

		JPanel top = new JPanel(new GridLayout(1, 1));
		top.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		top.add(px);
		return top;
	}

	public JPanel showMapping(Environment environment, String c, Mapping view) throws FQLException {
	
		JTabbedPane px = new JTabbedPane();

		if (DEBUG.mapping_graphical) {
			JPanel gp = view.pretty(environment);
			JPanel gp0 = new JPanel(new GridLayout(1, 1));
			gp0.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
			gp0.add(gp);
			px.add("Graphical", new JScrollPane(gp0));
			gp0.setSize(600, 600);
		}

		if (DEBUG.mapping_textual) {
			JPanel ta = view.text();
			px.add("Textual", ta);
		}

		if (DEBUG.mapping_tabular) {
			JPanel tp = view.view();
			px.add("Tabular", tp);
		}
		
		if (DEBUG.mapping_ed) {
			JPanel map = view.constraint();
			px.add("Embedded Dependency", map);
		}

		JPanel top = new JPanel(new GridLayout(1, 1));
		top.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		top.add(px);
		return top;
	}
	
	public JPanel showSchema(Environment environment, String c, Signature view) throws FQLException{
		JTabbedPane px = new JTabbedPane();

		if (DEBUG.schema_graphical) {
			JPanel gp = view.pretty(environment);
			JPanel gp0 = new JPanel(new GridLayout(1, 1));
			gp0.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
			gp0.add(gp);
			px.add("Graphical", new JScrollPane(gp0));
			gp0.setSize(600, 600);
		}

		if (DEBUG.schema_textual) {
			JPanel ta = view.text();
			px.add("Textual", ta);
		}
		
		if (DEBUG.schema_tabular) {
			JPanel tp = view.view();
			px.add("Tabular", tp);
		}
		
		if (DEBUG.schema_ed) {
			JPanel map = view.constraint();
			px.add("Embedded Dependency", map);
		}
		
		if (DEBUG.schema_denotation) {
			JPanel den = view.denotation();
			px.add("Denotation", den);
		}

		JPanel top = new JPanel(new GridLayout(1, 1));
		top.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		top.add(px);
		return top;
	}
	
	public Display(NewestFQLProgram p, final Environment environment) throws FQLException {
	
		for (String c : p.order) {
			if (environment.signatures.get(c) != null) {
				frames.add(new Pair<String, JComponent>("schema " + c, showSchema(environment, c, environment.getSchema(c))));
			} else if (environment.mappings.get(c) != null) {
				frames.add(new Pair<String, JComponent>("mapping " + c + " : " + p.maps_t.get(c).first + " -> " + p.maps_t.get(c).second, showMapping(environment, c, environment.getMapping(c))));
			} else if (environment.instances.get(c) != null) {
				frames.add(new Pair<String, JComponent>("instance " + c + " : " + p.insts_t.get(c), showInst(environment, c, environment.instances.get(c))));
			}
		}
		
	
			

	}


	JFrame frame = null;

	public void display(String s, List<String> order) {
		final CardLayout cl = new CardLayout();
		final JPanel x = new JPanel(cl);
		frame = new JFrame();

		// List<Pair<String, JComponent>> list = new LinkedList<Pair<String,
		// JComponent>>();
		// for (String p : order) {
		// list.add(new Pair<String, JComponent>(p, frames.get(p)));
		// }
		// Collections.sort(list, new Comparator<Pair<String, JComponent>>() {
		//
		// @Override
		// public int compare(Pair<String, JComponent> arg0,
		// Pair<String, JComponent> arg1) {
		// String s1 = arg0.first.split(" ")[1];
		// String s2 = arg1.first.split(" ")[1];
		// return s1.compareTo(s2);
		// }
		//
		// });

		final Vector<String> ooo = new Vector<>();

		for (Pair<String, JComponent> p : frames) {
			x.add(p.second, p.first);
			ooo.add(p.first);
		}
		x.add(new JPanel(), "blank");
		cl.show(x, "blank");

		final JList<String> yyy = new JList<>(ooo);
		JPanel temp1 = new JPanel(new GridLayout(1, 1));
		temp1.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEmptyBorder(), "Select:"));
		JScrollPane yyy1 = new JScrollPane(yyy);
		temp1.add(yyy1);
		temp1.setMinimumSize(new Dimension(200, 600));
		yyy.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		yyy.addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				int i = yyy.getSelectedIndex();
				if (i == -1) {
					cl.show(x, "blank");
				} else {
					cl.show(x, ooo.get(i).toString());
				}
			}

		});

		FQLSplit px = new FQLSplit(.5, JSplitPane.HORIZONTAL_SPLIT);
		px.setDividerSize(6);
		frame = new JFrame(/* "Viewer for " + */s);
		px.add(temp1);
		px.add(x);
		frame.setContentPane(px);
		frame.setSize(850, 600);

		ActionListener escListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				frame.dispose();
			}
		};

		frame.getRootPane().registerKeyboardAction(escListener,
				KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
				JComponent.WHEN_IN_FOCUSED_WINDOW);
		KeyStroke ctrlW = KeyStroke.getKeyStroke(KeyEvent.VK_W,
				InputEvent.CTRL_MASK);
		KeyStroke commandW = KeyStroke.getKeyStroke(KeyEvent.VK_W,
				InputEvent.META_MASK);
		frame.getRootPane().registerKeyboardAction(escListener, ctrlW,
				JComponent.WHEN_IN_FOCUSED_WINDOW);
		frame.getRootPane().registerKeyboardAction(escListener, commandW,
				JComponent.WHEN_IN_FOCUSED_WINDOW);

		frame.setLocationRelativeTo(null);
		frame.setVisible(true);

	}

	public void close() {
		if (frame == null) {
			return;
		}
		frame.setVisible(false);
		frame.dispose();
		frame = null;
	}

}
