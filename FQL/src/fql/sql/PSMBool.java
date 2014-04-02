package fql.sql;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fql.FQLException;
import fql.Pair;
import fql.cat.Arr;
import fql.decl.Attribute;
import fql.decl.InstExp;
import fql.decl.Instance;
import fql.decl.Node;
import fql.decl.Path;
import fql.decl.Signature;
import fql.decl.Transform;

public class PSMBool extends PSM {

	public PSMBool(boolean bool, String unit, String prop, Signature sig,
			String pre, InstExp.Const unitX,
			Map<Node, Map<Object, LinkedHashMap<Pair<Arr<Node, Path>, Attribute<Node>>, Object>>> m1, 
			Map<Node, Map<LinkedHashMap<Pair<Arr<Node, Path>, Attribute<Node>>, Object>, Object>> m2		
			) {
		super();
		this.unitX = unitX;
		this.bool = bool;
		this.unit = unit;
		this.prop = prop;
		this.sig = sig;
		this.pre = pre;
		this.m1 = m1;
		this.m2 = m2;
	}

	public boolean bool;
	public String unit, prop;
	Signature sig;
	public String pre;
	Map<Node, Map<Object, LinkedHashMap<Pair<Arr<Node, Path>, Attribute<Node>>, Object>>> m1; 
	Map<Node, Map<LinkedHashMap<Pair<Arr<Node, Path>, Attribute<Node>>, Object>, Object>> m2;
	InstExp.Const unitX;
	
	@Override
	public String isSql() {
		return pre;
	}

	@Override
	public void exec(PSMInterp interp,
			Map<String, Set<Map<Object, Object>>> state) {
		try {	
			
			Signature sig0 = new Signature(sig.nodes, sig.edges, new LinkedList<Attribute<Node>>(), sig.eqs);
			
			Instance unitI = new Instance(sig, PSMGen.gather(unit, sig, state));
			Instance propI = new Instance(sig, PSMGen.gather(prop, sig, state));

			//Instance unitX0 = new Instance(sig, unitX.data);
			
			Map<Node, Map<Object, Object>> subst_inv = new HashMap<>();
		//	Map<Node, Map<Object, Object>> subst = new HashMap<>();
			for (Node n : sig.nodes) {
				Map<Object, Object> m = new HashMap<>();
				Map<Object, Object> m2 = new HashMap<>();
				Set<Map<Object, Object>> g = state.get(unit + "_" + n + "_subst_inv");
				for (Map<Object, Object> j : g) {
					m.put(j.get("c0"), j.get("c1"));
			//		m2.put(j.get("c1"), j.get("c0"));
				}
				subst_inv.put(n, m);
			//	subst.put(n, m2);
			}
		//	System.out.println("subst_inv " + subst_inv);
		//	System.out.println("unitI " + unitI);
			
			List<Pair<String, List<Pair<Object, Object>>>> data = new LinkedList<>();
			for (Node n : sig.nodes) {				
				List<Pair<Object, Object>> set = new LinkedList<>();
				for (Pair<Object, Object> k : unitI.data.get(n.string)) {
					Object k0 = subst_inv.get(n).get(k.first);
		//			System.out.println("k0 " + k0);
					if (k0 == null) {
			//			System.out.println("k.first is " + k.first);
			//			System.out.println("subst_inv is " + subst_inv.get(n));
						throw new RuntimeException();
					}
					LinkedHashMap<Pair<Arr<Node, Path>, Attribute<Node>>, Object> v = m1.get(n).get(k0);
					
					Instance tofind = null;
					if (bool) {
						tofind = interp.prop1.get(prop).first.get(n).first;
					} else {
						tofind = new Instance(sig0);
					}
					Object found = interp.prop2.get(prop).second.get(n).second.get(tofind);
			//		System.out.println("found: " + found);
			//		System.out.println("v " + v);
				//	System.out.println("prop4 " + interp.prop4.get(prop).get(n));
					Object r = interp.prop4.get(prop).get(n).get(new Pair<>(found, v));
					set.add(new Pair<>(k.first, r));
				}
				data.add(new Pair<>(n.string, set));
			}
			
			Transform ret = new Transform(unitI, propI, data);
			PSMGen.shred(pre, ret, state);
		} catch (FQLException fe) {
			fe.printStackTrace();
			throw new RuntimeException(fe.getMessage());
		}
	}

	@Override
	public String toPSM() {
		return pre;
	}
	
	@Override
	public String toString() {
		return pre + " := " + prop + "." + bool + " " + unit;
	}

}
