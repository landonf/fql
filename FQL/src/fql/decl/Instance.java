package fql.decl;

import java.awt.BasicStroke;
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
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.table.TableRowSorter;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.algorithms.layout.ISOMLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.VertexLabelRenderer;
import fql.FQLException;
import fql.Pair;
import fql.cat.Arr;
import fql.cat.FDM;
import fql.cat.FinCat;
import fql.cat.Inst;
import fql.cat.Value;
import fql.gui.CategoryOfElements;
import fql.sql.PSM;
import fql.sql.PSMGen;
import fql.sql.PSMInterp;
import fql.sql.Relationalizer;

public class Instance  {

	// public static String data(String s) {
	// return s + " data";
	// }

//	public String name;
	
	//TODO better drop handling, by kind, visitor
	
	public void conformsTo(Signature s) throws FQLException {
//		System.out.println("Checking " + this + " against " + s);
		for (Node n : s.nodes) {
			Set<Pair<Object, Object>> i = data.get(n.string);
			if (i == null) {
				throw new FQLException("Missing node table " + n.string
						+ " in " + this);
			}
			for (Pair<Object, Object> p : i) {
				if (p.first == null || p.second == null) {
					throw new FQLException("Null data in " + this);
				}
				if (!p.first.equals(p.second)) {
					throw new FQLException("Not reflexive: " 
							+ s + " and " + this);
				}
			}
		}
		for (Attribute<Node> a : s.attrs) {
			Set<Pair<Object, Object>> i = data.get(a.name);
			if (i == null) {
				throw new FQLException("Missing Attribute<Node> table " + a.name
						+ " in " + this);
			}

			HashSet<Object> x = new HashSet<>();
			for (Pair<Object, Object> p : i) {
				x.add(p.first);
			}
			if (data.get(a.source.string).size() != x.size()) {
				throw new RuntimeException(
						"Instance " + this + " does not map all domain values in " + a.name);
			}

			for (Pair<Object, Object> p1 : i) {
				for (Pair<Object, Object> p2 : i) {
					if (p1.first.equals(p2.first)) {
						if (!p1.second.equals(p2.second)) {
							throw new FQLException("In " + this + ", not functional: "
									+ " in " + s);
						}
					}
				}
				// functional

				if (!contained(p1.first, data.get(a.source.string))) {
					throw new FQLException("Domain has non foreign key: "
							+ s + " and " + this);
				}
				if (!a.target.in(p1.second)) {
					throw new FQLException("Not a " + a.target + ": " + p1.second);
				}
			}
		}
		for (Edge e : s.edges) {
			Set<Pair<Object, Object>> i = data.get(e.name);
			if (i == null) {
				throw new FQLException("Missing edge table " + e.name + " in "
						+ this);
			}

			HashSet<Object> x = new HashSet<>();
			for (Pair<Object, Object> p : i) {
				x.add(p.first);
			}
			if (data.get(e.source.string).size() != x.size()) {
				throw new FQLException(
						"Instance does not map all domain values in " + e.name
								+ " in " + this);
			}

			for (Pair<Object, Object> p1 : i) {
				for (Pair<Object, Object> p2 : i) {
					if (p1.first.equals(p2.first)) {
						if (!p1.second.equals(p2.second)) {
							throw new FQLException("Not functional: " 
									+ " in " + s + " and " + this);
						}
					}
				}
				// functional

				if (!contained(p1.first, data.get(e.source.string))) {
					throw new FQLException("Domain has non foreign key: "
							+ p1.first + " in " + e.source.string);
				}
				if (!contained(p1.second, data.get(e.target.string))) {
					throw new FQLException("Range has non foreign key: "
							+ p1.second + " in " + e.target.string);
				}
			}
		}
		for (Eq eq : s.eqs) {
			Set<Pair<Object, Object>> lhs = evaluate(eq.lhs);
			Set<Pair<Object, Object>> rhs = evaluate(eq.rhs);
			if (!lhs.equals(rhs)) {
				throw new FQLException("Violates constraints: " + s + "\n\n eq is " + eq
						+ "\nlhs is " + lhs + "\n\nrhs is " + rhs);
			}
		}
/*
		if (DEBUG.VALIDATE_WITH_EDS) {
			validateUsingEDs();
		}
		*/
		// toFunctor();
	}
/*
	private void validateUsingEDs() throws FQLException {
		//System.out.println("Validating " + this);
		for (EmbeddedDependency ed : thesig.toED("")) {
			if (!ED.from(ed).holds(data)) {
				throw new FQLException("ED constraint violation in " + this + ": " + ed + "\n" + ED.from(ed) + "\n" + ED.conv(data));
			}
		}		
	}
	*/

	public Set<Pair<Object, Object>> evaluate(Path p) {
		Set<Pair<Object, Object>> x = data.get(p.source.string);
		if (x == null) {
			throw new RuntimeException("Couldnt find " + p.source.string);
		}
		for (Edge e : p.path) {
			if (data.get(e.name) == null) {
				throw new RuntimeException("Couldnt find " + e.name);
			}

			x = compose(x, data.get(e.name));
		}
		return x;
	}

	public static Set<Pair<Object, Object>> compose(
			Set<Pair<Object, Object>> x, Set<Pair<Object, Object>> y) {
		Set<Pair<Object, Object>> ret = new HashSet<>();

		for (Pair<Object, Object> p1 : x) {
			for (Pair<Object, Object> p2 : y) {
				if (p1.second.equals(p2.first)) {
					Pair<Object, Object> p = new Pair<>(p1.first, p2.second);
					ret.add(p);
				}
			}
		}
		return ret;
	}

	private boolean contained(Object second, Set<Pair<Object, Object>> set) {
		for (Pair<Object, Object> p : set) {
			if (p.first.equals(second) && p.second.equals(second)) {
				return true;
			}
		}
		return false;
	}

	public Map<String, Set<Pair<Object, Object>>> data;

	public Signature thesig;

	public Instance(Signature thesig,
			Map<String, Set<Pair<Object, Object>>> data) throws FQLException {
		this(thesig, degraph(data));
	}

	private static List<Pair<String, List<Pair<Object, Object>>>> degraph(
			Map<String, Set<Pair<Object, Object>>> data2) {
		List<Pair<String, List<Pair<Object, Object>>>> ret = new LinkedList<>();
		for (Entry<String, Set<Pair<Object, Object>>> e : data2.entrySet()) {
			ret.add(new Pair<String, List<Pair<Object, Object>>>(e.getKey(),
					new LinkedList<>(e.getValue())));
		}
		return ret;
	}

	private boolean external = false;

	public boolean isExternal() {
		return external;
	}

	public Instance(Signature thesig) throws FQLException {
		//this.name = n;
		this.data = new HashMap<>();
		this.external = true;
		for (Node node : thesig.nodes) {
			this.data.put(node.string, new HashSet<Pair<Object, Object>>());
		}
		for (Edge e : thesig.edges) {
			this.data.put(e.name, new HashSet<Pair<Object, Object>>());
		}
		for (Attribute<Node> a : thesig.attrs) {
			this.data.put(a.name, new HashSet<Pair<Object, Object>>());
		}
		this.thesig = thesig;
		if (!typeCheck(thesig)) {
			throw new FQLException("Type-checking failure " + this);
		}
		conformsTo(thesig);
	}

	public Instance(Signature thesig,
			List<Pair<String, List<Pair<Object, Object>>>> data)
			throws FQLException {
		//this.name = n;
		this.thesig = thesig;
		this.data = new HashMap<>();
		for (Node node : thesig.nodes) {
			this.data.put(node.string, makeFirst(node.string, data));
			// this.data.put(data(node.string), lookup(node.string, data));
		}
		for (Edge e : thesig.edges) {
			this.data.put(e.name, lookup(e.name, data));
		}
		for (Attribute<Node> a : thesig.attrs) {
			this.data.put(a.name, lookup(a.name, data));
		}
		if (!typeCheck(thesig)) {
			throw new FQLException("Type-checking failure " + this);
		}
		conformsTo(thesig);
	}

	private Set<Pair<Object, Object>> makeFirst(String string,
			List<Pair<String, List<Pair<Object, Object>>>> data2) {
		for (Pair<String, List<Pair<Object, Object>>> p : data2) {
			if (string.equals(p.first)) {
				return secol(p.second);
			}
		}
		throw new RuntimeException("conformsTo failure: cannot find " + string + " in " + data2
				);
	}

	private Set<Pair<Object, Object>> secol(List<Pair<Object, Object>> second) {
		Set<Pair<Object, Object>> ret = new HashSet<>();
		for (Pair<Object, Object> p : second) {
			ret.add(new Pair<>(p.first, p.first));
		}
		return ret;
	}

	private Set<Pair<Object, Object>> lookup(String n,
			List<Pair<String, List<Pair<Object, Object>>>> data2)
			throws FQLException {
		for (Pair<String, List<Pair<Object, Object>>> p : data2) {
			if (n.equals(p.first)) {
				return new HashSet<>(p.second);
			}
		}
		throw new FQLException("cannot find " + n + " in " + this);
	}

	// public Instance(String name, Query thequery, Instance theinstance)
	// throws FQLException {
	// if (!thequery.getSource().equals(theinstance.thesig)) {
	// throw new FQLException("Incompatible types. Expected "
	// + thequery.getSource() + " received " + theinstance.thesig);
	// }
	// thesig = thequery.getTarget();
	// data = thequery.eval(theinstance);
	// conformsTo(thesig);
	//
	// }
	//
	// public Instance(String name, Mapping m, Instance i, String type)
	// throws FQLException {
	// if (type.equals("delta")) {
	// if (!m.target.equals(i.thesig)) {
	// throw new FQLException("Incompatible types. Expected "
	// + m.target + " received " + i.thesig);
	// }
	// thesig = m.source;
	// data = m.evalDelta(i);
	// conformsTo(thesig);
	//
	// } else if (type.equals("sigma")) {
	// if (!m.source.equals(i.thesig)) {
	// throw new FQLException("Incompatible types. Expected "
	// + m.source + " received " + i.thesig);
	// }
	// thesig = m.target;
	// data = m.evalSigma(i);
	//
	// conformsTo(thesig);
	//
	// } else if (type.equals("pi")) {
	// if (!m.source.equals(i.thesig)) {
	// throw new FQLException("Incompatible types. Expected "
	// + m.source + " received " + i.thesig);
	// }
	// thesig = m.target;
	// data = m.evalPi(i);
	// conformsTo(thesig);
	//
	// } else {
	// throw new FQLException("Unknown type " + type);
	// }
	// toFunctor().morphs(toFunctor(), toFunctor());
	// }

	// this is the json one
/*	public Instance(
			Signature sig,
			List<Pair<String, List<Object>>> ob,
			List<Pair<Pair<Pair<Object, Object>, String>, List<Pair<Object, Object>>>> mo)
			throws FQLException {

		this(null, sig, jsonmap(ob, mo));
	} */
/*
	private static Map<String, Set<Pair<Object, Object>>> jsonmap(
			List<Pair<String, List<Object>>> ob,
			List<Pair<Pair<Pair<Object, Object>, String>, List<Pair<Object, Object>>>> mo) {
		Map<String, Set<Pair<Object, Object>>> map = new HashMap<>();
		for (Pair<String, List<Object>> o : ob) {
			map.put(o.first, dupl(o.second));
		}
		for (Pair<Pair<Pair<Object, Object>, String>, List<Pair<Object, Object>>> o : mo) {
			String arr = o.first.second;
			Set<Pair<Object, Object>> set = map.get(arr);
			if (set == null) {
				set = new HashSet<>();
				map.put(arr, set);
			}
			for (Pair<Object, Object> oo : o.second) {
				set.add(oo);
			}
		}
		return map;
	}
*/
	/*
	private static <X> Set<Pair<X, X>> dupl(List<X> x) {
		Set<Pair<X, X>> ret = new HashSet<>();
		for (X s : x) {
			ret.add(new Pair<>(s, s));
		}
		return ret;
	}
	*/

	private boolean typeCheck(Signature thesig2) {
		for (String s : data.keySet()) {
			if (!thesig2.contains(s) && !s.contains(" ")) {
				return false;
			}
		}
		for (String s : thesig2.all()) {
			if (null == data.get(s)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((data == null) ? 0 : data.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Instance other = (Instance) obj;
		if (data == null) {
			if (other.data != null)
				return false;
		} else if (!data.equals(other.data))
			return false;
		return true;
	}
	
	public String quickPrint() {
		return data.toString();
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer("{\n");

		boolean first = true;
		sb.append("nodes\n");
		for (Node k : thesig.nodes) {
			Set<Pair<Object, Object>> v = data.get(k.string);
			if (!first) {
				sb.append(",\n");
			}
			first = false;
			sb.append(k.string);
			sb.append(" -> { ");
			sb.append(printNode(v));
			sb.append(" }");
		}
		sb.append("\n ;\n");

		first = true;
		sb.append("attributes\n");
		for (Attribute<Node> k : thesig.attrs) {
			Set<Pair<Object, Object>> v = data.get(k.name);
			if (!first) {
				sb.append(",\n");
			}
			first = false;
			sb.append(k.name);
			sb.append(" -> { ");
			sb.append(printSet(v));
			sb.append(" }");
		}
		sb.append("\n ;\n");

		first = true;
		sb.append("arrows\n");
		for (Edge k : thesig.edges) {
			Set<Pair<Object, Object>> v = data.get(k.name);
			if (!first) {
				sb.append(",\n");
			}
			first = false;
			sb.append(k.name);
			sb.append(" -> { ");
			sb.append(printSet(v));
			sb.append(" }");
		}

		sb.append(";\n}");
		return sb.toString();

	}

	private String printNode(Set<Pair<Object, Object>> v) {
		StringBuffer sb = new StringBuffer();
		boolean first = true;
		for (Pair<Object, Object> p : v) {
			if (!first) {
				sb.append(", ");
			}
			first = false;
			sb.append(maybeQuote(p.first.toString()));
		}
		return sb.toString();
	}

	private String printSet(Set<Pair<Object, Object>> v) {
		StringBuffer sb = new StringBuffer();
		boolean first = true;
		if (v == null) {
			return "null";
		}
		for (Pair<Object, Object> p : v) {
			if (!first) {
				sb.append(", ");
			}
			first = false;
			sb.append("(");
			sb.append(maybeQuote(p.first.toString()));
			sb.append(",");
			sb.append(maybeQuote(p.second.toString()));
			sb.append(")");
		}
		return sb.toString();
	}

	private String maybeQuote(String s) {
		if (s.contains(" ") || s.contains("\n") || s.contains("\r")
				|| s.contains("\t")) {
			return "\"" + s + "\"";
		}
		return s;
	}

	public JPanel view() throws FQLException {
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
			Pair<String, String> cns = thesig.getColumnNames(k);
			JTable t = new JTable(arr, new Object[] { cns.first, cns.second });
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
					BorderFactory.createEmptyBorder(2, 2, 2, 2), k + "   ("
							+ xxx.size() + " rows)"));
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
		panel.setBorder(BorderFactory.createEtchedBorder());
		return panel;
	}

	public JPanel join() throws FQLException {
		// Map<String, Set<Pair<String,String>>> data;

		prejoin();

		List<JPanel> pans = makePanels();

		int x = (int) Math.ceil(Math.sqrt(pans.size()));
		JPanel panel;
		if (x == 0) {
			panel = new JPanel();
		} else {
			panel = new JPanel(new GridLayout(x, x));
		}
		for (JPanel p : pans) {
			panel.add(p);
		}
		panel.setBorder(BorderFactory.createEtchedBorder());
		return panel;
	}

	private List<JPanel> makePanels() {
		List<JPanel> ret = new LinkedList<>();

		Comparator<String> strcmp = new Comparator<String>() {
			public int compare(String f1, String f2) {
				return f1.compareTo(f2);
			}
		};

		List<String> xxx = new LinkedList<>(joined.keySet());
		Collections.sort(xxx, strcmp);

		for (String name : xxx) {
			JTable t = joined.get(name);
			JPanel p = new JPanel(new GridLayout(1, 1));
			// p.add(t);
			p.add(new JScrollPane(t));
			// p.setMaximumSize(new Dimension(200,200));
			p.setBorder(BorderFactory.createTitledBorder(
					BorderFactory.createEmptyBorder(), name + "   ("
							+ data.get(name).size() + " rows)"));
			ret.add(p);
		}

		return ret;
	}

	private void prejoin() {
		if (joined != null) {
			return;
		}
		vwr.setLayout(cards);
		vwr.add(new JPanel(), "");
		cards.show(vwr, "");
		Map<String, Map<String, Set<Pair<Object, Object>>>> jnd = new HashMap<>();
		Map<String, Set<Pair<Object, Object>>> nd = new HashMap<>();

		List<String> names = new LinkedList<>();

		for (Node n : thesig.nodes) {
			nd.put(n.string, data.get(n.string));
			jnd.put(n.string, new HashMap<String, Set<Pair<Object, Object>>>());
			names.add(n.string);
		}

		for (Edge e : thesig.edges) {
			jnd.get(e.source.string).put(e.name, data.get(e.name));
			// names.add(e.name);
		}

		for (Attribute<Node> a : thesig.attrs) {
			jnd.get(a.source.string).put(a.name, data.get(a.name));
		}

		Comparator<String> strcmp = new Comparator<String>() {
			public int compare(String f1, String f2) {
				return f1.compareTo(f2);
			}
		};
		Collections.sort(names, strcmp);

		joined = makejoined(jnd, nd, names);

	}

	@SuppressWarnings("serial")
	private Map<String, JTable> makejoined(
			Map<String, Map<String, Set<Pair<Object, Object>>>> joined,
			Map<String, Set<Pair<Object, Object>>> nd, List<String> names) {
		Comparator<String> strcmp = new Comparator<String>() {
			public int compare(String f1, String f2) {
				return f1.compareTo(f2);
			}
		};
		Map<String, JTable> ret = new HashMap<>();
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

			JTable t = new JTable(arr, cols3) {
				public Dimension getPreferredScrollableViewportSize() {
					Dimension d = getPreferredSize();
					return new Dimension(d.width, d.height );
				}
			};

			// cards.(name, t);

			// foo and t are for the graph and tabular pane, resp
			JTable foo = new JTable(t.getModel()) {
				public Dimension getPreferredScrollableViewportSize() {
					Dimension d = getPreferredSize();
					return new Dimension(d.width, d.height );
				}
			};
			JPanel p = new JPanel(new GridLayout(1, 1));
			// p.add(t);
			TableRowSorter<?> sorter = new MyTableRowSorter(t.getModel());

			sorter.toggleSortOrder(0);
			t.setRowSorter(sorter);
			sorter.allRowsChanged();
			TableRowSorter<?> sorter2 = new MyTableRowSorter(foo.getModel());

			sorter.toggleSortOrder(0);
			foo.setRowSorter(sorter2);
			sorter2.allRowsChanged();
			// foo.set
			// foo.setAutoCreateRowSorter(true);
			p.add(new JScrollPane(foo));
			// p.setMaximumSize(new Dimension(200,200));
			p.setBorder(BorderFactory.createTitledBorder(
					BorderFactory.createEmptyBorder(), name));
			vwr.add(p, name);

			// foo.setMaximumSize(new Dimension(600,200));

			ret.put(name, t);
		}

		return ret;
	}

	public JPanel text() {
		JTextArea ta = new JTextArea(toString());
		JPanel tap = new JPanel(new GridLayout(1, 1));
		ta.setBorder(BorderFactory.createEmptyBorder());
		//
		tap.setBorder(BorderFactory.createEmptyBorder());
		ta.setWrapStyleWord(true);
		ta.setLineWrap(true);
		JScrollPane xxx = new JScrollPane(ta);
		// xxx.setBorder(BorderFactory.createEmptyBorder());
		//
		tap.add(xxx);
		// tap.setSize(600, 600);

		return tap;
	}

	public static boolean iso(Instance i1, Instance i2) {
		sameNodes(i1, i2);
		sameEdges(i1, i2);

		Signature sig = i1.thesig;

		Map<String, List<Map<Object, Object>>> subs1 = new HashMap<>();
		Map<String, List<Map<Object, Object>>> subs2 = new HashMap<>();
		for (Node n : sig.nodes) {
			String k = n.string;

			List<Map<Object, Object>> i1i2 = Inst.bijections(
					dedupl(i1.data.get(k)), dedupl(i2.data.get(k)));
			List<Map<Object, Object>> i2i1 = Inst.bijections(
					dedupl(i2.data.get(k)), dedupl(i1.data.get(k)));

			subs1.put(k, i1i2);
			subs2.put(k, i2i1);
		}

		Subs subs1X = new Subs(subs1);
		Subs subs2X = new Subs(subs2);
		Map<String, Map<Object, Object>> sub;

		boolean flag = false;
		while ((sub = subs1X.next()) != null) {
			try {
				Instance iX = i1.apply(sub);
				if (iX.equals(i2)) {
					flag = true;
					break;
				}
			} catch (Exception e) {
			}
		}
		if (!flag) {
			return false;
		}

		flag = false;
		while ((sub = subs2X.next()) != null) {
			try {
				Instance iX = i2.apply(sub);
				if (iX.equals(i1)) {
					flag = true;
					break;
				}
			} catch (Exception e) {
			}
		}
		if (!flag) {
			return false;
		}

		return true;
	}

	static class Subs {
		private Map<String, List<Map<Object, Object>>> sub;
		private LinkedList<String> keys;
		private int[] counters;
		private int[] sizes;

		public Subs(Map<String, List<Map<Object, Object>>> subs1) {
			this.sub = subs1;
			this.keys = new LinkedList<>(sub.keySet());

			this.counters = makeCounters(keys.size() + 1);
			this.sizes = makeSizes(keys, sub);
		}

		public Map<String, Map<Object, Object>> next() {
			if (counters[keys.size()] == 1) {
				return null;
			}

			Map<String, Map<Object, Object>> s = new HashMap<>();
			for (String k : keys) {
				s.put(k, sub.get(k).get(counters[keys.indexOf(k)]));
			}

			inc5(counters, sizes);

			return s;
		}
	}

	public static void printnice(int[] x) {
		for (int i = 0; i < x.length; i++) {
			System.out.print(x[i]);
			System.out.print(" ");
		}
		System.out.println();
	}

	private static int[] makeSizes(List<String> keys,
			Map<String, List<Map<Object, Object>>> sub) {
		int[] ret = new int[keys.size()];
		int i = 0;
		for (String k : keys) {
			ret[i++] = sub.get(k).size();
		}
		return ret;
	}

	private static void inc5(int[] counters, int[] sizes) {
		counters[0]++;
		for (int i = 0; i < counters.length - 1; i++) {
			if (counters[i] == sizes[i]) {
				counters[i] = 0;
				counters[i + 1]++;
			}
		}
	}

	private static int[] makeCounters(int size) {
		int[] ret = new int[size];
		for (int i = 0; i < size; i++) {
			ret[i++] = 0;
		}
		return ret;
	}

	private Instance apply(Map<String, Map<Object, Object>> sub)
			throws FQLException {
		List<Pair<String, List<Pair<Object, Object>>>> ret = new LinkedList<>();

		for (Node n : thesig.nodes) {
			ret.add(new Pair<>(n.string, apply(data.get(n.string),
					sub.get(n.string), sub.get(n.string))));
		}
		for (Edge e : thesig.edges) {
			ret.add(new Pair<>(e.name, apply(data.get(e.name),
					sub.get(e.source.string), sub.get(e.target.string))));
		}

		return new Instance(thesig, ret);
	}

	private static List<Pair<Object, Object>> apply(
			Set<Pair<Object, Object>> set, Map<Object, Object> s1,
			Map<Object, Object> s2) {
		List<Pair<Object, Object>> ret = new LinkedList<>();

		for (Pair<Object, Object> p : set) {
			ret.add(new Pair<>(s1.get(p.first), s2.get(p.second)));
		}

		return ret;
	}

	private static List<Object> dedupl(Set<Pair<Object, Object>> set) {
		List<Object> ret = new LinkedList<>();
		for (Pair<Object, Object> p : set) {
			ret.add(p.first);
		}
		return ret;
	}

	private static void sameEdges(Instance i1, Instance i2) {
		for (Edge e1 : i1.thesig.edges) {
			if (!i2.thesig.edges.contains(e1)) {
				throw new RuntimeException("Missing " + e1 + " in " + i2 + ")");
			}
		}
		for (Edge e2 : i2.thesig.edges) {
			if (!i1.thesig.edges.contains(e2)) {
				throw new RuntimeException("Missing " + e2 + " in " + i1 + ")");
			}
		}
	}

	private static void sameNodes(Instance i1, Instance i2) {
		for (Node n1 : i1.thesig.nodes) {
			if (!i2.thesig.nodes.contains(n1)) {
				throw new RuntimeException("Missing " + n1 + " in " + i2 + ")");
			}
		}
		for (Node n2 : i2.thesig.nodes) {
			if (!i1.thesig.nodes.contains(n2)) {
				throw new RuntimeException("Missing " + n2 + " in " + i1 + ")");
			}
		}
	}

	public JPanel pretty() throws FQLException {
		return makeViewer();
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

	public JPanel makeViewer(/* Color c */) {
		Graph<String, String> g = build();
		if (g.getVertexCount() == 0) {
			return new JPanel();
		}
		return doView(g);
	}

	public JPanel doView(/* final Environment env ,*/ /* final Color color */ Graph<String, String> sgv) {
		// Layout<V, E>, BasicVisualizationServer<V,E>
		// Layout<String, String> layout = new KKLayout(sgv);

		// Layout<String, String> layout = new FRLayout(sgv);
		Layout<String, String> layout = new ISOMLayout<String, String>(sgv);
		// Layout<String, String> layout = new CircleLayout<>(sgv);
		layout.setSize(new Dimension(600, 400));
		VisualizationViewer<String, String> vv = new VisualizationViewer<String, String>(
				layout);
		//vv.setPreferredSize(new Dimension(600, 400));
		// vv.getRenderContext().setEdgeLabelRerderer(new MyEdgeT());
		// Setup up a new vertex to paint transformer...
		Transformer<String, Paint> vertexPaint = new Transformer<String, Paint>() {
			public Paint transform(String i) {
				if (thesig.isAttribute(i)) {
					return UIManager.getColor("Panel.background");
				} else {
					return thesig.colors.get(i);
				}
//				return color;
			}
		}; 
		DefaultModalGraphMouse<String, String> gm = new DefaultModalGraphMouse<>();
		// gm.setMode(ModalGraphMouse.Mode.TRANSFORMING);
		vv.setGraphMouse(gm);
		gm.setMode(Mode.PICKING);
		// gm.add(new AnnotatingGraphMousePlugin(vv.getRenderContext()) {
		//
		//
		//
		// }.);

		// Set up a new stroke Transformer for the edges
		// float dash[] = { 10.0f };
		// final Stroke edgeStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
		// BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f);
		// Transformer<String, Stroke> edgeStrokeTransformer = new
		// Transformer<String, Stroke>() {
		// public Stroke transform(String s) {
		// return edgeStroke;
		// }
		// };
		vv.getRenderContext().setVertexFillPaintTransformer(vertexPaint);
		// vv.getRenderContext().setEdgeStrokeTransformer(edgeStrokeTransformer);
		vv.getRenderContext().setVertexLabelRenderer(new MyVertexT());
		// vv.getRenderContext().setVertexLabelTransformer(new
		// ToStringLabeller());

		// new MyEdgeT()); // {

		// vv.getRenderContext().setEdgeLabelTransformer(new
		// MyEdgeT2(vv.getPickedEdgeState()));
		// vv.getRenderContext().setVertexLabelTransformer(new
		// MyVertexT(vv.getPickedVertexState()));
		// vv.getRenderer().getVertexRenderer().
		vv.getRenderContext().setLabelOffset(20);
		vv.getRenderContext().setEdgeLabelTransformer(
				new ToStringLabeller<String>());
		// vv.getRenderContext().getEdgeLabelRenderer().
		// vv.getRenderer().getVertexLabelRenderer().setPosition(Position.CNTR);

		float dash[] = { 1.0f };
		final Stroke edgeStroke = new BasicStroke(0.5f, BasicStroke.CAP_BUTT,
				BasicStroke.JOIN_MITER, 10.0f, dash, 10.0f);
		// Transformer<String, Stroke> edgeStrokeTransformer = new
		// Transformer<String, Stroke>() {
		// public Stroke transform(String s) {
		// return edgeStroke;
		// }
		// };
		final Stroke bs = new BasicStroke();
		Transformer<String, Stroke> edgeStrokeTransformer = new Transformer<String, Stroke>() {
			public Stroke transform(String s) {
				if (thesig.isAttribute(s)) {
					return edgeStroke;
				}
				return bs;
			}
		};

	//	vv.getRenderContext().setVertexFillPaintTransformer(vertexPaint);
		vv.getRenderContext().setEdgeStrokeTransformer(edgeStrokeTransformer);
		vv.getRenderContext().setVertexLabelTransformer(
				new ToStringLabeller<String>());
		vv.getRenderContext().setEdgeLabelTransformer(
				new ToStringLabeller<String>());
		
		GraphZoomScrollPane zzz = new GraphZoomScrollPane(vv);
		//JPanel ret = new JPanel(new GridLayout(1,1));
		//ret.add(zzz);
		//ret.setBorder(BorderFactory.createEtchedBorder());


		JSplitPane newthing = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		newthing.setResizeWeight(.8d); //setDividerLocation(.9d);
		newthing.add(zzz);
		newthing.add(vwr);
		JPanel xxx = new JPanel(new GridLayout(1, 1));
		xxx.add(newthing);
		// xxx.setMaximumSize(new Dimension(400,400));
		return xxx;
	}

	private class MyVertexT implements VertexLabelRenderer {

		public MyVertexT() {
		}

		@Override
		public <T> Component getVertexLabelRendererComponent(JComponent arg0,
				Object arg1, Font arg2, boolean arg3, T arg4) {
			if (arg3) {
				prejoin();
				// Map<String, JPanel> panels = makejoined();
				// if (pi.isPicked((String) arg4)) {

				cards.show(vwr, (String) arg4);

				String s = (String) arg4;
				if (thesig.isAttribute(s)) {
					s = thesig.getTypeLabel(s);
				}
				return new JLabel(s);

				// JTable t = joined.get(arg4);
				//
				// JPanel p = new JPanel(new GridLayout(1,1));
				// //p.add(t);
				// p.add(new JScrollPane(t));
				// // p.setMaximumSize(new Dimension(200,200));
				// p.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(),
				// (String)arg4));
				//
				//
				// // JPanel p = new JPanel(new GridLayout(1,1));
				// // p.add(new JScrollPane(joined.get(arg4)));
				// // p.setMaximumSize(new Dimension(100,100));
				// // p.setPreferredSize(new Dimension(100,100));
				// // p.setSize(new Dimension(100,100));
				// return p;
			} else {
				String s = (String) arg4;
				if (thesig.isAttribute(s)) {
					s = thesig.getTypeLabel(s);
				}
				return new JLabel(s);
			}
		}
	}

	// private class MyEdgeT extends DefaultEdgeLabelRenderer {
	// // private final PickedInfo<String> pi;
	//
	// public MyEdgeT(){
	// super(Color.GRAY, false);
	// // this.pi = pi;
	// }
	//
	// @Override
	// public <T> Component getEdgeLabelRendererComponent(
	// JComponent arg0, Object arg1, Font arg2, boolean arg3,
	// T arg4) {
	// // if (true) throw new RuntimeException();
	// if (arg3) {
	// // if (pi.isPicked((String) arg4)) {
	// Vector<String> ld = new Vector<>();
	//
	// Set<Pair<String, String>> table = data.get(arg4);
	//
	//
	// String s = (String) arg4;
	// boolean b = false;
	// s += " = ";
	// for (Pair<String, String> x : table) {
	// if (b) {
	// s += ", ";
	// }
	// b = true;
	// s += x.first;
	// ld.add(x.first);
	// }
	// JList<String> jl = new JList<>(ld);
	// JPanel p = new JPanel(new GridLayout(1,1));
	// p.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(),
	// (String) arg4));
	// p.add(new JScrollPane(jl));
	// // p.add(jl);
	//
	// // JLabel x = new JLabel(s);
	// // x.setFont(new Font("Arial", 8, Font.PLAIN));
	// // return x;
	// // return new JTextArea(s);
	// // return p;
	// return new JLabel("ZZZZ");
	// }
	// else {
	// return new JLabel("HHHH");
	// // return new JLabel("ZZZZZ" + (String)arg4);
	// }
	// }
	//
	// boolean b = false;
	// @Override
	// public boolean isRotateEdgeLabels() {
	// return b;
	// }
	//
	// @Override
	// public void setRotateEdgeLabels(boolean arg0) {
	// this.b = arg0;
	// }
	// }
	//
	// private class MyEdgeT2 implements Transformer<String,String>{
	// private final PickedInfo<String> pi;
	//
	// public MyEdgeT2( PickedInfo<String> pi ){
	// this.pi = pi;
	// }
	//
	// @Override
	// public String transform(String t) {
	// if (pi.isPicked(t)) {
	// Set<Pair<String, String>> table = data.get(t);
	//
	// String s = t;
	// boolean b = false;
	// s += " = ";
	// for (Pair<String, String> x : table) {
	// if (b) {
	// s += ", ";
	// }
	// b = true;
	// s += x.first;
	// s += " -> ";
	// s += x.second;
	// }
	// // JLabel x = new JLabel(s);
	// // x.setFont(new Font("Arial", 8, Font.PLAIN));
	// // return x;
	// return s;
	//
	// }
	// else {
	// return t;
	// }
	// }
	// }

	// private static List<Pair<String,String>> dupl(Map<String, String> map) {
	// List<Pair<String,String>> ret = new LinkedList<Pair<String,String>>();
	// for (String k : map.keySet()) {
	// ret.add(new Pair<>(k,map.get(k)));
	// }
	// return ret;
	// }
	//
	// private static List<Pair<String,String>> dupl(Set<String> set) {
	// List<Pair<String,String>> ret = new LinkedList<Pair<String,String>>();
	// for (String s : set) {
	// ret.add(new Pair<>(s,s));
	// }
	// return ret;
	// }

	public static Instance terminal(Signature s, String g) throws FQLException {
		List<Pair<String, List<Pair<Object, Object>>>> ret = new LinkedList<>();

		int i = 0;
		Map<Node, String> map = new HashMap<>();
		for (Node node : s.nodes) {
			List<Pair<Object, Object>> tuples = new LinkedList<>();

			if (g == null) {
				g = Integer.toString(i);
			}

			tuples.add(new Pair<Object, Object>(g, g));
			ret.add(new Pair<>(node.string, tuples));
			map.put(node, g);
			i++;
		}

		for (Edge e : s.edges) {
			List<Pair<Object, Object>> tuples = new LinkedList<>();
			tuples.add(new Pair<Object, Object>(map.get(e.source.string), map
					.get(e.target.string)));
			ret.add(new Pair<>(e.name, tuples));
		}

//		return null;
		return new Instance(s, ret);
	}

	public Inst<Node, Path, Object, Object> toFunctor2() throws FQLException {
		FinCat<Node, Path> cat = thesig.toCategory2().first;

		Map<Node, Set<Value<Object, Object>>> objM = new HashMap<>();
		for (Node obj : cat.objects) {
			if (data.get(obj.string) == null) {
				throw new RuntimeException("No data for " + obj + " in " + data);
			}
			objM.put(obj, conv(data.get(obj.string)));
		}

		Map<Arr<Node, Path>, Map<Value<Object, Object>, Value<Object, Object>>> arrM = new HashMap<>();
		for (Arr<Node, Path> arr : cat.arrows) {
			List<String> es = arr.arr.asList();

			String h = es.get(0);
			Set<Pair<Object, Object>> h0 = data.get(h);
			for (int i = 1; i < es.size(); i++) {
				h0 = compose(h0, data.get(es.get(i)));
			}
			Map<Value<Object, Object>, Value<Object, Object>> xxx = FDM
					.degraph(h0);
			arrM.put(arr, xxx);
		}

		return new Inst<Node, Path, Object, Object>(objM, arrM, cat);
	}

	// public Inst<String, List<List<String>>, String, String> toFunctor()
	// throws FQLException {
	// FinCat<String, List<List<String>>> cat = thesig.toCategory().first;
	//
	// Map<String, Set<Value<String, String>>> objM = new HashMap<>();
	// for (String obj : cat.objects) {
	// objM.put(obj, conv(data.get(obj)));
	// }
	//
	// Map<Arr<String, List<List<String>>>, Map<Value<String, String>,
	// Value<String, String>>> arrM = new HashMap<>();
	// for (Arr<String, List<List<String>>> arr : cat.arrows) {
	// List<String> es = arr.arr.get(0);
	//
	// String h = es.get(0);
	// Set<Pair<String, String>> h0 = data.get(h);
	// for (int i = 1; i < es.size(); i++) {
	// h0 = compose(h0, data.get(es.get(i)));
	// }
	// Map<Value<String, String>, Value<String, String>> xxx = FDM.degraph(h0);
	// arrM.put(arr, xxx);
	// }
	//
	// return new Inst<String, List<List<String>>, String, String>(objM, arrM,
	// cat);
	// }

	private Set<Value<Object, Object>> conv(Set<Pair<Object, Object>> set) {
		Set<Value<Object, Object>> ret = new HashSet<>();
		for (Pair<Object, Object> p : set) {
			ret.add(new Value<Object, Object>(p.first));
		}
		return ret;
	}

	JPanel vwr = new JPanel();
	CardLayout cards = new CardLayout();
	Map<String, JTable> joined;

	
	public JPanel groth() throws FQLException {
		return CategoryOfElements.makePanel(this);
	}

	public JPanel observables() {
		
		
		JTabbedPane t = new JTabbedPane();

		String name = "obsinput"; //dont use 'input' here - it conflicts
		Map<String, Set<Map<Object, Object>>> state = shred( name );
		//System.out.println(state);
		try {
			if (thesig.attrs.size() == 0) {
				throw new FQLException("Cannot generate observables - no attributes");
			}

			List<PSM> prog = (PSMGen.makeTables("output", thesig, false));

			Pair<Map<Node, List<String>>, List<PSM>> xxx = Relationalizer.compile(thesig, "output", name);
			prog.addAll(xxx.second);
			Map<String, Set<Map<Object, Object>>> res = new PSMInterp().interpX(prog, state);
						
			for (Node n : thesig.nodes) {
				t.addTab(n.string, makePanel(xxx.first.get(n), res, n));
			}
			JPanel ret = new JPanel(new GridLayout(1,1));
			ret.add(t);
			ret.setBorder(BorderFactory.createEtchedBorder());
			return ret;

		} catch (Exception e) {
			JPanel ret = new JPanel(new GridLayout(1,1));
			JTextArea a = new JTextArea(e.getMessage());
			ret.add(new JScrollPane(a));
			return ret;
		}
	}

	private JPanel makePanel(List<String> attrs,
			Map<String, Set<Map<Object, Object>>> res, Node n) {
		try {
//		System.out.println("********");
//		System.out.println(res);
//		
		JPanel ret = new JPanel(new GridLayout(1,1));
		Object[] colNames = new Object[attrs.size() + 1];
		int x = 1;
		colNames[0] = "ID";
		for (String s : attrs) {
			colNames[x++] = s;
		}
		Object[][] rows = new Object[data.get(n.string).size()][attrs.size() + 1];
		
		int j = 0;
		for (Map<Object, Object> row : res.get("output_" + n.string + "_observables")) {
			for (int i = 0; i < attrs.size(); i++) {
				rows[j][i+1] = row.get("c" + i);
			}
			rows[j][0] = row.get("id");
			j++;
		}
		
		JTable table = new JTable(rows, colNames);		
		TableRowSorter<?> sorter = new MyTableRowSorter(table.getModel());

		table.setRowSorter(sorter);
		sorter.allRowsChanged();
		sorter.toggleSortOrder(0);

		ret.add(new JScrollPane(table));
		
		String str = data.get(n.string).size() + " IDs, " + res.get("output_" + n.string + "_observables_proj").size() + " unique attribute combinations";
		ret.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), str));
		
		return ret;
		
		} catch (Throwable e) {
			e.printStackTrace();
			return new JPanel();
		}
	
	}

	private Map<String, Set<Map<Object, Object>>> shred(String pre) {
		Map<String, Set<Map<Object, Object>>> ret = new HashMap<>();
		for (String k : data.keySet()) {
			ret.put(pre + "_" + k, shred0(data.get(k)));
		}
		return ret;
	}

	private Set<Map<Object, Object>> shred0(Set<Pair<Object, Object>> set) {
		Set<Map<Object, Object>> ret = new HashSet<>();
		for (Pair<Object, Object> p : set) {
			Map<Object, Object> m = new HashMap<>();
			m.put("c0", p.first);
			m.put("c1", p.second);
			ret.add(m);
		}
		return ret;
	}


	/**
	 * Quickly compares two instances by checking the counts
	 * of tuples in all the rows.
	 */
	public static boolean quickCompare(Instance i, Instance j) throws FQLException {
		if (!i.data.keySet().equals(j.data.keySet())) {
			throw new RuntimeException(i.data.keySet() + "\n\n" + j.data.keySet());
		}
		for (String k : i.data.keySet()) {
			Set<Pair<Object, Object>> v = i.data.get(k);
			Set<Pair<Object, Object>> v0 = j.data.get(k);
			if (v.size() != v0.size()) {
				return false;
			}
		}
		
		return true;
	}

}
