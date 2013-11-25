package fql.decl;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
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

import edu.uci.ics.jung.algorithms.layout.ISOMLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import fql.FQLException;
import fql.Fn;
import fql.Pair;
import fql.Quad;
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
import fql.decl.QueryExp.Comp;
import fql.decl.QueryExp.Const;
import fql.decl.QueryExp.QueryExpVisitor;
import fql.decl.QueryExp.Var;

/**
 * 
 * @author ryan
 * 
 *         Queries, and composition.
 */
public class Query {

	public static Query toQuery(FQLProgram env, QueryExp q) {
		return q.accept(env, new ToQueryVisitor());
	}

	public Mapping project, join, union;

	public Signature getSource() {
		return project.target;
	}

	public Signature getTarget() {
		return union.target;
	}

	public Query(Mapping project, Mapping join, Mapping union)
			throws FQLException {
		if (!project.source.equals(join.source)) {
			throw new FQLException("Ill-typed: \nProject source: "
					+ project.source + "\nJoin source: " + join.source);
		}
		if (!join.target.equals(union.source)) {
			throw new FQLException("Ill-typed: \nJoin target: " + join.target
					+ "\nUnion source: " + union.source);
		}
		this.project = project;
		this.join = join;
		this.union = union;
		join.okForPi();
		union.okForSigma();
	}

	// public Query(String name, QueryExp d) throws FQLException {
	// this.name = name;
	// switch (d.kind) {
	// case COMPOSE :
	/*
	 * isId = false; Query m1 = env.getQuery(d.q1); Query m2 =
	 * env.getQuery(d.q2); // System.out.println("composing " + m1 + " and " +
	 * m2); // System.out.println("m1src " + m1.getSource() + " and " +
	 * m2.getTarget()); //
	 * System.out.println(m2.getTarget().equals(m1.getSource())); if
	 * (!m2.getTarget().equals(m1.getSource())) { throw new
	 * FQLException("Ill-typed: " + d.name); } Query q = Query.compose(env,
	 * name, m1, m2); // System.out.println("result " + q);
	 * 
	 * if (!q.getSource().equals(m2.getSource())) { throw new
	 * FQLException("Ill-typed: " + d.name + " " + q.getSource() + " and " +
	 * m2.getSource()); } if (!q.getTarget().equals(m1.getTarget())) { throw new
	 * FQLException("Ill-typed: " + d.name + " " + q.getTarget() + " and " +
	 * m1.getTarget()); } this.project = q.project; this.join = q.join;
	 * this.union = q.union; this.name = q.name; join.okForPi();
	 * union.okForSigma(); break; // throw new RuntimeException(); case ID :
	 * isId = true; Signature s = env.getSchema(d.schema); project = new
	 * Mapping(env, s); join = new Mapping(env, s); union = new Mapping(env, s);
	 * break;
	 * 
	 * case QUERY : // // F : S' -> S // G : S' -> S'' // H : S'' -> T isId =
	 * false; project = env.getMapping(d.project); join =
	 * env.getMapping(d.join); union = env.getMapping(d.union); join.okForPi();
	 * union.okForSigma(); if (!project.source.equals(join.source) ||
	 * !join.target.equals(union.source)) { throw new FQLException("Ill-typed: "
	 * + d); } break;
	 * 
	 * default: throw new RuntimeException("d.kind"); } }
	 */

	// boolean isId;

	public JPanel view() throws FQLException {
		JPanel p = new JPanel(new GridLayout(3, 1));
		p.setBorder(BorderFactory.createEmptyBorder());
		JPanel q = project.view();
		q.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createLineBorder(Color.BLACK), "Delta"));
		// + project.name + " : " + project.target.name0 + " -> "
		// + project.source.name0));
		p.add(q);

		q = join.view();
		q.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createLineBorder(Color.BLACK), "Pi")); // +
																		// join.name
		// + " : " + join.source.name0 + " -> "
		// + join.target.name0));
		p.add(q);

		q = union.view();
		q.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createLineBorder(Color.BLACK), "Sigma"));
		// + union.name + " : " + union.source.name0 + " -> "
		// + union.target.name0));
		p.add(q);

		return p;
	}

	@Override
	public String toString() {
		// String s = "query " + name + " : " + getSource().name0 + " -> " +
		// getTarget().name0 + " = ";
		// if (isId) {
		// return s + "id " + getSource().name0;
		// }
		throw new RuntimeException();
		// return "delta " + project + " pi " + join + " sigma " + union;
	}

	public static Map<String, Set<Object[]>> convert0(Instance theinstance) {
		Map<String, Set<Object[]>> ret = new HashMap<String, Set<Object[]>>();
		for (Entry<String, Set<Pair<Object, Object>>> k : theinstance.data
				.entrySet()) {
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

	public Graph<String, String> legend() {
		Graph<String, String> ret = new DirectedSparseMultigraph<String, String>();

		/*
		 * ret.addVertex(getSource().name0); ret.addVertex(getTarget().name0);
		 * ret.addVertex(join.source.name0); ret.addVertex(union.source.name0);
		 * 
		 * ret.addEdge(project.name, join.source.name0, getSource().name0);
		 * ret.addEdge(join.name, join.source.name0, join.target.name0);
		 * ret.addEdge(union.name, join.target.name0, union.target.name0);
		 */
		return ret;
	}

	public JPanel lowerComp2(final Environment env) {
		// Layout<V, E>, BasicVisualizationServer<V,E>
		// Layout<String, String> layout = new FRLayout<>(sgv);
		// Layout<String, String> layout = new KKLayout(sgv);
		// Layout<String, String> layout = new SpringLayout(sgv);

		Layout<String, String> layout = new ISOMLayout<String, String>(legend());
		// Layout<String, String> layout = new CircleLayout(sgv);
		layout.setSize(new Dimension(500, 100));
		// layout.setLocation(getSource().name0, new Point2D(0,0));
		VisualizationViewer<String, String> vv = new VisualizationViewer<String, String>(
				layout);
		vv.setPreferredSize(new Dimension(500, 100));
		// Setup up a new vertex to paint transformer...
		// Transformer<String, Paint> vertexPaint = new Transformer<String,
		// Paint>() {
		// public Paint transform(String i) {
		// return which(i);
		// }
		//
		// private Color which(String t) {
		// return env.colors.get(t);
		// }
		// };
		DefaultModalGraphMouse<String, String> gm = new DefaultModalGraphMouse<>();
		gm.setMode(ModalGraphMouse.Mode.TRANSFORMING);
		vv.setGraphMouse(gm);
		gm.setMode(Mode.PICKING);

		vv.getRenderContext().setEdgeLabelTransformer(
				new ToStringLabeller<String>());
		vv.getRenderContext().setVertexLabelTransformer(
				new ToStringLabeller<String>());

		// vv.getRenderContext().setVertexFillPaintTransformer(vertexPaint);
		// vv.getRenderContext().setEdgeStrokeTransformer(edgeStrokeTransformer);

		// vv.getRenderer().getVertexRenderer().
		// vv.getRenderContext().setLabelOffset(20);
		// vv.getRenderer().getVertexLabelRenderer().setPosition(Position.CNTR);

		return vv;
	}

	public JPanel lowerComp(final Environment env) {
		JPanel pan = new JPanel(new GridLayout(1, 4));
		/*
		 * JLabel l1 = new JLabel(getSource().name0 + "    <-----");
		 * l1.setBackground(env.colors.get(getSource().name0));
		 * l1.setOpaque(true); l1.setHorizontalAlignment(SwingConstants.CENTER);
		 * pan.add(l1);
		 * 
		 * 
		 * l1 = new JLabel(join.source.name0+ "    ----->");
		 * l1.setBackground(env.colors.get(join.source.name0));
		 * l1.setOpaque(true); l1.setHorizontalAlignment(SwingConstants.CENTER);
		 * pan.add(l1);
		 * 
		 * l1 = new JLabel(union.source.name0+ "    ----->");
		 * l1.setBackground(env.colors.get(union.source.name0));
		 * l1.setOpaque(true); l1.setHorizontalAlignment(SwingConstants.CENTER);
		 * pan.add(l1);
		 * 
		 * l1 = new JLabel(getTarget().name0);
		 * l1.setBackground(env.colors.get(getTarget().name0));
		 * l1.setOpaque(true); l1.setHorizontalAlignment(SwingConstants.CENTER);
		 * pan.add(l1);
		 * 
		 * // pan.setMa`
		 */
		return pan;
	}

	/**
	 * Implements composition at the semantic level
	 */
	public static <ObjS, ArrowS, ObjB, ArrowB, ObjA, ArrowA, ObjT, ArrowT, ObjD, ArrowD, ObjC, ArrowC, ObjU, ArrowU> SemQuery<Triple<Triple<ObjB, Triple<ObjA, ObjD, ObjT>, Arr<ObjA, ArrowA>>, Pair<ObjD, Value<Triple<ObjA, ObjD, ObjT>, Triple<ObjA, ObjD, ObjT>>>, Arr<Triple<ObjA, ObjD, ObjT>, Triple<Arr<ObjA, ArrowA>, Arr<ObjD, ArrowD>, Arr<ObjT, ArrowT>>>>, Pair<Arr<Triple<ObjB, Triple<ObjA, ObjD, ObjT>, Arr<ObjA, ArrowA>>, Pair<Arr<ObjB, ArrowB>, Arr<Triple<ObjA, ObjD, ObjT>, Triple<Arr<ObjA, ArrowA>, Arr<ObjD, ArrowD>, Arr<ObjT, ArrowT>>>>>, Arr<Pair<ObjD, Value<Triple<ObjA, ObjD, ObjT>, Triple<ObjA, ObjD, ObjT>>>, Arr<ObjD, ArrowD>>>, ObjS, ArrowS, Pair<ObjC, Value<Triple<ObjA, ObjD, ObjT>, Triple<ObjA, ObjD, ObjT>>>, Arr<ObjC, ArrowC>, ObjU, ArrowU> doComposition(/*
																																																																																																																																																																																																		 * Environment
																																																																																																																																																																																																		 * env
																																																																																																																																																																																																		 * ,
																																																																																																																																																																																																		 */
	FinFunctor<ObjB, ArrowB, ObjS, ArrowS> s,
			FinFunctor<ObjB, ArrowB, ObjA, ArrowA> f,
			FinFunctor<ObjA, ArrowA, ObjT, ArrowT> t,
			FinFunctor<ObjD, ArrowD, ObjT, ArrowT> u,
			FinFunctor<ObjD, ArrowD, ObjC, ArrowC> g,
			FinFunctor<ObjC, ArrowC, ObjU, ArrowU> v) throws FQLException {

		// name = name;

		// FinCat<ObjS, ArrowS> S = s.dstCat;
		FinCat<ObjB, ArrowB> B = s.srcCat;
		FinCat<ObjA, ArrowA> A = t.srcCat;
		FinCat<ObjT, ArrowT> T = t.dstCat;
		FinCat<ObjD, ArrowD> D = g.srcCat;
		if (B.attrs == null || A.attrs == null || T.attrs == null
				|| D.attrs == null) {
			throw new RuntimeException();
		}
		FinCat<ObjC, ArrowC> C = g.dstCat;
		// FinCat<ObjU, ArrowU> U = v.dstCat;

		Triple<FinCat<Triple<ObjA, ObjD, ObjT>, Triple<Arr<ObjA, ArrowA>, Arr<ObjD, ArrowD>, Arr<ObjT, ArrowT>>>, FinFunctor<Triple<ObjA, ObjD, ObjT>, Triple<Arr<ObjA, ArrowA>, Arr<ObjD, ArrowD>, Arr<ObjT, ArrowT>>, ObjA, ArrowA>, FinFunctor<Triple<ObjA, ObjD, ObjT>, Triple<Arr<ObjA, ArrowA>, Arr<ObjD, ArrowD>, Arr<ObjT, ArrowT>>, ObjD, ArrowD>> one = FDM
				.pullback(A, D, T, t, u);

		FinCat<Triple<ObjA, ObjD, ObjT>, Triple<Arr<ObjA, ArrowA>, Arr<ObjD, ArrowD>, Arr<ObjT, ArrowT>>> Aprime = one.first;
		FinFunctor<Triple<ObjA, ObjD, ObjT>, Triple<Arr<ObjA, ArrowA>, Arr<ObjD, ArrowD>, Arr<ObjT, ArrowT>>, ObjA, ArrowA> h = one.second;
		FinFunctor<Triple<ObjA, ObjD, ObjT>, Triple<Arr<ObjA, ArrowA>, Arr<ObjD, ArrowD>, Arr<ObjT, ArrowT>>, ObjD, ArrowD> k = one.third;

		// Attributes for Aprime, h, k
		int cnt = 0;
		Aprime.attrs = new LinkedList<>();
		h.am = new HashMap<>();
		k.am = new HashMap<>();
		for (Attribute<ObjA> a1 : A.attrs) {
			for (Attribute<ObjD> a2 : D.attrs) {
				Attribute<ObjT> l = t.am.get(a1);
				Attribute<ObjT> r = u.am.get(a2);
				if (l.equals(r)) {
					Attribute<Triple<ObjA, ObjD, ObjT>> toadd = new Attribute<>(
							"attr" + cnt++, new Triple<>(a1.source, a2.source,
									l.source), l.target);
					Aprime.attrs.add(toadd);
					h.am.put(toadd, a1);
					k.am.put(toadd, a2);
				}
			}
		}
		// attributes for Aprime, h, k

		CommaCat<ObjB, ArrowB, Triple<ObjA, ObjD, ObjT>, Triple<Arr<ObjA, ArrowA>, Arr<ObjD, ArrowD>, Arr<ObjT, ArrowT>>, ObjA, ArrowA> Bprime = new CommaCat<>(
				B, Aprime, A, f, h);
		FinFunctor<Triple<ObjB, Triple<ObjA, ObjD, ObjT>, Arr<ObjA, ArrowA>>, Pair<Arr<ObjB, ArrowB>, Arr<Triple<ObjA, ObjD, ObjT>, Triple<Arr<ObjA, ArrowA>, Arr<ObjD, ArrowD>, Arr<ObjT, ArrowT>>>>, ObjB, ArrowB> m = Bprime.projA;
		FinFunctor<Triple<ObjB, Triple<ObjA, ObjD, ObjT>, Arr<ObjA, ArrowA>>, Pair<Arr<ObjB, ArrowB>, Arr<Triple<ObjA, ObjD, ObjT>, Triple<Arr<ObjA, ArrowA>, Arr<ObjD, ArrowD>, Arr<ObjT, ArrowT>>>>, Triple<ObjA, ObjD, ObjT>, Triple<Arr<ObjA, ArrowA>, Arr<ObjD, ArrowD>, Arr<ObjT, ArrowT>>> r = Bprime.projB;

		// attributes for Bprime, m, r
		Bprime.attrs = new LinkedList<>();
		m.am = new HashMap<>();
		r.am = new HashMap<>();
		int xnc = 0;
		for (Attribute<Triple<ObjA, ObjD, ObjT>> att : Aprime.attrs) {
			Attribute<ObjA> ap = h.am.get(att);
			for (Attribute<ObjB> kk : f.am.keySet()) {
				if (f.am.get(kk).equals(ap)) {
					Attribute<Triple<ObjB, Triple<ObjA, ObjD, ObjT>, Arr<ObjA, ArrowA>>> toadd = new Attribute<>(
							"att" + xnc++, new Triple<>(kk.source, att.source,
									A.id(ap.source)), ap.target);
					Bprime.attrs.add(toadd);
					m.am.put(toadd, kk);
					r.am.put(toadd, att);
				}
			}
		}
		// attributes for Bprime, m, r

		Inst<ObjD, ArrowD, Triple<ObjA, ObjD, ObjT>, Triple<ObjA, ObjD, ObjT>> du = FDM
				.degrothendieck(k);
		Inst<ObjC, ArrowC, Triple<ObjA, ObjD, ObjT>, Triple<ObjA, ObjD, ObjT>> piresult = FDM
				.pi(g, du);
		FinFunctor<Pair<ObjC, Value<Triple<ObjA, ObjD, ObjT>, Triple<ObjA, ObjD, ObjT>>>, Arr<ObjC, ArrowC>, ObjC, ArrowC> w = FDM
				.grothendieck(piresult);
		FinCat<Pair<ObjC, Value<Triple<ObjA, ObjD, ObjT>, Triple<ObjA, ObjD, ObjT>>>, Arr<ObjC, ArrowC>> M = w.srcCat;

		// attributes for M, w
		int aname = 0;
		M.attrs = new LinkedList<>();
		w.am = new HashMap<>();
		for (Pair<ObjC, Value<Triple<ObjA, ObjD, ObjT>, Triple<ObjA, ObjD, ObjT>>> x : M.objects) {
			ObjC y = w.applyO(x);
			for (Attribute<ObjC> a : C.attrs) {
				if (a.source.equals(y)) {
					Attribute<Pair<ObjC, Value<Triple<ObjA, ObjD, ObjT>, Triple<ObjA, ObjD, ObjT>>>> a0 = new Attribute<>(
							a.name + "_" + aname++, x, a.target);
					M.attrs.add(a0);
					w.am.put(a0, a);
				}
			}
		}
		// attributes for M, w

		Inst<ObjD, ArrowD, Triple<ObjA, ObjD, ObjT>, Triple<ObjA, ObjD, ObjT>> deltaresult = FDM
				.delta(g, piresult);
		FinFunctor<Pair<ObjD, Value<Triple<ObjA, ObjD, ObjT>, Triple<ObjA, ObjD, ObjT>>>, Arr<ObjD, ArrowD>, ObjD, ArrowD> w0 = FDM
				.grothendieck(deltaresult);
		FinCat<Pair<ObjD, Value<Triple<ObjA, ObjD, ObjT>, Triple<ObjA, ObjD, ObjT>>>, Arr<ObjD, ArrowD>> Dprime = w0.srcCat;

		SetFunTrans<ObjD, ArrowD, Triple<ObjA, ObjD, ObjT>, Triple<ObjA, ObjD, ObjT>> epsilonresult = FDM
				.epsilon(g, deltaresult, du);

		// System.out.println("--------------------------");
		// System.out.println("k' src is");
		// System.out.println(k.srcCat);
		// System.out.println("integraldu's srcCat is");
		// FinFunctor<Triple<ObjA, ObjD, ObjT>, Triple<Arr<ObjA, ArrowA>,
		// Arr<ObjD, ArrowD>, Arr<ObjT, ArrowT>>, ObjD, ArrowD> kkk = one.third;
		// FinFunctor<Pair<ObjD, Value<Triple<ObjA, ObjD, ObjT>, Triple<ObjA,
		// ObjD, ObjT>>>, Arr<ObjD, ArrowD>, ObjD, ArrowD> xxx =
		// FDM.grothendieck(du);
		// System.out.println(xxx.srcCat);
		// System.out.println("deltapiu is");
		// System.out.println(deltaresult);
		// System.out.println("epsilon is");
		// System.out.println(epsilonresult);
		// System.out.println("--------------------------");

		FinFunctor<Pair<ObjD, Value<Triple<ObjA, ObjD, ObjT>, Triple<ObjA, ObjD, ObjT>>>, Arr<ObjD, ArrowD>, Pair<ObjD, Value<Triple<ObjA, ObjD, ObjT>, Triple<ObjA, ObjD, ObjT>>>, Arr<ObjD, ArrowD>> e0 = FDM
				.grothendieck(epsilonresult);

		FinFunctor<Pair<ObjD, Value<Triple<ObjA, ObjD, ObjT>, Triple<ObjA, ObjD, ObjT>>>, Arr<ObjD, ArrowD>, Pair<ObjC, Value<Triple<ObjA, ObjD, ObjT>, Triple<ObjA, ObjD, ObjT>>>, Arr<ObjC, ArrowC>> q = FDM
				.makeG(w0.srcCat, w.srcCat, g);

		FinCat<Pair<ObjD, Value<Triple<ObjA, ObjD, ObjT>, Triple<ObjA, ObjD, ObjT>>>, Arr<ObjD, ArrowD>> xxx = q.srcCat;
		FinCat<Pair<ObjC, Value<Triple<ObjA, ObjD, ObjT>, Triple<ObjA, ObjD, ObjT>>>, Arr<ObjC, ArrowC>> yyy = q.dstCat;

		// ///////////////////////////////////

		Map<Pair<ObjD, Value<Triple<ObjA, ObjD, ObjT>, Triple<ObjA, ObjD, ObjT>>>, Triple<ObjA, ObjD, ObjT>> pairToTripleBe1 = new HashMap<>();
		for (Pair<ObjD, Value<Triple<ObjA, ObjD, ObjT>, Triple<ObjA, ObjD, ObjT>>> c : e0.dstCat.objects) {
			if (!c.second.which.equals(VALUETYPE.CHOICE)) {
				throw new RuntimeException("Bad target type " + c.second + " "
						+ c.second.which);
			}
			pairToTripleBe1.put(c, c.second.tagCargo.x);
		}
		Map<Arr<Pair<ObjD, Value<Triple<ObjA, ObjD, ObjT>, Triple<ObjA, ObjD, ObjT>>>, Arr<ObjD, ArrowD>>, Arr<Triple<ObjA, ObjD, ObjT>, Triple<Arr<ObjA, ArrowA>, Arr<ObjD, ArrowD>, Arr<ObjT, ArrowT>>>> pairToTripleBe2 = new HashMap<>();
		for (Arr<Pair<ObjD, Value<Triple<ObjA, ObjD, ObjT>, Triple<ObjA, ObjD, ObjT>>>, Arr<ObjD, ArrowD>> c : e0.dstCat.arrows) {
			Triple<ObjA, ObjD, ObjT> arrsrc = pairToTripleBe1.get(c.src);
			Triple<ObjA, ObjD, ObjT> arrdst = pairToTripleBe1.get(c.dst);
			for (Arr<Triple<ObjA, ObjD, ObjT>, Triple<Arr<ObjA, ArrowA>, Arr<ObjD, ArrowD>, Arr<ObjT, ArrowT>>> cand : k.srcCat
					.hom(arrsrc, arrdst)) {
				if (cand.arr.second.equals(c.arr)) {
					pairToTripleBe2.put(c, cand);
				}
			}
			if (pairToTripleBe2.get(c) == null) {
				throw new RuntimeException("no candidate");
			}
		}

		FinFunctor<Pair<ObjD, Value<Triple<ObjA, ObjD, ObjT>, Triple<ObjA, ObjD, ObjT>>>, Arr<ObjD, ArrowD>, Triple<ObjA, ObjD, ObjT>, Triple<Arr<ObjA, ArrowA>, Arr<ObjD, ArrowD>, Arr<ObjT, ArrowT>>> pairToTripleBe = new FinFunctor<>(
				pairToTripleBe1, pairToTripleBe2, e0.dstCat, k.srcCat);

		FinFunctor<Pair<ObjD, Value<Triple<ObjA, ObjD, ObjT>, Triple<ObjA, ObjD, ObjT>>>, Arr<ObjD, ArrowD>, Triple<ObjA, ObjD, ObjT>, Triple<Arr<ObjA, ArrowA>, Arr<ObjD, ArrowD>, Arr<ObjT, ArrowT>>> e = FinFunctor
				.compose(e0, pairToTripleBe);

		// attributes for Dprime, e
		aname = 0;
		Dprime.attrs = new LinkedList<>();
		e.am = new HashMap<>();
		for (Pair<ObjD, Value<Triple<ObjA, ObjD, ObjT>, Triple<ObjA, ObjD, ObjT>>> x : Dprime.objects) {
			Triple<ObjA, ObjD, ObjT> y = e.applyO(x);
			for (Attribute<Triple<ObjA, ObjD, ObjT>> a : Aprime.attrs) {
				if (a.source.equals(y)) {
					Attribute<Pair<ObjD, Value<Triple<ObjA, ObjD, ObjT>, Triple<ObjA, ObjD, ObjT>>>> a0 = new Attribute<>(
							a.name + "_" + aname++, x, a.target);
					Dprime.attrs.add(a0);
					e.am.put(a0, a);
				}
			}
		}
		// attributes for Dprime, e

		CommaCat<Triple<ObjB, Triple<ObjA, ObjD, ObjT>, Arr<ObjA, ArrowA>>, Pair<Arr<ObjB, ArrowB>, Arr<Triple<ObjA, ObjD, ObjT>, Triple<Arr<ObjA, ArrowA>, Arr<ObjD, ArrowD>, Arr<ObjT, ArrowT>>>>, Pair<ObjD, Value<Triple<ObjA, ObjD, ObjT>, Triple<ObjA, ObjD, ObjT>>>, Arr<ObjD, ArrowD>, Triple<ObjA, ObjD, ObjT>, Triple<Arr<ObjA, ArrowA>, Arr<ObjD, ArrowD>, Arr<ObjT, ArrowT>>> N = new CommaCat<>(
				Bprime, Dprime, Aprime, r, e);
		FinFunctor<Triple<Triple<ObjB, Triple<ObjA, ObjD, ObjT>, Arr<ObjA, ArrowA>>, Pair<ObjD, Value<Triple<ObjA, ObjD, ObjT>, Triple<ObjA, ObjD, ObjT>>>, Arr<Triple<ObjA, ObjD, ObjT>, Triple<Arr<ObjA, ArrowA>, Arr<ObjD, ArrowD>, Arr<ObjT, ArrowT>>>>, Pair<Arr<Triple<ObjB, Triple<ObjA, ObjD, ObjT>, Arr<ObjA, ArrowA>>, Pair<Arr<ObjB, ArrowB>, Arr<Triple<ObjA, ObjD, ObjT>, Triple<Arr<ObjA, ArrowA>, Arr<ObjD, ArrowD>, Arr<ObjT, ArrowT>>>>>, Arr<Pair<ObjD, Value<Triple<ObjA, ObjD, ObjT>, Triple<ObjA, ObjD, ObjT>>>, Arr<ObjD, ArrowD>>>, Triple<ObjB, Triple<ObjA, ObjD, ObjT>, Arr<ObjA, ArrowA>>, Pair<Arr<ObjB, ArrowB>, Arr<Triple<ObjA, ObjD, ObjT>, Triple<Arr<ObjA, ArrowA>, Arr<ObjD, ArrowD>, Arr<ObjT, ArrowT>>>>> n = N.projA;
		FinFunctor<Triple<Triple<ObjB, Triple<ObjA, ObjD, ObjT>, Arr<ObjA, ArrowA>>, Pair<ObjD, Value<Triple<ObjA, ObjD, ObjT>, Triple<ObjA, ObjD, ObjT>>>, Arr<Triple<ObjA, ObjD, ObjT>, Triple<Arr<ObjA, ArrowA>, Arr<ObjD, ArrowD>, Arr<ObjT, ArrowT>>>>, Pair<Arr<Triple<ObjB, Triple<ObjA, ObjD, ObjT>, Arr<ObjA, ArrowA>>, Pair<Arr<ObjB, ArrowB>, Arr<Triple<ObjA, ObjD, ObjT>, Triple<Arr<ObjA, ArrowA>, Arr<ObjD, ArrowD>, Arr<ObjT, ArrowT>>>>>, Arr<Pair<ObjD, Value<Triple<ObjA, ObjD, ObjT>, Triple<ObjA, ObjD, ObjT>>>, Arr<ObjD, ArrowD>>>, Pair<ObjD, Value<Triple<ObjA, ObjD, ObjT>, Triple<ObjA, ObjD, ObjT>>>, Arr<ObjD, ArrowD>> p = N.projB;

		// attributes for N, n, p
		N.attrs = new LinkedList<>();
		n.am = new HashMap<>();
		p.am = new HashMap<>();
		xnc = 0;
		for (Attribute<Pair<ObjD, Value<Triple<ObjA, ObjD, ObjT>, Triple<ObjA, ObjD, ObjT>>>> att : Dprime.attrs) {
			Attribute<Triple<ObjA, ObjD, ObjT>> ap = e.am.get(att);
			for (Attribute<Triple<ObjB, Triple<ObjA, ObjD, ObjT>, Arr<ObjA, ArrowA>>> kk : r.am
					.keySet()) {
				if (r.am.get(kk).equals(ap)) {
					Attribute<Triple<Triple<ObjB, Triple<ObjA, ObjD, ObjT>, Arr<ObjA, ArrowA>>, Pair<ObjD, Value<Triple<ObjA, ObjD, ObjT>, Triple<ObjA, ObjD, ObjT>>>, Arr<Triple<ObjA, ObjD, ObjT>, Triple<Arr<ObjA, ArrowA>, Arr<ObjD, ArrowD>, Arr<ObjT, ArrowT>>>>> toadd = new Attribute<>(
							"att" + xnc++, new Triple<>(kk.source, att.source,
									Aprime.id(ap.source)), ap.target);
					N.attrs.add(toadd);
					n.am.put(toadd, kk);
					p.am.put(toadd, att);
				}
			}
		}
		// attributes for Bprime, m, r

		// attributes for q
		q.am = new HashMap<>();
		for (Attribute<Pair<ObjD, Value<Triple<ObjA, ObjD, ObjT>, Triple<ObjA, ObjD, ObjT>>>> a : xxx.attrs) {
			for (Attribute<Pair<ObjC, Value<Triple<ObjA, ObjD, ObjT>, Triple<ObjA, ObjD, ObjT>>>> a0 : yyy.attrs) {
				Pair<ObjD, Value<Triple<ObjA, ObjD, ObjT>, Triple<ObjA, ObjD, ObjT>>> ax = a.source;
				Pair<ObjC, Value<Triple<ObjA, ObjD, ObjT>, Triple<ObjA, ObjD, ObjT>>> a0x = a0.source;
				if (ax.second.equals(a0x.second)) {
					if (q.am.get(a) != null) {
						throw new RuntimeException();
					}
					q.am.put(a, a0);
				}
			}
		}
		//

		FinFunctor<Pair<ObjC, Value<Triple<ObjA, ObjD, ObjT>, Triple<ObjA, ObjD, ObjT>>>, Arr<ObjC, ArrowC>, ObjU, ArrowU> union = FinFunctor
				.compose(w, v);

		FinFunctor<Triple<Triple<ObjB, Triple<ObjA, ObjD, ObjT>, Arr<ObjA, ArrowA>>, Pair<ObjD, Value<Triple<ObjA, ObjD, ObjT>, Triple<ObjA, ObjD, ObjT>>>, Arr<Triple<ObjA, ObjD, ObjT>, Triple<Arr<ObjA, ArrowA>, Arr<ObjD, ArrowD>, Arr<ObjT, ArrowT>>>>, Pair<Arr<Triple<ObjB, Triple<ObjA, ObjD, ObjT>, Arr<ObjA, ArrowA>>, Pair<Arr<ObjB, ArrowB>, Arr<Triple<ObjA, ObjD, ObjT>, Triple<Arr<ObjA, ArrowA>, Arr<ObjD, ArrowD>, Arr<ObjT, ArrowT>>>>>, Arr<Pair<ObjD, Value<Triple<ObjA, ObjD, ObjT>, Triple<ObjA, ObjD, ObjT>>>, Arr<ObjD, ArrowD>>>, Pair<ObjC, Value<Triple<ObjA, ObjD, ObjT>, Triple<ObjA, ObjD, ObjT>>>, Arr<ObjC, ArrowC>> join = FinFunctor
				.compose(p, q);

		FinFunctor<Triple<Triple<ObjB, Triple<ObjA, ObjD, ObjT>, Arr<ObjA, ArrowA>>, Pair<ObjD, Value<Triple<ObjA, ObjD, ObjT>, Triple<ObjA, ObjD, ObjT>>>, Arr<Triple<ObjA, ObjD, ObjT>, Triple<Arr<ObjA, ArrowA>, Arr<ObjD, ArrowD>, Arr<ObjT, ArrowT>>>>, Pair<Arr<Triple<ObjB, Triple<ObjA, ObjD, ObjT>, Arr<ObjA, ArrowA>>, Pair<Arr<ObjB, ArrowB>, Arr<Triple<ObjA, ObjD, ObjT>, Triple<Arr<ObjA, ArrowA>, Arr<ObjD, ArrowD>, Arr<ObjT, ArrowT>>>>>, Arr<Pair<ObjD, Value<Triple<ObjA, ObjD, ObjT>, Triple<ObjA, ObjD, ObjT>>>, Arr<ObjD, ArrowD>>>, ObjS, ArrowS> project = FinFunctor
				.compose(FinFunctor.compose(n, m), s);

		SemQuery<Triple<Triple<ObjB, Triple<ObjA, ObjD, ObjT>, Arr<ObjA, ArrowA>>, Pair<ObjD, Value<Triple<ObjA, ObjD, ObjT>, Triple<ObjA, ObjD, ObjT>>>, Arr<Triple<ObjA, ObjD, ObjT>, Triple<Arr<ObjA, ArrowA>, Arr<ObjD, ArrowD>, Arr<ObjT, ArrowT>>>>, Pair<Arr<Triple<ObjB, Triple<ObjA, ObjD, ObjT>, Arr<ObjA, ArrowA>>, Pair<Arr<ObjB, ArrowB>, Arr<Triple<ObjA, ObjD, ObjT>, Triple<Arr<ObjA, ArrowA>, Arr<ObjD, ArrowD>, Arr<ObjT, ArrowT>>>>>, Arr<Pair<ObjD, Value<Triple<ObjA, ObjD, ObjT>, Triple<ObjA, ObjD, ObjT>>>, Arr<ObjD, ArrowD>>>, ObjS, ArrowS, Pair<ObjC, Value<Triple<ObjA, ObjD, ObjT>, Triple<ObjA, ObjD, ObjT>>>, Arr<ObjC, ArrowC>, ObjU, ArrowU> ret = new SemQuery<>(
				project, join, union);

		// if (DEBUG.INTERMEDIATE == Intermediate.ALL) {
		// env.signatures.put("Aprime", Aprime.toSig("Aprime").first);
		// env.signatures.put("Bprime", Bprime.toSig("Bprime").first);
		// env.signatures.put("N", N.toSig("Nprime").first);
		// env.signatures.put("Dprime", Dprime.toSig("Dprime").first);
		// env.signatures.put("M", M.toSig("MMM").first);
		// }

		return ret;
	}

	static <ObjD, ArrowD, ObjA, ArrowA, ObjT, ArrowT> Arr<Triple<ObjA, ObjD, ObjT>, Triple<Arr<ObjA, ArrowA>, Arr<ObjD, ArrowD>, Arr<ObjT, ArrowT>>> find2X(
			Arr<Pair<ObjD, String>, Arr<ObjD, ArrowD>> c,
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
	public static Query compose(Map<String, Type> types, Query q2, Query q1)
			throws FQLException {
		Mapping s0 = q1.project;
		Mapping f0 = q1.join;
		Mapping t0 = q1.union;
		Mapping u0 = q2.project;
		Mapping g0 = q2.join;
		Mapping v0 = q2.union;

		// System.out.println("name is " + name);

		Triple<FinFunctor<Node, Path, Node, Path>, Pair<FinCat<Node, Path>, Fn<Path, Arr<Node, Path>>>, Pair<FinCat<Node, Path>, Fn<Path, Arr<Node, Path>>>> isoS = s0
				.toFunctor2();
		Triple<FinFunctor<Node, Path, Node, Path>, Pair<FinCat<Node, Path>, Fn<Path, Arr<Node, Path>>>, Pair<FinCat<Node, Path>, Fn<Path, Arr<Node, Path>>>> isoT = v0
				.toFunctor2();

		// env, name, isoS.second.second.first.first,
		// isoS.second.second.second.first,

		SemQuery<Triple<Triple<Node, Triple<Node, Node, Node>, Arr<Node, Path>>, Pair<Node, Value<Triple<Node, Node, Node>, Triple<Node, Node, Node>>>, Arr<Triple<Node, Node, Node>, Triple<Arr<Node, Path>, Arr<Node, Path>, Arr<Node, Path>>>>, Pair<Arr<Triple<Node, Triple<Node, Node, Node>, Arr<Node, Path>>, Pair<Arr<Node, Path>, Arr<Triple<Node, Node, Node>, Triple<Arr<Node, Path>, Arr<Node, Path>, Arr<Node, Path>>>>>, Arr<Pair<Node, Value<Triple<Node, Node, Node>, Triple<Node, Node, Node>>>, Arr<Node, Path>>>, Node, Path, Pair<Node, Value<Triple<Node, Node, Node>, Triple<Node, Node, Node>>>, Arr<Node, Path>, Node, Path> ret = doComposition(
				isoS.first, f0.toFunctor2().first, t0.toFunctor2().first,
				u0.toFunctor2().first, g0.toFunctor2().first, isoT.first);

		Triple<Mapping, Quad<Signature, Pair<Map<Triple<Triple<Node, Triple<Node, Node, Node>, Arr<Node, Path>>, Pair<Node, Value<Triple<Node, Node, Node>, Triple<Node, Node, Node>>>, Arr<Triple<Node, Node, Node>, Triple<Arr<Node, Path>, Arr<Node, Path>, Arr<Node, Path>>>>, String>, Map<String, Triple<Triple<Node, Triple<Node, Node, Node>, Arr<Node, Path>>, Pair<Node, Value<Triple<Node, Node, Node>, Triple<Node, Node, Node>>>, Arr<Triple<Node, Node, Node>, Triple<Arr<Node, Path>, Arr<Node, Path>, Arr<Node, Path>>>>>>, Pair<Map<Arr<Triple<Triple<Node, Triple<Node, Node, Node>, Arr<Node, Path>>, Pair<Node, Value<Triple<Node, Node, Node>, Triple<Node, Node, Node>>>, Arr<Triple<Node, Node, Node>, Triple<Arr<Node, Path>, Arr<Node, Path>, Arr<Node, Path>>>>, Pair<Arr<Triple<Node, Triple<Node, Node, Node>, Arr<Node, Path>>, Pair<Arr<Node, Path>, Arr<Triple<Node, Node, Node>, Triple<Arr<Node, Path>, Arr<Node, Path>, Arr<Node, Path>>>>>, Arr<Pair<Node, Value<Triple<Node, Node, Node>, Triple<Node, Node, Node>>>, Arr<Node, Path>>>>, String>, Map<String, Arr<Triple<Triple<Node, Triple<Node, Node, Node>, Arr<Node, Path>>, Pair<Node, Value<Triple<Node, Node, Node>, Triple<Node, Node, Node>>>, Arr<Triple<Node, Node, Node>, Triple<Arr<Node, Path>, Arr<Node, Path>, Arr<Node, Path>>>>, Pair<Arr<Triple<Node, Triple<Node, Node, Node>, Arr<Node, Path>>, Pair<Arr<Node, Path>, Arr<Triple<Node, Node, Node>, Triple<Arr<Node, Path>, Arr<Node, Path>, Arr<Node, Path>>>>>, Arr<Pair<Node, Value<Triple<Node, Node, Node>, Triple<Node, Node, Node>>>, Arr<Node, Path>>>>>>, Pair<Map<Attribute<Triple<Triple<Node, Triple<Node, Node, Node>, Arr<Node, Path>>, Pair<Node, Value<Triple<Node, Node, Node>, Triple<Node, Node, Node>>>, Arr<Triple<Node, Node, Node>, Triple<Arr<Node, Path>, Arr<Node, Path>, Arr<Node, Path>>>>>, String>, Map<String, Attribute<Triple<Triple<Node, Triple<Node, Node, Node>, Arr<Node, Path>>, Pair<Node, Value<Triple<Node, Node, Node>, Triple<Node, Node, Node>>>, Arr<Triple<Node, Node, Node>, Triple<Arr<Node, Path>, Arr<Node, Path>, Arr<Node, Path>>>>>>>>, Quad<Signature, Pair<Map<Node, String>, Map<String, Node>>, Pair<Map<Arr<Node, Path>, String>, Map<String, Arr<Node, Path>>>, Pair<Map<Attribute<Node>, String>, Map<String, Attribute<Node>>>>> proj1 = ret.project
				.toMapping(types);
		Triple<Mapping, Quad<Signature, Pair<Map<Triple<Triple<Node, Triple<Node, Node, Node>, Arr<Node, Path>>, Pair<Node, Value<Triple<Node, Node, Node>, Triple<Node, Node, Node>>>, Arr<Triple<Node, Node, Node>, Triple<Arr<Node, Path>, Arr<Node, Path>, Arr<Node, Path>>>>, String>, Map<String, Triple<Triple<Node, Triple<Node, Node, Node>, Arr<Node, Path>>, Pair<Node, Value<Triple<Node, Node, Node>, Triple<Node, Node, Node>>>, Arr<Triple<Node, Node, Node>, Triple<Arr<Node, Path>, Arr<Node, Path>, Arr<Node, Path>>>>>>, Pair<Map<Arr<Triple<Triple<Node, Triple<Node, Node, Node>, Arr<Node, Path>>, Pair<Node, Value<Triple<Node, Node, Node>, Triple<Node, Node, Node>>>, Arr<Triple<Node, Node, Node>, Triple<Arr<Node, Path>, Arr<Node, Path>, Arr<Node, Path>>>>, Pair<Arr<Triple<Node, Triple<Node, Node, Node>, Arr<Node, Path>>, Pair<Arr<Node, Path>, Arr<Triple<Node, Node, Node>, Triple<Arr<Node, Path>, Arr<Node, Path>, Arr<Node, Path>>>>>, Arr<Pair<Node, Value<Triple<Node, Node, Node>, Triple<Node, Node, Node>>>, Arr<Node, Path>>>>, String>, Map<String, Arr<Triple<Triple<Node, Triple<Node, Node, Node>, Arr<Node, Path>>, Pair<Node, Value<Triple<Node, Node, Node>, Triple<Node, Node, Node>>>, Arr<Triple<Node, Node, Node>, Triple<Arr<Node, Path>, Arr<Node, Path>, Arr<Node, Path>>>>, Pair<Arr<Triple<Node, Triple<Node, Node, Node>, Arr<Node, Path>>, Pair<Arr<Node, Path>, Arr<Triple<Node, Node, Node>, Triple<Arr<Node, Path>, Arr<Node, Path>, Arr<Node, Path>>>>>, Arr<Pair<Node, Value<Triple<Node, Node, Node>, Triple<Node, Node, Node>>>, Arr<Node, Path>>>>>>, Pair<Map<Attribute<Triple<Triple<Node, Triple<Node, Node, Node>, Arr<Node, Path>>, Pair<Node, Value<Triple<Node, Node, Node>, Triple<Node, Node, Node>>>, Arr<Triple<Node, Node, Node>, Triple<Arr<Node, Path>, Arr<Node, Path>, Arr<Node, Path>>>>>, String>, Map<String, Attribute<Triple<Triple<Node, Triple<Node, Node, Node>, Arr<Node, Path>>, Pair<Node, Value<Triple<Node, Node, Node>, Triple<Node, Node, Node>>>, Arr<Triple<Node, Node, Node>, Triple<Arr<Node, Path>, Arr<Node, Path>, Arr<Node, Path>>>>>>>>, Quad<Signature, Pair<Map<Pair<Node, Value<Triple<Node, Node, Node>, Triple<Node, Node, Node>>>, String>, Map<String, Pair<Node, Value<Triple<Node, Node, Node>, Triple<Node, Node, Node>>>>>, Pair<Map<Arr<Pair<Node, Value<Triple<Node, Node, Node>, Triple<Node, Node, Node>>>, Arr<Node, Path>>, String>, Map<String, Arr<Pair<Node, Value<Triple<Node, Node, Node>, Triple<Node, Node, Node>>>, Arr<Node, Path>>>>, Pair<Map<Attribute<Pair<Node, Value<Triple<Node, Node, Node>, Triple<Node, Node, Node>>>>, String>, Map<String, Attribute<Pair<Node, Value<Triple<Node, Node, Node>, Triple<Node, Node, Node>>>>>>>> join1 = ret.join
				.toMapping(types);
		Triple<Mapping, Quad<Signature, Pair<Map<Pair<Node, Value<Triple<Node, Node, Node>, Triple<Node, Node, Node>>>, String>, Map<String, Pair<Node, Value<Triple<Node, Node, Node>, Triple<Node, Node, Node>>>>>, Pair<Map<Arr<Pair<Node, Value<Triple<Node, Node, Node>, Triple<Node, Node, Node>>>, Arr<Node, Path>>, String>, Map<String, Arr<Pair<Node, Value<Triple<Node, Node, Node>, Triple<Node, Node, Node>>>, Arr<Node, Path>>>>, Pair<Map<Attribute<Pair<Node, Value<Triple<Node, Node, Node>, Triple<Node, Node, Node>>>>, String>, Map<String, Attribute<Pair<Node, Value<Triple<Node, Node, Node>, Triple<Node, Node, Node>>>>>>>, Quad<Signature, Pair<Map<Node, String>, Map<String, Node>>, Pair<Map<Arr<Node, Path>, String>, Map<String, Arr<Node, Path>>>, Pair<Map<Attribute<Node>, String>, Map<String, Attribute<Node>>>>> union1 = ret.union
				.toMapping(types);
		/*
		 * env.signatures.put(name + "_B", proj1.second.first);
		 * env.signatures.put(name + "_A", join1.third.first);
		 * env.mappings.put(name + "_pi", join1.first);
		 */
		Map<String, Node> map1 = proj1.third.second.second;
		List<Pair<String, String>> xxx = new LinkedList<>();
		List<Pair<String, List<String>>> yyy = new LinkedList<>();
		for (Node n : proj1.first.target.nodes) {
			Node x1 = map1.get(n.string);
			xxx.add(new Pair<>(n.string, x1.string));
		}

		Map<String, Attribute<Node>> map0 = proj1.third.fourth.second;
		List<Pair<String, String>> zzz = new LinkedList<>();
		for (Attribute<Node> n : proj1.first.target.attrs) {
			Attribute<Node> x1 = map0.get(n.name);
			if (x1 == null) {
				throw new RuntimeException("Cannot find " + n.name + " in "
						+ map0);
			}
			zzz.add(new Pair<>(n.name, x1.name));
		}

		Map<String, Arr<Node, Path>> mapA = proj1.third.third.second;
		Fn<Path, Arr<Node, Path>> mapB = isoS.third.second; // .second.second;
		for (Edge e : proj1.first.target.edges) {
			Arr<Node, Path> x1 = mapA.get(e.name);
			Arr<Node, Path> x2 = mapB.of(x1.arr);
			// List<String> yyy2 = new LinkedList<>();
			// yyy2.add(x1.src);
			// if (!isoS.second.first.isId(x1)) {
			// yyy2.addAll(x2.arr.path);
			// }
			yyy.add(new Pair<>(e.name, x2.arr.asList()));
		}

		// System.out.println(xxx);
		// System.out.println(yyy);
		// System.out.println(proj1.first.target);
		// System.out.println(q1.getSource());
		Mapping xyz = new Mapping(/* name + "iso1", */proj1.first.target,
				q1.getSource(), xxx, zzz, yyy);

		// System.out.println("qdelta from " + proj1.first + "\nand\n" + xyz);
		Mapping newproj = Mapping.compose(/* name + "_delta", */proj1.first, xyz);
		// env.mappings.put(name + "_delta", newproj);

		map1 = union1.third.second.second;
		// Fn<Path, Arr<Node, Path>> map2 = isoT.third.second;
		xxx = new LinkedList<>();
		yyy = new LinkedList<>();
		for (Node n : union1.first.target.nodes) {
			Node x1 = map1.get(n.string);
			// String x2 = map2.of(x1);
			xxx.add(new Pair<>(n.string, x1.string));
		}
		map0 = union1.third.fourth.second;
		zzz = new LinkedList<>();
		for (Attribute<Node> n : union1.first.target.attrs) {
			Attribute<Node> x1 = map0.get(n.name);
			zzz.add(new Pair<>(n.name, x1.name));
		}

		mapA = union1.third.third.second;
		mapB = isoT.third.second;
		for (Edge e : union1.first.target.edges) {
			Arr<Node, Path> x1 = mapA.get(e.name);
			Arr<Node, Path> x2 = mapB.of(x1.arr);
			// List<String> yyy2 = new LinkedList<>();
			// yyy2.add(x1.src);
			// if (!isoS.second.first.isId(x1)) {
			// yyy2.add(x2);
			// }
			yyy.add(new Pair<>(e.name, x2.arr.asList()));
		}
		xyz = new Mapping(/* name + "iso2", */union1.first.target,
				q2.getTarget(), xxx, zzz, yyy);

		Mapping newunion = Mapping.compose(/* name + "_sigma", */union1.first,
				xyz);
		// env.mappings.put(name + "_sigma", newunion);

		return new Query(newproj, join1.first, newunion);

	}

	public static class ToQueryVisitor implements
			QueryExpVisitor<Query, FQLProgram> {

		List<String> seen = new LinkedList<>();

		@Override
		public Query visit(FQLProgram env, Const e) {
			Mapping d = e.delta.toMap(env);
			Mapping s = e.sigma.toMap(env);
			Mapping p = e.pi.toMap(env);
			try {
				return new Query(d, p, s);
			} catch (FQLException fe) {
				throw new RuntimeException(fe.getLocalizedMessage());
			}
		}

		@Override
		public Query visit(FQLProgram env, Comp e) {
			List<String> x = new LinkedList<>(seen);
			Query l = e.l.accept(env, this);
			seen = x;
			Query r = e.r.accept(env, this);
			seen = x;
			try {
				return compose(env.enums, r, l); // yes, backwards
			} catch (FQLException fe) {
				throw new RuntimeException(fe.getLocalizedMessage());
			}
		}

		@Override
		public Query visit(FQLProgram env, Var e) {
			if (seen.contains(e.v)) {
				throw new RuntimeException("Circular query: " + e.v);
			}
			seen.add(e.v);
			QueryExp x = env.queries.get(e.v);
			if (x == null) {
				throw new RuntimeException("Unknown query: " + e.v);
			}
			return x.accept(env, this);
		}

		public JPanel text(FQLProgram p) {
			JTextArea ta = new JTextArea(p.toString());
			JPanel tap = new JPanel(new GridLayout(1, 1));
			ta.setBorder(BorderFactory.createEmptyBorder());
			tap.setBorder(BorderFactory.createEmptyBorder());
			ta.setWrapStyleWord(true);
			ta.setLineWrap(true);
			JScrollPane xxx = new JScrollPane(ta);
			tap.add(xxx);
			return tap;
		}
	}

}
