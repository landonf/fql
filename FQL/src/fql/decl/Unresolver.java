package fql.decl;

import java.util.Map;

import fql.decl.SigExp.Const;
import fql.decl.SigExp.Exp;
import fql.decl.SigExp.One;
import fql.decl.SigExp.Plus;
import fql.decl.SigExp.SigExpVisitor;
import fql.decl.SigExp.Times;
import fql.decl.SigExp.Var;
import fql.decl.SigExp.Zero;

public class Unresolver implements SigExpVisitor<SigExp, Map<String, SigExp>> {

	@Override
	public SigExp visit(Map<String, SigExp> env, Zero e) {
		return e;
	}

	@Override
	public SigExp visit(Map<String, SigExp> env, One e) {
		return e;
	}

	@Override
	public SigExp visit(Map<String, SigExp> env, Plus e) {
		return new SigExp.Plus(e.a.accept(env, this), e.b.accept(env, this));
	}

	@Override
	public SigExp visit(Map<String, SigExp> env, Times e) {
		return new SigExp.Times(e.a.accept(env, this), e.b.accept(env, this));
	}

	@Override
	public SigExp visit(Map<String, SigExp> env, Exp e) {
		return new SigExp.Exp(e.a.accept(env, this), e.b.accept(env, this));
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
	
}