package fql.cat;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.table.DefaultTableModel;

import fql.DEBUG;
import fql.FQLException;
import fql.Pair;
import fql.Quad;
import fql.decl.Attribute;
import fql.decl.Edge;
import fql.decl.Eq;
import fql.decl.Instance;
import fql.decl.Mapping;
import fql.decl.Node;
import fql.decl.Path;
import fql.decl.Signature;
import fql.decl.Transform;
import fql.decl.Type;
import fql.sql.PSMInterp;

/**
 * 
 * @author ryan
 * 
 *         Left-Kan extensions with the Carmody-Walters algorithm.
 */
public class Denotation {

	static int LIMIT = 20, INC = 1;

	Signature xxxSig;
/*
	public Pair<FinCat<Node, Path>, Fn<Path, Arr<Node, Path>>> toCategory(
			Signature sig) throws FQLException {
		/*
		this.sig = sig;

		B = sig;
		A = B.onlyObjects();
		X = A.terminal("-1");
		F = subset(A, B);
		R = B.eqs;

		initTables();
		makeJTables();
		interp = new PSMInterp();
		
		
		xxxSig = sig;
		if (!enumerate(DEBUG.debug.MAX_DENOTE_ITERATIONS)) {
			throw new FQLException(
					"Category computation has exceeded allowed iterations.");
		}

		return toCategoryHelper(sig, true);
	} */
	/*
	private Pair<FinCat<Node, Path>, Fn<Path, Arr<Node, Path>>> catInv() throws FQLException {
		List<Node> objects = B.nodes;

		Set<Arr<Node, Path>> arrows = new HashSet<>();
		Map<Node, Arr<Node, Path>> identities = new HashMap<>();

		final Fn<Path, Integer> fn = makeFn();
		List<Path> paths = new LinkedList<>(); 
		final Map<Integer, Path> fn2 = new HashMap<>();

//		int numarrs = numarrs();
		for (Node n : B.nodes) {
			paths.add(new Path(B, n));
		}
		outer: for (int iter = 0; iter < DEBUG.debug.MAX_PATH_LENGTH; iter++) {
			for (Path p : paths) {
				Integer i = fn.of(p);
				if (fn2.get(i) == null) {
					fn2.put(i, p);
				}
				if (fn2.size() == numarrs) {
					break outer;
				}
			}
			List<Path> paths0 = new LinkedList<>();
			for (Path p : paths) {
				for (Edge e : B.outEdges(p.target)) {
					paths0.add(Path.append(B, p, new Path(B, e)));
				}
			}
			paths = paths0;
		}

		if (fn2.size() < numarrs) {
			for (Entry<Integer, Path> k : fn2.entrySet()) {
				if (k.getValue() == null) {
					System.out.println("missing " + k.getKey());
					if (replacees.contains(k.getKey())) {
						throw new RuntimeException();
					}
				}
			}
			String old_str = "Basis path lengths exceed allowed limit ("
					+ DEBUG.debug.MAX_PATH_LENGTH + ").  Only have "
					+ fn2.size() + " basis paths out of required " + numarrs();
//					+ ".  Sig is " + sig;
			throw new FQLException(old_str);
		}
*/
/*
	private Pair<FinCat<Node, Path>, Fn<Path, Arr<Node, Path>>> toCategoryHelper(
			Signature sig, boolean b) throws FQLException {
		List<Node> objects = B.nodes;

		Set<Arr<Node, Path>> arrows = new HashSet<>();
		Map<Node, Arr<Node, Path>> identities = new HashMap<>();

		final Fn<Path, Integer> fn = makeFn();
		List<Path> paths = new LinkedList<>(); // B.pathsLessThan(DEBUG.debug.MAX_PATH_LENGTH);
		final Map<Integer, Path> fn2 = new HashMap<>();
		
		int numarrs = numarrs();
		for (Node n : B.nodes) {
			paths.add(new Path(B, n));
		}
		outer: for (int iter = 0; iter < DEBUG.debug.MAX_PATH_LENGTH; iter++) {
			for (Path p : paths) {
				Integer i = fn.of(p);
				if (fn2.get(i) == null) {
					fn2.put(i, p);
				}
				if (fn2.size() == numarrs) {
					break outer;
				}
			}
			List<Path> paths0 = new LinkedList<>();
			for (Path p : paths) {
				for (Edge e : B.outEdges(p.target)) {
					paths0.add(Path.append(B, p, new Path(B, e)));
				}
			}
			paths = paths0;
		}

		if (fn2.size() < numarrs) {
			for (Entry<Integer, Path> k : fn2.entrySet()) {
				if (k.getValue() == null) {
					System.out.println("missing " + k.getKey());
					if (replacees.contains(k.getKey())) {
						throw new RuntimeException();
					}
				}
			}
			String old_str = "Basis path lengths exceed allowed limit ("
					+ DEBUG.debug.MAX_PATH_LENGTH + ").  Only have "
					+ fn2.size() + " basis paths out of required " + numarrs() + "."
					+ "  Probable cause: using parallel or hybrid left-kan algorithm (see options).";
//					+ ".  Sig is " + sig;
			throw new FQLException(old_str);
		}

		for (Integer i : fn2.keySet()) {
			Path p = fn2.get(i);
			arrows.add(new Arr<>(p, p.source, p.target));
		}

		for (Node n : objects) {
			Arr<Node, Path> a = new Arr<>(fn2.get(etables.get(n).get(-1)), n, n);
			identities.put(n, a);
			// arrows.add(a);
		}
		for (Edge e : Ltables.keySet()) {
			for (Integer i : Ltables.get(e).keySet()) {
				Path p = fn2.get(i);
				arrows.add(new Arr<>(p, p.source, p.target));
			}
		} 

		Fn<Path, Arr<Node, Path>> r2 = new Fn<Path, Arr<Node, Path>>() {
			@Override
			public Arr<Node, Path> of(Path x) {
				if (fn2.get(fn.of(x)) == null) {
					throw new RuntimeException("Given path " + x
							+ ", transforms to " + fn.of(x)
							+ ", which is not in " + fn2);
				}
				return new Arr<>(fn2.get(fn.of(x)), x.source, x.target);
			}
		};

		Map<Pair<Arr<Node, Path>, Arr<Node, Path>>, Arr<Node, Path>> composition = new HashMap<>();
		for (Arr<Node, Path> x : arrows) {
			for (Arr<Node, Path> y : arrows) {
				if (x.dst.equals(y.src)) {
					composition.put(new Pair<>(x, y),
							r2.of(Path.append(B, x.arr, y.arr)));
				}
			}
		}

		if (b) {
			FinCat<Node, Path> r1 = new FinCat<Node, Path>(objects,
					new LinkedList<>(arrows), composition, identities);

			return new Pair<>(r1, r2);
		} else {
			return null;
		}
	}

	private int numarrs() {
		Set<Integer> x = new HashSet<>();
		for (Node n : etables.keySet()) {
			if (x.contains(etables.get(n).get(-1))) {
				throw new RuntimeException();
			}
			x.add(etables.get(n).get(-1));
		}
		for (Edge e : Ltables.keySet()) {
			x.addAll(Ltables.get(e).keySet());
			x.addAll(Ltables.get(e).values());
		}
		//TODO should only have nulls if computation incomplete
		Iterator<Integer> it = x.iterator();
		while (it.hasNext()) {
			if (it.next() == null) {
				it.remove();
			}
		}
//		 System.out.println(x.size());
		return x.size();
	}

	private Fn<Path, Integer> makeFn() {
		return new Fn<Path, Integer>() {
			@Override
			public Integer of(Path x) {
				Integer i = etables.get(x.source).get(-1);
				for (Edge e : x.path) {
					i = Ltables.get(e).get(i);
				}
				if (i == null) {
					throw new RuntimeException(); // TODO
				}
				return i;
			}

		};
	} */

	public Pair<Instance, Map<Integer, List<Pair<String, Integer>>>> sigma(
			PSMInterp interp) throws FQLException {
		Map<String, Set<Pair<Object, Object>>> data = new HashMap<>();

		if (!enumerate(DEBUG.debug.MAX_DENOTE_ITERATIONS)) {
			throw new FQLException("Too many full sigma enumerations");
		}

		for (Edge e : Ltables.keySet()) {
			Map<Integer, Integer> t = Ltables.get(e);
			if (!e.name.contains(" ")) {
				data.put(e.name, conc(t));
			} else {
				String x = e.name.substring(0, e.name.length() - 3);
				data.put(x, conc(t));
			}
		}
		Instance ret = new Instance(sig, data);
		// if (X != null) {
		// System.out.println("input " + X);
		// System.out.println("output " + ret);
		// System.out.println("lineage " + lineage);
		// }
		return new Pair<>(ret, lineage);
	}

	private Set<Pair<Object, Object>> conc(Map<Integer, Integer> t) {
		Set<Pair<Object, Object>> ret = new HashSet<>();

		for (Entry<Integer, Integer> i : t.entrySet()) {
			ret.add(new Pair<Object, Object>(i.getKey().toString(), i.getValue().toString()));
		}

		return ret;
	}

	public static Quad<Instance, Map<Node, Map<Integer, Integer>>, Map<Node, Map<Integer, Integer>>, Map<Integer, List<Pair<String, Integer>>>> fullSigmaWithAttrs(
			PSMInterp inter, Mapping f, Instance i, Transform t, Instance JJJ,
			Integer xxx) throws FQLException {

		// System.out.println("Called with " + f + " on instance " + i);
		Mapping F = deAttr(f);
		// System.out.println("de-attred " + F);
		Pair<Instance, Map<Attribute<Node>, Map<Object, Object>>> I = deAttr(
				inter, i, F.source);
		// System.out.println("deat2 " + I);
		Integer kkk = inter.guid;
		Instance JJJ0 = null;
		Transform ttt = null;
		
		//the de-attrd t depends on j. t : I =<
		if (JJJ != null) {
			inter.guid = xxx;

			Pair<Instance, Map<Attribute<Node>, Map<Object, Object>>> JJJ0X = deAttr(
					inter, JJJ, F.target);

//			Pair<Instance, Map<Attribute<Node>, Map<Object, Object>>> qqq = deAttr(
	//				inter, t.dst, F.source);
		
			Pair<Instance, Map<Attribute<Node>, Map<Object, Object>>> qqq = delta(f,
					F, JJJ0X);
			
			ttt = deAttr(f.source, I, qqq, t);


			JJJ0 = JJJ0X.first;
			inter.guid = kkk;
		}


		Denotation D = new Denotation(inter, F, I.first, ttt, JJJ0);

		Pair<Instance, Map<Integer, List<Pair<String, Integer>>>> hhh = D
				.sigma(inter);

		Instance j = hhh.first;
		// System.out.println("j " + j);
		Instance ret = reAttr(D, f.target, j, I.second);
		// System.out.println(" J " + J);
		return new Quad<>(ret, D.etables, D.utables, hhh.second);
	}

	private static Pair<Instance, Map<Attribute<Node>, Map<Object, Object>>> delta(Mapping f0,
			Mapping f,
			Pair<Instance, Map<Attribute<Node>, Map<Object, Object>>> p) throws FQLException {

		Map<String, Set<Pair<Object, Object>>> data = new HashMap<>();
		for (Node n : f.source.nodes) {
			data.put(n.string, p.first.data.get(f.nm.get(n).string));
		}
		for (Edge e : f.source.edges) {
			data.put(e.name, p.first.evaluate(f.em.get(e)));
		}
		
		Instance J = new Instance(f.source, data);
		
		Map<Attribute<Node>, Map<Object, Object>> m = new HashMap<>();
		for (Attribute<Node> a : f0.source.attrs) {
			m.put(a, p.second.get(f0.am.get(a)));
		}
		
		return new Pair<>(J, m);
	}

	// maps fresh ID to attribute
	private static Transform deAttr(Signature sig,
			Pair<Instance, Map<Attribute<Node>, Map<Object, Object>>> src,
			Pair<Instance, Map<Attribute<Node>, Map<Object, Object>>> dst,
			Transform trans) {
		List<Pair<String, List<Pair<Object, Object>>>> data = new LinkedList<>(
				trans.data());

		// System.out.println("src " + src);
		for (Attribute<Node> k : sig.attrs) {
			List<Pair<Object, Object>> list = new LinkedList<>();
			// Node n = k.source;
			Set<Pair<Object, Object>> s = src.first.data.get(k.name);
			Map<Object, Object> t = dst.second.get(k);
			if (t == null) {
				throw new RuntimeException();
			}
			for (Pair<Object, Object> i : s) {
				Object v = src.second.get(k).get(i.first); // v is the constant
				if (v == null) {
					throw new RuntimeException();
				}
				Object v0 = revLookup(t, v);
				if (v0 == null) {
					throw new RuntimeException();
				}
				list.add(new Pair<>(i.first, v0));
			}
			data.add(new Pair<>(k.name, list));
		}

		return new Transform(src.first, dst.first, data);
	}

	/*
	 * private static Object revLookup(Set<Pair<Object, Object>> map, Object x)
	 * { if (x == null) { throw new RuntimeException(); } for (Pair<Object,
	 * Object> k : map) { if (k.second.equals(x)) { return (Integer) k.first; }
	 * } throw new RuntimeException(); }
	 */

	private static Instance reAttr(Denotation D, Signature thesig, Instance i,
			Map<Attribute<Node>, Map<Object, Object>> map0) throws FQLException {
		Map<String, Set<Pair<Object, Object>>> d = new HashMap<>();

		for (Node k : i.thesig.nodes) {
			d.put(k.string, i.data.get(k.string));
		}
		for (Edge k : thesig.edges) {
			d.put(k.name, i.data.get(k.name));
		}
		Map<Object, Object> map = new HashMap<>();
		for (Attribute<Node> k : map0.keySet()) {
			Map<Object, Object> v = map0.get(k);
			for (Object k0 : v.keySet()) {
				Object v0 = v.get(k0);
				if (map.containsKey(k0)) {
					throw new RuntimeException();
				}
				map.put(k0, v0);
			}
		}

		for (Attribute<Node> k : thesig.attrs) {
			Set<Pair<Object, Object>> t = new HashSet<>();
			for (Pair<Object, Object> v : i.data.get(k.name + "_edge")) {
				//Integer v0 = (Integer) v.second;
				// System.out.println("etables is " + D.etables);
				// System.out.println("looking for " + k.name);
				// System.out.println("Map is " + map);
				// System.out.println("want " + k);
				Object v1 = getFrom(k, D, map /* ().get(k) */, v.second.toString());
				// System.out.println("v1 is " + v1);
				t.add(new Pair<Object, Object>(v.first, v1));
			}
			d.put(k.name, t);
		}
		return new Instance(thesig, d);
	}

	private static Object getFrom(Attribute<Node> attr, Denotation D,
			Map<Object, Object> saved, String newkey) {
		List<Pair<Integer, Object>> pre = new LinkedList<>();

		for (Node kkk : D.etables.keySet()) {
			Map<Integer, Integer> nt = D.etables.get(kkk);
			for (Integer k : nt.keySet()) {
				if (nt.get(k).toString().equals(newkey)) {
					if (saved.get(k.toString()) == null) {
						throw new RuntimeException();
					}
					pre.add(new Pair<>(k, saved.get(k.toString())));
				}
			}
		}
		// System.out.println("preimage " + pre);
		if (pre.size() == 0) {
			if (!DEBUG.debug.ALLOW_NULLS) {
				throw new RuntimeException(
						"Full sigma not surjective: transform is " + D.etables
								+ " saved " + saved + " new key " + newkey);
			}
		}
		Set<Object> x = new HashSet<>();
		for (Pair<Integer, Object> i : pre) {
			x.add(i.second);
		}
		if (x.size() > 1) {
			throw new RuntimeException("Full sigma not unique: transform is "
					+ D.etables + " saved " + saved + " new key " + newkey);
		}
		for (Object ret : x) {
			return ret;
		}
		if (DEBUG.debug.ALLOW_NULLS) {
			if (!(attr.target instanceof Type.Varchar)) {
				throw new RuntimeException(
						"Cannot create nulls for any type but string");
			}
			return "NULL" + newkey; // TODO null hack
		}
		throw new RuntimeException();
	}

	private static Pair<Instance, Map<Attribute<Node>, Map<Object, Object>>> deAttr(
			PSMInterp inter, Instance i, Signature sig) throws FQLException {
		Map<String, Set<Pair<Object, Object>>> d = new HashMap<>();
		Map<Attribute<Node>, Map<Object, Object>> ret = new HashMap<>();

		for (Node k : i.thesig.nodes) {
			d.put(k.string, i.data.get(k.string));
		}
		for (Edge k : i.thesig.edges) {
			d.put(k.name, i.data.get(k.name));
		}
		for (Attribute<Node> k : i.thesig.attrs) {
			Map<Object, Object> ret0 = new HashMap<>();
			Set<Pair<Object, Object>> tn = new HashSet<>();
			Set<Pair<Object, Object>> te = new HashSet<>();
			for (Pair<Object, Object> v : i.data.get(k.name)) {
				Object x = revLookup(ret0, v.second);
				if (x == null) {
					x = new Integer(inter.guid++);
					ret0.put(x.toString(), v.second);
				}
				tn.add(new Pair<Object, Object>(x.toString(), x.toString()));
				te.add(new Pair<Object, Object>(v.first, x.toString()));
			}
			ret.put(k, ret0);
			d.put(k.name, tn);
			d.put(k.name + "_edge", te);
		}
		return new Pair<>(new Instance(sig, d), ret);
	}

	private static Object revLookup(Map<Object, Object> map, Object x) {
		if (x == null) {
			throw new RuntimeException();
		}
		for (Object k : map.keySet()) {
			if (map.get(k).equals(x)) {
				return k;
			}
		}
		return null;
	}

	private static Mapping deAttr(Mapping f) throws FQLException {
		Mapping ret = f.clone();
		deAttr(ret.source);
		deAttr(ret.target);

		for (Attribute<Node> k : ret.am.keySet()) {
			Attribute<Node> v = ret.am.get(k);
			Node src = new Node(k.name);
			Node dst = new Node(v.name);
			Edge srcE = new Edge(k.name + "_edge", k.source, src);
			Edge dstE = new Edge(v.name + "_edge", v.source, dst);
			ret.nm.put(src, dst);
			ret.em.put(srcE, new Path(ret.target, dstE));
		}
		ret.am.clear();

		return ret;
	}

	private static void deAttr(Signature source) {
		for (Attribute<Node> a : source.attrs) {
			Node dst = new Node(a.name);
			source.nodes.add(dst);
			source.edges.add(new Edge(a.name + "_edge", a.source, dst));
		}
		source.attrs.clear();
		source.doColors();
	}

	public Denotation(PSMInterp inter, Mapping f, Instance I, Transform alpha,
			Instance J) throws FQLException {
		Map<String, Set<Pair<Object, Object>>> data = new HashMap<>(I.data);

		this.sig = f.target;
		this.interp = inter;

		Signature A = f.source.clone();
		for (Node n : A.nodes) {
			Edge e = new Edge(n.string + " id", n, n);
			A.edges.add(e);
			A.eqs.add(new Eq(new Path(A, n), new Path(A, e)));
			data.put(n.string + " id", data.get(n.string));
		}
		Signature B = f.target.clone();
		for (Node n : B.nodes) {
			Edge e = new Edge(n.string + " id", n, n);
			B.edges.add(e);
			B.eqs.add(new Eq(new Path(B, n), new Path(B, e)));
		}

		LinkedHashMap<Edge, Path> ff = new LinkedHashMap<>(f.em);
		for (Node n : A.nodes) {
			Node m = f.nm.get(n);
			ff.put(new Edge(n.string + " id", n, n), new Path(B, m));
		}

		this.A = A;
		this.B = B;
		this.F = new Mapping(false, A, B, f.nm, ff,
				new LinkedHashMap<Attribute<Node>, Attribute<Node>>());
		this.X = new Instance(A, data);
		this.R = B.eqs;

		this.J = J;
		this.alpha = alpha;

		initTables();
		makeJTables();

//		 JPanel p = view(); JFrame fr = new JFrame("Full Sigma");
	//	 fr.setContentPane(p); fr.pack(); fr.setSize(600, 400);
//		 fr.setVisible(true);

	}

	public Denotation(Signature sig) throws FQLException {
		this.sig = sig;

		B = sig;
		A = B.onlyObjects();
		X = A.terminal("-1");
		F = subset(A, B);
		R = B.eqs;

		initTables();
		makeJTables();
		interp = new PSMInterp();

//		 JPanel p = view(); JFrame fr = new JFrame("Denotation");
//		 fr.setContentPane(p); fr.pack(); fr.setSize(600, 400);
//		 fr.setVisible(true);

	}

	private Mapping subset(Signature a, Signature b) throws FQLException {
		List<Pair<String, String>> obm = new LinkedList<>();
		for (Node n : a.nodes) {
			obm.add(new Pair<>(n.string, n.string));
		}
		return new Mapping(true, a, b, obm,
				new LinkedList<Pair<String, String>>(),
				new LinkedList<Pair<String, List<String>>>());
	}

	Signature sig;
	// int levels;

	Signature A, B;
	Set<Eq> R;
	Instance X; // A-inst
	Mapping F; // A -> B
	Transform alpha;
	Instance J;

	// xxx0 are the column names for display

	// nodes in B
	Map<Node, Map<Integer, Integer>> utables;// = new HashMap<>(); //TODO XXX
	
	Map<Node, String[]> utables0;// = new HashMap<>();

	// nodes in A
	Map<Node, Map<Integer, Integer>> etables;// = new HashMap<>();
	Map<Node, String[]> etables0;// = new HashMap<>();
	Map<Node, Node> etables1;// = new HashMap<>();

	// edges in B
	Map<Edge, Map<Integer, Integer>> Ltables;// / = new HashMap<>();
	Map<Edge, String[]> Ltables0;// = new HashMap<>();
	Map<Edge, Pair<Node, Node>> Ltables1;// = new HashMap<>();

	// eq in B
	Map<Eq, List<Integer[]>> rtables;// = new HashMap<>();
	Map<Eq, String[]> rtables0;// = new HashMap<>();
	Map<Eq, Node[]> rtables1;// = new HashMap<>();
	Map<Eq, Integer> rtables2;// = new HashMap<>();
	Map<Eq, Edge[]> rtables3;// = new HashMap<>();

	// edges in A
	Map<Edge, List<Integer[]>> ntables;// = new HashMap<>();
	Map<Edge, String[]> ntables0;// = new HashMap<>();
	Map<Edge, Node[]> ntables1;// = new HashMap<>();
	Map<Edge, Integer> ntables2;// = new HashMap<>();
	Map<Edge, Edge[]> ntables3;// = new HashMap<>();

	Instance L; // B-inst
	// Map<String, Map<Integer, Integer>> e = new HashMap<>();

	// node in B (paper switch A and B between main text and appendix)
	// Map<Node, Set<Pair<Integer, Integer>>> SA;// = new HashMap<>();

	List<Pair<Integer, Integer>> SA;

	Map<Integer, List<Pair<String, Integer>>> lineage;

	// returns true if finished
	public boolean enumerate(int size) throws FQLException {
		// if (J == null) {
		// happens for category computation
		// throw new RuntimeException();
		// }
		initTables();
		// _FRESH = FRESH_START; // sig.nodes.size();// + sig.edges.size();

	//	List<Pair<String, JComponent>> l = new LinkedList<>();
		
		int xxx = 0;
		//System.out.println("start");
		while (notComplete()) {
	//		l.add(new Pair<String, JComponent>("start " + (xxx+1), viewCurrentX()));
			//showView("iteration " + xxx);
			
			if (xxxSig != null) {
				// toCategoryHelper(xxxSig, false);
			}
		//	System.out.println("***");
		//	System.out.println(this);
			// if (xxx % 100 == 0) {
			// System.out.println("-- " + xxx);
			// for (Edge k : Ltables.keySet()) {
			// System.out.println(k + ": " + Ltables.get(k).size());
			// }
			// }
			// fillInPartial();
			if (xxx++ >= size) {
		//		popup(l);
				return false;
			}
	//		 checkRtables() ;			
			
			Set<Pair<Node, Integer>> a0s;
			if (DEBUG.debug.kan_option.startsWith("Hybrid")) {
				a0s = undefined();
			} else if (DEBUG.debug.kan_option.startsWith("Parallel")) {
				a0s = undefinedFull();
			} else {
				a0s = undefinedO();
			}
//			Set<Pair<Node, Integer>> a0s = undefinedO();
//			Set<Pair<Node, Integer>> a0s = undefinedFull();
	//		Set<Pair<Node, Integer>> a0s = undefined(); 
	//		System.out.println("created " + a0s);
			if (a0s.size() == 0) {
				throw new RuntimeException("no smallest");
			}
			for (Pair<Node, Integer> a0 : a0s) {
				create(a0.first, a0.second);
			}
//			System.out.println("after filling in");
//			System.out.println(this);
		//	 checkRtables() ;
			//l.add(new Pair<String, JComponent>("filled in " + xxx, viewCurrentX()));
			deriveConsequences();
		//	l.add(new Pair<String, JComponent>(xxx + "consequences " + xxx, viewCurrentX()));
		//	l.add(new Pair<String, JComponent>(xxx + " consequences ", new JTextArea(SA.toString())));
//			System.out.println(this);
			// }
			// checkRtables() ;
			// Node a;
			// int x = 0;
			Pair<Integer, Integer> uv;
			while ((uv = take()) != null) {
				// Pair<Integer, Integer> uv = take(SA);
				// checkRtables() ;
				delete(uv); // update in place
		//		 checkRtables() ;
				replace(uv);
		//		 checkRtables() ;
				deriveConsequences();
				// checkRtables() ;
				// if (xxx >= size) {
				// return false;
				// }
//				System.out.println();
			//	l.add(new Pair<String, JComponent>(xxx + " after " + uv, viewCurrentX()));
//				System.out.println(this);
			}
			// checkRtables() ;
			dedupl();
		}
		return true;
	}
/*
	private void popup(List<Pair<String, JComponent>> l) {
		JTabbedPane ret = new JTabbedPane();
		for (Pair<String, JComponent> k : l) {
			ret.add(k.first, k.second);
		}
		
		JPanel p = new JPanel(new GridLayout(1,1));
		p.add(ret);
		JFrame f = new JFrame("X");
		f.setContentPane(p);
		f.pack();
		f.setSize(300, 300);
		f.show();
	} */

	@Override
	public String toString() {
		return "Denotation [\netables=" + etables 
				+ "\n Ltables=" + Ltables
				+ "\n SA="+ SA + "\n]";
	}

	private void create(Node n, int i) {
		// System.out.println("add " + i + " for " + n);

		for (Eq k : rtables.keySet()) {
			Node[] v = rtables1.get(k);
			if (!v[0].equals(n)) {
				continue;
			}
			Integer[] x = new Integer[v.length];
			x[0] = i;
			x[rtables2.get(k)] = i;

			rtables.get(k).add(x);
		}

		for (Edge k : Ltables1.keySet()) {
			Pair<Node, Node> v = Ltables1.get(k);
			if (!v.first.equals(n)) {
				continue;
			}
			Map<Integer, Integer> m = Ltables.get(k);
			// System.out.println("!!!!!!!!!");
			m.put(i, null);
		}

	}

	PSMInterp interp;

	private int fresh() {
		// System.out.println(interp.guid);
		return ++interp.guid;
	}

	/*
	 * private Set<Pair<Node, Integer>> smallest() throws FQLException { //
	 * ekeys2 = shift(ekeys2); Set<Pair<Node, Integer>> ret = new HashSet<>();
	 * for (Node n : B.nodes) { Set<Integer> is = undefined(n); for (Integer i :
	 * is) { ret.add(new Pair<>(n, i)); } } return ret; }
	 */

	List<Node> ekeys;
	// List<Node> ekeys2;
	List<Edge> lkeys;

	void updateLineage(String col, Integer old, Integer nw) {
		if (!lineage.containsKey(old)) {
			lineage.put(old, new LinkedList<Pair<String, Integer>>());
		}
		List<Pair<String, Integer>> l = new LinkedList<>(lineage.get(old));
		l.add(new Pair<>(col, old));
		lineage.put(nw, l);
	} 

	private Set<Pair<Node, Integer>> undefinedO() throws FQLException {

		Set<Pair<Node, Integer>> realret = new HashSet<>();

		for (Node n0 : ekeys) {
			Map<Integer, Integer> x = etables.get(n0);

			for (Integer i : x.keySet()) {
				if (i == null) {
					throw new RuntimeException();
				}
				if (x.get(i) == null) {
					int ret = fresh();
					x.put(i, ret);

					updateLineage(n0.string, i, new Integer(ret));

					if (alpha != null) {
						System.out.println("1*****");
						utables.get(n0).put(ret,
								lookup(alpha.data.get(n0.string), i));
					}

					realret.add(new Pair<>(etables1.get(n0), ret));
					ekeys = shift(ekeys);

					return realret;
				}
			}
		}

		 for (Edge e0 : lkeys) {
			Map<Integer, Integer> x = Ltables.get(e0);

			//int count = 0;
			for (Integer i : x.keySet()) {
				if (i == null) {
					throw new RuntimeException("very bad");
				}
				if (x.get(i) == null) {
					int ret = fresh();
					x.put(i, ret);
					updateLineage(e0.name, i, new Integer(ret));
				//	count++;

					if (alpha != null) {
						String sss = e0.name;
						if (sss.endsWith(" id")) {
							sss = sss.substring(0, sss.length() - 3);
						}
						Integer xxx = lookup(J.data.get(sss),
								utables.get(e0.source).get(i));
						utables.get(e0.target).put(ret, xxx);
					}

					realret.add(new Pair<>(e0.target, ret));
					lkeys = shift(lkeys);

					return realret;
				}
			}

		}
		return realret;
	}

	private Set<Pair<Node, Integer>> undefined() throws FQLException {
		// ekeys = shift(ekeys);

		Set<Pair<Node, Integer>> realret = new HashSet<>();

		for (Node n0 : etables.keySet()) {
			Map<Integer, Integer> x = etables.get(n0);

			for (Integer i : x.keySet()) {
				if (i == null) {
					throw new RuntimeException();
				}
				if (x.get(i) == null) {
					int ret = fresh();
					x.put(i, ret);

					updateLineage(n0.string, i, new Integer(ret));

					if (alpha != null) {
						//System.out.println("2***** ");
						//System.out.println(utables.keySet());
						utables.get(F.nm.get(n0)).put(ret,
								lookup(alpha.data.get(n0.string), i));
					/*	System.out.println("trigger " + i);
						System.out.println("alpha " + alpha);
						System.out.println("J " + J);
						System.out.println("X " + X);
						System.out.println("lineage " + lineage);
						System.out.println(this); */
					}

					realret.add(new Pair<>(etables1.get(n0), ret));
				}
			}
		}

		// lkeys = shift(lkeys);
		outer: for (Edge e0 : lkeys) {
			Map<Integer, Integer> x = Ltables.get(e0);

			int count = 0;
			for (Integer i : x.keySet()) {
				if (i == null) {
					throw new RuntimeException("very bad");
				}
				if (x.get(i) == null) {
					int ret = fresh();
					x.put(i, ret);
					updateLineage(e0.name, i, new Integer(ret));
					count++;

					if (alpha != null) {
						String sss = e0.name;
						if (sss.endsWith(" id")) {
							sss = sss.substring(0, sss.length() - 3);
						}
//						System.out.println("J " + J);
	//					System.out.println("utables " + utables);
		//				System.out.println("alpha " + alpha);
						//System.out.println("lin " + lineage);
						Integer xxx = lookup(J.data.get(sss),
								utables.get(e0.source).get(i));
						utables.get(e0.target).put(ret, xxx);
						//System.out.println("ok on " + );
					}

					realret.add(new Pair<>(e0.target, ret));
					if (count > 1) {
						continue outer;
					}
				}
			}

		}
		return realret;
	}

	private Set<Pair<Node, Integer>> undefinedFull() throws FQLException {
		// ekeys = shift(ekeys);

		Set<Pair<Node, Integer>> realret = new HashSet<>();

		for (Node n0 : etables.keySet()) {
			Map<Integer, Integer> x = etables.get(n0);

			for (Integer i : x.keySet()) {
				if (i == null) {
					throw new RuntimeException();
				}
				if (x.get(i) == null) {
					int ret = fresh();
					x.put(i, ret);

					updateLineage(n0.string, i, new Integer(ret));

					if (alpha != null) {
						System.out.println("3*****");

						utables.get(n0).put(ret,
								lookup(alpha.data.get(n0.string), i));
					}

					realret.add(new Pair<>(etables1.get(n0), ret));
				}
			}
		}

		// lkeys = shift(lkeys);
		 for (Edge e0 : lkeys) {
			Map<Integer, Integer> x = Ltables.get(e0);

		//	int count = 0;
			for (Integer i : x.keySet()) {
				if (i == null) {
					throw new RuntimeException("very bad");
				}
				if (x.get(i) == null) {
					int ret = fresh();
					x.put(i, ret);
					updateLineage(e0.name, i, new Integer(ret));
			//		count++;

					if (alpha != null) {
						String sss = e0.name;
						if (sss.endsWith(" id")) {
							sss = sss.substring(0, sss.length() - 3);
						}
						Integer xxx = lookup(J.data.get(sss),
								utables.get(e0.source).get(i));
						utables.get(e0.target).put(ret, xxx);
					}

					realret.add(new Pair<>(e0.target, ret));
				//	if (count > 1) {
				//		continue outer;
				//	}
				}
			}

		}
		return realret;
	}
	private Integer lookup(Set<Pair<Object, Object>> set, Integer i) {
		if (i == null) {
			throw new RuntimeException();
		}
		if (set == null) {
			throw new RuntimeException();
		}
		for (Pair<Object, Object> k : set) {
			if (k.first.equals(i.toString())) {
				return Integer.parseInt(k.second.toString());
			}
		}
		throw new RuntimeException("Cannot find " + i + " in " + set);
	}

	private <X> List<X> shift(List<X> l) {
		if (l.size() == 0) {
			return l;
		}
		List<X> ret = new LinkedList<>(l);
		X x = ret.remove(0);
		ret.add(x);
		return ret;
	}

	private void deriveConsequences() {
		// TODO why is SA getting reflexive replacements?
		fillInPartial();
		for (Eq e : rtables.keySet()) {
			List<Integer[]> v = rtables.get(e);
			Node[] c = rtables1.get(e);
			Integer n = rtables2.get(e);
			Node a = c[n - 1];
			Node b = c[c.length - 1];
			if (!a.equals(b)) {
				throw new RuntimeException();
			}
			if (n - 1 == c.length - 1) {
				throw new RuntimeException();
			}
			for (Integer[] row : v) {
				if (row[n - 1] != null && row[c.length - 1] != null
						&& !row[n - 1].equals(row[c.length - 1])) {
					if (row[n - 1] < row[c.length - 1]) {
						addToSA(new Pair<>(row[n - 1], row[c.length - 1]));
					} else {
						addToSA(new Pair<>(row[c.length - 1], row[n - 1]));
					}
				}
			}
		}
		for (Edge e : ntables.keySet()) {
			List<Integer[]> v = ntables.get(e);
			Node[] c = ntables1.get(e);
			int n = 3;
			Node a = c[n - 1];
			Node b = c[c.length - 1];
			if (!a.equals(b)) {
				throw new RuntimeException();
			}
			if (n - 1 == c.length - 1) {
				throw new RuntimeException();
			}
			for (Integer[] row : v) {
				if (row[n - 1] != null && row[c.length - 1] != null
						&& !(row[n - 1].equals(row[c.length - 1]))) {
					if (row[n - 1] < row[c.length - 1]) {
						addToSA(new Pair<>(row[n - 1], row[c.length - 1]));
					} else {
						addToSA(new Pair<>(row[c.length - 1], row[n - 1]));
					}
				}
			}
		}
	}

	private void fillInPartial() {
		for (Eq k : rtables.keySet()) {
			List<Integer[]> v = rtables.get(k);
			int n = rtables2.get(k);
			Edge[] e = rtables3.get(k);
			for (Integer[] row : v) {
				Integer last = row[0];
				for (int i = 1; i < n; i++) {
					if (last != null) {
						last = Ltables.get(e[i - 1]).get(last);
						row[i] = last;
					}
				}
				last = row[n];
				for (int i = n + 1; i < row.length; i++) {
					if (last != null) {
						last = Ltables.get(e[i - 2]).get(last);
						row[i] = last;
					}
				}
			}

		}

		for (Edge k : ntables.keySet()) {
			List<Integer[]> v = ntables.get(k);

			Node A2 = k.target;
			Node A1 = k.source;
			for (Integer[] row : v) {
				row[2] = etables.get(A2).get(row[1]);
			}
			for (Integer[] row : v) {
				row[4] = etables.get(A1).get(row[3]);
			}

			Edge[] e = ntables3.get(k);
			for (Integer[] row : v) {
				Integer last = row[4];
				for (int i = 5; i < row.length; i++) {
					if (last != null) {
						last = Ltables.get(e[i - 4]).get(last);
						row[i] = last;
					}
				}
			}
		}
	}

	private void checkRtables() {
		for (Eq e : rtables.keySet()) {
			for (Integer[] i : rtables.get(e)) {
				if (rtables1.get(e).length != i.length) {
					throw new RuntimeException();
				}
			}
		}
	}

	// private Integer find(Edge e, Integer i) {
	// return Ltables.get(e).get(i);
	// }

	private void dedupl() {
		// LTables cannot have duplicate rows because are maps
		for (Eq n : rtables.keySet()) {
			List<Integer[]> m = rtables.get(n);
			List<Integer[]> mm = new LinkedList<>();
			for (Integer[] x : m) {
				if (!contains(x, mm)) {
					mm.add(x);
				}
			}
			rtables.put(n, mm);
		}
	}

	private boolean contains(Integer[] i, List<Integer[]> l) {
		for (Integer[] x : l) {
			if (arreq(x, i)) {
				return true;
			}
		}
		return false;
	}

	private boolean arreq(Integer[] a, Integer[] b) {
		for (int i = 0; i < a.length; i++) {
			if (a[i] == null && b[i] == null) {
				continue;
			}
			if (a[i] == null || b[i] == null) {
				return false;
			}
			if (!a[i].equals(b[i])) {
				return false;
			}
		}
		return true;
	}

	List<Integer> replacees = new LinkedList<>();
	private void replace(Pair<Integer, Integer> uv) {
		// TODO also substitute in lineage - do need to replace columns?
		//List<Pair<Integer, Integer>> ret = new LinkedList<>();
		if (uv.second.equals(uv.first)) {
			throw new RuntimeException("xxx"); //TODO check
		}
		if (uv.first == null) {
			throw new RuntimeException("null first");
		}
		if (uv.second == null) {
			throw new RuntimeException("null second");
		}
		lineage.remove(uv.second);
		for (Integer k : lineage.keySet()) {
			List<Pair<String, Integer>> v = lineage.get(k);
			for (Pair<String, Integer> p : v) {
				if (p.second.equals(uv.second)) {
					// System.out.println("replace " + uv);
					p.second = uv.first;
				}
			}
		}

//		 System.out.println("replacing " + uv.second + " with " + uv.first);
		//if (replacees.contains(uv.second)) {
		//	throw new RuntimeException();
		//}
		// replacees.add(uv.second);
		for (Node n : etables.keySet()) {
			Map<Integer, Integer> m = etables.get(n);
			Map<Integer, Integer> mm = new HashMap<>();
			for (Entry<Integer, Integer> e : m.entrySet()) {
				Integer x = e.getKey().equals(uv.second) ? uv.first : e
						.getKey();
				Integer y = uv.second.equals(e.getValue()) ? uv.first : e
						.getValue();
				if (y == null && !(e.getValue() == null)) {
					throw new RuntimeException();
				}
				mm.put(x, y);
			}
			etables.put(n, mm);
		}
		
		for (Edge n : Ltables.keySet()) {
			Map<Integer, Integer> m = Ltables.get(n);
			Map<Integer, Integer> mm = new HashMap<>();
			for (Entry<Integer, Integer> e : m.entrySet()) {

				if (e.getKey().equals(uv.second)) {
					
				} else if (e.getValue() != null && e.getValue().equals(uv.second)) {
					mm.put(e.getKey(), uv.first);
				} else {
					mm.put(e.getKey(), e.getValue());
				}
				
/*				if (e.getKey().equals(uv.second) && e.getValue() == null) {
					mm.put(uv.first, null);
				} else if (!e.getKey().equals(uv.second) && e.getValue() == null) { 
					mm.put(e.getKey(), null);				
			    } else if (e.getKey().equals(uv.second) && e.getValue().equals(uv.second)) {
					mm.put(uv.first, uv.first);
				} else if (e.getValue().equals(uv.second)) {
					mm.put(e.getKey(), uv.first);
				} 
			    else if (e.getKey().equals(uv.second)) {
			    				    	
					//choice to make here - use e's value, or use uv.first's value
					Integer cand = mm.get(uv.first);
					if (cand != null) {
						cand = (cand.equals(uv.second) ? uv.first : cand);
					}

					if (e.getValue() == null && cand != null) {
						mm.put(uv.first, cand);
					} else if (e.getValue() != null && cand == null) {
						mm.put(uv.first, e.getValue());
					} else if (e.getValue() == null && cand == null) {
						mm.put(uv.first, null);
					} else if (e.getValue() != null && cand != null) {
						mm.put(uv.first, cand); 
				   // 		if (!e.getValue().equals(cand)) {
				   // 			ret.add(new Pair<>(cand, e.getValue()));
				   // 		}

					} else {
						throw new RuntimeException("uuu");
					}  
					*/

				
			
			}
			Ltables.put(n, mm);
		}
		
/*		for (Edge n : Ltables.keySet()) {
			Map<Integer, Integer> m = Ltables.get(n);
			Map<Integer, Integer> mm = new HashMap<>();
			for (Entry<Integer, Integer> e : m.entrySet()) {

				if (e.getKey().equals(uv.second)) {
					Integer kk = m.get(uv.first);
					Integer kk0 = e.getValue();
					Integer use = null;
					if (kk != null && kk0 != null) {
						use = kk < kk0 ? kk0 : kk; // doesn't matter
					} else if (kk == null) {
						use = kk0;
					} else if (kk0 == null) {
						use = kk;
					} else {

					}

					mm.put(uv.first, use);
				} else if (uv.second.equals(e.getValue())) {
					mm.put(e.getKey(), uv.first);
				} else {
					mm.put(e.getKey(), e.getValue());
				}
			}
			Ltables.put(n, mm);
		}
		*/
		//TODO this checks
		for (Edge n : Ltables.keySet()) {
			Map<Integer, Integer> m = Ltables.get(n);
			if (m.containsKey(uv.second)) {
				throw new RuntimeException("key");
			}
			if (m.containsValue(uv.second)) {
				throw new RuntimeException("value");
			}

		}

		for (Eq n : rtables.keySet()) {
			List<Integer[]> m = rtables.get(n);
			List<Integer[]> mm = new LinkedList<>();
			for (Integer[] e : m) {
				Integer[] ee = new Integer[e.length];
				int i = 0;
				for (Integer x : e) {
					Integer xx = uv.second.equals(x) ? uv.first : x;
					ee[i++] = xx;
				}
				mm.add(ee);
			}
			rtables.put(n, mm);
		}

		// TODO hack with id generator

		if (alpha != null) {
			for (Node k : utables.keySet()) {
				Map<Integer, Integer> v = utables.get(k);
				Integer i1 = v.get(uv.second);
				Integer i2 = v.get(uv.first);
				if (i1 != null && i2 != null && !i1.equals(i2)) {
					throw new RuntimeException();
				}
				v.remove(uv.second);
			}
		}
		
		//return ret;
	}
	
	private void addToSA(Pair<Integer, Integer> uv) {
		if (uv.first.equals(uv.second)) {
			return;
		}
		if (SA.contains(uv) || SA.contains(new Pair<>(uv.second, uv.first)) ) {
			return;
		}
		if (uv.second > uv.first) {
			SA.add(uv);
		} else {
			SA.add(new Pair<>(uv.second, uv.first));
		}
	}

	// suppose a -> b
	// if have a -> c, now have b -> c
	// if have c -> a, now have c -> b
	// if have c -> b, now have
	private void delete(Pair<Integer, Integer> uv) {
		//System.out.println("deleting  " + uv);
		//int i = 0;

		for (;;) {
		Set<Pair<Integer, Integer>> newX = new HashSet<>(SA);
		SA.clear();
		//System.out.println("it" + i++);

		for (Pair<Integer, Integer> k : newX) {
			if (k.second.equals(uv.second)) {
				Pair<Integer, Integer> toAdd = new Pair<>(k.first, uv.first);
					addToSA(toAdd);
			} else if (k.first.equals(uv.second)) {
				Pair<Integer, Integer> toAdd = new Pair<>(uv.first, k.second);
				addToSA(toAdd); 
			}
		}
		if (new HashSet<>(SA).equals(newX)) {
			break;
		}
		}
		
		for (Edge e : Ltables.keySet()) {
			Integer gu = Ltables.get(e).get(uv.first);
			Integer gv = Ltables.get(e).get(uv.second);
			if (gu != null && gv != null && !gu.equals(gv)) {
					addToSA(new Pair<>(gu, gv));
			}
		}
	}

	private Pair<Integer, Integer> take() {
		if (SA.isEmpty()) {
			return null;
		}
		Pair<Integer, Integer> ret = null;
		for (Pair<Integer, Integer> p : SA) {
			ret = p;
			break;
		}
		SA.remove(ret);
		return ret;
	}

	private boolean notComplete() {
		for (Node n : etables.keySet()) {
			Map<Integer, Integer> xxx = etables.get(n);
			for (Integer yyy : xxx.keySet()) {
				if (yyy == null) {
					throw new RuntimeException();
				}
				if (xxx.get(yyy) == null) {
					return true;
				}
			}
		}

		for (Edge k : Ltables.keySet()) {
			Map<Integer, Integer> v = Ltables.get(k);
			for (Integer i : v.keySet()) {
				if (i == null) {
					throw new RuntimeException();
				}
				if (v.get(i) == null) {
					return true;
				}
			}
		}
		return false;
	}

	public void initTables() throws FQLException {
		lineage = new HashMap<>();

		utables = new HashMap<>();
		utables0 = new HashMap<>();

		etables = new HashMap<>();
		etables0 = new HashMap<>();
		etables1 = new HashMap<>();

		// edges in B
		Ltables = new HashMap<>();
		Ltables0 = new HashMap<>();
		Ltables1 = new HashMap<>();

		// eq in B
		rtables = new HashMap<>();
		rtables0 = new HashMap<>();
		rtables1 = new HashMap<>();
		rtables2 = new HashMap<>();
		rtables3 = new HashMap<>();

		// edges in A
		ntables = new HashMap<>();
		ntables0 = new HashMap<>();
		ntables1 = new HashMap<>();
		ntables2 = new HashMap<>();
		ntables3 = new HashMap<>();

		// node in B (paper switch A and B between main text and appendix)
		SA = new LinkedList<>();

		for (Node a : A.nodes) {
			Map<Integer, Integer> etable = new HashMap<>();
			for (Pair<Object, Object> p : X.data.get(a.string)) {
				if (p.first instanceof String) {
					etable.put(Integer.parseInt((String) p.first), null);
				} else {
					etable.put((Integer) p.first, null);
				}
			}
			String[] cnames = new String[2];
			cnames[0] = "X(" + a.string + ")";
			Node zzz = F.nm.get(a);
			cnames[1] = "L(" + zzz + ")";
			etables0.put(a, cnames);
			etables.put(a, etable);
			etables1.put(a, zzz);
		}
		for (Node b : B.nodes) {
			if (alpha != null) {
				utables.put(b, new HashMap<Integer, Integer>());
				//System.out.println("add to utable " + b);
				utables0.put(b, new String[] { "in", "out" });
			}
		}
		ekeys = new LinkedList<>(A.nodes);
		// ekeys2 = new LinkedList<>(B.nodes);
		for (Edge g : B.edges) {
			Ltables.put(g, new HashMap<Integer, Integer>());
			String[] cnames = new String[2];
			cnames[0] = "L(" + g.source.string + ")";
			cnames[1] = "L(" + g.target.string + ")";
			Ltables0.put(g, cnames);
			Ltables1.put(g, new Pair<>(g.source, g.target));
		}
		lkeys = new LinkedList<>(B.edges);
		for (Eq eq : R) {
			List<String> c = new LinkedList<>();
			List<Node> cc = new LinkedList<>();
			List<Edge> ccc = new LinkedList<>();
			c.add("L(" + eq.lhs.source.string + ")");
			cc.add(eq.lhs.source);
			for (Edge e : eq.lhs.path) {
				c.add("L(" + e.target.string + ")");
				cc.add(e.target);
				ccc.add(e);
			}
			c.add("L(" + eq.rhs.source.string + ")");
			rtables2.put(eq, cc.size());
			cc.add(eq.rhs.source);
			for (Edge e : eq.rhs.path) {
				c.add("L(" + e.target.string + ")");
				cc.add(e.target);
				ccc.add(e);
			}
			rtables.put(eq, new LinkedList<Integer[]>());
			rtables0.put(eq, c.toArray(new String[] {}));
			rtables1.put(eq, cc.toArray(new Node[] {}));
			rtables3.put(eq, ccc.toArray(new Edge[] {}));
		}
		checkRtables();
		for (Edge f : A.edges) {
			List<Edge> ccc = new LinkedList<>();
			ccc.add(f);
			Path g = F.em.get(f);
			List<String> c = new LinkedList<>();
			List<Node> cc = new LinkedList<>();
			c.add("X(" + f.source.string + ")");
			cc.add(f.source);
			c.add("X(" + f.target.string + ")");
			cc.add(f.target);
			String xxx = F.nm.get(f.target).string;
			c.add("L(" + xxx + ")");
			cc.add(F.nm.get(f.target));
			c.add("X(" + f.source.string + ")");
			cc.add(f.source);
			c.add("L(" + g.source.string + ")");
			cc.add(g.source);
			ntables2.put(f, cc.size());
			for (Edge e : g.path) {
				c.add("L(" + e.target.string + ")");
				cc.add(e.target);
				ccc.add(e);
			}
			ntables0.put(f, c.toArray(new String[] {}));
			ntables1.put(f, cc.toArray(new Node[] {}));

			List<Integer[]> l = new LinkedList<>();
			for (Pair<Object, Object> x : X.data.get(f.name)) {
				Integer[] r = new Integer[4 + 1 + g.path.size()];
				r[0] = Integer.parseInt(x.first.toString());
				r[1] = Integer.parseInt(x.second.toString());
				r[3] = Integer.parseInt(x.first.toString());

				// if (x.first instanceof String) {
				// r[0] = Integer.parseInt((String) x.first);
				// r[1] = Integer.parseInt((String) x.second);
				// r[3] = Integer.parseInt((String) x.first);
				// } else {
				// r[0] = (Integer) x.first;
				// r[1] = (Integer) x.second;
				// r[3] = (Integer) x.first;
				// }
				l.add(r);
			}

			ntables.put(f, l);
			ntables3.put(f, ccc.toArray(new Edge[] {}));
		}

	}
/*
	private JPanel makePanels(Map<String, JTable> in) {
		List<JPanel> ret = new LinkedList<>();

		Comparator<String> strcmp = new Comparator<String>() {
			public int compare(String f1, String f2) {
				return f1.compareTo(f2);
			}
		};

		List<String> xxx = new LinkedList<>(in.keySet());
		Collections.sort(xxx, strcmp);

		for (String name : xxx) {
			JTable t = in.get(name);
			JPanel p = new JPanel(new GridLayout(1, 1));
			// p.add(t);
			p.add(new JScrollPane(t));
			// p.setMaximumSize(new Dimension(200,200));
			p.setBorder(BorderFactory.createTitledBorder(
					BorderFactory.createEmptyBorder(), name));
			ret.add(p);
		}

		int x = (int) Math.ceil(Math.sqrt(ret.size()));
		if (x == 0) {
			x = 1;
		}
		JPanel panel = new JPanel(new GridLayout(x, x));
		for (JPanel p : ret) {
			panel.add(p);
		}
		// panel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		return panel;

	}
*/
	Map<Node, DefaultTableModel> et = new HashMap<>();
	Map<Edge, DefaultTableModel> lt = new HashMap<>();
	Map<Eq, DefaultTableModel> rt = new HashMap<>();
	Map<Edge, DefaultTableModel> nt = new HashMap<>();
	Map<Node, DefaultTableModel> ut = new HashMap<>();

	public void makeJTables() {

		for (Node n : etables0.keySet()) {
			et.put(n,
					new DefaultTableModel(graph(etables.get(n)), etables0
							.get(n)));
		}
		for (Edge e : Ltables.keySet()) {
			lt.put(e,
					new DefaultTableModel(graph(Ltables.get(e)), Ltables0
							.get(e)));
		}
		for (Eq e : rtables.keySet()) {
			rt.put(e,
					new DefaultTableModel(graph2(rtables.get(e),
							rtables0.get(e).length), rtables0.get(e)));
		}
		for (Edge e : ntables.keySet()) {
			nt.put(e,
					new DefaultTableModel(graph2(ntables.get(e),
							ntables0.get(e).length), ntables0.get(e)));
		}
		if (alpha != null) {
			for (Node n : utables.keySet()) {
				ut.put(n,
						new DefaultTableModel(graph(utables.get(n)), utables0
								.get(n)));
			}
		}
	}
/*
	public void updateView(int x) throws FQLException {
		enumerate(x);
		for (Node n : etables0.keySet()) {
			et.get(n).setDataVector(graph(etables.get(n)), etables0.get(n));
		}
		for (Edge e : Ltables.keySet()) {
			lt.get(e).setDataVector(graph(Ltables.get(e)), Ltables0.get(e));
		}
		for (Eq e : rtables.keySet()) {
			rt.get(e).setDataVector(
					graph2(rtables.get(e), rtables0.get(e).length),
					rtables0.get(e));
		}
		for (Edge e : ntables.keySet()) {
			nt.get(e).setDataVector(
					graph2(ntables.get(e), ntables0.get(e).length),
					ntables0.get(e));
		}
		if (alpha != null) {
			for (Node n : utables0.keySet()) {
				ut.get(n).setDataVector(graph(utables.get(n)), utables0.get(n));
			}
		}
	} */
	
/*	void showView(String s) {
		JPanel p = new JPanel(new GridLayout(1,1));
		p.add(viewCurrentX());
		JFrame f = new JFrame(s);
		f.setContentPane(p);
		f.pack();
		f.setSize(300, 300);
		f.show();
	} */
/*
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public JTabbedPane view(int l) throws FQLException {
		enumerate(l);
		return viewCurrent();
	}
	*/
	/*
	private JTabbedPane viewCurrentX() {
		JTabbedPane t = new JTabbedPane();

		Map<Node, DefaultTableModel> et = new HashMap<>();
		Map<Edge, DefaultTableModel> lt = new HashMap<>();
		Map<Eq, DefaultTableModel> rt = new HashMap<>();
		Map<Edge, DefaultTableModel> nt = new HashMap<>();
		Map<Node, DefaultTableModel> ut = new HashMap<>();

		for (Node n : etables0.keySet()) {
			et.put(n,
					new DefaultTableModel(graph(etables.get(n)), etables0
							.get(n)));
		}
		for (Edge e : Ltables.keySet()) {
			lt.put(e,
					new DefaultTableModel(graph(Ltables.get(e)), Ltables0
							.get(e)));
		}
		for (Eq e : rtables.keySet()) {
			rt.put(e,
					new DefaultTableModel(graph2(rtables.get(e),
							rtables0.get(e).length), rtables0.get(e)));
		}
		for (Edge e : ntables.keySet()) {
			nt.put(e,
					new DefaultTableModel(graph2(ntables.get(e),
							ntables0.get(e).length), ntables0.get(e)));
		}
		if (alpha != null) {
			for (Node n : utables.keySet()) {
				ut.put(n,
						new DefaultTableModel(graph(utables.get(n)), utables0
								.get(n)));
			}
		}

	//	Triple<JPanel, JPanel, JPanel> xxx = toCat();
	//	t.addTab("Category", xxx.first);
	//	 t.addTab("Signature", xxx.third);
	//	t.addTab("Normalizer", xxx.second);

		Map<String, JTable> m = new HashMap<>();
		for (Node n : etables0.keySet()) {
			// et.get(n).en`
			JTable table = new JTable(et.get(n));
			TableRowSorter<?> sorter = new TableRowSorter(table.getModel());
			table.setRowSorter(sorter);
			sorter.allRowsChanged();
			// sorter.toggleSortOrder(0);

			m.put("e_" + n.string, table);
		}
		t.addTab("e-Tables", makePanels(m));

		m = new HashMap<>();
		for (Edge e : Ltables.keySet()) {
			JTable table = new JTable(lt.get(e));
			TableRowSorter<?> sorter = new TableRowSorter(table.getModel());
			table.setRowSorter(sorter);
			sorter.allRowsChanged();
			// sorter.toggleSortOrder(0);

			m.put("L(" + e.name + ")", table);
		}
		t.addTab("L-Tables", makePanels(m));

		m = new HashMap<>();
		for (Eq e : rtables.keySet()) {
			JTable table = new JTable(rt.get(e));
			TableRowSorter<?> sorter = new TableRowSorter(table.getModel());
			table.setRowSorter(sorter);
			sorter.allRowsChanged();
			// sorter.toggleSortOrder(0);

			m.put(e.toString(), table);
		}
		t.addTab("Relation Tables", makePanels(m));

		m = new HashMap<>();
		for (Edge e : ntables.keySet()) {
			JTable table = new JTable(nt.get(e));
			TableRowSorter<?> sorter = new TableRowSorter(table.getModel());
			table.setRowSorter(sorter);
			sorter.allRowsChanged();
			// sorter.toggleSortOrder(0);

			m.put("F" + e.name + " = " + F.em.get(e), table);
		}
		if (alpha != null) { //TODO XXX this should go down one line?
			t.addTab("Naturality Tables", makePanels(m));
			m = new HashMap<>();
			for (Node n : utables.keySet()) {
				JTable table = new JTable(ut.get(n));
				TableRowSorter<?> sorter = new TableRowSorter(table.getModel());
				table.setRowSorter(sorter);
				sorter.allRowsChanged();
				// sorter.toggleSortOrder(0);

				m.put("alpha " + n.string, table);
			}
			t.addTab("Universal Tables", makePanels(m));
		}
		return t;
	}

	private JTabbedPane viewCurrent() {
		JTabbedPane t = new JTabbedPane();

		Triple<JPanel, JPanel, JPanel> xxx = toCat();
		t.addTab("Category", xxx.first);
		t.addTab("Signature", xxx.third);
		t.addTab("Normalizer", xxx.second);

		Map<String, JTable> m = new HashMap<>();
		for (Node n : etables0.keySet()) {
			// et.get(n).en`
			JTable table = new JTable(et.get(n));
			TableRowSorter<?> sorter = new TableRowSorter(table.getModel());
			table.setRowSorter(sorter);
			sorter.allRowsChanged();
			// sorter.toggleSortOrder(0);

			m.put("e_" + n.string, table);
		}
		t.addTab("e-Tables", makePanels(m));

		m = new HashMap<>();
		for (Edge e : Ltables.keySet()) {
			JTable table = new JTable(lt.get(e));
			TableRowSorter<?> sorter = new TableRowSorter(table.getModel());
			table.setRowSorter(sorter);
			sorter.allRowsChanged();
			// sorter.toggleSortOrder(0);

			m.put("L(" + e.name + ")", table);
		}
		t.addTab("L-Tables", makePanels(m));

		m = new HashMap<>();
		for (Eq e : rtables.keySet()) {
			JTable table = new JTable(rt.get(e));
			TableRowSorter<?> sorter = new TableRowSorter(table.getModel());
			table.setRowSorter(sorter);
			sorter.allRowsChanged();
			// sorter.toggleSortOrder(0);

			m.put(e.toString(), table);
		}
		t.addTab("Relation Tables", makePanels(m));

		m = new HashMap<>();
		for (Edge e : ntables.keySet()) {
			JTable table = new JTable(nt.get(e));
			TableRowSorter<?> sorter = new TableRowSorter(table.getModel());
			table.setRowSorter(sorter);
			sorter.allRowsChanged();
			// sorter.toggleSortOrder(0);

			m.put("F" + e.name + " = " + F.em.get(e), table);
		}
		if (alpha != null) {
			t.addTab("Naturality Tables", makePanels(m));
			m = new HashMap<>();
			for (Node n : utables.keySet()) {
				JTable table = new JTable(ut.get(n));
				TableRowSorter<?> sorter = new TableRowSorter(table.getModel());
				table.setRowSorter(sorter);
				sorter.allRowsChanged();
				// sorter.toggleSortOrder(0);

				m.put("alpha " + n.string, table);
			}
			t.addTab("Universal Tables", makePanels(m));
		}
		return t;
	} */
/*
	private Triple<JPanel, JPanel, JPanel> toCat() {
		JPanel p = new JPanel(new GridLayout(1, 1));
		JTextArea a = new JTextArea();
		JPanel q = null;
		JPanel rr = new JPanel(new GridLayout(1, 1));

		try {

			Pair<FinCat<Node, Path>, Fn<Path, Arr<Node, Path>>> xxx = toCategory(null);

			a.setText(xxx.first.toString());
			a.setCaretPosition(0);

			q = makeNormalizer(xxx.second);

			 JTextArea ra = new JTextArea(
			 xxx.first.toSig(new HashMap<String, Type>()).first
			 .toString());
			 rr.add(new JScrollPane(ra));

		} catch (Throwable e) {

			e.printStackTrace();
			a.setText(e.getMessage());
		}
		p.add(new JScrollPane(a));

		return new Triple<>(p, q, rr);
	}
*/ /*
	private JPanel makeNormalizer(final Fn<Path, Arr<Node, Path>> f) {
		final JPanel ret = new JPanel(new BorderLayout());

		JPanel p = new JPanel(new GridLayout(2, 1));
		final FQLTextPanel p1 = new FQLTextPanel("Input path", "");
		final FQLTextPanel p2 = new FQLTextPanel("Normalized path", "");
		p.add(p1);
		p.add(p2);

		JButton b = new JButton("Normalize");
		b.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = p1.getText();
				try {
					Path path = Path.parsePath(sig, s);
					Path ap = f.of(path).arr;
					p2.setText(ap.toString());
				} catch (FQLException ex) {
					p2.setText(ex.toString());
				}
			}
		});

		ret.add(p, BorderLayout.CENTER);
		ret.add(b, BorderLayout.PAGE_END);

		return ret;
	} */

/*	public JPanel view() throws FQLException {
		final JPanel ret = new JPanel(new BorderLayout());

		JTabbedPane t = view(0);
		ret.add(t, BorderLayout.CENTER);

		final JSlider slider = new JSlider(0, LIMIT, 0);
		slider.setLabelTable(slider.createStandardLabels(INC));
		slider.setPaintLabels(true);
		slider.setMajorTickSpacing(INC);
		slider.setSnapToTicks(true);
		slider.setMinorTickSpacing(INC);
		ret.add(slider, BorderLayout.NORTH);

		slider.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				try {
					JSlider source = (JSlider) e.getSource();
					if (!source.getValueIsAdjusting()) {
						updateView(slider.getValue());
					}
				} catch (FQLException ee) {
					throw new RuntimeException(ee);
				}
			}

		});

		ret.setBorder(BorderFactory.createEtchedBorder());
		return ret; 
	} */

	private <X, Y> Object[][] graph2(List<X[]> list, int n) {
		Object[][] ret = new Object[list.size()][n];

		int i = 0;
		for (X[] s : list) {
			ret[i] = s;
			i++;
		}

		return ret;
	}

	private <X, Y> Object[][] graph(Map<X, Y> map) {
		Object[][] ret = new Object[map.size()][2];
		int i = 0;
		for (X k : map.keySet()) {
			Object[] c = new Object[2];
			c[0] = k;
			c[1] = map.get(k);
			ret[i] = c;
			i++;
		}
		return ret;
	}

}
