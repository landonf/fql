package fql.decl;

import java.util.Map;

import fql.Pair;
import fql.decl.Caml.Apply;
import fql.decl.Caml.Case;
import fql.decl.Caml.Comp;
import fql.decl.Caml.Const;
import fql.decl.Caml.Curry;
import fql.decl.Caml.Dist1;
import fql.decl.Caml.Dist2;
import fql.decl.Caml.Eq;
import fql.decl.Caml.FF;
import fql.decl.Caml.Fst;
import fql.decl.Caml.Id;
import fql.decl.Caml.Inl;
import fql.decl.Caml.Inr;
import fql.decl.Caml.Prod;
import fql.decl.Caml.Snd;
import fql.decl.Caml.TT;
import fql.decl.Poly.Exp;
import fql.decl.Poly.One;
import fql.decl.Poly.Plus;
import fql.decl.Poly.Times;
import fql.decl.Poly.Two;
import fql.decl.Poly.Var;
import fql.decl.Poly.Zero;

/*
public class Resolver<C, D> implements CamlVisitor<C, D, Caml<C,D>, Pair<Map<String, Caml<C,D>>, Map<String, Poly<C>>>>,   
	 PolyVisitor<C, Poly<C>, Map<String, Poly<C>>> {

	@Override
	public Poly<C> visit(Map<String, Poly<C>> env, Zero<C> e) {
		return e;
	}

	@Override
	public Poly<C> visit(Map<String, Poly<C>> env, One<C> e) {
		return e;
	}

	@Override
	public Poly<C> visit(Map<String, Poly<C>> env, Plus<C> e) {
		return new Plus<C>(e.a.accept(env, this), e.b.accept(env, this));
	}

	@Override
	public Poly<C> visit(Map<String, Poly<C>> env, Times<C> e) {
		return new Times<C>(e.a.accept(env, this), e.b.accept(env, this));	}

	@Override
	public Poly<C> visit(Map<String, Poly<C>> env, Exp<C> e) {
		return new Exp<C>(e.a.accept(env, this), e.b.accept(env, this));
	}

	@Override
	public Poly<C> visit(Map<String, Poly<C>> env, Two<C> e) {
		return e;
	}

	@Override
	public Poly<C> visit(Map<String, Poly<C>> env, Var<C> e) {
		return env.get(e.v).accept(env, this);
	}

	@Override
	public Poly<C> visit(Map<String, Poly<C>> env, Poly.Const<C> e) {
		return e;
	}

	
	/////
	
	
	@Override
	public Caml<C, D> visit(
			Pair<Map<String, Caml<C, D>>, Map<String, Poly<C>>> env, Id<C, D> e) {
		return new Id<C,D>(e.t.accept(env.second, this));
	}

	@Override
	public Caml<C, D> visit(
			Pair<Map<String, Caml<C, D>>, Map<String, Poly<C>>> env,
			Comp<C, D> e) {
		return new Comp<C,D>(e.l.accept(env, this), e.r.accept(env, this));
	}

	@Override
	public Caml<C, D> visit(
			Pair<Map<String, Caml<C, D>>, Map<String, Poly<C>>> env,
			Dist1<C, D> e) {
		return new Dist1<C,D>(e.a.accept(env.second, this), e.b.accept(env.second, this), e.c.accept(env.second, this));
	}

	@Override
	public Caml<C, D> visit(
			Pair<Map<String, Caml<C, D>>, Map<String, Poly<C>>> env,
			Dist2<C, D> e) {
		return new Dist2<C,D>(e.a.accept(env.second, this), e.b.accept(env.second, this), e.c.accept(env.second, this));
	}

	@Override
	public Caml<C, D> visit(
			Pair<Map<String, Caml<C, D>>, Map<String, Poly<C>>> env,
			fql.decl.Caml.Var<C, D> e) {
		return env.first.get(e.v).accept(env, this);
	}

	@Override
	public Caml<C, D> visit(
			Pair<Map<String, Caml<C, D>>, Map<String, Poly<C>>> env,
			Const<C, D> e) {
		return new Const<C,D>(e.src.accept(env.second, this), e.dst.accept(env.second, this), e.d);
	}

	@Override
	public Caml<C, D> visit(
			Pair<Map<String, Caml<C, D>>, Map<String, Poly<C>>> env, TT<C, D> e) {
		return new TT<C,D>(e.t.accept(env.second, this));
	}

	@Override
	public Caml<C, D> visit(
			Pair<Map<String, Caml<C, D>>, Map<String, Poly<C>>> env, FF<C, D> e) {
		return new FF<C,D>(e.t.accept(env.second, this));
	}

	@Override
	public Caml<C, D> visit(
			Pair<Map<String, Caml<C, D>>, Map<String, Poly<C>>> env, Fst<C, D> e) {
		return new Fst<C,D>(e.s.accept(env.second, this), e.t.accept(env.second, this));
	}

	@Override
	public Caml<C, D> visit(
			Pair<Map<String, Caml<C, D>>, Map<String, Poly<C>>> env, Snd<C, D> e) {
		return new Snd<C,D>(e.s.accept(env.second, this), e.t.accept(env.second, this));

	}

	@Override
	public Caml<C, D> visit(
			Pair<Map<String, Caml<C, D>>, Map<String, Poly<C>>> env, Inl<C, D> e) {
		return new Inl<C,D>(e.s.accept(env.second, this), e.t.accept(env.second, this));

	}

	@Override
	public Caml<C, D> visit(
			Pair<Map<String, Caml<C, D>>, Map<String, Poly<C>>> env, Inr<C, D> e) {
		return new Inr<C,D>(e.s.accept(env.second, this), e.t.accept(env.second, this));

	}

	@Override
	public Caml<C, D> visit(
			Pair<Map<String, Caml<C, D>>, Map<String, Poly<C>>> env,
			Apply<C, D> e) {
		return new Apply<C,D>(e.s.accept(env.second, this), e.t.accept(env.second, this));
	}

	@Override
	public Caml<C, D> visit(
			Pair<Map<String, Caml<C, D>>, Map<String, Poly<C>>> env,
			Curry<C, D> e) {
		return new Curry<C,D>(e.f.accept(env, this));
	}

	@Override
	public Caml<C, D> visit(
			Pair<Map<String, Caml<C, D>>, Map<String, Poly<C>>> env, Eq<C, D> e) {
		return new Eq<C,D>(e.t.accept(env.second, this));
	}

	@Override
	public Caml<C, D> visit(
			Pair<Map<String, Caml<C, D>>, Map<String, Poly<C>>> env,
			Case<C, D> e) {
		return new Case<C,D>(e.l.accept(env, this), e.r.accept(env, this));
	}

	@Override
	public Caml<C, D> visit(
			Pair<Map<String, Caml<C, D>>, Map<String, Poly<C>>> env,
			Prod<C, D> e) {
		return new Prod<C,D>(e.l.accept(env, this), e.r.accept(env, this));
	}
}
*/

 public class Resolver {} 
