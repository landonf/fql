package fql.decl;

import java.util.List;
import java.util.Map;

import fql.Pair;
import fql.Triple;
import fql.decl.InstExp.Const;
import fql.decl.InstExp.Delta;
import fql.decl.InstExp.Exp;
import fql.decl.InstExp.External;
import fql.decl.InstExp.FullSigma;
import fql.decl.InstExp.InstExpVisitor;
import fql.decl.InstExp.One;
import fql.decl.InstExp.Pi;
import fql.decl.InstExp.Plus;
import fql.decl.InstExp.Relationalize;
import fql.decl.InstExp.Sigma;
import fql.decl.InstExp.Times;
import fql.decl.InstExp.Two;
import fql.decl.InstExp.Var;
import fql.decl.InstExp.Zero;

public class InstChecker implements InstExpVisitor<SigExp, Triple<Map<String, SigExp>, Map<String, MapExp>, Map<String, InstExp>>> {

	List<String> seen;
	
	public InstChecker(List<String> seen) {
		this.seen = seen;
	}

	@Override
	public SigExp visit(
			Triple<Map<String, SigExp>, Map<String, MapExp>, Map<String, InstExp>> env,
			Zero e) {
		e.sig.typeOf(env.first);
		return e.sig;
	}

	@Override
	public SigExp visit(
			Triple<Map<String, SigExp>, Map<String, MapExp>, Map<String, InstExp>> env,
			One e) {
		e.sig.typeOf(env.first);
		return e.sig;
	}

	@Override
	public SigExp visit(
			Triple<Map<String, SigExp>, Map<String, MapExp>, Map<String, InstExp>> env,
			Two e) {
		e.sig.typeOf(env.first);
		return e.sig;
	}

	@Override
	public SigExp visit(
			Triple<Map<String, SigExp>, Map<String, MapExp>, Map<String, InstExp>> env,
			Plus e) {
		SigExp lt = e.a.accept(env, this);
		SigExp rt = e.b.accept(env, this);
		if (!lt.equals(rt)) {
			throw new RuntimeException("Not of same type in " + e + ": " + lt + " and " + rt);
		}
		return lt;
	}

	@Override
	public SigExp visit(
			Triple<Map<String, SigExp>, Map<String, MapExp>, Map<String, InstExp>> env,
			Times e) {
		SigExp lt = e.a.accept(env, this);
		SigExp rt = e.b.accept(env, this);
		if (!lt.equals(rt)) {
			throw new RuntimeException("Not of same type in " + e + ": " + lt + " and " + rt);
		}
		return lt;
	}

	@Override
	public SigExp visit(
			Triple<Map<String, SigExp>, Map<String, MapExp>, Map<String, InstExp>> env,
			Exp e) {
		SigExp lt = e.a.accept(env, this);
		SigExp rt = e.b.accept(env, this);
		if (!lt.equals(rt)) {
			throw new RuntimeException("Not of same type in " + e + ": " + lt + " and " + rt);
		}
		return lt;
	}

	@Override
	public SigExp visit(
			Triple<Map<String, SigExp>, Map<String, MapExp>, Map<String, InstExp>> env,
			Var e) {
		if (seen.contains(e.v)) {
			throw new RuntimeException("Cyclic definition: " + e.v);
		}
		seen.add(e.v);
		InstExp i = env.third.get(e.v);
		if (i == null) {
			throw new RuntimeException("Unknown instance " + i);
		}
		return i.accept(env, this);
	}

	@Override
	//TODO const inst checker
	public SigExp visit(
			Triple<Map<String, SigExp>, Map<String, MapExp>, Map<String, InstExp>> env,
			Const e) {
		e.sig.typeOf(env.first);
		return e.sig;
	}

	@Override
	public SigExp visit(
			Triple<Map<String, SigExp>, Map<String, MapExp>, Map<String, InstExp>> env,
			Delta e) {
		SigExp it = e.I.accept(env, this);
		Pair<SigExp, SigExp> ft = e.F.type(env.first, env.second);
		if (!ft.second.equals(it)) {
			throw new RuntimeException("In " + e + " expected instance to be " + ft.second + " but is " + it);
		}
		return ft.first;
	}

	@Override
	//TODO check disc op fib
	public SigExp visit(
			Triple<Map<String, SigExp>, Map<String, MapExp>, Map<String, InstExp>> env,
			Sigma e) {
		SigExp it = e.I.accept(env, this);
		Pair<SigExp, SigExp> ft = e.F.type(env.first, env.second);
		if (!ft.first.equals(it)) {
			throw new RuntimeException("In " + e + " expected instance to be " + ft.first + " but is " + it);
		}
		return ft.second;
	}

	@Override
	//TODO check bijection
	public SigExp visit(
			Triple<Map<String, SigExp>, Map<String, MapExp>, Map<String, InstExp>> env,
			Pi e) {
		SigExp it = e.I.accept(env, this);
		Pair<SigExp, SigExp> ft = e.F.type(env.first, env.second);
		if (!ft.first.equals(it)) {
			throw new RuntimeException("In " + e + " expected instance to be " + ft.first + " but is " + it);
		}
		return ft.second;
	}

	@Override
	//TODO check union comp
	public SigExp visit(
			Triple<Map<String, SigExp>, Map<String, MapExp>, Map<String, InstExp>> env,
			FullSigma e) {
		SigExp it = e.I.accept(env, this);
		Pair<SigExp, SigExp> ft = e.F.type(env.first, env.second);
		if (!ft.first.equals(it)) {
			throw new RuntimeException("In " + e + " expected instance to be " + ft.first + " but is " + it);
		}
		return ft.second;
	}

	@Override
	public SigExp visit(
			Triple<Map<String, SigExp>, Map<String, MapExp>, Map<String, InstExp>> env,
			Relationalize e) {
		return e.I.accept(env, this);
	}

	@Override
	public SigExp visit(
			Triple<Map<String, SigExp>, Map<String, MapExp>, Map<String, InstExp>> env,
			External e) {
		e.sig.typeOf(env.first);
		return e.sig;
	}
	
	
}
