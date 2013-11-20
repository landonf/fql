package fql.cat;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;

import fql.DEBUG;
import fql.FQLException;
import fql.Fn;
import fql.Pair;
import fql.Triple;
import fql.decl.Edge;
import fql.decl.Eq;
import fql.decl.Instance;
import fql.decl.Mapping;
import fql.decl.Node;
import fql.decl.Path;
import fql.decl.Signature;
import fql.decl.Type;
import fql.gui.FQLTextPanel;
import fql.sql.PSMInterp;

/**
 * 
 * @author ryan
 *
 * Class for creating finite categories from signatures.
 * Implements the Carmody-Walters algorithm.
 */
public class Denotation {

	int counter = 0;
	static int LIMIT = 20, INC = 1;
	int _FRESH;
	int FRESH_START = 0;

	// does not copy
	public Pair<FinCat<Node, Path>, Fn<Path, Arr<Node, Path>>> toCategory()
			throws FQLException {

		if (!enumerate(DEBUG.MAX_DENOTE_ITERATIONS)) {
			throw new FQLException("Category size exceeds allowed limit");
		}

		List<Node> objects = B.nodes;

		Set<Arr<Node, Path>> arrows = new HashSet<>();
		Map<Node, Arr<Node, Path>> identities = new HashMap<>();

		final Fn<Path, Integer> fn = makeFn();
		List<Path> paths = B.pathsLessThan(DEBUG.MAX_PATH_LENGTH);
		final Map<Integer, Path> fn2 = new HashMap<>();
		for (Path p : paths) {
			Integer i = fn.of(p);
			if (fn2.get(i) == null || fn2.get(i).path.size() > p.path.size()) {
				fn2.put(i, p);
			}
		}
		if (fn2.size() < numarrs()) {
			throw new FQLException("Basis path lengths exceed allowed limit");
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
					throw new RuntimeException("Given path " + x + ", transforms to " + fn.of(x) + ", which is not in " + fn2 );
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

		FinCat<Node, Path> r1 = new FinCat<Node, Path>(objects,
				new LinkedList<>(arrows), composition, identities);

		// System.out.println("&&&&&&&&&&&&&&&&");
		// System.out.println(r1);
		// System.out.println("&&&&&&&&&&&&&&&&");
		return new Pair<>(r1, r2);
	}

	private int numarrs() {
		Set<Integer> x = new HashSet<>();
		for (Node n : etables.keySet()) {
			x.add(etables.get(n).get(-1));
		}
		for (Edge e : Ltables.keySet()) {
			x.addAll(Ltables.get(e).keySet());
			x.addAll(Ltables.get(e).values());
		}
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
				return i;
			}

		};
	}
	
	public Instance sigma() throws FQLException {
		Map<String, Set<Pair<Object, Object>>> data = new HashMap<>();
		
		if (!enumerate(128)) {
			throw new FQLException("Too many enumerations");
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
		PSMInterp.guid = _FRESH + 1;
		return new Instance(sig, data);
	}
	
	private Set<Pair<Object, Object>> conc(Map<Integer, Integer> t) {
		Set<Pair<Object, Object>> ret = new HashSet<>();
		
		for (Entry<Integer, Integer> i : t.entrySet()) {
			ret.add(new Pair<Object, Object>(i.getKey(), i.getValue()));
		}
		
		return ret;
	}

	public Denotation(Mapping f, Instance I) throws FQLException {
		FRESH_START = PSMInterp.guid + 1;
		//FRESH_START = 100;
		Map<String, Set<Pair<Object, Object>>> data
		 = new HashMap<>(I.data);
		 
		this.sig = f.target;
		 
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
		this.F = new Mapping(A, B, f.nm, ff); 
		this.X = new Instance(A, data); 
		this.R = B.eqs;
		
		
		initTables();
		makeJTables();
		
		/*
		JPanel p = view();
		JFrame fr = new JFrame("Full Sigma");
		fr.setContentPane(p);
		fr.pack();
		fr.setSize(600,400);
		fr.setVisible(true);
		*/
		
		
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
	}

	private Mapping subset(Signature a, Signature b) throws FQLException {
		List<Pair<String, String>> obm = new LinkedList<>();
		for (Node n : a.nodes) {
			obm.add(new Pair<>(n.string, n.string));
		}
		return new Mapping(true, a, b, obm, new LinkedList<Pair<String, String>>(),
				new LinkedList<Pair<String, List<String>>>());
	}

	Signature sig;
	int levels;

	Signature A, B;
	Set<Eq> R;
	Instance X; // A-inst
	Mapping F; // A -> B

	// xxx0 are the column names for display

	// nodes in A
	Map<Node, Map<Integer, Integer>> etables = new HashMap<>();
	Map<Node, String[]> etables0 = new HashMap<>();
	Map<Node, Node> etables1 = new HashMap<>();

	// edges in B
	Map<Edge, Map<Integer, Integer>> Ltables = new HashMap<>();
	Map<Edge, String[]> Ltables0 = new HashMap<>();
	Map<Edge, Pair<Node, Node>> Ltables1 = new HashMap<>();

	// eq in B
	Map<Eq, List<Integer[]>> rtables = new HashMap<>();
	Map<Eq, String[]> rtables0 = new HashMap<>();
	Map<Eq, Node[]> rtables1 = new HashMap<>();
	Map<Eq, Integer> rtables2 = new HashMap<>();
	Map<Eq, Edge[]> rtables3 = new HashMap<>();

	// edges in A
	Map<Edge, List<Integer[]>> ntables = new HashMap<>();
	Map<Edge, String[]> ntables0 = new HashMap<>();
	Map<Edge, Node[]> ntables1 = new HashMap<>();
	Map<Edge, Integer> ntables2 = new HashMap<>();
	Map<Edge, Edge[]> ntables3 = new HashMap<>();

	Instance L; // B-inst
	Map<String, Map<Integer, Integer>> e = new HashMap<>();

	//node in B (paper switch A and B between main text and appendix)
	Map<Node, Set<Pair<Integer, Integer>>> SA = new HashMap<>();

	// returns true if finished
	public boolean enumerate(int size) throws FQLException {
		initTables();
		_FRESH = FRESH_START; // sig.nodes.size();// + sig.edges.size();

		int xxx = 0;
		while (notComplete()) {
			// fillInPartial();
			if (xxx++ >= size) {
				return false;
			}
			// checkRtables() ;
			Pair<Node, Integer> a0 = smallest();
			if (a0 == null) {
				throw new RuntimeException("no smallest");
			}
			create(a0.first, a0.second);
			// checkRtables() ;
			deriveConsequences();
			// checkRtables() ;
			Node a;
			// int x = 0;
			while ((a = findNonemptySa()) != null) {
				Pair<Integer, Integer> uv = take(SA.get(a));
				// checkRtables() ;
				delete(a, SA, uv); // update in place
				// checkRtables() ;
				replace(uv);
				// checkRtables() ;
				deriveConsequences();
				// checkRtables() ;
				// if (xxx >= size) {
				// return false;
				// }

			}
			// checkRtables() ;
			dedupl();
		}
		return true;
	}

	@Override
	public String toString() {
		return "Denotation [etables=" + etables + ", etables0=" + etables0
				+ ", etables1=" + etables1 + ", Ltables=" + Ltables
				+ ", Ltables0=" + Ltables0 + ", Ltables1=" + Ltables1
				+ ", rtables=" + rtables + ", rtables0=" + rtables0
				+ ", rtables1=" + rtables1 + ", rtables2=" + rtables2
				+ ", ntables=" + ntables + ", ntables0=" + ntables0
				+ ", ntables1=" + ntables1 + ", SA=" + SA + "]";
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

	private int fresh() {
		return _FRESH++;
	}

	private Pair<Node, Integer> smallest() throws FQLException {
		for (Node n : B.nodes) {
			Integer i = hasUndefined(n);
			if (i != null) {
				return new Pair<>(n, i);
			}
		}
		throw new RuntimeException("no smallest");
	}

	List<Node> ekeys;
	List<Edge> lkeys;

	// check columns headed by n for undefined elements
	// needs to use all possible orders for the nodes and edges
	private Integer hasUndefined(Node n) throws FQLException {
		ekeys = shift(ekeys);

		for (Node n0 : ekeys) {
			if (!etables1.get(n0).equals(n)) {
				continue;
			}
			Map<Integer, Integer> x = etables.get(n0);
			if (x == null) {
				throw new RuntimeException("No node " + n + " in "
						+ Ltables.keySet());
			}
			// System.out.println("baz");
			for (Integer i : x.keySet()) {
				if (i == null) {
					throw new RuntimeException("very bad 0");
				}
				// System.out.println("box");
				if (x.get(i) == null) {
					int ret = fresh();
					x.put(i, ret);
					return ret;
				}
			}
		}

		lkeys = shift(lkeys);

		for (Edge e0 : lkeys) {
			if (e0.target.equals(n)) {
				Map<Integer, Integer> x = Ltables.get(e0);
				if (x == null) {
					throw new RuntimeException("No node " + n + " in "
							+ Ltables.keySet());
				}
				// System.out.println("foo");
				for (Integer i : x.keySet()) {
					if (i == null) {
						throw new RuntimeException("very bad");
					}
					// System.out.println("bar");
					if (x.get(i) == null) {
						int ret = fresh();
						x.put(i, ret);
						return ret;
					}
				}
			}
		}
		return null; // is ok
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
		fillInPartial();
		for (Eq e : rtables.keySet()) {
			// System.out.println(e);
			List<Integer[]> v = rtables.get(e);
			Node[] c = rtables1.get(e);
			Integer n = rtables2.get(e);
			// System.out.println(c.length);
			// System.out.println(n);
			Node a = c[n - 1];
			Node b = c[c.length - 1];
			if (!a.equals(b)) {
				throw new RuntimeException();
			}
			if (n - 1 == c.length - 1) {
				throw new RuntimeException();
			}
			for (Integer[] row : v) {
				if ( row[n - 1] != null 
						&& row[c.length - 1] != null && !row[n - 1].equals(row[c.length - 1]) ) {
					if (row[n - 1] < row[c.length - 1]) {
						SA.get(a)
								.add(new Pair<>(row[n - 1], row[c.length - 1]));
					} else {
						SA.get(a)
								.add(new Pair<>(row[c.length - 1], row[n - 1]));
					}
				}
			}
		}
		for (Edge e : ntables.keySet()) {
			// System.out.println(e);
			List<Integer[]> v = ntables.get(e);
			Node[] c = ntables1.get(e);
			int n = 3;
			// System.out.println(c.length);
			// System.out.println(n);
			Node a = c[n - 1];
			Node b = c[c.length - 1];
			if (!a.equals(b)) {
				throw new RuntimeException();
			}
			if (n - 1 == c.length - 1) {
				throw new RuntimeException();
			}
			for (Integer[] row : v) {
				if (row[n - 1] != null 
						&& row[c.length - 1] != null && !(row[n - 1].equals(row[c.length - 1]))) {
					if (row[n - 1] < row[c.length - 1]) {
		//				System.out.println("AAAA");
						SA.get(a)
								.add(new Pair<>(row[n - 1], row[c.length - 1]));
					} else {
				//		System.out.println("BBBB");
						SA.get(a)
								.add(new Pair<>(row[c.length - 1], row[n - 1]));
					}
				}
			}
		}
		// System.out.println("SA is " + SA);
	}

	// only do e tables from l tables
	private void fillInPartial() {
		// System.out.println("****");
		// System.out.println(L tables);
		for (Eq k : rtables.keySet()) {
			List<Integer[]> v = rtables.get(k);
			int n = rtables2.get(k);
			// Node[] c = rtables1.get(k);
			Edge[] e = rtables3.get(k);
			// System.out.println(c.length);
			// System.out.println(e.length);
			// System.out.println(n);
			for (Integer[] row : v) {
				// System.out.println(v);
				// System.out.println();
				Integer last = row[0];
				for (int i = 1; i < n; i++) {
					if (last != null) {
						last = Ltables.get(e[i - 1]).get(last);
						row[i] = last;
					}
				}
				last = row[n];
				// System.out.println(row.length);
				for (int i = n + 1; i < row.length; i++) {
					if (last != null) {
						// System.out.println("looking for " + e[i-2] + " and "
						// + last + " res " + Ltables.get(e[i-2]).get(last) +
						// " map " + Ltables.get(e[i-2]));
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
		//	if (!k.name.contains(" ")) {
//				System.out.println("Doing edge " + k);
//				System.out.println("Etable for " + A2 + " is " + etables.get(A2));
//				System.out.println("Etable for " + A1 + " is " + etables.get(A1));
//		//	}			
			for (Integer[] row : v) {
		//		System.out.println("lookup of " + row[1] + " in " + etables.get(A2) + " is " + etables.get(A2).get(row[1]));
				row[2] = etables.get(A2).get(row[1]);
			}
			for (Integer[] row : v) {
		//		System.out.println("lookup if " + row[3] + " is " + etables.get(A1).get(row[3]));
				row[4] = etables.get(A1).get(row[3]);
			}
			
			//int n = 4;
			// Node[] c = rtables1.get(k);
			Edge[] e = ntables3.get(k);
			// System.out.println(c.length);
			// System.out.println(e.length);
			// System.out.println(n);
			for (Integer[] row : v) {
				// System.out.println(v);
				// System.out.println();
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

	private void replace(Pair<Integer, Integer> uv) {
		// System.out.println("replacing " + uv.second + " with " + uv.first);
		for (Node n : etables.keySet()) {
			Map<Integer, Integer> m = etables.get(n);
			Map<Integer, Integer> mm = new HashMap<>();
			for (Entry<Integer, Integer> e : m.entrySet()) {
				Integer x = e.getKey().equals(uv.second) ? uv.first : e
						.getKey();
				Integer y = uv.second.equals(e.getValue()) ? uv.first : e
						.getValue();
				mm.put(x, y);
			}
			etables.put(n, mm);
		}
		// System.out.println("Ltables are " + Ltables);
		// System.out.println("Rtables are " + print(rtables));
		for (Edge n : Ltables.keySet()) {
			Map<Integer, Integer> m = Ltables.get(n);
			Map<Integer, Integer> mm = new HashMap<>();
			for (Entry<Integer, Integer> e : m.entrySet()) {
				if (e.getKey().equals(uv.second)) {
					continue;
				}
				if (uv.second.equals(e.getValue())) {
					mm.put(e.getKey(), uv.first);
					continue;
				}
				mm.put(e.getKey(), e.getValue());
				// ? uv.first : e.getKey();
				// Integer y = uv.second.equals(e.getValue()) ? uv.first :
				// e.getValue();
				// mm.put(x, y);
			}
			Ltables.put(n, mm);
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

	}



	private void delete(Node n, Map<Node, Set<Pair<Integer, Integer>>> m,
			Pair<Integer, Integer> uv) {

//		System.out.println("deleting node " + n + " pair " + uv);
		Pair<Integer, Integer> vw;
		while ((vw = getfrom(n, m, uv)) != null) {
			m.get(n).remove(vw);
			Pair<Integer, Integer> uw = new Pair<>(uv.first, vw.second);
			m.get(n).add(uw);
			// System.out.println("crazy remove 1");
		}
		for (Edge e : Ltables.keySet()) {
			if (Ltables1.get(e).first.equals(n)) {
				Integer gu = Ltables.get(e).get(uv.first);
				Integer gv = Ltables.get(e).get(uv.second);
				if (gu != null && gv != null && !gu.equals(gv)) {
					if (gu < gv) {
						// System.out.println("crazy remove 2 with " + gu + and
						// );
						m.get(n).add(new Pair<>(gu, gv));
					} else {
						// System.out.println("crazy remove 2");
						m.get(n).add(new Pair<>(gv, gu));
					}
				}
			}
		}

		m.get(n).remove(uv);
	}

	private Pair<Integer, Integer> getfrom(Node n,
			Map<Node, Set<Pair<Integer, Integer>>> m, Pair<Integer, Integer> uv) {
		//System.out.println("get from " + uv);
		for (Pair<Integer, Integer> p : m.get(n)) {
			//System.out.println("p is " + p);
			if (p.first.equals(uv.second)) {
				//System.out.println("hit");
				 if (p.second.equals(uv.first)) {
	//				 continue; doesn't work
					 // return p gives infinite loop
					throw new RuntimeException();
				} 
				return p;
			}
		}
		return null;
	}

	private Pair<Integer, Integer> take(Set<Pair<Integer, Integer>> set) {
		for (Pair<Integer, Integer> p : set) {
			return p;
		}
		throw new RuntimeException();
	}

	private Node findNonemptySa() {
		for (Node o : SA.keySet()) {
			Set<Pair<Integer, Integer>> v = SA.get(o);
			if (v.isEmpty()) {
				continue;
			}
			return o;
		}
		return null;
	}

	private boolean notComplete() {

		for (Node n : etables.keySet()) {
			Map<Integer, Integer> xxx = etables.get(n);
			for (Integer yyy : xxx.keySet()) {
				if (xxx.get(yyy) == null) {
					return true;
				}				
			}
		}

		for (Edge k : Ltables.keySet()) {
			Map<Integer, Integer> v = Ltables.get(k);
			if (v.size() == 0) {
				return true;
			}
			for (Integer i : v.keySet()) {
				if (v.get(i) == null) {
					return true;
				}
			}
		}
		return false;
	}

	// this will set the rhs of each etable to the lhs, and add to ltables
	public void initTables() throws FQLException {
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

//			SA.put(a, new HashSet<Pair<Integer, Integer>>());
		}
		for (Node b : B.nodes) {
			SA.put(b,  new HashSet<Pair<Integer, Integer>>());
		}
		ekeys = new LinkedList<>(A.nodes);
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
				if (x.first instanceof String) {
					r[0] = Integer.parseInt((String) x.first);
					r[1] = Integer.parseInt((String) x.second);
					r[3] = Integer.parseInt((String) x.first);
				} else {
					r[0] = (Integer) x.first;
					r[1] = (Integer) x.second;
					r[3] = (Integer) x.first;
				}
				l.add(r);
			}

			ntables.put(f, l);
			ntables3.put(f, ccc.toArray(new Edge[] {}));
		}
	}

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
		panel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		return panel;

	}

	Map<Node, DefaultTableModel> et = new HashMap<>();
	Map<Edge, DefaultTableModel> lt = new HashMap<>();
	Map<Eq, DefaultTableModel> rt = new HashMap<>();
	Map<Edge, DefaultTableModel> nt = new HashMap<>();

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
	}

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
	}

	public JTabbedPane view(int l) throws FQLException {
		enumerate(l);
		JTabbedPane t = new JTabbedPane();

		Triple<JPanel, JPanel, JPanel> xxx = toCat();
		t.addTab("Category", xxx.first);
		t.addTab("Signature", xxx.third);
		t.addTab("Normalizer", xxx.second);

		Map<String, JTable> m = new HashMap<>();
		for (Node n : etables0.keySet()) {
			m.put("e_" + n.string, new JTable(et.get(n)));
		}
		t.addTab("e-Tables", makePanels(m));

		m = new HashMap<>();
		for (Edge e : Ltables.keySet()) {
			m.put("L(" + e.name + ")", new JTable(lt.get(e)));
		}
		t.addTab("L-Tables", makePanels(m));

		m = new HashMap<>();
		for (Eq e : rtables.keySet()) {
			m.put(e.toString(), new JTable(rt.get(e)));
		}
		t.addTab("Relation Tables", makePanels(m));

		 m = new HashMap<>();
		 for (Edge e : ntables.keySet()) {
		 m.put("F" + e.name + " = " + F.em.get(e), new JTable(nt.get(e)));
		 }
//		 t.addTab("Naturality Tables", makePanels(m));

		return t;
	}

	private Triple<JPanel, JPanel, JPanel> toCat() {
		JPanel p = new JPanel(new GridLayout(1, 1));
		JTextArea a = new JTextArea();
		JPanel q = null;
		JPanel rr = new JPanel(new GridLayout(1, 1));

		try {

			Pair<FinCat<Node, Path>, Fn<Path, Arr<Node, Path>>> xxx = toCategory();

			a.setText(xxx.first.toString());
			a.setCaretPosition(0);

			q = makeNormalizer(xxx.second);
			
			JTextArea ra = new JTextArea(xxx.first.toSig(new HashMap<String, Type>()).first.toString());
			rr.add(new JScrollPane(ra));

		} catch (Throwable e) {
			e.printStackTrace();
			a.setText(e.getMessage());
		}
		p.add(new JScrollPane(a));

		return new Triple<>(p, q, rr);
	}

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
	}

	public JPanel view() throws FQLException {
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
					updateView(slider.getValue());
				} catch (FQLException ee) {
					throw new RuntimeException(ee);
				}
			}

		});

		return ret;
	}

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
