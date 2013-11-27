package fql.decl;

import fql.decl.FullQueryExp.Comp;
import fql.decl.FullQueryExp.Delta;
import fql.decl.FullQueryExp.FullQueryExpVisitor;
import fql.decl.FullQueryExp.Match;
import fql.decl.FullQueryExp.Pi;
import fql.decl.FullQueryExp.Sigma;
import fql.decl.FullQueryExp.Var;

public class ToFullQueryVisitor implements
		FullQueryExpVisitor<FullQuery, FQLProgram> {

	@Override
	public FullQuery visit(FQLProgram env, Delta e) {
		return new FullQuery.Delta(e.f.toMap(env));
	}

	@Override
	public FullQuery visit(FQLProgram env, Sigma e) {
		return new FullQuery.Sigma(e.f.toMap(env));
	}

	@Override
	public FullQuery visit(FQLProgram env, Pi e) {
		return new FullQuery.Pi(e.f.toMap(env));
	}

	@Override
	public FullQuery visit(FQLProgram env, Comp e) {
		return new FullQuery.Comp(e.l.accept(env, this), e.r.accept(env, this));
	}

	@Override
	public FullQuery visit(FQLProgram env, Var e) {
		return env.full_queries.get(e.v).accept(env, this);
	}

	@Override
	public FullQuery visit(FQLProgram env, Match e) {
			throw new RuntimeException("Match encountered in to query " + e);
	}

	

}
