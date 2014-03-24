package fql.sql;

import java.util.Map;
import java.util.Set;

import fql.FQLException;
import fql.Pair;
import fql.cat.Inst;
import fql.decl.Instance;
import fql.decl.Signature;
import fql.decl.Transform;

public class PSMIso extends PSM {
	
	public boolean lToR;
	public String l, r;
	public Signature sig;
	public String pre;

	public PSMIso(boolean lToR, String pre, String l, String r, Signature sig) {
		super();
		this.lToR = lToR;
		this.l = l;
		this.r = r;
		this.sig = sig;
		this.pre = pre;
	}

	@Override
	public void exec(PSMInterp interp,
			Map<String, Set<Map<Object, Object>>> state) {
		try {
			Instance li = new Instance(sig, PSMGen.gather(l, sig, state));
			Instance ri = new Instance(sig, PSMGen.gather(r, sig, state));
			
			Pair<Transform, Transform> k = Inst.iso(li, ri);
			if (k == null) {
				throw new RuntimeException("Cannot find iso between " + l + " and " + r);
			}
			
			if (lToR) {
				PSMGen.shred(pre, k.first, state);
			} else {
				PSMGen.shred(pre, k.second, state);
			}
			
		} catch (FQLException fe) {
			fe.printStackTrace();
			throw new RuntimeException(fe.getMessage());
		}
	}

	@Override
	public String toPSM() {
		throw new RuntimeException("Cannot generate SQL for iso.");
	}
	
	@Override
	public String toString() {
		if (lToR) {
			return "iso1 " + l + " " + r;
		} else {
			return "iso2 " + l + " " + r;
		}
	}
	
	@Override
	public String isSql() {
		return pre;
	}


}
