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

import fql.FQLException;
import fql.Pair;
import fql.decl.Environment;

/**
 * 
 * @author ryan
 *
 * Class for showing all the viewers.
 */
public class Display {

	List<Pair<String, JComponent>> frames = new LinkedList<>();

	public Display(final Environment environment, List<String> commands)
			throws FQLException {
		//frames = new HashMap<String, JComponent>();

		for (String c : commands) {
			Viewable<?> view = environment.get(c);
	
				JTabbedPane px = new JTabbedPane();
				
				JPanel gp = view.pretty(environment);
				JPanel gp0 = new JPanel(new GridLayout(1,1));
				gp0.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
				gp0.add(gp);
				px.add("Graphical", new JScrollPane(gp0));
				gp0.setSize(600, 600);

				JPanel tp = view.view();
				px.add("Tabular", tp);
				
				JPanel joined = view.join();
				if (joined != null) {
					px.add("Joined", joined);
				}
				
				JPanel ta = view.text();

				px.add("Textual", ta);
				
				JPanel ja = view.json();
				if (ja != null) {
					px.add("JSON", ja);
				}
				
				JPanel den = view.denotation();
				if (den != null) {
					px.add("Denotation", den);
				}
				
				JPanel init = view.initial();
				if (init != null) {
					px.add("Initial", init);
				}
				
				JPanel groth = view.groth();
				if (groth != null) {
					px.add("Grothendieck", new JScrollPane(groth));
				}
				
				JPanel rel = view.observables();
				if (rel != null) {
					px.add("Observables", new JScrollPane(rel));
				}
					
				JPanel top = new JPanel(new GridLayout(1,1));
				top.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
				top.add(px);
				String xxx = view.type() + " " + c; 
				if (environment.instances.containsKey(c)) {
					xxx += (" : " + environment.instances.get(c).thesig.name0);
				} else if (environment.mappings.containsKey(c)) {
						xxx += (" : " + environment.mappings.get(c).source.name0 + " -> " + environment.mappings.get(c).target.name0);	
					
				} else if (environment.queries.containsKey(c)) {
					xxx += (" : " + environment.queries.get(c).getSource().name0 + " -> " + environment.queries.get(c).getTarget().name0);
				}

				frames.add(new Pair<String, JComponent>(xxx, top));
			} 
		}
	

	JFrame frame = null;
	
	public void display(String s, List<String> order) {
		final CardLayout cl = new CardLayout();
		final JPanel x = new JPanel(cl);
		frame = new JFrame();
		
		
		//List<Pair<String, JComponent>> list = new LinkedList<Pair<String, JComponent>>();
//		for (String p : order) {
//			list.add(new Pair<String, JComponent>(p, frames.get(p)));
//		}
//		Collections.sort(list, new Comparator<Pair<String, JComponent>>() {
//
//			@Override
//			public int compare(Pair<String, JComponent> arg0,
//					Pair<String, JComponent> arg1) {
//				String s1 = arg0.first.split(" ")[1];
//				String s2 = arg1.first.split(" ")[1];
//				return s1.compareTo(s2);
//			}
//			
//		});
		
		final Vector<String> ooo = new Vector<>();
		
		for (Pair<String, JComponent> p : frames) {
			x.add(p.second, p.first);
			ooo.add(p.first);
		}
		x.add(new JPanel(), "blank");
		cl.show(x, "blank");
		
		final JList<String> yyy = new JList<>(ooo);
		JPanel temp1 = new JPanel(new GridLayout(1,1));
		temp1.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), "Select:"));
		JScrollPane yyy1 = new JScrollPane(yyy);
		temp1.add(yyy1);
		temp1.setMinimumSize(new Dimension(200,600));
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
		frame = new JFrame(/*"Viewer for " + */s);
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
		frame.getRootPane().registerKeyboardAction(escListener,
	           ctrlW,
	            JComponent.WHEN_IN_FOCUSED_WINDOW);
		frame.getRootPane().registerKeyboardAction(escListener,
	            commandW,
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
