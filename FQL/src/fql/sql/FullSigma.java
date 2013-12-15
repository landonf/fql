package fql.sql;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fql.FQLException;
import fql.Pair;
import fql.Triple;
import fql.cat.Denotation;
import fql.decl.Attribute;
import fql.decl.Edge;
import fql.decl.Instance;
import fql.decl.Mapping;
import fql.decl.Node;
import fql.decl.Signature;

/**
 * 
 * @author ryan
 *
 * PSM for full sigma.  Note that this cannot
 * actually be implemented by a real RDBMS.
 */
public class FullSigma extends PSM {
	
	Mapping f;
	String pre;
	String inst;

	public FullSigma(Mapping f, String pre, String inst) {
		this.f = f;
		this.pre = pre;
		this.inst = inst;
	}

	@Override
	public void exec(PSMInterp interp, Map<String, Set<Map<Object, Object>>> state) {
		Signature C = f.source;
		Signature D = f.target;
		List<Pair<String, List<Pair<Object, Object>>>> I0 = PSMGen.gather(inst, C, state);

		try {
			Instance I = new Instance(C, I0);
			interp.sigmas.put(pre, interp.guid);
			Triple<Instance, Map<Node, Map<Integer, Integer>>, Map<Node, Map<Integer, Integer>>> xxx = Denotation.fullSigmaWithAttrs(interp, f, I, null, null, null);
			interp.sigmas2.put(pre, interp.guid);
			Instance J = xxx.first;
			Map<Node, Map<Integer, Integer>> yyy = xxx.second;

			for (Node n : C.nodes) {
				state.put(pre + "_" + n.string + "_e", conv2(yyy.get(n)));				
			}

			for (Node n : D.nodes) {
				state.put(pre + "_" + n.string, conv(J.data.get(n.string)));
			}
			for (Edge n : D.edges) {
				state.put(pre + "_" + n.name, conv(J.data.get(n.name)));
			}
			for (Attribute<Node> n : D.attrs) {
				state.put(pre + "_" + n.name, conv(J.data.get(n.name)));
			}
			
		} catch (FQLException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		
	}

	private Set<Map<Object, Object>> conv2(Map<Integer, Integer> map) {
		Set<Map<Object, Object>> ret = new HashSet<>();
		
		for (Integer k : map.keySet()) {
			Integer v = map.get(k);
			Map<Object, Object> m = new HashMap<>();
			m.put("c0", k);
			m.put("c1", v);
			ret.add(m);
		}
		
		return ret;
	}

	static Set<Map<Object, Object>> conv(Set<Pair<Object, Object>> set) {
		Set<Map<Object, Object>> ret = new HashSet<>();
		for (Pair<Object, Object> p : set) {
			Map<Object, Object> m = new HashMap<>();
			m.put("c0", p.first);
			m.put("c1", p.second);
			ret.add(m);
		}
		return ret;
	}

	@Override
	public String toPSM() {
		throw new RuntimeException("Cannot generate SQL for full sigma");
	}

}
