package fql;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class SemQuery<ObjA, ArrowA, ObjB, ArrowB, ObjC, ArrowC, ObjD, ArrowD> {

	FinFunctor<ObjA, ArrowA, ObjB, ArrowB> project;
	FinFunctor<ObjA, ArrowA, ObjC, ArrowC> join;
	FinFunctor<ObjC, ArrowC, ObjD, ArrowD> union;
	
	public SemQuery(FinFunctor<ObjA, ArrowA, ObjB, ArrowB> project,
			FinFunctor<ObjA, ArrowA, ObjC, ArrowC> join,
			FinFunctor<ObjC, ArrowC, ObjD, ArrowD> union) {
		super();
		this.project = project;
		this.join = join;
		this.union = union;
	}

//	public Query toQuery(Environment env, String name, Signature s, Signature t) throws FQLException {
//		Triple<Mapping, Triple<Signature, Pair<Map<ObjA, String>, Map<String, ObjA>>, Pair<Map<ArrowA, String>, Map<String, ArrowA>>>, Triple<Signature, Pair<Map<ObjB, String>, Map<String, ObjB>>, Pair<Map<ArrowB, String>, Map<String, ArrowB>>>> project0 = project.toMapping(name, name + "_temp1", name + "_temp2");
//		Triple<Mapping, Triple<Signature, Pair<Map<ObjA, String>, Map<String, ObjA>>, Pair<Map<ArrowA, String>, Map<String, ArrowA>>>, Triple<Signature, Pair<Map<ObjC, String>, Map<String, ObjC>>, Pair<Map<ArrowC, String>, Map<String, ArrowC>>>> join0 = join.toMapping(name + "_temp2",  name + "_temp2");
//		Triple<Mapping, Triple<Signature, Pair<Map<ObjC, String>, Map<String, ObjC>>, Pair<Map<ArrowC, String>, Map<String, ArrowC>>>, Triple<Signature, Pair<Map<ObjD, String>, Map<String, ObjD>>, Pair<Map<ArrowD, String>, Map<String, ArrowD>>>> union0 = union.toMapping( name + "_temp2",  name + "_temp2");
//	
//		// have f : B -> C
//		// want g : A -> C
//		// find i : A -> B
//		// g = i ; f 
//		
////		@SuppressWarnings("unchecked")
//		Map<String, ObjB> map = project0.third.second.second;
//		Map<String, ArrowB> map2 = project0.third.third.second;
////		@SuppressWarnings("unchecked")
////		FinCat<String, List<List<String>>> C = (FinCat<String, List<List<String>>>) project.dstCat;
////		
//		System.out.println("s is ");
//		System.out.println(s);
//		System.out.println("semantic s is ");
//		System.out.println(project0.first.target);
//		System.out.println("map is " + project0.third);
//
//		List<Pair<String, List<String>>> arrows = new LinkedList<>();
//		List<Pair<String, String>> objs = new LinkedList<>();
////		for (Edge b : s.edges) {
////			ArrowB k = map2.get(b.name);
////			List<List<String>> eqc = find(k, project.dstCat.arrows);
////			arrows.add(new Pair<>(b.name, eqc));				
////		}
////		for (Node b : s.nodes) {
////			ObjB k = map.get(b.string);
////			objs.add(new Pair<>(b.string, k));				
////		}
//		
//	//	Mapping m = new Mapping("tempX", s, project0.first.target, objs, arrows);
//		
//		
////		Mapping projectX = project0.first.changeTarget(project0.third,s);
//		
//		
//		return new Query(name, env, project0.first, join0.first, union0.first);
//	}

	@Override
	public String toString() {
		return "SemQuery [project=" + project + ", join=" + join + ", union="
				+ union + "]";
	}

	
	
	
}
