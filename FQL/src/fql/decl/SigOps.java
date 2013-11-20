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

public class SigOps implements
		SigExpVisitor<SigExp.Const, FQLProgram>,
		MapExpVisitor<Const, FQLProgram> {

	public Pair<SigExp.Const, Fn<SigExp.Const, MapExp.Const>> one() {
		List<String> nodes = new LinkedList<>();
		nodes.add("node0");
		
		List<Triple<String, String, String>> attrs = new LinkedList<>();
		attrs.add(new Triple<>("int_attr", "node0", "int"));
		attrs.add(new Triple<>("string_attr", "node0", "string"));
		
		List<Triple<String, String, String>> arrows = new LinkedList<>();
		
		final SigExp.Const sig = new SigExp.Const(nodes, attrs, arrows, 
				new LinkedList<Pair<List<String>, List<String>>>());

		Fn<SigExp.Const, MapExp.Const> fn = new Fn<SigExp.Const, MapExp.Const>() {
			@Override
			public MapExp.Const of(SigExp.Const src) {
				List<Pair<String, String>> nm = new LinkedList<>();
				for (String k : src.nodes) {
					nm.add(new Pair<>(k, "node0"));
				}
				
				List<Pair<String, String>> am = new LinkedList<>();
				for (Triple<String, String, String> k : src.attrs) {
					if (k.third.equals("string")) {
						am.add(new Pair<>(k.first, "string_attr"));
					} else if (k.third.equals("int")) {
						am.add(new Pair<>(k.first, "int_attr"));
					} else {
						throw new RuntimeException();
					}
				}
				
				List<Pair<String, List<String>>> em = new LinkedList<>();
				for (Triple<String, String, String> k : src.arrows) {
					List<String> l = new LinkedList<>();
					l.add("node0");
					em.add(new Pair<>(k.first, l));
				}
				return new MapExp.Const(nm, am, em, src, sig);
			}
		};

		return new Pair<>(sig, fn);
	}
	
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

	public Quad<SigExp.Const, Const, Const, Fn<Triple<SigExp.Const, Const, Const>, Const>> prod(
			final SigExp.Const a, final SigExp.Const b) {
		int node_count = 0;
		final Map<Pair<String, String>, String> node_map = new LinkedHashMap<>();
		final List<Pair<String, String>> a_objs = new LinkedList<>(); //fst
		final List<Pair<String, String>> b_objs = new LinkedList<>(); //snd
		for (String n : a.nodes) {
			for (String m : b.nodes) {
				node_map.put(new Pair<>(n, m), "node" + node_count);
				a_objs.add(new Pair<>("node" + node_count, n));
				b_objs.add(new Pair<>("node" + node_count, m));
				node_count++;
			}
		}
		List<String> nodes = new LinkedList<>();
		nodes.addAll(node_map.values());

		int attr_count = 0;
		final Map<Pair<String, String>, String> attr_map = new LinkedHashMap<>();
		List<Pair<String, String>> a_attrs = new LinkedList<>(); //fst
		List<Pair<String, String>> b_attrs = new LinkedList<>(); //snd
		List<Triple<String, String, String>> attrs = new LinkedList<>();
		for (Triple<String, String, String> n : a.attrs) {
			for (Triple<String, String, String> m : b.attrs) {
				if (!n.third.equals(m.third)) {
					continue;
				}
				String k = node_map.get(new Pair<>(n.second, m.second));
				attrs.add(new Triple<>("attr" + attr_count, k, n.third));
				attr_map.put(new Pair<>(n.first, m.first), "attr" + attr_count);
				a_attrs.add(new Pair<>("attr" + attr_count, n.first));
				b_attrs.add(new Pair<>("attr" + attr_count, m.first));
				attr_count++;
			}
		}

		int edge_count = 0;
		final Map<Pair<String, String>, String> edge_map_1 = new LinkedHashMap<>();
		final Map<Pair<String, String>, String> edge_map_2 = new LinkedHashMap<>();		
		List<Pair<String, List<String>>> a_edges = new LinkedList<>(); //fst
		List<Pair<String, List<String>>> b_edges = new LinkedList<>(); //snd
		List<Triple<String, String, String>> edges = new LinkedList<>();
		for (Triple<String, String, String> n : a.arrows) {
			for (String m : b.nodes) {
				String k1 = node_map.get(new Pair<>(n.second, m));
				String k2 = node_map.get(new Pair<>(n.third, m));
				
				edges.add(new Triple<>("edge" + edge_count, k1, k2));
				edge_map_1.put(new Pair<>(n.first, m), "edge" + edge_count);
				
				List<String> al = new LinkedList<>();
				al.add(n.second);
				al.add(n.first);								
				a_edges.add(new Pair<>("edge" + edge_count, al));
				
				List<String> bl = new LinkedList<>();
				bl.add(m);
				b_edges.add(new Pair<>("edge" + edge_count, bl));
				
				edge_count++;
			}
		}
		for (Triple<String, String, String> n : b.arrows) {
			for (String m : a.nodes) {
				String k1 = node_map.get(new Pair<>(m, n.second));
				String k2 = node_map.get(new Pair<>(m, n.third));
				
				edges.add(new Triple<>("edge" + edge_count, k1, k2));
				edge_map_2.put(new Pair<>(n.first, m), "edge" + edge_count);
				
				List<String> al = new LinkedList<>();
				al.add(n.second);
				al.add(n.first);								
				b_edges.add(new Pair<>("edge" + edge_count, al));
				
				List<String> bl = new LinkedList<>();
				bl.add(m);
				a_edges.add(new Pair<>("edge" + edge_count, bl));
				
				edge_count++;
			}
		}
	
		List<Pair<List<String>, List<String>>> eqs = new LinkedList<>();
		for (Triple<String, String, String> n : a.arrows) {
			for (Triple<String, String, String> m : b.arrows) {
				List<String> lhs = new LinkedList<>();
				List<String> rhs = new LinkedList<>();
				
				String src = node_map.get(new Pair<>(n.second, m.second));
				
				String lhs1 = edge_map_1.get(new Pair<>(n.first, m.second));
				String lhs2 = edge_map_2.get(new Pair<>(m.first, n.third));
				lhs.add(src); lhs.add(lhs1); lhs.add(lhs2);
				
				String rhs1 = edge_map_2.get(new Pair<>(m.first, n.second));
				String rhs2 = edge_map_1.get(new Pair<>(n.first, m.third));
				rhs.add(src); rhs.add(rhs1); rhs.add(rhs2);
				
				eqs.add(new Pair<>(lhs, rhs));
			}
		}
		for (Pair<List<String>, List<String>> eqA : a.eqs) {
			for (String srcB : b.nodes) {
				List<String> lhsA = new LinkedList<>(eqA.first);
				List<String> rhsA = new LinkedList<>(eqA.second);
				String srcA = lhsA.remove(0); rhsA.remove(0);
				
				String src = node_map.get(new Pair<>(srcA, srcB));
				
				List<String> lhs = new LinkedList<>();
				List<String> rhs = new LinkedList<>();
				lhs.add(src); rhs.add(src);
				
				for (String k : lhsA) {
					lhs.add(edge_map_1.get(new Pair<>(k, srcB)));
				}
				for (String k : rhsA) {
					rhs.add(edge_map_1.get(new Pair<>(k, srcB)));
				}
				eqs.add(new Pair<>(lhs, rhs));
			}
		}
		for (Pair<List<String>, List<String>> eqA : b.eqs) {
			for (String srcB : a.nodes) {
				List<String> lhsA = new LinkedList<>(eqA.first);
				List<String> rhsA = new LinkedList<>(eqA.second);
				String srcA = lhsA.remove(0); rhsA.remove(0);
				
				String src = node_map.get(new Pair<>(srcB, srcA));
				
				List<String> lhs = new LinkedList<>();
				List<String> rhs = new LinkedList<>();
				lhs.add(src); rhs.add(src);
				
				for (String k : lhsA) {
					lhs.add(edge_map_2.get(new Pair<>(k, srcB)));
				}
				for (String k : rhsA) {
					rhs.add(edge_map_2.get(new Pair<>(k, srcB)));
				}
				eqs.add(new Pair<>(lhs, rhs));
			}
		}

		final SigExp.Const sig = new SigExp.Const(nodes, attrs, edges, eqs);
		Const fst = new MapExp.Const(a_objs, a_attrs, a_edges, sig, a);
		Const snd = new MapExp.Const(b_objs, b_attrs, b_edges, sig, b);
		
//		System.out.println(sig);
//		System.out.println("fst" + fst);
//		System.out.println("snd" + snd);

		Fn<Triple<SigExp.Const, MapExp.Const, MapExp.Const>, MapExp.Const> pair = new Fn<Triple<SigExp.Const, MapExp.Const, MapExp.Const>, MapExp.Const>() {
			@Override
			public Const of(Triple<fql.decl.SigExp.Const, Const, Const> x) {
				SigExp.Const c = x.first;
				Const f = x.second;
				Const g = x.third;

				if (!f.src.equals(g.src)) {
					throw new RuntimeException("Sources don't agree: " + f.src + " and " + g.src);
				}
				if (!f.dst.equals(a)) {
					throw new RuntimeException("Target of " + f + " is not " + a);
				}
				if (!g.dst.equals(b)) {
					throw new RuntimeException("Target of " + g + "is not " + b);
				}

				List<Pair<String, String>> objs = new LinkedList<>();
				for (String obj_c : c.nodes) {
					objs.add(new Pair<>(obj_c, node_map.get(new Pair<>(lookup(obj_c, f.objs), lookup(obj_c, g.objs)))));
				}

				List<Pair<String, String>> attrs = new LinkedList<>();
				for (Triple<String, String, String> attr_c : c.attrs) {
					attrs.add(new Pair<>(attr_c.first, attr_map.get(new Pair<>(lookup(attr_c.first, f.attrs), lookup(attr_c.first, g.attrs)))));
				}

				List<Pair<String, List<String>>> arrows = new LinkedList<>();
				for (Triple<String, String, String> edge_c : c.arrows) {
					List<String> fc = lookup(edge_c.first, f.arrows);
					List<String> gc = lookup(edge_c.first, g.arrows);
					List<String> ret = new LinkedList<>();
					String fcN = fc.get(0);
					String gcN = gc.get(0);
					String node_start = node_map.get(new Pair<>(fcN, gcN));
					ret.add(node_start);
					//System.out.println("edge map 1 is " + edge_map_1);
					//System.out.println("edge map 2 is " + edge_map_2);
					for (int i = 1; i < fc.size(); i++) {
						String fcE = fc.get(i);
						Pair<String, String> p = new Pair<>(fcE, gcN);
						//System.out.println("trying " + p);
						String v = edge_map_1.get(p);
						//System.out.println("result " + v);
						ret.add(v);
					}
					node_start = lookup(edge_c.third, f.objs);

					//System.out.println("last is " + node_start);
//					String last = node_map.get(lookup3(fc.get(fc.size() - 1), a.arrows).third);
					for (int i = 1; i < gc.size(); i++) {
						String gcE = gc.get(i);
						Pair<String, String> p = new Pair<>(gcE, node_start);
						//System.out.println("xtrying " + p);
						String v = edge_map_2.get(p);
						//System.out.println("xresult " + v);
						ret.add(v);
					}
					arrows.add(new Pair<>(edge_c.first, ret));
				}
				Const ret = new Const(objs, attrs, arrows, c, sig);
				//System.out.println("retconst " + ret);
				return ret; 
			} 
		};
			
		return new Quad<>(sig, fst, snd, pair);
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

		Fn<Triple<SigExp.Const, MapExp.Const, MapExp.Const>, MapExp.Const> match = new Fn<Triple<SigExp.Const, MapExp.Const, MapExp.Const>, MapExp.Const>() {
			@Override
			public Const of(Triple<fql.decl.SigExp.Const, Const, Const> x) {
				SigExp.Const c = x.first;
				Const f = x.second;
				Const g = x.third;

				if (!f.dst.equals(g.dst)) {
					throw new RuntimeException("Targets don't agree: " + f.dst + " and " + g.dst);
				}
				if (!f.src.equals(a)) {
					throw new RuntimeException("Source of " + f + " is not " + a);
				}
				if (!g.src.equals(b)) {
					throw new RuntimeException("Source of " + g + "is not " + b);
				}

				List<Pair<String, String>> objs = new LinkedList<>();
				for (String obj_a : a.nodes) {
					objs.add(new Pair<>(node_map_1.get(obj_a), lookup(obj_a,
							f.objs)));
				}
				for (String obj_b : b.nodes) {
					objs.add(new Pair<>(node_map_2.get(obj_b), lookup(obj_b,
							g.objs)));
				}

				List<Pair<String, String>> attrs = new LinkedList<>();
				for (Triple<String, String, String> attr_a : a.attrs) {
					attrs.add(new Pair<>(attr_map_1.get(attr_a.first).first,
							lookup(attr_a.first, f.attrs)));
				}
				for (Triple<String, String, String> attr_b : b.attrs) {
					attrs.add(new Pair<>(attr_map_2.get(attr_b.first).first,
							lookup(attr_b.first, g.attrs)));
				}

				List<Pair<String, List<String>>> arrows = new LinkedList<>();
				for (Triple<String, String, String> edge_a : a.arrows) {
					arrows.add(new Pair<>(edge_map_1.get(edge_a.first).first,
							lookup(edge_a.first, f.arrows)));
				}
				for (Triple<String, String, String> edge_b : b.arrows) {
					arrows.add(new Pair<>(edge_map_2.get(edge_b.first).first,
							lookup(edge_b.first, g.arrows)));
				}

				return new Const(objs, attrs, arrows, sig, c);
			}

		};

		return new Quad<>(sig, inj1, inj2, match);
	}

	private static <A, B> B lookup(A a, List<Pair<A, B>> l) {
		for (Pair<A, B> k : l) {
			if (k.first.equals(a)) {
				return k.second;
			}
		}
		throw new RuntimeException();
	}
/*
	private static <A, B, C> Triple<A,B,C> lookup3(A a, List<Triple<A, B, C>> l) {
		for (Triple<A, B, C> k : l) {
			if (k.first.equals(a)) {
				return k;
			}
		}
		throw new RuntimeException("Cannot find " + a + " in " + l);
	}
	*/

	// /////////////////////////////////////

	@Override
	public fql.decl.SigExp.Const visit(FQLProgram env, Zero e) {
		return zero().first;
	}

	@Override
	public fql.decl.SigExp.Const visit(FQLProgram env, One e) {
		return one().first;
	}

	@Override
	public fql.decl.SigExp.Const visit(FQLProgram env, Plus e) {
		return plus(e.a.accept(env, this), e.b.accept(env, this)).first;
	}

	@Override
	public fql.decl.SigExp.Const visit(FQLProgram env, Times e) {
		return prod(e.a.accept(env, this), e.b.accept(env, this)).first;
	}

	@Override
	public fql.decl.SigExp.Const visit(FQLProgram env, Exp e) {
		throw new RuntimeException();
	}

	@Override
	public fql.decl.SigExp.Const visit(FQLProgram env, Var e) {
		return env.sigs.get(e.v).accept(env, this);
	}

	@Override
	public fql.decl.SigExp.Const visit(FQLProgram env,
			fql.decl.SigExp.Const e) {
		return e;
	}

	// /////////////////////////////

	@Override
	public Const visit(FQLProgram env, Id e) {
		SigExp.Const s = e.t.toConst(env);

		List<Pair<String, String>> objs = new LinkedList<>();
		for (String x : s.nodes) {
			objs.add(new Pair<>(x, x));
		}

		List<Pair<String, String>> attrs = new LinkedList<>();
		for (Triple<String, String, String> x : s.attrs) {
			attrs.add(new Pair<>(x.first, x.first));
		}

		List<Pair<String, List<String>>> arrows = new LinkedList<>();
		for (Triple<String, String, String> x : s.arrows) {
			List<String> l = new LinkedList<>();
			l.add(x.second);
			l.add(x.first);
			arrows.add(new Pair<>(x.first, l));
		}

		return new Const(objs, attrs, arrows, s, s);
	}

	@Override
	public Const visit(FQLProgram env,
			Comp e) {
		Const a = e.l.toConst(env);
		Const b = e.r.toConst(env);

		if (!a.dst.equals(b.src)) {
			throw new RuntimeException();
		}

		List<Pair<String, String>> objs = new LinkedList<>();
		for (Pair<String, String> x : a.objs) {
			objs.add(new Pair<>(x.first, lookup(x.second, b.objs)));
		}

		List<Pair<String, String>> attrs = new LinkedList<>();
		for (Pair<String, String> x : a.attrs) {
			attrs.add(new Pair<>(x.first, lookup(x.second, b.attrs)));
		}

		List<Pair<String, List<String>>> arrows = new LinkedList<>();
		for (Pair<String, List<String>> x : a.arrows) {
			String n = lookup(x.second.get(0), b.objs);
			
			List<String> l = new LinkedList<>();
			l.add(n);
			for (int i = 1; i < x.second.size(); i++) {
				List<String> p = lookup(x.second.get(i), b.arrows);
				l.addAll(p.subList(1, x.second.size()));
			}
			arrows.add(new Pair<>(x.first, l));
		}

		return new Const(objs, attrs, arrows, a.src, b.dst);
	}

	@Override
	public Const visit(FQLProgram env,
			Dist1 e) {
		throw new RuntimeException();
	}

	@Override
	public Const visit(FQLProgram env,
			Dist2 e) {
		throw new RuntimeException();
	}
	
	//TODO add foreign key and primary key to generated SQL

	@Override
	public Const visit(FQLProgram env,
			fql.decl.MapExp.Var e) {
		return env.maps.get(e.v).accept(env, this);
	}

	@Override
	public Const visit(FQLProgram env,
			Const e) {
		Pair<SigExp, SigExp> k = e.type(env);  //resolve vars
		return new Const(e.objs, e.attrs, e.arrows, k.first, k.second);
	}

	@Override
	public Const visit(FQLProgram env, TT e) {
		return one().second.of(e.t.accept(env, this));
	}

	@Override
	public Const visit(FQLProgram env, FF e) {
		return zero().second.of(e.t.accept(env, this));
	}

	@Override
	public Const visit(FQLProgram env, Fst e) {
		return prod(e.s.accept(env, this), e.t.accept(env, this)).second;
	}

	@Override
	public Const visit(FQLProgram env, Snd e) {
		return prod(e.s.accept(env, this), e.t.accept(env, this)).third;
	}

	@Override
	public Const visit(FQLProgram env, Inl e) {
		return plus(e.s.accept(env, this), e.t.accept(env, this)).second;
	}

	@Override
	public Const visit(FQLProgram env, Inr e) {
		return plus(e.s.accept(env, this), e.t.accept(env, this)).third;
	}

	@Override
	public Const visit(FQLProgram env,
			Apply e) {
		throw new RuntimeException();
	}

	@Override
	public Const visit(FQLProgram env,
			Curry e) {
		throw new RuntimeException();
	}

	@Override
	public Const visit(FQLProgram env,
			Case e) {
		Const lx = e.l.accept(env, this);
		Const rx = e.r.accept(env, this);
		SigExp.Const cx = lx.dst.accept(env, this);

		return plus(lx.src.accept(env, this),
				rx.src.accept(env, this)).fourth.of(new Triple<>(cx, lx,
				rx));

	}

	@Override
	public Const visit(FQLProgram env,
			Prod e) {
		Const lx = e.l.accept(env, this);
		Const rx = e.r.accept(env, this);
		SigExp.Const cx = lx.src.accept(env, this);
		SigExp.Const dx = rx.src.accept(env, this);
		if (!cx.equals(dx)) {
			throw new RuntimeException(cx + " and " + dx + " and " + lx + " and " + rx);
		}
		
		return prod(lx.dst.accept(env, this),
				rx.dst.accept(env, this)).fourth.of(new Triple<>(cx, lx,
				rx));
	}

}
