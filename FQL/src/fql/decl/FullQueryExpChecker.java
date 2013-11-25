package fql.decl;

import java.util.LinkedList;
import java.util.List;

import fql.Pair;
import fql.Triple;
import fql.decl.FullQueryExp.Comp;
import fql.decl.FullQueryExp.Delta;
import fql.decl.FullQueryExp.FullQueryExpVisitor;
import fql.decl.FullQueryExp.Match;
import fql.decl.FullQueryExp.Pi;
import fql.decl.FullQueryExp.Sigma;
import fql.decl.FullQueryExp.Var;

public class FullQueryExpChecker implements
		FullQueryExpVisitor<Pair<SigExp, SigExp>, FQLProgram> {

	List<String> seen = new LinkedList<>();

	@Override
	public Pair<SigExp, SigExp> visit(FQLProgram env, Comp e) {
		List<String> x = new LinkedList<String>(seen);
		Pair<SigExp, SigExp> lt = e.l.accept(env, this);
		seen = x;
		Pair<SigExp, SigExp> rt = e.r.accept(env, this);
		seen = x;
		if (!lt.second.equals(rt.first)) {
			throw new RuntimeException("Mismatch: " + lt.second + " and "
					+ rt.first);
		}
		return new Pair<>(lt.first, rt.second);
	}

	@Override
	public Pair<SigExp, SigExp> visit(FQLProgram env, Var e) {
		if (seen.contains(e.v)) {
			throw new RuntimeException("Circular: " + e.v);
		}
		seen.add(e.v);
		FullQueryExp q = env.full_queries.get(e.v);
		if (q == null) {
			throw new RuntimeException("Unknown query: " + e.v);
		}
		return q.accept(env, this);
	}

	@Override
	public Pair<SigExp, SigExp> visit(FQLProgram env, Match e) {
		List<String> x = new LinkedList<String>(seen);
		SigExp.Const s = e.src.typeOf(env).toConst(env);
		seen = x;
		SigExp.Const t = e.dst.typeOf(env).toConst(env);
		seen = x;
		for (Pair<String, String> p : e.rel) {
			if (!contains(s.attrs, p.first)) {
				throw new RuntimeException(p.first + " is not in attriubtes of " + e.src);
			}
			if (!contains(t.attrs, p.second)) {
				throw new RuntimeException(p.second + " is not in attributes of " + e.dst);				
			}
		}
		if (e.kind.equals("delta sigma forward")) {
			return new Pair<SigExp, SigExp>(t,s);
		} else if (e.kind.equals("delta pi forward")) {
			return new Pair<SigExp, SigExp>(t,s);			
		} else if (e.kind.equals("delta sigma backward")) {
			return new Pair<SigExp, SigExp>(s,t);						
		} else if (e.kind.equals("delta pi backward")) {
			return new Pair<SigExp, SigExp>(s,t);			
		} else {
			throw new RuntimeException("Unknown kind: " + e.kind);
		}
	}

	private boolean contains(List<Triple<String, String, String>> attrs,
			String s) {
		for (Triple<String, String, String> a : attrs) {
			if (a.first.equals(s)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Pair<SigExp, SigExp> visit(FQLProgram env, Delta e) {
		Pair<SigExp, SigExp> k = e.f.type(env);
		return new Pair<>(k.second, k.first);
	}

	@Override
	public Pair<SigExp, SigExp> visit(FQLProgram env, Sigma e) {
		return e.f.type(env);
	}

	@Override
	public Pair<SigExp, SigExp> visit(FQLProgram env, Pi e) {
		return e.f.type(env);
	}
}
