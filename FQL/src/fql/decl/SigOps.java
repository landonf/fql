package fql.decl;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import fql.Fn;
import fql.Pair;
import fql.Quad;
import fql.Triple;
import fql.decl.MapExp.Apply;
import fql.decl.MapExp.Case;
import fql.decl.MapExp.Comp;
import fql.decl.MapExp.Const;
import fql.decl.MapExp.Curry;
import fql.decl.MapExp.Dist1;
import fql.decl.MapExp.Dist2;
import fql.decl.MapExp.FF;
import fql.decl.MapExp.Fst;
import fql.decl.MapExp.Id;
import fql.decl.MapExp.Inl;
import fql.decl.MapExp.Inr;
import fql.decl.MapExp.MapExpVisitor;
import fql.decl.MapExp.Prod;
import fql.decl.MapExp.Snd;
import fql.decl.MapExp.TT;
import fql.decl.SigExp.Exp;
import fql.decl.SigExp.One;
import fql.decl.SigExp.Plus;
import fql.decl.SigExp.SigExpVisitor;
import fql.decl.SigExp.Times;
import fql.decl.SigExp.Var;
import fql.decl.SigExp.Zero;

public class SigOps implements SigExpVisitor<SigExp.Const, Map<String, SigExp>>,
 MapExpVisitor<Const, Pair<Map<String, SigExp>, Map<String, MapExp>>> {

	

	public Pair<SigExp.Const, Fn<SigExp.Const, MapExp.Const>> zero() {
		final SigExp.Const sig = new SigExp.Const(new LinkedList<String>(),
				new LinkedList<Triple<String, String, String>>(),
				new LinkedList<Triple<String, String, String>>(),
				new LinkedList<Pair<List<String>, List<String>>>());

		Fn<SigExp.Const, MapExp.Const> fn = new Fn<SigExp.Const, MapExp.Const>() {
			@Override
			public MapExp.Const of(SigExp.Const x) {
				return new MapExp.Const(new LinkedList<Pair<String, String>>(),
						new LinkedList<Pair<String, String>>(),
						new LinkedList<Pair<String, List<String>>>(), sig, x);
			}
		};

		return new Pair<>(sig, fn);
	}

	public Quad<SigExp.Const, Const, Const, Fn<Triple<SigExp.Const, Const, Const>, Const>> plus(
			final SigExp.Const a, final SigExp.Const b) {
		int node_count = 0;
		final Map<String, String> node_map_1 = new LinkedHashMap<>();
		final Map<String, String> node_map_2 = new LinkedHashMap<>();
		List<Pair<String, String>> a_objs = new LinkedList<>();
		List<Pair<String, String>> b_objs = new LinkedList<>();
		for (String n : a.nodes) {
			node_map_1.put(n, "node" + node_count);
			a_objs.add(new Pair<>(n, "node" + node_count));
			node_count++;
		}
		for (String n : b.nodes) {
			node_map_2.put(n, "node" + node_count);
			b_objs.add(new Pair<>(n, "node" + node_count));
			node_count++;
		}
		List<String> nodes = new LinkedList<>();
		nodes.addAll(node_map_1.values());
		nodes.addAll(node_map_2.values());

		int attr_count = 0;
		final Map<String, Triple<String, String, String>> attr_map_1 = new LinkedHashMap<>();
		final Map<String, Triple<String, String, String>> attr_map_2 = new LinkedHashMap<>();
		List<Pair<String, String>> a_attrs = new LinkedList<>();
		List<Pair<String, String>> b_attrs = new LinkedList<>();
		for (Triple<String, String, String> n : a.attrs) {
			attr_map_1.put(n.first, new Triple<>("attr" + attr_count,
					node_map_1.get(n.second), n.third));
			a_attrs.add(new Pair<>(n.first, "attr" + attr_count));
			attr_count++;
		}
		for (Triple<String, String, String> n : b.attrs) {
			attr_map_2.put(n.first, new Triple<>("attr" + attr_count,
					node_map_2.get(n.second), n.third));
			b_attrs.add(new Pair<>(n.first, "attr" + attr_count));
			attr_count++;
		}
		List<Triple<String, String, String>> attrs = new LinkedList<>();
		attrs.addAll(attr_map_1.values());
		attrs.addAll(attr_map_2.values());

		int edge_count = 0;
		final Map<String, Triple<String, String, String>> edge_map_1 = new LinkedHashMap<>();
		final Map<String, Triple<String, String, String>> edge_map_2 = new LinkedHashMap<>();
		List<Pair<String, List<String>>> a_arrows = new LinkedList<>();
		List<Pair<String, List<String>>> b_arrows = new LinkedList<>();
		for (Triple<String, String, String> n : a.arrows) {
			edge_map_1.put(n.first, new Triple<>("edge" + edge_count,
					node_map_1.get(n.second), node_map_1.get(n.third)));
			List<String> x = new LinkedList<>();
			x.add(node_map_1.get(n.second));
			x.add("edge" + edge_count);
			a_arrows.add(new Pair<>(n.first, x));
			edge_count++;
		}
		for (Triple<String, String, String> n : b.arrows) {
			edge_map_2.put(n.first, new Triple<>("edge" + edge_count,
					node_map_2.get(n.second), node_map_2.get(n.third)));
			List<String> x = new LinkedList<>();
			x.add(node_map_2.get(n.second));
			x.add("edge" + edge_count);
			b_arrows.add(new Pair<>(n.first, x));
			edge_count++;
		}
		List<Triple<String, String, String>> arrows = new LinkedList<>();
		arrows.addAll(edge_map_1.values());
		arrows.addAll(edge_map_2.values());

		List<Pair<List<String>, List<String>>> eqs = new LinkedList<>();
		for (Pair<List<String>, List<String>> eq : a.eqs) {
			List<String> lhs = new LinkedList<>();
			lhs.add(node_map_1.get(eq.first.get(0)));
			for (int i = 1; i < eq.first.size(); i++) {
				lhs.add(edge_map_1.get(eq.first.get(i)).first);
			}
			List<String> rhs = new LinkedList<>();
			rhs.add(node_map_1.get(eq.second.get(0)));
			for (int i = 1; i < eq.second.size(); i++) {
				rhs.add(edge_map_1.get(eq.second.get(i)).first);
			}
			eqs.add(new Pair<>(lhs, rhs));
		}
		for (Pair<List<String>, List<String>> eq : b.eqs) {
			List<String> lhs = new LinkedList<>();
			lhs.add(node_map_2.get(eq.first.get(0)));
			for (int i = 1; i < eq.first.size(); i++) {
				lhs.add(edge_map_2.get(eq.first.get(i)).first);
			}
			List<String> rhs = new LinkedList<>();
			rhs.add(node_map_2.get(eq.second.get(0)));
			for (int i = 1; i < eq.second.size(); i++) {
				rhs.add(edge_map_2.get(eq.second.get(i)).first);
			}
			eqs.add(new Pair<>(lhs, rhs));
		}

		final SigExp.Const sig = new SigExp.Const(nodes, attrs, arrows, eqs);
		Const inj1 = new MapExp.Const(a_objs, a_attrs, a_arrows, a, sig);
		Const inj2 = new MapExp.Const(b_objs, b_attrs, b_arrows, b, sig);
		
		//System.out.println("result");
		//System.out.println(sig);

		Fn<Triple<SigExp.Const, MapExp.Const, MapExp.Const>, MapExp.Const> match = new Fn<Triple<SigExp.Const, MapExp.Const, MapExp.Const>, MapExp.Const>() {
			@Override
			public Const of(Triple<fql.decl.SigExp.Const, Const, Const> x) {
				SigExp.Const c = x.first;
				Const f = x.second;
				Const g = x.third;

				if (!f.dst.equals(g.dst) || !f.src.equals(a)
						|| !g.src.equals(b)) {
					throw new RuntimeException();
				}

				List<Pair<String, String>> objs = new LinkedList<>();
				for (String obj_a : a.nodes) {
					objs.add(new Pair<>(node_map_1.get(obj_a), lookup(obj_a, f.objs)));
				}
				for (String obj_b : b.nodes) {
					objs.add(new Pair<>(node_map_2.get(obj_b), lookup(obj_b, g.objs)));
				}
				
				List<Pair<String, String>> attrs = new LinkedList<>();
				for (Triple<String, String, String> attr_a : a.attrs) {
					attrs.add(new Pair<>(attr_map_1.get(attr_a.first).first, lookup(attr_a.first, f.attrs)));
				}
				for (Triple<String, String, String> attr_b : b.attrs) {
					attrs.add(new Pair<>(attr_map_2.get(attr_b.first).first, lookup(attr_b.first, g.attrs)));
				}
				
				List<Pair<String, List<String>>> arrows = new LinkedList<>();
				for (Triple<String, String, String> edge_a : a.arrows) {
					arrows.add(new Pair<>(edge_map_1.get(edge_a.first).first, lookup(edge_a.first, f.arrows)));
				}
				for (Triple<String, String, String> edge_b : b.arrows) {
					arrows.add(new Pair<>(edge_map_2.get(edge_b.first).first, lookup(edge_b.first, g.arrows)));
				}
				
				return new Const(objs, attrs, arrows, sig, c);
			}

		};

		return new Quad<>(sig, inj1, inj2, match);
	}
	
	private static <A,B> B lookup(A a, List<Pair<A, B>> l) {
		for (Pair<A, B> k : l) {
			if (k.first.equals(a)) {
				return k.second;
			}
		}
		throw new RuntimeException();
	}

	///////////////////////////////////////
	
	@Override
	public fql.decl.SigExp.Const visit(Map<String, SigExp> env, Zero e) {
		return zero().first;
	}

	@Override
	public fql.decl.SigExp.Const visit(Map<String, SigExp> env, One e) {
		throw new RuntimeException();
	}

	@Override
	public fql.decl.SigExp.Const visit(Map<String, SigExp> env, Plus e) {
		return plus(e.a.accept(env, this), e.b.accept(env, this)).first;
	}

	@Override
	public fql.decl.SigExp.Const visit(Map<String, SigExp> env, Times e) {
		throw new RuntimeException();
	}

	@Override
	public fql.decl.SigExp.Const visit(Map<String, SigExp> env, Exp e) {
		throw new RuntimeException();
	}

	@Override
	public fql.decl.SigExp.Const visit(Map<String, SigExp> env, Var e) {
		return env.get(e.v).accept(env, this);
	}

	@Override
	public fql.decl.SigExp.Const visit(Map<String, SigExp> env,
			fql.decl.SigExp.Const e) {
		return e;
	}
	
	///////////////////////////////

	@Override
	public Const visit(Pair<Map<String, SigExp>, Map<String, MapExp>> env, Id e) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Const visit(Pair<Map<String, SigExp>, Map<String, MapExp>> env,
			Comp e) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Const visit(Pair<Map<String, SigExp>, Map<String, MapExp>> env,
			Dist1 e) {
		throw new RuntimeException();
	}

	@Override
	public Const visit(Pair<Map<String, SigExp>, Map<String, MapExp>> env,
			Dist2 e) {
		throw new RuntimeException();
	}

	@Override
	public Const visit(Pair<Map<String, SigExp>, Map<String, MapExp>> env,
			fql.decl.MapExp.Var e) {
		return env.second.get(e.v).accept(env, this);
	}

	@Override
	public Const visit(Pair<Map<String, SigExp>, Map<String, MapExp>> env,
			Const e) {
		return e;
	}

	@Override
	public Const visit(Pair<Map<String, SigExp>, Map<String, MapExp>> env, TT e) {
		throw new RuntimeException();
	}

	@Override
	public Const visit(Pair<Map<String, SigExp>, Map<String, MapExp>> env, FF e) {
		return zero().second.of(e.t.accept(env.first, this));
	}

	@Override
	public Const visit(Pair<Map<String, SigExp>, Map<String, MapExp>> env, Fst e) {
		throw new RuntimeException();
	}

	@Override
	public Const visit(Pair<Map<String, SigExp>, Map<String, MapExp>> env, Snd e) {
		throw new RuntimeException();
	}

	@Override
	public Const visit(Pair<Map<String, SigExp>, Map<String, MapExp>> env, Inl e) {
		return plus(e.s.accept(env.first, this), e.t.accept(env.first, this)).second;
	}

	@Override
	public Const visit(Pair<Map<String, SigExp>, Map<String, MapExp>> env, Inr e) {
		return plus(e.s.accept(env.first, this), e.t.accept(env.first, this)).third;
	}

	@Override
	public Const visit(Pair<Map<String, SigExp>, Map<String, MapExp>> env,
			Apply e) {
		throw new RuntimeException();
	}

	@Override
	public Const visit(Pair<Map<String, SigExp>, Map<String, MapExp>> env,
			Curry e) {
		throw new RuntimeException();
	}

	@Override
	public Const visit(Pair<Map<String, SigExp>, Map<String, MapExp>> env,
			Case e) {
		Const lx = e.l.accept(env, this);
		Const rx = e.r.accept(env, this);
		SigExp.Const cx = lx.dst.accept(env.first, this);
		
		return plus(lx.src.accept(env.first, this), rx.src.accept(env.first, this)).fourth.of(new Triple<>(cx, lx, rx));

	}

	@Override
	public Const visit(Pair<Map<String, SigExp>, Map<String, MapExp>> env,
			Prod e) {
		throw new RuntimeException();
	}

}
