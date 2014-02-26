package fql.decl;

import java.util.Map;

import fql.decl.SigExp.Const;
import fql.decl.SigExp.Exp;
import fql.decl.SigExp.One;
import fql.decl.SigExp.Opposite;
import fql.decl.SigExp.Plus;
import fql.decl.SigExp.SigExpVisitor;
import fql.decl.SigExp.Times;
import fql.decl.SigExp.Union;
import fql.decl.SigExp.Unknown;
import fql.decl.SigExp.Var;
import fql.decl.SigExp.Zero;

public class Unresolver implements SigExpVisitor<SigExp, Map<String, SigExp>> {

	@Override
	public SigExp visit(Map<String, SigExp> env, Zero e) {
		for (String k : env.keySet()) {
			SigExp e0 = env.get(k);
			if (e0.equals(e)) {
				return new SigExp.Var(k);
			}
		}
		return e;
	}

	@Override
	public SigExp visit(Map<String, SigExp> env, One e) {
		for (String k : env.keySet()) {
			SigExp e0 = env.get(k);
			if (e0.equals(e)) {
				return new SigExp.Var(k);
			}
		}
		return e;
	}

	@Override
	public SigExp visit(Map<String, SigExp> env, Plus e) {
		SigExp t = new SigExp.Plus(e.a.accept(env, this), e.b.accept(env, this));
		for (String k : env.keySet()) {
			SigExp e0 = env.get(k);
			if (e0.equals(t)) {
				return new SigExp.Var(k);
			}
		}
		return t;
	}

	@Override
	public SigExp visit(Map<String, SigExp> env, Times e) {
		SigExp t = new SigExp.Times(e.a.accept(env, this), e.b.accept(env, this));
		for (String k : env.keySet()) {
			SigExp e0 = env.get(k);
			if (e0.equals(t)) {
				return new SigExp.Var(k);
			}
		}
		return t;
	}

	@Override
	public SigExp visit(Map<String, SigExp> env, Exp e) {
	//	System.out.println("unresolving " + e);
		SigExp t = new SigExp.Exp(e.a.accept(env, this), e.b.accept(env, this));
	//	System.out.println(t);
		for (String k : env.keySet()) {
			SigExp e0 = env.get(k);
	//		System.out.println("k " + e0);
			if (e0.equals(t)) {
	//			System.out.println("returning " + k);
				return new SigExp.Var(k);
			}
		}
	//	System.out.println("nope");
		return t;
	}

	@Override
	public SigExp visit(Map<String, SigExp> env, Var e) {
		return e;
	}

	@Override
	public SigExp visit(Map<String, SigExp> env, Const e) {
		for (String k : env.keySet()) {
			SigExp e0 = env.get(k);
			if (e0.equals(e)) {
				return new SigExp.Var(k);
			}
		}
		return e;
	}

	@Override
	public SigExp visit(Map<String, SigExp> env, Union e) {
		SigExp t = new SigExp.Union(e.l.accept(env, this), e.r.accept(env, this));
		for (String k : env.keySet()) {
			SigExp e0 = env.get(k);
			if (e0.equals(t)) {
				return new SigExp.Var(k);
			}
		}
		return t;
	}

	@Override
	public SigExp visit(Map<String, SigExp> env, Opposite e) {
		SigExp t = new SigExp.Opposite(e.e.accept(env, this));
		for (String k : env.keySet()) {
			SigExp e0 = env.get(k);
			if (e0.equals(t)) {
				return new SigExp.Var(k);
			}
		}
		return t;
	}
	
	@Override
	public fql.decl.SigExp.Const visit(Map<String, SigExp> env, Unknown e) {
		throw new RuntimeException("Encountered unknown type.");
	}

	
}