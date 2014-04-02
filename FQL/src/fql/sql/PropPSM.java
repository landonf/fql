package fql.sql;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import fql.FQLException;
import fql.Fn;
import fql.Pair;
import fql.Triple;
import fql.cat.Arr;
import fql.cat.FinCat;
import fql.decl.Attribute;
import fql.decl.Edge;
import fql.decl.Instance;
import fql.decl.IntRef;
import fql.decl.Node;
import fql.decl.Path;
import fql.decl.Signature;
import fql.decl.Transform;

public class PropPSM extends PSM {
	
	String pre;
	Signature sig;

	@Override
	public String isSql() {
		return pre;
	}

	public void exec(PSMInterp interp,
			Map<String, Set<Map<Object, Object>>> state) {
		try {
			IntRef ref = new IntRef(interp.guid);
		
			Signature sigX = new Signature(sig.nodes, sig.edges, new LinkedList<Attribute<Node>>(), sig.eqs);
			
			//Pair<FinCat<Node, Path>, Fn<Path, Arr<Node, Path>>> ooo = sig.toCategory2();
		//	FinCat<Node, Path> cat = ooo.first;
			
			Pair<Pair<Map<Node, Triple<Instance, Map<Object, Path>, Map<Path, Object>>>, Map<Edge, Transform>>, Pair<Instance, Map<Node, Pair<Map<Object, Instance>, Map<Instance, Object>>>>> xxx = sigX.omega(ref);
			interp.prop1.put(pre, xxx.first);
			interp.prop2.put(pre, xxx.second);
			Instance old = xxx.second.first;
			//System.out.println("start obsvar");
			Map<Node, List<LinkedHashMap<Pair<Arr<Node, Path>, Attribute<Node>>, Object>>> m = Relationalizer.fast_obs(sig);
		//	Map<Node, Set<Set<Triple<Arr<Node, Path>, Attribute<Node>, Object>>>> me = Relationalizer.obsbar0(sig);
		//	Map<Node, Set<Set<Triple<Arr<Node, Path>, Attribute<Node>, Object>>>> m = Relationalizer.obsbar1(sig);
		   // System.out.println(kkk);
		   // System.out.println("m " + m);
		    //System.out.println("me " + me);
			Map<String, Set<Pair<Object, Object>>> data = new HashMap<>();			
			Map<Node, Map<Object, Pair<Object, LinkedHashMap<Pair<Arr<Node, Path>, Attribute<Node>>, Object>>>> m1 = new HashMap<>();
			Map<Node, Map<Pair<Object, LinkedHashMap<Pair<Arr<Node, Path>, Attribute<Node>>, Object>>, Object>> m2 = new HashMap<>();
	//		interp.prop3.put(pre, m1);
	//		interp.prop4.put(pre, m2);
			for (Node n : sig.nodes) {
				Map<Object, Pair<Object, LinkedHashMap<Pair<Arr<Node, Path>, Attribute<Node>>, Object>>> map1 = new HashMap<>();
				Map<Pair<Object, LinkedHashMap<Pair<Arr<Node, Path>, Attribute<Node>>, Object>>, Object> map2 = new HashMap<>();
				Set<Pair<Object, Object>> set = new HashSet<>();
				m1.put(n, map1);
				m2.put(n, map2);				
				for (Pair<Object, Object> i1 : old.data.get(n.string)) {
					for (LinkedHashMap<Pair<Arr<Node, Path>, Attribute<Node>>, Object> i2 : m.get(n)) {
							Object o = Integer.toString(++ref.i);
							map1.put(o, new Pair<>(i1.first, i2));
							map2.put(new Pair<>(i1.first, i2), o);
							set.add(new Pair<Object, Object>(o, o));
					}
				}
				data.put(n.string, set);
			}
			for (Attribute<Node> a : sig.attrs) {
				Set<Pair<Object, Object>> set = new HashSet<>();
				for (Pair<Object, Object> k : data.get(a.source.string)) {
					Pair<Object, LinkedHashMap<Pair<Arr<Node, Path>, Attribute<Node>>, Object>> kk = m1.get(a.source).get(k.first);
					//Object old_id = kk.first;
					LinkedHashMap<Pair<Arr<Node, Path>, Attribute<Node>>, Object> new_id = kk.second;
					set.add(new Pair<Object, Object>(k.first, new_id.get(new Pair<>(new Arr<>(new Path(sig, a.source), a.source, a.source), a))));
				}
				data.put(a.name, set);
			}
			for (Edge a : sig.edges) {
				Set<Pair<Object, Object>> set = new HashSet<>();
			//	System.out.println(data.get(a.source.string).size());
				for (Pair<Object, Object> k : data.get(a.source.string)) {
					Pair<Object, LinkedHashMap<Pair<Arr<Node, Path>, Attribute<Node>>, Object>> kk = m1.get(a.source).get(k.first);
					Object old_id = kk.first;
					Object old_id0 = lookup(old.data.get(a.name), old_id);
					LinkedHashMap<Pair<Arr<Node, Path>, Attribute<Node>>, Object> new_id = kk.second;
					LinkedHashMap<Pair<Arr<Node, Path>, Attribute<Node>>, Object> new_id0 = truncate(sig, new_id, a, m.get(a.target));
					Object o = m2.get(a.target).get(new Pair<>(old_id0, new_id0));
					set.add(new Pair<>(k.first, o));
				}
				data.put(a.name, set);
			}
			interp.prop3.put(pre, m1);
			interp.prop4.put(pre, m2);
			Instance ne = new Instance(sig, data);	
			//System.out.println("shreding");
			PSMGen.shred(pre, ne, state);
			//System.out.println("done");
			interp.guid = ref.i;		
		} catch (FQLException fe) {
			fe.printStackTrace();
			throw new RuntimeException(fe.getMessage());
		}
	} /*
	@Override
	public void exec(PSMInterp interp,
			Map<String, Set<Map<Object, Object>>> state) {
		try {
			IntRef ref = new IntRef(interp.guid);
		
			Signature sigX = new Signature(sig.nodes, sig.edges, new LinkedList<Attribute<Node>>(), sig.eqs);
			
			Pair<FinCat<Node, Path>, Fn<Path, Arr<Node, Path>>> ooo = sig.toCategory2();
			FinCat<Node, Path> cat = ooo.first;
			
			Pair<Pair<Map<Node, Triple<Instance, Map<Object, Path>, Map<Path, Object>>>, Map<Edge, Transform>>, Pair<Instance, Map<Node, Pair<Map<Object, Instance>, Map<Instance, Object>>>>> xxx = sigX.omega(ref);
			interp.prop1.put(pre, xxx.first);
			interp.prop2.put(pre, xxx.second);
			Instance old = xxx.second.first;
			//System.out.println("start obsvar");
			Map<Node, List<Map<Arr<Node, Path>, Map<Attribute<Node>, Object>>>> m = Relationalizer.fast_obs(sig);
			Map<Node, Set<Set<Triple<Arr<Node, Path>, Attribute<Node>, Object>>>> me = Relationalizer.obsbar0(sig);
		//	Map<Node, Set<Set<Triple<Arr<Node, Path>, Attribute<Node>, Object>>>> m = Relationalizer.obsbar1(sig);
		   // System.out.println(kkk);
		    System.out.println("me " + me);
		    System.out.println("mxxx" + m);
			Map<String, Set<Pair<Object, Object>>> data = new HashMap<>();			
			Map<Node, Map<Object, Pair<Object, Map<Arr<Node, Path>, Map<Attribute<Node>, Object>>>>> m1 = new HashMap<>();
			Map<Node, Map<Pair<Object, Map<Arr<Node, Path>, Map<Attribute<Node>, Object>>>, Object>> m2 = new HashMap<>();
	//		interp.prop3.put(pre, m1);
	//		interp.prop4.put(pre, m2);
			for (Node n : sig.nodes) {
				Map<Object, Pair<Object, Map<Arr<Node, Path>, Map<Attribute<Node>, Object>>>> map1 = new HashMap<>();
				Map<Pair<Object, Map<Arr<Node, Path>, Map<Attribute<Node>, Object>>>, Object> map2 = new HashMap<>();
				Set<Pair<Object, Object>> set = new HashSet<>();
				m1.put(n, map1);
				m2.put(n, map2);				
				for (Pair<Object, Object> i1 : old.data.get(n.string)) {
					for (Map<Arr<Node, Path>, Map<Attribute<Node>, Object>> i2 : m.get(n)) {
						Object o = Integer.toString(++ref.i);
						map1.put(o, new Pair<>(i1.first, i2));
						map2.put(new Pair<>(i1.first, i2), o);
						set.add(new Pair<Object, Object>(o, o));
					}
				}
				data.put(n.string, set);
			}
			for (Attribute<Node> a : sig.attrs) {
				Set<Pair<Object, Object>> set = new HashSet<>();
				for (Pair<Object, Object> k : data.get(a.source.string)) {
					Pair<Object, Map<Arr<Node, Path>, Map<Attribute<Node>, Object>>> kk = m1.get(a.source).get(k.first);
					//Object old_id = kk.first;
					Map<Arr<Node, Path>, Map<Attribute<Node>, Object>> new_id = kk.second;
					for (Entry<Arr<Node, Path>, Map<Attribute<Node>, Object>> t : new_id.entrySet()) {
						if (!cat.isId(t.getKey())) {
							continue;
						}
						set.add(new Pair<Object, Object>(k.first, t.getValue().get(a)));
//						if (t.second.equals(a)) {
	//						set.add(new Pair<Object, Object>(k.first, t.third));
		//					break;
			//			}
					}
				}
				data.put(a.name, set);
			}
			for (Edge a : sig.edges) {
				Set<Pair<Object, Object>> set = new HashSet<>();
				for (Pair<Object, Object> k : data.get(a.source.string)) {
					Pair<Object, Map<Arr<Node, Path>, Map<Attribute<Node>, Object>>> kk = m1.get(a.source).get(k.first);
					Object old_id = kk.first;
					Object old_id0 = lookup(old.data.get(a.name), old_id);
					Map<Arr<Node, Path>, Map<Attribute<Node>, Object>> new_id = kk.second;
					Map<Arr<Node, Path>, Map<Attribute<Node>, Object>> new_id0 = truncate(sig, new_id, a, m.get(a.target));
					Object o = m2.get(a.target).get(new Pair<>(old_id0, new_id0));
					set.add(new Pair<>(k.first, o));
				}
				data.put(a.name, set);
			}
			
			Instance ne = new Instance(sig, data);			
			PSMGen.shred(pre, ne, state);
			interp.guid = ref.i;		
		} catch (FQLException fe) {
			fe.printStackTrace();
			throw new RuntimeException(fe.getMessage());
		}
	} */

	private static <K,V> boolean containsAll(Map<K,V> bigger, Map<K,V> smaller) {
		for (Entry<K, V> k : smaller.entrySet()) {
			if (!bigger.get(k.getKey()).equals(k.getValue())) {
				//System.out.println("return false on " + bigger + " and " + smaller);
				return false;
			}
		}
		//System.out.println("returning true on " + bigger + "\n and \n" + smaller);
		return true;
	}
	public static LinkedHashMap<Pair<Arr<Node, Path>, Attribute<Node>>, Object> truncate(Signature sig,
			LinkedHashMap<Pair<Arr<Node, Path>, Attribute<Node>>, Object> new_id, Edge e, 
			List<LinkedHashMap<Pair<Arr<Node, Path>, Attribute<Node>>, Object>> list) throws FQLException {
		//System.out.println("start");
		Fn<Path, Arr<Node, Path>> fn = sig.toCategory2().second;
		FinCat<Node, Path> cat = sig.toCategory2().first;
		Arr<Node, Path> p = fn.of(new Path(sig, e));
		List<LinkedHashMap<Pair<Arr<Node, Path>, Attribute<Node>>, Object>> ret = new LinkedList<>();
	//	System.out.println("newid " + new_id);
		outer: for (LinkedHashMap<Pair<Arr<Node, Path>, Attribute<Node>>, Object> k : list) {
		//	System.out.println("Checking " + k);
			Map<Pair<Arr<Node, Path>, Attribute<Node>>, Object> cand = new HashMap<>();
			for (Entry<Pair<Arr<Node, Path>, Attribute<Node>>, Object> v : k.entrySet()) {		
				Pair<Arr<Node, Path>, Attribute<Node>> key = new Pair<>(cat.compose(p, v.getKey().first), v.getKey().second);
				Object value = cand.get(key);
				if (value == null) {
					cand.put(key, v.getValue());
				} else if (value != null) {
					if (value != v.getValue()) {
						continue outer;
					}
				}
			}
			//System.out.println("Candidate " + cand);
		//	if (new_id.equals(cand)) {
		//		ret.add(k);
		//	}
			if (containsAll(new_id, cand)) {
		//		System.out.println("end");
				return k;
			}
//				ret.add(k);
		//		System.out.println("added");
	//		}  else {
			//	System.out.println("Not added");
		//	}
		}
		//if (ret.size() != 1) {
			throw new RuntimeException("Cannot truncate:\n" + new_id + "\n\non edge\n" + e + "\n\nagainst " + list + "\n\npossible candidates\n" + ret);
	//	}
	//	return ret.get(0);
	}
	/*public static Map<Arr<Node, Path>, Map<Attribute<Node>, Object>> truncate(Signature sig,
			Map<Arr<Node, Path>, Map<Attribute<Node>, Object>> new_id, Edge e, 
			List<Map<Arr<Node, Path>, Map<Attribute<Node>, Object>>> list) throws FQLException {
		Fn<Path, Arr<Node, Path>> fn = sig.toCategory2().second;
		FinCat<Node, Path> cat = sig.toCategory2().first;
		Arr<Node, Path> p = fn.of(new Path(sig, e));
		Map<Arr<Node, Path>, Map<Attribute<Node>, Object>> ret = null;
		outer: for (Map<Arr<Node, Path>, Map<Attribute<Node>, Object>> k : list) {
			Map<Arr<Node, Path>, Map<Attribute<Node>, Object>> cand = new HashMap<>();
			for (Entry<Arr<Node, Path>, Map<Attribute<Node>, Object>> v : k.entrySet()) {
				Arr<Node, Path> c = cat.compose(p, v.getKey());
				cand.put(c, v.getValue());
			}
			if (containsAll(new_id, cand)) { 
		//		return k;
				if (!(ret == null)) {
					if (!ret.equals(k)) {
						throw new RuntimeException("not canonical, previous " + ret + " now " + k + " containing " + cand);
					}
				}
				ret = k;
			}
		}
		if (ret == null) {
			throw new RuntimeException("Cannot truncate: " + new_id + " on edge " + e + " against " + list);
		}
		return ret;
	}*/

	public static <X,Y> Y lookup(Collection<Pair<X,Y>> set, X x) {
		for (Pair<X, Y> k : set) {
			if (k.first.equals(x)) {
				return k.second;
			}
		}
		throw new RuntimeException("Cannot find " + x + " in " + set);
	}
	
	public PropPSM(String pre, Signature sig) {
		super();
		this.pre = pre;
		this.sig = sig;
	}

	@Override
	public String toPSM() {
		throw new RuntimeException("Cannot generate SQL for prop.");
	}
	
	@Override
	public String toString() {
		return "prop " + sig.toString();
	}

}
