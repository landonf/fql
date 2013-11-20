package fql.decl;

import java.util.LinkedList;
import java.util.List;

import fql.FQLException;
import fql.decl.SigExp.Const;
import fql.decl.SigExp.Exp;
import fql.decl.SigExp.One;
import fql.decl.SigExp.Plus;
import fql.decl.SigExp.SigExpVisitor;
import fql.decl.SigExp.Times;
import fql.decl.SigExp.Var;
import fql.decl.SigExp.Zero;

public class SigExpChecker implements SigExpVisitor<SigExp, FQLProgram>{

	public List<String> seen = new LinkedList<>();
	
	@Override
	public SigExp visit(FQLProgram env, Zero e) {
		return e;
	}

	@Override
	public SigExp visit(FQLProgram env, One e) {
		return e;
	}

	@Override
	public SigExp visit(FQLProgram env, Plus e) {
		List<String> s = new LinkedList<>(seen);
		SigExp a = e.a.accept(env, this);
		seen = s;
		SigExp b = e.b.accept(env, this);
		seen = s;
		return new SigExp.Plus(a,b); 
	}

	@Override
	public SigExp visit(FQLProgram env, Times e) {
		List<String> s = new LinkedList<>(seen);
		SigExp a = e.a.accept(env, this);
		seen = s;
		SigExp b = e.b.accept(env, this);
		seen = s;
		return new Times(a,b);
	}

	@Override
	public SigExp visit(FQLProgram env, Exp e) {
		List<String> s = new LinkedList<>(seen);
		SigExp a = e.a.accept(env, this);
		seen = s;
		SigExp b = e.b.accept(env, this);
		seen = s;
		return new Exp(a,b);
	}

	@Override
	public SigExp visit(FQLProgram env, Var e) {
		if (seen.contains(e.v)) {
			throw new RuntimeException("Cyclic definition: " + e);
		}
		seen.add(e.v);
		SigExp r = env.sigs.get(e.v);
		if (r == null) {
			throw new RuntimeException("Unknown schema: " + e);
		}
		return r.accept(env, this);
	}

	@Override
	public SigExp visit(FQLProgram env, Const e) {
		try {
			new Signature(env.enums, e.nodes, e.attrs, e.arrows, e.eqs);
		} catch (FQLException ee) {
			throw new RuntimeException(ee.getLocalizedMessage());
		}
		return e;
	}
	
}
