package fql;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Paint;
import java.awt.event.MouseListener;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;

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

/**
 * 
 * @author ryan
 *
 * Signatures.
 */
public class Signature implements Viewable<Signature> {

	List<Node> nodes;
	List<Edge> edges;
	Set<Eq> eqs;
	String name0;
	
	public Signature(String n, List<Triple<String, String, String>> arrows, List<Pair<List<String>, List<String>>> equivs) throws FQLException {
		Set<Node> nodesA = new HashSet<Node>();
		Set<Edge> edgesA = new HashSet<Edge>();
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
			
			//isolated node
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
			
			Node target_node = lookup(target, nodesA);
			if (target_node == null) {
				target_node = new Node(target);
			}
			nodesA.add(target_node);
			
			Edge e = new Edge(name, source_node, target_node);
			edgesA.add(e);		
		}
		
		nodes = new LinkedList<Node>(nodesA);
		edges = new LinkedList<Edge>(edgesA);
		
		eqs = new HashSet<Eq>();
		for (Pair<List<String>, List<String>> equiv : equivs) {
			Path lhs = new Path(this, equiv.first);
			Path rhs = new Path(this, equiv.second);
			if (!lhs.source.equals(rhs.source)) {
				throw new FQLException("source object mismatch " + lhs + " and " + rhs);
			}
			if (!lhs.target.equals(rhs.target)) {
				throw new FQLException("target object mismatch " + lhs + " and " + rhs);
			}
			Eq eq = new Eq(lhs, rhs);
			eqs.add(eq);
		}
		
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
		for(;;) {
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
		Arrays.sort(arr,  new Comparator<Object[]>()
                {
            public int compare(Object[] f1, Object[] f2)
            {
                return f1[0].toString().compareTo(f2[0].toString());
            }        
        });

		JTable eqsComponent = new JTable(arr, new Object[] { "lhs", "rhs"});
		MouseListener[] listeners = eqsComponent.getMouseListeners();
		for (MouseListener l : listeners)
		{
		    eqsComponent.removeMouseListener(l);
		}
		eqsComponent.setRowSelectionAllowed(false);
		eqsComponent.setColumnSelectionAllowed(false);
		
		JPanel p = new JPanel(new GridLayout(1,3));
		
		p.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		
		JPanel eqsTemp = new JPanel(new GridLayout(1,1));
		
		eqsTemp.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(2,2,2,2), "Equations"));
		eqsTemp.add(new JScrollPane(eqsComponent));
		
		Object[][] sn = new String[nodes.size()][1];
		int ii = 0;
		for (Node n : nodes) {
			sn[ii++][0] = n.string;
		}
		Arrays.sort(sn,  new Comparator<Object[]>()
                {
            public int compare(Object[] f1, Object[] f2)
            {
                return f1[0].toString().compareTo(f2[0].toString());
            }        
        });

		JTable nodesComponent = new JTable(sn, new String[] { "Name" });
		JPanel nodesTemp = new JPanel(new GridLayout(1,1));
		nodesTemp.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(2,2,2,2), "Nodes"));
		nodesTemp.add(new JScrollPane(nodesComponent));
		nodesComponent.setRowSelectionAllowed(false);
		nodesComponent.setColumnSelectionAllowed(false);
		listeners = nodesComponent.getMouseListeners();
		for (MouseListener l : listeners)
		{
			nodesComponent.removeMouseListener(l);
		}
		
		Object[][] es = new String[edges.size()][3];
		int jj = 0;
		for (Edge eq : edges) {
			es[jj][0] = eq.name;
			es[jj][1] = eq.source.string;
			es[jj][2] = eq.target.string;
			jj++;
		}
		Arrays.sort(es,  new Comparator<Object[]>()
                {
            public int compare(Object[] f1, Object[] f2)
            {
                return f1[0].toString().compareTo(f2[0].toString());
            }        
        });

		JTable esC = new JTable(es, new String[] { "Name", "Source", "Target"});
		JPanel edgesTemp = new JPanel(new GridLayout(1,1));
		edgesTemp.add(new JScrollPane(esC));
		edgesTemp.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(2,2,2,2), "Edges"));

		esC.setRowSelectionAllowed(false);
		esC.setColumnSelectionAllowed(false);
		listeners = esC.getMouseListeners();
		for (MouseListener l : listeners)
		{
		    esC.removeMouseListener(l);
		}
			
		p.add(nodesTemp);
		p.add(edgesTemp);
		p.add(eqsTemp);
		
		return p;
	}



	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer("{ ");
		
		boolean first = true;
		for (Edge e : edges) {
			if (!first) {
				sb.append(", ");
			}
			first = false;
			sb.append(e);
		}
		
//		first = true;
		for (Node n : nodes) {
			if (!first) {
				sb.append(", ");
			}
			if (!disconnected(n)) {
				continue;
			}
			first = false;
			sb.append(n.string);
		}
		
		sb.append(" ; ");
		
		first = true;
		for (Eq eq : eqs) {
			if (!first) {
				sb.append(",");
			}
			first = false;
			sb.append(eq);
		}
		sb.append(" }");
		return sb.toString();
	}

	private boolean disconnected(Node n) {
		for (Edge e : edges) {
			if (e.source.equals(n) || e.target.equals(n)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public String plan() {
		// TODO plan signature
		return "todo";
	}
	
	@Override
	public JPanel text() {
			String s = toString().replace(";", "\n\n;\n\n");
			String[] t = s.split(",");
			String ret = "";
			for (String a : t) {
			  ret += (a.trim() + ",\n\n");
			}
			
			JTextArea ta = new JTextArea(ret);
			JPanel tap = new JPanel(new GridLayout(1,1));
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
		if (nodes.contains(new Node (s))) {
			return new Pair<String, String>("ID", "ID");
		}
		Edge e = getEdge(s);
		return new Pair<String, String>(e.source.string, e.target.string);		
	}

	@Override
	public boolean equals0(Signature view2) {
//		if (Equality.which.equals(Equality.syntactic)) {
			return equals(view2);
//		}
//		return false;
		//TODO: (DEFER) eq signature
	}

	@Override
	public boolean iso(Signature view) {
		// TODO (DEFER) iso signature
		return false;
	}

	@Override
	public String isos(Signature view) {
		// TODO (DEFER) isos signature
		return "todo - isos signature";
	}

	@Override
	public String homos(Signature view) {
		// TODO (DEFER) morphs signature
		return "todo - morphs signature";
	}

	public Set<String> all() {
		Set<String> ret = new HashSet<String>();
		for (Node n : nodes) {
			ret.add(n.string);
		}
		for (Edge e : edges) {
			ret.add(e.name);
		}
		return ret;
	}
	

	public boolean contains(String s) {
		return all().contains(s);
	}

	/**
	 * Converts a signature to a category.
	 * @return the category, and some isomorphisms
	 * @throws FQLException
	 */
	public Pair<FinCat<String, List<List<String>>>, Pair<Pair<Map<String, String>, Map<String, String>>, Pair<Map<String, Arr<String, List<List<String>>>>, Map<Arr<String, List<List<String>>>, String>>>>
	toCategory() throws FQLException {
		if (!acyclic()) {
			throw new FQLException("Not acyclic: " + this);
		}
		
		List<List<String>> paths = new LinkedList<List<String>>();
		
		for (Node n : nodes) {
			LinkedList<String> l = new LinkedList<String>();
			l.add(n.string);
			paths.add(l);
			List<List<String>> ret0 = bfs(l, n);
			paths.addAll(ret0);
		}

		List<List<List<String>>> eqcs = new LinkedList<List<List<String>>>();
		for (List<String> path : paths) {
			List<List<String>> eqc = new LinkedList<List<String>>();
			eqc.add(path);
			eqcs.add(eqc);
		}
		
		merge(eqcs);
		
		List<String> objects = new LinkedList<String>();
		for (Node n : nodes) {
			objects.add(n.string);
		}
		
		List<Arr<String, List<List<String>>>> arrows = new LinkedList<>();
		for (List<List<String>> eqc : eqcs) {
			arrows.add(new Arr<>(eqc, srcOf(eqc), dstOf(eqc)));
		}
		
		Map<Pair<Arr<String, List<List<String>>>, Arr<String,List<List<String>>>>, Arr<String,List<List<String>>>> composition = new HashMap<>();
		for (Arr<String, List<List<String>>> a : arrows) {
			for (Arr<String, List<List<String>>> b : arrows) {
				if (a.dst.equals(b.src)) {
					Arr<String, List<List<String>>> c = new Arr<>(compose(a.arr,b.arr), a.src, b.dst);
					Arr<String, List<List<String>>> c0 = new Arr<>(subsetLookup(c.arr, eqcs), a.src, b.dst);
					if (!arrows.contains(c0)) {
						throw new RuntimeException("Not closed under composition. Computed: " + c + " ; found " + c0 + " category is " + eqcs);
					}
					composition.put(new Pair<>(a,b), c0);
				}
			}
		}
		
		Map<String, Arr<String, List<List<String>>>> identities = new HashMap<>();
		for (Node n : nodes) {
			List<String> x = new LinkedList<String>();
			x.add(n.string);
			for (List<List<String>> eqc : eqcs) {
				if (eqc.contains(x)) {
					identities.put(n.string, new Arr<>(eqc,srcOf(eqc), dstOf(eqc)));
				}
			}
		}
		
		Map<String, String> objIso1 = new HashMap<>();
		Map<String, String> objIso2 = new HashMap<>();
		Map<String, Arr<String, List<List<String>>>> arrowIso1 = new HashMap<>();
		Map<Arr<String,List<List<String>>>, String> arrowIso2 = new HashMap<>();
		
		for  (Node n : nodes) {
			objIso1.put(n.string, n.string);
			objIso2.put(n.string, n.string);
		}
		for (Edge e : edges) {
			List<String> tofind = new LinkedList<>();
			tofind.add(e.source.string);
			tofind.add(e.name);
			for (List<List<String>> eqc : eqcs) {
				if (eqc.contains(tofind)) {
					arrowIso1.put(e.name, new Arr<>(eqc, srcOf(eqc), dstOf(eqc)));
					arrowIso2.put(new Arr<>(eqc, srcOf(eqc), dstOf(eqc)), e.name);
				}
			}
		}
		
		Pair<Map<String, String>, Map<String, String>> x = new Pair<>(objIso1, objIso2);
		Pair<Map<String, Arr<String, List<List<String>>>>, Map<Arr<String, List<List<String>>>, String>> y = new Pair<>(arrowIso1, arrowIso2);
		
		Pair<Pair<Map<String, String>, Map<String, String>>, Pair<Map<String, Arr<String, List<List<String>>>>, Map<Arr<String, List<List<String>>>, String>>> ret2 = new Pair<>(x, y);
		
		FinCat<String, List<List<String>>> ret = new FinCat<>(objects, arrows, composition, identities);
		return new Pair<>(ret, ret2);
	}

	private String dstOf(List<List<String>> eqc) throws FQLException {
		List<String> x = eqc.get(0);
		return new Path(this, x).target.string;
	}

	private String srcOf(List<List<String>> eqc) throws FQLException {
		List<String> x = eqc.get(0);
		return new Path(this, x).source.string;
	}

	private List<List<String>> subsetLookup(List<List<String>> c,
			List<List<List<String>>> eqcs) {
		for (List<List<String>> eqc : eqcs) {
			if (subset(c, eqc)) {
				return eqc;
			}
		}
		throw new RuntimeException("Cannot find " + c + " inside any equiv class " + eqcs);
	}

	private boolean subset(List<List<String>> c, List<List<String>> eqc) {
		for (List<String> c0 : c) {
			if (!eqc.contains(c0)) {
				return false;
			}
		}
		return true;
	}

	private void merge(List<List<List<String>>> eqcs) {
		for (Eq eq : eqs) {
			List<List<String>> lhs = null, rhs = null;
			for (List<List<String>> eqc : eqcs) {
				if (eqc.contains(pathToList(eq.lhs))) {
					lhs = eqc;
				}
				if (eqc.contains(pathToList(eq.rhs))) {
					rhs = eqc;
				}
			}
			if (lhs == null) {
				throw new RuntimeException("Missing lhs equiv class " + eq);
			}
			if (rhs == null) {
				throw new RuntimeException("Missing rhs equiv class " + eq);
			}
			if (!lhs.equals(rhs)) {
				eqcs.remove(lhs);
				eqcs.remove(rhs);
				List<List<String>> n = new LinkedList<List<String>>();
				n.addAll(lhs);
				n.addAll(rhs);
				eqcs.add(n);
				merge(eqcs);
				return;
			}
		}
	}

	public static List<String> pathToList(Path p) {
		List<String> ret = new LinkedList<String>();
		ret.add(p.source.string);
		for (Edge e : p.path) {
			ret.add(e.name);
		}
		return ret;
	}

	private List<List<String>> compose(List<List<String>> a,
			List<List<String>> b) {
		List<List<String>> ret = new LinkedList<List<String>>();
		
		for (List<String> path1 : a) {
			for (List<String> path2 : b) {
				List<String> path3 = new LinkedList<String>();
				path3.addAll(path1);
				List<String> path2X = new LinkedList<String>(path2);
				path2X.remove(0);
				path3.addAll(path2X);
				ret.add(path3);
			}
		}
		return ret;
	}

	private List<List<String>> bfs(LinkedList<String> l, Node n) {
		Set<Edge> outs = outEdges(n);
		List<List<String>> ret = new LinkedList<List<String>>();
		for (Edge e : outs) {
			LinkedList<String> r = new LinkedList<String>(l);
			r.add(e.name);
			ret.add(r);
			ret.addAll(bfs(new LinkedList<String>(r), e.target));
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
	

	public Graph<String,String> build() {
		// Graph<V, E> where V is the type of the vertices
	
		Graph<String, String> g2 = new DirectedSparseMultigraph<String, String>();
		for (Node n : nodes) {
			g2.addVertex(n.string);
		}
		
		for (Edge e : edges) {
			g2.addEdge(e.name, e.source.string, e.target.string);
		}
		
		return g2;
	}

	public JPanel makeViewer() {
		Graph<String,String> g = build();
		return doView(g);
	}


	public  JPanel doView(Graph<String,String> sgv) {
		// Layout<V, E>, BasicVisualizationServer<V,E>
	//	Layout<String, String> layout = new FRLayout(sgv);
		Layout<String, String> layout = new ISOMLayout<String,String>(sgv);
		//		Layout<String, String> layout = new CircleLayout(sgv);
		layout.setSize(new Dimension(600, 400));
	//	BasicVisualizationServer<String, String> vv = new BasicVisualizationServer<String, String>(
		//		layout);
		VisualizationViewer<String, String> vv = new VisualizationViewer<String, String>(layout);
		vv.setPreferredSize(new Dimension(600, 400));
		// Setup up a new vertex to paint transformer...
		Transformer<String, Paint> vertexPaint = new Transformer<String, Paint>() {
			public Paint transform(String i) {
				return Environment.colors.get(name0);
			}
		};
		DefaultModalGraphMouse<String, String> gm = new DefaultModalGraphMouse<>();
        gm.setMode(ModalGraphMouse.Mode.TRANSFORMING);
        vv.setGraphMouse(gm);
        gm.setMode(Mode.PICKING);
		// Set up a new stroke Transformer for the edges
	//	float dash[] = { 10.0f };
//		final Stroke edgeStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
//				BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f);
//		Transformer<String, Stroke> edgeStrokeTransformer = new Transformer<String, Stroke>() {
//			public Stroke transform(String s) {
//				return edgeStroke;
//			}
//		};
		vv.getRenderContext().setVertexFillPaintTransformer(vertexPaint);
		//vv.getRenderContext().setEdgeStrokeTransformer(edgeStrokeTransformer);
		vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller<String>());
		vv.getRenderContext().setEdgeLabelTransformer(new ToStringLabeller<String>());
		//vv.getRenderer().getVertexRenderer().
	//	vv.getRenderContext().setLabelOffset(20);
	//	vv.getRenderer().getVertexLabelRenderer().setPosition(Position.CNTR);
		
		return vv;
	}
	
	@Override
	public JPanel pretty() throws FQLException {
		return makeViewer();
	}

	@Override
	public String type() {
		return "schema";
	}
	
	@Override
	/**
	 * Equality of denoted categories.
	 */
	public boolean equals(Object o) {
		if (!(o instanceof Signature)) {
			return false;
		}
		Signature s = (Signature) o;
		try {
			return (toCategory().equals(s.toCategory()));
		} catch (FQLException e) {
			throw new RuntimeException(e);
		}
	}
	

}
