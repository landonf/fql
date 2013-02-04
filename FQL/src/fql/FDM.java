package fql;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class FDM {

	public static <Obj, Y, X> Set<Value<Y,X>[]> productN(Map<Obj, Set<Value<Y,X>>> I,
			List<Obj> objs) {

		Set<Value<Y,X>[]> ret = null;
		for (Obj o : objs) {
			if (ret == null) {
				ret = up(I.get(o));
				continue;
			}
			ret = product(ret, up(I.get(o)));
		}
		if (ret == null) {
			throw new RuntimeException("No nodes in N+1ary product " + I
					+ " and  + objs");
		}
		return ret;
	}

	private static <Y,X> Set<Value<Y,X>[]> up(Set<Value<Y,X>> set) {
		Set<Value<Y,X>[]> ret = new HashSet<Value<Y,X>[]>();
		for (Value<Y,X> s : set) {
			ret.add(new Value[] { s });
		}
		return ret;
	}

	public static <Obj, Arrow, Y, X> Set<Value<Y,X>[]> lim2(FinCat<Obj, Arrow> B,
			Inst<Obj, Arrow, Y, X> I) throws FQLException {

		// System.out.println("taking limit, I is " + I);

		Set<Value<Y, X>[]> x0 = productN(I.objM, B.objects);

		// System.out.println("big product is " + pn(x0));

		Map<Obj, Integer> cnames = new HashMap<>();
		int i = 0;
		for (Obj o : B.objects) {
			cnames.put(o, i++);
		}

		// System.out.println("cnames are " + cnames);

		int m = B.objects.size();
		for (Arr<Obj, Arrow> e : B.arrows) {
			x0 = product(x0, graph(I.applyA(e)));
			// System.out.println("after prod " + pn(x0));
			// System.out.println("selecting col " + m + " and " +
			// cnames.get(B.src(e)));
			x0 = select(x0, m, cnames.get(e.src));
			// System.out.println("after select1 " + pn(x0));
			x0 = select(x0, m + 1, cnames.get(e.dst));
			// System.out.println("after select2 " + pn(x0));
			x0 = firstM(x0, B.objects.size());
			// System.out.println("after firstM " + pn(x0));

		}

		// System.out.println("after arrow processing " + pn(x0));

		x0 = keygen(x0);

//		 System.out.println("after keygen " + pn(x0));
		return x0;

	}

	private static <X> String pn(Set<X[]> x0) {
		String ret = "\n";
		for (X[] x : x0) {
			for (X y : x) {
				ret += y;
				ret += " ,,,,,, ";
			}
			ret += "\n";
		}
		return ret;
	}

	private static <X> Set<X[]> select(Set<X[]> i, int m, int n) {
		Set<X[]> ret = new HashSet<>();
		for (X[] tuple : i) {
			if (tuple[m].equals(tuple[n])) {
				ret.add(tuple);
			}
		}
		return ret;
	}

	private static <Y,X> Set<Value<Y,X>[]> keygen(Set<Value<Y,X>[]> x0) {
		Set<Value<Y,X>[]> ret = new HashSet<>();
		for (Value<Y,X>[] x : x0) {
			Value<Y,X>[] y =  new Value[x.length + 1];
			y[0] = new Value<>(x);
			for (int j = 1; j <= x.length; j++) {
				y[j] = x[j - 1];
			}
			ret.add(y);
		}
		return ret;
	}

	private static <Y,X> Set<Value<Y,X>[]> firstM(Set<Value<Y,X>[]> X, int size) {
		Set<Value<Y,X>[]> ret = new HashSet<>();
		for (Value<Y,X>[] x : X) {
			Value<Y,X>[] g =  new Value[size];
			for (int i = 0; i < size; i++) {
				g[i] = x[i];
			}
			ret.add(g);
		}
		return ret;
	}

	private static <Y,X> Set<Value<Y,X>[]> graph(Map<Value<Y,X>, Value<Y,X>> m) {
		Set<Value<Y,X>[]> ret = new HashSet<>();
		for (Entry<Value<Y,X>, Value<Y,X>> e : m.entrySet()) {
			ret.add( new Value[] { e.getKey(), e.getValue() });
		}
		return ret;
	}

	 private static <X> Set<Pair<X, X>> graph2(Map<X, X> m) {
	 Set<Pair<X, X>> ret = new HashSet<>();
	 for (Entry<X, X> e : m.entrySet()) {
	 ret.add(new Pair<>(e.getKey(), e.getValue()));
	 }
	 return ret;
	 }

	public static <ObjC, ArrowC, ObjD, ArrowD, Y, X> Inst<ObjD, ArrowD, Y, X> 
	pi(
			FinFunctor<ObjC, ArrowC, ObjD, ArrowD> F, Inst<ObjC, ArrowC, Y, X> inst)
			throws FQLException {
		FinCat<ObjD, ArrowD> D = F.dstCat;
		FinCat<ObjC, ArrowC> C = F.srcCat;

		Map<ObjD, Set<Value<Y, X>>> ret1 = new HashMap<>();
		Map<Arr<ObjD, ArrowD>, Map<Value<Y, X>, Value<Y, X>>> ret2 = new HashMap<>();

		Map<ObjD, CommaCat<ObjD, ArrowD, ObjC, ArrowC, ObjD, ArrowD>> nodecats = new HashMap<>();
		Map<ObjD, Set<Value<Y, X>[]>> nodetables = new HashMap<>();

		for (ObjD d0 : D.objects()) {
			CommaCat<ObjD, ArrowD, ObjC, ArrowC, ObjD, ArrowD> B = doComma2(D,
					C, F, d0);

			Set<Value<Y, X>[]> r = lim2(B, delta(B.projB(), inst));

			ret1.put(d0, squish(r));
			nodecats.put(d0, B);
			nodetables.put(d0, r);
		}

		for (Arr<ObjD, ArrowD> s : D.arrows) {

			// if (D.isId(s)) {
			// continue; //TODO: wtf
			// }

			ObjD dA = s.src;
			CommaCat<ObjD, ArrowD, ObjC, ArrowC, ObjD, ArrowD> BA = nodecats
					.get(dA);
			// doComma2(D,C, F, dA);
			Set<Value<Y, X>[]> q1 = nodetables.get(dA);
			Map<ObjC, Integer> cnames1 = new HashMap<>();
			int i = 0;
			for (Triple<ObjD, ObjC, Arr<ObjD, ArrowD>> o : BA.objects) {
				cnames1.put(o.second, i++);
			}

			ObjD dB = s.dst;
			CommaCat<ObjD, ArrowD, ObjC, ArrowC, ObjD, ArrowD> BB = nodecats
					.get(dB); // doComma2(D,C, F, dB);
			Set<Value<Y, X>[]> q2 = nodetables.get(dB); // lim2(BB, delta(BB.projB(),
													// inst));
			Map<ObjC, Integer> cnames2 = new HashMap<>();
			i = 0;
			for (Triple<ObjD, ObjC, Arr<ObjD, ArrowD>> o : BB.objects) {
				cnames2.put(o.second, i++);
			}

			// RA rau = chop1(q2.second.length, q2.first);
			// RA rav = chop1(q1.second.length, q1.first);
			Set<Value<Y, X>[]> raw = product(q2, q1);

			Set<Value<Y, X>[]> rax = subset(cnames2, cnames1, raw);

			Map<Value<Y, X>, Value<Y, X>> ray = project(rax, cnames2.size() + 1, 0);

			ret2.put(s, ray);
		}

		// for (ObjD d0 : D.objects()) {
		// ArrowD d0id = D.id(d0);
		// ret2.put(d0id, dupl(ret1.get(d0)));
		// }

		return new Inst<>(ret1, ret2, D);
	}

	// private static Map<String, String> dupl(Set<String> set) {
	// Map<String, String> ret = new HashMap<>();
	// for (String s : set) {
	// ret.put(s, s);
	// }
	// return ret;
	// }

	static <ObjC,Y,X> Set<Value<Y,X>[]> subset(Map<ObjC, Integer> q1cols,
			Map<ObjC, Integer> q2cols, Set<Value<Y,X>[]> q1q2) {
		for (ObjC x : q1cols.keySet()) {
			int i = q1cols.get(x);
			int j = q2cols.get(x);
			q1q2 = select(q1q2, i + 1, j + 2 + q1cols.size());
		}
		return q1q2;
	}

	private static <X> Map<X, X> project(Set<X[]> x, int i, int j) {
		Map<X, X> ret = new HashMap<X, X>();
		for (X[] s : x) {
			if (ret.containsKey(s[i]) && !ret.get(s[i]).equals(s[j])) {
				throw new RuntimeException("Is not map : " + pn(x) + " on " + i
						+ " and " + j);
			}
			ret.put(s[i], s[j]);
		}
		return ret;
	}

	private static <X> Set<X> squish(Set<X[]> r) {
		Set<X> ret = new HashSet<X>();
		for (X[] x : r) {
			ret.add(x[0]);
		}
		return ret;
	}

	private static <Y,X> Set<Value<Y,X>[]> product(Set<Value<Y,X>[]> A, Set<Value<Y,X>[]> B) {
		Set<Value<Y,X>[]> ret = new HashSet<>();
		for (Value<Y,X>[] a : A) {
			for (Value<Y,X>[] b : B) {
				Value<Y,X>[] c =  new Value[a.length + b.length];
				int i = 0;
				for (Value<Y,X> x : a) {
					c[i++] = x;
				}
				for (Value<Y,X> x : b) {
					c[i++] = x;
				}
				ret.add(c);
			}
		}
		return ret;
	}

	public static <ObjC, ArrowC, ObjD, ArrowD> CommaCat<ObjD, ArrowD, ObjC, ArrowC, ObjD, ArrowD> doComma2(
			FinCat<ObjD, ArrowD> D, FinCat<ObjC, ArrowC> C,
			FinFunctor<ObjC, ArrowC, ObjD, ArrowD> F, ObjD d0) {

		FinFunctor<ObjD, ArrowD, ObjD, ArrowD> d = FinFunctor.singleton(D, d0,
				D.id(d0));
		CommaCat<ObjD, ArrowD, ObjC, ArrowC, ObjD, ArrowD> B = new CommaCat<ObjD, ArrowD, ObjC, ArrowC, ObjD, ArrowD>(
				d.srcCat, C, D, d, F);

		return B;
	}

	public static <A, B, C> List<Triple<A, B, C>> fpsets(List<A> A, List<B> B,
			List<C> C, Map<A, C> f, Map<B, C> g) {
		List<Triple<A, B, C>> ret = new LinkedList<>();

		// System.out.println("taking fp of\n" + A + "\n" + B + "\n" + C + "\n"
		// + f + "\n" + g);
		for (A a : A) {
			for (B b : B) {
				for (C c : C) {
					C c1 = f.get(a);
					C c2 = g.get(b);
					if (c1 == null || c2 == null) {
						continue;
					}
					if (c.equals(c1) && c.equals(c2)) {
						ret.add(new Triple<>(a, b, c));
					}
				}
			}
		}

		return ret;
	}

	public static <ObjA, ArrowA, ObjB, ArrowB, ObjC, ArrowC> 
	Triple<FinCat    <Triple<ObjA, ObjB, ObjC>, Triple<Arr<ObjA, ArrowA>, Arr<ObjB, ArrowB>, Arr<ObjC, ArrowC>>>, 
	       FinFunctor<Triple<ObjA, ObjB, ObjC>, Triple<Arr<ObjA, ArrowA>, Arr<ObjB, ArrowB>, Arr<ObjC, ArrowC>>, ObjA, ArrowA>, 
	       FinFunctor<Triple<ObjA, ObjB, ObjC>, Triple<Arr<ObjA, ArrowA>, Arr<ObjB, ArrowB>, Arr<ObjC, ArrowC>>, ObjB, ArrowB>> 
	pullback(
			FinCat<ObjA, ArrowA> A, FinCat<ObjB, ArrowB> B,
			FinCat<ObjC, ArrowC> C, FinFunctor<ObjA, ArrowA, ObjC, ArrowC> f,
			FinFunctor<ObjB, ArrowB, ObjC, ArrowC> g) 
			{

		List<Triple<ObjA, ObjB, ObjC>> objects = fpsets(A.objects, B.objects,
				C.objects, f.objMapping, g.objMapping);
		List<Triple<Arr<ObjA, ArrowA>, Arr<ObjB, ArrowB>, Arr<ObjC, ArrowC>>> arrows0 = fpsets(
				A.arrows, B.arrows, C.arrows, f.arrowMapping, g.arrowMapping);
		
		List<Arr<Triple<ObjA, ObjB, ObjC>, Triple<Arr<ObjA, ArrowA>, Arr<ObjB, ArrowB>, Arr<ObjC, ArrowC>>>> arrows
		= new LinkedList<>();
//		 Map<Triple<Arr<ObjA,ArrowA>, Arr<ObjB,ArrowB>, Arr<ObjC,ArrowC>>,
//		 Triple<ObjA, ObjB, ObjC>> src = new HashMap<>();
//		 Map<Triple<Arr<ObjA,ArrowA>, Arr<ObjB,ArrowB>, Arr<ObjC,ArrowC>>,
//		 Triple<ObjA, ObjB, ObjC>> dst = new HashMap<>();
		 for (Triple<Arr<ObjA, ArrowA>, Arr<ObjB, ArrowB>, Arr<ObjC, ArrowC>>
		 arrow : arrows0) {
			 arrows.add(new Arr<>(arrow, new Triple<>(arrow.first.src, arrow.second.src, arrow.third.src), new Triple<>(arrow.first.dst,
					 arrow.second.dst, arrow.third.dst)));
		 }

		Map<Triple<ObjA, ObjB, ObjC>, Arr<Triple<ObjA,ObjB,ObjC>, Triple<Arr<ObjA, ArrowA>, Arr<ObjB, ArrowB>, Arr<ObjC, ArrowC>>>> identities = new HashMap<>();
		for (Triple<ObjA, ObjB, ObjC> object : objects) {
			identities.put(
					object,
					new Arr<>(new Triple<>(A.id(object.first), B.id(object.second), C
							.id(object.third)), object, object));
		}

		Map<Pair<Arr<Triple<ObjA,ObjB,ObjC>, Triple<Arr<ObjA, ArrowA>, Arr<ObjB, ArrowB>, Arr<ObjC, ArrowC>>>, 
		         Arr<Triple<ObjA,ObjB,ObjC>, Triple<Arr<ObjA, ArrowA>, Arr<ObjB, ArrowB>, Arr<ObjC, ArrowC>>>>, 
		         Arr<Triple<ObjA,ObjB,ObjC>, Triple<Arr<ObjA, ArrowA>, Arr<ObjB, ArrowB>, Arr<ObjC, ArrowC>>>> 
		composition = new HashMap<>();
		for (Arr<Triple<ObjA, ObjB, ObjC>, Triple<Arr<ObjA, ArrowA>, Arr<ObjB, ArrowB>, Arr<ObjC, ArrowC>>> a : arrows) {
			for (Arr<Triple<ObjA, ObjB, ObjC>, Triple<Arr<ObjA, ArrowA>, Arr<ObjB, ArrowB>, Arr<ObjC, ArrowC>>> b : arrows) {
				if (a.dst.equals(b.src)) {
					composition.put(
							new Pair<>(a, b), new Arr<>(
							new Triple<>(A.compose(a.arr.first, b.arr.first), B
									.compose(a.arr.second, b.arr.second), C.compose(
									a.arr.third, b.arr.third)), a.src, b.dst));
				}
			}
		}

		FinCat<Triple<ObjA, ObjB, ObjC>, Triple<Arr<ObjA, ArrowA>, Arr<ObjB, ArrowB>, Arr<ObjC, ArrowC>>> ret1 = new FinCat<>(
				objects, arrows, composition, identities);

		Map<Triple<ObjA, ObjB, ObjC>, ObjA> ret1A = new HashMap<>();
		for (Triple<ObjA, ObjB, ObjC> o : ret1.objects) {
			ret1A.put(o, o.first);
		}
		Map<Arr<Triple<ObjA, ObjB, ObjC>,Triple<Arr<ObjA,ArrowA>, Arr<ObjB,ArrowB>, Arr<ObjC,ArrowC>>>, Arr<ObjA,ArrowA>> ret1B = new HashMap<>();
		for (Arr<Triple<ObjA, ObjB, ObjC>, Triple<Arr<ObjA, ArrowA>, Arr<ObjB, ArrowB>, Arr<ObjC, ArrowC>>> x : ret1.arrows) {
			ret1B.put(x, x.arr.first);
		}
		FinFunctor<Triple<ObjA, ObjB, ObjC>, Triple<Arr<ObjA, ArrowA>, Arr<ObjB, ArrowB>, Arr<ObjC, ArrowC>>, ObjA, ArrowA> ret2 = new FinFunctor<>(
				ret1A, ret1B, ret1, A);

		Map<Triple<ObjA, ObjB, ObjC>, ObjB> ret2A = new HashMap<>();
		for (Triple<ObjA, ObjB, ObjC> o : ret1.objects) {
			ret2A.put(o, o.second);
		}
		Map<Arr<Triple<ObjA, ObjB, ObjC>,Triple<Arr<ObjA,ArrowA>, Arr<ObjB,ArrowB>, Arr<ObjC,ArrowC>>>, Arr<ObjB,ArrowB>> ret2B = new HashMap<>();
		for (Arr<Triple<ObjA, ObjB, ObjC>, Triple<Arr<ObjA, ArrowA>, Arr<ObjB, ArrowB>, Arr<ObjC, ArrowC>>> x : ret1.arrows) {
			ret2B.put(x, x.arr.second);
		}
		FinFunctor<Triple<ObjA, ObjB, ObjC>, Triple<Arr<ObjA, ArrowA>, Arr<ObjB, ArrowB>, Arr<ObjC, ArrowC>>, ObjB, ArrowB> ret3 = new FinFunctor<>(
				ret2A, ret2B, ret1, B);

		return new Triple<>(ret1, ret2, ret3);
	}

	// private static <ObjC, ArrowC, ObjD, ArrowD> ArrowC findEquiv2(ObjC c,
	// FinFunctor<ObjC,ArrowC,ObjD,ArrowD> f, ArrowD e)
	// throws FQLException {
	// FinCat<ObjC,ArrowC> C = f.srcCat;
	// FinCat<ObjD,ArrowD> D = f.dstCat;
	//
	// for (ArrowC path : C.arrows) {
	// // List<String> p = peqc.get(0);
	// // Path path = new Path(f.source, p);
	// if (!C.src(path).equals(c)) {
	// continue;
	// }
	// ArrowD path_f = f.arrowMapping.get(path);
	// if (path_f.equals(e)) {
	// return path;
	// }
	// }
	//
	// throw new FQLException("[Sem] Could not find path mapping to " + e
	// + " under " + f);
	// }

	//
	// public static <ObjX, ArrowX, ObjC, ArrowC> Pair<Map<ObjC, Set<String>>,
	// Map<ArrowC, Map<String,String>>>
	// sigma(FinFunctor<ObjX, ArrowX, ObjC, ArrowC> F) throws FQLException {
	//
	// FinCat<ObjX, ArrowX> C = F.srcCat;
	// FinCat<ObjC, ArrowC> D = F.dstCat;
	//
	// Map<ObjC, Set<String>> ret1 = new HashMap<>();
	// Map<ArrowC, Map<String, String>> ret2 = new HashMap<>();
	//
	// // Map<String, RA> ret = new HashMap<String, RA>();
	// //Map<ObjC, List<ObjX>> tags = new HashMap<>();
	//
	// for (ObjC d : D.objects) {
	// List<ObjX> tn = new LinkedList<>();
	// List<ObjX> tj = new LinkedList<>();
	// for (ObjX c : C.objects) {
	// if (F.objMapping.get(c).equals(d)) {
	// tn.add(c);
	// tj.add(c);
	// tj.add(c);
	// }
	// }
	// ret1.put(d, DisjointUnion.exec1(tn,tj)); //new DisjointUnion(tn, tj));
	// //tags.put(d, tj);
	// // System.out.println("on " + d + " tags " + tj.toString());
	// }
	//
	// for (ArrowC e : D.arrows) {
	// ObjC d = D.src(e);
	// ObjC d0 = D.dst(e);
	// List<ArrowX> tn = new LinkedList<>();
	// List<ObjX> tx = new LinkedList<>();
	// for (ObjX c : C.objects) {
	// if (F.objMapping.get(c).equals(d)) {
	// ArrowX pc = findEquiv2(c, F, e);
	// // Path pc = findEquiv(c, F, e);
	//
	// // RA q = compose(pc);
	// tn.add(pc);
	// tx.add(c);
	// tx.add(C.src(pc));
	// }
	// }
	// ret2.put(e, DisjointUnion.exec2(tn, tx));
	// }
	// return new Pair<>(ret1, ret2);
	// }
	//
	// public static <ObjX, ArrowX, ObjC, ArrowC> Inst<ObjC, ArrowC>
	// degrothendieck(
	// FinFunctor<ObjX, ArrowX, ObjC, ArrowC> F) throws FQLException {
	//
	// FinCat<ObjC, ArrowC> C = F.dstCat;
	// Triple<Mapping, Map<ArrowX, String>, Map<ArrowC, String>> f0 = F
	// .toMapping();
	// Mapping f = f0.first;
	// Instance i = Instance.terminal(f.source);
	// Map<String, Set<Pair<String, String>>> j = f.evalSigma(i);
	//
	// // System.out.println("********");
	// // System.out.println("degrothendiek " + F);
	// // System.out.println("\ntarget cat is --\n");
	// // System.out.println(C);
	// // System.out.println("\nsrc cat is --\n");
	// // System.out.println(F.srcCat);
	// //
	// // System.out.println("\ni is --\n");
	// // System.out.println(i);
	// // System.out.println("\n j is --\n");
	// // System.out.println(j);
	// // System.out.println("********");
	//
	// Map<ArrowC, String> names = f0.third;
	// Map<ObjC, Set<String>> ret1 = new HashMap<>();
	// Map<ArrowC, Map<String, String>> ret2 = new HashMap<>();
	//
	// for (ArrowC c : C.arrows()) {
	// if (C.isId(c)) {
	// ret1.put(C.src(c), derefl(j.get(names.get(c))));
	// }
	// ret2.put(c, degraph(j.get(names.get(c))));
	// }
	//
	// return new Inst<>(ret1, ret2, C);
	// }

	public static <ObjX, ArrowX, ObjC, ArrowC> 
	Inst<ObjC, ArrowC, ObjX, ObjX> degrothendieck(FinFunctor<ObjX, ArrowX, ObjC, ArrowC> F) throws FQLException {

		FinCat<ObjC, ArrowC> C = F.dstCat;
		Inst<ObjX, ArrowX, ObjX, ObjX> i = Inst.terminal(F.srcCat);
		Inst<ObjC, ArrowC, ObjX, ObjX> j = sigma(F, i);

		return j;

	}

	private static <X> Set<X> derefl(Set<Pair<X, X>> set) {
		Set<X> ret = new HashSet<>(set.size());
		for (Pair<X, X> p : set) {
			ret.add(p.first);
		}
		return ret;
	}

	private static <X> Set<Pair<X, X>> refl(Set<X> set) {
		Set<Pair<X, X>> ret = new HashSet<>(set.size());
		for (X p : set) {
			ret.add(new Pair<>(p, p));
		}
		return ret;
	}

	static <Y,X> Map<Value<Y,X>, Value<Y,X>> degraph(Set<Pair<X, X>> set) {
		Map<Value<Y,X>, Value<Y,X>> ret = new HashMap<>(set.size());
		for (Pair<X, X> p : set) {
			ret.put(new Value<Y,X>(p.first), new Value<Y,X>(p.second));
		}
		return ret;
	}

	static <Y,X> Map<Value<Y,X>, Value<Y,X>> degraph2(Set<Pair<Value<Y,X>, Value<Y,X>>> set) {
		Map<Value<Y,X>, Value<Y,X>> ret = new HashMap<>(set.size());
		for (Pair<Value<Y, X>, Value<Y, X>> p : set) {
			ret.put(p.first, p.second);
		}
		return ret;
	}
	// public static <Obj, Arrow> FinFunctor<Pair<Obj, String>, Arrow, Obj,
	// Arrow>

	public static <Obj, Arrow, Y, X> 
	FinFunctor<Pair<Obj, Value<Y, X>>, Arr<Obj, Arrow>, Pair<Obj, Value<Y, X>>, Arr<Obj, Arrow>> 
	grothendieck(
			SetFunTrans<Obj, Arrow, Y, X> sf) {

		FinFunctor<Pair<Obj, Value<Y, X>>, Arr<Obj, Arrow>, Obj, Arrow> r1 = grothendieck(sf.F);
		FinFunctor<Pair<Obj, Value<Y, X>>, Arr<Obj, Arrow>, Obj, Arrow> r2 = grothendieck(sf.G);

		FinCat<Pair<Obj, Value<Y,X>>, Arr<Obj, Arrow>> c1 = r1.srcCat;
		FinCat<Pair<Obj, Value<Y,X>>, Arr<Obj, Arrow>> c2 = r2.srcCat;

		Map<Pair<Obj, Value<Y,X>>, Pair<Obj, Value<Y,X>>> objM = new HashMap<>();
		Map<Arr<Pair<Obj, Value<Y,X>>, Arr<Obj, Arrow>>, Arr<Pair<Obj, Value<Y,X>>, Arr<Obj, Arrow>>> arrM = new HashMap<>();

		for (Pair<Obj, Value<Y,X>> a : c1.objects) {
			objM.put(a, new Pair<>(a.first, sf.eta(a.first).get(a.second)));
		}

		for (Arr<Pair<Obj, Value<Y,X>>, Arr<Obj, Arrow>> a : c1.arrows) {
			arrM.put(a, new Arr<>(a.arr, objM.get(a.src), objM.get(a.dst)));
		}
//		System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
//		System.out.println("c1 is " +  c1);
//		System.out.println("c2 is " +  c2);
//		System.out.println("objM " + objM);
		
		return new FinFunctor<>(objM, arrM, c1, c2);
	}

	public static <Obj, Arrow, Y, X> 
	FinFunctor<Pair<Obj, Value<Y,X>>, Arr<Obj, Arrow>, Obj, Arrow> 
	grothendieck(
			Inst<Obj, Arrow, Y, X> sf) {

		// System.out.println("taking grothendieck of ");
		// System.out.println(sf);

		FinCat<Obj, Arrow> C = sf.cat;
		Map<Obj, Set<Value<Y, X>>> objM = sf.objM;
		Map<Arr<Obj, Arrow>, Map<Value<Y, X>, Value<Y, X>>> arrM = sf.arrM;

		Set<Pair<Obj, Value<Y, X>>> objects = new HashSet<>();
		for (Obj o : C.objects()) {
			for (Value<Y, X> s : objM.get(o)) {
				objects.add(new Pair<>(o, s));
			}
		}

		Set<Arr<Pair<Obj, Value<Y, X>>, Arr<Obj,Arrow>>> arrows = new HashSet<>();
		// Map<Arrow, Pair<Obj, String>> src = new HashMap<>();
		// Map<Arrow, Pair<Obj, String>> dst = new HashMap<>();
		for (Pair<Obj, Value<Y, X>> o1 : objects) {
			for (Pair<Obj, Value<Y, X>> o2 : objects) {
				for (Arr<Obj, Arrow> a : C.hom(o1.first, o2.first)) {
					if (sf.applyA(a).get(o1.second).equals(o2.second)) {
						arrows.add(new Arr<>(a, o1, o2));
						// src.put(a, o1);
						// dst.put(a, o2);
					}
				}
			}
		}

		Map <Pair<Obj, Value<Y, X>>, Arr<Pair<Obj, Value<Y, X>>, Arr<Obj,Arrow>>> identities = new HashMap<>();
		for (Pair<Obj, Value<Y, X>> o : objects) {
			identities.put(o, new Arr<>(C.id(o.first), o, o));
		}

		Map<Pair<Arr<Pair<Obj, Value<Y, X>>, Arr<Obj,Arrow>>,Arr<Pair<Obj, Value<Y,X>>, Arr<Obj,Arrow>>>,Arr<Pair<Obj, Value<Y,X>>, Arr<Obj,Arrow>>>
		composition = new HashMap<>();
		for (Arr<Pair<Obj, Value<Y, X>>, Arr<Obj, Arrow>> a : arrows) {
			for (Arr<Pair<Obj, Value<Y, X>>, Arr<Obj, Arrow>> b : arrows) {
				if (a.dst.equals(b.src)) {
					composition.put(new Pair<>(a, b), new Arr<>(C.compose(a.arr, b.arr), a.src, b.dst));
				}
			}
		}

		FinCat<Pair<Obj, Value<Y,X>>, Arr<Obj,Arrow>> ret1 = new FinCat<>(new LinkedList<>(
				objects), new LinkedList<>(arrows), composition, identities);

		Map<Pair<Obj, Value<Y,X>>, Obj> m1 = new HashMap<>();
		for (Pair<Obj, Value<Y,X>> p : objects) {
			m1.put(p, p.first);
		}
		Map <Arr<Pair<Obj, Value<Y,X>>, Arr<Obj,Arrow>>, 
		    Arr<Obj,Arrow>> m2 = new HashMap<>();
		for (Arr<Pair<Obj, Value<Y,X>>, Arr<Obj, Arrow>> a : arrows) {
			m2.put(a, a.arr);
		}

		FinFunctor<Pair<Obj, Value<Y,X>>, Arr<Obj,Arrow>, Obj, Arrow> ret2 = new FinFunctor<>(
				m1, m2, ret1, C);

		return ret2;
		// return new Pair<>(ret1, ret2);
	}

	// public static <ObjA, ArrowA, ObjB, ArrowB> Inst<ObjA, ArrowA> pi2(
	// FinFunctor<ObjB, ArrowB, ObjA, ArrowA> ff, Inst<ObjB, ArrowB> ii)
	// throws FQLException {
	//
	// Map<ObjB, Set<String>> i1 = ii.objM;
	// Map<ArrowB, Map<String, String>> i2 = ii.arrM;
	//
	// Instance i = Instance.populate(ff.srcCat, i1, i2);
	// Triple<Mapping, Map<ArrowB, String>, Map<ArrowA, String>> triple = ff
	// .toMapping();
	// Map<String, Set<Pair<String, String>>> j = triple.first.evalPi(i);
	//
	// FinCat<ObjA, ArrowA> A = ff.dstCat;
	//
	// Map<ArrowA, String> names = triple.third;
	// Map<ObjA, Set<String>> ret1 = new HashMap<>();
	// Map<ArrowA, Map<String, String>> ret2 = new HashMap<>();
	// for (ArrowA a : A.arrows()) {
	// if (A.isId(a)) {
	// ret1.put(A.src(a), derefl(j.get(names.get(a))));
	// }
	// ret2.put(a, degraph(j.get(names.get(a))));
	// }
	//
	// return new Inst<>(ret1, ret2, ff.dstCat);
	// }

	public static <ObjC, ArrowC, ObjD, ArrowD, Y, X> Inst<ObjC, ArrowC, Y, X>
	delta(
			FinFunctor<ObjC, ArrowC, ObjD, ArrowD> F, Inst<ObjD, ArrowD, Y, X> I) {
		Map<ObjC, Set<Value<Y, X>>> ret1 = new HashMap<>();
		Map<Arr<ObjC, ArrowC>, Map<Value<Y, X>, Value<Y, X>>> ret2 = new HashMap<>();

		for (ObjC o : F.srcCat.objects) {
			ret1.put(o, I.objM.get(F.applyO(o)));
		}

		for (Arr<ObjC, ArrowC> a : F.srcCat.arrows) {
			ret2.put(a, I.arrM.get(F.applyA(a)));
		}

		return new Inst<>(ret1, ret2, F.srcCat);
	}

	// public static <ObjA, ArrowA, ObjB, ArrowB, ObC, ArrowC>
	// Pair<Map<ObjC, Set<String>>, Map<ArrowC, Map<String, String>>>
	// epsilon(FinFunctor<ObjB, ArrowB, ObjA, ArrowA> f, Map<ObjB, Set<String>>
	// i1, Map<ArrowB, Map<String, String>> i2) {
	// return null;
	// }

	static <ObjC, ArrowC, ObjD, ArrowD, Y, X> SetFunTrans<ObjC, ArrowC, Y, X>
	epsilon(
			FinFunctor<ObjC, ArrowC, ObjD, ArrowD> F, Inst<ObjC, ArrowC, Y, X> res,
			Inst<ObjC, ArrowC, Y, X> I) throws FQLException {

//		System.out.println("Computing epsilon");
//		System.out.println("F " + F);
//		System.out.println("res " + res);
//		System.out.println("I " + I);
		Map<ObjC, Map<Value<Y, X>, Value<Y, X>>> map = new HashMap<>();

		for (ObjC C : F.srcCat.objects) {
			CommaCat<ObjD, ArrowD, ObjC, ArrowC, ObjD, ArrowD> B = doComma2(
					F.dstCat, F.srcCat, F, F.applyO(C));

			Set<Value<Y, X>[]> r = lim2(B, delta(B.projB(), I));
			int i = 0;
			boolean flag = true;
			for (Triple<ObjD, ObjC, Arr<ObjD, ArrowD>> o : B.objects) {
				if (o.second.equals(C)) {
					Map<Value<Y,X>, Value<Y,X>> xxx = project(r, 0, i + 1);
					map.put(C, xxx); //new Arr<>(xxx, C, o.second));
					flag = false;
					break;
				}
				i++;
			}
			if (flag) {
				throw new RuntimeException("Couldn't find " + C + " in "
						+ B.objects);
			}
		}

		return new SetFunTrans<>(map, res, I);
	}

	// public static <ObjC, ArrowC, ObjB, ArrowB, ObjA, ArrowA> void
	// distribute2(
	// FinFunctor<ObjC, ArrowC, ObjB, ArrowB> u,
	// FinFunctor<ObjB, ArrowB, ObjA, ArrowA> f) throws FQLException {
	// FinCat<ObjA, ArrowA> A = f.dstCat;
	// FinCat<ObjB, ArrowB> B = f.srcCat;
	// FinCat<ObjC, ArrowC> C = u.srcCat;
	//
	// Inst<ObjB, ArrowB> du = degrothendieck(u);
	//
	// Inst<ObjA, ArrowA> piresult = pi(f, du);
	//
	// FinFunctor<Pair<ObjA, String>, ArrowA, ObjA, ArrowA> v =
	// grothendieck(piresult);
	// FinCat<Pair<ObjA, String>, ArrowA> M = v.srcCat;
	//
	// Inst<ObjB, ArrowB> deltaresult = delta(f, piresult);
	// FinFunctor<Pair<ObjB, String>, ArrowB, ObjB, ArrowB> w =
	// grothendieck(deltaresult);
	// FinCat<Pair<ObjB, String>, ArrowB> N = w.srcCat;
	//
	// // SetFunTrans<ObjB, ArrowB> epsilonresult = epsilon(f, du);
	// // FinFunctor<Pair<ObjB, String>, ArrowB, Pair<ObjB, String>, ArrowB> s
	// // = grothendieck(epsilonresult);
	//
	// // should beq equal
	// // FinCat<Pair<ObjB, String>, ArrowB> intepsilon = s.dstCat;
	// // FinCat<Pair<ObjB, String>, ArrowB> intbdu = grothendieck(du).srcCat;
	//
	// // should be equal to N
	// // FinCat<Pair<ObjB, String>, ArrowB> Nprime = s.srcCat;
	//
	// // FinFunctor<ObjC, ArrowC, ObjB, ArrowB> u,
	//
	// FinFunctor<Pair<ObjB, String>, ArrowB, ObjB, ArrowB> k =
	// grothendieck(du);
	// // FinFunctor<ObjC, ArrowC, ObjB, ArrowB> u0;
	//
	// // intbdu should equal Bprime
	// // Nprime should equal N
	// System.out.println("******************");
	// System.out.println("u is\n" + u);
	// System.out.println("******************");
	// System.out.println("du is\n" + du);
	// System.out.println("******************");
	// System.out.println("intbdu is\n" + k);
	// System.out.println("******************");
	//
	// // Pair<Map<ObjC, Set<String>>, Map<ArrowC, Map<String, String>>>
	// // epsilonresult = null; // epsilon(f, du.first, du.second);
	//
	// // Pair<FinCat<Pair<ObjC, String>, ArrowC>, FinFunctor<Pair<ObjC,
	// // String>, ArrowC, ObjC, ArrowC>> gres3
	// // = grothendieck(C, epsilonresult.first, epsilonresult.second);
	//
	// // FinFunctor<Pair<ObjC, String>, ArrowC, ObjC, ArrowC> e =
	// // gres3.second;
	//
	// // Pair<Map<ObjB, Set<String>>, Map<ArrowB, Map<String, String>>>
	// // epsilonresult = null; // epsilon(f, du.first, du.second);
	// //
	// // Pair<FinCat<Pair<ObjB, String>, ArrowB>, FinFunctor<Pair<ObjB,
	// // String>, ArrowB, ObjB, ArrowB>> gres3
	// // = grothendieck(B, epsilonresult.first, epsilonresult.second);
	//
	// }
//
//	private static Set<String[]> squish2(Map<String, String> project) {
//		// TODO Auto-generated method stub
//		return null;
//	}

	static <ObjB, X> Pair<ObjB, X> find(ObjB b, List<Pair<ObjB, X>> B) {
		for (Pair<ObjB, X> b0 : B) {
			if (b0.first.equals(b)) {
				return b0;
			}
		}
		throw new RuntimeException("Couldn't find " + b + " in " + B);
	}

//	static class DistributeResult<ObjC, ArrowC, ObjB, ArrowB, ObjA, ArrowA> {
//		public DistributeResult(
//				FinFunctor<Pair<ObjA, String>, ArrowA, ObjA, ArrowA> v,
//				FinFunctor<Pair<ObjB, String>, ArrowB, ObjB, ArrowB> w,
//				FinFunctor<Pair<ObjB, String>, ArrowB, Triple<ObjA, ObjB, ObjC>, Triple<ArrowA, ArrowB, ArrowC>> e,
//				FinFunctor<Pair<ObjB, String>, ArrowB, Pair<ObjA, String>, ArrowA> g) {
//			super();
//			this.v = v;
//			this.w = w;
//			this.e = e;
//			this.g = g;
//		}
//
//		// FinFunctor<Triple<ObjA, ObjB, ObjC>, Triple<ArrowA,ArrowB,ArrowC>,
//		// ObjB, ArrowB> u;
//		// FinFunctor<ObjB, ArrowB, ObjA, ArrowA> f;
//		FinFunctor<Pair<ObjA, String>, ArrowA, ObjA, ArrowA> v;
//		FinFunctor<Pair<ObjB, String>, ArrowB, ObjB, ArrowB> w;
//		FinFunctor<Pair<ObjB, String>, ArrowB, Triple<ObjA, ObjB, ObjC>, Triple<ArrowA, ArrowB, ArrowC>> e;
//		FinFunctor<Pair<ObjB, String>, ArrowB, Pair<ObjA, String>, ArrowA> g;
//	}
//
//	static class DistributeResult2<ObjC, ArrowC, ObjB, ArrowB, ObjA, ArrowA, ObjT, ArrowT> {
//
//		// FinFunctor<Triple<ObjA, ObjB, ObjC>, Triple<ArrowA,ArrowB,ArrowC>,
//		// ObjB, ArrowB> u;
//		// FinFunctor<ObjB, ArrowB, ObjA, ArrowA> f;
//		// FinFunctor<Triple<ObjA, ObjB, ObjC>, Triple<ArrowA, ArrowB, ArrowC>,
//		// ObjA, ArrowA> v;
//		FinFunctor<Pair<ObjA, String>, ArrowA, ObjA, ArrowA> v;
//
//		FinFunctor<Pair<ObjB, String>, ArrowB, ObjB, ArrowB> w;
//
//		// FinFunctor<Triple<ObjA, ObjB, ObjC>, Triple<ArrowA, ArrowB, ArrowC>,
//		// ObjB, ArrowB> w;
//		FinFunctor<Triple<ObjA, ObjB, ObjC>, Triple<ArrowA, ArrowB, ArrowC>, Triple<ObjA, ObjB, ObjC>, Triple<ArrowA, ArrowB, ArrowC>> e;
//		FinFunctor<Triple<ObjA, ObjB, ObjC>, Triple<ArrowA, ArrowB, ArrowC>, Triple<ObjA, ObjB, ObjC>, Triple<ArrowA, ArrowB, ArrowC>> g;
//	}

	static <ObjC, ArrowC, ObjD, ArrowD> 
	FinFunctor<Pair<ObjC, String>, ArrowC, Pair<ObjD, String>, ArrowD> makeG2(
			FinCat<Pair<ObjD, String>, Arr<ObjD, ArrowD>> srcCat,
			FinCat<Pair<ObjC, String>, Arr<ObjC, ArrowC>> srcCat2,
			FinFunctor<ObjD, ArrowD, ObjC, ArrowC> g) {
	return null;
	}
	
	static <ObjA, ArrowA, ObjB, ArrowB, Y, X> 
	FinFunctor<Pair<ObjB, Value<Y,X>>, Arr<ObjB,ArrowB>, Pair<ObjA, Value<Y,X>>, Arr<ObjA,ArrowA>> makeG(
			FinCat<Pair<ObjB, Value<Y,X>>, Arr<ObjB,ArrowB>> B,
			FinCat<Pair<ObjA, Value<Y,X>>, Arr<ObjA,ArrowA>> A,
			FinFunctor<ObjB, ArrowB, ObjA, ArrowA> f) {

		Map<Pair<ObjB, Value<Y,X>>, Pair<ObjA, Value<Y,X>>> objM = new HashMap<>();
		Map<Arr<Pair<ObjB, Value<Y,X>>,Arr<ObjB,ArrowB>>,  Arr<Pair<ObjA, Value<Y,X>>,Arr<ObjA,ArrowA>>> arrM = new HashMap<>();

		for (Pair<ObjB, Value<Y,X>> p : B.objects) {
			objM.put(p, new Pair<>(f.applyO(p.first), p.second));
		}
		
		//Map<Pair<ObjA,ObjA>, Set<Arr<Pair<ObjA,String>, ObjA>>> mm = new HashMap<>();
		
		//Set<Arr<Pair<ObjA, String>, ArrowA>> xx = new HashSet<>();
	
		for (Pair<ObjB, Value<Y,X>> p1 : B.objects) {
			for (Pair<ObjB, Value<Y,X>> p2 : B.objects) {
				Set<Arr<Pair<ObjB, Value<Y,X>>, Arr<ObjB, ArrowB>>> pp = B.hom(p1, p2);
				for (Arr<Pair<ObjB, Value<Y,X>>, Arr<ObjB, ArrowB>> r : pp) {
					Arr<ObjA, ArrowA> kkk = f.applyA(r.arr); //new Arr<>(r.arr, r.src.first, r.dst.first));
					
					Pair<ObjA, Value<Y,X>> jj = objM.get(p1);
					Pair<ObjA, Value<Y,X>> kk = objM.get(p2);
					
					Arr<Pair<ObjA, Value<Y,X>>, Arr<ObjA, ArrowA>> ll = new Arr<>(kkk, jj, kk);
					arrM.put(r, ll);
					
	//				Arr<Pair<ObjA, String>, ArrowA>
				}
			}
		}
		
	

		return new FinFunctor<Pair<ObjB, Value<Y,X>>, Arr<ObjB,ArrowB>, Pair<ObjA, Value<Y,X>>, Arr<ObjA,ArrowA>>(objM, arrM, B, A);
	}

//	public static <ObjC, ArrowC, ObjB, ArrowB, ObjA, ArrowA, ObjT, ArrowT> DistributeResult2<ObjC, ArrowC, ObjB, ArrowB, ObjA, ArrowA, ObjT, ArrowT> distribute(
//			FinFunctor<Triple<ObjA, ObjB, ObjT>, Triple<ArrowA, ArrowB, ArrowT>, ObjB, ArrowB> k,
//			FinFunctor<ObjB, ArrowB, ObjC, ArrowC> g2) throws FQLException {
//
//		// f is singles
//		// System.out.println("f is " + f);
//
//		// ///////////////////////////////////////////////////////////////////////////////
//
//		FinCat<ObjC, ArrowC> A = g2.dstCat;
//		FinCat<ObjB, ArrowB> B = g2.srcCat;
//		FinCat<Triple<ObjA, ObjB, ObjT>, Triple<ArrowA, ArrowB, ArrowT>> C = k.srcCat;
//
//		Inst<ObjB, ArrowB> du = degrothendieck(k);
//
//		Inst<ObjC, ArrowC> piresult = pi(g2, du);
//
//		FinFunctor<Pair<ObjC, String>, ArrowC, ObjC, ArrowC> v = grothendieck(piresult);
//		FinCat<Pair<ObjC, String>, ArrowC> M = v.srcCat;
//
//		Inst<ObjB, ArrowB> deltaresult = delta(g2, piresult);
//		FinFunctor<Pair<ObjB, String>, ArrowB, ObjB, ArrowB> w = grothendieck(deltaresult);
//		FinCat<Pair<ObjB, String>, ArrowB> N = w.srcCat;
//
//		SetFunTrans<ObjB, ArrowB> epsilonresult = epsilon(g2, deltaresult, du);
//		FinFunctor<Pair<ObjB, String>, ArrowB, Pair<ObjB, String>, ArrowB> e = grothendieck(epsilonresult);
//
//		// FinFunctor<Pair<ObjA, String>, ArrowA, ObjA, ArrowA> v;
//		FinFunctor<Pair<ObjB, String>, ArrowB, Pair<ObjC, String>, ArrowC> g = makeG(
//				w.srcCat, v.srcCat, g2);
//
//		// DistributeResult2<ObjC, ArrowC, ObjB, ArrowB, ObjA, ArrowA> ret2 =
//		// new DistributeResult2<>();
//
//		// e
//		// ////////////////////////////////////////////////////////////////////////////////////////
//		//
//		// FinFunctor<Pair<ObjB, String>, ArrowB, Pair<ObjB, String>, ArrowB> e
//		// FinFunctor<Triple<ObjA, ObjB, ObjC>, Triple<ArrowA, ArrowB, ArrowC>,
//		// Triple<ObjA, ObjB, ObjC>, Triple<ArrowA,ArrowB,ArrowC>> eret;
//
//		Map<Triple<ObjA, ObjB, ObjT>, Pair<ObjB, String>> tripleToPairBe1 = new HashMap<>();
//		for (Triple<ObjA, ObjB, ObjT> c : k.srcCat.objects) {
//			tripleToPairBe1.put(c, find(c.second, e.srcCat.objects));
//		}
//		Map<Triple<ArrowA, ArrowB, ArrowT>, ArrowB> tripleToPairBe2 = new HashMap<>();
//		for (Arr<Triple<ObjA, ObjB, ObjT>, Triple<ArrowA, ArrowB, ArrowT>> c : k.srcCat.arrows) {
//			tripleToPairBe2.put(c, c.second);
//		}
//		FinFunctor<Triple<ObjA, ObjB, ObjT>, Triple<ArrowA, ArrowB, ArrowT>, Pair<ObjB, String>, ArrowB> tripleToPairBe = new FinFunctor<>(
//				tripleToPairBe1, tripleToPairBe2, k.srcCat, e.srcCat);
//
//		Map<Pair<ObjB, String>, Triple<ObjA, ObjB, ObjT>> pairToTripleBe1 = new HashMap<>();
//		for (Pair<ObjB, String> c : e.srcCat.objects) {
//			pairToTripleBe1.put(c, find2(c.first, k.srcCat.objects));
//		}
//		Map<ArrowB, Triple<ArrowA, ArrowB, ArrowT>> pairToTripleBe2 = new HashMap<>();
//		for (Arr<Pair<ObjB, String>, ArrowB> c : e.srcCat.arrows) {
//			pairToTripleBe2.put(c, find2(c, k.srcCat.arrows));
//		}
//		FinFunctor<Pair<ObjB, String>, ArrowB, Triple<ObjA, ObjB, ObjT>, Triple<ArrowA, ArrowB, ArrowT>> pairToTripleBe = new FinFunctor<>(
//				pairToTripleBe1, pairToTripleBe2, e.srcCat, k.srcCat);
//
//		// ret2.e = FinFunctor.compose(tripleToPairBe, e, pairToTripleBe);
//
//		// v
//		// /////////////////////////////////////////////////////////////////////////
//		//
//		// FinFunctor<Pair<ObjA, String>, ArrowA, ObjA, ArrowA> v =
//		// grothendieck(piresult);
//		// FinFunctor<Triple<ObjA, ObjB, ObjC>, Triple<ArrowA, ArrowB, ArrowC>,
//		// ObjA, ArrowA> v_ret;
//
//		// System.out.println(" v is ***");
//		// System.out.println(v);
//		// System.out.println("v dst ***");
//		// System.out.println(v.srcCat.objects);
//		// System.out.println("***");
//		//
//		// Inst<ObjA, ArrowA> dv = degrothendieck(v);
//		//
//		// FinFunctor<Pair<ObjA, String>, ArrowA, ObjA, ArrowA> k =
//		// grothendieck(dv);
//		// System.out.println("intadv " + k);
//		//
//		// Map<Triple<ObjA, ObjB, ObjC>, Pair<ObjA, String>> tripleToPairAv1 =
//		// new HashMap<>();
//		// for (Pair<ObjA, String> c : v.srcCat.objects) {
//		// tripleToPairAv1.put(find1T(c.first, k.srcCat.objects), c);
//		// }
//		// Map<Triple<ArrowA, ArrowB, ArrowC>, ArrowA> tripleToPairAv2 = new
//		// HashMap<>();
//		// for (Triple<ArrowA, ArrowB, ArrowC> c : u.srcCat.arrows) {
//		// tripleToPairAv2.put(c, c.first);
//		// }
//		//
//		// FinFunctor<Triple<ObjA, ObjB, ObjC>, Triple<ArrowA, ArrowB, ArrowC>,
//		// Pair<ObjA, String>, ArrowA> tripleToPairAv
//		// = new FinFunctor<>(tripleToPairAv1, tripleToPairAv2, u.srcCat,
//		// v.srcCat);
//		//
//		// ret2.v = FinFunctor.compose(tripleToPairAv, v);
//
//		// w
//		// /////////////////////////////////////////////////////////////////////////
//		//
//		// FinFunctor<Pair<ObjB, String>, ArrowB, ObjB, ArrowB> w =
//		// grothendieck(deltaresult);
//		// FinFunctor<Triple<ObjA, ObjB, ObjC>, Triple<ArrowA, ArrowB, ArrowC>,
//		// ObjB, ArrowB> wret;
//
//		// Map<Triple<ObjA, ObjB, ObjC>, Pair<ObjB, String>> tripleToPairBw1 =
//		// new HashMap<>();
//		// for (Triple<ObjA, ObjB, ObjC> c : u.srcCat.objects) {
//		// tripleToPairBw1.put(c, find(c.first, v.srcCat.objects));
//		// }
//		// Map<Triple<ArrowA, ArrowB, ArrowC>, ArrowB> tripleToPairBw2 = new
//		// HashMap<>();
//		// for (Triple<ArrowA, ArrowB, ArrowC> c : u.srcCat.arrows) {
//		// tripleToPairBw2.put(c, c.first);
//		// }
//		//
//		// FinFunctor<Triple<ObjA, ObjB, ObjC>, Triple<ArrowA, ArrowB, ArrowC>,
//		// Pair<ObjB, String>, ArrowB> tripleToPairBw
//		// = new FinFunctor<>(tripleToPairBw1, tripleToPairBw2, u.srcCat,
//		// w.srcCat);
//		//
//		// ret2.w = FinFunctor.compose(tripleToPairBw, w);
//
//		// g
//		// //////////////////////////////////////////////////////////////////////
//		//
//		// FinFunctor<Pair<ObjB, String>, ArrowB, Pair<ObjA, String>, ArrowA> g
//		// = null;
//		//
//		// FinFunctor<Triple<ObjA, ObjB, ObjC>, Triple<ArrowA, ArrowB, ArrowC>,
//		// Triple<ObjA, ObjB, ObjC>, Triple<ArrowA, ArrowB, ArrowC>> g;
//
//		// FinFunctor<Pair<ObjB, String>, ArrowB, Pair<ObjB, String>, ArrowB> e
//		//
//		// FinFunctor<Triple<ObjA, ObjB, ObjC>, Triple<ArrowA, ArrowB, ArrowC>,
//		// Triple<ObjA, ObjB, ObjC>, Triple<ArrowA,ArrowB,ArrowC>> eret;
//
//		// Map<Triple<ObjA, ObjB, ObjC>, Pair<ObjB, String>> tripleToPairg1 =
//		// new HashMap<>();
//		// for (Triple<ObjA, ObjB, ObjC> c : u.srcCat.objects) {
//		// tripleToPairBe1.put(c, find(c.second, g.srcCat.objects));
//		// }
//		// Map<Triple<ArrowA, ArrowB, ArrowC>, ArrowB> tripleToPairg2 = new
//		// HashMap<>();
//		// for (Triple<ArrowA, ArrowB, ArrowC> c : u.srcCat.arrows) {
//		// tripleToPairBe2.put(c, c.second);
//		// }
//		// FinFunctor<Triple<ObjA, ObjB, ObjC>, Triple<ArrowA, ArrowB, ArrowC>,
//		// Pair<ObjB, String>, ArrowB> tripleToPairg = new FinFunctor<>(
//		// tripleToPairg1, tripleToPairg2, u.srcCat, g.srcCat);
//		//
//		// Map<Pair<ObjA, String>, Triple<ObjA, ObjB, ObjC>> pairToTripleg1 =
//		// new HashMap<>();
//		// // for (Triple<ObjA, ObjB, ObjC> c : u.srcCat.objects) {
//		// // pairToTripleg1.put(c, find(c.first, g.dstCat.objects));
//		// // }
//		// Map<ArrowA, Triple<ArrowA, ArrowB, ArrowC>> pairToTripleg2 = new
//		// HashMap<>();
//		// // for (Triple<ArrowA, ArrowB, ArrowC> c : u.srcCat.arrows) {
//		// // pairToTripleg2.put(c, c.first);
//		// // }
//		//
//		// FinFunctor<Pair<ObjA, String>, ArrowA, Triple<ObjA, ObjB, ObjC>,
//		// Triple<ArrowA, ArrowB, ArrowC>> pairToTripleg = new FinFunctor<>(
//		// pairToTripleg1, pairToTripleg2, g.dstCat, u.srcCat);
//		//
//		// ret2.g = FinFunctor.compose(tripleToPairg, g, pairToTripleg);
//
//		return null; // ret2;
//	}

	static <ObjA, ObjB, ObjC> Triple<ObjA, ObjB, ObjC> find2(ObjB b,
			List<Triple<ObjA, ObjB, ObjC>> B) {
		for (Triple<ObjA, ObjB, ObjC> b0 : B) {
			if (b0.second.equals(b)) {
				return b0;
			}
		}
		throw new RuntimeException("Couldn't find " + b + " in " + B);
	}

	static <ObjA, ObjB, ObjC> Triple<ObjA, ObjB, ObjC> find1(ObjA b,
			List<Triple<ObjA, ObjB, ObjC>> B) {
		for (Triple<ObjA, ObjB, ObjC> b0 : B) {
			if (b0.first.equals(b)) {
				return b0;
			}
		}
		throw new RuntimeException("Couldn't find " + b + " in " + B);
	}

	static <ObjA, ObjB, ObjC> Triple<ObjA, ObjB, ObjC> find3(ObjC b,
			List<Triple<ObjA, ObjB, ObjC>> B) {
		for (Triple<ObjA, ObjB, ObjC> b0 : B) {
			if (b0.third.equals(b)) {
				return b0;
			}
		}
		throw new RuntimeException("Couldn't find " + b + " in " + B);
	}

	static <ObjC, ArrowC, ObjD, ArrowD> Arr<ObjC, ArrowC> findEquiv(ObjC c,
			FinFunctor<ObjC, ArrowC, ObjD, ArrowD> f, Arr<ObjD,ArrowD> e)
			throws FQLException {
		FinCat<ObjC, ArrowC> C = f.srcCat;
		FinCat<ObjD, ArrowD> D = f.dstCat;

		for (Arr<ObjC, ArrowC> peqc : C.arrows) {
			if (!peqc.src.equals(c)) {
				continue;
			}
			if (f.applyA(peqc).equals(e)) {
				return peqc;
			}
		}
		throw new FQLException("Could not find path mapping to " + e
				+ " under " + f);
		// return null;
	}

	public static 
	<ObjC, ArrowC, ObjD, ArrowD, X> Inst<ObjD, ArrowD, ObjC, X>
	sigma(
			FinFunctor<ObjC, ArrowC, ObjD, ArrowD> F, Inst<ObjC, ArrowC, ObjC, X> inst)
			throws FQLException {
		FinCat<ObjD, ArrowD> D = F.dstCat;
		FinCat<ObjC, ArrowC> C = F.srcCat;

		Map<ObjD, Set<Value<ObjC,X>>> ret1 = new HashMap<>();
		Map<Arr<ObjD, ArrowD>, Map<Value<ObjC,X>, Value<ObjC,X>>> ret2 = new HashMap<>();

		// Map<Node, List<String>> tags = new HashMap<Node, List<String>>();

		for (ObjD d : D.objects) {
			List<Set<Pair<Value<ObjC,X>, Value<ObjC,X>>>> tn = new LinkedList<>();
			List<Pair<ObjC, ObjC>> tj = new LinkedList<>();
			for (ObjC c : C.objects) {
				if (F.applyO(c).equals(d)) {
					tn.add(refl(inst.applyO(c)));
					tj.add(new Pair<>(c, c));
				}
			}
			ret1.put(d, derefl(disjointunion(tn, tj)));
			// tags.put(d, tj);
			// System.out.println("on " + d + " tags " + tj.toString());
		}

		for (Arr<ObjD, ArrowD> e : D.arrows) {
			ObjD d = e.src;
			// //Node d0 = e.target;
			List<Set<Pair<Value<ObjC, X>, Value<ObjC, X>>>> tn = new LinkedList<>();
			List<Pair<ObjC, ObjC>> tx = new LinkedList<>();
			for (ObjC c : C.objects) {
				if (F.applyO(c).equals(d)) {

					Arr<ObjC,ArrowC> pc = findEquiv(c, F, e);
					// //System.out.println("path is " + pc);
					//
					// //System.out.println("in " + e + " c is " + c +
					// " mapped " + tags.get(c));
					// //System.out.println("in " + e + " d is " + F.nm.get +
					// " mapped " + tags.get(c));
					//
					// // System.out.println("zzzz " + pc.target);
					//
					// RA q = compose(pc);
					tn.add(graph2(inst.applyA(pc)));
					tx.add(new Pair<>(c, pc.dst));
					// // tx.add(new
					// Integer(tags.get(d).indexOf(c.string)).toString());
					// // System.out.println("tags get d is " + tags.get(d) +
					// " d is " + d + " c is " + c + " indexof " +
					// tags.get(d).indexOf(c) + " contains " +
					// tags.get(d).contains(c));
					// // List<String> s = tags.get(pc.source);
					// // List<String> t = tags.get(pc.target);
					// // System.out.println("in " + e + " src " + pc.source +
					// " srctags " + s + " dst " + pc.target + " dsttags " + t);
					//
					// // tx.add();
					// // tx.add(tags.get(pc.target));
				}
			}
			ret2.put(e, degraph2(disjointunion(tn, tx)));
		}
		return new Inst<>(ret1, ret2, D);
	}

	private static <Y,X> Set<Pair<Value<Y,X>, Value<Y,X>>> disjointunion(
			List<Set<Pair<Value<Y,X>, Value<Y,X>>>> tn, List<Pair<Y,Y>> tags) {
		Set<Pair<Value<Y,X>, Value<Y,X>>> ret = new HashSet<>();
		for (int i = 0; i < tn.size(); i++) {
			Set<Pair<Value<Y,X>, Value<Y,X>>> table = tn.get(i);
			Pair<Y, Y> tag = tags.get(i);
			for (Pair<Value<Y,X>, Value<Y,X>> p : table) {
				ret.add(new Pair<>(new Value<Y,X>(tag.first, p.first), new Value<Y,X>(tag.second, p.second)));
			}
		}
		return ret;
	}

}
