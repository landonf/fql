package fql;


public class CatUtils {
	
//	public static <Obj, Arrow> boolean isomorphic(FinCat<Obj, Arrow> a,
//			FinCat<Obj, Arrow> b) {
//		return isomorphisms(a, b).size() > 0;
//	}
//
//	public static <Obj, Arrow> Set<Pair<FinFunctor<Obj, Arrow, Obj, Arrow>, FinFunctor<Obj, Arrow, Obj, Arrow>>> isomorphisms(
//			FinCat<Obj, Arrow> A, FinCat<Obj, Arrow> B) {
//		Set<Pair<FinFunctor<Obj, Arrow, Obj, Arrow>, FinFunctor<Obj, Arrow, Obj, Arrow>>> ret = new HashSet<Pair<FinFunctor<Obj, Arrow, Obj, Arrow>, FinFunctor<Obj, Arrow, Obj, Arrow>>>();
//		for (FinFunctor<Obj, Arrow, Obj, Arrow> f : morphisms(A, B)) {
//			for (FinFunctor<Obj, Arrow, Obj, Arrow> g : morphisms(B, A)) {
//				if (isomorphic(A, B, f, g)) {
//					ret.add(new Pair<FinFunctor<Obj, Arrow, Obj, Arrow>, FinFunctor<Obj, Arrow, Obj, Arrow>>(
//							f, g));
//				}
//			}
//		}
//		return ret;
//	}
//	
//	//Knuth-bendix
//	//Co re
//	//Computational category theory
//
//	public static <ObjA, ArrowA, ObjB, ArrowB> Set<FinFunctor<ObjA, ArrowA, ObjB, ArrowB>> morphisms(
//			FinCat<ObjA, ArrowA> a, FinCat<ObjB, ArrowB> b) {
//		return null;
//	}
//
//	public static <ObjA, ArrowA, ObjB, ArrowB> Set<FinTrans<ObjA, ArrowA, ObjB, ArrowB>> morphisms(
//			FinFunctor<ObjA, ArrowA, ObjB, ArrowB> a, FinFunctor<ObjA, ArrowA, ObjB, ArrowB> b) {
//		return null;
//	}
//	
//	
//	
//	public static <ObjA, ArrowA, ObjB, ArrowB> boolean isomorphic(
//			FinCat<ObjA, ArrowA> a, FinCat<ObjB, ArrowB> b,
//			FinFunctor<ObjA, ArrowA, ObjB, ArrowB> f0,
//			FinFunctor<ObjA, ArrowA, ObjB, ArrowB> g0) {
//		for (FinTrans<ObjA, ArrowA, ObjB, ArrowB> n : morphisms(f0, g0)) {
//			//for every obj in C, n is isomorphism in D, the ok
//		}
//		return false;
//	}

}
