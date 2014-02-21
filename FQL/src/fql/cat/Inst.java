package fql.cat;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
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
import fql.decl.Mapping;
import fql.decl.Node;
import fql.decl.Path;
import fql.decl.Signature;

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

			if (ams0.isEmpty()) {
				ams0.add(new LinkedHashMap<Attribute<Node>, Attribute<Node>>());
			}
			if (ems0.isEmpty()) {
				ems0.add(new LinkedHashMap<Edge, Path>());
			}

			for (LinkedHashMap<Attribute<Node>, Attribute<Node>> am : ams0) {
				for (LinkedHashMap<Edge, Path> em : ems0) {
					try {
						Mapping m = new Mapping(true, exp, base, nm, em, am);
					//	System.out.println(m);
						mappings.add(m);
					} catch (Exception e) {
					//	e.printStackTrace();
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
			//	System.out.println("on " + s + " and " + t
			//			+ " trans cands are " + map0);
				outer: for (Map<Node, Path> k : map0) {
					for (Node x : k.keySet()) {
						for (Node y : k.keySet()) {
							for (Arr<Node, Path> f : exp0.hom(x, y)) {
				//				System.out.println("f " + f.arr.toLong());
					//			System.out
						//				.println("kgetx " + k.get(x).toLong());
						//		System.out.println("tappy "
							//			+ t.appy(f.arr).toLong()); 
								Path lhs = Path.append(base, k.get(x),
										t.appy(f.arr));
								Path rhs = Path.append(base, s.appy(f.arr),
										k.get(y));
							//	System.out.println("for " + k + " x " + x
								//		+ " y " + y + " lhs " + lhs + " rhs "
									//	+ rhs);
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
			identities.put(m, (new Arr<>(map, m, m)));
		}
	//	System.out.println("identities " + identities);
		Map<Pair<Arr<Mapping, Map<Node, Path>>, Arr<Mapping, Map<Node, Path>>>, Arr<Mapping, Map<Node, Path>>> composition = new HashMap<>();
		for (Arr<Mapping, Map<Node, Path>> a1 : arrows) {
			for (Arr<Mapping, Map<Node, Path>> a2 : arrows) {
				if (!a1.dst.equals(a2.src)) {
					continue;
				}
				Map<Node, Path> m = new HashMap<>();
				for (Node n : exp.nodes) {
					m.put(n, Path.append(base, a1.arr.get(n), a2.arr.get(n)));
				}
				composition.put(new Pair<>(a1, a2),
						new Arr<>(m, a1.src, a2.dst));
			}
		}
		//System.out.println(composition);
		return new FinCat<>(mappings, arrows, composition, identities);
	}

	/*
	 * public static void main(String[] args) { Map<String, List<String>> map =
	 * new HashMap<>();
	 * 
	 * List<String> l1 = new LinkedList<>(); l1.add("a"); l1.add("b");
	 * l1.add("c");
	 * 
	 * List<String> l2 = new LinkedList<>(); l2.add("x"); l2.add("y");
	 * 
	 * map.put("l1", l1); map.put("l2", l1); map.put("l3", l1);
	 * 
	 * System.out.println(homomorphs(map)); }
	 */
	/*
	 * public static void main(String[] args) { List<String> A = new
	 * LinkedList<>(); List<String> B = new LinkedList<>();
	 * 
	 * A.add("a"); A.add("b"); A.add("c"); // A.add("d"); B.add("x");
	 * B.add("y"); B.add("z"); System.out.println("bijections " + bijections(A,
	 * B)); System.out.println("morphisms " + homomorphs(A, B)); }
	 */

	public static <Obj, Arrow, Y, X> List<Map<Obj, Map<Value<Y, X>, Value<Y, X>>>> morphsX(
			Map<Obj, List<Map<Value<Y, X>, Value<Y, X>>>> map) {
		List<Map<Obj, Map<Value<Y, X>, Value<Y, X>>>> ret = new LinkedList<>();

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

	private static <Obj, Arrow, Y, X> Map<Obj, Map<Value<Y, X>, Value<Y, X>>> make5(
			int[] counters, List<Obj> A,
			Map<Obj, List<Map<Value<Y, X>, Value<Y, X>>>> B) {
		Map<Obj, Map<Value<Y, X>, Value<Y, X>>> ret = new HashMap<>();
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

	public static <Obj, Arrow, Y, X> Map<Obj, List<Map<Value<Y, X>, Value<Y, X>>>> morphs2(
			Inst<Obj, Arrow, Y, X> i1, Inst<Obj, Arrow, Y, X> i2) {
		Map<Obj, List<Map<Value<Y, X>, Value<Y, X>>>> ret = new HashMap<>();

		for (Obj o : i1.cat.objects) {

			List<Map<Value<Y, X>, Value<Y, X>>> morphs3 = bijections(
					new LinkedList<>(i1.applyO(o)),
					new LinkedList<>(i2.applyO(o)));

			ret.put(o, morphs3);
		}

		return ret;
	}

	public static <X, Y> List<LinkedHashMap<X, Y>> homomorphs(Map<X, List<Y>> L) {
		List<LinkedHashMap<X, Y>> ret = new LinkedList<>();

		if (L.isEmpty()) {
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

		// System.out.println("Expecting " + Math.pow(B.size(), A.size())
		// + " morphs " + B.size() + " and " + A.size());

		if (A.size() == 0) {
			return ret;
		}

		if (B.size() == 0) {
			throw new RuntimeException();
		}

		int[] counters = new int[A.size() + 1];

		// int i = 0;
		for (;;) {

			if (counters[A.size()] == 1) {
				break;
			}
			ret.add(make2(counters, A, B));
			inc(counters, B.size());
			// i++;
			// if (i == 100) {
			// System.out.println(A);
			// System.out.println(B);
			// for (int j = 0; j < counters.length; j++) {
			// System.out.println(j + " = " + counters[j] + ",");
			// }
			// }
		}

		return ret;
	}

	//

	public static <X> List<Map<X, X>> bijections(List<X> A, List<X> B) {
		List<Map<X, X>> ret = new LinkedList<>();

		// System.out.println("Expecting " + Math. + " morphs " + B.size() +
		// " and " + A.size());

		if (A.size() == 0) {
			return ret;
		}

		if (B.size() == 0) {
			throw new RuntimeException();
		}

		if (A.size() != B.size()) {
			throw new RuntimeException();
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
			Map<X, X> m = new HashMap<>();
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

	public static <Obj, Arrow, Y, X> Set<SetFunTrans<Obj, Arrow, Y, X>> morphs(
			Inst<Obj, Arrow, Y, X> i1, Inst<Obj, Arrow, Y, X> i2) {

		Set<SetFunTrans<Obj, Arrow, Y, X>> ret = new HashSet<>();

		Map<Obj, List<Map<Value<Y, X>, Value<Y, X>>>> x = morphs2(i1, i2);

		List<Map<Obj, Map<Value<Y, X>, Value<Y, X>>>> y = morphsX(x);

		for (Map<Obj, Map<Value<Y, X>, Value<Y, X>>> map : y) {
			try {
				SetFunTrans<Obj, Arrow, Y, X> xxx = new SetFunTrans<>(map, i1,
						i2);
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
