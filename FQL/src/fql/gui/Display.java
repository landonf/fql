package fql.gui;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
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

	Map<String, JComponent> frames;

	public Display(Environment environment, List<String> commands)
			throws FQLException {
		frames = new HashMap<String, JComponent>();

		for (String c : commands) {
				Viewable<?> view = environment.get(c);
	
				JTabbedPane px = new JTabbedPane();
				
				JPanel gp = view.pretty();
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

				frames.put(xxx, top);
			} 
		}
	

	JFrame frame = null;
	
	public void display() {
		final CardLayout cl = new CardLayout();
		final JPanel x = new JPanel(cl);
		frame = new JFrame();
		
		List<Pair<String, JComponent>> list = new LinkedList<Pair<String, JComponent>>();
		for (Entry<String, JComponent> p : frames.entrySet()) {
			list.add(new Pair<String, JComponent>(p.getKey(), p.getValue()));
		}
		Collections.sort(list, new Comparator<Pair<String, JComponent>>() {

			@Override
			public int compare(Pair<String, JComponent> arg0,
					Pair<String, JComponent> arg1) {
				String s1 = arg0.first.split(" ")[1];
				String s2 = arg1.first.split(" ")[1];
				return s1.compareTo(s2);
			}
			
		});
		
		final Vector<String> ooo = new Vector<>();
		
		for (Pair<String, JComponent> p : list) {
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
		
		frame = new JFrame("Viewer");
		px.add(temp1);
		px.add(x);
		frame.setContentPane(px);
		frame.setSize(850, 600);
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
