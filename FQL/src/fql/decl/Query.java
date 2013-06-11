package fql.decl;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Paint;
import java.awt.Stroke;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
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
import fql.DEBUG;
import fql.DEBUG.Intermediate;
import fql.FQLException;
import fql.Fn;
import fql.Pair;
import fql.Triple;
import fql.cat.Arr;
import fql.cat.CommaCat;
import fql.cat.FDM;
import fql.cat.FinCat;
import fql.cat.FinFunctor;
import fql.cat.Inst;
import fql.cat.SemQuery;
import fql.cat.SetFunTrans;
import fql.cat.Value;
import fql.cat.Value.VALUETYPE;
import fql.gui.Viewable;
import fql.sql.PSMGen;

/**
 * 
 * @author ryan
 *
 * Queries, and composition.
 */
public class Query implements Viewable<Query> {

	public Mapping project, join, union;
	
	public String name;

	public Signature getSource() {
		return project.target;
	}

	public Signature getTarget() {
		return union.target;
	}

	/**
	 * 
	 * @param name
	 * @param env
	 * @param project
	 * @param join
	 * @param union
	 * @throws FQLException
	 */
	public Query(String name, Mapping project, Mapping join, Mapping union) throws FQLException {
		if (!project.source.equals(join.source)) {
			throw new FQLException("Ill-typed: " + name + "\nProject source: " + project.source + "\nJoin source: " + join.source);
		}
		if (!join.target.equals(union.source)) {
			throw new FQLException("Ill-typed: " + name + "\nJoin target: " + join.target + "\nUnion source: " + union.source);
		}
		this.name = name;
		this.project = project;
		this.join = join;
		this.union = union;
	}
	
	public Query(String name, Environment env, QueryDecl d) throws FQLException {
		this.name = name;
		switch (d.kind) {
		case COMPOSE : 
			isId = false;
			Query m1 = env.getQuery(d.q1);
			Query m2 = env.getQuery(d.q2);
//			System.out.println("composing " + m1 + " and " + m2);
//			System.out.println("m1src " + m1.getSource() + " and " + m2.getTarget());
//			System.out.println(m2.getTarget().equals(m1.getSource()));
			if (!m2.getTarget().equals(m1.getSource())) {
				throw new FQLException("Ill-typed: " + d.name);
			}
			Query q = Query.compose(env, name, m1, m2);
		//	System.out.println("result " + q);

			if (!q.getSource().equals(m2.getSource())) {
				throw new FQLException("Ill-typed: " + d.name + " " + q.getSource() + " and " + m2.getSource());				
			}
			if (!q.getTarget().equals(m1.getTarget())) {
				throw new FQLException("Ill-typed: " + d.name + " " + q.getTarget() + " and " + m1.getTarget());
			}
			this.project = q.project;
			this.join = q.join;
			this.union = q.union;
			this.name = q.name;
			break; 
		case ID : 
			isId = true;
			Signature s = env.getSchema(d.schema);
			project = new Mapping(env, s);
			join = new Mapping(env, s);
			union = new Mapping(env, s);
			break;

		case QUERY : 
			//
			// F : S' -> S
			// G : S' -> S''
			// H : S'' -> T
			isId = false;
			project = env.getMapping(d.project);
			join = env.getMapping(d.join);
			union = env.getMapping(d.union);
			if (!project.source.equals(join.source) || !join.target.equals(union.source)) {
				throw new FQLException("Ill-typed: " + d);
			}
			break;
			
		 default:
			throw new RuntimeException("d.kind");
		}
	}

boolean isId;

	@Override
	public JPanel view() throws FQLException {
		JPanel p = new JPanel(new GridLayout(3,1));
		p.setBorder(BorderFactory.createEmptyBorder());
		
		JPanel q  = project.view();
		q.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), "Delta " + project.name + " : " + project.target.name0 + " -> " + project.source.name0));
		p.add(q);
		
		q = join.view();		
		q.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), "Pi "  + join.name + " : " + join.source.name0 + " -> " + join.target.name0));
		p.add(q);
		
		q = union.view();
		q.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), "Sigma " + union.name + " : " + union.source.name0 + " -> " + union.target.name0));
		p.add(q);
		
		return p;
	}

	@Override
	public JPanel text() {
			String ret = toString();
			
			JPanel tap = new JPanel(new GridLayout(2,2));

			JTextArea ta = new JTextArea(ret);
			ta.setWrapStyleWord(true);
			ta.setLineWrap(true);
			JScrollPane xxx = new JScrollPane(ta);		
			JPanel p = new JPanel(new GridLayout(1,1));
			p.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), "Query " + name + " : " + project.target.name0 + " -> " + union.target.name0 ));
			p.add(xxx);
			tap.add(p);

			
			String delta = "";
			try {
				delta = Mapping.printNicely(PSMGen.delta(project, "input", "output"));
			} catch (Exception e) {
				delta = e.toString();
			}
			
			String sigma = "";
			try {
				sigma = Mapping.printNicely(PSMGen.sigma(project, "input", "output"));
			} catch (Exception e) {
				sigma = e.toString();
			}
			
			String pi = "";
			try {
				pi = Mapping.printNicely(PSMGen.pi(project, "input", "output"));
			} catch (Exception e) {
				pi = e.toString();
			}

			JTextArea ta2 = new JTextArea(delta);
			ta2.setWrapStyleWord(true);
			ta2.setLineWrap(true);
			JScrollPane xxx2 = new JScrollPane(ta2);		
			JPanel p2 = new JPanel(new GridLayout(1,1));
			p2.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), "Delta " + project.name + " : " + project.target.name0 + " -> " + project.source.name0));
			p2.add(xxx2);
			tap.add(p2);
			

			JTextArea ta3 = new JTextArea(pi);
			ta3.setWrapStyleWord(true);
			ta3.setLineWrap(true);
			JScrollPane xxx3 = new JScrollPane(ta3);		
			JPanel p3 = new JPanel(new GridLayout(1,1));
			p3.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), "Pi "  + join.name + " : " + join.source.name0 + " -> " + join.target.name0));
			p3.add(xxx3);
			tap.add(p3);
			
			JTextArea ta4 = new JTextArea(sigma);
			ta4.setWrapStyleWord(true);
			ta4.setLineWrap(true);
			JScrollPane xxx4 = new JScrollPane(ta4);		
			JPanel p4 = new JPanel(new GridLayout(1,1));
			p4.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), "Sigma " + union.name + " : " + union.source.name0 + " -> " + union.target.name0));
			p4.add(xxx4);
			tap.add(p4);	
	
			return tap;
	}


	@Override
	public String toString() {
		String s = "query " + name + " : " + getSource().name0 + " -> " + getTarget().name0 + " = ";
		if (isId) {
			return s + "id " + getSource().name0;
		}
		return s + "delta " + project.name + " pi " + join.name + " sigma " + union.name;
	}

	/**
	 * Evaluates a query on an instance
	 */
//	public Map<String, Set<Pair<String, String>>> eval(Instance theinstance) throws FQLException {
//		Map<String, RA> delta = RA.delta(project);
//		Map<String, RA> sigma = RA.sigma(union);
//		Map<String, RA> pi = RA.pi(join);
//		
//		Map<String, Set<Object[]>> i0 = convert0(theinstance);
//		
//		Map<String, Set<Object[]>> i1 = RA.eval0(delta, i0);
//		Map<String, Set<String[]>> i2 = RA.eval0(sigma, i1);
//		Map<String, Set<String[]>> i3 = RA.eval0(pi, i2);
//		
//		return convert(i3);
//	}
	
	
	public static Map<String, Set<Object[]>> convert0(Instance theinstance) {
		Map<String, Set<Object[]>> ret = new HashMap<String, Set<Object[]>>();
		for (Entry<String, Set<Pair<Object, Object>>> k : theinstance.data.entrySet()) {
			ret.put(k.getKey(), conv(k.getValue()));
		}
		return ret;
	}

	private static Set<Object[]> conv(Set<Pair<Object, Object>> set) {
		Set<Object[]> ret = new HashSet<Object[]>();
		for (Pair<Object, Object> p : set) {
			Object[] s = new Object[] { p.first, p.second };
			ret.add(s);
		}
		return ret;
	}

	public static Map<String, Set<Pair<Object, Object>>> convert(
			Map<String, Set<Object[]>> i1) {
		Map<String, Set<Pair<Object, Object>>> ret = new HashMap<>();
		for (Entry<String, Set<Object[]>> s : i1.entrySet()) {
			ret.put(s.getKey(), convX(s.getValue()));
		}
		return ret;
	}

	private static Set<Pair<Object, Object>> convX(Set<Object[]> set) {
		Set<Pair<Object, Object>> ret = new HashSet<>();
		for (Object[] s : set) {
			ret.add(new Pair<>(s[0], s[1]));
		}
		return ret;
	}
	
	@Override
	public JPanel pretty(final Environment env) throws FQLException {
			Graph<String,String> g = build();
			if (g.getVertexCount() == 0) {
				return new JPanel();
			}
			return doView(env, g);
	}
	
	@Override
	public String type() {
		return "query";
	}
	
	public  JPanel doView(final Environment env, Graph<String,String> sgv) {
		// Layout<V, E>, BasicVisualizationServer<V,E>
		//Layout<String, String> layout = new FRLayout<>(sgv);
		//Layout<String, String> layout = new KKLayout(sgv);
		//Layout<String, String> layout = new SpringLayout(sgv);

			Layout<String, String> layout = new ISOMLayout<String,String>(sgv);
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
				//String j = t.substring(i+1);
				String p = t.substring(0, i);
				if (p.equals("@project_source")) {
					return env.colors.get(project.source.name0);
				} else if (p.equals("@project_target")) {
					return env.colors.get(project.target.name0);
				} else if (p.equals("@join_target")) {
					return env.colors.get(join.target.name0);
				} else {
					return env.colors.get(union.target.name0);
				}
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
				if (s.contains("#")) {
					return edgeStroke;
				} 
				return bs;
				//return edgeStroke;
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
				if (p.equals("@project_source")) {
					j = project.source.name0 + "." + j;
				} else if (p.equals("@project_target")) {
					j = project.target.name0 + "." + j;
				} else if (p.equals("@join_target")) {
					j = join.target.name0 + "." + j;
				} else {
					j = union.target.name0 + "." + j;
				}
				return j;
			}
		});
		vv.getRenderContext().setEdgeLabelTransformer(new ToStringLabeller<String>() {
			
			@Override
			public String transform(String t) {
				int i = t.indexOf(".");
				String j = t.substring(i+1);
				String p = t.substring(0, i);
				if (p.equals("#project")) {
					j = project.name;
				} else if (p.equals("#join")) {
					j = join.name;
				} else if (p.equals("#union")) {
					j = union.name;
				} else {
				}
				return j;
			}
		});
		//vv.getRenderer().getVertexRenderer().
	//	vv.getRenderContext().setLabelOffset(20);
	//	vv.getRenderer().getVertexLabelRenderer().setPosition(Position.CNTR);
		
		return vv;
	}
	
	public Graph<String,String> build() {
		// Graph<V, E> where V is the type of the vertices
	
		Graph<String, String> g2 = new DirectedSparseMultigraph<String, String>();
		for (Node n : project.target.nodes) {
			g2.addVertex("@project_target" + "." + n.string);
		}
		for (Node n : project.source.nodes) {
			g2.addVertex("@project_source" + "." + n.string);
		}
		for (Node n : join.target.nodes) {
			g2.addVertex("@join_target" + "." + n.string);
		}
		for (Node n : union.target.nodes) {
			g2.addVertex("@union_target" + "." + n.string);
		}
		if (DEBUG.SHOW_QUERY_PATHS) {
			for (Edge e : project.target.edges) {
				g2.addEdge("@project_target" + "." + e.name, "@project_target" + "." + e.source.string, "@project_target" + "." + e.target.string);
			}
			for (Edge e : project.source.edges) {
				g2.addEdge("@project_source" + "." + e.name, "@project_source" + "." + e.source.string, "@project_source" + "." + e.target.string);
			}
			for (Edge e : join.target.edges) {
				g2.addEdge("@join_target" + "." + e.name, "@join_target" + "." + e.source.string, "@join_target" + "." + e.target.string);
			}
			for (Edge e : union.target.edges) {
				g2.addEdge("@union_target" + "." + e.name, "@union_target" + "." + e.source.string, "@union_target" + "." + e.target.string);
			}
			
		}
		
				
		for (Node n : project.nm.keySet()) {
			Node m = project.nm.get(n);
			g2.addEdge("#project." + n.string + " " + m.string, "@project_source" + "." + n.string, "@project_target" + "." + m.string);
		}
		for (Node n : join.nm.keySet()) {
			Node m = join.nm.get(n);
			g2.addEdge("#join." + n.string + " " + m.string, "@project_source" + "." + n.string, "@join_target" + "." + m.string);
		}
		for (Node n : union.nm.keySet()) {
			Node m = union.nm.get(n);
			g2.addEdge("#union." + n.string + " " + m.string, "@join_target" + "." + n.string, "@union_target" + "." + m.string);
		}
		
		return g2;
	}
	
	/**
	 * Implements composition at the semantic level
	 */
	public static 
	<ObjS,ArrowS,ObjB,ArrowB,ObjA,ArrowA,ObjT,ArrowT,ObjD,ArrowD,ObjC,ArrowC,ObjU,ArrowU> 
	SemQuery<Triple<Triple<ObjB, Triple<ObjA, ObjD, ObjT>, Arr<ObjA, ArrowA>>, Pair<ObjD, Value<Triple<ObjA, ObjD, ObjT>, Triple<ObjA, ObjD, ObjT>>>, Arr<Triple<ObjA, ObjD, ObjT>, Triple<Arr<ObjA, ArrowA>, Arr<ObjD, ArrowD>, Arr<ObjT, ArrowT>>>>, Pair<Arr<Triple<ObjB, Triple<ObjA, ObjD, ObjT>, Arr<ObjA, ArrowA>>, Pair<Arr<ObjB, ArrowB>, Arr<Triple<ObjA, ObjD, ObjT>, Triple<Arr<ObjA, ArrowA>, Arr<ObjD, ArrowD>, Arr<ObjT, ArrowT>>>>>, Arr<Pair<ObjD, Value<Triple<ObjA, ObjD, ObjT>, Triple<ObjA, ObjD, ObjT>>>, Arr<ObjD, ArrowD>>>, ObjS, ArrowS, Pair<ObjC, Value<Triple<ObjA, ObjD, ObjT>, Triple<ObjA, ObjD, ObjT>>>, Arr<ObjC, ArrowC>, ObjU, ArrowU> 
	doComposition(Environment env,
			 FinFunctor<ObjB, ArrowB, ObjS, ArrowS> s,
        	 FinFunctor<ObjB, ArrowB, ObjA, ArrowA> f,
        	 FinFunctor<ObjA, ArrowA, ObjT, ArrowT> t,
        	 FinFunctor<ObjD, ArrowD, ObjT, ArrowT> u,
		     FinFunctor<ObjD, ArrowD, ObjC, ArrowC> g,
		     FinFunctor<ObjC, ArrowC, ObjU, ArrowU> v) throws FQLException {
		
//		name = name;
		
	//	FinCat<ObjS, ArrowS> S = s.dstCat;
		FinCat<ObjB, ArrowB> B = s.srcCat;
		FinCat<ObjA, ArrowA> A = t.srcCat;
		FinCat<ObjT, ArrowT> T = t.dstCat;
		FinCat<ObjD, ArrowD> D = g.srcCat;
		//FinCat<ObjC, ArrowC> C = g.dstCat;
		//FinCat<ObjU, ArrowU> U = v.dstCat;
				
		Triple<FinCat<Triple<ObjA, ObjD, ObjT>, Triple<Arr<ObjA, ArrowA>, Arr<ObjD, ArrowD>, Arr<ObjT, ArrowT>>>, FinFunctor<Triple<ObjA, ObjD, ObjT>, Triple<Arr<ObjA, ArrowA>, Arr<ObjD, ArrowD>, Arr<ObjT, ArrowT>>, ObjA, ArrowA>, FinFunctor<Triple<ObjA, ObjD, ObjT>, Triple<Arr<ObjA, ArrowA>, Arr<ObjD, ArrowD>, Arr<ObjT, ArrowT>>, ObjD, ArrowD>> 
		  one = FDM.pullback(A, D, T, t, u);
		

		FinCat<Triple<ObjA, ObjD, ObjT>, Triple<Arr<ObjA, ArrowA>, Arr<ObjD, ArrowD>, Arr<ObjT, ArrowT>>> Aprime = one.first;
		FinFunctor<Triple<ObjA, ObjD, ObjT>, Triple<Arr<ObjA, ArrowA>, Arr<ObjD, ArrowD>, Arr<ObjT, ArrowT>>, ObjA, ArrowA> h = one.second;
		FinFunctor<Triple<ObjA, ObjD, ObjT>, Triple<Arr<ObjA, ArrowA>, Arr<ObjD, ArrowD>, Arr<ObjT, ArrowT>>, ObjD, ArrowD> k = one.third;
		
		
		CommaCat<ObjB, ArrowB, Triple<ObjA, ObjD, ObjT>, Triple<Arr<ObjA, ArrowA>, Arr<ObjD, ArrowD>, Arr<ObjT, ArrowT>>, ObjA, ArrowA> Bprime = new CommaCat<>(B, Aprime, A, f, h);
		FinFunctor<Triple<ObjB, Triple<ObjA, ObjD, ObjT>, Arr<ObjA, ArrowA>>, Pair<Arr<ObjB, ArrowB>, Arr<Triple<ObjA, ObjD, ObjT>, Triple<Arr<ObjA, ArrowA>, Arr<ObjD, ArrowD>, Arr<ObjT, ArrowT>>>>, ObjB, ArrowB> 
		m = Bprime.projA;
		FinFunctor<Triple<ObjB, Triple<ObjA, ObjD, ObjT>, Arr<ObjA, ArrowA>>, Pair<Arr<ObjB, ArrowB>, Arr<Triple<ObjA, ObjD, ObjT>, Triple<Arr<ObjA, ArrowA>, Arr<ObjD, ArrowD>, Arr<ObjT, ArrowT>>>>, Triple<ObjA, ObjD, ObjT>, Triple<Arr<ObjA, ArrowA>, Arr<ObjD, ArrowD>, Arr<ObjT, ArrowT>>> 
		r = Bprime.projB;

		Inst<ObjD, ArrowD, Triple<ObjA, ObjD, ObjT>, Triple<ObjA, ObjD, ObjT>> du = FDM.degrothendieck(k);
		Inst<ObjC, ArrowC, Triple<ObjA, ObjD, ObjT>, Triple<ObjA, ObjD, ObjT>> piresult = FDM.pi(g, du);
		FinFunctor<Pair<ObjC, Value<Triple<ObjA, ObjD, ObjT>, Triple<ObjA, ObjD, ObjT>>>, Arr<ObjC, ArrowC>, ObjC, ArrowC> w = FDM.grothendieck(piresult);
		FinCat<Pair<ObjC, Value<Triple<ObjA, ObjD, ObjT>, Triple<ObjA, ObjD, ObjT>>>, Arr<ObjC, ArrowC>> M = w.srcCat;
				
		Inst<ObjD, ArrowD, Triple<ObjA, ObjD, ObjT>, Triple<ObjA, ObjD, ObjT>> deltaresult = FDM.delta(g, piresult);
		FinFunctor<Pair<ObjD, Value<Triple<ObjA, ObjD, ObjT>, Triple<ObjA, ObjD, ObjT>>>, Arr<ObjD, ArrowD>, ObjD, ArrowD> w0 = FDM.grothendieck(deltaresult);
		FinCat<Pair<ObjD, Value<Triple<ObjA, ObjD, ObjT>, Triple<ObjA, ObjD, ObjT>>>, Arr<ObjD, ArrowD>> Dprime = w0.srcCat;

		SetFunTrans<ObjD, ArrowD, Triple<ObjA, ObjD, ObjT>, Triple<ObjA, ObjD, ObjT>> epsilonresult = FDM.epsilon(g, deltaresult, du);
		
//		System.out.println("--------------------------");
//		System.out.println("k' src is");
//		System.out.println(k.srcCat);
//		System.out.println("integraldu's srcCat is");
//		FinFunctor<Triple<ObjA, ObjD, ObjT>,                                              Triple<Arr<ObjA, ArrowA>, Arr<ObjD, ArrowD>, Arr<ObjT, ArrowT>>, ObjD, ArrowD> kkk = one.third;
	//FinFunctor<Pair<ObjD, Value<Triple<ObjA, ObjD, ObjT>, Triple<ObjA, ObjD, ObjT>>>, Arr<ObjD, ArrowD>,                                               ObjD, ArrowD> xxx = FDM.grothendieck(du);
//		System.out.println(xxx.srcCat);
//		System.out.println("deltapiu is");
//		System.out.println(deltaresult);
//		System.out.println("epsilon is");
//		System.out.println(epsilonresult);
	//	System.out.println("--------------------------");

		
		FinFunctor<Pair<ObjD, Value<Triple<ObjA, ObjD, ObjT>, Triple<ObjA, ObjD, ObjT>>>, Arr<ObjD, ArrowD>, Pair<ObjD, Value<Triple<ObjA, ObjD, ObjT>, Triple<ObjA, ObjD, ObjT>>>, Arr<ObjD, ArrowD>>
		e0 = FDM.grothendieck(epsilonresult);
		
		FinFunctor<Pair<ObjD, Value<Triple<ObjA, ObjD, ObjT>, Triple<ObjA, ObjD, ObjT>>>, Arr<ObjD, ArrowD>, Pair<ObjC, Value<Triple<ObjA, ObjD, ObjT>, Triple<ObjA, ObjD, ObjT>>>, Arr<ObjC, ArrowC>> q = FDM.makeG(w0.srcCat, w.srcCat, g);

		
		/////////////////////////////////////

		Map<Pair<ObjD, Value<Triple<ObjA, ObjD, ObjT>, Triple<ObjA, ObjD, ObjT>>>, Triple<ObjA, ObjD, ObjT>> pairToTripleBe1 = new HashMap<>();
		for (Pair<ObjD, Value<Triple<ObjA, ObjD, ObjT>, Triple<ObjA, ObjD, ObjT>>> c : e0.dstCat.objects) {
			if (!c.second.which.equals(VALUETYPE.CHOICE)) {
				throw new RuntimeException("Bad target type " + c.second + " " + c.second.which);
			}
			pairToTripleBe1.put(c, c.second.tagCargo.x);
	}
		Map<Arr<Pair<ObjD, Value<Triple<ObjA, ObjD, ObjT>, Triple<ObjA, ObjD, ObjT>>>, Arr<ObjD, ArrowD>>, Arr<Triple<ObjA, ObjD, ObjT>, Triple<Arr<ObjA, ArrowA>, Arr<ObjD, ArrowD>, Arr<ObjT, ArrowT>>>> 
		pairToTripleBe2 = new HashMap<>();
		for (Arr<Pair<ObjD, Value<Triple<ObjA, ObjD, ObjT>, Triple<ObjA, ObjD, ObjT>>>, Arr<ObjD, ArrowD>> c : e0.dstCat.arrows) {
			Triple<ObjA, ObjD, ObjT> arrsrc = pairToTripleBe1.get(c.src);
			Triple<ObjA, ObjD, ObjT> arrdst = pairToTripleBe1.get(c.dst);
			for (Arr<Triple<ObjA, ObjD, ObjT>, Triple<Arr<ObjA, ArrowA>, Arr<ObjD, ArrowD>, Arr<ObjT, ArrowT>>> cand : k.srcCat.hom(arrsrc, arrdst)) {
				if (cand.arr.second.equals(c.arr)) {
					pairToTripleBe2.put(c, cand);
				}
			}
			if (pairToTripleBe2.get(c) == null) {
				throw new RuntimeException("no candidate");
			}
		}

		FinFunctor<Pair<ObjD, Value<Triple<ObjA, ObjD, ObjT>, Triple<ObjA, ObjD, ObjT>>>, Arr<ObjD, ArrowD>, Triple<ObjA, ObjD, ObjT>, Triple<Arr<ObjA, ArrowA>, Arr<ObjD, ArrowD>, Arr<ObjT, ArrowT>>> 
		pairToTripleBe = 
				new FinFunctor<>(pairToTripleBe1, pairToTripleBe2, e0.dstCat, k.srcCat);
		
		FinFunctor<Pair<ObjD, Value<Triple<ObjA, ObjD, ObjT>, Triple<ObjA, ObjD, ObjT>>>, Arr<ObjD, ArrowD>, Triple<ObjA, ObjD, ObjT>, Triple<Arr<ObjA, ArrowA>, Arr<ObjD, ArrowD>, Arr<ObjT, ArrowT>>> e
				= FinFunctor.compose(e0, pairToTripleBe);

		////////////////////////////////////
		
		CommaCat<Triple<ObjB, Triple<ObjA, ObjD, ObjT>, Arr<ObjA, ArrowA>>, Pair<Arr<ObjB, ArrowB>, Arr<Triple<ObjA, ObjD, ObjT>, Triple<Arr<ObjA, ArrowA>, Arr<ObjD, ArrowD>, Arr<ObjT, ArrowT>>>>, Pair<ObjD, Value<Triple<ObjA, ObjD, ObjT>, Triple<ObjA, ObjD, ObjT>>>, Arr<ObjD, ArrowD>, Triple<ObjA, ObjD, ObjT>, Triple<Arr<ObjA, ArrowA>, Arr<ObjD, ArrowD>, Arr<ObjT, ArrowT>>> 
		N = new CommaCat<>(Bprime, Dprime, Aprime, r, e);
		FinFunctor<Triple<Triple<ObjB, Triple<ObjA, ObjD, ObjT>, Arr<ObjA, ArrowA>>, Pair<ObjD, Value<Triple<ObjA, ObjD, ObjT>, Triple<ObjA, ObjD, ObjT>>>, Arr<Triple<ObjA, ObjD, ObjT>, Triple<Arr<ObjA, ArrowA>, Arr<ObjD, ArrowD>, Arr<ObjT, ArrowT>>>>, Pair<Arr<Triple<ObjB, Triple<ObjA, ObjD, ObjT>, Arr<ObjA, ArrowA>>, Pair<Arr<ObjB, ArrowB>, Arr<Triple<ObjA, ObjD, ObjT>, Triple<Arr<ObjA, ArrowA>, Arr<ObjD, ArrowD>, Arr<ObjT, ArrowT>>>>>, Arr<Pair<ObjD, Value<Triple<ObjA, ObjD, ObjT>, Triple<ObjA, ObjD, ObjT>>>, Arr<ObjD, ArrowD>>>, Triple<ObjB, Triple<ObjA, ObjD, ObjT>, Arr<ObjA, ArrowA>>, Pair<Arr<ObjB, ArrowB>, Arr<Triple<ObjA, ObjD, ObjT>, Triple<Arr<ObjA, ArrowA>, Arr<ObjD, ArrowD>, Arr<ObjT, ArrowT>>>>> 
		n = N.projA;
		FinFunctor<Triple<Triple<ObjB, Triple<ObjA, ObjD, ObjT>, Arr<ObjA, ArrowA>>, Pair<ObjD, Value<Triple<ObjA, ObjD, ObjT>, Triple<ObjA, ObjD, ObjT>>>, Arr<Triple<ObjA, ObjD, ObjT>, Triple<Arr<ObjA, ArrowA>, Arr<ObjD, ArrowD>, Arr<ObjT, ArrowT>>>>, Pair<Arr<Triple<ObjB, Triple<ObjA, ObjD, ObjT>, Arr<ObjA, ArrowA>>, Pair<Arr<ObjB, ArrowB>, Arr<Triple<ObjA, ObjD, ObjT>, Triple<Arr<ObjA, ArrowA>, Arr<ObjD, ArrowD>, Arr<ObjT, ArrowT>>>>>, Arr<Pair<ObjD, Value<Triple<ObjA, ObjD, ObjT>, Triple<ObjA, ObjD, ObjT>>>, Arr<ObjD, ArrowD>>>, Pair<ObjD, Value<Triple<ObjA, ObjD, ObjT>, Triple<ObjA, ObjD, ObjT>>>, Arr<ObjD, ArrowD>>
		p = N.projB;
		
		FinFunctor<Pair<ObjC, Value<Triple<ObjA, ObjD, ObjT>, Triple<ObjA, ObjD, ObjT>>>, Arr<ObjC, ArrowC>, ObjU, ArrowU> union = FinFunctor.compose(w, v);
		
		FinFunctor<Triple<Triple<ObjB, Triple<ObjA, ObjD, ObjT>, Arr<ObjA, ArrowA>>, Pair<ObjD, Value<Triple<ObjA, ObjD, ObjT>, Triple<ObjA, ObjD, ObjT>>>, Arr<Triple<ObjA, ObjD, ObjT>, Triple<Arr<ObjA, ArrowA>, Arr<ObjD, ArrowD>, Arr<ObjT, ArrowT>>>>, Pair<Arr<Triple<ObjB, Triple<ObjA, ObjD, ObjT>, Arr<ObjA, ArrowA>>, Pair<Arr<ObjB, ArrowB>, Arr<Triple<ObjA, ObjD, ObjT>, Triple<Arr<ObjA, ArrowA>, Arr<ObjD, ArrowD>, Arr<ObjT, ArrowT>>>>>, Arr<Pair<ObjD, Value<Triple<ObjA, ObjD, ObjT>, Triple<ObjA, ObjD, ObjT>>>, Arr<ObjD, ArrowD>>>, Pair<ObjC, Value<Triple<ObjA, ObjD, ObjT>, Triple<ObjA, ObjD, ObjT>>>, Arr<ObjC, ArrowC>> join = FinFunctor.compose(p,q);

		FinFunctor<Triple<Triple<ObjB, Triple<ObjA, ObjD, ObjT>, Arr<ObjA, ArrowA>>, Pair<ObjD, Value<Triple<ObjA, ObjD, ObjT>, Triple<ObjA, ObjD, ObjT>>>, Arr<Triple<ObjA, ObjD, ObjT>, Triple<Arr<ObjA, ArrowA>, Arr<ObjD, ArrowD>, Arr<ObjT, ArrowT>>>>, Pair<Arr<Triple<ObjB, Triple<ObjA, ObjD, ObjT>, Arr<ObjA, ArrowA>>, Pair<Arr<ObjB, ArrowB>, Arr<Triple<ObjA, ObjD, ObjT>, Triple<Arr<ObjA, ArrowA>, Arr<ObjD, ArrowD>, Arr<ObjT, ArrowT>>>>>, Arr<Pair<ObjD, Value<Triple<ObjA, ObjD, ObjT>, Triple<ObjA, ObjD, ObjT>>>, Arr<ObjD, ArrowD>>>, ObjS, ArrowS> project = FinFunctor.compose(FinFunctor.compose(n,m),s);
				
		SemQuery<Triple<Triple<ObjB, Triple<ObjA, ObjD, ObjT>, Arr<ObjA, ArrowA>>, Pair<ObjD, Value<Triple<ObjA, ObjD, ObjT>, Triple<ObjA, ObjD, ObjT>>>, Arr<Triple<ObjA, ObjD, ObjT>, Triple<Arr<ObjA, ArrowA>, Arr<ObjD, ArrowD>, Arr<ObjT, ArrowT>>>>, Pair<Arr<Triple<ObjB, Triple<ObjA, ObjD, ObjT>, Arr<ObjA, ArrowA>>, Pair<Arr<ObjB, ArrowB>, Arr<Triple<ObjA, ObjD, ObjT>, Triple<Arr<ObjA, ArrowA>, Arr<ObjD, ArrowD>, Arr<ObjT, ArrowT>>>>>, Arr<Pair<ObjD, Value<Triple<ObjA, ObjD, ObjT>, Triple<ObjA, ObjD, ObjT>>>, Arr<ObjD, ArrowD>>>, ObjS, ArrowS, Pair<ObjC, Value<Triple<ObjA, ObjD, ObjT>, Triple<ObjA, ObjD, ObjT>>>, Arr<ObjC, ArrowC>, ObjU, ArrowU> ret = new SemQuery<>(project, join, union);

		if (DEBUG.INTERMEDIATE == Intermediate.ALL) {
			env.signatures.put("Aprime", Aprime.toSig("Aprime").first);
			env.signatures.put("Bprime", Bprime.toSig("Bprime").first);
			env.signatures.put("N", N.toSig("Nprime").first);
			env.signatures.put("Dprime", Dprime.toSig("Dprime").first);
			env.signatures.put("M", M.toSig("MMM").first);
		}
		
		return ret;
	}
	
	static <ObjD,ArrowD,ObjA,ArrowA,ObjT,ArrowT> Arr<Triple<ObjA, ObjD, ObjT>, Triple<Arr<ObjA, ArrowA>, Arr<ObjD, ArrowD>, Arr<ObjT, ArrowT>>>
	find2X(Arr<Pair<ObjD, String>, Arr<ObjD, ArrowD>> c, 
			List<Arr<Triple<ObjA, ObjD, ObjT>, Triple<Arr<ObjA, ArrowA>, Arr<ObjD, ArrowD>, Arr<ObjT, ArrowT>>>> sss) {
		for (Arr<Triple<ObjA, ObjD, ObjT>, Triple<Arr<ObjA, ArrowA>, Arr<ObjD, ArrowD>, Arr<ObjT, ArrowT>>> b0 : sss) {
			if (b0.arr.second.equals(c.arr)) {
				return b0;
			}
		}
		throw new RuntimeException("Couldn't find " + c + " in " + sss);
	}

	/**
	 * Implements composition at a syntactic level, adding to the environment.
	 */
	public static Query compose(Environment env, String name, Query q2, Query q1) throws FQLException {
		Mapping s0 = q1.project;
		Mapping f0 = q1.join;
		Mapping t0 = q1.union;
		Mapping u0 = q2.project;
		Mapping g0 = q2.join;
		Mapping v0 = q2.union;
		
		//System.out.println("name is " + name);
		
		Triple<FinFunctor<Node, Path, Node, Path>, Pair<FinCat<Node, Path>, Fn<Path, Arr<Node, Path>>>, Pair<FinCat<Node, Path>, Fn<Path, Arr<Node, Path>>>> 
		isoS = s0.toFunctor2();
		Triple<FinFunctor<Node, Path, Node, Path>, Pair<FinCat<Node, Path>, Fn<Path, Arr<Node, Path>>>, Pair<FinCat<Node, Path>, Fn<Path, Arr<Node, Path>>>> 
		isoT = v0.toFunctor2();
		
//		env, name, isoS.second.second.first.first, isoS.second.second.second.first,
		
		SemQuery<Triple<Triple<Node, Triple<Node, Node, Node>, Arr<Node, Path>>, Pair<Node, Value<Triple<Node, Node, Node>, Triple<Node, Node, Node>>>, Arr<Triple<Node, Node, Node>, Triple<Arr<Node, Path>, Arr<Node, Path>, Arr<Node, Path>>>>, Pair<Arr<Triple<Node, Triple<Node, Node, Node>, Arr<Node, Path>>, Pair<Arr<Node, Path>, Arr<Triple<Node, Node, Node>, Triple<Arr<Node, Path>, Arr<Node, Path>, Arr<Node, Path>>>>>, Arr<Pair<Node, Value<Triple<Node, Node, Node>, Triple<Node, Node, Node>>>, Arr<Node, Path>>>, Node, Path, Pair<Node, Value<Triple<Node, Node, Node>, Triple<Node, Node, Node>>>, Arr<Node, Path>, Node, Path> 
		ret = doComposition(env, isoS.first,f0.toFunctor2().first,t0.toFunctor2().first,u0.toFunctor2().first,g0.toFunctor2().first,isoT.first);

		Triple<Mapping, Triple<Signature, Pair<Map<Triple<Triple<Node, Triple<Node, Node, Node>, Arr<Node, Path>>, Pair<Node, Value<Triple<Node, Node, Node>, Triple<Node, Node, Node>>>, Arr<Triple<Node, Node, Node>, Triple<Arr<Node, Path>, Arr<Node, Path>, Arr<Node, Path>>>>, String>, Map<String, Triple<Triple<Node, Triple<Node, Node, Node>, Arr<Node, Path>>, Pair<Node, Value<Triple<Node, Node, Node>, Triple<Node, Node, Node>>>, Arr<Triple<Node, Node, Node>, Triple<Arr<Node, Path>, Arr<Node, Path>, Arr<Node, Path>>>>>>, Pair<Map<Arr<Triple<Triple<Node, Triple<Node, Node, Node>, Arr<Node, Path>>, Pair<Node, Value<Triple<Node, Node, Node>, Triple<Node, Node, Node>>>, Arr<Triple<Node, Node, Node>, Triple<Arr<Node, Path>, Arr<Node, Path>, Arr<Node, Path>>>>, Pair<Arr<Triple<Node, Triple<Node, Node, Node>, Arr<Node, Path>>, Pair<Arr<Node, Path>, Arr<Triple<Node, Node, Node>, Triple<Arr<Node, Path>, Arr<Node, Path>, Arr<Node, Path>>>>>, Arr<Pair<Node, Value<Triple<Node, Node, Node>, Triple<Node, Node, Node>>>, Arr<Node, Path>>>>, String>, Map<String, Arr<Triple<Triple<Node, Triple<Node, Node, Node>, Arr<Node, Path>>, Pair<Node, Value<Triple<Node, Node, Node>, Triple<Node, Node, Node>>>, Arr<Triple<Node, Node, Node>, Triple<Arr<Node, Path>, Arr<Node, Path>, Arr<Node, Path>>>>, Pair<Arr<Triple<Node, Triple<Node, Node, Node>, Arr<Node, Path>>, Pair<Arr<Node, Path>, Arr<Triple<Node, Node, Node>, Triple<Arr<Node, Path>, Arr<Node, Path>, Arr<Node, Path>>>>>, Arr<Pair<Node, Value<Triple<Node, Node, Node>, Triple<Node, Node, Node>>>, Arr<Node, Path>>>>>>>, Triple<Signature, Pair<Map<Node, String>, Map<String, Node>>, Pair<Map<Arr<Node, Path>, String>, Map<String, Arr<Node, Path>>>>> 
		proj1 = ret.project.toMapping(name + "_delta", name + "_B", name + "_S");
		Triple<Mapping, Triple<Signature, Pair<Map<Triple<Triple<Node, Triple<Node, Node, Node>, Arr<Node, Path>>, Pair<Node, Value<Triple<Node, Node, Node>, Triple<Node, Node, Node>>>, Arr<Triple<Node, Node, Node>, Triple<Arr<Node, Path>, Arr<Node, Path>, Arr<Node, Path>>>>, String>, Map<String, Triple<Triple<Node, Triple<Node, Node, Node>, Arr<Node, Path>>, Pair<Node, Value<Triple<Node, Node, Node>, Triple<Node, Node, Node>>>, Arr<Triple<Node, Node, Node>, Triple<Arr<Node, Path>, Arr<Node, Path>, Arr<Node, Path>>>>>>, Pair<Map<Arr<Triple<Triple<Node, Triple<Node, Node, Node>, Arr<Node, Path>>, Pair<Node, Value<Triple<Node, Node, Node>, Triple<Node, Node, Node>>>, Arr<Triple<Node, Node, Node>, Triple<Arr<Node, Path>, Arr<Node, Path>, Arr<Node, Path>>>>, Pair<Arr<Triple<Node, Triple<Node, Node, Node>, Arr<Node, Path>>, Pair<Arr<Node, Path>, Arr<Triple<Node, Node, Node>, Triple<Arr<Node, Path>, Arr<Node, Path>, Arr<Node, Path>>>>>, Arr<Pair<Node, Value<Triple<Node, Node, Node>, Triple<Node, Node, Node>>>, Arr<Node, Path>>>>, String>, Map<String, Arr<Triple<Triple<Node, Triple<Node, Node, Node>, Arr<Node, Path>>, Pair<Node, Value<Triple<Node, Node, Node>, Triple<Node, Node, Node>>>, Arr<Triple<Node, Node, Node>, Triple<Arr<Node, Path>, Arr<Node, Path>, Arr<Node, Path>>>>, Pair<Arr<Triple<Node, Triple<Node, Node, Node>, Arr<Node, Path>>, Pair<Arr<Node, Path>, Arr<Triple<Node, Node, Node>, Triple<Arr<Node, Path>, Arr<Node, Path>, Arr<Node, Path>>>>>, Arr<Pair<Node, Value<Triple<Node, Node, Node>, Triple<Node, Node, Node>>>, Arr<Node, Path>>>>>>>, Triple<Signature, Pair<Map<Pair<Node, Value<Triple<Node, Node, Node>, Triple<Node, Node, Node>>>, String>, Map<String, Pair<Node, Value<Triple<Node, Node, Node>, Triple<Node, Node, Node>>>>>, Pair<Map<Arr<Pair<Node, Value<Triple<Node, Node, Node>, Triple<Node, Node, Node>>>, Arr<Node, Path>>, String>, Map<String, Arr<Pair<Node, Value<Triple<Node, Node, Node>, Triple<Node, Node, Node>>>, Arr<Node, Path>>>>>> 
		join1 = ret.join.toMapping(name + "_pi", name + "_B", name + "_A" );
		Triple<Mapping, Triple<Signature, Pair<Map<Pair<Node, Value<Triple<Node, Node, Node>, Triple<Node, Node, Node>>>, String>, Map<String, Pair<Node, Value<Triple<Node, Node, Node>, Triple<Node, Node, Node>>>>>, Pair<Map<Arr<Pair<Node, Value<Triple<Node, Node, Node>, Triple<Node, Node, Node>>>, Arr<Node, Path>>, String>, Map<String, Arr<Pair<Node, Value<Triple<Node, Node, Node>, Triple<Node, Node, Node>>>, Arr<Node, Path>>>>>, Triple<Signature, Pair<Map<Node, String>, Map<String, Node>>, Pair<Map<Arr<Node, Path>, String>, Map<String, Arr<Node, Path>>>>> 
		union1 = ret.union.toMapping(name + "_sigma", name + "_A", name + "_T");
		
		env.signatures.put(name + "_B", proj1.second.first);
		env.signatures.put(name + "_A", join1.third.first);
		env.mappings.put(name + "_pi", join1.first);
		
		Map<String, Node> map1 =  proj1.third.second.second;
		List<Pair<String,String>> xxx = new LinkedList<>();
		List<Pair<String, List<String>>> yyy = new LinkedList<>();
		for (Node n : proj1.first.target.nodes) {
			Node x1 = map1.get(n.string);
			xxx.add(new Pair<>(n.string, x1.string));
		}

		Map<String, Arr<Node, Path>> mapA =  proj1.third.third.second;
		Fn<Path, Arr<Node, Path>> mapB = isoS.third.second; //.second.second;
		for (Edge e : proj1.first.target.edges) {
			Arr<Node, Path> x1 = mapA.get(e.name);
			Arr<Node, Path> x2 =  mapB.of(x1.arr);
			//List<String> yyy2 = new LinkedList<>();
		//	yyy2.add(x1.src);
			//if (!isoS.second.first.isId(x1)) {
			//	yyy2.addAll(x2.arr.path);				
		//	}
			yyy.add(new Pair<>(e.name, x2.arr.asList()));
		}
		
//		System.out.println(xxx);
//		System.out.println(yyy);
//		System.out.println(proj1.first.target);
//		System.out.println(q1.getSource());
		Mapping xyz = new Mapping(name + "iso1",proj1.first.target, q1.getSource(),  xxx, yyy);
		
		Mapping newproj = Mapping.compose(name + "_delta", proj1.first, xyz);
		env.mappings.put(name + "_delta", newproj);

		map1 =  union1.third.second.second;
		//Fn<Path, Arr<Node, Path>> map2 = isoT.third.second;
		xxx = new LinkedList<>();
		yyy = new LinkedList<>();
		for (Node n : union1.first.target.nodes) {
			Node x1 = map1.get(n.string);
//			String x2 =  map2.of(x1);
			xxx.add(new Pair<>(n.string, x1.string));
		}
		
		mapA =  union1.third.third.second;
		mapB = isoT.third.second;
		for (Edge e : union1.first.target.edges) {
			Arr<Node, Path> x1 = mapA.get(e.name);
			Arr<Node, Path> x2 =  mapB.of(x1.arr);
//			List<String> yyy2 = new LinkedList<>();
//			yyy2.add(x1.src);
//			if (!isoS.second.first.isId(x1)) {
//				yyy2.add(x2);				
//			}
			yyy.add(new Pair<>(e.name, x2.arr.asList()));
		}
		xyz = new Mapping(name + "iso2",union1.first.target, q2.getTarget(),  xxx, yyy);
		
		Mapping newunion = Mapping.compose(name + "_sigma", union1.first, xyz);
		env.mappings.put(name + "_sigma", newunion);
	
	return new Query(name, newproj, join1.first, newunion);		

	}

	@Override
	public JPanel join() {
		return null;
	}

	@Override
	public JPanel json() {
		return null;
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
