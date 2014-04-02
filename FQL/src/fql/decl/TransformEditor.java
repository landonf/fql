package fql.decl;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import fql.DEBUG;
import fql.FQLException;
import fql.Pair;

public class TransformEditor {

	public InstExp.Const inst1, inst2;
	public TransExp.Const trans;
	public Signature thesig;
	public String name;

	public Map<String, Set<Pair<Object, Object>>> d = new HashMap<>();

	public TransformEditor(String name, Signature sig, TransExp.Const trans, InstExp.Const inst1, InstExp.Const inst2)
			throws FQLException {
		this.trans = trans;
		this.thesig = sig;
		this.name = name;
		this.inst1 = inst1;
		this.inst2 = inst2;

		for (Node n : thesig.nodes) {
			d.put(n.string, new HashSet<Pair<Object, Object>>());
		}
		for (Pair<String, List<Pair<Object, Object>>> n : trans.objs) {
			Set<Pair<Object, Object>> p = d.get(n.first);
			if (p == null) {
				continue;
			}
			p.addAll(n.second);
		}
	}

	public List<Pair<Object, Object>> proj1(List<Pair<Object, Object>> x) {
		List<Pair<Object, Object>> ret = new LinkedList<>();

		for (Pair<Object, Object> k : x) {
			ret.add(new Pair<>(k.first, k.first));
		}

		return ret;
	}

	public List<Pair<Object, Object>> proj2(List<Pair<Object, Object>> x) {
		List<Pair<Object, Object>> ret = new LinkedList<>();

		for (Pair<Object, Object> k : x) {
			ret.add(new Pair<>(k.second, k.second));
		}

		return ret;
	}

	public Graph<String, String> build() {
		// Graph<V, E> where V is the type of the vertices

		Graph<String, String> g2 = new DirectedSparseMultigraph<String, String>();
		for (Node n : thesig.nodes) {
			g2.addVertex(n.string);
		}

		for (Edge e : thesig.edges) {
			g2.addEdge(e.name, e.source.string, e.target.string);
		}

		for (Attribute<Node> a : thesig.attrs) {
			g2.addVertex(a.name);
			g2.addEdge(a.name, a.source.string, a.name);
		}

		return g2;
	}

	JComponent makePanel() {
		Graph<String, String> g = build();
		if (g.getVertexCount() == 0) {
			JPanel p = new JPanel();
			p.setSize(new Dimension(600, 400));
			return p;
		}
		return doView(g);
	}

	@SuppressWarnings({ "rawtypes", "unchecked", "serial" })
	private JPanel doLower(String n) {
		JPanel ret = new JPanel(new GridLayout(1,1));
		ret.setBorder(BorderFactory.createEmptyBorder());
		
		JSplitPane outer = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		JSplitPane inner = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		outer.setBorder(BorderFactory.createEmptyBorder());
		inner.setBorder(BorderFactory.createEmptyBorder());
		outer.setResizeWeight(.5d);
		outer.setDividerLocation(.5d);
		inner.setResizeWeight(.5d);
		inner.setDividerLocation(.5d);

		JPanel p = new JPanel(new GridLayout(1,1));
		JScrollPane jsp = new JScrollPane();
		p.add(jsp);
		
		Vector arr = new Vector();
		List<Pair<Object, Object>> jjj = lookup(inst1.data, n);
		if (jjj == null) {
			 jjj = new LinkedList<Pair<Object, Object>>();
		}
		for (Pair<Object, Object> k : jjj) {
			Vector v = new Vector();
			v.add(k.first);
			List<Pair<Object, Object>> hhh = lookup(trans.objs, n);
			if (hhh == null) {
				hhh = new LinkedList<Pair<Object, Object>>();
			}
			Object uuu = lookup(hhh, k.first);
			if (uuu != null) {
				v.add(uuu);
			} else {
				v.add(null);
			}
			arr.add(v);
		}
		
		Vector cols3 = new Vector();
		cols3.add("ID in " + trans.src);
		cols3.add("ID in " + trans.dst);
		DefaultTableModel dtm = new DefaultTableModel(arr, cols3 );
		JTable foo = new JTable(dtm) {
			public Dimension getPreferredScrollableViewportSize() {
				Dimension d = getPreferredSize();
				return new Dimension(d.width, d.height);
			}
		};
		TableRowSorter<?> sorter2 = new MyTableRowSorter(foo.getModel());
		sorter2.toggleSortOrder(0);
		foo.setRowSorter(sorter2);
		sorter2.allRowsChanged();
		tj.put(n, foo);
		tjx.put(n, p);
		
		tjx.get(n).setBorder(
				BorderFactory.createTitledBorder(
						BorderFactory.createEmptyBorder(), name + "." + n
								+ " (" + dtm.getRowCount()
								+ " rows)"));
		
		jsp.setViewportView(tj.get(n));

		outer.add(inner);
		outer.add(p);
		
		inner.add(j1.get(n));
		inner.add(j2.get(n));
	
		ret.add(outer);
		
		//ret.add(new JLabel(n));
		return ret;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public TransExp.Const show() {
		String title = "Visual Editor for Transform " + name + " : " + trans.src + " -> " + trans.dst;
		vwr.setLayout(cards);
		vwr.add(new JPanel(), "");
		cards.show(vwr, "");

		prejoin(trans.src, inst1, j1);
		prejoin(trans.dst, inst2, j2);
		
		for (Node n : thesig.nodes) {
			joined.put(n.string, doLower(n.string));
			vwr.add(joined.get(n.string), n.string);
		}

		JComponent message = makePanel();

		int i = JOptionPane.showConfirmDialog(null, message, title,
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null);
		if (i != JOptionPane.OK_OPTION) {
			return null;
		}

		List<Pair<String, List<Pair<Object, Object>>>> nodes = new LinkedList<>();
	//	Map<String, List<Pair<Object, Object>>> map = new HashMap<>();

		for (Node n : thesig.nodes) {
			JTable t = tj.get(n.string);
			DefaultTableModel m = (DefaultTableModel) t.getModel();
			//int cols = m.getColumnCount();
			Vector<Vector> rows = m.getDataVector();
			List<Pair<Object, Object>> ids = new LinkedList<>();
			for (Vector row : rows) {
				Object id = row.get(0);
				Object id2 = row.get(1);
				ids.add(new Pair<Object, Object>(id, id2));

/*				for (int col = 1; col < cols; col++) {
					String colname = m.getColumnName(col);
					Object val = row.get(col);
					if (!map.containsKey(colname)) {
						map.put(colname, new LinkedList<Pair<Object, Object>>());
					}
					List<Pair<Object, Object>> attr = map.get(colname);
					attr.add(new Pair<Object, Object>(id, val));
				} */
			}
			nodes.add(new Pair<>(n.string, ids));
		}

/*		List<Pair<String, List<Pair<Object, Object>>>> attrs = new LinkedList<>();
		List<Pair<String, List<Pair<Object, Object>>>> arrows = new LinkedList<>();

		for (Edge e : thesig.edges) {
			List<Pair<Object, Object>> g = map.get(e.name);
			arrows.add(new Pair<>(e.name, g));
		}
		for (Attribute<Node> e : thesig.attrs) {
			List<Pair<Object, Object>> g = map.get(e.name);
			attrs.add(new Pair<>(e.name, g));
		} */

//		System.out.println(nodes);
	//	System.out.println(attrs);
//		System.out.println(arrows);
		return new TransExp.Const(nodes, trans.src, trans.dst);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public JComponent doView(
	/* final Environment env , *//* final Color color */Graph<String, String> sgv) {
		try {
			Class<?> c = Class.forName(DEBUG.layout_prefix
					+ DEBUG.debug.inst_graph);
			Constructor<?> x = c.getConstructor(Graph.class);
			Layout<String, String> layout = (Layout<String, String>) x
					.newInstance(sgv);

			layout.setSize(new Dimension(500, 340));
			final VisualizationViewer<String, String> vv = new VisualizationViewer<String, String>(
					layout);
			Transformer<String, Paint> vertexPaint = new Transformer<String, Paint>() {
				public Paint transform(String i) {
					if (thesig.isAttribute(i)) {
						return UIManager.getColor("Panel.background");
					} else {
						return thesig.colors.get(i);
					}
					// return color;
				}
			};
			DefaultModalGraphMouse<String, String> gm = new DefaultModalGraphMouse<>();
			// gm.setMode(ModalGraphMouse.Mode.TRANSFORMING);
			vv.setGraphMouse(gm);
			gm.setMode(Mode.PICKING);
			vv.getRenderContext().setVertexFillPaintTransformer(vertexPaint);
			vv.getRenderContext().setVertexLabelTransformer(new Transformer() {

				@Override
				public Object transform(Object arg0) {
					String str = (String) arg0;
					if (thesig.isAttribute(str)) {
						str = thesig.getTypeLabel(str);
					}
					return str;
				}

			});

			vv.getPickedVertexState().addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() != ItemEvent.SELECTED) {
						return;
					}
					vv.getPickedEdgeState().clear();
					String str = ((String) e.getItem());

					if (thesig.isNode(str)) {
						cards.show(vwr, str);
						card = str;
					}
				}

			});
			vv.getRenderContext().setLabelOffset(20);
			vv.getRenderContext().setEdgeLabelTransformer(new Transformer() {

				@Override
				public Object transform(Object arg0) {
					String s = arg0.toString();
					if (thesig.isAttribute(s)) {
						return "";
					}
					return s;
				}

			});

			float dash[] = { 1.0f };
			final Stroke edgeStroke = new BasicStroke(0.5f,
					BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash,
					10.0f);
			final Stroke bs = new BasicStroke();
			Transformer<String, Stroke> edgeStrokeTransformer = new Transformer<String, Stroke>() {
				public Stroke transform(String s) {
					if (thesig.isAttribute(s)) {
						return edgeStroke;
					}
					return bs;
				}
			};

			vv.getRenderContext().setEdgeStrokeTransformer(
					edgeStrokeTransformer);
			vv.getRenderContext().setVertexLabelTransformer(
					new ToStringLabeller<String>());

			GraphZoomScrollPane zzz = new GraphZoomScrollPane(vv);
			zzz.setPreferredSize(new Dimension(600, 400));
			vwr.setPreferredSize(new Dimension(600, 200));
			// JPanel newthing = new JPanel(new GridLayout(2,1));

			JSplitPane newthing = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
			newthing.setResizeWeight(.5d);
			newthing.setDividerLocation(.5d);

			// setDividerLocation(.9d);
			newthing.add(zzz);

			JPanel yyy = new JPanel(new FlowLayout(FlowLayout.LEFT));
			JButton ar = new JButton("Add Row");
			JButton dr = new JButton("Delete Rows");
			ar.setPreferredSize(dr.getPreferredSize());
			yyy.add(ar);
			yyy.add(dr);

			JPanel xxx = new JPanel(new BorderLayout());
			xxx.add(vwr, BorderLayout.CENTER);
			xxx.add(yyy, BorderLayout.SOUTH);

			newthing.add(xxx);

			newthing.resetToPreferredSizes();

			dr.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					JTable t = tj.get(card);
					int[] i = t.getSelectedRows();
					int j = 0;
					DefaultTableModel dtm = (DefaultTableModel) t.getModel();
					for (int x : i) {
						dtm.removeRow(x - (j++));
					}
					tjx.get(card).setBorder(
							BorderFactory.createTitledBorder(
									BorderFactory.createEmptyBorder(), name + "." + card
											+ " (" + dtm.getRowCount()
											+ " rows)"));
				}
			});

			ar.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					JTable t = tj.get(card);
					DefaultTableModel dtm = (DefaultTableModel) t.getModel();
					dtm.addRow((Vector) null);
					tjx.get(card).setBorder(
							BorderFactory.createTitledBorder(
									BorderFactory.createEmptyBorder(), name + "." + card
											+ " (" + dtm.getRowCount()
											+ " rows)"));
				}
			});
			// xxx.setMaximumSize(new Dimension(400,400));
			// return xxx;
			return newthing;
		} catch (Throwable t) {
			t.printStackTrace();
			throw new RuntimeException();
		}
	}

	
	private static <X,Y> Y lookup(List<Pair<X,Y>> set, X x) {
		for (Pair<X, Y> k : set) {
			if (k.first.equals(x)) {
				return k.second;
			}
		}
		return null;
	}
	JPanel vwr = new JPanel();
	CardLayout cards = new CardLayout();
	Map<String, JTable> tj = new HashMap<>();
	Map<String, JPanel> j1 = new HashMap<>();
	Map<String, JPanel> j2 = new HashMap<>();
	Map<String, JPanel> joined = new HashMap<>();
	Map<String, JPanel> tjx = new HashMap<>();
//	Map<String, JPanel> joined2y = new HashMap<>();
	
	String card = "";

	private void prejoin(String pre, InstExp.Const I, Map<String, JPanel> pans) {
		
		Map<String, Map<String, Set<Pair<Object, Object>>>> jnd = new HashMap<>();
		Map<String, Set<Pair<Object, Object>>> nd = new HashMap<>();

		List<String> names = new LinkedList<>();

		for (Node n : thesig.nodes) {
			List<Pair<Object, Object>> xxx = lookup(I.data, n.string);
			nd.put(n.string, new HashSet<>(xxx));
			jnd.put(n.string, new HashMap<String, Set<Pair<Object, Object>>>());
			names.add(n.string);
		}

		for (Edge e : thesig.edges) {
			jnd.get(e.source.string).put(e.name, new HashSet<>(lookup(I.data, e.name)));
			// names.add(e.name);
		}

		for (Attribute<Node> a : thesig.attrs) {
			jnd.get(a.source.string).put(a.name, new HashSet<>(lookup(I.data, a.name)));
			// names.add(a.name);
		}

		Comparator<String> strcmp = new Comparator<String>() {
			public int compare(String f1, String f2) {
				return f1.compareTo(f2);
			}
		};
		Collections.sort(names, strcmp);
		makejoined(pre, pans, jnd, nd, names);

	}

	@SuppressWarnings("serial")
	private void makejoined(String pre,
			Map<String, JPanel> pans,
			Map<String, Map<String, Set<Pair<Object, Object>>>> joined,
			Map<String, Set<Pair<Object, Object>>> nd, List<String> names) {
		Comparator<String> strcmp = new Comparator<String>() {
			public int compare(String f1, String f2) {
				return f1.compareTo(f2);
			}
		};
//		Map<String, JTable> ret = new HashMap<>();
		for (String name : names) {
			// System.out.println("Name " + name);
			Map<String, Set<Pair<Object, Object>>> m = joined.get(name);
			// System.out.println("m " + m);
			Set<Pair<Object, Object>> ids = nd.get(name);
			// System.out.println("ids " + ids);
			Object[][] arr = new Object[ids.size()][m.size() + 1];
			Set<String> cols = m.keySet();
			// System.out.println("cols " + cols);
			List<String> cols2 = new LinkedList<>(cols);
			Collections.sort(cols2, strcmp);
			cols2.add(0, "ID");
			// System.out.println("cols2 " + cols2);
			Object[] cols3 = cols2.toArray();
			// System.out.println("cols3 " + cols3);

			int i = 0;
			for (Pair<Object, Object> id : ids) {
				// System.out.println("id " + id);
				arr[i][0] = id.first;
				// System.out.println(" i " + i + " j " + 0 + " val " +
				// arr[i][0]);

				int j = 1;
				for (String col : cols2) {
					if (col.equals("ID")) {
						continue;
					}
					// System.out.println("col " + col);
					Set<Pair<Object, Object>> coldata = m.get(col);
					for (Pair<Object, Object> p : coldata) {
						// System.out.println("p " + p);
						if (p.first.equals(id.first)) {
							arr[i][j] = p.second;
							// System.out.println(" i " + i + " j " + j +
							// " val " + arr[i][j]);
							break;
						}
					}
					j++;
				}
				i++;
			}

			// Arrays.sort(arr, new Comparator<Object[]>() {
			//
			// @Override
			// public int compare(Object[] o1, Object[] o2) {
			// return o1[0].toString().compareTo(o2[0].toString());
			// }
			//
			// });

			// JTable t = new JTable(arr, cols3) {
			// public Dimension getPreferredScrollableViewportSize() {
			// Dimension d = getPreferredSize();
			// return new Dimension(d.width, d.height);
			// }
			// };

			// cards.(name, t);

			// foo and t are for the graph and tabular pane, resp
			DefaultTableModel dtm = new DefaultTableModel(arr, cols3);
			JTable foo = new JTable(dtm) {
				public Dimension getPreferredScrollableViewportSize() {
					Dimension d = getPreferredSize();
					return new Dimension(d.width, d.height);
				}
			};
			JPanel p = new JPanel(new GridLayout(1, 1));
			// p.add(t);

			TableRowSorter<?> sorter2 = new MyTableRowSorter(foo.getModel());

			sorter2.toggleSortOrder(0);
			foo.setRowSorter(sorter2);
			sorter2.allRowsChanged();
			// foo.set
			// foo.setAutoCreateRowSorter(true);
			p.add(new JScrollPane(foo));

			// p.setMaximumSize(new Dimension(200,200));
			p.setBorder(BorderFactory.createTitledBorder(
					BorderFactory.createEmptyBorder(), pre + "." + name + " (" + ids.size()
							+ " rows)"));
			//vwr.add(p, name);
			//pans.put(name, p);
			// foo.setMaximumSize(new Dimension(600,200));
			//JScrollPane jsp = new JScrollPane(foo);
			//JPanel bar = new JPanel(new GridLayout(1,1));
			//bar.add(jsp);
			pans.put(name, p);
		}

//		return ret;
	}

}
