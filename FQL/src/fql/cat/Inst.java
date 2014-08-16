package fql.cat;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import fql.DEBUG;
import fql.FQLException;
import fql.Fn;
import fql.Pair;
import fql.decl.Attribute;
import fql.decl.Edge;
import fql.decl.Instance;
import fql.decl.Mapping;
import fql.decl.Node;
import fql.decl.Path;
import fql.decl.Signature;
import fql.decl.Transform;

/**
 * 
 * @author ryan
 * 
 *         Finite instances - functors to set.
 * 
 * @param <Obj>
 *            type of objects
 * @param <Arrow>
 *            type of arrows
 * @param <Y>
 *            carrier for set objects
 * @param <X>
 *            carrier for set arrows
 */
public class Inst<Obj, Arrow, Y, X> {

	public static Pair<Transform, Transform> iso(Instance a, Instance b) {
		
		if (a.thesig.nodes.size() == 0) {
			Transform t1 = new Transform(a, b, new LinkedList<Pair<String, List<Pair<Object, Object>>>>());
			Transform t2 = new Transform(b, a, new LinkedList<Pair<String, List<Pair<Object, Object>>>>());
			return new Pair<>(t1, t2);
		}
		
		Map<Node, List<LinkedHashMap<Pair<Object, Object>, Pair<Object, Object>>>> m = new HashMap<>();
		for (Node n : a.thesig.nodes) {
			if (a.data.get(n.string).size() != b.data.get(n.string).size()) {
				return null;
			}
			List<LinkedHashMap<Pair<Object, Object>, Pair<Object, Object>>> bijs = bijections(
					new LinkedList<>(a.data.get(n.string)), new LinkedList<>(
							b.data.get(n.string)));
			Iterator<LinkedHashMap<Pair<Object, Object>, Pair<Object, Object>>> it = bijs
					.iterator();
			while (it.hasNext()) {
				LinkedHashMap<Pair<Object, Object>, Pair<Object, Object>> bij = it
						.next();
				if (!preservesAttrs(bij, a, b, n)) {
					it.remove();
				}
			}
			m.put(n, bijs);
		}
		List<LinkedHashMap<Node, LinkedHashMap<Pair<Object, Object>, Pair<Object, Object>>>> m0 = homomorphs(m);
		for (LinkedHashMap<Node, LinkedHashMap<Pair<Object, Object>, Pair<Object, Object>>> k : m0) {
			List<Pair<String, List<Pair<Object, Object>>>> data1 = new LinkedList<>();
			List<Pair<String, List<Pair<Object, Object>>>> data2 = new LinkedList<>();
			for (Node n : a.thesig.nodes) {
				LinkedHashMap<Pair<Object, Object>, Pair<Object, Object>> v = k
						.get(n);
				List<Pair<Object, Object>> d1 = new LinkedList<>();
				List<Pair<Object, Object>> d2 = new LinkedList<>();
				for (Entry<Pair<Object, Object>, Pair<Object, Object>> u : v
						.entrySet()) {
					d1.add(new Pair<>(u.getKey().first, u.getValue().first));
					d2.add(new Pair<>(u.getValue().first, u.getKey().first));
				}
				data1.add(new Pair<>(n.string, d1));
				data2.add(new Pair<>(n.string, d2));
			}
			try {
				Transform t1 = new Transform(a, b, data1);
				Transform t2 = new Transform(b, a, data2);
				Instance b0 = t1.apply(a);
				Instance a0 = t2.apply(b);
				if (a.equals(a0) && b.equals(b0)) {
					return new Pair<>(t1, t2);
				}
			} catch (Exception re) {
			}
		}

		return null;
	}

	private static <X, Y> Y lookup(Set<Pair<X, Y>> set, X x) {
		for (Pair<X, Y> k : set) {
			if (k.first.equals(x)) {
				return k.second;
			}
		}
		throw new RuntimeException("Cannot find " + x + " in " + set);
	}

	private static boolean preservesAttrs(
			Map<Pair<Object, Object>, Pair<Object, Object>> bij, Instance a,
			Instance b, Node n) {

		for (Attribute<Node> att : a.thesig.attrsFor(n)) {
			for (Pair<Object, Object> id1 : a.data.get(n.string)) {
				Object oldAtt = lookup(a.data.get(att.name), id1.first);
				Pair<Object, Object> id2 = bij.get(id1);
				Object newAtt = lookup(b.data.get(att.name), id2.first);
				if (!oldAtt.equals(newAtt)) {
					return false;
				}
			}
		}

		return true;
	}

	public static Pair<Mapping, Mapping> iso(Signature a, Signature b)
			throws FQLException {
		List<Mapping> ls = bistuff(b, a);
		// System.out.println("ls " + ls);
		if (ls == null) {
			return null;
		}
		List<Mapping> rs = bistuff(a, b);
		// System.out.println("rs " + rs);
		if (rs == null) {
			return null;
		}

		Pair<FinCat<Node, Path>, Fn<Path, Arr<Node, Path>>> a1 = a
				.toCategory2();
		Pair<FinCat<Node, Path>, Fn<Path, Arr<Node, Path>>> b1 = b
				.toCategory2();

		/*
		 * for (Node aa : a1.first.objects) { for (Node bb : a1.first.objects) {
		 * System.out.println("hom " + aa + " " + bb + " " + a1.first.hom(aa,
		 * bb).size()); } } System.out.println("**"); for (Node aa :
		 * b1.first.objects) { for (Node bb : b1.first.objects) {
		 * System.out.println("hom " + aa + " " + bb + " " + b1.first.hom(aa,
		 * bb).size()); } }
		 */

		for (Mapping l : ls) {
			inner: for (Mapping r : rs) {
				for (Node n : a.nodes) {
					if (!n.equals(r.nm.get(l.nm.get(n)))) {
						continue inner;
					}
				}
				for (Node m : b.nodes) {
					if (!m.equals(l.nm.get(r.nm.get(m)))) {
						continue inner;
					}
				}
				for (Attribute<Node> n : a.attrs) {
					if (!n.equals(r.am.get(l.am.get(n)))) {
						continue inner;
					}
				}
				for (Attribute<Node> m : b.attrs) {
					if (!m.equals(l.am.get(r.am.get(m)))) {
						continue inner;
					}
				}
				for (Edge n : a.edges) {
					if (!a1.second.of(new Path(a, n)).equals(
							a1.second.of(r.appy(b, l.em.get(n))))) {
						continue inner;
					}
				}
				for (Edge m : b.edges) {
					if (!b1.second.of(new Path(b, m)).equals(
							b1.second.of(l.appy(b, r.em.get(m))))) {
						continue inner;
					}
				}
				return new Pair<>(l, r);
			}
		}

		return null;
	}

	public static List<Mapping> bistuff(Signature base, Signature exp)
			throws FQLException {

		if (base.nodes.size() != exp.nodes.size()) {
			return null;
		}
		if (base.attrs.size() != exp.attrs.size()) {
			return null;
		}

		Pair<FinCat<Node, Path>, Fn<Path, Arr<Node, Path>>> xxx = base
				.toCategory2();
		FinCat<Node, Path> base0 = xxx.first;
		Pair<FinCat<Node, Path>, Fn<Path, Arr<Node, Path>>> yyy = exp
				.toCategory2();
		FinCat<Node, Path> exp0 = yyy.first;

		if (xxx.first.arrows.size() != yyy.first.arrows.size()) {
			return null;
		}

		List<LinkedHashMap<Node, Node>> nms = bijections(exp.nodes, base.nodes);

		// System.out.println("nms " + nms);

		// List<Mapping> mappings = new LinkedList<>();

		// List<Triple<Map<Node, Node>, Map<Node, Attribute<Node>>, Map<Edge,
		// Path>>> temp = new LinkedList<>();

		// List<Map<Node, Pair<List<Map<Attribute<Node>, Attribute<Node>>>,
		// Map<Node, List<Map<Arr<Node, Path>, Arr<Node, Path>>>>>>> oml = new
		// LinkedList<>();
		List<Mapping> ret = new LinkedList<>();
		outmost: for (LinkedHashMap<Node, Node> nm : nms) {
			// System.out.println(nm);
			// Map<Node, Pair<List<Map<Attribute<Node>, Attribute<Node>>>,
			// Map<Node, List<Map<Arr<Node, Path>, Arr<Node, Path>>>>>> om
			// = new HashMap<>();
			Map<Pair<Node, Node>, List<LinkedHashMap<Arr<Node, Path>, Arr<Node, Path>>>> bijm = new HashMap<>();
			Map<Node, List<LinkedHashMap<Attribute<Node>, Attribute<Node>>>> atm = new HashMap<>();
			for (Node s : nm.keySet()) {
				List<Attribute<Node>> sa = exp.attrsFor(s);
				List<Attribute<Node>> ta = base.attrsFor(nm.get(s));
				if (sa.size() != ta.size()) {
					continue outmost;
				}
				List<LinkedHashMap<Attribute<Node>, Attribute<Node>>> am = bijections(
						sa, ta);
				Iterator<LinkedHashMap<Attribute<Node>, Attribute<Node>>> it = am
						.iterator();
				outer: while (it.hasNext()) {
					Map<Attribute<Node>, Attribute<Node>> k = it.next();
					for (Entry<Attribute<Node>, Attribute<Node>> v : k
							.entrySet()) {
						if (!v.getKey().target.equals(v.getValue().target)) {
							it.remove();
							continue outer;
						}
					}
				}
				if (am.isEmpty() && !sa.isEmpty()) {
					continue outmost;
				}
				atm.put(s, am);
				// System.out.println("am " + am);
				// Map<Pair<Node, Node>, List<Map<Arr<Node, Path>, Arr<Node,
				// Path>>>> bijm = new HashMap<>();
				Node sx = nm.get(s);
				for (Node t : nm.keySet()) {
					Set<Arr<Node, Path>> h1 = exp0.hom(s, t);
					Node st = nm.get(t);
					Set<Arr<Node, Path>> h2 = base0.hom(sx, st);
					if (h1.size() != h2.size()) {
						/*
						 * System.out.println("exp0 hom " + s + " " + t +
						 * " size " + h1.size());
						 * System.out.println("base0 hom " + sx + " " + st +
						 * " size " + h2.size());
						 */
						continue outmost;
					}
					List<LinkedHashMap<Arr<Node, Path>, Arr<Node, Path>>> bij = bijections(
							new LinkedList<>(h1), new LinkedList<>(h2));
					// System.out.println("hit " + s + " " + t + " " + bij);
					if (!bij.isEmpty()) {
						bijm.put(new Pair<>(s, t), bij);
					}
				}
				// om.put(s, new Pair<>(am, bijm));
			}

			// oml.add(om);
			// System.out.println("bijm " + bijm);
			List<LinkedHashMap<Pair<Node, Node>, LinkedHashMap<Arr<Node, Path>, Arr<Node, Path>>>> bijmX = homomorphs(bijm);
			// System.out.println("bijmX " + bijmX);
			// System.out.println("atm " + atm);
			List<LinkedHashMap<Node, LinkedHashMap<Attribute<Node>, Attribute<Node>>>> atmX = homomorphs(atm);
			// System.out.println("atmX " + atmX);

			List<LinkedHashMap<Arr<Node, Path>, Arr<Node, Path>>> bijmZ = new LinkedList<>();
			for (LinkedHashMap<Pair<Node, Node>, LinkedHashMap<Arr<Node, Path>, Arr<Node, Path>>> k : bijmX) {
				LinkedHashMap<Arr<Node, Path>, Arr<Node, Path>> bijmY = new LinkedHashMap<>();
				for (Entry<Pair<Node, Node>, LinkedHashMap<Arr<Node, Path>, Arr<Node, Path>>> v : k
						.entrySet()) {
					bijmY.putAll(v.getValue());
				}
				bijmZ.add(bijmY);
			}

			List<LinkedHashMap<Attribute<Node>, Attribute<Node>>> atmZ = new LinkedList<>();
			for (LinkedHashMap<Node, LinkedHashMap<Attribute<Node>, Attribute<Node>>> k : atmX) {
				LinkedHashMap<Attribute<Node>, Attribute<Node>> atmY = new LinkedHashMap<>();
				for (Entry<Node, LinkedHashMap<Attribute<Node>, Attribute<Node>>> v : k
						.entrySet()) {
					atmY.putAll(v.getValue());
				}
				atmZ.add(atmY);
			}
			if (exp.attrs.isEmpty()) {
				atmZ.add(new LinkedHashMap<Attribute<Node>, Attribute<Node>>());
			}
			if (exp.edges.isEmpty()) {
				bijmZ.add(new LinkedHashMap<Arr<Node, Path>, Arr<Node, Path>>());
			}

			// System.out.println("bijmZ " + bijmZ);
			// System.out.println("atmZ " + atmZ);

			for (Map<Arr<Node, Path>, Arr<Node, Path>> f : bijmZ) {
				LinkedHashMap<Edge, Path> em = new LinkedHashMap<>();
				for (Edge e : exp.edges) {
					// System.out.println("yyy.second.of " + yyy.second.of(new
					// Path(exp, e)));
					// System.out.println(f);
					em.put(e, f.get(yyy.second.of(new Path(exp, e))).arr);
				}
				for (LinkedHashMap<Attribute<Node>, Attribute<Node>> g : atmZ) {
					try {
						Mapping m = new Mapping(true, exp, base, nm, em, g);
						ret.add(m);
					} catch (FQLException fe) {
						// fe.printStackTrace();
					}
				}
			}
			// break;
		}

		return ret;
	}

	// I => J
	public static List<Transform> hom(Instance I, Instance J) {
		if (!I.thesig.equals(J.thesig)) {
			throw new RuntimeException();
		}

		List<Transform> ret = new LinkedList<>();

		Map<Node, List<LinkedHashMap<Pair<Object, Object>, Pair<Object, Object>>>> m = new HashMap<>();
		for (Node n : I.thesig.nodes) {
			LinkedList<Pair<Object, Object>> src = new LinkedList<>(
					I.data.get(n.string));
			LinkedList<Pair<Object, Object>> dst = new LinkedList<>(
					J.data.get(n.string));

			List<LinkedHashMap<Pair<Object, Object>, Pair<Object, Object>>> h = homomorphs(
					src, dst);
			m.put(n, h);
		}
		List<LinkedHashMap<Node, LinkedHashMap<Pair<Object, Object>, Pair<Object, Object>>>> map = homomorphs(m);

		for (LinkedHashMap<Node, LinkedHashMap<Pair<Object, Object>, Pair<Object, Object>>> t : map) {
			try {
				List<Pair<String, List<Pair<Object, Object>>>> l = new LinkedList<>();
				for (Entry<Node, LinkedHashMap<Pair<Object, Object>, Pair<Object, Object>>> u : t
						.entrySet()) {
					l.add(new Pair<>(u.getKey().string, conv(u.getValue())));
				}
				Transform tr = new Transform(I, J, l);
				ret.add(tr);
			} catch (Exception e) {
			}
		}

		// System.out.println("hom size on " + I + "\n" + J + "\n" +
		// ret.size());
		return ret;
	}

	private static List<Pair<Object, Object>> conv(
			LinkedHashMap<Pair<Object, Object>, Pair<Object, Object>> l) {
		List<Pair<Object, Object>> ret = new LinkedList<>();

		for (Entry<Pair<Object, Object>, Pair<Object, Object>> k : l.entrySet()) {
			ret.add(new Pair<>(k.getKey().first, k.getValue().first));
		}

		return ret;
	}

	// base^exp
	public static FinCat<Mapping, Map<Node, Path>> stuff(Signature base,
			Signature exp) throws FQLException {

		Pair<FinCat<Node, Path>, Fn<Path, Arr<Node, Path>>> xxx = base
				.toCategory2();
		FinCat<Node, Path> base0 = xxx.first;
		Pair<FinCat<Node, Path>, Fn<Path, Arr<Node, Path>>> yyy = exp
				.toCategory2();
		FinCat<Node, Path> exp0 = yyy.first;

		List<LinkedHashMap<Node, Node>> nms = homomorphs(exp.nodes, base.nodes);

		// System.out.println(nms);

		List<Mapping> mappings = new LinkedList<>();

		// List<Triple<Map<Node, Node>, Map<Node, Attribute<Node>>, Map<Edge,
		// Path>>> temp = new LinkedList<>();

		for (LinkedHashMap<Node, Node> nm : nms) {
			LinkedHashMap<Attribute<Node>, List<Attribute<Node>>> ams = new LinkedHashMap<>();
			for (Attribute<Node> a : exp.attrs) {
				ams.put(a, base.attrsFor(nm.get(a.source)));
			}
			// System.out.println("on " + nm + ": " + ams);

			LinkedHashMap<Edge, List<Path>> ems = new LinkedHashMap<>();
			for (Edge e : exp.edges) {
				Set<Arr<Node, Path>> s = base0.hom(nm.get(e.source),
						nm.get(e.target));
				List<Path> p = new LinkedList<>();
				for (Arr<Node, Path> sx : s) {
					p.add(sx.arr);
				}
				ems.put(e, p);
			}
			// System.out.println("on " + nm + ": " + ems);

			List<LinkedHashMap<Attribute<Node>, Attribute<Node>>> ams0 = homomorphs(ams);
			List<LinkedHashMap<Edge, Path>> ems0 = homomorphs(ems);

			/*if (exp.attrs.isEmpty()) {
				ams0.add(new LinkedHashMap<Attribute<Node>, Attribute<Node>>());
			}
			if (exp.edges.isEmpty()) {
				ems0.add(new LinkedHashMap<Edge, Path>());
			}*/

			for (LinkedHashMap<Attribute<Node>, Attribute<Node>> am : ams0) {
				for (LinkedHashMap<Edge, Path> em : ems0) {
					try {
						Mapping m = new Mapping(true, exp, base, nm, em, am);
						// System.out.println("*****" + m);
						mappings.add(m);
					} catch (Exception e) {
						// e.printStackTrace();
					}
				}
			}
		}

		// System.out.println(mappings);

		List<Arr<Mapping, Map<Node, Path>>> arrows = new LinkedList<>();

		for (Mapping s : mappings) {
			for (Mapping t : mappings) {
				Map<Node, List<Path>> map = new HashMap<>();
				for (Node n : exp.nodes) {
					List<Path> p = new LinkedList<>();
					for (Arr<Node, Path> k : base0
							.hom(s.nm.get(n), t.nm.get(n))) {
						p.add(k.arr);
					}
					map.put(n, p);
				}
				List<LinkedHashMap<Node, Path>> map0 = homomorphs(map);
				// System.out.println("on " + s + " and " + t
				// + " trans cands are " + map0);
				outer: for (Map<Node, Path> k : map0) {
					for (Node x : k.keySet()) {
						for (Node y : k.keySet()) {
							for (Arr<Node, Path> f : exp0.hom(x, y)) {
								// System.out.println("f " + f.arr.toLong());
								// System.out
								// .println("kgetx " + k.get(x).toLong());
								// System.out.println("tappy "
								// + t.appy(f.arr).toLong());
								Path lhs = Path.append(base, k.get(x),
										t.appy(exp, f.arr));
								Path rhs = Path.append(base, s.appy(exp, f.arr),
										k.get(y));
								// System.out.println("for " + k + " x " + x
								// + " y " + y + " lhs " + lhs + " rhs "
								// + rhs);
								if (!xxx.second.of(lhs).equals(
										xxx.second.of(rhs))) {
									continue outer;
								}
							}
						}
					}
					arrows.add(new Arr<>(k, s, t));
				}
			}
		}

		Map<Mapping, Arr<Mapping, Map<Node, Path>>> identities = new HashMap<>();
		for (Mapping m : mappings) {
			Map<Node, Path> map = new HashMap<>();
			for (Node n : m.source.nodes) {
				map.put(n, new Path(m.target, m.nm.get(n)));
			}
			Arr<Mapping, Map<Node, Path>> uuu = new Arr<>(map, m, m);
			identities.put(m, new Arr<>(map, m, m));
			if (!arrows.contains(uuu)) {
				arrows.add(uuu);
			}
		}
		// System.out.println("identities " + identities);
		Map<Pair<Arr<Mapping, Map<Node, Path>>, Arr<Mapping, Map<Node, Path>>>, Arr<Mapping, Map<Node, Path>>> composition = new HashMap<>();
		for (Arr<Mapping, Map<Node, Path>> a1 : arrows) {
			for (Arr<Mapping, Map<Node, Path>> a2 : arrows) {
				if (!a1.dst.equals(a2.src)) {
					continue;
				}
				Map<Node, Path> m = new HashMap<>();
				for (Node n : exp.nodes) {
					m.put(n, xxx.second.of(Path.append(base, a1.arr.get(n),
							a2.arr.get(n))).arr);
				}
				composition.put(new Pair<>(a1, a2),
						new Arr<>(m, a1.src, a2.dst));
			}
		}
		// System.out.println(composition);
		return new FinCat<>(mappings, arrows, composition, identities);
	}

	public static <Obj, Arrow, Y, X> List<LinkedHashMap<Obj, LinkedHashMap<Value<Y, X>, Value<Y, X>>>> morphsX(
			LinkedHashMap<Obj, List<LinkedHashMap<Value<Y, X>, Value<Y, X>>>> map) {
		List<LinkedHashMap<Obj, LinkedHashMap<Value<Y, X>, Value<Y, X>>>> ret = new LinkedList<>();

		List<Obj> A = new LinkedList<>(map.keySet());
		int[] sizes = new int[A.size()];
		for (int i = 0; i < A.size(); i++) {
			sizes[i] = map.get(A.get(i)).size();
		}

		if (A.size() == 0) {
			return ret;
		}

		int[] counters = new int[A.size() + 1];

		for (;;) {
			if (counters[A.size()] == 1) {
				break;
			}
			ret.add(make5(counters, A, map));
			inc5(counters, sizes);
		}

		return ret;
	}

	private static <Obj, Arrow, Y, X> LinkedHashMap<Obj, LinkedHashMap<Value<Y, X>, Value<Y, X>>> make5(
			int[] counters, List<Obj> A,
			LinkedHashMap<Obj, List<LinkedHashMap<Value<Y, X>, Value<Y, X>>>> B) {
		LinkedHashMap<Obj, LinkedHashMap<Value<Y, X>, Value<Y, X>>> ret = new LinkedHashMap<>();
		int i = 0;
		for (Obj x : A) {
			ret.put(x, B.get(x).get(counters[i++]));
		}
		return ret;
	}

	private static <Obj, Arrow, Y, X> void inc5(int[] counters, int[] sizes) {
		counters[0]++;
		for (int i = 0; i < counters.length - 1; i++) {
			if (counters[i] == sizes[i]) {
				counters[i] = 0;
				counters[i + 1]++;
			}
		}
	}

	public static <Obj, Arrow, Y, X> LinkedHashMap<Obj, List<LinkedHashMap<Value<Y, X>, Value<Y, X>>>> morphs2(
			Inst<Obj, Arrow, Y, X> i1, Inst<Obj, Arrow, Y, X> i2) {
		LinkedHashMap<Obj, List<LinkedHashMap<Value<Y, X>, Value<Y, X>>>> ret = new LinkedHashMap<>();

		for (Obj o : i1.cat.objects) {

			List<LinkedHashMap<Value<Y, X>, Value<Y, X>>> morphs3 = bijections(
					new LinkedList<>(i1.applyO(o)),
					new LinkedList<>(i2.applyO(o)));

			ret.put(o, morphs3);
		}

		return ret;
	}

	public static <X, Y> List<LinkedHashMap<X, Y>> homomorphs(Map<X, List<Y>> L) {
		List<LinkedHashMap<X, Y>> ret = new LinkedList<>();

		if (L.isEmpty()) {
			ret.add(new LinkedHashMap<X,Y>()); //TODO
			return ret;
		}
		for (Entry<X, List<Y>> k : L.entrySet()) {
			if (k.getValue().isEmpty()) {
				return ret;
			}
		}

		int[] counters = new int[L.keySet().size() + 1];
		int[] lengths = new int[L.keySet().size()];
		int i = 0;
		for (Entry<X, List<Y>> x : L.entrySet()) {
			lengths[i++] = x.getValue().size();
		}

		for (;;) {

			if (counters[L.keySet().size()] == 1) {
				break;
			}
			ret.add(make3(counters, L));
			inc3(counters, lengths);

		}

		return ret;
	}

	private static <X, Y> LinkedHashMap<X, Y> make3(int[] counters,
			Map<X, List<Y>> L) {
		LinkedHashMap<X, Y> ret = new LinkedHashMap<>();
		int i = 0;
		for (X x : L.keySet()) {
			ret.put(x, L.get(x).get(counters[i++]));
		}
		return ret;
	}

	private static <X, Y> void inc3(int[] counters, int[] lengths) {
		counters[0]++;
		for (int i = 0; i < counters.length - 1; i++) {
			if (counters[i] == lengths[i]) {
				counters[i] = 0;
				counters[i + 1]++;
			}
		}
	}

	public static <X, Y> List<LinkedHashMap<X, Y>> homomorphs(List<X> A,
			List<Y> B) {
		List<LinkedHashMap<X, Y>> ret = new LinkedList<>();

		if (A.size() == 0) {
			ret.add(new LinkedHashMap<X, Y>());
			return ret;
		}
		if (B.size() == 0) {
			return ret;
		}

		int[] counters = new int[A.size() + 1];

		for (;;) {
			if (counters[A.size()] == 1) {
				break;
			}
			ret.add(make2(counters, A, B));
			inc(counters, B.size());
		}

		return ret;
	}

	
	//

	public static <X> List<LinkedHashMap<X, X>> bijections(List<X> A, List<X> B) {
		List<LinkedHashMap<X, X>> ret = new LinkedList<>();

		// System.out.println("Expecting " + Math. + " morphs " + B.size() +
		// " and " + A.size());

		// if (B.size() == 0) {
		// throw new RuntimeException();
		// }

		if (A.size() != B.size()) {
			throw new RuntimeException();
		}

		if (A.size() == 0) {
			ret.add(new LinkedHashMap<X, X>());
			return ret;
		}

		List<Integer> seq = new LinkedList<>();
		for (int i = 0; i < A.size(); i++) {
			seq.add(i);
		}

		// System.out.println(seq);
		Collection<List<Integer>> xxx = new SetPermutations<Integer>()
				.permute(seq);
		// System.out.println(xxx);

		for (List<Integer> l : xxx) {
			LinkedHashMap<X, X> m = new LinkedHashMap<>();
			int j = 0;
			for (Integer i : l) {
				m.put(A.get(j), B.get(i));
				j++;
			}
			ret.add(m);
		}

		return ret;
	}

	private static <X, Y> LinkedHashMap<X, Y> make2(int[] counters, List<X> A,
			List<Y> B) {
		LinkedHashMap<X, Y> ret = new LinkedHashMap<>();
		int i = 0;
		for (X x : A) {
			ret.put(x, B.get(counters[i++]));
		}
		return ret;
	}

	private static void inc(int[] counters, int size) {
		counters[0]++;
		for (int i = 0; i < counters.length - 1; i++) {
			if (counters[i] == size) {
				counters[i] = 0;
				counters[i + 1]++;
			}
		}
	}

	@SuppressWarnings("unchecked")
	public static <Obj, Arrow, Y, X> Set<SetFunTrans<Obj, Arrow, Y, X>> morphs(
			Inst<Obj, Arrow, Y, X> i1, Inst<Obj, Arrow, Y, X> i2) {

		Set<SetFunTrans<Obj, Arrow, Y, X>> ret = new HashSet<>();

		LinkedHashMap<Obj, List<LinkedHashMap<Value<Y, X>, Value<Y, X>>>> x = morphs2(
				i1, i2);

		List<LinkedHashMap<Obj, LinkedHashMap<Value<Y, X>, Value<Y, X>>>> y = morphsX(x);

		for (LinkedHashMap<Obj, LinkedHashMap<Value<Y, X>, Value<Y, X>>> map : y) {
			try {
				Map<Obj, Map<Value<Y, X>, Value<Y, X>>> uuu = (Map<Obj, Map<Value<Y, X>, Value<Y, X>>>) ((Object) map);
				SetFunTrans<Obj, Arrow, Y, X> xxx = new SetFunTrans<Obj, Arrow, Y, X>(
						uuu, i1, i2);
				xxx.validate();
				ret.add(xxx);
			} catch (FQLException e) {
				System.out.println("Ignoring " + map);
			}
		}

		return ret;
	}

	Map<Obj, Set<Value<Y, X>>> objM;
	Map<Arr<Obj, Arrow>, Map<Value<Y, X>, Value<Y, X>>> arrM;
	public FinCat<Obj, Arrow> cat;

	public Inst(Map<Obj, Set<Value<Y, X>>> objM,
			Map<Arr<Obj, Arrow>, Map<Value<Y, X>, Value<Y, X>>> arrM,
			FinCat<Obj, Arrow> cat) {
		this.objM = objM;
		this.arrM = arrM;
		this.cat = cat;
		if (DEBUG.debug.VALIDATE) {
			validate();
		}
		// System.out.println("automorphisms are");
		// int i = 0;
		// for (SetFunTrans<Obj, Arrow, Y, X> s : morphs(this, this)) {
		// System.out.println(i++);
		// }
	}

	@Override
	public String toString() {
		return "SetFunctor [objM=\n" + objM + ",\narrM=" + arrM + "]";
	}

	public Set<Value<Y, X>> applyO(Object o) {
		return objM.get(o);
	}

	public Map<Value<Y, X>, Value<Y, X>> applyA(Arr<Obj, Arrow> a) {
		return arrM.get(a);
	}

	public void validate() {
		for (Obj o : cat.objects) {
			if (!objM.containsKey(o)) {
				throw new RuntimeException("Functor does not map " + o
						+ " \n in \n " + this);
			}
		}
		for (Arr<Obj, Arrow> a : cat.arrows) {
			if (!arrM.containsKey(a)) {
				throw new RuntimeException("Functor does not map " + a + this);
			}
			Set<Value<Y, X>> src = objM.get(a.src);
			Set<Value<Y, X>> dst = objM.get(a.dst);
			Map<Value<Y, X>, Value<Y, X>> f = arrM.get(a);
			for (Value<Y, X> src0 : src) {
				if (f.get(src0) == null) {
					throw new RuntimeException();
				}
				if (!dst.contains(f.get(src0))) {
					throw new RuntimeException();
				}
			}

			for (Value<Y, X> aa : f.keySet()) {
				Value<Y, X> bb = f.get(aa);
				if (!src.contains(aa)) {
					throw new RuntimeException();
				}
				if (!dst.contains(bb)) {
					throw new RuntimeException();
				}
			}

			for (Arr<Obj, Arrow> b : cat.arrows) {
				Arr<Obj, Arrow> c = cat.compose(a, b);
				if (c == null) {
					continue;
				}
				Map<Value<Y, X>, Value<Y, X>> a0 = arrM.get(a);
				Map<Value<Y, X>, Value<Y, X>> b0 = arrM.get(b);
				Map<Value<Y, X>, Value<Y, X>> c0 = arrM.get(c);
				if (!c0.equals(compose(a0, b0))) {
					throw new RuntimeException("Func does not preserve \n " + a
							+ "\n" + b + "\n" + c + "\n" + a0 + "\n" + b0
							+ "\n" + c0 + "\n" + compose(a0, b0) + "\n" + cat);
				}
			}
		}
	}

	private Map<Value<Y, X>, Value<Y, X>> compose(
			Map<Value<Y, X>, Value<Y, X>> f, Map<Value<Y, X>, Value<Y, X>> g) {
		Map<Value<Y, X>, Value<Y, X>> ret = new HashMap<>();
		for (Value<Y, X> s : f.keySet()) {
			ret.put(s, g.get(f.get(s)));
		}
		return ret;
	}

	/**
	 * Constructs a terminal (one element) instance
	 */
	public static <Obj, Arrow, Y> Inst<Obj, Arrow, Y, Obj> terminal(
			FinCat<Obj, Arrow> s) throws FQLException {

		Map<Obj, Set<Value<Y, Obj>>> ret1 = new HashMap<>();
		Map<Arr<Obj, Arrow>, Map<Value<Y, Obj>, Value<Y, Obj>>> ret2 = new HashMap<>();

		for (Obj o : s.objects) {
			Set<Value<Y, Obj>> x = new HashSet<>();
			x.add(new Value<Y, Obj>(o));
			ret1.put(o, x);
		}

		for (Arr<Obj, Arrow> a : s.arrows) {
			Map<Value<Y, Obj>, Value<Y, Obj>> x = new HashMap<>();
			x.put(new Value<Y, Obj>(a.src), new Value<Y, Obj>(a.dst));
			ret2.put(a, x);
		}

		return new Inst<>(ret1, ret2, s);
	}

}
