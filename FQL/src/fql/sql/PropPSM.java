package fql.sql;

import java.util.Map;
import java.util.Set;

import fql.FQLException;
import fql.Pair;
import fql.Triple;
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

	@Override
	public void exec(PSMInterp interp,
			Map<String, Set<Map<Object, Object>>> state) {
		try {
			IntRef ref = new IntRef(interp.guid);
		
			Pair<Pair<Map<Node, Triple<Instance, Map<Object, Path>, Map<Path, Object>>>, Map<Edge, Transform>>, Pair<Instance, Map<Node, Pair<Map<Object, Instance>, Map<Instance, Object>>>>> xxx = sig.omega(ref);

			interp.prop1.put(pre, xxx.first);
			interp.prop2.put(pre, xxx.second);
			
			PSMGen.shred(pre, xxx.second.first, state);
		
			interp.guid = ref.i;		
		} catch (FQLException fe) {
			fe.printStackTrace();
			throw new RuntimeException(fe.getMessage());
		}
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
