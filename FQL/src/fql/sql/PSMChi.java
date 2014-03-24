package fql.sql;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fql.FQLException;
import fql.Pair;
import fql.decl.Edge;
import fql.decl.Instance;
import fql.decl.Node;
import fql.decl.Path;
import fql.decl.Signature;
import fql.decl.Transform;

public class PSMChi extends PSM {

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
		try {
			Instance I = new Instance(sig, PSMGen.gather(a, sig, state));
			Instance J = new Instance(sig, PSMGen.gather(b, sig, state));
			Instance P = new Instance(sig, PSMGen.gather(prop, sig, state));
			Transform t= new Transform(I, J, PSMGen.gather(f, sig, state));
			
			List<Pair<String, List<Pair<Object, Object>>>> data = new LinkedList<>();
			for (Node c : sig.nodes) {
				List<Pair<Object, Object>> l = new LinkedList<>();
				Instance Hc = interp.prop1.get(prop).first.get(c).first;
				for (Pair<Object, Object> x : J.data.get(c.string)) {
					
					List<Pair<String, List<Pair<Object, Object>>>> z = new LinkedList<>();
					for (Node d : sig.nodes) {
						List<Pair<Object, Object>> y = new LinkedList<>();
						for (Pair<Object, Object> ff : Hc.data.get(d.string)) {
							Path f = interp.prop1.get(prop).first.get(c).second.get(ff.first);//c->d
							Object xd = lookup(J.evaluate(f), x.first);
							y.add(new Pair<>(ff.first, xd));
						}
						z.add(new Pair<>(d.string, y));
					}
					Transform xx = new Transform(Hc, J, z);
					
					Map<String, Set<Pair<Object, Object>>> q = new HashMap<>();
					for (Node d : sig.nodes) {
						Set<Pair<Object, Object>> g = new HashSet<>();
						for (Pair<Object, Object> y : Hc.data.get(d.string)) {
							if (proj2(t.data.get(d.string)).contains(lookup(xx.data.get(d.string), y.first)))  {
								g.add(y);
							}
						}
						q.put(d.string, g);
					}
					for (Edge e : sig.edges) {
						Set<Pair<Object, Object>> set = new HashSet<>();
						for (Pair<Object, Object> j : Hc.data.get(e.name)) {
							if (proj1(Hc.data.get(e.source.string)).contains(j.first) && 
								proj2(Hc.data.get(e.target.string)).contains(j.second)) {
								set.add(j);
							}
						}
/*						for (Pair<Object, Object> j : q.get(e.source.string)) {
							System.out.println("j " + j);
							Instance u = interp.prop2.get(prop).second.get(e.source).first.get(j.first);
							System.out.println("map " + interp.prop2.get(prop).second.get(e.source).first);
							System.out.println("u " + u);
						//	System.out.println("trans " + rx.second.get(e));
							Instance v = interp.prop1.get(prop).second.get(e).preimage(u);
						//	System.out.println("v " + v); 
							Object o = interp.prop2.get(prop).second.get(e.target).second.get(v);
						//	System.out.println("mgetarget " + m.get(e.target).second);
						//	System.out.println("o " + o);
							set.add(new Pair<>(j.first, o));
						}			*/
						q.put(e.name, set);
					}
					//also do edges
					Instance pb = new Instance(sig, q);
					Object fnl = interp.prop2.get(prop).second.get(c).second.get(pb);
					l.add(new Pair<>(x.first, fnl));
				}
				data.add(new Pair<>(c.string, l));
			}
			
			Transform ret = new Transform(J, P, data );
			PSMGen.shred(pre, ret, state);
		} catch (FQLException fe) {
			fe.printStackTrace();
			throw new RuntimeException(fe.getLocalizedMessage());
		}
	}
	
	private static <X,Y> Set<X> proj1(Set<Pair<X,Y>> set) {
		Set<X> ret = new HashSet<>();
		for (Pair<X, Y> k : set) {
			ret.add(k.first);
		}
		return ret;
	}
	
	private static <X,Y> Set<Y> proj2(Set<Pair<X,Y>> set) {
		Set<Y> ret = new HashSet<>();
		for (Pair<X, Y> k : set) {
			ret.add(k.second);
		}
		return ret;
	}
	
	private static <X,Y> Y lookup(Set<Pair<X,Y>> set, X x) {
		for (Pair<X, Y> k : set) {
			if (k.first.equals(x)) {
				return k.second;
			}
		}
		throw new RuntimeException("cannot find " + x + " in " + set);
	}

	@Override
	public String toPSM() {
		throw new RuntimeException("Cannot generate SQL for chi.");
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

	public PSMChi(Signature sig, String pre, String a, String b, String prop,
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
