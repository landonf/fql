package fql.decl;

import java.util.Map;

import fql.Fn;
import fql.Pair;
import fql.decl.Poly.Const;
import fql.decl.Poly.Exp;
import fql.decl.Poly.One;
import fql.decl.Poly.Plus;
import fql.decl.Poly.Times;
import fql.decl.Poly.Two;
import fql.decl.Poly.Var;
import fql.decl.Poly.Zero;

public class PolyChecker<T,C> implements PolyVisitor<T,C,T,Map<String,T>> {

	private Fn<Pair<T, C>, T> checkConst;

	public PolyChecker(Fn<Pair<T,C>, T> checkConst) {
		this.checkConst = checkConst;
	}
	
	@Override
	public T visit(Map<String, T> env, Zero<T, C> e) {
		return e.t;
	}

	@Override
	public T visit(Map<String, T> env, One<T, C> e) {
		return e.t;
	}

	@Override
	public T visit(Map<String, T> env, Plus<T, C> e) {
		T t1 = e.a.accept(env, this);
		T t2 = e.b.accept(env, this);
		if (!t1.equals(t2)) {
			throw new RuntimeException("Type mismatch on " + e + ": " + t1 + " and " + t2);
		}
		return t1;
	}

	@Override
	public T visit(Map<String, T> env, Times<T, C> e) {
		T t1 = e.a.accept(env, this);
		T t2 = e.b.accept(env, this);
		if (!t1.equals(t2)) {
			throw new RuntimeException("Type mismatch on " + e + ": " + t1 + " and " + t2);
		}
		return t1;
	}

	@Override
	public T visit(Map<String, T> env, Exp<T, C> e) {
		T t1 = e.a.accept(env, this);
		T t2 = e.b.accept(env, this);
		if (!t1.equals(t2)) {
			throw new RuntimeException("Type mismatch on " + e + ": " + t1 + " and " + t2);
		}
		return t1;
	}

	@Override
	public T visit(Map<String, T> env, Two<T, C> e) {
		return e.t;
	}

	@Override
	public T visit(Map<String, T> env, Var<T, C> e) {
		T t = env.get(e.v);
		if (t == null) {
			throw new RuntimeException("Unknown var: " + e);
		}
		return t;
	}

	@Override
	public T visit(Map<String, T> env, Const<T, C> e) {
		return checkConst.of(new Pair<>(e.t, e.c));
	}

}
