package fql.cat;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import fql.FQLException;
import fql.Pair;
import fql.Triple;
import fql.decl.Environment;
import fql.decl.Instance;
import fql.decl.Mapping;
import fql.decl.Node;
import fql.decl.Path;
import fql.decl.Signature;
import fql.sql.PSM;
import fql.sql.PSMGen;
import fql.sql.PSMInterp;

/**
 * 
 * @author ryan
 *
 * Implementation of delta, sigma, pi and related machinery.
 * (Non-query generation; supports composition).
 */
public class FDM {

	/**
	 * N-ary product.
	 */
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

	@SuppressWarnings("unchecked")
	private static <Y,X> Set<Value<Y,X>[]> up(Set<Value<Y,X>> set) {
		Set<Value<Y,X>[]> ret = new HashSet<Value<Y,X>[]>();
		for (Value<Y,X> s : set) {
			ret.add(new Value[] { s });
		}
		return ret;
	}

	/**
	 * Limit as join all
	 */
	public static <ObjC, ObjD, ArrowC, ArrowD, Y, X> Set<Value<Y,X>[]> lim2
	(
			CommaCat<ObjD, ArrowD, ObjC, ArrowC, ObjD, ArrowD> B,
			Inst<Triple<ObjD, ObjC, Arr<ObjD, ArrowD>>, Pair<Arr<ObjD,ArrowD>,Arr<ObjC,ArrowC>>, Y, X> I) throws FQLException {
	
		if (B.objects.size() == 0) {
			return null;
		}
		
		Set<Value<Y, X>[]> x0 = productN(I.objM, B.objects);
		int m = B.objects.size();
		Triple<ObjD, ObjC, Arr<ObjD, ArrowD>>[] cnames = new Triple[m];
		int i = 0;
//		
		for (Triple<ObjD, ObjC, Arr<ObjD, ArrowD>> o : B.objects) {
			cnames[i++] = o; 
		}

		
		for (Arr<Triple<ObjD, ObjC, Arr<ObjD, ArrowD>>, Pair<Arr<ObjD, ArrowD>, Arr<ObjC, ArrowC>>> e : B.arrows) {
			x0 = product(x0, graph(I.applyA(e)));
			// System.out.println("after prod " + pn(x0));
			// System.out.println("selecting col " + m + " and " +
			// cnames.get(B.src(e)));
			x0 = select(x0, m, cnamelkp(cnames, e.src));
			// System.out.println("after select1 " + pn(x0));
			x0 = select(x0, m + 1, cnamelkp(cnames, e.dst));
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
			if (tuple[m] instanceof Value) {
			//	System.out.println("here");
				if (((Value)(tuple[m])).which != ((Value)(tuple[n])).which) {
					throw new RuntimeException();
				}
			}
			if (tuple[m].equals(tuple[n])) {
				ret.add(tuple);
			}
		}
		return ret;
	}

	/**
	 *  Generates a column of keys by forming value-level tuples.
	 */
	private static <Y,X> Set<Value<Y,X>[]> keygen(Set<Value<Y,X>[]> x0) {
		Set<Value<Y,X>[]> ret = new HashSet<>();
		for (Value<Y,X>[] x : x0) {
			@SuppressWarnings("unchecked")
			Value<Y,X>[] y =  new Value[x.length + 1];
			y[0] = new Value<>(x);
			for (int j = 1; j <= x.length; j++) {
				y[j] = x[j - 1];
			}
			ret.add(y);
		}
		return ret;
	}

	/**
	 * projects the first size columns from a relation.
	 */
	private static <Y,X> Set<Value<Y,X>[]> firstM(Set<Value<Y,X>[]> X, int size) {
		Set<Value<Y,X>[]> ret = new HashSet<>();
		for (Value<Y,X>[] x : X) {
			@SuppressWarnings("unchecked")
			Value<Y,X>[] g =  new Value[size];
			for (int i = 0; i < size; i++) {
				g[i] = x[i];
			}
			ret.add(g);
		}
		return ret;
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

	@SuppressWarnings("unchecked")
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
	
	public static <Obj,Arrow,X,Y> Inst<Obj, Arrow, X, Y> 
	pi2(FinFunctor<Obj,Arrow,X,Y> F, Inst<Obj,Arrow,X,Y> inst)
			throws FQLException {
		
//		Triple<Mapping, Triple<Signature, Pair<Map<Obj, String>, Map<String, Obj>>, Pair<Map<Arr<Obj, Arrow>, String>, Map<String, Arr<Obj, Arrow>>>>, Triple<Signature, Pair<Map<X, String>, Map<String, X>>, Pair<Map<Arr<X, Y>, String>, Map<String, Arr<X, Y>>>>>
//		tr = F.toMapping("F", "SRC", "DST");
//		
//		Instance inp = fromInst(inst);
//		
//		Mapping f1 = tr.first;
//		Triple<Signature, Pair<Map<Obj, String>, Map<String, Obj>>, Pair<Map<Arr<Obj, Arrow>, String>, Map<String, Arr<Obj, Arrow>>>> f2 = tr.second;
//		Triple<Signature, Pair<Map<X, String>, Map<String, X>>, Pair<Map<Arr<X, Y>, String>, Map<String, Arr<X, Y>>>> f3 = tr.third;
//		
//		Map<String, Set<Map<String, Object>>> state = shred(inp);
//		
//		List<PSM> l = PSMGen.pi(tr.first, "SRC", "DST");
//		Map<String, Set<Map<String, Object>>> res = PSMInterp.interpX(l, state);
//		
//		List<Pair<String, List<Pair<Object, Object>>>> gr = Environment.gather("DST", tr.third.first, state);
//		
//		Inst<Node, Path, Object, Object> v = new Instance(null, tr.third.first, gr).toFunctor2();
//	
		return null;
	}

	/**
	 * Pi 
	 * @param F the functor
	 * @param inst the instance
	 * @return Pi_F(inst)
	 * @throws FQLException
	 */
	public static <ObjC, ArrowC, ObjD, ArrowD, Y, X> Inst<ObjD, ArrowD, Y, X> 
	pi(
			FinFunctor<ObjC, ArrowC, ObjD, ArrowD> F, Inst<ObjC, ArrowC, Y, X> inst)
			throws FQLException {
		
//		System.out.println("problematic pi");
//		System.out.println("src cat " + F.srcCat);
//		System.out.println("src cat " + F.dstCat);
//		System.out.println(F);
//		System.out.println(inst);
//		
		FinCat<ObjD, ArrowD> D = F.dstCat;
		FinCat<ObjC, ArrowC> C = F.srcCat;

		Map<ObjD, Set<Value<Y, X>>> ret1 = new HashMap<>();
		Map<Arr<ObjD, ArrowD>, Map<Value<Y, X>, Value<Y, X>>> ret2 = new HashMap<>();

		Map<ObjD, CommaCat<ObjD, ArrowD, ObjC, ArrowC, ObjD, ArrowD>> nodecats = new HashMap<>();
		Map<ObjD, Set<Value<Y, X>[]>> nodetables = new HashMap<>();

		for (ObjD d0 : D.objects) {
			CommaCat<ObjD, ArrowD, ObjC, ArrowC, ObjD, ArrowD> B = doComma2(D,
					C, F, d0);
			

			//Inst<Triple<ObjD, ObjC, Arr<ObjD, ArrowD>>, Pair<Arr<ObjD, ArrowD>, Arr<ObjC, ArrowC>>, Y, X> s = delta(B.projB, inst);
			Set<Value<Y, X>[]> r = lim2(B, delta(B.projB, inst));

			if (r == null) {
				throw new RuntimeException();
//				Set<Value<Y,X>> xxx = new HashSet<>();
//				Value<Y,X> arr = (Value<Y, X>) new Value<>("*");
//				
//				Value<Y,X>[] arr0 = new Value[1];
//				arr0[0] = arr;
//				Set<Value<Y,X>[]> yyy = new HashSet<>();
//				yyy.add(arr0);
//				xxx.add(arr);
//				ret1.put(d0, xxx);
//				nodetables.put(d0, yyy);
				
			} else {
				ret1.put(d0, squish(r));
				nodetables.put(d0, r);
			}
			nodecats.put(d0, B);
			
		}

		for (Arr<ObjD, ArrowD> s : D.arrows) {
			
//			if (D.isId(s)) {
//				continue;
//			}

			//switched
			ObjD dA = s.src;
			CommaCat<ObjD, ArrowD, ObjC, ArrowC, ObjD, ArrowD> 
			BA = nodecats.get(dA);
			
			Set<Value<Y, X>[]> q1 = nodetables.get(dA);
			Triple<ObjD, ObjC, Arr<ObjD, ArrowD>> cnames1[] = new Triple[BA.objects.size()];
			int i = 0;
			for (Triple<ObjD, ObjC, Arr<ObjD, ArrowD>> o : BA.objects) {
				cnames1[i++] = o;
			}

			ObjD dB = s.dst;
			CommaCat<ObjD, ArrowD, ObjC, ArrowC, ObjD, ArrowD> 
			BB = nodecats.get(dB); 
			Set<Value<Y, X>[]> q2 = nodetables.get(dB); 
			
			Triple<ObjD, ObjC, Arr<ObjD, ArrowD>> cnames2[] = new Triple[BB.objects.size()];
			i = 0;
			for (Triple<ObjD, ObjC, Arr<ObjD, ArrowD>> o : BB.objects) {
				cnames2[i++] = o;
			}

			Set<Value<Y, X>[]> raw = product(q2, q1);
		//	System.out.println("raw is " + raw);
			Set<Value<Y, X>[]> rax = subset2(D, s, cnames2, cnames1, raw);
		//	System.out.println("rax is " + rax);
			Map<Value<Y, X>, Value<Y, X>> ray = project(rax, cnames2.length + 1, 0);

			ret2.put(s, ray);
			
		//	List<Pair<Pair<String, String>, Pair<String, String>>> where = subset(
			//		D, kkk.second.of(new Path(D0, s)), dst, q2cols, q1cols, q2,
			//		q1);
		}
		
		//System.out.println("ret1 " + ret1);
		//System.out.println("ret2 " + ret2);

		return new Inst<>(ret1, ret2, D);
	}


	/**
	 * The "subset portion" of pi
	 */
//	private static <ObjC, ObjD,Y, X> Set<Value<Y,X>[]> subset(
//			FinCat<ObjD, Y> d,
//			Arr<ObjD, Y> s,
//			Map<ObjC, Integer> cnames2,
//			Map<ObjC, Integer> cnames1, Set<Value<Y, X>[]> raw) {
//		for (ObjC  x : cnames2.keySet()) {
//			int i = cnames2.get(x);
//			int j = cnames1.get(x);
//			raw = select(raw, i + 1, j + 2 + cnames2.size());
//		}
//		return raw;
//	}
	
	private static <ObjC, ArrowC, ObjD, ArrowD, Y, X> Set<Value<Y,X>[]> subset2(
			FinCat<ObjD, ArrowD> cat,
			Arr<ObjD, ArrowD> e,
			Triple<ObjD, ObjC, Arr<ObjD, ArrowD>>[] q2cols,
			Triple<ObjD, ObjC, Arr<ObjD, ArrowD>>[] q1cols, Set<Value<Y, X>[]> raw) {
//	 System.out.println("trying subset " + print(q1cols) + " in " +
//	 print(q2cols));
	List<Pair<Pair<String, String>, Pair<String, String>>> ret = new LinkedList<>();
	//System.out.println("Arr" + e);
	//System.out.println("Cat" + cat);
	// turn e into arrow e', compute e' ; q2col, look for that
	
	a: for (int i = 0; i < q2cols.length; i++) {
		boolean b = false;
		for (int j = 0; j < q1cols.length; j++) {
			Triple<ObjD, ObjC, Arr<ObjD, ArrowD>> q2c = q2cols[i];
			Triple<ObjD, ObjC, Arr<ObjD, ArrowD>> q1c = q1cols[j];
//			System.out.println("^^^" + q1c);
//			System.out.println("^^^" + q2c);
//			System.out.println("compose " + cat.compose(e, q2c.third));
//			System.out.println("compose " + cat.compose(q2c.third, e));
//			System.out.println("compose " + cat.compose(e, q1c.third));
//			System.out.println("compose " + cat.compose(q1c.third, e));
////			// if (q1c.equals(q2c)) {
			if (q1c.third.equals(cat.compose(e, q2c.third)) && q2c.second.equals(q1c.second)) {
	//			System.out.println("hit on " + q2c.third);
//				Pair<Pair<String, String>, Pair<String, String>> retadd = new Pair<>(new Pair<>(
//						pre + "_" + q1name + "_limit", "c" + j),
//						new Pair<String, String>(pre + "_" + q2name + "_limit", "c" + i));
//			//	ret.add(retadd);
			//	System.out.println("raw is " + printSetOfArrays(raw));
				//1,2
		//		System.out.println(" colums " + (j + 1) + " and " + (i + 2 + q1cols.length));
				
				raw = select(raw, i + 1, j + 2 + q2cols.length);
				//System.out.println("raw now " + printSetOfArrays(raw));
				//System.out.println("added to where: " +  retadd);
				if (b) {
					throw new RuntimeException();
				}
				b = true;
				continue a;
			}
		}
		String xxx = "";
		for (Triple<ObjD, ObjC, Arr<ObjD, ArrowD>> yyy : q1cols) {
			xxx += ", " + yyy;
		}
		throw new RuntimeException("No col " + q2cols[i] + " in " + xxx
				);

	}
	//System.out.println("where is " + ret);
	return raw;
	}
	
	public static <Y,X> String  printSetOfArrays(Set<Value<Y,X>[]> x) {
		String ret = "";
		for (Object[] o : x) {
			for (int i = 0 ; i < o.length; i++) {
				ret += "c" + i + "=" + o[i] + " ";
			}
			ret += "\n";
		}
		return ret;
	}

	/**
	 * Projection on two columns
	 */
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

	/**
	 * Just takes the zero-th ID column of a relation
	 */
	private static <X> Set<X> squish(Set<X[]> r) {
		Set<X> ret = new HashSet<X>();
		for (X[] x : r) {
			ret.add(x[0]);
		}
		return ret;
	}

	/**
	 * Cartesian product of sets
	 */
	private static <Y,X> Set<Value<Y,X>[]> product(Set<Value<Y,X>[]> A, Set<Value<Y,X>[]> B) {
		Set<Value<Y,X>[]> ret = new HashSet<>();
		for (Value<Y,X>[] a : A) {
			for (Value<Y,X>[] b : B) {
				@SuppressWarnings("unchecked")
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

	/**
	 * Does the comma category construction for singleton categories for sigma for pi
	 */
	private static <ObjC, ArrowC, ObjD, ArrowD> CommaCat<ObjD, ArrowD, ObjC, ArrowC, ObjD, ArrowD> 
	doComma2(
			FinCat<ObjD, ArrowD> D, FinCat<ObjC, ArrowC> C,
			FinFunctor<ObjC, ArrowC, ObjD, ArrowD> F, ObjD d0) {

		FinFunctor<ObjD, ArrowD, ObjD, ArrowD> d = FinFunctor.singleton(D, d0,
				D.id(d0));
		CommaCat<ObjD, ArrowD, ObjC, ArrowC, ObjD, ArrowD> B = new CommaCat<ObjD, ArrowD, ObjC, ArrowC, ObjD, ArrowD>(
				d.srcCat, C, D, d, F);

		return B;
	}

	/**
	 * Fiber product of sets
	 */
	static <A, B, C> List<Triple<A, B, C>> fpsets(List<A> A, List<B> B,
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

	/**
	 * Computes pullbacks
	 */
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

	
	/**
	 * Differentiation, via sigma on terminal instances.
	 */
	public static <ObjX, ArrowX, ObjC, ArrowC> 
	Inst<ObjC, ArrowC, ObjX, ObjX> 
	degrothendieck(FinFunctor<ObjX, ArrowX, ObjC, ArrowC> F) throws FQLException {

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

	public static <Y,X> Map<Value<Y,X>, Value<Y,X>> degraph(Set<Pair<X, X>> set) {
		Map<Value<Y,X>, Value<Y,X>> ret = new HashMap<>(set.size());
		for (Pair<X, X> p : set) {
			ret.put(new Value<Y,X>(p.first), new Value<Y,X>(p.second));
		}
		return ret;
	}

	private static <Y,X> Map<Value<Y,X>, Value<Y,X>> degraph2(Set<Pair<Value<Y,X>, Value<Y,X>>> set) {
		Map<Value<Y,X>, Value<Y,X>> ret = new HashMap<>(set.size());
		for (Pair<Value<Y, X>, Value<Y, X>> p : set) {
			ret.put(p.first, p.second);
		}
		return ret;
	}

	/**
	 * Integration, of a natural transformation
	 */
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

	/**
	 * Integration for an instance.
	 */
	public static <Obj, Arrow, Y, X> 
	FinFunctor<Pair<Obj, Value<Y,X>>, Arr<Obj, Arrow>, Obj, Arrow> 
	grothendieck(
			Inst<Obj, Arrow, Y, X> sf) {

		FinCat<Obj, Arrow> C = sf.cat;
		Map<Obj, Set<Value<Y, X>>> objM = sf.objM;

		Set<Pair<Obj, Value<Y, X>>> objects = new HashSet<>();
		for (Obj o : C.objects) {
			for (Value<Y, X> s : objM.get(o)) {
				objects.add(new Pair<>(o, s));
			}
		}

		Set<Arr<Pair<Obj, Value<Y, X>>, Arr<Obj,Arrow>>> arrows = new HashSet<>();
		for (Pair<Obj, Value<Y, X>> o1 : objects) {
			for (Pair<Obj, Value<Y, X>> o2 : objects) {
				for (Arr<Obj, Arrow> a : C.hom(o1.first, o2.first)) {
					if (sf.applyA(a).get(o1.second).equals(o2.second)) {
						arrows.add(new Arr<>(a, o1, o2));
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
	}

	/**
	 * Delta
	 */
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

	/**
	 * Computes epislon, the co-unit part of the distributivity diagram for composition
	 */
	public static <ObjC, ArrowC, ObjD, ArrowD, Y, X> SetFunTrans<ObjC, ArrowC, Y, X>
	epsilon(
			FinFunctor<ObjC, ArrowC, ObjD, ArrowD> F, Inst<ObjC, ArrowC, Y, X> res,
			Inst<ObjC, ArrowC, Y, X> I) throws FQLException {

//		System.out.println("Computing epsilon");
//		System.out.println("F " + F);
//		System.out.println("res " + res);
//		System.out.println("I " + I);
		Map<ObjC, Map<Value<Y, X>, Value<Y, X>>> map = new HashMap<>();

		//TODO: this has same problem as pi?
		for (ObjC C : F.srcCat.objects) {
			CommaCat<ObjD, ArrowD, ObjC, ArrowC, ObjD, ArrowD> B = doComma2(
					F.dstCat, F.srcCat, F, F.applyO(C));

			Set<Value<Y, X>[]> r = lim2(B, delta(B.projB, I));
			if (r == null) {
				throw new RuntimeException();
			}
			int i = 0;
			boolean flag = true;
			//TODO add check if it ever finds two
			for (Triple<ObjD, ObjC, Arr<ObjD, ArrowD>> o : B.objects) {
				if (o.second.equals(C) && F.dstCat.isId(o.third)) {
					Map<Value<Y,X>, Value<Y,X>> xxx = project(r, 0, i + 1);
					map.put(C, xxx); 
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


	/**
	 * Computes the g arrow in the distributive diagram
	 */
	public static <ObjA, ArrowA, ObjB, ArrowB, Y, X> 
	FinFunctor<Pair<ObjB, Value<Y,X>>, Arr<ObjB,ArrowB>, Pair<ObjA, Value<Y,X>>, Arr<ObjA,ArrowA>> makeG(
			FinCat<Pair<ObjB, Value<Y,X>>, Arr<ObjB,ArrowB>> B,
			FinCat<Pair<ObjA, Value<Y,X>>, Arr<ObjA,ArrowA>> A,
			FinFunctor<ObjB, ArrowB, ObjA, ArrowA> f) {

		Map<Pair<ObjB, Value<Y,X>>, Pair<ObjA, Value<Y,X>>> objM = new HashMap<>();
		Map<Arr<Pair<ObjB, Value<Y,X>>,Arr<ObjB,ArrowB>>,  Arr<Pair<ObjA, Value<Y,X>>,Arr<ObjA,ArrowA>>> arrM = new HashMap<>();

		for (Pair<ObjB, Value<Y,X>> p : B.objects) {
			objM.put(p, new Pair<>(f.applyO(p.first), p.second));
		}
			
		for (Pair<ObjB, Value<Y,X>> p1 : B.objects) {
			for (Pair<ObjB, Value<Y,X>> p2 : B.objects) {
				Set<Arr<Pair<ObjB, Value<Y,X>>, Arr<ObjB, ArrowB>>> pp = B.hom(p1, p2);
				for (Arr<Pair<ObjB, Value<Y,X>>, Arr<ObjB, ArrowB>> r : pp) {
					Arr<ObjA, ArrowA> kkk = f.applyA(r.arr); //new Arr<>(r.arr, r.src.first, r.dst.first));
					
					Pair<ObjA, Value<Y,X>> jj = objM.get(p1);
					Pair<ObjA, Value<Y,X>> kk = objM.get(p2);
					
					Arr<Pair<ObjA, Value<Y,X>>, Arr<ObjA, ArrowA>> ll = new Arr<>(kkk, jj, kk);
					arrM.put(r, ll);
				}
			}
		}
		
	

		return new FinFunctor<Pair<ObjB, Value<Y,X>>, Arr<ObjB,ArrowB>, Pair<ObjA, Value<Y,X>>, Arr<ObjA,ArrowA>>(objM, arrM, B, A);
	}

	/**
	 * Finds paths mapping to a path, for sigma
	 */
	private static <ObjC, ArrowC, ObjD, ArrowD> Arr<ObjC, ArrowC> 
	findEquiv(ObjC c,
			FinFunctor<ObjC, ArrowC, ObjD, ArrowD> f, Arr<ObjD,ArrowD> e)
			throws FQLException {
		FinCat<ObjC, ArrowC> C = f.srcCat;
	
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
	}

	/**
	 * Sigma
	 */
	public static 
	<ObjC, ArrowC, ObjD, ArrowD, X> Inst<ObjD, ArrowD, ObjC, X>
	sigma(
			FinFunctor<ObjC, ArrowC, ObjD, ArrowD> F, Inst<ObjC, ArrowC, ObjC, X> inst)
			throws FQLException {
		FinCat<ObjD, ArrowD> D = F.dstCat;
		FinCat<ObjC, ArrowC> C = F.srcCat;

		Map<ObjD, Set<Value<ObjC,X>>> ret1 = new HashMap<>();
		Map<Arr<ObjD, ArrowD>, Map<Value<ObjC,X>, Value<ObjC,X>>> ret2 = new HashMap<>();

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
		}

		for (Arr<ObjD, ArrowD> e : D.arrows) {
			ObjD d = e.src;
			List<Set<Pair<Value<ObjC, X>, Value<ObjC, X>>>> tn = new LinkedList<>();
			List<Pair<ObjC, ObjC>> tx = new LinkedList<>();
			for (ObjC c : C.objects) {
				if (F.applyO(c).equals(d)) {

					Arr<ObjC,ArrowC> pc = findEquiv(c, F, e);
					tn.add(graph2(inst.applyA(pc)));
					tx.add(new Pair<>(c, pc.dst));
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
