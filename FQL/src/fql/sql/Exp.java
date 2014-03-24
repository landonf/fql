package fql.sql;

import java.util.Map;
import java.util.Set;

import fql.FQLException;
import fql.Pair;
import fql.Quad;
import fql.Triple;
import fql.decl.Edge;
import fql.decl.Instance;
import fql.decl.IntRef;
import fql.decl.Node;
import fql.decl.Path;
import fql.decl.Signature;
import fql.decl.Transform;

public class Exp extends PSM {

	public String pre, I, J;
	public Signature sig;
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((I == null) ? 0 : I.hashCode());
		result = prime * result + ((J == null) ? 0 : J.hashCode());
		result = prime * result + ((pre == null) ? 0 : pre.hashCode());
		result = prime * result + ((sig == null) ? 0 : sig.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Exp other = (Exp) obj;
		if (I == null) {
			if (other.I != null)
				return false;
		} else if (!I.equals(other.I))
			return false;
		if (J == null) {
			if (other.J != null)
				return false;
		} else if (!J.equals(other.J))
			return false;
		if (pre == null) {
			if (other.pre != null)
				return false;
		} else if (!pre.equals(other.pre))
			return false;
		if (sig == null) {
			if (other.sig != null)
				return false;
		} else if (!sig.equals(other.sig))
			return false;
		return true;
	}

	public Exp(String pre, String i, String j, Signature sig) {
		super();
		this.pre = pre;
		I = i;
		J = j;
		this.sig = sig;
	}

	@Override
	public void exec(PSMInterp interp,
			Map<String, Set<Map<Object, Object>>> state) {
		try {
			Instance Ix = new Instance(sig, PSMGen.gather(I, sig, state));
			Instance Jx = new Instance(sig, PSMGen.gather(J, sig, state));
			IntRef idx = new IntRef(interp.guid);
			Quad<Instance, Map<Node, Map<Object, Transform>>, Map<Node, Triple<Instance, Map<Object, Pair<Object, Object>>, Map<Pair<Object, Object>, Object>>>, Pair<Map<Node, Triple<Instance, Map<Object, Path>, Map<Path, Object>>>, Map<Edge, Transform>>> ret = Instance.exp(idx, Ix, Jx);
			interp.guid = idx.i;
			interp.exps.put(pre, ret);
			PSMGen.shred(pre, ret.first, state);
		} catch (FQLException fe) {
			fe.printStackTrace();
			throw new RuntimeException(fe.getLocalizedMessage());
		}
	}
	
	@Override
	public String toString() {
		return pre + " = (" + I + "^" + J + ")";
	}

	@Override
	public String toPSM() {
		throw new RuntimeException("Cannot generate SQL for exponentials.");
	}
	
	@Override
	public String isSql() {
		return pre;
	}


}
