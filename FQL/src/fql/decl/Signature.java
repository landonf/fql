package fql.decl;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Paint;
import java.awt.Stroke;
import java.util.Arrays;
import java.util.Collection;
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
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.UIManager;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.algorithms.layout.ISOMLayout;
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
import fql.cat.Denotation;
import fql.cat.FinCat;
import fql.sql.EmbeddedDependency;

/**
 * 
 * @author ryan
 * 
 *         Signatures.
 */
public class Signature  {

	public List<Node> nodes;
	public List<Edge> edges;
	public List<Attribute<Node>> attrs;
	
	public Map<String, Color> colors;

	public static Color[] colors_arr = new Color[] { Color.RED, Color.GREEN, Color.BLUE, Color.MAGENTA, Color.yellow, Color.CYAN, Color.GRAY, Color.ORANGE, Color.PINK, Color.BLACK};
	
	public Set<Eq> eqs;
//	public String name0;

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
	}
	
	public Signature clone() {
		Signature s = new Signature();
		s.nodes = new LinkedList<>(nodes);
		s.edges = new LinkedList<>(edges);
		s.attrs = new LinkedList<>(attrs);
		s.eqs = new HashSet<>(eqs);
		s.colors = new HashMap<>(colors);
	//	s.name0 = newname;
		return s;
	}
	
	public Signature(Map<String, Type> types, List<String> nodes_str,
			List<Triple<String, String, String>> attrs_str,
			List<Triple<String, String, String>> arrows,
			List<Pair<List<String>, List<String>>> equivs) throws FQLException {
		Set<Node> nodesA = new HashSet<>();
		Set<Edge> edgesA = new HashSet<>();
		Set<Attribute<Node>> attrsA = new HashSet<>();
	//	name0 = n;

		Set<String> seen = new HashSet<String>();
		for (String s : nodes_str) {
			if (seen.contains(s)) {
				throw new FQLException("Duplicate name: " + s);
			}
			seen.add(s);
			nodesA.add(new Node(s));
		}

		for (Triple<String, String, String> arrow : arrows) {
			String name = arrow.first;
			String source = arrow.second;
			String target = arrow.third;

			if (seen.contains(name)) {
				throw new FQLException("Duplicate name: " + name);
			}
			seen.add(name);

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

			if (seen.contains(name)) {
				throw new FQLException("Duplicate name: " + name);
			}
			seen.add(name);

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
						+ " and " + rhs ); //+ " in " + name0);
			}
			if (!lhs.target.equals(rhs.target)) {
				throw new FQLException("target object mismatch " + lhs
						+ " and " + rhs ); //+ " in " + name0);
			}
			Eq eq = new Eq(lhs, rhs);
			eqs.add(eq);
		}
		
		Collections.sort(nodes);
		Collections.sort(attrs);
		Collections.sort(arrows);
		
		//System.out.println(this);
		if (!DEBUG.debug.ALLOW_INFINITES) {
		//	try {
				toCategory2();
		//	} catch (FQLException fe) {
		/*		try {
					JPanel p = denotation();
					JFrame fr = new JFrame("Category Denotation Debugger");
					fr.setContentPane(p);
					fr.pack();
					fr.setSize(600, 400);
					fr.setVisible(true);
				} catch (Throwable fex) { fex.printStackTrace(); } */
			//	throw fe;
			//}
		}
		
		doColors();
	}
/*
	public Signature(String n, List<Triple<String, String, String>> arrows,
			List<Pair<List<String>, List<String>>> equivs) throws FQLException {
		Set<Node> nodesA = new HashSet<>();
		Set<Edge> edgesA = new HashSet<>();
		Set<Attribute<Node>> attrsA = new HashSet<>();
		name0 = n;

		Set<String> seen = new HashSet<String>();
		for (Triple<String, String, String> arrow : arrows) {
			String name = arrow.first;
			String source = arrow.second;
			String target = arrow.third;

			if (seen.contains(name)) {
				throw new FQLException("Duplicate name: " + name);
			}
			seen.add(name);

			// isolated node
			if (source == null) {
				Node nd = new Node(name);
				nodesA.add(nd);
				continue;
			}

			Node source_node = lookup(source, nodesA);
			if (source_node == null) {
				source_node = new Node(source);
			}
			nodesA.add(source_node);

			Type t;
			if ((t = tryParseType(target)) != null) {
				Attribute<Node> a = new Attribute<>(name, source_node, t);
				attrsA.add(a);
			} else {
				Node target_node = lookup(target, nodesA);
				if (target_node == null) {
					target_node = new Node(target);
				}
				nodesA.add(target_node);

				Edge e = new Edge(name, source_node, target_node);
				edgesA.add(e);
			}
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
						+ " and " + rhs);
			}
			if (!lhs.target.equals(rhs.target)) {
				throw new FQLException("target object mismatch " + lhs
						+ " and " + rhs);
			}
			Eq eq = new Eq(lhs, rhs);
			eqs.add(eq);
		}
		if (!DEBUG.ALLOW_INFINITES) {
			toCategory2();
		}
	}
*/
	/*
	private Type tryParseType(String s) {
		if (s.equals("string")) {
			return new Varchar();
		} else if (s.equals("int")) {
			return new Int();
		} else {
			return new Type.
		}
		return null;
	}
	*/
/*
	// for json
	public Signature(
			List<String> obs,
			List<Pair<Pair<String, String>, String>> arrows,
			List<Pair<List<Pair<Pair<String, String>, String>>, List<Pair<Pair<String, String>, String>>>> equivs)
			throws FQLException {
		Set<Node> nodesA = new HashSet<Node>();
		Set<Edge> edgesA = new HashSet<Edge>();
		attrs = new LinkedList<>();

		// name0 = n;

		Set<String> seen_obs = new HashSet<String>();
		for (String o : obs) {
			if (seen_obs.contains(o)) {
				throw new FQLException("Duplicate object : " + o);
			}
			seen_obs.add(o);
			nodesA.add(new Node(o));
		}

		Set<String> seen = new HashSet<>();
		for (Pair<Pair<String, String>, String> arrow : arrows) {
			String name = arrow.second;
			String source = arrow.first.first;
			String target = arrow.first.second;

			if (seen.contains(name)) {
				throw new FQLException("Duplicate edge: " + name);
			}
			seen.add(name);

			Node source_node = lookup(source, nodesA);
			// if (source_node == null) {
			// throw new FQLException("Missing node " + source_node + " in " +
			// name0);
			// }
			Node target_node = lookup(target, nodesA);
			// if (target_node == null) {
			// throw new FQLException("Missing node " + target_node + " in " +
			// name0);
			// }

			Edge e = new Edge(name, source_node, target_node);
			edgesA.add(e);
		}

		nodes = new LinkedList<Node>(nodesA);
		edges = new LinkedList<Edge>(edgesA);

		eqs = new HashSet<Eq>();
		for (Pair<List<Pair<Pair<String, String>, String>>, List<Pair<Pair<String, String>, String>>> equiv : equivs) {
			if (equiv.first.size() == 0 && equiv.second.size() == 0) {
				throw new FQLException("empty eq " + equiv);
			}
			Path lhs, rhs;
			List<String> temp = new LinkedList<>();
			if (equiv.first.size() == 0) {
				rhs = new Path(new Unit(), this, equiv.second);
				temp.add(rhs.source.string);
				lhs = new Path(this, temp);
			} else if (equiv.second.size() == 0) {
				lhs = new Path(new Unit(), this, equiv.first);
				temp.add(lhs.source.string);
				rhs = new Path(this, temp);
			} else {
				lhs = new Path(new Unit(), this, equiv.first);
				rhs = new Path(new Unit(), this, equiv.second);
			}

			if (!lhs.source.equals(rhs.source)) {
				throw new FQLException("source object mismatch " + lhs
						+ " and " + rhs);
			}
			if (!lhs.target.equals(rhs.target)) {
				throw new FQLException("target object mismatch " + lhs
						+ " and " + rhs);
			}
			Eq eq = new Eq(lhs, rhs);
			eqs.add(eq);
		}

	}
*/
	public Signature(List<Node> n, List<Edge> e, List<Attribute<Node>> a,
			Set<Eq> ee) {
	//	name0 = s;
		nodes = n;
		edges = e;
		eqs = ee;
		attrs = a;
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
		//p.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

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

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer("{\n");

		boolean first = true;
		sb.append("nodes\n");
		for (Node n : nodes) {
			if (!first) {
				sb.append(",\n");
			}
			first = false;
			sb.append(n.string);
		}
		sb.append("\n ;\n");

		first = true;
		sb.append("attributes\n");
		for (Attribute<Node> a : attrs) {
			if (!first) {
				sb.append(",\n");
			}
			first = false;
			sb.append(a);
		}
		sb.append("\n ;\n");

		first = true;
		sb.append("arrows\n");
		for (Edge e : edges) {
			if (!first) {
				sb.append(",\n");
			}
			first = false;
			sb.append(e);
		}
		// first = true;

		sb.append("\n ;\n");

		first = true;
		sb.append("equations\n");
		for (Eq eq : eqs) {
			if (!first) {
				sb.append(",\n");
			}
			first = false;
			sb.append(eq);
		}
		sb.append("\n ;\n}");
		return sb.toString();
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
		//ta.setBorder(BorderFactory.createEmptyBorder());
		//
		//tap.setBorder(BorderFactory.createEtchedBorder());
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

	public Pair<FinCat<Node, Path>, Fn<Path, Arr<Node, Path>>> toCategory2()
			throws FQLException {
		Denotation d = new Denotation(this);
		Pair<FinCat<Node, Path>, Fn<Path, Arr<Node, Path>>> ret = d.toCategory();
		
		ret.first.attrs = attrs;
		
		return ret;
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

	public JComponent makeViewer(/* final Environment env */) {
		Graph<String, String> g = build();
		if (g.getVertexCount() == 0) {
			return new JPanel();
		}
		return doView(/* env, */ g);
	}

	public JComponent doView(/* final Environment env, */Graph<String, String> sgv) {
		// Layout<V, E>, BasicVisualizationServer<V,E>
		// Layout<String, String> layout = new FRLayout(sgv);
		Layout<String, String> layout = new ISOMLayout<String, String>(sgv);
		// Layout<String, String> layout = new CircleLayout(sgv);
		layout.setSize(new Dimension(600, 400));
		// BasicVisualizationServer<String, String> vv = new
		// BasicVisualizationServer<String, String>(
		// layout);
		VisualizationViewer<String, String> vv = new VisualizationViewer<String, String>(
				layout);
		//vv.setPreferredSize(new Dimension(600, 400));
		// Setup up a new vertex to paint transformer...
		 Transformer<String, Paint> vertexPaint = new Transformer<String, Paint>() {
			public Paint transform(String i) {
				if (isAttribute(i)) {
					return UIManager.getColor("Panel.background");
//					return env.colors.get(name0);
				} else {
					return colors.get(i);
				}
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
		vv.getRenderContext().setEdgeStrokeTransformer(edgeStrokeTransformer);
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
		JPanel ret = new JPanel(new GridLayout(1,1));
		ret.add(zzz);
		ret.setBorder(BorderFactory.createEtchedBorder());
		return ret;
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

	public JComponent pretty(/*final Environment env*/) throws FQLException {
		return makeViewer();
	}

//	public String type() {
//		return "schema";
//	}

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
	public String tojson() {
		String ns = PrettyPrinter.sep(",", "[", "]", nodes);
		String es = PrettyPrinter.sep(",\n", "[", "]", edges);
		String rs = PrettyPrinter.sep(",", "[", "]", new LinkedList<>(eqs));
		String ret = "{\n\"objects\": " + ns + " , \n\"arrows\": " + es
				+ " ,\n\"relations\": " + rs + "\n}";

		// try {
		// Partial<Signature> out = new
		// JSONParsers.JSONSigParser(true).parse(new Tokens(ret));
		// System.out.println(out.value);
		// } catch(Exception e) {
		// e.printStackTrace();
		// }

		return ret;
	}
	*/

	public Instance terminal(String g) throws FQLException {
		return Instance.terminal(this, g);
	}
/*
	@Override
	public JPanel json() {
		JTextArea q = new JTextArea(tojson());
		// q.setWrapStyleWord(true);
		// q.setLineWrap(true);
		JPanel p = new JPanel(new GridLayout(1, 1));
		JScrollPane jsc = new JScrollPane(q);
		// jsc.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		p.add(jsc);
		return p;
	}
	*/

	JPanel den = null;

	public JPanel denotation() throws FQLException {
		if (den == null) {
			den = new Denotation(this).view();
		}
		return den;
	}

	public Signature onlyObjects() {
		return new Signature(nodes,
				new LinkedList<Edge>(), new LinkedList<Attribute<Node>>(),
				new HashSet<Eq>());
	}

	public boolean isNode(String s) {
		try {
			return getNode(s) != null;
		} catch (FQLException fql) {
			return false;
		}
	}

	public List<Attribute<Node>> attrsFor(Node n) {
		List<Attribute<Node>> a = new LinkedList<>();
		for (Attribute<Node> x : attrs) {
			if (x.source.equals(n)) {
				a.add(x);
			}
		}
		return a;
	}

	/*
	public JPanel initial() throws FQLException {
		List<Pair<String, List<Pair<Object, Object>>>> b = new LinkedList<>();
		for (Node n : nodes) {
			List<Pair<Object, Object>> x = new LinkedList<>();
			b.add(new Pair<>(n.string, x));
		}
		for (Attribute<Node> n : attrs) {
			List<Pair<Object, Object>> x = new LinkedList<>();
			b.add(new Pair<>(n.name, x));
		}
		for (Edge n : edges) {
			List<Pair<Object, Object>> x = new LinkedList<>();
			b.add(new Pair<>(n.name, x));
		}
		Instance i = new Instance("", this, b);
		return i.join();
	}
	*/

	
	public JPanel constraint() {
		List<EmbeddedDependency> l = toED("");
		
		JPanel ret = new JPanel(new GridLayout(1,1));
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

	public List<EmbeddedDependency> toED(String pre) {
		List<EmbeddedDependency> ret = new LinkedList<>();
		
		
		int v = 0;
		for (Node n : nodes) {
			List<String> forall = new LinkedList<>();
			List<String> exists = new LinkedList<>();
			List<Triple<String, String, String>> where = new LinkedList<>();
			List<Triple<String, String, String>> tgd = new LinkedList<>();
			List<Pair<String, String>> egd = new LinkedList<>();
		//	List<Triple<String, String, String>> not = new LinkedList<>();
			
			String u = "v" + (v++);
			String w = "v" + (v++);
			
			forall.add(u);
			forall.add(w);
			where.add(new Triple<>(pre + n.string, u, w));
			egd.add(new Pair<>(u, w));
			
//			for (Node m : nodes) {
//				if (n.equals(m)) {
//					continue;
//				}
//				not.add(new Triple<>(name0 + "." + m.string, u, w));
//			}

			EmbeddedDependency ed = new EmbeddedDependency(forall, exists, where, tgd, egd);
			ret.add(ed);
		}
		
		for (Edge e : edges) {
			List<String> forall = new LinkedList<>();
			List<String> exists = new LinkedList<>();
			List<Triple<String, String, String>> where = new LinkedList<>();
			List<Triple<String, String, String>> tgd = new LinkedList<>();
			List<Pair<String, String>> egd = new LinkedList<>();
		//	List<Triple<String, String, String>> not = new LinkedList<>();
		
			String u = "v" + (v++);
			String w = "v" + (v++);
			
			forall.add(u);
			forall.add(w);
			where.add(new Triple<>(pre + e.name, u, w));
			tgd.add(new Triple<>(pre + e.source.string, u, u));
			tgd.add(new Triple<>(pre + e.target.string, w, w));
			
			EmbeddedDependency ed = new EmbeddedDependency(forall, exists, where, tgd, egd);
			ret.add(ed);
			
			forall = new LinkedList<>();
			exists = new LinkedList<>();
			where = new LinkedList<>();
			tgd = new LinkedList<>();
			egd = new LinkedList<>();
		//	not = new LinkedList<>();
			
			String x = "v" + (v++);
			forall.add(u); forall.add(w); forall.add(x);
			where.add(new Triple<>(pre + e.name, u, w));
			where.add(new Triple<>(pre + e.name, u, x));
			egd.add(new Pair<>(w,x));
			
			ed = new EmbeddedDependency(forall, exists, where, tgd, egd);
			ret.add(ed);
			
			forall = new LinkedList<>();
			exists = new LinkedList<>();
			where = new LinkedList<>();
			tgd = new LinkedList<>();
			egd = new LinkedList<>();
		//	not = new LinkedList<>();
			forall.add(x);
			where.add(new Triple<>(pre + e.source.string, x, x));
			String z = "v" + (v++);
			exists.add(z);
			tgd.add(new Triple<>(pre + e.name, x, z));

			ed = new EmbeddedDependency(forall, exists, where, tgd, egd);
			ret.add(ed);
			
		}
		
		for (Attribute<Node> e : attrs) {
			List<String> forall = new LinkedList<>();
			List<String> exists = new LinkedList<>();
			List<Triple<String, String, String>> where = new LinkedList<>();
			List<Triple<String, String, String>> tgd = new LinkedList<>();
			List<Pair<String, String>> egd = new LinkedList<>();
			//List<Triple<String, String, String>> not = new LinkedList<>();
		
			String u = "v" + (v++);
			String w = "v" + (v++);
			
			forall.add(u);
			forall.add(w);
			where.add(new Triple<>(pre + e.name, u, w));
			tgd.add(new Triple<>(pre + e.source.string, u, u));
	//		tgd.add(new Triple<>(name0 + "." + e.target., w, w));
			
			EmbeddedDependency ed = new EmbeddedDependency(forall, exists, where, tgd, egd);
			ret.add(ed);
			
			forall = new LinkedList<>();
			exists = new LinkedList<>();
			where = new LinkedList<>();
			tgd = new LinkedList<>();
			egd = new LinkedList<>();
			//not = new LinkedList<>();
			
			String x = "v" + (v++);
			forall.add(u); forall.add(w); forall.add(x);
			where.add(new Triple<>(pre + e.name, u, w));
			where.add(new Triple<>(pre + e.name, u, x));
			egd.add(new Pair<>(w,x));
			
			ed = new EmbeddedDependency(forall, exists, where, tgd, egd);
			ret.add(ed);
			
			forall = new LinkedList<>();
			exists = new LinkedList<>();
			where = new LinkedList<>();
			tgd = new LinkedList<>();
			egd = new LinkedList<>();
			//not = new LinkedList<>();
			forall.add(x);
			where.add(new Triple<>(pre + e.source.string, x, x));
			String z = "v" + (v++);
			exists.add(z);
			tgd.add(new Triple<>(pre + e.name, x, z));

			ed = new EmbeddedDependency(forall, exists, where, tgd, egd);
			ret.add(ed);	
		}
		
		
		for (Eq eq : eqs) {
			ret.add(EmbeddedDependency.eq(pre, eq.lhs, eq.rhs));
			ret.add(EmbeddedDependency.eq(pre, eq.rhs, eq.lhs));
		}
		
		return ret;
	}

	public static Signature sum(String c, String d, Signature C, Signature D) throws FQLException {
		//String n = c + "_plus_" + d; 
		List<String> o = new LinkedList<>();
		for (Node node : C.nodes) {
			o.add(c + "_" + node.string);
		}
		for (Node node : D.nodes) {
			o.add(d + "_" + node.string);
		}
		
		List<Triple<String, String, String>> a = new LinkedList<>();
		if (C.attrs.size() > 0 || D.attrs.size() > 0) {
			throw new FQLException("Cannot have attributes in signature sum");
		}
		List<Triple<String, String, String>> e = new LinkedList<>();
		for (Edge edge : C.edges) {
			e.add(new Triple<>(c + "_" + edge.name, c + "_" + edge.source.string, c + "_" + edge.target.string));
		}
		for (Edge edge : D.edges) {
			e.add(new Triple<>(d + "_" + edge.name, d + "_" + edge.source.string, d + "_" + edge.target.string));
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
		
		return new Signature(new HashMap<String, Type>(), /*n, */o, a, e, cc);
	}

	
}
