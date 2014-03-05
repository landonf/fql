package fql.cat;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import fql.DEBUG;
import fql.FQLException;
import fql.Pair;
import fql.decl.Edge;
import fql.decl.Eq;
import fql.decl.Instance;
import fql.decl.Mapping;
import fql.decl.Node;
import fql.decl.Path;
import fql.decl.Signature;
import fql.decl.Transform;

public class LeftKan {

	
	private Signature A, B;
	private Mapping F;
	private Instance X;
	//Map<Object, Integer> rank = new HashMap<>(); //assumes all IDs globally unique
	public int fresh; 	
	
	private boolean gamma() {
		boolean ret = false;
		
		for (;;) {
			Pair<Node, Pair<Integer, Integer>> k = gamma0();
			if (k == null) {
				return ret;
			}
			ret = true;
			gamma1(k.first, k.second);			
		}	
	}
	
	private void filter(Set<Pair<Integer, Integer>> set, Integer d) {
		Iterator<Pair<Integer, Integer>> it = set.iterator();
		while (it.hasNext()) {
			Pair<Integer, Integer> p = it.next();
			if (p.first.equals(d) || p.second.equals(d)) {
				it.remove();
			}
		}
	}
	
	private void gamma1(Node b1, Pair<Integer, Integer> xy) {
		//System.out.println("remove " + xy);
		//System.out.println("before " + this);
		if (xy.first.equals(xy.second)) {
			Sb.get(b1).remove(xy);
			return;
		}
		Integer x, y;
		if (xy.first > xy.second) {
			x = xy.second;
			y = xy.first;
		} else {
			x = xy.first;
			y = xy.second;
		}

		Pb.get(b1).remove(new Pair<>(y,y));
		
		replace(x, y);

		Set<Pair<Integer, Integer>> set0 = new HashSet<>(Sb.get(b1));
		for (Pair<Integer, Integer> k : Sb.get(b1)) {
			if (k.first.equals(y)) {
				set0.add(new Pair<>(x, k.second));
			}
			if (k.second.equals(y)) {
				set0.add(new Pair<>(k.first, x));
			}
		}
		filter(set0, y); 		
		Sb.put(b1, set0);
		
		for (Edge g : Pg.keySet()) {
			Set<Pair<Integer, Integer>> set = Pg.get(g);
			Set<Pair<Integer, Integer>> a = new HashSet<>();
			if (g.source.equals(b1) && g.target.equals(b1)) {
				for (Pair<Integer, Integer> k : set) {
					if (k.first.equals(y) && k.second.equals(y)) {
						a.add(new Pair<>(x,x));
					} 
					if (k.first.equals(y) && !k.second.equals(y)) {
						a.add(new Pair<>(x, k.second));
					}
					if (k.second.equals(y) && !k.first.equals(y)) {
						a.add(new Pair<>(k.first, x));
					}
				}
			} else if (g.source.equals(b1)) {
				for (Pair<Integer, Integer> k : set) {
					if (k.first.equals(y) && !k.second.equals(y)) {
						a.add(new Pair<>(x, k.second));
					}
				}
			} else if (g.target.equals(b1)) {
				for (Pair<Integer, Integer> k : set) {
					if (k.second.equals(y) && !k.first.equals(y)) {
						a.add(new Pair<>(k.first, x));
					}
				}
			}
			set.addAll(a);
			filter(set, y);
		}
		
		lineage.remove(y);
		for (Integer k : lineage.keySet()) {
			List<Pair<String, Integer>> v = lineage.get(k);
			for (Pair<String, Integer> p : v) {
				if (p.second.equals(y)) {
					// System.out.println("replace " + uv);
					p.second = x;
				}
			}
		}
//		System.out.println("after " + this);
	}

	private void replace(Integer x, Integer y) {
		for (Set<Pair<Object, Integer>> a : ua.values()) {
			for (Pair<Object, Integer> s : a) {
				if (s.second.equals(y)) {
					s.second = x;
				}
			}
		}
	}
	
	private Pair<Node, Pair<Integer, Integer>> gamma0() {
		for (Entry<Node, Set<Pair<Integer, Integer>>> c : Sb.entrySet()) {
			if (c.getValue().size() == 0) {
				continue;
			}
			for (Pair<Integer, Integer> p0 : c.getValue()) {
				return new Pair<>(c.getKey(), p0);
			}
		}
		
		return null;
	}
	
	private boolean bgd() {
		return beta1() || beta2() || delta() || gamma();
	}
	
	private boolean step() {
		boolean ret = false;
		while (bgd()) {
			ret = true;
		}
		return ret || alpha();
	}
	
	//true = success
	public boolean compute() {
		for (int i = 0; i < DEBUG.debug.MAX_DENOTE_ITERATIONS; i++) {
			//System.out.println(this);
			if (!step()) {
//				System.out.println("done");
				return true;
			}
		}
		return false;
	}
	
	//beta, delta, gamma
	
	private boolean beta2() {
		boolean ret = false;
		try {
			for (Edge e : A.edges) {
				Path g = F.appy(new Path(A, e));
				Set<Pair<Object, Integer>> lhs = Instance.compose(X.data.get(e.name), ua.get(e.target));
				Set<Pair<Object, Integer>> rhs = Instance.compose(ua.get(e.source), eval(g));
				Node n = g.target;
				ret = ret || addCoincidences(lhs, rhs, n);
			}
		} catch (FQLException fe) {
			fe.printStackTrace();
			throw new RuntimeException(fe.getMessage());
		}
		
		return ret;
	}
	
	private boolean beta1() {
		//System.out.println("start beta: " + this);
		boolean ret = false;
		for (Eq eq : B.eqs) {
			Set<Pair<Integer, Integer>> lhs = eval(eq.lhs);
			Set<Pair<Integer, Integer>> rhs = eval(eq.rhs);
			Node n = eq.lhs.target;
			ret = ret || addCoincidences(lhs, rhs, n);
		}		
//		System.out.println("end beta: " + this);
		return ret;
	}
	
	private <X> boolean addCoincidences(
			Set<Pair<X, Integer>> lhs, Set<Pair<X, Integer>> rhs,
			Node n) {
		boolean ret = false;
		for (Pair<?, Integer> l : lhs) {
			for (Pair<?, Integer> r : rhs) {
				if (!l.first.equals(r.first)) {
					continue;
				}
				if (l.second.equals(r.second)) {
					continue;
				}
//				System.out.println("coincidence: " + l.second + " and " + r.second);
				ret = Sb.get(n).add(new Pair<>(l.second, r.second)) || ret;
				ret = Sb.get(n).add(new Pair<>(r.second, l.second)) || ret;
			}
		}
		return ret;
	}
	
	
	public Set<Pair<Integer, Integer>> eval(Path p) {
		Set<Pair<Integer, Integer>> ret = Pb.get(p.source);
		for (Edge e : p.path) {
			ret = Instance.compose(ret, Pg.get(e));
		}
//		System.out.println("eval " + p + " result: " + ret);

		return ret;
	}
	
	private Integer fresh() {
		return ++fresh;
	}
	
	private Pair<Integer, Edge> smallest() {
		Pair<Integer, Edge> ret = null;
		for (Edge g : Pg.keySet()) {
			Set<Pair<Integer, Integer>> pg = Pg.get(g);
			outer: for (Pair<Integer, Integer> xx : Pb.get(g.source)) {
				Integer x = xx.first;
				for (Pair<Integer, Integer> p : pg) {
					if (p.first.equals(x)) {
						continue outer;
					}
				}
				if (ret == null || x < ret.first) {
					ret = new Pair<>(x, g);
				}
			}
		}
		return ret;
	}
	
	private boolean alpha() {
		Pair<Integer, Edge> p = smallest();
		if (p == null) {
			return false;
		}
		Integer x = p.first;
		
		Edge g = p.second;
		Node b2 = g.target;
		Integer y = fresh();
		
		Pb.get(b2).add(new Pair<>(y,y));
		Pg.get(g ).add(new Pair<>(x,y));
		
		updateLineage(g, x, y);
		
//		System.out.println("add " + y + " for " + p);
		
		return true;
	}
	
	private boolean delta() {
		boolean ret = false;
		for (Edge g : B.edges) {
			for (Pair<Integer, Integer> x : Pb.get(g.source)) {
				Integer y = null;
				Iterator<Pair<Integer, Integer>> it = Pg.get(g).iterator();
				while (it.hasNext()) {
					Pair<Integer, Integer> z = it.next();
					if (!x.first.equals(z.first)) {
						continue;
					}
					if (y == null) {
						y = z.second;
						continue;
					}
//					if (z.second.equals(y)) {
						ret = true;
						it.remove();
//						System.out.println("non-determinism: " + x.first + " mapsto " + y + " and " + z.second);
						Sb.get(g.target).add(new Pair<>(y, z.second));
						Sb.get(g.target).add(new Pair<>(z.second, y));
	//				}
				}
			}
		}
		return ret;
	}
	
	
	public Map<Integer, List<Pair<String, Integer>>> lineage = new HashMap<>();
	
	void updateLineage(Edge col, Integer old, Integer nw) {
		if (!lineage.containsKey(old)) {
			lineage.put(old, new LinkedList<Pair<String, Integer>>());
		}
		List<Pair<String, Integer>> l = new LinkedList<>(lineage.get(old));
		l.add(new Pair<>(col.name, old));
		lineage.put(nw, l);
	}

	private Instance J;
	private Transform alpha;
	
	public LeftKan(int fresh, Mapping f, Instance x) {
		this(fresh, f, x, null, null);
	}
	public LeftKan(int fresh, Mapping f, Instance x, Transform alpha,
			Instance J) {
		A = f.source;
		B = f.target;
		F = f;
		X = x;
		this.fresh = fresh;
		this.J = J;
		this.alpha = alpha;
		
		for (Node n : B.nodes) {
			Pb.put(n, new HashSet<Pair<Integer, Integer>>());
			Sb.put(n, new HashSet<Pair<Integer, Integer>>());
			if (alpha != null) {
				utables.put(n, new HashSet<Pair<Integer, Object>>());
			}
		}
		for (Edge e : B.edges) {
			Pg.put(e, new HashSet<Pair<Integer, Integer>>());
		}
		Set<Object> rank = new HashSet<>();

		for (Node n : A.nodes) {
			Set<Pair<Object, Integer>> j = new HashSet<>();
			Set<Pair<Integer, Integer>> i = Pb.get(F.nm.get(n));
			Set<Pair<Object, Object>> k = X.data.get(n.string);
			for (Pair<Object, Object> v : k) {
				if (rank.contains(v.first)) {
					throw new RuntimeException("Contains non-unique ID " + v.first + ": " + x);
				}
				int id = fresh();
				rank.add(v.first);
				j.add(new Pair<>(v.first, id));
				i.add(new Pair<>(id, id));
			}
			ua.put(n, j);
		}
//		System.out.println("initial: " + this);
	}
	
	@Override
	public String toString() {
		return "LeftKan [lineage=" + lineage + ", Pb=" + Pb + ", Pg=" + Pg
				+ ", ua=" + ua + ", Sb=" + Sb + "]";
	}

	public Map<Node, Set<Pair<Integer, Integer>>> Pb = new HashMap<>(); 
	public Map<Edge, Set<Pair<Integer, Integer>>> Pg = new HashMap<>();
	public Map<Node, Set<Pair<Object , Integer>>> ua = new HashMap<>();
	public Map<Node, Set<Pair<Integer, Object>>> utables = new HashMap<>();

	private Map<Node, Set<Pair<Integer, Integer>>> Sb = new HashMap<>();
}
