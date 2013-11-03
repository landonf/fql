package fql.decl;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import fql.Pair;
import fql.decl.MapExp.Apply;
import fql.decl.MapExp.Case;
import fql.decl.MapExp.Comp;
import fql.decl.MapExp.Const;
import fql.decl.MapExp.Curry;
import fql.decl.MapExp.Dist1;
import fql.decl.MapExp.Dist2;
import fql.decl.MapExp.FF;
import fql.decl.MapExp.Fst;
import fql.decl.MapExp.Id;
import fql.decl.MapExp.Inl;
import fql.decl.MapExp.Inr;
import fql.decl.MapExp.MapExpVisitor;
import fql.decl.MapExp.Prod;
import fql.decl.MapExp.Snd;
import fql.decl.MapExp.TT;
import fql.decl.MapExp.Var;

public class MapExpChecker implements MapExpVisitor<Pair<SigExp, SigExp>, Pair<Map<String, SigExp>, Map<String, MapExp>>> {

	public List<String> seen = new LinkedList<>();
	
	public MapExpChecker(List<String> seen) {
		this.seen = seen;
	}

	@Override
	public Pair<SigExp, SigExp> visit(
			Pair<Map<String, SigExp>, Map<String, MapExp>> env, Id e) {
		e.t.typeOf(env.first);
		return new Pair<>(e.t, e.t);
	}

	@Override
	public Pair<SigExp, SigExp> visit(
			Pair<Map<String, SigExp>, Map<String, MapExp>> env, Comp e) {
		Pair<SigExp, SigExp> lt = e.l.accept(env, this);
		Pair<SigExp, SigExp> rt = e.r.accept(env, this);
		if (!lt.second.equals(rt.first)) {
			throw new RuntimeException("Composition type mismatch: " + lt.second + " and " + rt.first + " on " + e);
		}
		return new Pair<>(lt.first, rt.second);

	}

	@Override
	public Pair<SigExp, SigExp> visit(
			Pair<Map<String, SigExp>, Map<String, MapExp>> env, Dist1 e) {
		e.a.typeOf(env.first);
		e.b.typeOf(env.first);
		e.c.typeOf(env.first);
		return new Pair<SigExp, SigExp>(new SigExp.Times(e.a,
				new SigExp.Plus(e.b, e.c)), new SigExp.Plus(new SigExp.Times(e.a,
				e.b), new SigExp.Times(e.a, e.c)));
	}

	@Override
	public Pair<SigExp, SigExp> visit(
			Pair<Map<String, SigExp>, Map<String, MapExp>> env, Dist2 e) {
		e.a.typeOf(env.first);
		e.b.typeOf(env.first);
		e.c.typeOf(env.first);
		return new Pair<SigExp, SigExp>(new SigExp.Plus(new SigExp.Times(e.a,
				e.b), new SigExp.Times(e.a, e.c)), new SigExp.Times(e.a,
						new SigExp.Plus(e.b, e.c)));
	}

	@Override
	public Pair<SigExp, SigExp> visit(
			Pair<Map<String, SigExp>, Map<String, MapExp>> env, Var e) {
		if (seen.contains(e.v)) {
			throw new RuntimeException("Cyclic definition: " + e);
		}
		seen.add(e.v);
		MapExp r = env.second.get(e.v);
		if (r == null) {
			throw new RuntimeException("Unknown mapping " + e.v);
		}
		return r.accept(env, this);
	}

	//TODO this when sigops ready
	@Override
	public Pair<SigExp, SigExp> visit(
			Pair<Map<String, SigExp>, Map<String, MapExp>> env, Const e) {
		e.src.typeOf(env.first);
		e.dst.typeOf(env.first);
		return new Pair<>(e.src, e.dst);
	}

	@Override
	public Pair<SigExp, SigExp> visit(
			Pair<Map<String, SigExp>, Map<String, MapExp>> env, TT e) {
		e.t.typeOf(env.first);
		return new Pair<SigExp, SigExp>(e.t, new SigExp.One());
	}

	@Override
	public Pair<SigExp, SigExp> visit(
			Pair<Map<String, SigExp>, Map<String, MapExp>> env, FF e) {
		e.t.typeOf(env.first);
		return new Pair<SigExp, SigExp>(e.t, new SigExp.Zero());
	}

	@Override
	public Pair<SigExp, SigExp> visit(
			Pair<Map<String, SigExp>, Map<String, MapExp>> env, Fst e) {
		e.s.typeOf(env.first);
		e.t.typeOf(env.first);
		return new Pair<SigExp, SigExp>(new SigExp.Times(e.s, e.t), e.s);
	}

	@Override
	public Pair<SigExp, SigExp> visit(
			Pair<Map<String, SigExp>, Map<String, MapExp>> env, Snd e) {
		e.s.typeOf(env.first);
		e.t.typeOf(env.first);
		return new Pair<SigExp, SigExp>(new SigExp.Times(e.s, e.t), e.t);
	}

	@Override
	public Pair<SigExp, SigExp> visit(
			Pair<Map<String, SigExp>, Map<String, MapExp>> env, Inl e) {
		e.s.typeOf(env.first);
		e.t.typeOf(env.first);
		return new Pair<SigExp, SigExp>(e.s, new SigExp.Plus(e.s, e.t));
	}

	@Override
	public Pair<SigExp, SigExp> visit(
			Pair<Map<String, SigExp>, Map<String, MapExp>> env, Inr e) {
		e.s.typeOf(env.first);
		e.t.typeOf(env.first);
		return new Pair<SigExp, SigExp>(e.t, new SigExp.Plus(e.s, e.t));
	}

	@Override
	public Pair<SigExp, SigExp> visit(
			Pair<Map<String, SigExp>, Map<String, MapExp>> env, Apply e) {
		e.s.typeOf(env.first);
		e.t.typeOf(env.first);
		return new Pair<SigExp, SigExp>(new SigExp.Times(new SigExp.Exp(e.s,
				e.t), e.t), e.s);
	}

	@Override
	public Pair<SigExp, SigExp> visit(
			Pair<Map<String, SigExp>, Map<String, MapExp>> env, Curry e) {
		Pair<SigExp, SigExp> ft = e.f.accept(env, this);
		if (!(ft.first instanceof SigExp.Times)) {
			throw new RuntimeException("Not a product: " + ft.first + " in " + e);
		}
		SigExp.Times t = (SigExp.Times) ft.first;
		return new Pair<SigExp, SigExp>(t.a, new SigExp.Exp(ft.second, t.b));
	}

	@Override
	public Pair<SigExp, SigExp> visit(
			Pair<Map<String, SigExp>, Map<String, MapExp>> env, Case e) {
		Pair<SigExp, SigExp> lt = e.l.accept(env, this);
		Pair<SigExp, SigExp> rt = e.r.accept(env, this);
		if (!lt.second.equals(rt.second)) {
			throw new RuntimeException("target schema mismatch on " + e + ": " + lt.second + " and " + rt.second);
		}
		return new Pair<SigExp, SigExp>(new SigExp.Plus(lt.first, rt.first), lt.second);

	}

	@Override
	public Pair<SigExp, SigExp> visit(
			Pair<Map<String, SigExp>, Map<String, MapExp>> env, Prod e) {
		Pair<SigExp, SigExp> lt = e.l.accept(env, this);
		Pair<SigExp, SigExp> rt = e.r.accept(env, this);
		if (!lt.first.equals(rt.first)) {
			throw new RuntimeException("source schema mismatch on " + e + ": " + lt.first + " and " + rt.first);
		}
		return new Pair<SigExp, SigExp>(lt.first, new SigExp.Times(lt.second, rt.second));
	}

}
