package fql.decl;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Paint;
import java.awt.Stroke;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.table.TableRowSorter;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.VertexLabelRenderer;
import fql.DEBUG;
import fql.FQLException;
import fql.Pair;
import fql.Triple;
import fql.cat.Arr;
import fql.cat.FinCat;

public class Transform {

	Instance src, dst;
	Map<String, Set<Pair<Object, Object>>> data;

	public Transform(Instance src, Instance dst,
			List<Pair<String, List<Pair<Object, Object>>>> b) {
		this.src = src;
		this.dst = dst;

		data = new HashMap<>();
		for (Pair<String, List<Pair<Object, Object>>> k : b) {
			if (src.thesig.isNode(k.first)) {
				data.put(k.first, new HashSet<>(k.second));
			} else {
				continue;
			}
		}

		validate();
	}

	public void validate() {
		
		for (Node n : src.thesig.nodes) {
			Set<Pair<Object, Object>> v = data.get(n.string);
			if (v == null) {
				throw new RuntimeException("Missing node: " + n);
			}
			for (Pair<Object, Object> k : src.data.get(n.string)) {
				try {
					lookup(data.get(n.string), k.first);
				} catch (RuntimeException re) {
					throw new RuntimeException("Not total: " + n.string + "\n\n" + this);
				}
			}
			for (Pair<Object, Object> k : v) {
				if (!src.data.get(n.string).contains(new Pair<>(k.first, k.first))) {
					throw new RuntimeException("Non-domain value in " + n + "\n\n" + this);
				}
				if (!dst.data.get(n.string).contains(new Pair<>(k.second, k.second))) {
					throw new RuntimeException("Non-range value in " + n + "\n\n" + this);					
				}
			}
		}
		
		for (Edge f : src.thesig.edges) {
			Set<Pair<Object, Object>> lhs = compose(data.get(f.source.string),
					dst.data.get(f.name));
			Set<Pair<Object, Object>> rhs = compose(src.data.get(f.name),
					data.get(f.target.string));

			if (!lhs.equals(rhs)) {
				throw new RuntimeException("Not respected on " + f + " in " + this);
			}
		}
		
		for (Node n : src.thesig.nodes) {
			Set<Pair<Object, Object>> N = src.data.get(n.string);
			for (Pair<Object, Object> id : N) {
				for (Attribute<Node> attr : src.thesig.attrsFor(n)) {
					Set<Pair<Object, Object>> a = src.data.get(attr.name);
					Object valSrc = lookup(a, id.first);
					
					Object trans_id = lookup(data.get(n.string), id.first);
					a = dst.data.get(attr.name);
					Object valDst = lookup(a, trans_id);
					
					if (!valSrc.equals(valDst)) {
						String xxx = "cannot pair (" + id.first + ", " + trans_id + "), not equal: att(" + id.first + ") = " + valSrc + " and att(" + trans_id + ") = " + valDst;
						throw new RuntimeException(xxx);
					}
				}
			}
		}
		
	}

	private Set<Pair<Object, Object>> compose(Set<Pair<Object, Object>> l,
			Set<Pair<Object, Object>> r) {
		Set<Pair<Object, Object>> ret = new HashSet<>();
		for (Pair<Object, Object> k : l) {
			ret.add(new Pair<>(k.first, lookup(r, k.second)));	
		}
		return ret;
	}

	public JPanel text() {
		JPanel ret = new JPanel(new GridLayout(1, 1));
		JTextArea area = new JTextArea(toString());
		ret.add(new JScrollPane(area));
		return ret;
	}

	public JPanel graphical(String src, String dst) {
		return makePanel(src, dst);
	}

	public JPanel view(String src_n, String dst_n) throws FQLException {
		List<JPanel> panels = new LinkedList<JPanel>();
		// Map<String, Set<Pair<String,String>>> data;
		LinkedList<String> sorted = new LinkedList<String>(data.keySet());
		Collections.sort(sorted, new Comparator<String>() {
			public int compare(String f1, String f2) {
				return f1.toString().compareTo(f2.toString());
			}
		});
		for (String k : sorted) {
			Set<Pair<Object, Object>> xxx = data.get(k);
			List<Pair<Object, Object>> table = new LinkedList<>(xxx);
			// Collections.sort(table, new Comparator<Pair<Object, Object>>()
			// {
			// public int compare(Pair<Object,Object> f1, Pair<Object,Object>
			// f2)
			// {
			// return f1.first.toString().compareTo(f2.first.toString());
			// }
			// });

			Object[][] arr = new Object[table.size()][2];
			int i = 0;
			for (Pair<Object, Object> p : table) {
				arr[i][0] = p.first;
				arr[i][1] = p.second;
				i++;
			}
			// Pair<String, String> cns = src.thesig.getColumnNames(k);
			JTable t = new JTable(arr, new Object[] { src_n, dst_n });
			// //t.setRowSelectionAllowed(false);
			// t.setColumnSelectionAllowed(false);
			// MouseListener[] listeners = t.getMouseListeners();
			// for (MouseListener l : listeners) {
			// t.removeMouseListener(l);
			// }
			TableRowSorter<?> sorter = new MyTableRowSorter(t.getModel());

			t.setRowSorter(sorter);
			sorter.allRowsChanged();
			sorter.toggleSortOrder(0);
			t.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
			JPanel p = new JPanel(new GridLayout(1, 1));
			p.add(new JScrollPane(t));
			p.setBorder(BorderFactory.createTitledBorder(
					BorderFactory.createEmptyBorder(2, 2, 2, 2), k));
			panels.add(p);
			p.setSize(60, 60);
		}

		int x = (int) Math.ceil(Math.sqrt(panels.size()));
		if (x == 0) {
			return new JPanel();
		}
		JPanel panel = new JPanel(new GridLayout(x, x));
		for (JPanel p : panels) {
			panel.add(p);
		}
		panel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		return panel;
	}
/*
	private List<Pair<Object, Object>> get(String k,
			List<Pair<String, List<Pair<Object, Object>>>> x) {
		for (Pair<String, List<Pair<Object, Object>>> k0 : x) {
			if (k0.first.equals(k)) {
				return k0.second;
			}
		}
		throw new RuntimeException();
	}

	private Collection<String> keySet(
			List<Pair<String, List<Pair<Object, Object>>>> d) {
		List<String> ret = new LinkedList<>();
		for (Pair<String, List<Pair<Object, Object>>> k : d) {
			ret.add(k.first);
		}
		return ret;
	}
	*/

	@Override
	public String toString() {
		String ret = "";

		boolean b = false;
		for (String k0 : data.keySet()) {
			Pair<String, Set<Pair<Object, Object>>> k = new Pair<>(k0, data.get(k0));
			if (b) {
				ret += ",\n";
			}
			b = true;

			String ret0 = "";
			boolean c = false;
			for (Pair<Object, Object> v : k.second) {
				if (c) {
					ret0 += ",";
				}
				c = true;
				ret0 += "(" + v.first + "," + v.second + ")";
			}

			ret += k.first + " -> {" + ret0 + "}";
		}
		return "{nodes\n" + ret + ";}";
	}

	private Pair<Graph<Triple<Node, Object, String>, Pair<Path, Integer>>, HashMap<Triple<Node, Object, String>, Map<Attribute<Node>, Object>>> build(
			String src_n, String dst_n) throws FQLException {
		FinCat<Node, Path> c = src.thesig.toCategory2().first;
		HashMap<Triple<Node, Object, String>, Map<Attribute<Node>, Object>> map = new HashMap<>();

		Graph<Triple<Node, Object, String>, Pair<Path, Integer>> g2 = new DirectedSparseMultigraph<>();
		for (Node n : c.objects) {
			for (Pair<Object, Object> o : src.data.get(n.string)) {
				Triple<Node, Object, String> xx = new Triple<>(n, o.first,
						src_n);
				g2.addVertex(xx);

				List<Attribute<Node>> attrs = src.thesig.attrsFor(n);
				Map<Attribute<Node>, Object> m = new HashMap<>();
				for (Attribute<Node> attr : attrs) {
					Object a = lookup(src.data.get(attr.name), o.first);
					m.put(attr, a);
				}
				map.put(xx, m);
			}
		}

		int j = 0;
		for (Triple<Node, Object, String> x : g2.getVertices()) {
			for (Triple<Node, Object, String> y : g2.getVertices()) {
				if (!x.third.equals(y.third)) {
					continue;
				}
				Set<Arr<Node, Path>> h = c.hom(x.first, y.first);
				for (Arr<Node, Path> arr : h) {
					if (c.isId(arr)) {
						continue;
					}
					if (!DEBUG.debug.ALL_GR_PATHS && arr.arr.path.size() != 1) {
						continue;
					}
					if (doLookup(src, arr.arr, x.second, y.second)) {
						g2.addEdge(new Pair<>(arr.arr, j++), x, y);
					}
				}
			}
		}

		for (Node n : c.objects) {
			for (Pair<Object, Object> o : dst.data.get(n.string)) {
				Triple<Node, Object, String> xx = new Triple<>(n, o.first,
						dst_n);
				g2.addVertex(xx);

				List<Attribute<Node>> attrs = dst.thesig.attrsFor(n);
				Map<Attribute<Node>, Object> m = new HashMap<>();
				for (Attribute<Node> attr : attrs) {
					Object a = lookup(dst.data.get(attr.name), o.first);
					m.put(attr, a);
				}
				map.put(xx, m);
			}
		}

		for (Triple<Node, Object, String> x : g2.getVertices()) {
			for (Triple<Node, Object, String> y : g2.getVertices()) {
				Set<Arr<Node, Path>> h = c.hom(x.first, y.first);
				for (Arr<Node, Path> arr : h) {
					if (c.isId(arr)) {
						continue;
					}
					if (!DEBUG.debug.ALL_GR_PATHS && arr.arr.path.size() != 1) {
						continue;
					}
					if (doLookup(dst, arr.arr, x.second, y.second)) {
						g2.addEdge(new Pair<>(arr.arr, j++), x, y);
					}
				}
			}
		}

		for (String k : data.keySet()) {
			Set<Pair<Object, Object>> v = data.get(k);
			for (Pair<Object, Object> i : v) {
				Node n = src.thesig.getNode(k);
				g2.addEdge(new Pair<Path, Integer>(null, j++), new Triple<>(n,
						i.first, src_n), new Triple<>(n, i.second, dst_n));
			}
		}

		return new Pair<>(g2, map);
	}

	private static Object lookup(Set<Pair<Object, Object>> set, Object first) {
		for (Pair<Object, Object> p : set) {
			if (p.first.equals(first)) {
				return p.second;
			}
		}
		throw new RuntimeException("not found: " + first + " in " + set);
	}

	private static boolean doLookup(Instance i, Path arr, Object x1, Object x2) {
		for (Pair<Object, Object> y : i.evaluate(arr)) {
			if (y.first.equals(x1) && y.second.equals(x2)) {
				return true;
			}
		}
		return false;
	}
	
	//public JComponent lowerComp() throws FQLException {
	//	JComponent c = src.thesig.pretty();
		//c.setMaximumSize(new Dimension(400,100));
	//	return c;
	//}

	public JComponent lowerComp() {
		int size = src.thesig.nodes.size();

		JPanel pan = new JPanel(new GridLayout(1, size));
		for (Node n : src.thesig.nodes) {
			JLabel l = new JLabel(n.string);
			l.setOpaque(true);
			l.setHorizontalAlignment(SwingConstants.CENTER);
			l.setBackground(src.thesig.colors.get(n.string));
			pan.add(l);
		}
		pan.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEmptyBorder(2, 2, 2, 2), "Legend"));
		
		return new JScrollPane(pan);
	} 
	

	public JPanel doView(
			final String src_n,
			final String dst_n,
			Graph<Triple<Node, Object, String>, Pair<Path, Integer>> first,
			HashMap<Triple<Node, Object, String>, Map<Attribute<Node>, Object>> second) throws FQLException {

		// HashMap<Pair<Node, Object>,String> map = new HashMap<>();
		JPanel cards = new JPanel(new CardLayout());

		// Layout<V, E>, BasicVisualizationServer<V,E>
		Layout<Triple<Node, Object, String>, Pair<Path, Integer>> layout = new FRLayout<>(
				first);
		// Layout<Pair<Node, Object>, Pair<Path, Integer>> layout = new
		// ISOMLayout<>(sgv);
		// Layout<String, String> layout = new CircleLayout(sgv);
		layout.setSize(new Dimension(600, 350));
		// BasicVisualizationServer<String, String> vv = new
		// BasicVisualizationServer<String, String>(
		// layout);
		VisualizationViewer<Triple<Node, Object, String>, Pair<Path, Integer>> vv = new VisualizationViewer<>(
				layout);
		//vv.setPreferredSize(new Dimension(600, 350));
		// Setup up a new vertex to paint transformer...
		Transformer<Triple<Node, Object, String>, Paint> vertexPaint = new Transformer<Triple<Node, Object, String>, Paint>() {
			public Paint transform(Triple<Node, Object, String> i) {
				return src.thesig.colors.get(i.first.string);
			}
		};
		DefaultModalGraphMouse<String, String> gm = new DefaultModalGraphMouse<>();
		gm.setMode(ModalGraphMouse.Mode.TRANSFORMING);
		vv.setGraphMouse(gm);
		gm.setMode(Mode.PICKING);
		// Set up a new stroke Transformer for the edges
		float dash[] = { 1.0f };
		final Stroke edgeStroke = new BasicStroke(0.5f, BasicStroke.CAP_BUTT,
				BasicStroke.JOIN_MITER, 10.0f, dash, 10.0f);
		// Transformer<String, Stroke> edgeStrokeTransformer = new
		// Transformer<String, Stroke>() {
		// public Stroke transform(String s) {
		// return edgeStroke;
		// }
		// };
		vv.getRenderContext().setVertexLabelRenderer(new MyVertexT(cards));
		final Stroke bs = new BasicStroke();
		Transformer<Pair<Path, Integer>, Stroke> edgeStrokeTransformer = new Transformer<Pair<Path, Integer>, Stroke>() {
			public Stroke transform(Pair<Path, Integer> s) {
				if (s.first == null) {
					return edgeStroke;
				}
				return bs;
			}
		};
		vv.getRenderContext().setVertexFillPaintTransformer(vertexPaint);
		vv.getRenderContext().setEdgeStrokeTransformer(edgeStrokeTransformer);
		// vv.getRenderContext().setVertexLabelTransformer(
		// new ToStringLabeller<String>());
		vv.getRenderContext().setEdgeLabelTransformer(
				new ToStringLabeller<Pair<Path, Integer>>() {

					@Override
					public String transform(Pair<Path, Integer> t) {
						if (t.first == null) {
							return "";
						}
						return t.first.toString();
					}

				});
		// new ToStringLabeller<String>());
		// vv.getRenderer().getVertexRenderer().
		// vv.getRenderContext().setLabelOffset(20);
		// vv.getRenderer().getVertexLabelRenderer().setPosition(Position.CNTR);

		vv.getRenderContext().setVertexLabelTransformer(
				new ToStringLabeller<Triple<Node, Object, String>>() {

					@Override
					public String transform(Triple<Node, Object, String> t) {
						return t.third + "." + t.first + "."
								+ t.second.toString();
					}

				});

		JPanel ret = new JPanel(new BorderLayout());
		JSplitPane pane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

		for (Triple<Node, Object, String> n : first.getVertices()) {
			Map<Attribute<Node>, Object> s = second.get(n);

			Object[] columnNames = new Object[s.keySet().size()];
			Object[][] rowData = new Object[1][s.keySet().size()];

			int i = 0;
			// for (Pair<Node, Object> k : map0.keySet()) {
			// Map<Attribute<Node>, Object> v = ma;
			for (Attribute<Node> a : s.keySet()) {
				columnNames[i] = a.name;
				rowData[0][i] = s.get(a);
				i++;
			}

			// }
			JPanel p = new JPanel(new GridLayout(1, 1));
			JTable table = new JTable(rowData, columnNames);
			JScrollPane jsp = new JScrollPane(table);
			p.setBorder(BorderFactory.createTitledBorder(
					BorderFactory.createEmptyBorder(), "Attributes for "
							+ n.second));

			p.add(jsp);
			cards.add(p, n.second.toString());
		}
		cards.add(new JPanel(), "blank");
		CardLayout cl = (CardLayout) (cards.getLayout());
		cl.show(cards, "blank");

		pane.add(new GraphZoomScrollPane(vv));
		pane.setResizeWeight(1.0d);
		pane.add(cards);

		cards.setPreferredSize(new Dimension(400, 100));

		ret.add(pane, BorderLayout.CENTER);
		ret.add(lowerComp(), BorderLayout.NORTH);
		ret.setBorder(BorderFactory.createEtchedBorder());
		return ret;
	}

	public JPanel makePanel(String src_n, String dst_n) {
		try {
			Pair<Graph<Triple<Node, Object, String>, Pair<Path, Integer>>, HashMap<Triple<Node, Object, String>, Map<Attribute<Node>, Object>>> g = build(
					src_n, dst_n);
			if (g.first.getVertexCount() == 0) {
				return new JPanel();
			}
			return doView(src_n, dst_n, g.first, g.second);
		} catch (FQLException e) {
			JPanel p = new JPanel(new GridLayout(1, 1));
			JTextArea a = new JTextArea(e.getMessage());
			p.add(new JScrollPane(a));
			return p;
		}

	}

	private static class MyVertexT implements VertexLabelRenderer {

		JPanel cards;

		public MyVertexT(JPanel cards) {
			this.cards = cards;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> Component getVertexLabelRendererComponent(JComponent arg0,
				Object arg1, Font arg2, boolean arg3, T arg4) {
			Triple<Node, Object, String> p = (Triple<Node, Object, String>) arg4;
			if (arg3) {
				CardLayout c = (CardLayout) cards.getLayout();
				c.show(cards, p.second.toString());
			}

			return new JLabel(p.second.toString());

		}
	}

}
