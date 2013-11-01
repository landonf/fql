package fql.decl;

import java.util.Map;

import fql.Fn;
import fql.Pair;
import fql.Triple;
import fql.Unit;
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
import fql.decl.Caml.Var;

public class CamlChecker<T,C,D> implements CamlVisitor<T, C, D, Pair<Poly<T,C>,Poly<T,C>>, Map<String, Pair<Poly<T,C>,Poly<T,C>>>> {

	Fn<Poly<T,C>, T> polyChecker;
	Fn<Triple<Poly<T,C>, Poly<T,C>, D>, Unit> constChecker;
	
	public CamlChecker(Fn<Poly<T,C>, T> polyChecker, Fn<Triple<Poly<T,C>, Poly<T,C>, D>, Unit> constChecker) {
		this.polyChecker = polyChecker;
		this.constChecker = constChecker;
	}
	
	@Override
	public Pair<Poly<T, C>, Poly<T, C>> visit(
			Map<String, Pair<Poly<T, C>, Poly<T, C>>> env, Id<T, C, D> e) {
		polyChecker.of(e.t);
		return new Pair<>(e.t, e.t);
	}

	@Override
	public Pair<Poly<T, C>, Poly<T, C>> visit(
			Map<String, Pair<Poly<T, C>, Poly<T, C>>> env, Comp<T, C, D> e) {
		Pair<Poly<T, C>, Poly<T, C>> t1 = e.l.accept(env, this);
		Pair<Poly<T, C>, Poly<T, C>> t2 = e.r.accept(env, this);
		if (!(t1.second.equals(t2.first))) {
			throw new RuntimeException("Middle type mismatch on " + e + ": " + t1.second + " and " + t2.first);
		}
		polyChecker.of(t1.first);
		polyChecker.of(t1.second);
		polyChecker.of(t2.first);
		polyChecker.of(t2.second);
		return new Pair<>(t1.first, t2.second);
	}

	//A * (B + C) -> A * B + A * C
	@Override
	public Pair<Poly<T, C>, Poly<T, C>> visit(
			Map<String, Pair<Poly<T, C>, Poly<T, C>>> env, Dist1<T, C, D> e) {
		polyChecker.of(e.a);
		polyChecker.of(e.b);
		polyChecker.of(e.c);
		Poly<T,C> t1 = new Poly.Times<>(e.a, new Poly.Plus<>(e.b, e.c));
		Poly<T,C> t2 = new Poly.Plus<>(new Poly.Times<>(e.a, e.b), new Poly.Times<>(e.a, e.c));
		return new Pair<>(t1, t2);
	}

	@Override
	public Pair<Poly<T, C>, Poly<T, C>> visit(
			Map<String, Pair<Poly<T, C>, Poly<T, C>>> env, Dist2<T, C, D> e) {
		polyChecker.of(e.a);
		polyChecker.of(e.b);
		polyChecker.of(e.c);
		Poly<T,C> t1 = new Poly.Times<>(e.a, new Poly.Plus<>(e.b, e.c));
		Poly<T,C> t2 = new Poly.Plus<>(new Poly.Times<>(e.a, e.b), new Poly.Times<>(e.a, e.c));
		return new Pair<>(t2, t1);
	}

	@Override
	public Pair<Poly<T, C>, Poly<T, C>> visit(
			Map<String, Pair<Poly<T, C>, Poly<T, C>>> env, Var<T, C, D> e) {
		Pair<Poly<T, C>, Poly<T, C>> t = env.get(e.v);
		if (t == null) {
			throw new RuntimeException("Unknown var: " + e);
		}
		return t;
	}

	@Override
	public Pair<Poly<T, C>, Poly<T, C>> visit(
			Map<String, Pair<Poly<T, C>, Poly<T, C>>> env, Const<T, C, D> e) {
		polyChecker.of(e.dst);
		polyChecker.of(e.src);
		constChecker.of(new Triple<>(e.src, e.dst, e.d));
		return new Pair<>(e.src, e.dst);
	}

	@Override
	public Pair<Poly<T, C>, Poly<T, C>> visit(
			Map<String, Pair<Poly<T, C>, Poly<T, C>>> env, TT<T, C, D> e) {
		T t1 = polyChecker.of(e.t);
		return new Pair<Poly<T,C>,Poly<T,C>>(e.t, new Poly.One<T,C>(t1));
	}

	@Override
	public Pair<Poly<T, C>, Poly<T, C>> visit(
			Map<String, Pair<Poly<T, C>, Poly<T, C>>> env, FF<T, C, D> e) {
		T t1 = polyChecker.of(e.t);
		return new Pair<Poly<T,C>,Poly<T,C>>(new Poly.Zero<T,C>(t1), e.t);
	}

	@Override
	public Pair<Poly<T, C>, Poly<T, C>> visit(
			Map<String, Pair<Poly<T, C>, Poly<T, C>>> env, Fst<T, C, D> e) {
		polyChecker.of(e.s);
		polyChecker.of(e.t);
		return new Pair<Poly<T,C>,Poly<T,C>>(new Poly.Times<>(e.s, e.t), e.s);
	}

	@Override
	public Pair<Poly<T, C>, Poly<T, C>> visit(
			Map<String, Pair<Poly<T, C>, Poly<T, C>>> env, Snd<T, C, D> e) {
		polyChecker.of(e.s);
		polyChecker.of(e.t);
		return new Pair<Poly<T,C>,Poly<T,C>>(new Poly.Times<>(e.s, e.t), e.t);
	}

	@Override
	public Pair<Poly<T, C>, Poly<T, C>> visit(
			Map<String, Pair<Poly<T, C>, Poly<T, C>>> env, Inl<T, C, D> e) {
		polyChecker.of(e.s);
		polyChecker.of(e.t);
		return new Pair<Poly<T,C>,Poly<T,C>>(e.s, new Poly.Plus<>(e.s, e.t));

	}

	@Override
	public Pair<Poly<T, C>, Poly<T, C>> visit(
			Map<String, Pair<Poly<T, C>, Poly<T, C>>> env, Inr<T, C, D> e) {
		polyChecker.of(e.s);
		polyChecker.of(e.t);
		return new Pair<Poly<T,C>,Poly<T,C>>(e.t, new Poly.Plus<>(e.s, e.t));

	}

	@Override
	public Pair<Poly<T, C>, Poly<T, C>> visit(
			Map<String, Pair<Poly<T, C>, Poly<T, C>>> env, Apply<T, C, D> e) {
		polyChecker.of(e.s);
		polyChecker.of(e.t);
		return new Pair<Poly<T,C>,Poly<T,C>>(new Poly.Times<>(new Poly.Exp<>(e.s, e.t), e.t), e.s);
	}

	@Override
	public Pair<Poly<T, C>, Poly<T, C>> visit(
			Map<String, Pair<Poly<T, C>, Poly<T, C>>> env, Curry<T, C, D> e) {
		Pair<Poly<T, C>, Poly<T, C>> t = e.f.accept(env, this);
		if (!(t.first instanceof Poly.Times)) {
			throw new RuntimeException("Expected product argument in " + e + " but received " + t.first);
		}
		Poly.Times<T,C> t0 = (Poly.Times<T,C>) t.first;
		return new Pair<Poly<T,C>,Poly<T,C>>(t0.a, new Poly.Exp<>(t.second, t0.b));
	}

	@Override
	public Pair<Poly<T, C>, Poly<T, C>> visit(
			Map<String, Pair<Poly<T, C>, Poly<T, C>>> env, Eq<T, C, D> e) {
		T t = polyChecker.of(e.t);
		return new Pair<Poly<T,C>,Poly<T,C>>(new Poly.Times<>(e.t, e.t), new Poly.Two<T, C>(t));		
	}

	@Override
	public Pair<Poly<T, C>, Poly<T, C>> visit(
			Map<String, Pair<Poly<T, C>, Poly<T, C>>> env, Case<T, C, D> e) {
		Pair<Poly<T, C>, Poly<T, C>> t1 = e.l.accept(env, this);
		Pair<Poly<T, C>, Poly<T, C>> t2 = e.r.accept(env, this);
		if (!t1.second.equals(t2.second)) {
			throw new RuntimeException("Not common co-domain on " + e + ": given " + t1.second + " and " + t2.second);
		}
		return new Pair<Poly<T,C>,Poly<T,C>>(new Poly.Plus<>(t1.first, t2.first), t2.second);
	}

	@Override
	public Pair<Poly<T, C>, Poly<T, C>> visit(
			Map<String, Pair<Poly<T, C>, Poly<T, C>>> env, Prod<T, C, D> e) {
		Pair<Poly<T, C>, Poly<T, C>> t1 = e.l.accept(env, this);
		Pair<Poly<T, C>, Poly<T, C>> t2 = e.r.accept(env, this);
		if (!t1.first.equals(t2.first)) {
			throw new RuntimeException("Not common domain on " + e + ": given " + t1.first + " and " + t2.first);
		}
		return new Pair<Poly<T,C>,Poly<T,C>>(t1.first, new Poly.Times<>(t1.second, t2.second));
	}

}
