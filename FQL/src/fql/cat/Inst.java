package fql.cat;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fql.DEBUG;
import fql.FQLException;

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

	public static void main(String[] args) {
		List<String> A = new LinkedList<>();
		List<String> B = new LinkedList<>();

		A.add("a");
		A.add("b");
		A.add("c");
		// A.add("d");
		B.add("x");
		B.add("y");
		B.add("z");
		System.out.println("bijections " + bijections(A, B));
		System.out.println("morphisms " + homomorphs(A, B));
	}

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

	public static <X> List<Map<X, X>> homomorphs(List<X> A, List<X> B) {
		List<Map<X, X>> ret = new LinkedList<>();

		System.out.println("Expecting " + Math.pow(B.size(), A.size())
				+ " morphs " + B.size() + " and " + A.size());

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

	private static <X> Map<X, X> make2(int[] counters, List<X> A, List<X> B) {
		Map<X, X> ret = new HashMap<>();
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
