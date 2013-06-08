package fql.decl;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Paint;
import java.awt.Stroke;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.UIManager;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.algorithms.layout.FRLayout;
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
import fql.cat.Arr;
import fql.cat.FinCat;
import fql.cat.FinFunctor;
import fql.gui.Viewable;
import fql.parse.FqlTokenizer;
import fql.parse.JSONParsers.JSONMappingParser;
import fql.parse.Jsonable;
import fql.sql.PSM;
import fql.sql.PSMGen;
import fql.sql.RA;

/**
 * 
 * @author ryan
 *
 * Implementation of signature morphisms
 */
public class Mapping implements Viewable<Mapping>, Jsonable {
	
	boolean ALLOW_WF_CHECK = true;
	
	public void validate() throws FQLException {
		for (Attribute a : source.attrs) {
			Attribute b = am.get(a);
			if (b == null) {
				throw new FQLException("Mapping " + name + " does not map attribute " + a);
			}
			if (!a.target.equals(b.target)) {
				throw new FQLException("Mapping " + name + " does not preserve typing on " + a + " and " + b);
			}
		}
		
		//should be check by knuth-bendix
		
		if (!DEBUG.ALLOW_INFINITES && !name.equals("")) {
			
			Triple<FinFunctor<Node, Path, Node, Path>, Pair<FinCat<Node, Path>, Fn<Path, Arr<Node, Path>>>, Pair<FinCat<Node, Path>, Fn<Path, Arr<Node, Path>>>> zzz = toFunctor2();
	
			for (Eq x : source.eqs) {
				appy(x.lhs);
				
				if (!zzz.third.second.of(appy(x.lhs)).equals(zzz.third.second.of(appy(x.rhs)))) {
					throw new FQLException("On " + name + ", equivalence " + x + 
							" not respected on \n\n" + x + "\n and \n" + appy(x.lhs) + "\n\n" + appy(x.rhs)
							);
							
				}
			}			
		}
		 
	}
	
	public Map<Node, Node> nm = new HashMap<>();
	public Map<Edge, Path> em = new HashMap<>();
	public Map<Attribute, Attribute> am = new HashMap<>();
	public Signature source;
	public Signature target;
	public String name;
	boolean isId = false;
		
	public Mapping(String name, Environment env, MappingDecl md) throws FQLException {
		this.name = name;
		switch (md.kind) {
//		case COMPOSE : 
//			Mapping m1 = env.getMapping(md.source);
//			Mapping m2 = env.getMapping(md.target);
//		
//			if (!m2.target.equals(m1.source)) {
//				throw new FQLException("Ill-typed: " + md);
//			}
//			this.source = m2.source;
//			this.target = m1.target;
//			for (Node k : m1.source.nodes) {
//				Node v = m1.nm.get(k);
//				nm.put(k, m2.nm.get(v));
//			}
//			for (Edge k : m1.source.edges) {
//				Path v = m1.em.get(k);
//				Path p0 = expand(v, m2.nm, m2.em);
//				em.put(k, p0);
//			}
//			
//			
//			break;
		case ID : 
			Signature s = env.getSchema(md.schema);
			if (!(md.schema.equals(md.source) && md.schema.equals(md.target))) {
				throw new FQLException("Bad identity mapping : " + md.name);
			}
			identity(env, s);
			break;
		case MORPHISM :
//			Pair<List<Pair<String, String>>, List<Pair<String, String>>> xxx = filter(md.objs);
			initialize(env.getSchema(md.source), env.getSchema(md.target), md.objs, md.arrows);
			break;
		}
		validate();
	}
	
	private Pair<List<Pair<String, String>>, List<Pair<String, String>>> filter(
			List<Pair<String, String>> objs) throws FQLException {
		List<Pair<String, String>> ret = new LinkedList<>();
		List<Pair<String, String>> ret2 = new LinkedList<>();
		
		for (Pair<String, String> p : objs) {
			if (source.isAttribute(p.first) && target.isAttribute(p.second)) {
				ret.add(p);
			} else if (source.isNode(p.first) && target.isNode(p.second)) {
				ret2.add(p);
			} else {
				throw new FQLException("Bad mapping: " + p);
			}
		}

		return new Pair<>(ret2, ret);
	}

	private Path expand(Path v, Map<Node, Node> nm2, Map<Edge, Path> em2) {
		Node newhead = nm2.get(v.source);
		Node newtarget = nm2.get(v.target);
		List<Edge> newedges = new LinkedList<Edge>();
		for (Edge e : v.path) {
			Path p = em2.get(e);
			newedges.addAll(p.path);
			newtarget = p.target;
		}
		return new Path(newhead, newtarget, newedges);
	}

	public Mapping(Environment env, Signature s) throws FQLException {
		identity(env, s);
		validate();
	}
	
	/**
	 * Constructs a new mapping.  
	 */
	public Mapping(String name, Signature source, Signature target,
			List<Pair<String, String>> objs,
			List<Pair<String, List<String>>> arrows) throws FQLException {
		this.name = name;
		initialize(source, target, objs, arrows);
		validate();
	}

	public Mapping(
			Signature source,
			Signature target,
			List<Pair<String, String>> objs,
			List<Pair<Pair<Pair<String, String>, String>, List<Pair<Pair<String, String>, String>>>> arrows) throws FQLException {
		this.source = source;
		this.target = target;
		for (Pair<String, String> p : objs) {
			Node sn = this.source.getNode(p.first);
			Node tn = this.target.getNode(p.second);
			nm.put(sn, tn);
		}
		for (Pair<Pair<Pair<String, String>, String>, List<Pair<Pair<String, String>, String>>> arrow : arrows) {
			Edge e = this.source.getEdge(arrow.first.second);
			Node n = nm.get(e.source);
			Path p = new Path(this.target, arrow.second, n);
			em.put(e, p);
		}
		for (Node n : this.source.nodes) {
			if (nm.get(n) == null) {
				throw new FQLException("Missing node mapping from " + n + " in " + name + "\n" + this);
			}
		}
		for (Edge e : this.source.edges) {
			if (em.get(e) == null) {
				throw new FQLException("Missing edge mapping from " + e + " in " + name);
			}
		}

		validate();
	}

	/**
	 * Does most of the work of the constructor.
	 */
	private void initialize(Signature source, Signature target,
			List<Pair<String, String>> objs,
			List<Pair<String, List<String>>> arrows
			) throws FQLException {
		this.source = source;
		this.target = target;
		
		Pair<List<Pair<String, String>>, List<Pair<String, String>>> s = filter(objs);
		objs = s.first;
		List<Pair<String, String>> attrs = s.second;
		for (Pair<String, String> p : objs) {
			Node sn = this.source.getNode(p.first);
			Node tn = this.target.getNode(p.second);
			nm.put(sn, tn);
		}
		for (Pair<String, List<String>> arrow : arrows) {
			Edge e = this.source.getEdge(arrow.first);
			Path p = new Path(this.target, arrow.second);
			em.put(e, p);
		}
		for (Pair<String, String> a : attrs) {
			am.put(source.getAttr(a.first), target.getAttr(a.second));
		}
		for (Node n : this.source.nodes) {
			if (nm.get(n) == null) {
				throw new FQLException("Missing node mapping from " + n + " in " + name + "\n" + this);
			}
		}
		for (Edge e : this.source.edges) {
			if (em.get(e) == null) {
				throw new FQLException("Missing edge mapping from " + e + " in " + name);
			}
		}
		for (Attribute a : this.source.attrs) {
			if (am.get(a) == null) {
				throw new FQLException("Missing attribute mapping from " + a + " in " + name);
			}
		}
		//TODO: check compatible types
	}
	
	/**
	 *  Constructs an identity mapping
	 */
	private void identity(Environment env, Signature s)  throws FQLException {
		for (Node n : s.nodes) {
			nm.put(n, n);
		}
		for (Edge e : s.edges) {
			em.put(e, new Path(s, e));
		}
		for (Attribute a : s.attrs) {
			am.put(a, a);
		}
		this.source = s;
		this.target = s;
		isId = true;
	}

	@Override
	/**
	 * The viewer for mappings.
	 */
	public JPanel view() {
		Object[][] arr = new Object[nm.size()][2];
		int i = 0;
		for (Entry<Node, Node> eq : nm.entrySet()) {
			arr[i][0] = eq.getKey();
			arr[i][1] = eq.getValue();
		i++;
		}
		Arrays.sort(arr,  new Comparator<Object[]>()
                {
            public int compare(Object[] f1, Object[] f2)
            {
                return f1[0].toString().compareTo(f2[0].toString());
            }        
        });
		
		JTable nmC = new JTable(arr, new Object[] { "Source node in " + source.name0 , "Target node in " + target.name0});
		
		Object[][] arr2 = new Object[em.size()][2];
		int i2 = 0;
		for (Entry<Edge, Path> eq : em.entrySet()) {
			arr2[i2][0] = eq.getKey();
			arr2[i2][1] = eq.getValue().toLong();
			i2++;
		}
		Arrays.sort(arr2,  new Comparator<Object[]>()
                {
            public int compare(Object[] f1, Object[] f2)
            {
                return f1[0].toString().compareTo(f2[0].toString());
            }        
        });


		JTable emC = new JTable(arr2, new Object[] { "Source edge in " + source.name0 , "Target path in " + target.name0});

		
		Object[][] arr3 = new Object[am.size()][2];
		int i3 = 0;
		for (Entry<Attribute, Attribute> eq : am.entrySet()) {
			arr3[i3][0] = eq.getKey();
			arr3[i3][1] = eq.getValue();
			i3++;
		}
		Arrays.sort(arr3,  new Comparator<Object[]>()
                {
            public int compare(Object[] f1, Object[] f2)
            {
                return f1[0].toString().compareTo(f2[0].toString());
            }        
        });
		JTable amC = new JTable(arr3, new Object[] { "Source attribute in " + source.name0 , "Target attribute in " + target.name0});

		
		
		JPanel p = new JPanel(new GridLayout(2,2));
		
		JScrollPane q1 = new JScrollPane(nmC);
		JScrollPane q2 = new JScrollPane(emC);
		JScrollPane q3 = new JScrollPane(amC);
		
		JPanel j1 = new JPanel(new GridLayout(1,1));
		j1.add(q1);
		j1.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), "Node mapping"));		
		p.add(j1);

		JPanel j2 = new JPanel(new GridLayout(1,1));
		j2.add(q2);
		j2.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), "Edge mapping"));
		p.add(j2);
		
		JPanel j3 = new JPanel(new GridLayout(1,1));
		j3.add(q3);
		j3.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), "Attribute mapping"));
		p.add(j3);
				
		return p;
	}

	static String printNicely(Map<String, RA> r) {
		String s = "";
		for (String k : r.keySet()) {
			RA e = r.get(k);
			s += (k + " = " + e + "\n\n");
		}
		return s;
	}

	@Override
	/**
	 * Text view for mappings.
	 */
	public JPanel text() {
		
//		String[] t = toString().split(";");
//		String ret = "";
//		for (String a : t) {
//		  ret += (a.trim());
//		}
		
		JPanel tap = new JPanel(new GridLayout(2,2));

		JTextArea ta = new JTextArea(toString());
		ta.setWrapStyleWord(true);
		ta.setLineWrap(true);
		JScrollPane xxx = new JScrollPane(ta);		
		JPanel p = new JPanel(new GridLayout(1,1));
		p.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), "Mapping " + name + " : " + source.name0 + " -> " + target.name0 ));
		p.add(xxx);
		tap.add(p);
		
		String delta = "";
		try {
			delta = printNicely(PSMGen.delta(this, "input", "output"));
		} catch (Exception e) {
			delta = e.toString();
		}
		
		String sigma = "";
		try {
			sigma = printNicely(PSMGen.sigma(this, "input", "output"));
		} catch (Exception e) {
			sigma = e.toString();
		}
		
		String pi = "";
		try {
			pi = printNicely(PSMGen.pi(this, "input", "output"));
		} catch (Exception e) {
			pi = e.toString();
		}

		JTextArea ta2 = new JTextArea(delta);
		ta2.setWrapStyleWord(true);
		ta2.setLineWrap(true);
		JScrollPane xxx2 = new JScrollPane(ta2);		
		JPanel p2 = new JPanel(new GridLayout(1,1));
		p2.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), "Delta " + name + " : " + target.name0 + " -> " + source.name0));
		p2.add(xxx2);
		tap.add(p2);
		

		JTextArea ta3 = new JTextArea(pi);
		ta3.setWrapStyleWord(true);
		ta3.setLineWrap(true);
		JScrollPane xxx3 = new JScrollPane(ta3);		
		JPanel p3 = new JPanel(new GridLayout(1,1));
		p3.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), "Pi "  + name + " : " + source.name0 + " -> " + target.name0));
		p3.add(xxx3);
		tap.add(p3);
		
		JTextArea ta4 = new JTextArea(sigma);
		ta4.setWrapStyleWord(true);
		ta4.setLineWrap(true);
		JScrollPane xxx4 = new JScrollPane(ta4);		
		JPanel p4 = new JPanel(new GridLayout(1,1));
		p4.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), "Sigma " + name + " : " + source.name0 + " -> " + target.name0));
		p4.add(xxx4);
		tap.add(p4);

		return tap;
	}

	
	

	static String printNicely(List<PSM> delta) {
		String ret = "";
		for (PSM p : delta) {
			ret += p + "\n\n";
		}
		return ret;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer("mapping " + name + " : " + source.name0 + " -> " + target.name0 + " = ");
		if (isId) {
			sb.append("id " + source.name0);
			return sb.toString();
		} else {
			sb.append("{\n");
		}
		boolean first = true;
		for (Node k : nm.keySet()) {
			Node v = nm.get(k);
			if (!first) {
				sb.append(",\n");
			}
			first = false;
			//sb.append("(");
			sb.append(k);
			sb.append(" -> ");
			sb.append(v);
			//sb.append(")");
		}
		for (Attribute k : am.keySet()) {
			Attribute v = am.get(k);
			if (!first) {
				sb.append(",\n");
			}
			first = false;
		//	sb.append("(");
			sb.append(k.name);
			sb.append(" -> ");
			sb.append(v.name);
		//	sb.append(")");
		}
		sb.append("\n ;\n");
		first = true;
		for (Edge k : em.keySet()) {
			Path v = em.get(k);
			if (!first) {
				sb.append(",\n");
			}
			first = false;
			//sb.append("(");
			sb.append(k);
			sb.append(" -> ");
			sb.append(v);
			//sb.append(")");
		}
		sb.append("\n}");
		return sb.toString();
	}

	/**
	 * Applies to a path
	 */
	public Path appy(Path path) {
		List<Edge> r = new LinkedList<Edge>();
		for (Edge e : path.path) {
			Path p = em.get(e);
			r.addAll(p.path);
		}
		
		return new Path(nm.get(path.source), nm.get(path.target), r);
	}
	
	/**
	 * Converts a mapping to a functor.
	 * @return the functor, and some isomorphisms
	 */
//	public Triple<FinFunctor<String, List<List<String>>, String, List<List<String>>>, Pair<FinCat<String, List<List<String>>>, Pair<Pair<Map<String, String>, Map<String, String>>, Pair<Map<String, Arr<String, List<List<String>>>>, Map<Arr<String, List<List<String>>>, String>>>>, Pair<FinCat<String, List<List<String>>>, Pair<Pair<Map<String, String>, Map<String, String>>, Pair<Map<String, Arr<String, List<List<String>>>>, Map<Arr<String, List<List<String>>>, String>>>>> toFunctor() throws FQLException {
//		HashMap<String, String> objMapping = new HashMap<String, String>();
//		HashMap<Arr<String,List<List<String>>>, Arr<String,List<List<String>>>> arrowMapping = new HashMap<>();
//		
//		for (Entry<Node, Node> e : nm.entrySet()) {
//			objMapping.put(e.getKey().string, e.getValue().string);
//		}
//		
//		Pair<FinCat<String, List<List<String>>>, Pair<Pair<Map<String, String>, Map<String, String>>, Pair<Map<String, Arr<String, List<List<String>>>>, Map<Arr<String, List<List<String>>>, String>>>> srcCat0 = source.toCategory();
//		Pair<FinCat<String, List<List<String>>>, Pair<Pair<Map<String, String>, Map<String, String>>, Pair<Map<String, Arr<String, List<List<String>>>>, Map<Arr<String, List<List<String>>>, String>>>> dstCat0 = target.toCategory();
//		
//		FinCat<String, List<List<String>>> srcCat = srcCat0.first;
//		FinCat<String, List<List<String>>> dstCat = dstCat0.first;
//		
//		for (Arr<String, List<List<String>>> arroweqc : srcCat.arrows) {
//			List<String> arrow = arroweqc.arr.get(0);
//			
//			List<String> mapped = apply(arrow);
//			Arr<String, List<List<String>>> mappedeqc = findeqc(dstCat, mapped);
//			arrowMapping.put(arroweqc, mappedeqc);
//		}
//		
//		return new Triple<>(new FinFunctor<>(objMapping, arrowMapping, srcCat, dstCat), srcCat0, dstCat0);
//	}
	
	public Triple<FinFunctor<Node, Path, Node, Path>, Pair<FinCat<Node, Path>, Fn<Path, Arr<Node, Path>>>, Pair<FinCat<Node, Path>, Fn<Path, Arr<Node, Path>>>> toFunctor2() throws FQLException {
	HashMap<Node, Node> objMapping = new HashMap<>();
	HashMap<Arr<Node,Path>, Arr<Node, Path>> arrowMapping = new HashMap<>();
	
	for (Entry<Node, Node> e : nm.entrySet()) {
		objMapping.put(e.getKey(), e.getValue());
	}
	
	Pair<FinCat<Node, Path>, Fn<Path, Arr<Node, Path>>> srcCat0 = source.toCategory2();
	Pair<FinCat<Node, Path>, Fn<Path, Arr<Node, Path>>> dstCat0 = target.toCategory2();
	
	FinCat<Node, Path> srcCat = srcCat0.first;
	FinCat<Node, Path> dstCat = dstCat0.first;
	
	for (Arr<Node, Path> arroweqc : srcCat.arrows) {
		Path arrow = arroweqc.arr;
		
		Path mapped = appy(arrow);
		arrowMapping.put(new Arr<>(arrow, arrow.source, arrow.target), dstCat0.second.of(mapped));
	}
	
		return new Triple<>(new FinFunctor<>(objMapping, arrowMapping, srcCat,
				dstCat), srcCat0, dstCat0);
}

	
//	private Arr<String, List<List<String>>> findeqc(
//			FinCat<String, List<List<String>>> cat, List<String> path) {
//		for (Arr<String, List<List<String>>> eqc : cat.arrows) {
//			if (eqc.arr.contains(path)) {
//				return eqc;
//			}
//		}
//		throw new RuntimeException("No equivalence class for " + path + " in " + cat);
//	}

//	private List<String> apply(List<String> arrow) {
//		List<String> ret = new LinkedList<String>();
//		for (Entry<Node, Node> e : nm.entrySet()) {
//			if (e.getKey().string.equals(arrow.get(0))) {
//				ret.add(e.getValue().string);
//				break;
//			}
//		}
//		
//		for (int i = 1; i < arrow.size(); i++) {
//			String s = arrow.get(i);
//			for (Entry<Edge, Path> e : em.entrySet()) {
//				if (e.getKey().name.equals(s)) {
//					List<String> x = e.getValue().asList();
//					x.remove(0);
//					ret.addAll(x);
//				}
//			}
//		}
//		return ret;
//	}
	
	
	
	
	public Map<String, Set<Pair<Object, Object>>> evalDelta(Instance theinstance) throws FQLException {
		
		//System.out.println(FDM.delta(this.toFunctor(), theinstance.toFunctor()));
		
		Map<String, RA> x = RA.delta(this);
		Map<String, Set<Object[]>> i0 = Query.convert0(theinstance);
		Map<String, Set<Object[]>> i1 = RA.eval0(x, i0);
		return Query.convert(i1);
	}
	

	public Map<String, Set<Pair<Object, Object>>> evalSigma(Instance theinstance) throws FQLException {
		Map<String, RA> x = RA.sigma(this);
		Map<String, Set<Object[]>> i0 = Query.convert0(theinstance);
		Map<String, Set<Object[]>> i1 = RA.eval0(x, i0);
		
		//System.out.println(FDM.sigma(this.toFunctor(), theinstance.toFunctor()));

		return Query.convert(i1);
	}
	
	public Map<String, Set<Pair<Object, Object>>> evalPi(Instance theinstance) throws FQLException {
		

		Map<String, RA> x = RA.pi(this);
		Map<String, Set<Object[]>> i0 = Query.convert0(theinstance);
		Map<String, Set<Object[]>> i1 = RA.eval0(x, i0);
		
		//System.out.println(FDM.grothendieck(FDM.pi(this.toFunctor().first, theinstance.toFunctor())));

		return Query.convert(i1);
	}
	
	@Override
	public JPanel pretty(final Environment env) throws FQLException {
			Graph<String, String> g = build();
			if (g.getVertexCount() == 0) {
				return new JPanel();
			}
			return doView(env, g);
	}

	@Override
	public String type() {
		return "mapping";
	}
	

	public Graph<String,String> build() {
		// Graph<V, E> where V is the type of the vertices
	
		Graph<String, String> g2 = new DirectedSparseMultigraph<String, String>();
		for (Node n : source.nodes) {
			g2.addVertex("@source" + "." + n.string);
		}
		for (Edge e : source.edges) {
			g2.addEdge("@source" + "." + e.name, "@source" + "." + e.source.string, "@source" + "." + e.target.string);
		}
		for (Attribute a : source.attrs) {
			g2.addVertex("@source" + "." + a.name);
			g2.addEdge("@source" + "." + a.name, "@source" + "." + a.source.string, "@source" + "." + a.name);
		}
		

		for (Node n : target.nodes) {
			g2.addVertex("@target" + "." + n.string);
		}	
		for (Edge e : target.edges) {
			g2.addEdge("@target" + "." + e.name, "@target" + "." + e.source.string, "@target" + "." + e.target.string);
		}
		for (Attribute a : target.attrs) {
			g2.addVertex("@target" + "." + a.name);
			g2.addEdge("@target" + "." + a.name, "@target" + "." + a.source.string, "@target" + "." + a.name);
		}

	
		for (Node n : nm.keySet()) {
			Node m = nm.get(n);
			g2.addEdge(n.string + " " + m.string, "@source" + "." + n.string, "@target" + "." + m.string);
		}
		
		for (Attribute n : am.keySet()) {
			Attribute m = am.get(n);
			g2.addEdge(n.name + " " + m.name, "@source" + "." + n.name, "@target" + "." + m.name);
		}
		
		return g2;
	}

	
//	Transformer<String, Paint> vertexPaint = new Transformer<String, Paint>() {
//		public Paint transform(String i) {
//			return which(i);
//		}
//
//		private Color which(String t) {
//			int i = t.indexOf(".");
//			String j = t.substring(i+1);
//			String p = t.substring(0, i);
//			System.out.println("%%%%%%%%%%%%%%%%%% " + j);
//			if (source.isAttribute(j) || target.isAttribute(j)) {
//				return null;
//			}
//			if (p.equals("@source")) {
//				return Environment.colors.get(source.name0);
//			} 
//				return Environment.colors.get(target.name0);
//		}
//	};
	
	public  JPanel doView(final Environment env, Graph<String,String> sgv) {
		// Layout<V, E>, BasicVisualizationServer<V,E>
		Layout<String, String> layout = new FRLayout<>(sgv);
		//Layout<String, String> layout = new KKLayout(sgv);

		//	Layout<String, String> layout = new ISOMLayout<String,String>(sgv);
	//			Layout<String, String> layout = new CircleLayout(sgv);
		layout.setSize(new Dimension(600, 400));
		VisualizationViewer<String, String> vv = new VisualizationViewer<String, String>(layout);
		vv.setPreferredSize(new Dimension(600, 400));
		// Setup up a new vertex to paint transformer...
		Transformer<String, Paint> vertexPaint = new Transformer<String, Paint>() {
			public Paint transform(String i) {
				return which(i);
			}

			private Color which(String t) {
				int i = t.indexOf(".");
				String j = t.substring(i+1);
				String p = t.substring(0, i);
				if (source.isAttribute(j) || target.isAttribute(j)) {
					return UIManager.getColor ( "Panel.background" );
				}
				if (p.equals("@source")) {
					return env.colors.get(source.name0);
				} 
					return env.colors.get(target.name0);
			}
		};
		DefaultModalGraphMouse<String, String> gm = new DefaultModalGraphMouse<>();
        gm.setMode(ModalGraphMouse.Mode.TRANSFORMING);
        vv.setGraphMouse(gm);
        gm.setMode(Mode.PICKING);

		// Set up a new stroke Transformer for the edges
		float dash[] = { 10.0f };
		final Stroke edgeStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
				BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f);
		final Stroke bs = new BasicStroke();

		Transformer<String, Stroke> edgeStrokeTransformer = new Transformer<String, Stroke>() {
			public Stroke transform(String s) {
				if (s.contains(" ")) {
					return edgeStroke;
				} 
				return bs;
			}
		};
		vv.getRenderContext().setVertexFillPaintTransformer(vertexPaint);
		vv.getRenderContext().setEdgeStrokeTransformer(edgeStrokeTransformer);
		vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller<String>() {
			@Override
			public String transform(String t) {
				int i = t.indexOf(".");
				String j = t.substring(i+1);
				String p = t.substring(0, i);
				if (p.equals("@source")) {
					j = source.name0 + "." + j;
				} else {
					j = target.name0 + "." + j;
				}
				return j;
			}
		});
		vv.getRenderContext().setEdgeLabelTransformer(new ToStringLabeller<String>() {
			
			@Override
			public String transform(String t) {
//				if (t.contains(" ")) {
//					return "";
//				}
//				return t;
				return "";
			}
		});
		//vv.getRenderer().getVertexRenderer().
	//	vv.getRenderContext().setLabelOffset(20);
	//	vv.getRenderer().getVertexLabelRenderer().setPosition(Position.CNTR);
		
		
		return vv;
	}

	public static Mapping compose(String string, Mapping l, Mapping r) throws FQLException {
		if (!l.target.equals(r.source)) {
			throw new RuntimeException(l.target + "\n\n" + r.source);
		}
		
		List<Pair<String, String>> xxx = new LinkedList<>();
		List<Pair<String, List<String>>> yyy = new LinkedList<>();
		
		for (Node n : l.source.nodes) {
			xxx.add(new Pair<>(n.string, r.nm.get(l.nm.get(n)).string));
		}
		
		for (Edge e : l.source.edges) {
			Path p = l.em.get(e);
			yyy.add(new Pair<>(e.name, r.appy(p).asList()));
		}
		//TODO attribute-related
		return new Mapping(string, l.source, r.target, xxx, yyy);
	}

	@Override
	public JPanel join() {
		return null;
	}

	@Override
	public String tojson() {
		String ret = "{\n" +
		"\"source\" : " + source.tojson() + ",\n" +
		"\"target\" : " + target.tojson() + ",\n" +
		"\"onObjects\" : " + jsonNodes() + ",\n" +
		"\"onGenerators\" : " + jsonEdges() +
		"\n}\n";
		
//		try {
//			System.out.println(new JSONMappingParser().parse(new Tokens(ret)));
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		return ret;
	}

	private String jsonEdges() {
		String s = "[";
		
		boolean first = true;
		for (Entry<Edge,Path> e : em.entrySet()) {
			if (!first) {
				s += ","; 
			}
			first = false;
			s +=  "\n{" + "\"arrow\" : " + (e.getKey().tojson() + ",\n\"path\" : " + e.getValue().tojson()) + "}";
		}
		
		return s + "\n]";

	}

	private String jsonNodes() {
		String s = "{";
		
		boolean first = true;
		for (Entry<Node,Node> e : nm.entrySet()) {
			if (!first) {
				s += ","; 
			}
			first = false;
			s += (e.getKey().tojson() + " : " + e.getValue().tojson());
		}
		
		return s + "}";
	}

	@Override
	public JPanel json() {
		JTextArea q = new JTextArea(tojson());		
		q.setWrapStyleWord(true);
		q.setLineWrap(true);
		JPanel p = new JPanel(new GridLayout(1,1));
		JScrollPane jsc = new JScrollPane(q);
	//	jsc.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		p.add(jsc);
		return p;
	}

	public static Mapping fromjson(String mapping) throws Exception {
		return new JSONMappingParser().parse(new FqlTokenizer(mapping)).value;
	}

	@Override
	public JPanel denotation() throws FQLException {
		return null;
	}

	@Override
	public JPanel initial() throws FQLException {
		return null;
	}
	
	
}
