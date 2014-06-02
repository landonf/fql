package fql.decl;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.UIManager;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import fql.DEBUG;
import fql.FQLException;
import fql.Fn;
import fql.Pair;
import fql.Triple;
import fql.cat.Arr;
import fql.cat.FinCat;
import fql.cat.Inst;
import fql.cat.LeftKanCat;
import fql.gui.FQLTextPanel;
import fql.sql.EmbeddedDependency;

/**
 * 
 * @author ryan
 * 
 *         Signatures.
 */
public class Signature {

	public SigExp.Const toConst() {
		List<String> nds = new LinkedList<>();
		for (Node n : nodes) {
			nds.add(n.string);
		}
		List<Triple<String, String, String>> atts = new LinkedList<>();
		for (Attribute<Node> a : attrs) {
			atts.add(new Triple<>(a.name, a.source.string, a.target.toString()));
		}
		List<Triple<String, String, String>> arrs = new LinkedList<>();
		for (Edge e : edges) {
			arrs.add(new Triple<>(e.name, e.source.string, e.target.string));
		}
		List<Pair<List<String>, List<String>>> es = new LinkedList<>();
		for (Eq e : eqs) {
			es.add(new Pair<>(e.lhs.asList(), e.rhs.asList()));
		}
		return new SigExp.Const(nds, atts, arrs, es);
	}

	public List<Node> nodes;
	public List<Edge> edges;
	public List<Attribute<Node>> attrs;

//	public Map<String, Color> colors;

//	public static Color[] colors_arr = new Color[] { Color.RED, Color.GREEN,
	//		Color.BLUE, Color.MAGENTA, Color.yellow, Color.CYAN, Color.GRAY,
		//	Color.ORANGE, Color.PINK, Color.BLACK };

	public Set<Eq> eqs;

	// public String name0;
/*
	public void doColors() {
		colors = new HashMap<>();
		int i = 0;
		for (Node n : nodes) {
			if (i == colors_arr.length) {
				colors.put(n.string, Color.WHITE);
			} else {
				colors.put(n.string, colors_arr[i++]);
			}
		}
	} */

	public Signature clone() {
		Signature s = new Signature();
		s.nodes = new LinkedList<>(nodes);
		s.edges = new LinkedList<>(edges);
		s.attrs = new LinkedList<>(attrs);
		s.eqs = new HashSet<>(eqs);
//		s.colors = new HashMap<>(colors);
		// s.name0 = newname;
		return s;
	}

	Map<String, Type> types;

	public Signature(Map<String, Type> types, List<String> nodes_str,
			List<Triple<String, String, String>> attrs_str,
			List<Triple<String, String, String>> arrows,
			List<Pair<List<String>, List<String>>> equivs) throws FQLException {
		this.types = types;
		Set<Node> nodesA = new HashSet<>();
		List<Edge> edgesA = new LinkedList<>();
		// List<Edge> edgesX = new LinkedList<>();
		Set<Attribute<Node>> attrsA = new HashSet<>();
		// name0 = n;

		Collections.sort(arrows);

		Set<String> seen = new HashSet<String>();
		for (String s : nodes_str) {
			if (seen.contains(s.toLowerCase())) {
				throw new FQLException("Duplicate name: " + s);
			}
			seen.add(s.toLowerCase());
			nodesA.add(new Node(s));
		}

		for (Triple<String, String, String> arrow : arrows) {
			String name = arrow.first;
			String source = arrow.second;
			String target = arrow.third;

			if (seen.contains(name.toLowerCase())) {
				throw new FQLException("Duplicate name: " + name);
			}
			seen.add(name.toLowerCase());

			Node source_node = lookup(source, nodesA);
			if (source_node == null) {
				throw new FQLException("Missing node " + source /* + " in " + n */);
			}
			Node target_node = lookup(target, nodesA);
			if (target_node == null) {
				throw new FQLException("Missing node " + target /* + " in " + n */);
			}
			// nodesA.add(target_node);
			Edge e = new Edge(name, source_node, target_node);
			edgesA.add(e);
		}

		for (Triple<String, String, String> attr : attrs_str) {
			String name = attr.first;
			String source = attr.second;
			String target = attr.third;

			if (seen.contains(name.toLowerCase())) {
				throw new FQLException("Duplicate name: " + name);
			}
			seen.add(name.toLowerCase());

			Node source_node = lookup(source, nodesA);
			if (source_node == null) {
				throw new FQLException("Missing node " + source /* + " in " + n */);
			}

			Type type = types.get(target);
			if (type == null) {
				throw new RuntimeException("Missing enum: " + target);
			}
			Attribute<Node> a = new Attribute<>(name, source_node, type);

			attrsA.add(a);
		}

		nodes = new LinkedList<>(nodesA);
		edges = new LinkedList<>(edgesA);
		attrs = new LinkedList<>(attrsA);

		eqs = new HashSet<Eq>();
		for (Pair<List<String>, List<String>> equiv : equivs) {
			Path lhs = new Path(this, equiv.first);
			Path rhs = new Path(this, equiv.second);
			if (!lhs.source.equals(rhs.source)) {
				throw new FQLException("source object mismatch " + lhs
						+ " and " + rhs); // + " in " + name0);
			}
			if (!lhs.target.equals(rhs.target)) {
				throw new FQLException("target object mismatch " + lhs
						+ " and " + rhs); // + " in " + name0);
			}
			Eq eq = new Eq(lhs, rhs);
			eqs.add(eq);
		}

		Collections.sort(nodes);
		Collections.sort(attrs);

		// System.out.println(this);
		if (!DEBUG.debug.ALLOW_INFINITES) {
			// try {
			toCategory2();
			// } catch (FQLException fe) {
			/*
			 * try { JPanel p = denotation(); JFrame fr = new
			 * JFrame("Category Denotation Debugger"); fr.setContentPane(p);
			 * fr.pack(); fr.setSize(600, 400); fr.setVisible(true); } catch
			 * (Throwable fex) { fex.printStackTrace(); }
			 */
			// throw fe;
			// }
		}
//		doColors();
	}

	/*
	 * public Signature(String n, List<Triple<String, String, String>> arrows,
	 * List<Pair<List<String>, List<String>>> equivs) throws FQLException {
	 * Set<Node> nodesA = new HashSet<>(); Set<Edge> edgesA = new HashSet<>();
	 * Set<Attribute<Node>> attrsA = new HashSet<>(); name0 = n;
	 * 
	 * Set<String> seen = new HashSet<String>(); for (Triple<String, String,
	 * String> arrow : arrows) { String name = arrow.first; String source =
	 * arrow.second; String target = arrow.third;
	 * 
	 * if (seen.contains(name)) { throw new FQLException("Duplicate name: " +
	 * name); } seen.add(name);
	 * 
	 * // isolated node if (source == null) { Node nd = new Node(name);
	 * nodesA.add(nd); continue; }
	 * 
	 * Node source_node = lookup(source, nodesA); if (source_node == null) {
	 * source_node = new Node(source); } nodesA.add(source_node);
	 * 
	 * Type t; if ((t = tryParseType(target)) != null) { Attribute<Node> a = new
	 * Attribute<>(name, source_node, t); attrsA.add(a); } else { Node
	 * target_node = lookup(target, nodesA); if (target_node == null) {
	 * target_node = new Node(target); } nodesA.add(target_node);
	 * 
	 * Edge e = new Edge(name, source_node, target_node); edgesA.add(e); } }
	 * 
	 * nodes = new LinkedList<>(nodesA); edges = new LinkedList<>(edgesA); attrs
	 * = new LinkedList<>(attrsA);
	 * 
	 * eqs = new HashSet<Eq>(); for (Pair<List<String>, List<String>> equiv :
	 * equivs) { Path lhs = new Path(this, equiv.first); Path rhs = new
	 * Path(this, equiv.second); if (!lhs.source.equals(rhs.source)) { throw new
	 * FQLException("source object mismatch " + lhs + " and " + rhs); } if
	 * (!lhs.target.equals(rhs.target)) { throw new
	 * FQLException("target object mismatch " + lhs + " and " + rhs); } Eq eq =
	 * new Eq(lhs, rhs); eqs.add(eq); } if (!DEBUG.ALLOW_INFINITES) {
	 * toCategory2(); } }
	 */
	/*
	 * private Type tryParseType(String s) { if (s.equals("string")) { return
	 * new Varchar(); } else if (s.equals("int")) { return new Int(); } else {
	 * return new Type. } return null; }
	 */
	/*
	 * // for json public Signature( List<String> obs, List<Pair<Pair<String,
	 * String>, String>> arrows, List<Pair<List<Pair<Pair<String, String>,
	 * String>>, List<Pair<Pair<String, String>, String>>>> equivs) throws
	 * FQLException { Set<Node> nodesA = new HashSet<Node>(); Set<Edge> edgesA =
	 * new HashSet<Edge>(); attrs = new LinkedList<>();
	 * 
	 * // name0 = n;
	 * 
	 * Set<String> seen_obs = new HashSet<String>(); for (String o : obs) { if
	 * (seen_obs.contains(o)) { throw new FQLException("Duplicate object : " +
	 * o); } seen_obs.add(o); nodesA.add(new Node(o)); }
	 * 
	 * Set<String> seen = new HashSet<>(); for (Pair<Pair<String, String>,
	 * String> arrow : arrows) { String name = arrow.second; String source =
	 * arrow.first.first; String target = arrow.first.second;
	 * 
	 * if (seen.contains(name)) { throw new FQLException("Duplicate edge: " +
	 * name); } seen.add(name);
	 * 
	 * Node source_node = lookup(source, nodesA); // if (source_node == null) {
	 * // throw new FQLException("Missing node " + source_node + " in " + //
	 * name0); // } Node target_node = lookup(target, nodesA); // if
	 * (target_node == null) { // throw new FQLException("Missing node " +
	 * target_node + " in " + // name0); // }
	 * 
	 * Edge e = new Edge(name, source_node, target_node); edgesA.add(e); }
	 * 
	 * nodes = new LinkedList<Node>(nodesA); edges = new
	 * LinkedList<Edge>(edgesA);
	 * 
	 * eqs = new HashSet<Eq>(); for (Pair<List<Pair<Pair<String, String>,
	 * String>>, List<Pair<Pair<String, String>, String>>> equiv : equivs) { if
	 * (equiv.first.size() == 0 && equiv.second.size() == 0) { throw new
	 * FQLException("empty eq " + equiv); } Path lhs, rhs; List<String> temp =
	 * new LinkedList<>(); if (equiv.first.size() == 0) { rhs = new Path(new
	 * Unit(), this, equiv.second); temp.add(rhs.source.string); lhs = new
	 * Path(this, temp); } else if (equiv.second.size() == 0) { lhs = new
	 * Path(new Unit(), this, equiv.first); temp.add(lhs.source.string); rhs =
	 * new Path(this, temp); } else { lhs = new Path(new Unit(), this,
	 * equiv.first); rhs = new Path(new Unit(), this, equiv.second); }
	 * 
	 * if (!lhs.source.equals(rhs.source)) { throw new
	 * FQLException("source object mismatch " + lhs + " and " + rhs); } if
	 * (!lhs.target.equals(rhs.target)) { throw new
	 * FQLException("target object mismatch " + lhs + " and " + rhs); } Eq eq =
	 * new Eq(lhs, rhs); eqs.add(eq); }
	 * 
	 * }
	 */
	public Signature(List<Node> n, List<Edge> e, List<Attribute<Node>> a,
			Set<Eq> ee) {
		// name0 = s;
		nodes = n;
		edges = e;
		eqs = ee;
		attrs = a;
		// System.out.println("yyyyyy " + edges);
	}

	private Signature() {

	}

	public boolean acyclic() {
		for (Node n : nodes) {
			Set<Node> r = reachable(n);
			if (r.contains(n)) {
				return false;
			}
		}
		return true;
	}

	private Set<Node> reachable1(Node n) {
		Set<Node> ret = new HashSet<Node>();
		for (Edge e : edges) {
			if (e.source.equals(n)) {
				ret.add(e.target);
			}
		}
		return ret;
	}

	private Set<Node> reachableS1(Set<Node> nodes) {
		Set<Node> ret = new HashSet<Node>();
		for (Node node : nodes) {
			ret.addAll(reachable1(node));
		}
		return ret;
	}

	private Set<Node> reachableFix(Set<Node> nodes) {
		Set<Node> x = new HashSet<Node>(nodes);
		for (;;) {
			int i = x.size();
			x.addAll(reachableS1(nodes));
			if (i == x.size()) {
				return x;
			}
		}
	}

	private Set<Node> reachable(Node node) {
		Set<Node> init = reachable1(node);
		Set<Node> cl = reachableFix(init);
		return cl;
	}

	private Node lookup(String string, Collection<Node> nodes) {
		// System.out.println("Looking up " + string + " in " + nodes);
		for (Node node : nodes) {
			if (node.string.equals(string)) {
				return node;
			}
		}
		return null;
	}

	public Edge getEdge(String string) throws FQLException {
		for (Edge edge : edges) {
			if (edge.name.equals(string)) {
				return edge;
			}
		}
		throw new FQLException("Unknown arrow: " + string);
	}

	public Node getNode(String string) throws FQLException {
		for (Node node : nodes) {
			if (node.string.equals(string)) {
				return node;
			}
		}
		throw new FQLException("Unknown node: " + string);
	}

	public JPanel view() {
		Object[][] arr = new Object[eqs.size()][2];
		int i = 0;
		for (Eq eq : eqs) {
			arr[i][0] = eq.lhs;
			arr[i][1] = eq.rhs;
			i++;
		}
		Arrays.sort(arr, new Comparator<Object[]>() {
			public int compare(Object[] f1, Object[] f2) {
				return f1[0].toString().compareTo(f2[0].toString());
			}
		});

		JTable eqsComponent = new JTable(arr, new Object[] { "lhs", "rhs" });
		// MouseListener[] listeners = eqsComponent.getMouseListeners();
		// for (MouseListener l : listeners) {
		// eqsComponent.removeMouseListener(l);
		// }
		// eqsComponent.setRowSelectionAllowed(false);
		// eqsComponent.setColumnSelectionAllowed(false);

		JPanel p = new JPanel(new GridLayout(2, 2));
		p.setBorder(BorderFactory.createEtchedBorder());
		// p.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

		JPanel eqsTemp = new JPanel(new GridLayout(1, 1));

		eqsTemp.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEmptyBorder(2, 2, 2, 2), "Equations"));
		eqsTemp.add(new JScrollPane(eqsComponent));

		Object[][] sn = new String[nodes.size()][1];
		int ii = 0;
		for (Node n : nodes) {
			sn[ii++][0] = n.string;
		}
		Arrays.sort(sn, new Comparator<Object[]>() {
			public int compare(Object[] f1, Object[] f2) {
				return f1[0].toString().compareTo(f2[0].toString());
			}
		});

		JTable nodesComponent = new JTable(sn, new String[] { "Name" });
		JPanel nodesTemp = new JPanel(new GridLayout(1, 1));
		nodesTemp.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEmptyBorder(2, 2, 2, 2), "Nodes"));
		nodesTemp.add(new JScrollPane(nodesComponent));
		// nodesComponent.setRowSelectionAllowed(false);
		// nodesComponent.setColumnSelectionAllowed(false);
		// listeners = nodesComponent.getMouseListeners();
		// for (MouseListener l : listeners) {
		// nodesComponent.removeMouseListener(l);
		// }

		Object[][] es = new String[edges.size()][3];
		int jj = 0;
		for (Edge eq : edges) {
			es[jj][0] = eq.name;
			es[jj][1] = eq.source.string;
			es[jj][2] = eq.target.string;
			jj++;
		}
		Arrays.sort(es, new Comparator<Object[]>() {
			public int compare(Object[] f1, Object[] f2) {
				return f1[0].toString().compareTo(f2[0].toString());
			}
		});

		JTable esC = new JTable(es, new String[] { "Name", "Source", "Target" });
		JPanel edgesTemp = new JPanel(new GridLayout(1, 1));
		edgesTemp.add(new JScrollPane(esC));
		edgesTemp.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEmptyBorder(2, 2, 2, 2), "Arrows"));

		// esC.setRowSelectionAllowed(false);
		// esC.setColumnSelectionAllowed(false);
		// listeners = esC.getMouseListeners();
		// for (MouseListener l : listeners) {
		// esC.removeMouseListener(l);
		// }

		Object[][] as = new String[attrs.size()][3];
		jj = 0;
		for (Attribute<Node> a : attrs) {
			as[jj][0] = a.name;
			as[jj][1] = a.source.string;
			as[jj][2] = a.target.toString();
			jj++;
		}
		Arrays.sort(as, new Comparator<Object[]>() {
			public int compare(Object[] f1, Object[] f2) {
				return f1[0].toString().compareTo(f2[0].toString());
			}
		});

		JTable asC = new JTable(as, new String[] { "Name", "Source", "Type" });
		JPanel attrsTemp = new JPanel(new GridLayout(1, 1));
		attrsTemp.add(new JScrollPane(asC));
		attrsTemp.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEmptyBorder(2, 2, 2, 2), "Attributes"));

		// esC.setRowSelectionAllowed(false);
		// esC.setColumnSelectionAllowed(false);
		// listeners = esC.getMouseListeners();
		// for (MouseListener l : listeners) {
		// esC.removeMouseListener(l);
		// }
		p.add(nodesTemp);
		p.add(edgesTemp);
		p.add(attrsTemp);
		p.add(eqsTemp);

		return p;
	}

	public static String toString(
			Pair<Signature, List<Pair<Attribute<Node>, Pair<Edge, Attribute<Node>>>>> x) {

		List<Pair<Attribute<Node>, Pair<Edge, Attribute<Node>>>> k = x.second;
		StringBuffer sb = new StringBuffer();

		boolean first = true;
		for (Pair<Attribute<Node>, Pair<Edge, Attribute<Node>>> eq : k) {
			if (!first) {
				sb.append(",\n");
			}
			first = false;
			String z = eq.first.source + "." + eq.first.name + " = "
					+ eq.second.first.source.string + "."
					+ eq.second.first.name + "." + eq.second.second.name;
			sb.append(z);
		}
		sb.append("\n");

		if (k.size() > 0) {
			return x.first.toString() + "\n\nattribute equations\n" + sb;
		} else {
			return x.first.toString();
		}
	}

	@Override
	public String toString() {
		String x = "\n nodes\n";
		boolean b = false;
		for (Node n : nodes) {
			if (b) {
				x += ",\n";
			}
			x += "  " + n;
			b = true;
		}
		x = x.trim();
		x += ";\n";
		x += " attributes\n";
		b = false;
		for (Attribute<Node> a : attrs) {
			if (b) {
				x += ",\n";
			}
			x += "  " + a.name + ": " + a.source.string + " -> "
					+ a.target.toString();
			b = true;
		}

		x = x.trim();
		x += ";\n";
		x += " arrows\n";

		b = false;
		for (Edge a : edges) {
			if (b) {
				x += ",\n";
			}
			x += "  " + a.name + ": " + a.source.string + " -> "
					+ a.target.string;
			b = true;
		}

		x = x.trim();
		x += ";\n";
		x += " equations\n";

		b = false;
		for (Eq a : eqs) {
			if (b) {
				x += ",\n";
			}
			x += "  " + a;
			b = true;
		}
		x = x.trim();
		return "{\n " + x + ";\n}";
	}

	// private boolean disconnected(Node n) {
	// for (Edge e : edges) {
	// if (e.source.equals(n) || e.target.equals(n)) {
	// return false;
	// }
	// }
	// for (Attribute a : attrs) {
	// if (a.source.equals(n)) {
	// return false;
	// }
	// }
	// return true;
	// }

	public JPanel text() {
		// String s = toString().replace(";", "\n\n;\n\n");
		// String[] t = s.split(",");
		// String ret = "";
		// for (String a : t) {
		// ret += (a.trim() + ",\n\n");
		// }
		// ret = ret.trim();
		// if (ret.endsWith(",")) {
		// ret = ret.substring(0, ret.length() - 1);
		// }

		JTextArea ta = new JTextArea(toString());
		JPanel tap = new JPanel(new GridLayout(1, 1));
		// ta.setBorder(BorderFactory.createEmptyBorder());
		//
		// tap.setBorder(BorderFactory.createEtchedBorder());
		ta.setWrapStyleWord(true);
		ta.setLineWrap(true);
		JScrollPane xxx = new JScrollPane(ta);
		//
		tap.add(xxx);

		return tap;

	}

	public Pair<String, String> getColumnNames(String s) throws FQLException {
		// if (s.contains(" ")) {
		// return new Pair<>("ID", "string");
		// }
		if (nodes.contains(new Node(s))) {
			return new Pair<String, String>("ID", "ID");
		}
		Attribute<Node> a = getAttr(s);
		if (a != null) {
			return new Pair<String, String>(a.source.string,
					a.target.toString());
		}
		Edge e = getEdge(s);
		return new Pair<String, String>(e.source.string, e.target.string);
	}

	public Attribute<Node> getAttr(String s) {
		for (Attribute<Node> a : attrs) {
			if (a.name.equals(s)) {
				return a;
			}
		}
		return null;
	}

	public Set<String> all() {
		Set<String> ret = new HashSet<String>();
		for (Node n : nodes) {
			ret.add(n.string);
		}
		for (Edge e : edges) {
			ret.add(e.name);
		}
		for (Attribute<Node> a : attrs) {
			ret.add(a.name);
		}
		return ret;
	}

	public boolean contains(String s) {
		return all().contains(s);
	}

	/**
	 * Converts a signature to a category.
	 * 
	 * @return the category, and some isomorphisms
	 * @throws FQLException
	 */

	public List<Path> pathsLessThan(int i) throws FQLException {
		List<List<String>> paths = new LinkedList<>();

		for (Node n : nodes) {
			LinkedList<String> l = new LinkedList<String>();
			l.add(n.string);
			paths.add(l);
			// List<List<String>> ret0 = bfs(l, n);
			List<List<String>> ret0 = bfs(l, n, 0, i);
			// System.out.println("ret0 " + ret0);
			// System.out.println("ret1 " + ret1);
			paths.addAll(ret0);
		}

		List<Path> ret = new LinkedList<>();
		for (List<String> x : paths) {
			ret.add(new Path(this, x));
		}
		return ret;
	}

	// TODO dangerous
	Pair<FinCat<Node, Path>, Fn<Path, Arr<Node, Path>>> cached = null;

	public Pair<FinCat<Node, Path>, Fn<Path, Arr<Node, Path>>> toCategory2()
			throws FQLException {
		if (cached != null) {
			return cached;
		}

		/*
		 * Denotation d = new Denotation(this); cached = d.toCategory(this);
		 */
		cached = LeftKanCat.toCategory(this);
		cached.first.attrs = attrs;

		return cached;
	}

	public static List<String> pathToList(Path p) {
		List<String> ret = new LinkedList<String>();
		ret.add(p.source.string);
		for (Edge e : p.path) {
			ret.add(e.name);
		}
		return ret;
	}

	private List<List<String>> bfs(LinkedList<String> l, Node n, int i, int stop) {
		Set<Edge> outs = outEdges(n);
		List<List<String>> ret = new LinkedList<List<String>>();

		if (i == stop) {
			return ret;
		}
		for (Edge e : outs) {
			LinkedList<String> r = new LinkedList<String>(l);
			r.add(e.name);
			ret.add(r);
			ret.addAll(bfs(new LinkedList<String>(r), e.target, i + 1, stop));
		}
		return ret;
	}

	public Set<Edge> outEdges(Node n) {
		Set<Edge> ret = new HashSet<Edge>();
		for (Edge e : edges) {
			if (e.source.equals(n)) {
				ret.add(e);
			}
		}
		return ret;
	}

	public Graph<String, String> build() {
		// Graph<V, E> where V is the type of the vertices

		Graph<String, String> g2 = new DirectedSparseMultigraph<String, String>();
		for (Node n : nodes) {
			g2.addVertex(n.string);
		}

		for (Edge e : edges) {
			g2.addEdge(e.name, e.source.string, e.target.string);
		}

		for (Attribute<Node> a : attrs) {
			g2.addVertex(a.name);
			g2.addEdge(a.name, a.source.string, a.name);
		}

		return g2;
	}

	public JComponent makeViewer(Color clr) {
		Graph<String, String> g = build();
		if (g.getVertexCount() == 0) {
			return new JPanel();
		}
		return doView(clr, g);
	}

	@SuppressWarnings("unchecked")
	public JComponent doView(final Color clr,
	/* final Environment env, */Graph<String, String> sgv) {
		// Layout<V, E>, BasicVisualizationServer<V,E>
		// Layout<String, String> layout = new FRLayout(sgv);

		try {
			Class<?> c = Class.forName(DEBUG.layout_prefix
					+ DEBUG.debug.schema_graph);
			Constructor<?> x = c.getConstructor(Graph.class);
			Layout<String, String> layout = (Layout<String, String>) x
					.newInstance(sgv);
			// Layout<String, String> layout = new ISOMLayout<String,
			// String>(sgv);

			// Layout<String, String> layout = new CircleLayout(sgv);
			layout.setSize(new Dimension(600, 400));
			// BasicVisualizationServer<String, String> vv = new
			// BasicVisualizationServer<String, String>(
			// layout);
			VisualizationViewer<String, String> vv = new VisualizationViewer<String, String>(
					layout);
			// vv.setPreferredSize(new Dimension(600, 400));
			// Setup up a new vertex to paint transformer...
			Transformer<String, Paint> vertexPaint = new Transformer<String, Paint>() {
				public Paint transform(String i) {
					if (isAttribute(i)) {
						return UIManager.getColor("Panel.background");
						// return env.colors.get(name0);
					} else {
						return clr;
					}
				}
			};
			DefaultModalGraphMouse<String, String> gm = new DefaultModalGraphMouse<>();
			gm.setMode(ModalGraphMouse.Mode.TRANSFORMING);
			vv.setGraphMouse(gm);
			gm.setMode(Mode.PICKING);
			// Set up a new stroke Transformer for the edges
			float dash[] = { 1.0f };
			final Stroke edgeStroke = new BasicStroke(0.5f,
					BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash,
					10.0f);
			// Transformer<String, Stroke> edgeStrokeTransformer = new
			// Transformer<String, Stroke>() {
			// public Stroke transform(String s) {
			// return edgeStroke;
			// }
			// };
			final Stroke bs = new BasicStroke();
			Transformer<String, Stroke> edgeStrokeTransformer = new Transformer<String, Stroke>() {
				public Stroke transform(String s) {
					if (isAttribute(s)) {
						return edgeStroke;
					}
					return bs;
				}
			};
			vv.getRenderContext().setVertexFillPaintTransformer(vertexPaint);
			vv.getRenderContext().setEdgeStrokeTransformer(
					edgeStrokeTransformer);
			vv.getRenderContext().setVertexLabelTransformer(
					new ToStringLabeller<String>());
			vv.getRenderContext().setEdgeLabelTransformer(
					new ToStringLabeller<String>());
			// new ToStringLabeller<String>());
			// vv.getRenderer().getVertexRenderer().
			// vv.getRenderContext().setLabelOffset(20);
			// vv.getRenderer().getVertexLabelRenderer().setPosition(Position.CNTR);

			vv.getRenderContext().setVertexLabelTransformer(
					new ToStringLabeller<String>() {

						@Override
						public String transform(String t) {
							if (isAttribute(t)) {
								return getTypeLabel(t);
							}
							return t;
						}

					});

			GraphZoomScrollPane zzz = new GraphZoomScrollPane(vv);
			JPanel ret = new JPanel(new GridLayout(1, 1));
			ret.add(zzz);
			ret.setBorder(BorderFactory.createEtchedBorder());
			return ret;
		} catch (Throwable cnf) {
			cnf.printStackTrace();
			throw new RuntimeException();
		}

	}

	public String getTypeLabel(String t) {
		for (Attribute<Node> a : attrs) {
			if (a.name.equals(t)) {
				return a.target.toString();
			}
		}
		throw new RuntimeException();
	}

	public boolean isAttribute(String t) {
		for (Attribute<Node> a : attrs) {
			if (a.name.equals(t)) {
				return true;
			}
		}
		return false;
	}

	public JComponent pretty(Color clr) throws FQLException {
		return makeViewer(clr);
	}

	// public String type() {
	// return "schema";
	// }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((attrs == null) ? 0 : attrs.hashCode());
		result = prime * result + ((edges == null) ? 0 : edges.hashCode());
		result = prime * result + ((eqs == null) ? 0 : eqs.hashCode());
		result = prime * result + ((nodes == null) ? 0 : nodes.hashCode());
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
		Signature other = (Signature) obj;
		if (attrs == null) {
			if (other.attrs != null)
				return false;
		} else if (!attrs.equals(other.attrs))
			return false;
		if (edges == null) {
			if (other.edges != null)
				return false;
		} else if (!edges.equals(other.edges))
			return false;
		if (eqs == null) {
			if (other.eqs != null)
				return false;
		} else if (!eqs.equals(other.eqs))
			return false;
		if (nodes == null) {
			if (other.nodes != null)
				return false;
		} else if (!nodes.equals(other.nodes))
			return false;
		return true;
	}

	/*
	 * public String tojson() { String ns = PrettyPrinter.sep(",", "[", "]",
	 * nodes); String es = PrettyPrinter.sep(",\n", "[", "]", edges); String rs
	 * = PrettyPrinter.sep(",", "[", "]", new LinkedList<>(eqs)); String ret =
	 * "{\n\"objects\": " + ns + " , \n\"arrows\": " + es +
	 * " ,\n\"relations\": " + rs + "\n}";
	 * 
	 * // try { // Partial<Signature> out = new //
	 * JSONParsers.JSONSigParser(true).parse(new Tokens(ret)); //
	 * System.out.println(out.value); // } catch(Exception e) { //
	 * e.printStackTrace(); // }
	 * 
	 * return ret; }
	 */

	public Instance terminal(String g) throws FQLException {
		return Instance.terminal(this, g);
	}

	/*
	 * @Override public JPanel json() { JTextArea q = new JTextArea(tojson());
	 * // q.setWrapStyleWord(true); // q.setLineWrap(true); JPanel p = new
	 * JPanel(new GridLayout(1, 1)); JScrollPane jsc = new JScrollPane(q); //
	 * jsc.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	 * p.add(jsc); return p; }
	 */

	JPanel den = null;

	public JPanel denotation() {
		try {
			if (den == null) {
				toCategory2();
				den = makePanel();
			}
			return den;
		} catch (FQLException fe) {
			den = new JPanel(new GridLayout(1,1));
			JTextArea a = new JTextArea(fe.getMessage());
			JScrollPane jsp = new JScrollPane(a);
			den.add(jsp);
			return den;
		}
	}

	public JPanel makePanel() throws FQLException {
		JPanel ret = new JPanel(new GridLayout(1, 1));
		JTabbedPane t = new JTabbedPane();

		JPanel p = new JPanel(new GridLayout(1, 1));
		JTextArea a = new JTextArea();
		JPanel q = null;
		JPanel rr = new JPanel(new GridLayout(1, 1));

		a.setText(cached.first.toString());
		a.setCaretPosition(0);

		q = makeNormalizer();

		JTextArea ra = new JTextArea(cached.first.toSig(types).first.toString());
		rr.add(new JScrollPane(ra));

		p.add(new JScrollPane(a));

		t.addTab("Category", p);
		t.addTab("Signature", rr);
		t.addTab("Normalizer", q);

		ret.add(t);
		return ret;
	}

	private JPanel makeNormalizer() {
		final JPanel ret = new JPanel(new BorderLayout());

		JPanel p = new JPanel(new GridLayout(2, 1));
		final FQLTextPanel p1 = new FQLTextPanel("Input path", "");
		final FQLTextPanel p2 = new FQLTextPanel("Normalized path", "");
		p.add(p1);
		p.add(p2);

		JButton b = new JButton("Normalize");
		b.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = p1.getText();
				try {
					Path path = Path.parsePath(Signature.this, s);
					Path ap = cached.second.of(path).arr;
					p2.setText(ap.toString());
				} catch (FQLException ex) {
					p2.setText(ex.toString());
				}
			}
		});

		ret.add(p, BorderLayout.CENTER);
		ret.add(b, BorderLayout.PAGE_END);

		return ret;
	}

	public Signature onlyObjects() {
		return new Signature(nodes, new LinkedList<Edge>(),
				new LinkedList<Attribute<Node>>(), new HashSet<Eq>());
	}

	public boolean isNode(String s) {
		try {
			return getNode(s) != null;
		} catch (FQLException fql) {
			return false;
		}
	}

	private Map<Node, List<Attribute<Node>>> attrsFor_cache = new HashMap<>();
	public List<Attribute<Node>> attrsFor(Node n) {
		List<Attribute<Node>> a = attrsFor_cache.get(n);
		if (a != null) {
			return a;
		}
		a = new LinkedList<>();
		for (Attribute<Node> x : attrs) {
			if (x.source.equals(n)) {
				a.add(x);
			}
		}
		attrsFor_cache.put(n, a);
		return a;
	}

	public List<Node> order() {
		List<Node> ret = new LinkedList<>(nodes);
		Comparator<Node> c = new Comparator<Node>() {
			@Override
			public int compare(Node o1, Node o2) {
				return edgesFrom(o1).size() - edgesFrom(o2).size(); 
			}			
		};
		Collections.sort(ret, c);
	//	System.out.println("order " + ret);
		return ret;
	}
	
	private Map<Node, List<Edge>> edgesFrom_cache = new HashMap<>();
	public List<Edge> edgesFrom(Node n) {
		List<Edge> a = edgesFrom_cache.get(n);
		if (a != null) {
			return a;
		}
		a = new LinkedList<>();
		for (Edge x : edges) {
			if (x.source.equals(n)) {
				a.add(x);
			}
		}
		edgesFrom_cache.put(n, a);
		return a;
	}
	
	private Map<Node, List<Edge>> edgesTo_cache = new HashMap<>();
	public List<Edge> edgesTo(Node n) {
		List<Edge> a = edgesTo_cache.get(n);
		if (a != null) {
			return a;
		}
		a = new LinkedList<>();
		for (Edge x : edges) {
			if (x.target.equals(n)) {
				a.add(x);
			}
		}
		edgesTo_cache.put(n, a);
		return a;
	}
	/*
	 * public JPanel initial() throws FQLException { List<Pair<String,
	 * List<Pair<Object, Object>>>> b = new LinkedList<>(); for (Node n : nodes)
	 * { List<Pair<Object, Object>> x = new LinkedList<>(); b.add(new
	 * Pair<>(n.string, x)); } for (Attribute<Node> n : attrs) {
	 * List<Pair<Object, Object>> x = new LinkedList<>(); b.add(new
	 * Pair<>(n.name, x)); } for (Edge n : edges) { List<Pair<Object, Object>> x
	 * = new LinkedList<>(); b.add(new Pair<>(n.name, x)); } Instance i = new
	 * Instance("", this, b); return i.join(); }
	 */

	public JPanel constraint() {
		List<EmbeddedDependency> l = toED("");

		JPanel ret = new JPanel(new GridLayout(1, 1));
		ret.setBorder(BorderFactory.createEtchedBorder());
		String s = "";
		int i = 0;
		for (EmbeddedDependency d : l) {
			if (i++ > 0) {
				s += "\n\n";
			}
			s += d.toString();
		}
		JTextArea area = new JTextArea(s);
		area.setFont(new Font("Courier", Font.PLAIN, 13));
		JScrollPane jsp = new JScrollPane(area);
		area.setWrapStyleWord(true);
		area.setLineWrap(true);
		jsp.setBorder(BorderFactory.createEmptyBorder());
		ret.add(jsp);
		return ret;
	}

	//

	public List<EmbeddedDependency> toED(String pre) {
		List<Pair<Attribute<Node>, Pair<Edge, Attribute<Node>>>> x = new LinkedList<>();
		return toED(pre, new Pair<>(this, x));
	}

	public static List<EmbeddedDependency> toED(
			String pre,
			Pair<Signature, List<Pair<Attribute<Node>, Pair<Edge, Attribute<Node>>>>> xxx) {
		Signature sig = xxx.first;

		// public List<EmbeddedDependency> toED(String pre) {
		List<EmbeddedDependency> ret = new LinkedList<>();

		int v = 0;
		for (Node n : sig.nodes) {
			List<String> forall = new LinkedList<>();
			List<String> exists = new LinkedList<>();
			List<Triple<String, String, String>> where = new LinkedList<>();
			List<Triple<String, String, String>> tgd = new LinkedList<>();
			List<Pair<String, String>> egd = new LinkedList<>();
			// List<Triple<String, String, String>> not = new LinkedList<>();

			String u = "v" + (v++);
			String w = "v" + (v++);

			forall.add(u);
			forall.add(w);
			where.add(new Triple<>(pre + n.string, u, w));
			egd.add(new Pair<>(u, w));

			// for (Node m : nodes) {
			// if (n.equals(m)) {
			// continue;
			// }
			// not.add(new Triple<>(name0 + "." + m.string, u, w));
			// }

			EmbeddedDependency ed = new EmbeddedDependency(forall, exists,
					where, tgd, egd);
			ret.add(ed);
		}

		for (Edge e : sig.edges) {
			List<String> forall = new LinkedList<>();
			List<String> exists = new LinkedList<>();
			List<Triple<String, String, String>> where = new LinkedList<>();
			List<Triple<String, String, String>> tgd = new LinkedList<>();
			List<Pair<String, String>> egd = new LinkedList<>();
			// List<Triple<String, String, String>> not = new LinkedList<>();

			String u = "v" + (v++);
			String w = "v" + (v++);

			forall.add(u);
			forall.add(w);
			where.add(new Triple<>(pre + e.name, u, w));
			tgd.add(new Triple<>(pre + e.source.string, u, u));
			tgd.add(new Triple<>(pre + e.target.string, w, w));

			EmbeddedDependency ed = new EmbeddedDependency(forall, exists,
					where, tgd, egd);
			ret.add(ed);

			forall = new LinkedList<>();
			exists = new LinkedList<>();
			where = new LinkedList<>();
			tgd = new LinkedList<>();
			egd = new LinkedList<>();
			// not = new LinkedList<>();

			String x = "v" + (v++);
			forall.add(u);
			forall.add(w);
			forall.add(x);
			where.add(new Triple<>(pre + e.name, u, w));
			where.add(new Triple<>(pre + e.name, u, x));
			egd.add(new Pair<>(w, x));

			ed = new EmbeddedDependency(forall, exists, where, tgd, egd);
			ret.add(ed);

			forall = new LinkedList<>();
			exists = new LinkedList<>();
			where = new LinkedList<>();
			tgd = new LinkedList<>();
			egd = new LinkedList<>();
			// not = new LinkedList<>();
			forall.add(x);
			where.add(new Triple<>(pre + e.source.string, x, x));
			String z = "v" + (v++);
			exists.add(z);
			tgd.add(new Triple<>(pre + e.name, x, z));

			ed = new EmbeddedDependency(forall, exists, where, tgd, egd);
			ret.add(ed);

		}

		// TODO EDs for transforms

		for (Attribute<Node> e : sig.attrs) {
			List<String> forall = new LinkedList<>();
			List<String> exists = new LinkedList<>();
			List<Triple<String, String, String>> where = new LinkedList<>();
			List<Triple<String, String, String>> tgd = new LinkedList<>();
			List<Pair<String, String>> egd = new LinkedList<>();
			// List<Triple<String, String, String>> not = new LinkedList<>();

			String u = "v" + (v++);
			String w = "v" + (v++);

			forall.add(u);
			forall.add(w);
			where.add(new Triple<>(pre + e.name, u, w));
			tgd.add(new Triple<>(pre + e.source.string, u, u));
			// tgd.add(new Triple<>(name0 + "." + e.target., w, w));

			EmbeddedDependency ed = new EmbeddedDependency(forall, exists,
					where, tgd, egd);
			ret.add(ed);

			forall = new LinkedList<>();
			exists = new LinkedList<>();
			where = new LinkedList<>();
			tgd = new LinkedList<>();
			egd = new LinkedList<>();
			// not = new LinkedList<>();

			String x = "v" + (v++);
			forall.add(u);
			forall.add(w);
			forall.add(x);
			where.add(new Triple<>(pre + e.name, u, w));
			where.add(new Triple<>(pre + e.name, u, x));
			egd.add(new Pair<>(w, x));

			ed = new EmbeddedDependency(forall, exists, where, tgd, egd);
			ret.add(ed);

			forall = new LinkedList<>();
			exists = new LinkedList<>();
			where = new LinkedList<>();
			tgd = new LinkedList<>();
			egd = new LinkedList<>();
			// not = new LinkedList<>();
			forall.add(x);
			where.add(new Triple<>(pre + e.source.string, x, x));
			String z = "v" + (v++);
			exists.add(z);
			tgd.add(new Triple<>(pre + e.name, x, z));

			ed = new EmbeddedDependency(forall, exists, where, tgd, egd);
			ret.add(ed);
		}

		for (Eq eq : sig.eqs) {
			ret.add(EmbeddedDependency.eq2(pre, eq.lhs, eq.rhs));
		}

		for (Pair<Attribute<Node>, Pair<Edge, Attribute<Node>>> eq : xxx.second) {
			ret.add(EmbeddedDependency.eq3(pre, eq));
		}

		return ret;
	}

	public static Signature sum(String c, String d, Signature C, Signature D)
			throws FQLException {
		List<String> o = new LinkedList<>();
		for (Node node : C.nodes) {
			o.add(c + "_" + node.string);
		}
		for (Node node : D.nodes) {
			o.add(d + "_" + node.string);
		}

		List<Triple<String, String, String>> e = new LinkedList<>();
		for (Edge edge : C.edges) {
			e.add(new Triple<>(c + "_" + edge.name, c + "_"
					+ edge.source.string, c + "_" + edge.target.string));
		}
		for (Edge edge : D.edges) {
			e.add(new Triple<>(d + "_" + edge.name, d + "_"
					+ edge.source.string, d + "_" + edge.target.string));
		}

		Map<String, Type> types = new HashMap<>();
		List<Triple<String, String, String>> a = new LinkedList<>();
		for (Attribute<Node> edge : C.attrs) {
			types.put(edge.target.toString(), edge.target);
			a.add(new Triple<>(c + "_" + edge.name, c + "_"
					+ edge.source.string, edge.target.toString()));
		}
		for (Attribute<Node> edge : D.attrs) {
			types.put(edge.target.toString(), edge.target);
			a.add(new Triple<>(d + "_" + edge.name, d + "_"
					+ edge.source.string, edge.target.toString()));
		}

		List<Pair<List<String>, List<String>>> cc = new LinkedList<>();
		for (Eq eq : C.eqs) {
			List<String> lhs = new LinkedList<>();
			lhs.add(c + "_" + eq.lhs.source.string);
			for (Edge edge : eq.lhs.path) {
				lhs.add(c + "_" + edge.name);
			}
			List<String> rhs = new LinkedList<>();
			rhs.add(c + "_" + eq.rhs.source.string);
			for (Edge edge : eq.rhs.path) {
				rhs.add(c + "_" + edge.name);
			}
			cc.add(new Pair<>(lhs, rhs));
		}
		for (Eq eq : D.eqs) {
			List<String> lhs = new LinkedList<>();
			lhs.add(d + "_" + eq.lhs.source.string);
			for (Edge edge : eq.lhs.path) {
				lhs.add(d + "_" + edge.name);
			}
			List<String> rhs = new LinkedList<>();
			rhs.add(d + "_" + eq.rhs.source.string);
			for (Edge edge : eq.rhs.path) {
				rhs.add(d + "_" + edge.name);
			}
			cc.add(new Pair<>(lhs, rhs));
		}

		return new Signature(types, o, a, e, cc);
	}

	public JPanel rdf() {
		JTextArea ta = new JTextArea(rdfX());
		JPanel tap = new JPanel(new GridLayout(1, 1));
		ta.setBorder(BorderFactory.createEmptyBorder());
		//
		tap.setBorder(BorderFactory.createEmptyBorder());
		// ta.setWrapStyleWord(true);
		// ta.setLineWrap(true);
		JScrollPane xxx = new JScrollPane(ta);
		// xxx.setBorder(BorderFactory.createEmptyBorder());
		//
		tap.add(xxx);
		// tap.setSize(600, 600);

		return tap;
	}

	public String rdfX() {
		String xxx = "";
		// String prefix = "fql://entity/"; // + name + "/";

		for (Node n : nodes) {
			xxx += "<owl:Class rdf:about=\"fql://node/" + n.string + "\"/>\n";
			xxx += "\n";
		}
		// xxx += "\n";
		for (Attribute<Node> a : attrs) {
			xxx += "<owl:FunctionalProperty rdf:about=\"fql://attribute/"
					+ a.name + "\">\n";
			xxx += "    <rdfs:domain rdf:resource=\"fql://node/"
					+ a.source.string + "\"/>\n";
			if (a.target instanceof Type.Int) {
				xxx += "    <rdfs:range rdf:resource=\"http://www.w3.org/2001/XMLSchema#int\"/>\n";
			} else {
				xxx += "    <rdfs:range rdf:resource=\"http://www.w3.org/2001/XMLSchema#string\"/>\n";
			}
			xxx += "</owl:FunctionalProperty>\n";
			xxx += "\n";
		}
		// xxx += "\n";
		for (Edge a : edges) {
			xxx += "<owl:FunctionalProperty rdf:about=\"fql://arrow/" + a.name
					+ "\">\n";
			xxx += "    <rdfs:domain rdf:resource=\"fql://node/"
					+ a.source.string + "\"/>\n";
			xxx += "    <rdfs:range rdf:resource=\"fql://node/"
					+ a.target.string + "\"/>\n";
			xxx += "</owl:FunctionalProperty>\n";
			xxx += "\n";
		}
		// xxx += "\n";
		String ret = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
				+ "\n<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\""
				+ "\n    xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\""
				+ "\n    xmlns:owl=\"http://www.w3.org/2002/07/owl#\""
				+ "\n    xmlns:node=\"fql://node/\""
				+ "\n    xmlns:arrow=\"fql://arrow/\""
				+ "\n    xmlns:attribute=\"fql://attribute/\">\n\n"
				+ xxx
				+ "<!-- Note: generated OWL schemas do not include path equations, or enforce that properties must be total. -->\n\n"
				+ "</rdf:RDF>";
		return ret;
	}

	

	private Triple<Instance, Map<Object, Path>, Map<Path, Object>> rep(
			IntRef idx, Node c) throws FQLException {
		Map<Object, Path> m1 = new HashMap<>();
		Map<Path, Object> m2 = new HashMap<>();
		Map<String, Set<Pair<Object, Object>>> data = new HashMap<>();

		Pair<Map<Node, Set<Path>>, Map<Edge, Map<Path, Path>>> xxx = rep0(c);
		Map<Node, Set<Path>> nm = xxx.first;
		Map<Edge, Map<Path, Path>> em = xxx.second;

		for (Node n : nodes) {
			Set<Path> s = nm.get(n);
			Set<Pair<Object, Object>> t = new HashSet<>();
			for (Path p : s) {
				String str = Integer.toString(++idx.i);
				m1.put(str, p);
				if (m2.containsKey(p)) {
					throw new RuntimeException();
				}
				m2.put(p, str);
				t.add(new Pair<Object, Object>(str, str));
			}
			data.put(n.string, t);
		}
		for (Edge e : edges) {
			Map<Path, Path> s = em.get(e);
			Set<Pair<Object, Object>> t = new HashSet<>();
			for (Entry<Path, Path> p : s.entrySet()) {
				t.add(new Pair<Object, Object>(m2.get(p.getKey()), m2.get(p
						.getValue())));
			}
			data.put(e.name, t);

		}

		return new Triple<>(new Instance(this, data), m1, m2);
	}
	
	public Pair<Pair<Map<Node, Triple<Instance, Map<Object, Path>, Map<Path, Object>>>, Map<Edge, Transform>>, Pair<Instance, Map<Node, Pair<Map<Object, Instance>, Map<Instance, Object>>>>> omega(IntRef ref) throws FQLException {
		//System.out.println("Starting omega");
		IntRef ix = new IntRef(0);
		Map<String, Set<Pair<Object, Object>>> data = new HashMap<>();
		Map<Node, Pair<Map<Object, Instance>, Map<Instance, Object>>> m = new HashMap<>();
		
		Pair<Map<Node, Triple<Instance, Map<Object, Path>, Map<Path, Object>>>, Map<Edge, Transform>> rx = repX(ix);
		
		for (Node n : nodes) {
			Set<Pair<Object, Object>> set = new HashSet<>();
			Map<Object, Instance> m1 = new HashMap<>();
			Map<Instance, Object> m2 = new HashMap<>();
			Triple<Instance, Map<Object, Path>, Map<Path, Object>> t = rx.first.get(n);
			List<Instance> l = t.first.subInstances();
			for (Instance i : l) {
				Object id = Integer.toString(++ref.i);
				set.add(new Pair<>(id, id));
				m1.put(id, i);
				m2.put(i, id);
			}
			data.put(n.string, set);
			m.put(n, new Pair<>(m1, m2));
		}
		
		for (Edge e : edges) {
			Set<Pair<Object, Object>> set = new HashSet<>();
			for (Pair<Object, Object> j : data.get(e.source.string)) {
			//	System.out.println("j " + j);
				Instance u = m.get(e.source).first.get(j.first);
			//	System.out.println("u " + u);
			//	System.out.println("trans " + rx.second.get(e));
				Instance v = rx.second.get(e).preimage(u);
			//	System.out.println("v " + v); 
				Object o = m.get(e.target).second.get(v);
			//	System.out.println("mgetarget " + m.get(e.target).second);
			//	System.out.println("o " + o);
				set.add(new Pair<>(j.first, o));
			}			
			data.put(e.name, set);
		}
		
		Instance omega = new Instance(this, data);
		//System.out.println("End omega");
		return new Pair<>(rx, new Pair<>(omega, m));
	}
	

	public Pair<Map<Node, Triple<Instance, Map<Object, Path>, Map<Path, Object>>>, Map<Edge, Transform>> repX(
			IntRef i) throws FQLException {
		toCategory2();
		Map<Node, Triple<Instance, Map<Object, Path>, Map<Path, Object>>> ret = new HashMap<>();
		for (Node n : nodes) {
			ret.put(n, rep(i, n));
		}
		Map<Edge, Transform> ret0 = new HashMap<>();
		for (Edge e : edges) {
			List<Pair<String, List<Pair<Object, Object>>>> d = new LinkedList<>();

			Triple<Instance, Map<Object, Path>, Map<Path, Object>> s = ret
					.get(e.source);
			Triple<Instance, Map<Object, Path>, Map<Path, Object>> t = ret
					.get(e.target);
			
			for (Node n : nodes) {
				List<Pair<Object, Object>> set = new LinkedList<>();
				for (Pair<Object, Object> id : t.first.data.get(n.string)) {
					Arr<Node, Path> uuu = cached.second.of(Path.append(this, new Path(this, e), t.second.get(id.first)));
				//	System.out.println("uuu " + uuu);
				//	System.out.println("st" + s.third);
					set.add(new Pair<>(id.first, s.third.get(uuu.arr)));
				}
				d.add(new Pair<>(n.string, set));
			}
			ret0.put(e, new Transform(t.first, s.first, d));
		}
		return new Pair<>(ret, ret0);
	}

	private Pair<Map<Node, Set<Path>>, Map<Edge, Map<Path, Path>>> rep0(Node c)
			throws FQLException {
		// Pair<FinCat<Node, Path>, Fn<Path, Arr<Node, Path>>> xxx =
		// toCategory2();
		// FinCat<Node, Path> cat = xxx.first;
		// Fn<Path, Arr<Node, Path>> fn = xxx.second;

		Map<Node, Set<Path>> m1 = new HashMap<>();
		Map<Edge, Map<Path, Path>> m2 = new HashMap<>();

		for (Node a : nodes) {
			Set<Path> p = new HashSet<>();
			for (Arr<Node, Path> arr : cached.first.hom(c, a)) {
				p.add(arr.arr);
			}
			m1.put(a, p);
		}
		for (Edge arr : edges) {
			Map<Path, Path> m = new HashMap<>();
			for (Path p : m1.get(arr.source)) {
				m.put(p, cached.second.of(Path.append(this, p, new Path(this,
						arr))).arr);
			}
			m2.put(arr, m);
		}

		return new Pair<>(m1, m2);
	}

	Map<Node, List<Pair<Arr<Node, Path>, Attribute<Node>>>> obs_cached = null;
	public  Map<Node, List<Pair<Arr<Node, Path>, Attribute<Node>>>> obs() 
			throws FQLException {
		if (obs_cached != null) {
			return obs_cached;
		}
		Map<Node, List<Pair<Arr<Node, Path>, Attribute<Node>>>> ret = new HashMap<>();

		Pair<FinCat<Node, Path>, Fn<Path, Arr<Node, Path>>> xxx = 
				toCategory2();
		FinCat<Node, Path> cat = xxx.first;

		for (Node c : nodes) {
			List<Pair<Arr<Node, Path>, Attribute<Node>>> l = new LinkedList<>();
			for (Node d : nodes) {
				for (Arr<Node, Path> p : cat.hom(c, d)) {
					for (Attribute<Node> a : attrsFor(d)) {
						l.add(new Pair<>(p, a));
					}
				}
			}
			ret.put(c, l);
		}
		obs_cached = ret;
		return ret;
	}
	
	Map<Node, List<LinkedHashMap<Pair<Arr<Node, Path>, Attribute<Node>>, Object>>> obsbar_cached = null;
	public  Map<Node, List<LinkedHashMap<Pair<Arr<Node, Path>, Attribute<Node>>, Object>>> obsbar() 
			throws FQLException {
		if (obsbar_cached != null) {
			return obsbar_cached;
		}
		Map<Node, List<LinkedHashMap<Pair<Arr<Node, Path>, Attribute<Node>>, Object>>> ret = new HashMap<>();

		Pair<FinCat<Node, Path>, Fn<Path, Arr<Node, Path>>> xxx = 
				toCategory2();
		FinCat<Node, Path> cat = xxx.first;

		for (Node c : nodes) {
			Map<Pair<Arr<Node, Path>, Attribute<Node>>, List<Object>> m = new HashMap<>();

			for (Node d : nodes) {
				for (Arr<Node, Path> p : cat.hom(c, d)) {
					for (Attribute<Node> a : attrsFor(d)) {
						Type.Enum en = (Type.Enum) a.target;
						m.put(new Pair<>(p, a), new LinkedList<Object>(en.values));
					}
				}
			}
			List<LinkedHashMap<Pair<Arr<Node, Path>, Attribute<Node>>, Object>> set = Inst.homomorphs(m);
			ret.put(c, set);
		}
		obsbar_cached = ret;
		return ret;
	}
	
}
