package fql.sql;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fql.FQLException;
import fql.Pair;
import fql.decl.Instance;
import fql.decl.Node;
import fql.decl.Signature;
import fql.decl.Transform;

public class PSMBool extends PSM {

	public PSMBool(boolean bool, String unit, String prop, Signature sig,
			String pre) {
		super();
		this.bool = bool;
		this.unit = unit;
		this.prop = prop;
		this.sig = sig;
		this.pre = pre;
	}

	public boolean bool;
	public String unit, prop;
	Signature sig;
	public String pre;
	
	@Override
	public String isSql() {
		return pre;
	}

	@Override
	public void exec(PSMInterp interp,
			Map<String, Set<Map<Object, Object>>> state) {
		try {	
			Instance unitI = new Instance(sig, PSMGen.gather(unit, sig, state));
			Instance propI = new Instance(sig, PSMGen.gather(prop, sig, state));
			
			List<Pair<String, List<Pair<Object, Object>>>> data = new LinkedList<>();
			for (Node n : sig.nodes) {				
				for (Pair<Object, Object> k : unitI.data.get(n.string)) {
					Instance tofind = null;
					if (bool) {
						tofind = interp.prop1.get(prop).first.get(n).first;
					} else {
						tofind = new Instance(sig);
					}
					Object found = interp.prop2.get(prop).second.get(n).second.get(tofind);
					List<Pair<Object, Object>> o = new LinkedList<>();
					o.add(new Pair<>(k.first, found));
					data.add(new Pair<>(n.string, o));
					break;
				}
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
