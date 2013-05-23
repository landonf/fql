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
	public static Set<String[]> eval(RA e, Map<String, Set<String[]>> inst)
			throws FQLException {
		if (e instanceof Keygen) {
			Keygen k = (Keygen) e;
			Set<String[]> i = eval(k.e, inst);
			Set<String[]> ret = new HashSet<String[]>();

			//int count = 0;
			for (String[] tuple : i) {
				String[] newtuple = new String[tuple.length + 2];
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
			Set<String[]> ret = new HashSet<String[]>();
			Set<String[]> i = eval(s.e, inst);

			for (String[] tuple : i) {
				if (tuple[s.i].equals(tuple[s.j])) {
					ret.add(tuple);
				}
			}
			return ret;
		}
		if (e instanceof Project) {
			Project p = (Project) e;
			Set<String[]> ret = new HashSet<String[]>();
			Set<String[]> i = eval(p.e, inst);

			for (String[] tuple : i) {
				String[] t = new String[p.cols.length];
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
			Set<String[]> ret = new HashSet<String[]>();
			Set<String[]> i1 = eval(p.e1, inst);
			Set<String[]> i2 = eval(p.e2, inst);

			for (String[] t1 : i1) {
				for (String[] t2 : i2) {
					ret.add(mergeTuple(t1, t2));
				}
			}
			return ret;
		}
		if (e instanceof DisjointUnion) {
			DisjointUnion u = (DisjointUnion) e;
			Set<String[]> ret = new HashSet<String[]>();
			if (u.e.size() == 0) {
				return ret;
			}
//			if (u.e.size() == 1) {
//				return eval(u.e.get(0), inst);
//			}
			int i = 0;
			for (RA r : u.e) {
				Set<String[]> ix = eval(r, inst);
				Set<String[]> iy = tag(ix, u.tags.get(i++), u.tags.get(i++));
				ret.addAll(iy);
			}
			return ret;
		}

		if (e instanceof Relvar) {
			Relvar r = (Relvar) e;
			Set<String[]> ret = inst.get(r.r);
			if (ret == null) {
				throw new RuntimeException("Cannot find " + r.r + " in " + inst);
			}
			return ret;
		}
		if (e instanceof EmptyRA) {
			return new HashSet<String[]>();
		}
		if (e instanceof SingletonRA) {
			Set<String[]> ret = new HashSet<String[]>();
			String[] x = new String[] { "!" };
			ret.add(x);
			return ret;
		}

		throw new FQLException("Unknown RA " + e);
	}

	private static String makeKey(String[] tuple) {
		if (tuple.length == 1) {
			return tuple[0];
		}
		String ret = tuple[0];
		for (String i : tuple) {
			ret += ("^" + i);
		}
		return ret;
	}

	/**
	 * Adds tags to the tuples in a binary relation
	 */
	private static Set<String[]> tag(Set<String[]> ix, String i, String k) throws FQLException {
		Set<String[]> ret = new HashSet<String[]>();
		for (String[] s : ix) {
			if (s.length != 2) {
				throw new FQLException("disjoint union over non-binary " + ix + " tuple is " + s);
			}
			String[] s0 = new String[2];
			s0[0] = i + "_" + s[0];
			s0[1] = k + "_" + s[1];
			ret.add(s0);
		}
		return ret;
	}

	private static String[] mergeTuple(String[] t1, String[] t2) {
		String[] ret = new String[t1.length + t2.length];
		for (int i = 0; i < t1.length; i++) {
			ret[i] = t1[i];
		}
		for (int i = 0; i < t2.length; i++) {
			ret[t1.length + i] = t2[i];
		}

		return ret;
	}

	/**
	 * A private test instances for pi
	 * @return
	 */
	@SuppressWarnings("unused")
	private static Map<String, Set<String[]>> test0() {
		Map<String, Set<String[]>> ret = new HashMap<String, Set<String[]>>();

		Set<String[]> C1 = new HashSet<String[]>();
		C1.add(new String[] { "c1A", "c1A" });
		C1.add(new String[] { "c1B", "c1B" });

		Set<String[]> C2 = new HashSet<String[]>();
		C2.add(new String[] { "c2", "c2" });
		//C2.add(new String[] { "x", "x" });

		Set<String[]> c = new HashSet<String[]>();
		c.add(new String[] { "c1A", "c2" });
		c.add(new String[] { "c1B", "c2" });

		ret.put("C1", C1);
		ret.put("C2", C2);
		ret.put("c", c);
		return ret;
	}
	
	/**
	 * Limit as join all.  Compare to FDM.lim
	 */
	public static  <Arrow> Pair<RA, String[]> 
	lim(CommaCat<Node, Path, Node, Path, Node, Path> b,
			Map<Triple<Node, Node, Arr<Node, Path>>, RA> map,
			Map<Pair<Arr<Node, Path>, Arr<Node, Path>>, RA> map2) throws FQLException {
		// System.out.println("Taking limit for " + B);
		RA x0 = null;
		int m = b.objects.size();
		String[] cnames = new String[m];
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
				cnames[temp] = n.second.string;
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
			x0 = new Select(x0, m, cnamelkp(cnames, e.src.second.string));
//			 System.out.println("Query is " + x0);
//			 printNicely(eval(x0, test0()));
//			 System.out.println("end test");
			x0 = new Select(x0, m + 1, cnamelkp(cnames, e.dst.second.string));
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
		return new Pair<RA, String[]>(ret, cnames);
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
		
		if (!FinFunctor.isDiscreteOpFib(F.toFunctor2().first)) {
			throw new FQLException("Not a discrete op-fibration: " + F.name);
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

	private static boolean eqpath(FinCat<String, List<List<String>>> c,
			Path p1, Path p2) {
		for (Arr<String, List<List<String>>> peqc : c.arrows) {
			if (peqc.arr.contains(Signature.pathToList(p1))) {
				return peqc.arr.contains(Signature.pathToList(p2));
			}
		}
		throw new RuntimeException("eqpath cannot find " + p1 + " and " + p2
				+ " in " + c);
	}

	/**
	 * Query generation for pi
	 */
	public static Map<String, RA> pi(Mapping F0) throws FQLException {
		Signature D0 = F0.target;
		Signature C0 = F0.source;
		FinCat<Node, Path> D = D0.toCategory2().first;
		FinCat<Node, Path> C = C0.toCategory2().first;
		FinFunctor<Node, Path, Node, Path> F = F0.toFunctor2().first;
		Map<String, RA> ret = new HashMap<String, RA>();

		for (Node d0 : D.objects) {
			CommaCat<Node, Path, Node, Path, Node, Path> B = doComma(D, C, F, d0, D0);

			RA r = lim(B, deltaObj(B.projB), deltaArr(B.projB)).first;
			ret.put(d0.string, squish(r));
		}

		for (Edge s : F0.target.edges) {
			Node dA = s.source;
			CommaCat<Node, Path, Node, Path, Node, Path> BA = doComma(D, C, F, dA, D0);
			Pair<RA, String[]> q1 = lim(BA, deltaObj(BA.projB), deltaArr(BA.projB));

			Node dB = s.target;
			CommaCat<Node, Path, Node, Path, Node, Path> BB = doComma(D, C, F, dB, D0);
			Pair<RA, String[]> q2 = lim(BB,deltaObj(BB.projB), deltaArr(BB.projB));
			
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
			
			RA rax = subset(q2.second, q1.second, raw);
			
//			System.out.println("Testing rax ");
//			System.out.println("Query is " + rax);
//			printNicely(eval(rax, test0()));
//			System.out.println("end test");

			
			RA ray = new Project(rax, new int[] { q2.second.length+1, 0 });

			ret.put(s.name, ray);
		}

		return ret;
	}


	private static RA subset(String[] q1cols, String[] q2cols, RA q1q2) {
	//	System.out.println("trying subset " + print(q1cols) + " in " + print(q2cols));
		a: for (int i = 0; i < q1cols.length; i++) {
			for (int j = 0; j < q2cols.length; j++) {
				if (q1cols[i].equals(q2cols[j])) {
					q1q2 = new Select(q1q2, i+1, j+2+q1cols.length);
					continue a;
				}
			}
			throw new RuntimeException("No col " + q1cols[i] + " in " + print(q2cols));
		}
		return q1q2;
	}

	private static String print(String[] q2cols) {
		String s = "";
				for (String a : q2cols) {
					s += " " + a;
				}
		return s;
	}

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

	private static RA compose(List<String> p) {
		RA r = new Relvar(p.get(0));
		for (int i = 1; i < p.size(); i++) {
			r = compose(r, new Relvar(p.get(i)));
		}
		return r;
	}

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
	public static Map<String, Set<String[]>> eval0(Map<String, RA> ras,
			Map<String, Set<String[]>> i) throws FQLException {
		Map<String, Set<String[]>> ret = new HashMap<String, Set<String[]>>();
		for (String k : ras.keySet()) {
			RA v = ras.get(k);
			ret.put(k, RA.eval(v, i));
		}

		return ret;
	}

}
