package fql;

import java.awt.GridLayout;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

public class Util {

	private static JPanel makeRowOrCol(List<JComponent> list, int orientation) {
		if (list.size() == 0) {
			JPanel ret = new JPanel();
			ret.setBorder(BorderFactory.createEmptyBorder());
			return ret;
		}
		JPanel ret = new JPanel(new GridLayout(1,1));
		if (list.size() == 1) {
			ret.add(list.get(0));
			return ret;
		}
		Iterator<JComponent> it = list.iterator();
		JComponent sofar = it.next();
		double n = 2;
		while (it.hasNext()) {
			JSplitPane jsp = new JSplitPane(orientation);
			jsp.setResizeWeight(1.0d / n);
			jsp.add(sofar);
			jsp.add(it.next());
			jsp.setDividerSize(2);
			jsp.setBorder(BorderFactory.createEmptyBorder());
			sofar = jsp;
			n++;
		}
		ret.add(sofar);
		return ret;
	}
	
	public static JPanel makeGrid(List<JComponent> list) {
		int n = (int) Math.ceil(Math.sqrt(list.size()));

		List<JComponent> list2 = new LinkedList<>();
		for (int i = 0; i < list.size(); i += n) {
			int end = Math.min(list.size(), i+n); 
			list2.add(makeRowOrCol(list.subList(i, end), JSplitPane.HORIZONTAL_SPLIT));
		}
		
		JScrollPane jsp = new JScrollPane(makeRowOrCol(list2, JSplitPane.VERTICAL_SPLIT));
		JPanel ret = new JPanel(new GridLayout(1,1));
		ret.add(jsp);
		return ret;
	}
	
}
