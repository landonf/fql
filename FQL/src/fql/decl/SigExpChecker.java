package fql.decl;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import fql.FQLException;
import fql.Unit;
import fql.decl.SigExp.Const;
import fql.decl.SigExp.Exp;
import fql.decl.SigExp.One;
import fql.decl.SigExp.Plus;
import fql.decl.SigExp.SigExpVisitor;
import fql.decl.SigExp.Times;
import fql.decl.SigExp.Var;
import fql.decl.SigExp.Zero;

public class SigExpChecker implements SigExpVisitor<Unit, Map<String, SigExp>>{

	public List<String> seen = new LinkedList<>();
	
	@Override
	public Unit visit(Map<String, SigExp> env, Zero e) {
		return new Unit();
	}

	@Override
	public Unit visit(Map<String, SigExp> env, One e) {
		return new Unit();
	}

	@Override
	public Unit visit(Map<String, SigExp> env, Plus e) {
		List<String> s = new LinkedList<>(seen);
		e.a.accept(env, this);
		seen = s;
		e.b.accept(env, this);
		seen = s;
		return new Unit();
	}

	@Override
	public Unit visit(Map<String, SigExp> env, Times e) {
		List<String> s = new LinkedList<>(seen);
		e.a.accept(env, this);
		seen = s;
		e.b.accept(env, this);
		seen = s;
		return new Unit();
	}

	@Override
	public Unit visit(Map<String, SigExp> env, Exp e) {
		List<String> s = new LinkedList<>(seen);
		e.a.accept(env, this);
		seen = s;
		e.b.accept(env, this);
		seen = s;
		return new Unit();
	}

	@Override
	public Unit visit(Map<String, SigExp> env, Var e) {
		if (seen.contains(e.v)) {
			throw new RuntimeException("Cyclic definition: " + e);
		}
		seen.add(e.v);
		SigExp r = env.get(e.v);
		if (r == null) {
			throw new RuntimeException("Unknown schema: " + e);
		}
		return r.accept(env, this);
	}

	@Override
	public Unit visit(Map<String, SigExp> env, Const e) {
		try {
			new Signature(e.nodes, e.attrs, e.arrows, e.eqs);
		} catch (FQLException ee) {
			throw new RuntimeException(ee.getLocalizedMessage());
		}
		return new Unit();
	}
	
	//TODO take care of seen for map checker like in sig checker

}
