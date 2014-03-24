package fql.sql;

import java.util.Map;
import java.util.Set;

import fql.decl.Signature;

public class PSMUnChi extends PSM {

	Signature sig;
	String pre, a, b, prop, f;
	
	@Override
	public String toString() {
		return prop + ".chi " + f;
	}
	
	@Override
	public String isSql() {
		return pre;
	}

	@Override
	public void exec(PSMInterp interp,
			Map<String, Set<Map<Object, Object>>> state) {
		// TODO Auto-generated method stub
		throw new RuntimeException();
	}

	@Override
	public String toPSM() {
		throw new RuntimeException("Cannot generate SQL for unchi.");
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((a == null) ? 0 : a.hashCode());
		result = prime * result + ((b == null) ? 0 : b.hashCode());
		result = prime * result + ((f == null) ? 0 : f.hashCode());
		result = prime * result + ((pre == null) ? 0 : pre.hashCode());
		result = prime * result + ((prop == null) ? 0 : prop.hashCode());
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
		PSMChi other = (PSMChi) obj;
		if (a == null) {
			if (other.a != null)
				return false;
		} else if (!a.equals(other.a))
			return false;
		if (b == null) {
			if (other.b != null)
				return false;
		} else if (!b.equals(other.b))
			return false;
		if (f == null) {
			if (other.f != null)
				return false;
		} else if (!f.equals(other.f))
			return false;
		if (pre == null) {
			if (other.pre != null)
				return false;
		} else if (!pre.equals(other.pre))
			return false;
		if (prop == null) {
			if (other.prop != null)
				return false;
		} else if (!prop.equals(other.prop))
			return false;
		if (sig == null) {
			if (other.sig != null)
				return false;
		} else if (!sig.equals(other.sig))
			return false;
		return true;
	}

	public PSMUnChi(Signature sig, String pre, String a, String b, String prop,
			String f) {
		super();
		this.sig = sig;
		this.pre = pre;
		this.a = a;
		this.b = b;
		this.prop = prop;
		this.f = f;
	}

}
