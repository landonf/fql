package fql;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class FinFunctor<ObjA, ArrowA, ObjB, ArrowB> 
// implements Functor<ObjA, ArrowA, ObjB, ArrowB> 
{
	
	public ObjB applyO(ObjA a) {
		return objMapping.get(a);
	}
	public Arr<ObjB,ArrowB> applyA(Arr<ObjA, ArrowA> a) {
		return arrowMapping.get(a);
	}
	
	Map<ObjA, ObjB> objMapping;
	Map<Arr<ObjA,ArrowA>, Arr<ObjB,ArrowB>> arrowMapping;
	FinCat<ObjA, ArrowA> srcCat;
	FinCat<ObjB, ArrowB> dstCat;
	
	public FinFunctor(Map<ObjA,ObjB> objMapping, 
			Map<Arr<ObjA,ArrowA>,Arr<ObjB,ArrowB>> arrowMapping,
			FinCat<ObjA, ArrowA> srcCat,
			FinCat<ObjB, ArrowB> dstCat) {
		this.objMapping = objMapping;
		this.arrowMapping = arrowMapping;
		this.srcCat = srcCat; 
		this.dstCat = dstCat;
		validate();
	}
	
	
	
	static <Obj,Arrow> FinFunctor<Obj,Arrow,Obj,Arrow> singleton(FinCat<Obj,Arrow> D, Obj d, Arr<Obj,Arrow> i) {
		FinCat<Obj, Arrow> C = new FinCat<>(d,i);
		Map<Obj, Obj> objMapping = new HashMap<>();
		Map<Arr<Obj,Arrow>,Arr<Obj,Arrow>> arrowMapping = new HashMap<>();
		objMapping.put(d, d);
		arrowMapping.put(i, i);
		
		return new FinFunctor<>(objMapping, arrowMapping, C, D);
	}
	
	public void validate() {
		for (ObjA o : srcCat.objects) {
			if (!objMapping.containsKey(o)) {
				throw new RuntimeException("Functor does not map " + o + this);
			}
			if (!dstCat.objects.contains(objMapping.get(o))) {
				if (!dstCat.arrows.contains(arrowMapping.get(o))) {
					throw new RuntimeException("Functor maps to bad object " + o + arrowMapping.get(o) + this);
				}

			}
		}
		for (Arr<ObjA,ArrowA> a : srcCat.arrows) {
			if (!arrowMapping.containsKey(a)) {
				throw new RuntimeException("Functor does not map " + a + this);
			}
			if (!dstCat.arrows.contains(arrowMapping.get(a))) {
 				throw new RuntimeException("Functor maps to bad arrow " + a + "\n\n" + this + "\n\n dstCat " + dstCat);
			}
			for (Arr<ObjA,ArrowA> b : srcCat.arrows) {
				Arr<ObjA,ArrowA> c = srcCat.compose(a, b);
				if (c == null) {
					continue;
				}
				Arr<ObjB,ArrowB> a0 = arrowMapping.get(a);
				Arr<ObjB,ArrowB> b0 = arrowMapping.get(b);
				Arr<ObjB,ArrowB> c0 = arrowMapping.get(c);
				if (!c0.equals(dstCat.compose(a0, b0))) {
					throw new RuntimeException("Func does not preserve " + a + b + c + a0 + b0 + c0 + dstCat.compose(a0, b0) + srcCat + dstCat);
				}
			}	
		}
	}


	@Override
	public String toString() {
		return "FinFunctor [\nobjMapping=\n" + objMapping + "\n\narrowMapping=\n"
				+ arrowMapping + "\n]";
	}

	@Override
	public boolean equals(Object obj) {
		throw new RuntimeException("Cannot equate functors");
	}

	static int nameIdx = 0;

	public Triple<Mapping, Triple<Signature, Pair<Map<ObjA, String>, Map<String, ObjA>>, Pair<Map<Arr<ObjA, ArrowA>, String>, Map<String, Arr<ObjA, ArrowA>>>>, Triple<Signature, Pair<Map<ObjB, String>, Map<String, ObjB>>, Pair<Map<Arr<ObjB, ArrowB>, String>, Map<String, Arr<ObjB, ArrowB>>>>>
	toMapping(String n, String n1, String n2) throws FQLException {
		Triple<Signature, Pair<Map<ObjA, String>, Map<String, ObjA>>, Pair<Map<Arr<ObjA, ArrowA>, String>, Map<String, Arr<ObjA, ArrowA>>>> src = srcCat.toSig(n1);
		Triple<Signature, Pair<Map<ObjB, String>, Map<String, ObjB>>, Pair<Map<Arr<ObjB, ArrowB>, String>, Map<String, Arr<ObjB, ArrowB>>>> dst = dstCat.toSig(n2);
		
		Signature srcSig = src.first;
		Signature dstSig = dst.first;
		
		Map<Arr<ObjA, ArrowA>, String> srcM = src.third.first;
		Map<ObjA, String> srcM2 = src.second.first;
		Map<Arr<ObjB, ArrowB>, String> dstM = dst.third.first;
		Map<ObjB, String> dstM2 = dst.second.first;
		
//		System.out.println("&&&&&&&&&&&&&&&&&&&&&&&");
//		
//		System.out.println(srcSig);
//		System.out.println(dstSig);
//		System.out.println(srcM2);	
//		srcCat.validate();
//		System.out.println(srcCat.arrows);
//		System.out.println("&&&&&&&&&&&&&&&&&&&&&&&");

		List<Pair<String, String>> nm = new LinkedList<>();
		List<Pair<String, List<String>>> em = new LinkedList<>();
		for (Arr<ObjA, ArrowA> a : srcCat.arrows) {
			//System.out.println("%%%%%%%processing " + a);
			if (srcCat.isId(a)) {
				ObjA o = a.src;
				ObjB u = objMapping.get(o);
//				System.out.println("%%%%%%% o is " + o);
//				System.out.println("%%%%%%% u is " + u);
//				System.out.println("%%%%%%% get o " + srcM2.get(o));
//				System.out.println("%%%%%%% get u " + dstM2.get(u));


				nm.add(new Pair<>(srcM2.get(o), dstM2.get(u)));
			} else {
				Arr<ObjB, ArrowB> u = arrowMapping.get(a);
				List<String> t = new LinkedList<String>();
				t.add(dstM2.get(u.src));
				if (!dstCat.isId(u)) {
					t.add(dstM.get(u));
				}
				em.add(new Pair<>(srcM.get(a), t));
//				System.out.println("%%%%%%% t is " + t);
//				System.out.println("%%%%%%% u is " + u);
//				System.out.println("%%%%%%% srcM.get(a) is " + srcM.get(a));
//				System.out.println("%%%%%%% get u " + dstM.get(u));

			}
		}
		
		System.out.flush();
		
//		System.out.println("1------------");
//		System.out.println("2------------");
//		System.out.println("3------------");
//		System.out.println(srcCat);
//		System.out.println(dstCat);
//		System.out.println(nm);
//		System.out.println(em);
//		System.out.println("------------");
		
		Mapping m = new Mapping(n + "_m" + nameIdx++, srcSig, dstSig, nm, em);
		return new Triple<>(m, src, dst);
		
	}
	
	//F : c1 -> T
	//G : c2 -> T
	//h : c1 -> c2
	//G' : c1 -> t := h ; G  

	static <ObjC1, ArrowC1, ObjC2, ArrowC2, ObjT, ArrowT> 
	FinFunctor<ObjC1, ArrowC1, ObjT, ArrowT> compose(FinFunctor<ObjC1, ArrowC1, ObjC2, ArrowC2> h, FinFunctor<ObjC2, ArrowC2, ObjT, ArrowT> G) {
		
		Map<ObjC1, ObjT> ret1 = new HashMap<>();
		Map<Arr<ObjC1,ArrowC1>, Arr<ObjT,ArrowT>> ret2 = new HashMap<>();
		
		for (ObjC1 c1 : h.srcCat.objects) {
			ret1.put(c1, G.applyO(h.applyO(c1)));
		}
		
		for (Arr<ObjC1,ArrowC1> c1 : h.srcCat.arrows) {
			ret2.put(c1, G.applyA(h.applyA(c1)));
		}
		
		return new FinFunctor<>(ret1,ret2,h.srcCat,G.dstCat);
	}
	
	static <O1, A1, O2, A2, O3, A3, O4, A4> 
	FinFunctor<O1,A1,O4,A4> compose(FinFunctor<O1, A1, O2, A2> h, FinFunctor<O2, A2, O3, A3> G, FinFunctor<O3,A3,O4,A4> H) {
	
		return compose(compose(h, G), H);
	}

}








