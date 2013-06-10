package fql.examples;

import java.awt.GridLayout;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import fql.gui.FQLTextPanel;

public class ExampleMaker {

	static FQLTextPanel p = new FQLTextPanel("input", "");
	static FQLTextPanel q = new FQLTextPanel("output", "");
	
	static JPanel make() {
		JPanel ret = new JPanel(new GridLayout(1,1));
		
		JSplitPane pane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		pane.setDividerLocation(0.5);
		pane.add(p);
		pane.add(q);
		
		ret.add(pane);
		
		return ret;
	}
	
	public static void main(String[] args) {
		JFrame f = new JFrame("Example Maker");	
		
		MenuBar mb = new MenuBar();
		Menu m = new Menu("Run");
		MenuItem i = new MenuItem("Run");
		mb.add(m);
		m.add(i);
		f.setMenuBar(mb);
		
		i.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				q.setText(run(p.getText()));
			}

			
		});
		
		f.setContentPane(make());
		f.setSize(600, 600);
		f.setVisible(true);
		
	}

	protected static String run(String s) {
		BufferedReader br = new BufferedReader(new StringReader(s.trim()));
		StringBuffer sb = new StringBuffer();
		String l;
		try {
			boolean first = true;
			while ((l = br.readLine()) != null) {
				if (!first) {
					sb.append("+ \"\\n");
				} else {
					first = false;
					sb.append("\"");
				}
				sb.append(l);
				sb.append("\"");
				sb.append("\n");				
			}
			return sb.toString();
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
		
	}
	
}
