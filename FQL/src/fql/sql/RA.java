package fql.sql;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import fql.FQLException;
import fql.Fn;
import fql.Pair;
import fql.Triple;
import fql.cat.Arr;
import fql.cat.CommaCat;
import fql.cat.FinCat;
import fql.cat.FinFunctor;
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
 * Syntax for RA, and query generation.
 */
public abstract class RA {

	/** Forms a binary reflexive table from an n-ary table with an id column 0 */
	public static RA squish(RA e) {
		return new Project(e, new int[] { 0, 1 });
	}

	/**
	 * Evaluates an RA expression on an instance.
	 */
	public static Set<Object[]> eval(RA e, Map<String, Set<Object[]>> i0)
			throws FQLException {
		if (e instanceof Keygen) {
			Keygen k = (Keygen) e;
			Set<Object[]> i = eval(k.e, i0);
			Set<Object[]> ret = new HashSet<Object[]>();

			//int count = 0;
			for (Object[] tuple : i) {
				Object[] newtuple = new Object[tuple.length + 2];
				newtuple[0] = makeKey(tuple); 
				newtuple[1] = newtuple[0];
				for (int j = 1; j <= tuple.length; j++) {
					newtuple[j + 1] = tuple[j - 1];
				}
				ret.add(newtuple);
			}
			return ret;
		}
		if (e instanceof Select) {
			Select s = (Select) e;
			Set<Object[]> ret = new HashSet<Object[]>();
			Set<Object[]> i = eval(s.e, i0);

			for (Object[] tuple : i) {
				if (tuple[s.i].equals(tuple[s.j])) {
					ret.add(tuple);
				}
			}
			return ret;
		}
		if (e instanceof Project) {
			Project p = (Project) e;
			Set<Object[]> ret = new HashSet<Object[]>();
			Set<Object[]> i = eval(p.e, i0);

			for (Object[] tuple : i) {
				Object[] t = new Object[p.cols.length];
				int f = 0;
				for (int x : p.cols) {
					t[f] = tuple[x];
					f++;
				}
				ret.add(t);
			}
			return ret;
		}
		if (e instanceof Product) {
			Product p = (Product) e;
			Set<Object[]> ret = new HashSet<Object[]>();
			Set<Object[]> i1 = eval(p.e1, i0);
			Set<Object[]> i2 = eval(p.e2, i0);

			for (Object[] t1 : i1) {
				for (Object[] t2 : i2) {
					ret.add(mergeTuple(t1, t2));
				}
			}
			return ret;
		}
		if (e instanceof DisjointUnion) {
			DisjointUnion u = (DisjointUnion) e;
			Set<Object[]> ret = new HashSet<Object[]>();
			if (u.e.size() == 0) {
				return ret;
			}
//			if (u.e.size() == 1) {
//				return eval(u.e.get(0), inst);
//			}
			int i = 0;
			for (RA r : u.e) {
				Set<Object[]> ix = eval(r, i0);
				Set<Object[]> iy = tag(ix, u.tags.get(i++), u.tags.get(i++));
				ret.addAll(iy);
			}
			return ret;
		}

		if (e instanceof Relvar) {
			Relvar r = (Relvar) e;
			Set<Object[]> ret = i0.get(r.r);
			if (ret == null) {
				throw new RuntimeException("Cannot find " + r.r + " in " + i0);
			}
			return ret;
		}
		if (e instanceof EmptyRA) {
			return new HashSet<Object[]>();
		}
		if (e instanceof SingletonRA) {
			Set<Object[]> ret = new HashSet<Object[]>();
			String[] x = new String[] { "!" };
			ret.add(x);
			return ret;
		}

		throw new FQLException("Unknown RA " + e);
	}

	private static Object makeKey(Object[] tuple) {
		if (tuple.length == 1) {
			return tuple[0];
		}
		Object ret = tuple[0];
		for (Object i : tuple) {
			ret += ("^" + i);
		}
		return ret;
	}

	/**
	 * Adds tags to the tuples in a binary relation
	 */
	private static Set<Object[]> tag(Set<Object[]> ix, Object i, Object k) throws FQLException {
		Set<Object[]> ret = new HashSet<Object[]>();
		for (Object[] s : ix) {
			if (s.length != 2) {
				throw new FQLException("disjoint union over non-binary " + ix + " tuple is " + s);
			}
			Object[] s0 = new String[2];
			s0[0] = i + "_" + s[0];
			s0[1] = k + "_" + s[1];
			ret.add(s0);
		}
		return ret;
	}

	private static Object[] mergeTuple(Object[] t1, Object[] t2) {
		Object[] ret = new Object[t1.length + t2.length];
		for (int i = 0; i < t1.length; i++) {
			ret[i] = t1[i];
		}
		for (int i = 0; i < t2.length; i++) {
			ret[t1.length + i] = t2[i];
		}

		return ret;
	}

//	/**
//	 * A private test instances for pi
//	 * @return
//	 */
//	@SuppressWarnings("unused")
//	private static Map<String, Set<String[]>> test0() {
//		Map<String, Set<String[]>> ret = new HashMap<String, Set<String[]>>();
//
//		Set<String[]> C1 = new HashSet<String[]>();
//		C1.add(new String[] { "c1A", "c1A" });
//		C1.add(new String[] { "c1B", "c1B" });
//
//		Set<String[]> C2 = new HashSet<String[]>();
//		C2.add(new String[] { "c2", "c2" });
//		//C2.add(new String[] { "x", "x" });
//
//		Set<String[]> c = new HashSet<String[]>();
//		c.add(new String[] { "c1A", "c2" });
//		c.add(new String[] { "c1B", "c2" });
//
//		ret.put("C1", C1);
//		ret.put("C2", C2);
//		ret.put("c", c);
//		return ret;
//	}
//	
	/**
	 * Limit as join all.  Compare to FDM.lim
	 */
	@SuppressWarnings("unchecked")
	public static  <Arrow> Pair<RA, Triple<Node, Node, Arr<Node, Path>>[]> 
	lim(CommaCat<Node, Path, Node, Path, Node, Path> b,
			Map<Triple<Node, Node, Arr<Node, Path>>, RA> map,
			Map<Pair<Arr<Node, Path>, Arr<Node, Path>>, RA> map2) throws FQLException {
		// System.out.println("Taking limit for " + B);
		RA x0 = null;
		int m = b.objects.size();
		Triple<Node, Node, Arr<Node, Path>>[] cnames = new Triple[m];
		int temp = 0;

		if (m == 0) {
			x0 = new SingletonRA();
		} else {
			for (Triple<Node, Node, Arr<Node, Path>> n : b.objects) {
				if (x0 == null) {
					x0 = map.get(n);
				} else {
					x0 = new Product(x0, map.get(n));
				}
				cnames[temp] = n;
				temp++;
			}
			x0 = new Project(x0, makeCols(temp));
		}

//		 System.out.println("Testing initial part ");
//		 System.out.println("Query is " + x0);
//		 printNicely(eval(x0, test0()));
//		 System.out.println("end test");

		int[] cols = new int[m];
		for (int i = 0; i < m; i++) {
			cols[i] = i;
		}

		for (Arr<Triple<Node, Node, Arr<Node, Path>>, Pair<Arr<Node, Path>, Arr<Node, Path>>> e : b.arrows) {
//			 System.out.println("Doing arrow " + e);
//			 System.out.println("map is " + map);
//			 System.out.println("one " + map.get(e.arr));
//			 System.out.println("two " + map.get(e.arr.first));
//			 System.out.println("three " + map.get(e.arr.second));

			x0 = new Product(x0, map2.get(e.arr));
//			 System.out.println("Query is " + x0);
//			 printNicely(eval(x0, test0()));
//			 System.out.println("end test");
			x0 = new Select(x0, m, cnamelkp(cnames, e.src));
//			 System.out.println("Query is " + x0);
//			 printNicely(eval(x0, test0()));
//			 System.out.println("end test");
			x0 = new Select(x0, m + 1, cnamelkp(cnames, e.dst));
//			 System.out.println("Query is " + x0);
//			 printNicely(eval(x0, test0()));
//			 System.out.println("end test");
			x0 = new Project(x0, cols);
//			 System.out.println("Query is " + x0);
//			 printNicely(eval(x0, test0()));
//			 System.out.println("end test");
		}

		RA ret = new Keygen(x0);
//		 System.out.println("Result of test ");
//		 printNicely(eval(ret, test0()));
//		 System.out.println("Query was " + ret);
//		 System.out.println("end test");
		return new Pair<RA, Triple<Node, Node, Arr<Node, Path>>[]>(ret, cnames);
	}

	private static int[] makeCols(int n) {
		int[] ret = new int[n];
		for (int i = 0; i < n; i++) {
			ret[i] = 2 * i;
		}
		return ret;
	}

	 @SuppressWarnings("unused")
	private static void printNicely(Set<String[]> eval) {
		 for (String[] s : eval) {
			 for (String x : s) {
				 System.out.print(x + " ");
			 }
			 System.out.println();
		 }
	 }


	private static <Obj> int cnamelkp(Obj[] cnames, Obj s) throws FQLException {
		for (int i = 0; i < cnames.length; i++) {
			if (s.equals(cnames[i])) {
				return i;
			}
		}
		throw new FQLException("Cannot lookup position of " + s + " in "
				+ cnames.toString());
	}

	
	/**
	 * Query generation for sigma.
	 */
	public static Map<String, RA> sigma(Mapping F) throws FQLException {
		Signature C = F.source;
		Signature D = F.target;
		Map<String, RA> ret = new HashMap<String, RA>();
		Map<Node, List<String>> tags = new HashMap<Node, List<String>>();
		
		try {
			Triple<FinFunctor<Node, Path, Node, Path>, Pair<FinCat<Node, Path>, Fn<Path, Arr<Node, Path>>>, Pair<FinCat<Node, Path>, Fn<Path, Arr<Node, Path>>>> func = F.toFunctor2();
			if (!FinFunctor.isDiscreteOpFib(func.first)) {
				throw new FQLException("Not a discrete op-fibration" /* + F.name */);
			}

		} catch (RuntimeException e) {
			e.printStackTrace();
			throw e;
		}
		
		for (Node d : D.nodes) {
			List<RA> tn = new LinkedList<RA>();
			List<String> tj = new LinkedList<String>();
			for (Node c : C.nodes) {
				if (F.nm.get(c).equals(d)) {
					tn.add(new Relvar(c.string));
					tj.add(c.string);
					tj.add(c.string);
				}
			}
			ret.put(d.string, new DisjointUnion(tn, tj));
			tags.put(d, tj);
		}

		for (Edge e : D.edges) {
			Node d = e.source;
			//Node d0 = e.target;
			List<RA> tn = new LinkedList<RA>();
			List<String> tx = new LinkedList<String>();
			for (Node c : C.nodes) {
				if (F.nm.get(c).equals(d)) {
					Path pc = findEquiv(c, F, e);					
					RA q = compose(pc);
					tn.add(q);
					tx.add(c.string);
					tx.add(pc.target.string);
				}
			}
			ret.put(e.name, new DisjointUnion(tn, tx));
		}
		return ret;
	}

	private static Path findEquiv(Node c, Mapping f, Edge e)
			throws FQLException {
		Signature C = f.source;
		Signature D = f.target;
		if (!C.acyclic()) {
			throw new FQLException("Sigma must be acyclic " + C);
		}
		FinCat<Node, Path> C0 = C.toCategory2().first;
		for (Arr<Node, Path> peqc : C0.arrows) {
			Path path = peqc.arr;
			//Path path = new Path(f.source, p);
			if (!path.source.equals(c)) {
				continue;
			}
			Path path_f = f.appy(path);
			Fn<Path, Arr<Node, Path>> F = D.toCategory2().second;
			if (F.of(path_f).equals(F.of(new Path(D, e)))) {
				return path;
			}
		}
		throw new FQLException("Could not find path mapping to " + e
				+ " under " + f);
	}

//	private static boolean eqpath(FinCat<String, List<List<String>>> c,
//			Path p1, Path p2) {
//		for (Arr<String, List<List<String>>> peqc : c.arrows) {
//			if (peqc.arr.contains(Signature.pathToList(p1))) {
//				return peqc.arr.contains(Signature.pathToList(p2));
//			}
//		}
//		throw new RuntimeException("eqpath cannot find " + p1 + " and " + p2
//				+ " in " + c);
//	}

	/**
	 * Query generation for pi
	 */
	public static Map<String, RA> pi(Mapping F0) throws FQLException {
		Signature D0 = F0.target;
		Signature C0 = F0.source;
		Pair<FinCat<Node, Path>, Fn<Path, Arr<Node, Path>>> DD = D0.toCategory2();
		FinCat<Node, Path> D = DD.first;
		FinCat<Node, Path> C = C0.toCategory2().first;
		FinFunctor<Node, Path, Node, Path> F = F0.toFunctor2().first;
		Map<String, RA> ret = new HashMap<String, RA>();
		
		for (Node d0 : D.objects) {
			CommaCat<Node, Path, Node, Path, Node, Path> B = doComma(D, C, F, d0, D0);

			Pair<RA, Triple<Node, Node, Arr<Node, Path>>[]> rr = lim(B, deltaObj(B.projB), deltaArr(B.projB));
			ret.put(d0.string, squish(rr.first));
		}

		for (Edge s : F0.target.edges) {
			Node dA = s.source;
			CommaCat<Node, Path, Node, Path, Node, Path> BA = doComma(D, C, F, dA, D0);
			Pair<RA, Triple<Node, Node, Arr<Node, Path>>[]> q1 = lim(BA, deltaObj(BA.projB), deltaArr(BA.projB));

			Node dB = s.target;
			CommaCat<Node, Path, Node, Path, Node, Path> BB = doComma(D, C, F, dB, D0);
			Pair<RA, Triple<Node, Node, Arr<Node, Path>>[]> q2 = lim(BB,deltaObj(BB.projB), deltaArr(BB.projB));
			
			RA rau = chop1(q2.second.length, q2.first);
			RA rav = chop1(q1.second.length, q1.first);
			RA raw = new Product(rau, rav);

//			System.out.println("Testing rau ");
//			System.out.println("Query is " + rau);
//			printNicely(eval(rau, test0()));
//			System.out.println("end test");
//
//			System.out.println("Testing rav ");
//			System.out.println("Query is " + rav);
//			printNicely(eval(rav, test0()));
//			System.out.println("end test");
//
//			System.out.println("Testing raw ");
//			System.out.println("Query is " + raw);
//			printNicely(eval(raw, test0()));
//			System.out.println("end test");
//
			
			RA rax = subset(D, DD.second.of(new Path(D0, s)), q2.second, q1.second, raw);
			
//			System.out.println("Testing rax ");
//			System.out.println("Query is " + rax);
//			printNicely(eval(rax, test0()));
//			System.out.println("end test");

			
			RA ray = new Project(rax, new int[] { q2.second.length+1, 0 });

			ret.put(s.name, ray);
		}

		return ret;
	}


	private static RA subset(
			FinCat<Node, Path> cat, Arr<Node, Path> e, 
			Triple<Node, Node, Arr<Node, Path>>[] q2cols,
			Triple<Node, Node, Arr<Node, Path>>[] q1cols, RA raw) throws FQLException {
//		 System.out.println("trying subset " + print(q1cols) + " in " +
//		 print(q2cols));
		//List<Pair<Pair<String, String>, Pair<String, String>>> ret = new LinkedList<>();
		//System.out.println("Arr " + e);
		//System.out.println("Cat" + cat);
		// turn e into arrow e', compute e' ; q2col, look for that
		/* a: */ for (int i = 0; i < q2cols.length; i++) {
			boolean b = false;
			for (int j = 0; j < q1cols.length; j++) {
				Triple<Node, Node, Arr<Node, Path>> q2c = q2cols[i];
				Triple<Node, Node, Arr<Node, Path>> q1c = q1cols[j];
//				System.out.println("^^^" + q1c);
//				System.out.println("^^^" + q2c);
//				System.out.println("compose " + cat.compose(e, q2c.third));
//				System.out.println("compose " + cat.compose(q2c.third, e));
//				System.out.println("compose " + cat.compose(e, q1c.third));
//				System.out.println("compose " + cat.compose(q1c.third, e));
//				// if (q1c.equals(q2c)) {
			
				if (q1c.third.equals(cat.compose(e, q2c.third)) && q1c.second.equals(q2c.second)) {
			//		System.out.println("hit on " + q2c.third);
					//System.out.println("raw is " + printSetOfArrays(raw));
					//1,2
			//		System.out.println("equating colums " + (j + 1) + " and " + (i + 2 + q1cols.length));
					
					raw = new Select(raw, i + 1, j + 2 + q2cols.length);
				//	System.out.println("raw now " + printSetOfArrays(raw));
					//System.out.println("added to where: " +  retadd);
					if (b) {
						throw new RuntimeException();
					}
				//	ret.add(retadd);
				//	if (b) throw new FQLException("not uniq: " + "lookup for " + q2c + " and " + q1c);
					
					b = true;
					
					//System.out.println("added to where: " +  retadd);
					//continue a;
				}
			}
			if (b) continue;
			String xxx = "";
			for (Triple<Node, Node, Arr<Node, Path>> yyy : q1cols) {
				xxx += ", " + yyy;
			}
			throw new RuntimeException("No col " + q2cols[i] + " in " + xxx
					);

		}
		//System.out.println("where is " + ret);
		return raw;

		// a: for (int i = 0; i < q1cols.length; i++) {
		// for (int j = 0; j < q2cols.length; j++) {
		// if (q1cols[i].equals(q2cols[j])) {
		// int col1 = i+1;
		// int col2 = j+
		// // int col2 = j+2+q1cols.length;
		// ret.add(new Pair<>(new Pair<>(),new Pair<>()));
		// q1q2 = new Select(q1q2, i+1, j+2+q1cols.length);
		// continue a;
		// }
		// }
		// throw new RuntimeException("No col " + q1cols[i] + " in " + q2cols);
		// }
		// return ret;
	}

//	private static RA subset(String[] q1cols, String[] q2cols, RA q1q2) {
//	//	System.out.println("trying subset " + print(q1cols) + " in " + print(q2cols));
//		a: for (int i = 0; i < q1cols.length; i++) {
//			for (int j = 0; j < q2cols.length; j++) {
//				if (q1cols[i].equals(q2cols[j])) {
//					q1q2 = new Select(q1q2, i+1, j+2+q1cols.length);
//					continue a;
//				}
//			}
//			throw new RuntimeException("No col " + q1cols[i] + " in " + print(q2cols));
//		}
//		return q1q2;
//	}

//	private static String print(String[] q2cols) {
//		String s = "";
//				for (String a : q2cols) {
//					s += " " + a;
//				}
//		return s;
//	}

	@SuppressWarnings("unused")
	private static String printNice(
			Triple<String, String, List<List<String>>>[] x) {
		String s = "";
		for (Triple<String, String, List<List<String>>> y : x) {
			String sx = "(" + y.first + ", " + y.second + ", [";
			for (List<String> a : y.third) {
				for (String b : a) {
					sx += (b + " , ");
				}
			}
			sx += "])";
			s += sx;
		}
		return s;
	}

	private static RA chop1(int n, RA r) {
		int[] cols = new int[n + 1];
		for (int i = 1; i <= n + 1; i++) {
			cols[i - 1] = i;
		}
		return new Project(r, cols);
	}
	
	/**
	 * wrapper for comma categories
	 */
	private static CommaCat<Node, Path, Node, Path, Node, Path> doComma(
			FinCat<Node, Path> d2,
			FinCat<Node, Path> c,
			FinFunctor<Node, Path, Node, Path> f,
			Node d0, Signature S) throws FQLException {
//		List<String> x = new LinkedList<String>();
//		x.add(d0);
//		List<List<String>> y = new LinkedList<List<String>>();
//		y.add(x);

		FinFunctor<Node, Path, Node, Path> d = FinFunctor
				.singleton(d2, d0, new Arr<>(d2.identities.get(d0).arr,d0,d0));
		CommaCat<Node, Path, Node, Path, Node, Path>
		B = new CommaCat<>(d.srcCat, c, d2, d, f);
		return B;
	}

	/**
	 * Query generation for delta.
	 */
	public static Map<String, RA> delta(Mapping m) {
		Map<String, RA> ret = new HashMap<String, RA>();
		for (Entry<Node, Node> n : m.nm.entrySet()) {
			ret.put(n.getKey().string, new Relvar(n.getValue().string));
		}
		for (Entry<Edge, Path> e : m.em.entrySet()) {
			ret.put(e.getKey().name, compose(e.getValue()));
		}
		for (Entry<Attribute<Node>, Attribute<Node>> a : m.am.entrySet()) {
			ret.put(a.getKey().name, new Relvar(a.getValue().name));
		}
		return ret;
	}

	/** these bastardized versions of delta only works in support of pi
	 */
	private static Map<Triple<Node, Node, Arr<Node, Path>>, RA> deltaObj(
			FinFunctor<Triple<Node, Node, Arr<Node, Path>>, Pair<Arr<Node, Path>, Arr<Node, Path>>, Node, Path> projB) {
		Map<Triple<Node, Node, Arr<Node, Path>>, RA> ret = new HashMap<>();
		for (Entry<Triple<Node, Node, Arr<Node, Path>>, Node> p : projB.objMapping
				.entrySet()) {
			ret.put(p.getKey(), new Relvar(p.getKey().second.string));
		}
		return ret;
	}

	/** these bastardized versions of delta only works in support of pi
	 */
	private static Map<Pair<Arr<Node, Path>, Arr<Node, Path>>, RA> deltaArr(
			FinFunctor<Triple<Node, Node, Arr<Node, Path>>, Pair<Arr<Node, Path>, Arr<Node, Path>>, Node, Path> projB) {
		Map<Pair<Arr<Node, Path>, Arr<Node, Path>>, RA> ret = new HashMap<>();
		for (Entry<Arr<Triple<Node, Node, Arr<Node, Path>>, Pair<Arr<Node, Path>, Arr<Node, Path>>>, Arr<Node, Path>> p : projB.arrowMapping.entrySet()) {
			Path x = p.getKey().arr.second.arr;
			ret.put(p.getKey().arr, compose(x)); 
		}

		return ret;
	}

//	private static RA compose(List<String> p) {
//		RA r = new Relvar(p.get(0));
//		for (int i = 1; i < p.size(); i++) {
//			r = compose(r, new Relvar(p.get(i)));
//		}
//		return r;
//	}

	private static RA compose(RA a, RA b) {
		RA c = new Product(a, b);
		RA d = new Select(c, 1, 2);
		return new Project(d, new int[] { 0, 3 });
	}

	private static RA compose(Path p) {
		RA r = new Relvar(p.source.string);
		for (Edge e : p.path) {
			r = compose(r, new Relvar(e.name));
		}
		return r;
	}

	/**
	 * Wrapper for evaluation.
	 */
	public static Map<String, Set<Object[]>> eval0(Map<String, RA> ras,
			Map<String, Set<Object[]>> i0) throws FQLException {
		Map<String, Set<Object[]>> ret = new HashMap<String, Set<Object[]>>();
		for (String k : ras.keySet()) {
			RA v = ras.get(k);
			ret.put(k, RA.eval(v, i0));
		}

		return ret;
	}

}
