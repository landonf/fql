package fql;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * 
 * @author ryan
 *
 * Implementation of comma categories.
 * 
 * @param <ObjA> Left
 * @param <ArrowA> Left
 * @param <ObjB> Right 
 * @param <ArrowB> Right
 * @param <ObjC> Middle 
 * @param <ArrowC> Middle
 */
public class CommaCat<ObjA, ArrowA, ObjB, ArrowB, ObjC, ArrowC> extends
		FinCat<Triple<ObjA, ObjB, Arr<ObjC,ArrowC>>, Pair<Arr<ObjA,ArrowA>, Arr<ObjB,ArrowB>>> {

	FinCat<ObjA, ArrowA> A;
	FinCat<ObjB, ArrowB> B;
	FinCat<ObjC, ArrowC> C;
	FinFunctor<ObjA, ArrowA, ObjC, ArrowC> F;
	FinFunctor<ObjB, ArrowB, ObjC, ArrowC> G;
	FinFunctor<Triple<ObjA, ObjB, Arr<ObjC, ArrowC>>, Pair<Arr<ObjA, ArrowA>, Arr<ObjB, ArrowB>>, ObjA, ArrowA> projA;
	FinFunctor<Triple<ObjA, ObjB, Arr<ObjC, ArrowC>>, Pair< Arr<ObjA, ArrowA>,  Arr<ObjB, ArrowB>>, ObjB, ArrowB> projB;

	public CommaCat(FinCat<ObjA, ArrowA> A, FinCat<ObjB, ArrowB> B,
			FinCat<ObjC, ArrowC> C, FinFunctor<ObjA, ArrowA, ObjC, ArrowC> F,
			FinFunctor<ObjB, ArrowB, ObjC, ArrowC> G) {
		this.A = A;
		this.B = B;
		this.C = C;
		this.F = F;
		this.G = G;
		
//		System.out.println("*****************************");
//		System.out.println("Creating comma category");
//		System.out.println("A is " + A);
//		System.out.println("B is " + B);
//		System.out.println("C is " + C);
//		System.out.println("F is " + F);
//		System.out.println("G is " + G);
//		
		objects = new LinkedList<Triple<ObjA, ObjB, Arr<ObjC,ArrowC>>>();
		for (ObjA objA : A.objects) {
			for (ObjB objB : B.objects) {
				ObjC s = F.objMapping.get(objA);
				ObjC t = G.objMapping.get(objB);
				for (Arr<ObjC,ArrowC> arrowC : C.arrows) {
					if (arrowC.src.equals(s) && arrowC.dst.equals(t)) {
						objects.add(new Triple<>(objA, objB, arrowC));
					}
				}
			}
		}
		
	//	System.out.println("objects are " + objects);

		//arrows = new LinkedList<Pair<Arr<ObjA,ArrowA>, Arr<ObjB,ArrowB>>>();
		for (Triple<ObjA, ObjB, Arr<ObjC, ArrowC>> obj1 : objects) {
			for (Triple<ObjA, ObjB, Arr<ObjC, ArrowC>> obj2 : objects) {
				ObjA a1 = obj1.first;
				ObjB b1 = obj1.second;
				Arr<ObjC, ArrowC> c1 = obj1.third;
				ObjA a2 = obj2.first;
				ObjB b2 = obj2.second;
				Arr<ObjC, ArrowC> c2 = obj2.third;
				for (Arr<ObjA,ArrowA> m : A.arrows) {
					if (!(m.src.equals(a1) && m.dst.equals(a2))) {
						continue;
					}
					for (Arr<ObjB,ArrowB> n : B.arrows) {
						if (!(n.src.equals(b1) && n.dst
								.equals(b2))) {
							continue;
						}
//						System.out.println("a1 " + a1);
//						System.out.println("b1 " + b1);
//						System.out.println("c1 " + c1);
//						System.out.println("a2 " + a2);
//						System.out.println("b2 " + b2);
//						System.out.println("c2 " + c2);
//						System.out.println("m " + m);
//						System.out.println("n " + n);
//						System.out.println("A" + C);
//						System.out.println("B" + c2);
//						System.out.println("C" + F.arrowMapping.get(m));
//						System.out.println("X" + G.arrowMapping.get(n));
					//	System.out.println("Y" + F.arrowMapping);
					//	if (F.arrowMapping.get(G.arrowMapping.get(n)) == null) {
						//	System.exit(-1);
						//}
						//System.out.println("D" + F.arrowMapping.get(G.arrowMapping.get(n)));
//						System.out.println("E" + G.arrowMapping.get(c1));
//						System.out.println("--------\n");
//						System.out.println("F" + F);
					//	ArrowC lhs = C.compose(c2, F.arrowMapping.get(m));
						
						//note composition is backwards - is ; not o
						Arr<ObjC, ArrowC> lhs = C.compose(F.arrowMapping.get(m), c2);
						assert (lhs != null);
					//	System.out.println();
					//	System.out.println();
						
					//	System.out.println("c2 " + c2);
				//		System.out.println("Fm " + F.arrowMapping.get(m));
					//	System.out.println("lhs " + lhs);
						//ArrowC rhs = C.compose(G.arrowMapping.get(n), c1);
						Arr<ObjC, ArrowC> rhs = C.compose(c1, G.arrowMapping.get(n));

						assert (rhs != null);
					//	System.out.println("c1 " + c1);
				//		System.out.println("Gn " + G.arrowMapping.get(n));
					//	System.out.println("rhs " + rhs);
						if (lhs.equals(rhs)) {
							Pair<Arr<ObjA,ArrowA>, Arr<ObjB,ArrowB>> arr = new Pair<>(m, n);
							arrows.add(new Arr<>(arr, obj1, obj2));
						}
					}
				}
			}
		}
	//	System.out.println("arrows are " + arrows);

		for (Triple<ObjA, ObjB, Arr<ObjC,ArrowC>> obj : objects) {
			identities.put(obj, new Arr<>(new Pair<>(A.identities.get(obj.first), B.identities.get(obj.second)),obj,obj));
		}

		for (Arr<Triple<ObjA, ObjB, Arr<ObjC, ArrowC>>, Pair<Arr<ObjA, ArrowA>, Arr<ObjB, ArrowB>>> arrow1 : arrows) {
			for (Arr<Triple<ObjA, ObjB, Arr<ObjC, ArrowC>>, Pair<Arr<ObjA, ArrowA>, Arr<ObjB, ArrowB>>> arrow2 : arrows) {
				if (arrow1.dst.equals(arrow2.src)) {
					Pair<Arr<ObjA, ArrowA>, Arr<ObjB, ArrowB>> c = new Pair<>(
							A.compose(arrow1.arr.first, arrow2.arr.first), B.compose(
									arrow1.arr.second, arrow2.arr.second));
					composition
							.put(new Pair<>(arrow1, arrow2), new Arr<>(c, arrow1.src, arrow2.dst));
				}
			};
		}
		
		projA();
		projB();

		if (DEBUG.VALIDATE) {
			validate();
		}
//		System.out.println("result is " + this);
//		System.out.println("***********************");
	}

	private void projA() {
		Map<Triple<ObjA, ObjB, Arr<ObjC,ArrowC>>, ObjA> objMapping = new HashMap<>();
		Map<Arr<Triple<ObjA, ObjB, Arr<ObjC, ArrowC>>,Pair<Arr<ObjA,ArrowA>, Arr<ObjB,ArrowB>>>, Arr<ObjA,ArrowA>> arrowMapping = new HashMap<>();
		
		for (Triple<ObjA, ObjB, Arr<ObjC, ArrowC>> obj : objects) {
			objMapping.put(obj, obj.first);
		}
		for (Arr<Triple<ObjA, ObjB, Arr<ObjC, ArrowC>>, Pair<Arr<ObjA, ArrowA>, Arr<ObjB, ArrowB>>> arr : arrows) {
			arrowMapping.put(arr, arr.arr.first);
		}

		projA = new FinFunctor<>(objMapping, arrowMapping, this, A);
	}

	private void projB() {
		Map<Triple<ObjA, ObjB,  Arr<ObjC, ArrowC>>, ObjB> objMapping = new HashMap<>();
		Map<Arr<Triple<ObjA, ObjB, Arr<ObjC, ArrowC>>,Pair<Arr<ObjA,ArrowA>, Arr<ObjB,ArrowB>>>, Arr<ObjB,ArrowB>> arrowMapping = new HashMap<>();
		
		for (Triple<ObjA, ObjB, Arr<ObjC, ArrowC>> obj : objects) {
			objMapping.put(obj, obj.second);
		}
		for (Arr<Triple<ObjA, ObjB, Arr<ObjC, ArrowC>>, Pair<Arr<ObjA, ArrowA>, Arr<ObjB, ArrowB>>> arr : arrows) {
			arrowMapping.put(arr, arr.arr.second);
		}

		projB = new FinFunctor<>(objMapping, arrowMapping, this, B);
	}


}
