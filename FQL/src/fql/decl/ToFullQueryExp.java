package fql.decl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fql.Pair;
import fql.Triple;
import fql.decl.FullQueryExp.Comp;
import fql.decl.FullQueryExp.Delta;
import fql.decl.FullQueryExp.FullQueryExpVisitor;
import fql.decl.FullQueryExp.Match;
import fql.decl.FullQueryExp.Pi;
import fql.decl.FullQueryExp.Sigma;
import fql.decl.FullQueryExp.Var;

public class ToFullQueryExp implements
		FullQueryExpVisitor<FullQueryExp, FQLProgram> {

	@Override
	public FullQueryExp visit(FQLProgram env, Delta e) {
		return new FullQueryExp.Delta(e.f);
	}

	@Override
	public FullQueryExp visit(FQLProgram env, Sigma e) {
		return new FullQueryExp.Sigma(e.f);
	}

	@Override
	public FullQueryExp visit(FQLProgram env, Pi e) {
		return new FullQueryExp.Pi(e.f);
	}

	@Override
	public FullQueryExp visit(FQLProgram env, Comp e) {
		return new FullQueryExp.Comp(e.l.accept(env, this), e.r.accept(env,
				this));
	}

	@Override
	public FullQueryExp visit(FQLProgram env, Var e) {
		return env.full_queries.get(e.v).accept(env, this);
	}

	@Override
	public FullQueryExp visit(FQLProgram env, Match e) {
		try {
			SigExp.Const s = e.src.typeOf(env).toConst(env);
			SigExp.Const t = e.dst.typeOf(env).toConst(env);

			Pair<Map<Set<Pair<String, String>>, String>, Map<Set<Pair<String, String>>, String>> xxx = computeEqCs(
					s, t, e.rel);
			Map<Set<Pair<String, String>>, String> node_map = xxx.first;
			Map<Set<Pair<String, String>>, String> attr_map = xxx.second;

			Set<Pair<List<String>, List<String>>> eqs = new HashSet<>();
			Set<Triple<String, String, String>> arrows = new HashSet<>();
			Set<String> nodes = new HashSet<>();
			Set<Triple<String, String, String>> attrs = new HashSet<>();
			List<Pair<String, String>> inj1Node = new LinkedList<>();
			List<Pair<String, String>> inj1Attrs = new LinkedList<>();
			List<Pair<String, String>> inj2Node = new LinkedList<>();
			List<Pair<String, String>> inj2Attrs = new LinkedList<>();
			List<Pair<String, List<String>>> inj2Arrows = new LinkedList<>();
			List<Pair<String, List<String>>> inj1Arrows = new LinkedList<>();

			for (Triple<String, String, String> att : s.attrs) {
				String eqc = lookupAttr("left", att, attr_map);
				attrs.add(new Triple<>(eqc, lookupNode("left", att.second,
						node_map), att.third));
				inj1Attrs.add(new Pair<>(att.first, eqc));
			}
			for (Triple<String, String, String> att : t.attrs) {
				String eqc = lookupAttr("right", att, attr_map);
				attrs.add(new Triple<>(eqc, lookupNode("right", att.second,
						node_map), att.third));
				inj2Attrs.add(new Pair<>(att.first, eqc));
			}

			for (String n : s.nodes) {
				String eqc = lookupNode("left", n, node_map);
				nodes.add(eqc);
				inj1Node.add(new Pair<>(n, eqc));
			}
			for (String n : t.nodes) {
				String eqc = lookupNode("right", n, node_map);
				nodes.add(eqc);
				inj2Node.add(new Pair<>(n, eqc));
			}

			for (Triple<String, String, String> n : s.arrows) {
				String eqc1 = lookupNode("left", n.second, node_map);
				String eqc2 = lookupNode("left", n.third, node_map);
				arrows.add(new Triple<>("left_" + n.first, eqc1, eqc2));
				List<String> l = new LinkedList<>();
				l.add(eqc1);
				l.add("left_" + n.first);
				inj1Arrows.add(new Pair<>(n.first, l));
			}
			for (Triple<String, String, String> n : t.arrows) {
				String eqc1 = lookupNode("right", n.second, node_map);
				String eqc2 = lookupNode("right", n.third, node_map);
				arrows.add(new Triple<>("right_" + n.first, eqc1, eqc2));
				List<String> l = new LinkedList<>();
				l.add(eqc1);
				l.add("right_" + n.first);
				inj2Arrows.add(new Pair<>(n.first, l));
			}

			for (Pair<List<String>, List<String>> eq : s.eqs) {
				List<String> lhs = new LinkedList<>();
				lhs.add(lookupNode("left", eq.first.get(0), node_map));
				for (int i = 1; i < eq.first.size(); i++) {
					lhs.add("left_" + eq.first.get(i));
				}
				List<String> rhs = new LinkedList<>();
				rhs.add(lookupNode("left", eq.second.get(0), node_map));
				for (int i = 1; i < eq.second.size(); i++) {
					rhs.add("left_" + eq.second.get(i));
				}
				eqs.add(new Pair<>(lhs, rhs));
			}
			for (Pair<List<String>, List<String>> eq : t.eqs) {
				List<String> lhs = new LinkedList<>();
				lhs.add(lookupNode("right", eq.first.get(0), node_map));
				for (int i = 1; i < eq.first.size(); i++) {
					lhs.add("right_" + eq.first.get(i));
				}
				List<String> rhs = new LinkedList<>();
				rhs.add(lookupNode("right", eq.second.get(0), node_map));
				for (int i = 1; i < eq.second.size(); i++) {
					rhs.add("right_" + eq.second.get(i));
				}
				eqs.add(new Pair<>(lhs, rhs));
			}

			SigExp.Const x = new SigExp.Const(new LinkedList<>(nodes), new LinkedList<>(attrs), new LinkedList<>(arrows), new LinkedList<>(eqs));
			MapExp.Const inj1 = new MapExp.Const(inj1Node, inj1Attrs,
					inj1Arrows, s, x);
			MapExp.Const inj2 = new MapExp.Const(inj2Node, inj2Attrs,
					inj2Arrows, t, x);
			// System.out.println("X X X" + x);
			// System.out.println("inj1" + inj1);
			// System.out.println("inj2" + inj2);

			if (e.kind.equals("delta sigma forward")) {
				FullQueryExp q = new FullQueryExp.Comp(new FullQueryExp.Sigma(
						inj2), new FullQueryExp.Delta(inj1));
				// System.out.println(q);
				return q;
			} else if (e.kind.equals("delta pi forward")) {
				FullQueryExp q = new FullQueryExp.Comp(
						new FullQueryExp.Pi(inj2), new FullQueryExp.Delta(inj1));
				// System.out.println(q);
				return q;
			} else if (e.kind.equals("delta sigma backward")) {
				FullQueryExp q = new FullQueryExp.Comp(new FullQueryExp.Sigma(
						inj1), new FullQueryExp.Delta(inj2));
				// System.out.println(q);
				return q;
			} else if (e.kind.equals("delta pi backward")) {
				FullQueryExp q = new FullQueryExp.Comp(
						new FullQueryExp.Pi(inj1), new FullQueryExp.Delta(inj2));
				// System.out.println(q);
				return q;
			}
			throw new RuntimeException("Unknown kind: " + e.kind);

		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RuntimeException(ex.getLocalizedMessage());
		}
	}

	private Pair<Map<Set<Pair<String, String>>, String>, Map<Set<Pair<String, String>>, String>> computeEqCs(
			fql.decl.SigExp.Const s, fql.decl.SigExp.Const t,
			Set<Pair<String, String>> rel) {

		Map<Set<Pair<String, String>>, String> node_map = new HashMap<>();
		Map<Set<Pair<String, String>>, String> attr_map = new HashMap<>();

		Set<Set<Pair<String, String>>> nodeEqcs = new HashSet<>();
		Set<Set<Pair<String, String>>> attEqcs = new HashSet<>();

		for (Pair<String, String> p : rel) {
			Triple<String, String, String> st = lookup(s.attrs, p.first);
			Triple<String, String, String> tt = lookup(t.attrs, p.second);
			Set<Pair<String, String>> xxx = new HashSet<>();
			xxx.add(new Pair<>("left", st.first));
			xxx.add(new Pair<>("right", tt.first));
			attEqcs.add(xxx);

			Set<Pair<String, String>> yyy = new HashSet<>();
			yyy.add(new Pair<>("left", st.second));
			yyy.add(new Pair<>("right", tt.second));
			nodeEqcs.add(yyy);
		}

//		System.out.println("before merge");
		System.out.println(nodeEqcs);
		System.out.println(attEqcs);
		mergeEqcs(nodeEqcs);
		mergeEqcs(attEqcs);
	//	System.out.println("after merge");
		System.out.println(nodeEqcs);
		System.out.println(attEqcs);

		int count = 0;
		for (Set<Pair<String, String>> k : nodeEqcs) {
			node_map.put(k, "match_node" + count++);
		}
		count = 0;
		for (Set<Pair<String, String>> k : attEqcs) {
			attr_map.put(k, "match_att" + count++);
		}

		return new Pair<>(node_map, attr_map);
	}

	private void mergeEqcs(Set<Set<Pair<String, String>>> nodeEqcs) {

		for (;;) {
			Set<Pair<String, String>> x = null;
			Set<Pair<String, String>> y = null;
			lbl: for (Set<Pair<String, String>> k : nodeEqcs) {
				for (Set<Pair<String, String>> v : nodeEqcs) {
					if (k == v) {
						continue;
					}
					if (haveCommon(k, v)) {
						//System.out.println("Common " + k + " and " + v);
						x = k;
						y = v;
						break lbl;
					}
				}
			}

			if (x == null && y == null) {
				return;
			}
			//System.out.println("adding all " + y + " to " + x);
			x.addAll(y);
			nodeEqcs.remove(y);
			x = null;
			y = null;
		}
	}

	private boolean haveCommon(Set<Pair<String, String>> k,
			Set<Pair<String, String>> v) {
		for (Pair<String, String> kk : k) {
			for (Pair<String, String> vv : v) {
				if (kk.equals(vv)) {
					return true;
				}
			}
		}
		return false;
	}

	private Triple<String, String, String> lookup(
			List<Triple<String, String, String>> attrs, String first) {
		for (Triple<String, String, String> k : attrs) {
			if (k.first.equals(first)) {
				return k;
			}
		}
		throw new RuntimeException();
	}

	private String lookupAttr(String pre, Triple<String, String, String> att,
			Map<Set<Pair<String, String>>, String> attr_map) {
		for (Set<Pair<String, String>> k : attr_map.keySet()) {
			if (k.contains(new Pair<>(pre, att.first))) {
				return attr_map.get(k);
			}
		}
		return pre + "_" + att.first;

	}

	private String lookupNode(String pre, String n,
			Map<Set<Pair<String, String>>, String> node_map) {
		System.out.println("looking up " + n + " in " + node_map);
		for (Set<Pair<String, String>> k : node_map.keySet()) {
			if (k.contains(new Pair<>(pre, n))) {
				return node_map.get(k);
			}
		}
		System.out.println("no hit");
		return pre + "_" + n;
	}

}
