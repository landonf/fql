package fql.decl;

import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Paint;
import java.awt.Stroke;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
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
import fql.Unit;
import fql.cat.Arr;
import fql.cat.Denotation;
import fql.cat.FinCat;
import fql.gui.Viewable;
import fql.parse.PrettyPrinter;

/**
 * 
 * @author ryan
 * 
 *         Signatures.
 */
public class Signature implements Viewable<Signature> {

	
	public List<Node> nodes;
	public List<Edge> edges;
	public List<Attribute> attrs;

	public Set<Eq> eqs;
	public String name0;
	
	public Signature(String n, List<String> nodes_str, List<Triple<String, String, String>> attrs_str, List<Triple<String, String, String>> arrows,
			List<Pair<List<String>, List<String>>> equivs) throws FQLException {
		Set<Node> nodesA = new HashSet<>();
		Set<Edge> edgesA = new HashSet<>();
		Set<Attribute> attrsA = new HashSet<>();
		name0 = n;
		
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
				throw new FQLException("Missing node " + source + " in " + n);
			}
			Node target_node = lookup(target, nodesA);
			if (target_node == null) {
				throw new FQLException("Missing node " + target + " in " + n);
			}
			//nodesA.add(target_node);
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
				throw new FQLException("Missing node " + source + " in " + n);
			}
			
			
			Attribute a = new Attribute(name, source_node, tryParseType(target));
		
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

	public Signature(String n, List<Triple<String, String, String>> arrows,
			List<Pair<List<String>, List<String>>> equivs) throws FQLException {
		Set<Node> nodesA = new HashSet<>();
		Set<Edge> edgesA = new HashSet<>();
		Set<Attribute> attrsA = new HashSet<>();
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
				Attribute a = new Attribute(name, source_node, t);
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

	private Type tryParseType(String s) {
		if (s.equals("string")) {
			return new Varchar();
		} else if (s.equals("int")) {
			return new Int();
		}
		return null;
	}

	// for json
	public Signature(
			List<String> obs,
			List<Pair<Pair<String, String>, String>> arrows,
			List<Pair<List<Pair<Pair<String, String>, String>>, List<Pair<Pair<String, String>, String>>>> equivs)
			throws FQLException {
		Set<Node> nodesA = new HashSet<Node>();
		Set<Edge> edgesA = new HashSet<Edge>();
		attrs = new LinkedList<Attribute>();

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
//			if (source_node == null) {
//				throw new FQLException("Missing node " + source_node + " in " + name0);
//			}
			Node target_node = lookup(target, nodesA);
//			if (target_node == null) {
//				throw new FQLException("Missing node " + target_node + " in " + name0);
//			}
			
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

	public Signature(String s, List<Node> n, List<Edge> e, List<Attribute> a, Set<Eq> ee) {
		name0 = s;
		nodes = n;
		edges = e;
		eqs = ee;
		attrs = a;
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
		//System.out.println("Looking up " + string + " in " + nodes);
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
		throw new FQLException("Unknown edge: " + string);
	}

	public Node getNode(String string) throws FQLException {
		for (Node node : nodes) {
			if (node.string.equals(string)) {
				return node;
			}
		}
		throw new FQLException("Unknown node: " + string);
	}

	@Override
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
//		MouseListener[] listeners = eqsComponent.getMouseListeners();
//		for (MouseListener l : listeners) {
//			eqsComponent.removeMouseListener(l);
//		}
//		eqsComponent.setRowSelectionAllowed(false);
//		eqsComponent.setColumnSelectionAllowed(false);

		JPanel p = new JPanel(new GridLayout(2, 2));

		p.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

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
				BorderFactory.createEmptyBorder(2, 2, 2, 2), "Entities"));
		nodesTemp.add(new JScrollPane(nodesComponent));
//		nodesComponent.setRowSelectionAllowed(false);
//		nodesComponent.setColumnSelectionAllowed(false);
//		listeners = nodesComponent.getMouseListeners();
//		for (MouseListener l : listeners) {
//			nodesComponent.removeMouseListener(l);
//		}

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
				BorderFactory.createEmptyBorder(2, 2, 2, 2), "Foreign keys"));

//		esC.setRowSelectionAllowed(false);
//		esC.setColumnSelectionAllowed(false);
//		listeners = esC.getMouseListeners();
//		for (MouseListener l : listeners) {
//			esC.removeMouseListener(l);
//		}

		Object[][] as = new String[attrs.size()][3];
		 jj = 0;
		for (Attribute a : attrs) {
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

//		esC.setRowSelectionAllowed(false);
//		esC.setColumnSelectionAllowed(false);
//		listeners = esC.getMouseListeners();
//		for (MouseListener l : listeners) {
//			esC.removeMouseListener(l);
//		}
		p.add(nodesTemp);
		p.add(edgesTemp);
		p.add(attrsTemp);
		p.add(eqsTemp);

		return p;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer("schema " + name0 + " = {\n");

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
		for (Attribute a : attrs) {
			if (!first) {
				sb.append(",\n");
			}
			first = false;
			sb.append(a);
		}
		sb.append("\n ;\n");
		
		first = true;
		sb.append("edges\n");
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

	private boolean disconnected(Node n) {
		for (Edge e : edges) {
			if (e.source.equals(n) || e.target.equals(n)) {
				return false;
			}
		}
		for (Attribute a : attrs) {
			if (a.source.equals(n)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public JPanel text() {
//		String s = toString().replace(";", "\n\n;\n\n");
//		String[] t = s.split(",");
//		String ret = "";
//		for (String a : t) {
//			ret += (a.trim() + ",\n\n");
//		}
//		ret = ret.trim();
//		if (ret.endsWith(",")) {
//			ret = ret.substring(0, ret.length() - 1);
//		}

		JTextArea ta = new JTextArea(toString());
		JPanel tap = new JPanel(new GridLayout(1, 1));
		ta.setBorder(BorderFactory.createEmptyBorder());
		//
		tap.setBorder(BorderFactory.createEmptyBorder());
		ta.setWrapStyleWord(true);
		ta.setLineWrap(true);
		JScrollPane xxx = new JScrollPane(ta);
		//
		tap.add(xxx);

		return tap;

	}

	public Pair<String, String> getColumnNames(String s) throws FQLException {
//		if (s.contains(" ")) {
//			return new Pair<>("ID", "string");
//		}
		if (nodes.contains(new Node(s))) {
			return new Pair<String, String>("ID", "ID");
		}
		Attribute a = getAttr(s);
		if (a != null) {
			return new Pair<String, String>(a.source.string, a.target.toString());
		}
		Edge e = getEdge(s);
		return new Pair<String, String>(e.source.string, e.target.string);
	}

	public Attribute getAttr(String s) {
		for (Attribute a : attrs) {
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
		for (Attribute a : attrs) {
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
		//	List<List<String>> ret0 = bfs(l, n);
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
	
	public Pair<FinCat<Node, Path>, Fn<Path, Arr<Node, Path>>> toCategory2() throws FQLException {
		Denotation d = new Denotation(this);
		return d.toCategory();
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
			ret.addAll(bfs(new LinkedList<String>(r), e.target, i+1, stop));
		}
		return ret;
	}

	private Set<Edge> outEdges(Node n) {
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
		
		for (Attribute a : attrs) {
			g2.addVertex(a.name);
			g2.addEdge(a.name, a.source.string, a.name);
		}
		

		return g2;
	}

	public JPanel makeViewer(final Environment env) {
		Graph<String, String> g = build();
		if (g.getVertexCount() == 0) {
			return new JPanel();
		}
		return doView(env, g);
	}

	public JPanel doView(final Environment env, Graph<String, String> sgv) {
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
		vv.setPreferredSize(new Dimension(600, 400));
		// Setup up a new vertex to paint transformer...
		Transformer<String, Paint> vertexPaint = new Transformer<String, Paint>() {
			public Paint transform(String i) {
				if (!isAttribute(i)) {
					return env.colors.get(name0);
			} else {
				return UIManager.getColor ( "Panel.background" );
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
		BasicStroke.JOIN_MITER, 10.0f, dash, 10.0f );
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
		vv.getRenderContext().setEdgeLabelTransformer(new ToStringLabeller<String>()); /* {
			
			@Override
			public String transform(String t) {
				if (isAttribute(t)) {
					return getTypeLabel(t);
				}
				return t;
			}

		

			
		}); */
//				new ToStringLabeller<String>());
		// vv.getRenderer().getVertexRenderer().
		// vv.getRenderContext().setLabelOffset(20);
		// vv.getRenderer().getVertexLabelRenderer().setPosition(Position.CNTR);

		
		vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller<String>() {
			
			@Override
			public String transform(String t) {
				if (isAttribute(t)) {
					return getTypeLabel(t);
				}
				return t;
			}

		

			
		});
		
		return vv;
	}
	
	public String getTypeLabel(String t) {
		for (Attribute a : attrs) {
			if (a.name.equals(t)) {
				return a.target.toString();
			}
		}
		throw new RuntimeException();
	}
	
	
	public boolean isAttribute(String t) {
		for (Attribute a : attrs) {
			if (a.name.equals(t)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public JPanel pretty(final Environment env) throws FQLException {
		return makeViewer(env);
	}

	@Override
	public String type() {
		return "schema";
	}

	@Override
	/**
	 * Name-based equality
	 */
	public boolean equals(Object o) {
		if (!(o instanceof Signature)) {
			return false;
		}
		Signature s = (Signature) o;
		return (s.name0.equals(name0));
	}

	@Override
	public JPanel join() {
		return null;
	}

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

	public Instance terminal(String g) throws FQLException {
		return Instance.terminal(this, g);
	}

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

	JPanel den = null;

	@Override
	public JPanel denotation() throws FQLException {
		if (den == null) {
			den = new Denotation(this).view();
		}
		return den;
	}

	public Signature onlyObjects() {
		return new Signature("Obj(" + name0 + ")", nodes,
				new LinkedList<Edge>(), new LinkedList<Attribute>(), new HashSet<Eq>());
	}

	public boolean isNode(String s) {
		try {
			return getNode(s) != null;
		} catch (FQLException fql) {
			return false;
		}
	}

	public List<Attribute> attrsFor(Node n) {
		List<Attribute> a = new LinkedList<>();
		for (Attribute x : attrs) {
			if (x.source.equals(n)) {
				a.add(x);
			}
		}
		return a;
	}

	@Override
	public JPanel initial() throws FQLException {
		List<Pair<String, List<Pair<Object, Object>>>> b = new LinkedList<>();
		for (Node n : nodes) {
			List<Pair<Object, Object>> x = new LinkedList<>();
			b.add(new Pair<>(n.string, x));
		}
		for (Attribute n : attrs) {
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

}
