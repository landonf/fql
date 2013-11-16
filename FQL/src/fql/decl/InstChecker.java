package fql.decl;

import java.util.Map;

import fql.FQLException;
import fql.Pair;
import fql.Quad;
import fql.decl.InstExp.Const;
import fql.decl.InstExp.Delta;
import fql.decl.InstExp.Eval;
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
import fql.decl.InstExp.Zero;

public class InstChecker implements InstExpVisitor<SigExp, Quad<Map<String, SigExp>, Map<String, MapExp>, Map<String, InstExp>, Map<String, QueryExp>>> {

	@Override
	public SigExp visit(
			Quad<Map<String, SigExp>, Map<String, MapExp>, Map<String, InstExp>, Map<String, QueryExp>> env,
			Zero e) {
		SigExp k = e.sig.typeOf(env.first);
		return k;
	}

	@Override
	public SigExp visit(
			Quad<Map<String, SigExp>, Map<String, MapExp>, Map<String, InstExp>, Map<String, QueryExp>> env,
			One e) {
		SigExp k = e.sig.typeOf(env.first);
		return k;
	}

	@Override
	public SigExp visit(
			Quad<Map<String, SigExp>, Map<String, MapExp>, Map<String, InstExp>, Map<String, QueryExp>> env,
			Two e) {
		SigExp k = e.sig.typeOf(env.first);
		return k;
	}

	@Override
	public SigExp visit(
			Quad<Map<String, SigExp>, Map<String, MapExp>, Map<String, InstExp>, Map<String, QueryExp>> env,
			Plus e) {
		SigExp lt = env.third.get(e.a).accept(env, this);
		SigExp rt = env.third.get(e.b).accept(env, this);
		if (!lt.equals(rt)) {
			throw new RuntimeException("Not of same type in " + e + ": " + lt + " and " + rt);
		}
		return lt;
	}

	@Override
	public SigExp visit(
			Quad<Map<String, SigExp>, Map<String, MapExp>, Map<String, InstExp>, Map<String, QueryExp>> env,
			Times e) {
		SigExp lt = env.third.get(e.a).accept(env, this);
		SigExp rt = env.third.get(e.b).accept(env, this);
		if (!lt.equals(rt)) {
			throw new RuntimeException("Not of same type in " + e + ": " + lt + " and " + rt);
		}
		return lt;
	}

	@Override
	public SigExp visit(
			Quad<Map<String, SigExp>, Map<String, MapExp>, Map<String, InstExp>, Map<String, QueryExp>> env,
			Exp e) {
		SigExp lt = env.third.get(e.a).accept(env, this);
		SigExp rt = env.third.get(e.b).accept(env, this);
		if (!lt.equals(rt)) {
			throw new RuntimeException("Not of same type in " + e + ": " + lt + " and " + rt);
		}
		return lt;
	}
/*
	@Override
	public SigExp visit(
			Quad<Map<String, SigExp>, Map<String, MapExp>, Map<String, InstExp>, Map<String, QueryExp>> env,
			Var e) {
		if (seen.contains(e.v)) {
			throw new RuntimeException("Cyclic definition: " + e.v);
		}
		seen.add(e.v);
		InstExp i = env.third.get(e.v);
		if (i == null) {
			throw new RuntimeException("Unknown instance " + e);
		}
		return i.accept(env, this);
	}
	*/

	@Override
	public SigExp visit(
			Quad<Map<String, SigExp>, Map<String, MapExp>, Map<String, InstExp>, Map<String, QueryExp>> env,
			Const e) {
		SigExp k = e.sig.typeOf(env.first);
		try {
			new Instance(k.toSig(env.first), e.data);
			return k;
		} catch (FQLException fe) {
			fe.printStackTrace();
			throw new RuntimeException(fe.getLocalizedMessage());
		}
	}

	@Override
	public SigExp visit(
			Quad<Map<String, SigExp>, Map<String, MapExp>, Map<String, InstExp>, Map<String, QueryExp>> env,
			Delta e) {
		SigExp it = env.third.get(e.I).accept(env, this);
		Pair<SigExp, SigExp> ft = e.F.type(env.first, env.second);
		if (!ft.second.equals(it)) {
			throw new RuntimeException("In " + e + " expected instance to be " + ft.second + " but is " + it);
		}
		return ft.first;
	}

	@Override
	//TODO check disc op fib
	public SigExp visit(
			Quad<Map<String, SigExp>, Map<String, MapExp>, Map<String, InstExp>, Map<String, QueryExp>> env,
			Sigma e) {
		SigExp it = env.third.get(e.I).accept(env, this);
		Pair<SigExp, SigExp> ft = e.F.type(env.first, env.second);
		if (!ft.first.equals(it)) {
			throw new RuntimeException("In " + e + " expected instance to be " + ft.first + " but is " + it);
		}
		return ft.second;
	}

	@Override
	//TODO check bijection
	public SigExp visit(
			Quad<Map<String, SigExp>, Map<String, MapExp>, Map<String, InstExp>, Map<String, QueryExp>> env,
			Pi e) {
		SigExp it = env.third.get(e.I).accept(env, this);
		Pair<SigExp, SigExp> ft = e.F.type(env.first, env.second);
		if (!ft.first.equals(it)) {
			throw new RuntimeException("In " + e + " expected instance to be " + ft.first + " but is " + it);
		}
		return ft.second;
	}

	@Override
	//TODO check union comp
	public SigExp visit(
			Quad<Map<String, SigExp>, Map<String, MapExp>, Map<String, InstExp>, Map<String, QueryExp>> env,
			FullSigma e) {
		SigExp it = env.third.get(e.I).accept(env, this);
		Pair<SigExp, SigExp> ft = e.F.type(env.first, env.second);
		if (!ft.first.equals(it)) {
			throw new RuntimeException("In " + e + " expected instance to be " + ft.first + " but is " + it);
		}
		return ft.second;
	}

	@Override
	public SigExp visit(
			Quad<Map<String, SigExp>, Map<String, MapExp>, Map<String, InstExp>, Map<String, QueryExp>> env,
			Relationalize e) {
		return env.third.get(e.I).accept(env, this);
	}

	@Override
	public SigExp visit(
			Quad<Map<String, SigExp>, Map<String, MapExp>, Map<String, InstExp>, Map<String, QueryExp>> env,
			External e) {
		SigExp k = e.sig.typeOf(env.first);
		return k;
	}

	@Override
	public SigExp visit(
			Quad<Map<String, SigExp>, Map<String, MapExp>, Map<String, InstExp>, Map<String, QueryExp>> env,
			Eval e) {
		Pair<SigExp, SigExp> k = e.q.type(env.first, env.second, env.fourth);
		if (null == env.third.get(e.e)) {
			throw new RuntimeException("Unknown: " + e.e);
		}
		SigExp v = env.third.get(e.e).accept(env, this);
		if (!(k.first.equals(v))) {
			throw new RuntimeException("On " + e + ", expected input to be " + k.first + " but computed " + v);
		}
		return k.second;
	}
	
	
}
