package fql.kb;

import java.util.LinkedList;
import java.util.List;

import fql.Pair;
import fql.Path;
import fql.Signature;

public abstract class Term {
	
	static int idx = 0;
	
	static String fresh() {
		return "_" + idx++;
	}

	static Pair<List<Pair<Term, Term>>, List<Pair<Term, Term>>> rule1(
			List<Pair<Term, Term>> eqs, List<Pair<Term, Term>> reds) {
		return null;
	}

	static Pair<List<Pair<Term, Term>>, List<Pair<Term, Term>>> rule2(
			List<Pair<Term, Term>> eqs, List<Pair<Term, Term>> reds) {
		return null;
	}
	
	static List<Pair<List<Pair<Term, Term>>, List<Pair<Term, Term>>>> unfailingCompletion(
			List<Pair<Term, Term>> eqs, List<Pair<Term, Term>> reds) {
		List<Pair<List<Pair<Term, Term>>, List<Pair<Term, Term>>>> ret
		 = new LinkedList<Pair<List<Pair<Term, Term>>, List<Pair<Term, Term>>>>();
		Pair<List<Pair<Term, Term>>, List<Pair<Term, Term>>> x;
		
		x = rule1(eqs, reds);
		if (fair(x)) {
			ret.add(x);
		}
		
		x = rule2(eqs, reds);
		if (fair(x)) {
			ret.add(x);
		}
		
		return ret;
	}

	private static boolean fair(
			Pair<List<Pair<Term, Term>>, List<Pair<Term, Term>>> x) {
		return false;
	}
	
	static boolean pathequiv(Signature c, Path p1, Path p2) {
		Term s = pathToTerm(p1);
		Term t = pathToTerm(p2);
		List<Pair<Term, Term>> e = sigToEqs(c);
		return valid(e, s, t);
	}

	private static List<Pair<Term, Term>> sigToEqs(Signature c) {
		return null;
	}

	private static Term pathToTerm(Path p1) {
		return null;
	}

	static boolean valid(List<Pair<Term, Term>> E, Term s, Term t) {
		String v = fresh();
		E.add(new Pair<Term, Term>(
				new Equality(new Var(v), new Var(v)), new Boolean(true)));
		E.add(new Pair<Term, Term>(
				new Equality(s, t), new Boolean(false)));
		
	//	List<Pair<Term, Term>> eqs = E;
		//List<Pair<Term, Term>> reds = new LinkedList<Pair<Term, Term>>();
		
	
		
		//fix(eqs, reds, c1, c2);

		
		// enumerate fair unfailing completions until find true = false
		return false;
	}
	
	static Pair<Term, Term> c1 = new Pair<Term, Term>(
			new Boolean(true), new Boolean(false));
	static Pair<Term, Term> c2 = new Pair<Term, Term>(
			new Boolean(false), new Boolean(true));
	
	//breadth first
	//fairness check after
//	private static void fix(List<Pair<Term, Term>> eqs,
//			List<Pair<Term, Term>> reds) throws Found {
//		for (;;) {
//			List<Pair<List<Pair<Term, Term>>, List<Pair<Term, Term>>>> rets = 
//			unfailingCompletion(eqs, reds);
//			for (Pair<List<Pair<Term, Term>>, List<Pair<Term, Term>>> ret : rets) {
//				if (ret.first.equals(c1) || ret.first.equals(c2)) {
//					throw new Found();
//				}
//				if (ret.second.equals(c1) || ret.second.equals(c2)) {
//					throw new Found();
//				}
//			}
//			for (Pair<List<Pair<Term, Term>>, List<Pair<Term, Term>>> ret : rets) {
//				fix(ret.first, ret.second);
//			}
//		}
//	}

	static class Var extends Term {
		public Var(String name) {

		}
	}
	
	static class Found extends Exception {

		private static final long serialVersionUID = 1L;
		
	}

	static abstract class Op extends Term {
		static class Id extends Op {
			public Id(String name) {

			}
		}
	}
		
		static class Equality extends Op {
			Term lhs, rhs;
			public Equality(Term lhs, Term rhs) {
				this.lhs = lhs;
				this.rhs = rhs;
			}
		}
		
		static class Boolean extends Op {
			boolean b;
			public Boolean(boolean b) {
				this.b = b;
			}
			
		}

		static class Compose extends Op {

		}

		static class Obj extends Op {

		}

		static class Mor extends Op {

		}
	

	static boolean lt(Term t1, Term t2) {
		return false;

	}

}
