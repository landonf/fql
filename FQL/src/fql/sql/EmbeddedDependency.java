package fql.sql;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import fql.Pair;
import fql.Triple;
import fql.decl.Attribute;
import fql.decl.Edge;
import fql.decl.Node;
import fql.decl.Path;

/**
 * 
 * @author ryan
 *
 * Alternative representation for embedded dependencies.
 */
public class EmbeddedDependency {

	public List<String> forall, exists;
	public List<Triple<String, String, String>> where, tgd, not;
	public List<Pair<String, String>> egd;

	public EmbeddedDependency(List<String> forall, List<String> exists,
			List<Triple<String, String, String>> where,
			List<Triple<String, String, String>> tgd, /*
													 * List<Triple<String,
													 * String, String>> not,
													 */
			List<Pair<String, String>> egd) {

		Set<String> yyy = new HashSet<>();
		for (Triple<String, String, String> p : where) {
			yyy.add(p.second);
			yyy.add(p.third);
		}
		for (String s : yyy) {
			if (!forall.contains(s)) {
				throw new RuntimeException("not bound " + s);
			}
		}

		// System.out.println(" IN forall " + forall + " where " + where +
		// " exists " + exists + " s.t. " + egd + " " + tgd);

		this.forall = forall;

		Set<Set<String>> eqcs = new HashSet<>();
		for (String s : forall) {
			Set<String> S = new HashSet<>();
			S.add(s);
			eqcs.add(S);
		}
		for (String s : exists) {
			Set<String> S = new HashSet<>();
			S.add(s);
			eqcs.add(S);
		}

		for (;;) {
			Set<String> a = null, b = null;
			X: for (Pair<String, String> eq : egd) {
				for (Set<String> eqc1 : eqcs) {
					if (eqc1.contains(eq.first)) {
						for (Set<String> eqc2 : eqcs) {
							if (eqc2.contains(eq.second)) {
								if (eqc1.equals(eqc2)) {
									continue;
								}
								a = eqc1;
								b = eqc2;
								break X;
							}
						}
					}
				}
			}
			if (a != null) {
				eqcs.remove(a);
				eqcs.remove(b);
				Set<String> eqc = new HashSet<>();
				eqc.addAll(a);
				eqc.addAll(b);
				eqcs.add(eqc);
			} else {
				break;
			}
		}

		this.exists = new LinkedList<>(exists);
		this.tgd = new LinkedList<>(tgd);
		this.egd = new LinkedList<>(egd);

		// for each target var, if equiv to something else, substitute, remove
		// target var from exists and eqs
		for (String e : exists) {
			for (String a : forall) {
				if (equiv(eqcs, e, a)) {
					this.exists.remove(e);
					this.tgd = subst1(this.tgd, e, a);
					this.egd = subst2(this.egd, e, a);
				}
			}
			for (String a : exists) {
				if (e.equals(a)) {
					continue;
				}
				if (!this.exists.contains(e)) {
					continue;
				}
				if (!this.exists.contains(a)) {
					continue;
				}
				if (equiv(eqcs, e, a)) {
					this.exists.remove(e);
					this.tgd = subst1(this.tgd, e, a);
					this.egd = subst2(this.egd, e, a);
				}
			}
		}

		for (;;) {
			Pair<String, String> toRemove = null;
			a: for (Pair<String, String> p : this.egd) {
				if (p.first.equals(p.second)) {
					toRemove = p;
					break a;
				}
				for (Pair<String, String> q : this.egd) {
					if (p.equals(q)) {
						continue;
					}
					if (equiv(eqcs, p.first, q.first)
							&& equiv(eqcs, p.second, q.second)) {
						toRemove = p;
						break a;
					}
				}
			}
			if (toRemove != null) {
				this.egd.remove(toRemove);
			} else {
				break;
			}
		}

		this.where = new LinkedList<>(new HashSet<>(where));
		this.tgd = new LinkedList<>(new HashSet<>(this.tgd));
		this.egd = new LinkedList<>(new HashSet<>(this.egd));

		for (Triple<String, String, String> t : this.where) {
			if (this.tgd.contains(t)) {
				this.tgd.remove(t);
			}
		}

		// for each a = b, remove a' = b' if a = a' and b = b'

		// remoave x = x
		// remove a = b if have b = a
		// remove R(a,b) if occurs in source tableau

		// this.not = not;

		// check : vars in exists should not appear in eqs
		Set<String> eqvars = new HashSet<>();
		for (Pair<String, String> p : this.egd) {
			eqvars.add(p.first);
			eqvars.add(p.second);
		}
		for (String s : this.exists) {
			if (eqvars.contains(s)) {
				throw new RuntimeException();
			}
		}

		// System.out.println(" OUT forall " + this.forall + " where " +
		// this.where + " exists " + this.exists + " s.t. " + this.egd + " " +
		// this.tgd);
	}

	private List<Pair<String, String>> subst2(List<Pair<String, String>> l,
			String e, String a) {
		List<Pair<String, String>> x = new LinkedList<>();

		for (Pair<String, String> t : l) {
			x.add(new Pair<>(t.first.equals(e) ? a : t.first, t.second
					.equals(e) ? a : t.second));
		}

		return x;
	}

	private List<Triple<String, String, String>> subst1(
			List<Triple<String, String, String>> l, String e, String a) {
		List<Triple<String, String, String>> x = new LinkedList<>();

		for (Triple<String, String, String> t : l) {
			x.add(new Triple<>(t.first, t.second.equals(e) ? a : t.second,
					t.third.equals(e) ? a : t.third));
		}

		return x;
	}

	private boolean equiv(Set<Set<String>> eqcs, String e, String a) {
		for (Set<String> eqc : eqcs) {
			if (eqc.contains(e)) {
				return eqc.contains(a);
			}
		}
		throw new RuntimeException();
	}
	
	public static EmbeddedDependency eq3(String pre,
			Pair<Attribute<Node>, Pair<Edge, Attribute<Node>>> eq) {

		List<Triple<String, String, String>> where = new LinkedList<>();
	//	where.add(new Triple<>(lhs.source.string, "x", "x"));
	//	where.add(new Triple<>(rhs.source.string, "x", "x"));

		List<String> forall = new LinkedList<>();

		forall.add("a");
		forall.add("b");
		forall.add("c");
		forall.add("d");
		where.add(new Triple<>(pre + eq.first.name, "a", "b"));
		where.add(new Triple<>(pre + eq.second.first.name, "a", "c"));
		where.add(new Triple<>(pre + eq.second.second.name, "c", "d"));

		List<Pair<String, String>> egd = new LinkedList<>();
		egd.add(new Pair<>("b", "d"));
		
		List<String> exists = new LinkedList<>();
		List<Triple<String, String, String>> tgd = new LinkedList<>();
		
		EmbeddedDependency ed = new EmbeddedDependency(forall, exists, where,
				tgd, egd);
		
		return ed;
	}

	public static EmbeddedDependency eq2(String pre, Path lhs, Path rhs) {

		List<Triple<String, String, String>> where = new LinkedList<>();
	//	where.add(new Triple<>(lhs.source.string, "x", "x"));
	//	where.add(new Triple<>(rhs.source.string, "x", "x"));

		List<String> forall = new LinkedList<>();

		int i = 0;
		String last = "x";
		forall.add("x");
		for (String e : lhs.asList()) {
			where.add(new Triple<>(pre + e, last, "y"+i));
			last = "y"+i;
			forall.add(last);
			i++;
		}

		String lhsLast = last;
		
		last = "x";
		for (String e : rhs.asList()) {
			where.add(new Triple<>(pre + e, last, "z"+i));
			last = "z"+i;
			forall.add(last);
			i++;
		}

		List<Pair<String, String>> egd = new LinkedList<>();
		egd.add(new Pair<>(lhsLast, last));
		
		List<String> exists = new LinkedList<>();
		List<Triple<String, String, String>> tgd = new LinkedList<>();
		
		EmbeddedDependency ed = new EmbeddedDependency(forall, exists, where,
				tgd, egd);
		
		return ed;
	}
	/*
	public static EmbeddedDependency eq(String pre, Path lhs, Path rhs) {

		List<String> forall = matrix("u", lhs);
		List<String> exists = matrix("v", rhs);

		List<Triple<String, String, String>> where = telescope(pre, "x", "y",
				lhs, forall);
		forall.add("x");
		forall.add("y");

		List<Triple<String, String, String>> tgd = telescope(pre, "x", "y",
				rhs, exists);

		List<Pair<String, String>> egd = new LinkedList<>();
		// List<Triple<String, String, String>> not = new LinkedList<>();

		EmbeddedDependency ed = new EmbeddedDependency(forall, exists, where,
				tgd, egd);

		// System.out.println("lhs " + lhs);
		// System.out.println("rhs " + rhs);
		// System.out.println("ed " + ed);

		return ed;

	} */
/*
	private static List<Triple<String, String, String>> telescope(String pre,
			String x, String y, Path p, List<String> vars) {

		List<Triple<String, String, String>> ret = new LinkedList<>();

		ret.add(new Triple<>(pre + p.source.string, x, x));

		int i = 0;
		for (Edge e : p.path) {
			String v = vars.get(i++);
			ret.add(new Triple<>(pre + e.name, x, v));
			x = v;
		}
		ret.add(new Triple<>(pre + p.target.string, x, y));

		return ret;
	} */

	@SuppressWarnings("unused")
	private static List<String> matrix(String pre, Path p) {
		List<String> ret = new LinkedList<>();

		int i = 0;
		for (Edge e : p.path) {
			ret.add(pre + i++);
		}

		return ret;
	}

	public String toString() {
		String ret = "";

		ret += "forall ";
		int i = 0;
		for (String s : forall) {
			if (i++ > 0) {
				ret += " ";
			}
			ret += s;
		}

		ret += ", ";
		i = 0;
		for (Triple<String, String, String> s : where) {
			if (i++ > 0) {
				ret += " /\\ ";
			}
			ret += s.first + "(" + s.second + ", " + s.third + ")";
		}

		ret += " -> ";
		// if (exists.size() > 0) {
		ret += "exists ";
		i = 0;
		for (String s : exists) {
			if (i++ > 0) {
				ret += " ";
			}
			ret += s;
		}
		ret += ", ";
		// }

		i = 0;
		for (Pair<String, String> s : egd) {
			if (i++ > 0) {
				ret += " /\\ ";
			}
			ret += s.first + " = " + s.second;
		}

		for (Triple<String, String, String> s : tgd) {
			if (i++ > 0) {
				ret += " /\\ ";
			}
			ret += s.first + "(" + s.second + ", " + s.third + ")";
		}
		/*
		 * for (Triple<String, String, String> s : not) { if (i++ > 0) { ret +=
		 * " /\\ "; } ret += "not " + s.first + "(" + s.second + ", " + s.third
		 * + ")"; }
		 */

		return ret;
	}

	

}
