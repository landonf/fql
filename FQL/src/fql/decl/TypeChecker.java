package fql.decl;

import fql.Fn;
import fql.Pair;
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
import fql.decl.Poly.Exp;
import fql.decl.Poly.One;
import fql.decl.Poly.Plus;
import fql.decl.Poly.Times;
import fql.decl.Poly.Two;
import fql.decl.Poly.Zero;

// inline everything, so no vars
//inline phase
public class TypeChecker<T, C, D> implements CamlVisitor<T, C, D, Pair<Poly<T,C>, Poly<T,C>>, TypeChecker.TypingCtx<T, C, D>>,
  PolyVisitor<T, C, T, TypeChecker.TypingCtx<T, C, D>> {
 

	public static class TypingCtx<T, C, D> {
		Fn<Const<T, C, D>, Unit> f;
		Fn<Poly.Const<T, C>, Unit> g;
	}
	
	@Override
	public Pair<Poly<T,C>, Poly<T,C>> visit(TypingCtx<T, C, D> env, Var<T, C, D> e) {
		throw new TypeCheckError("Encountered unbound variable " + e);
	}

	@Override
	public Pair<Poly<T,C>, Poly<T,C>> visit(TypingCtx<T, C, D> env, TT<T, C, D> e) {
		return new Pair<Poly<T,C>, Poly<T,C>>(e.t, new Poly.One<T, C>(e.t.accept(env, this)));
	}

	@Override
	public Pair<Poly<T,C>, Poly<T,C>> visit(TypingCtx<T, C, D> env, FF<T, C, D> e) {
		return new Pair<Poly<T,C>, Poly<T,C>>(new Poly.Zero<T, C>(e.t.accept(env, this)), e.t);
	}

	@Override
	public Pair<Poly<T,C>, Poly<T,C>> visit(TypingCtx<T, C, D> env, Fst<T, C, D> e) {
		return new Pair<Poly<T,C>, Poly<T,C>>(new Poly.Times<T, C>(e.s, e.t), e.s);
	}

	@Override
	public Pair<Poly<T,C>, Poly<T,C>> visit(TypingCtx<T, C, D> env, Snd<T, C, D> e) {
		return new Pair<Poly<T,C>, Poly<T,C>>(new Poly.Times<T, C>(e.s, e.t), e.t);
	}

	@Override
	public Pair<Poly<T,C>, Poly<T,C>> visit(TypingCtx<T, C, D> env, Inl<T, C, D> e) {
		return new Pair<Poly<T,C>, Poly<T,C>>(e.s, new Poly.Times<T, C>(e.s, e.t));
	}

	@Override
	public Pair<Poly<T,C>, Poly<T,C>> visit(TypingCtx<T, C, D> env, Inr<T, C, D> e) {
		return new Pair<Poly<T,C>, Poly<T,C>>(e.t, new Poly.Times<T, C>(e.s, e.t));
	}

	@Override
	public Pair<Poly<T,C>, Poly<T,C>> visit(TypingCtx<T, C, D> env, Apply<T, C, D> e) {
		return new Pair<Poly<T,C>, Poly<T,C>>(new Poly.Times<T, C>(new Poly.Exp<T,C>(e.s, e.t), e.t), e.s);
	}

	@Override
	public Pair<Poly<T,C>, Poly<T,C>> visit(TypingCtx<T, C, D> env, Curry<T, C, D> e) {
		try {
			Pair<Poly<T,C>, Poly<T,C>> t = e.f.accept(env,  this);
			Poly<T,C> src = t.first;
			Poly<T,C> dst = t.second;
			if (! (src instanceof Poly.Times)) {
				throw new TypeCheckError("Excepted product type for " + e + ", but computed " + src);
			}
			Poly.Times<T, C> t0 = (Poly.Times<T, C>) src;
			
			return new Pair<Poly<T,C>,Poly<T,C>>(t0.a, new Poly.Exp<T, C>(dst, t0.b));
		} catch (TypeCheckError ex) {
			ex.add("In " + e);
			throw ex;
		}
	}

	@Override
	public Pair<Poly<T,C>, Poly<T,C>> visit(TypingCtx<T, C, D> env, Eq<T, C, D> e) {
		return new Pair<Poly<T,C>,Poly<T,C>>(new Poly.Times<T, C>(e.t, e.t), new Poly.Two<T, C>((e.t.accept(env, this))));
	}

	@Override
	public Pair<Poly<T,C>, Poly<T,C>> visit(TypingCtx<T, C, D> env, Case<T, C, D> e) {
		try {
			Pair<Poly<T,C>, Poly<T,C>> t1 = e.l.accept(env, this);
			Pair<Poly<T,C>, Poly<T,C>> t2 = e.r.accept(env, this);
			Poly<T,C> src1 = t1.first;
			Poly<T,C> src2 = t2.first;
			if (!src1.equals(src2)) {
				throw new TypeCheckError("Sources do not match: " + src1 + " and " + src2);
			}
			return new Pair<Poly<T,C>, Poly<T,C>>(src1, new Poly.Times<>(t1.second, t2.second));
		} catch (TypeCheckError ex) {
			ex.add("In " + e);
			throw ex;
		}
	}

	@Override
	public Pair<Poly<T,C>, Poly<T,C>> visit(TypingCtx<T, C, D> env, Prod<T, C, D> e) {
		try {
			Pair<Poly<T,C>, Poly<T,C>> t1 = e.l.accept(env, this);
			Pair<Poly<T,C>, Poly<T,C>> t2 = e.r.accept(env, this);
			Poly<T,C> src1 = t1.first;
			Poly<T,C> src2 = t2.first;
			if (!src1.equals(src2)) {
				throw new TypeCheckError("Sources do not match: " + src1 + " and " + src2);
			}
			return new Pair<Poly<T,C>, Poly<T,C>>(src1, new Poly.Times<>(t1.second, t2.second));
		} catch (TypeCheckError ex) {
			ex.add("In " + e);
			throw ex;
		}
	}

	@Override
	public Pair<Poly<T,C>, Poly<T,C>> visit(TypingCtx<T, C, D> env, Const<T, C, D> e) {
		env.f.of(e);
		return new Pair<Poly<T,C>, Poly<T,C>>(e.src, e.dst);
	}

	@Override
	public Pair<Poly<T,C>, Poly<T,C>> visit(TypingCtx<T, C, D> env, Id<T, C, D> e) {
		return new Pair<Poly<T,C>, Poly<T,C>>(e.t, e.t);
	}

	@Override
	public Pair<Poly<T,C>, Poly<T,C>> visit(TypingCtx<T, C, D> env, Comp<T, C, D> e) {
		try {
			Pair<Poly<T,C>, Poly<T,C>> t1 = e.l.accept(env, this);
			Pair<Poly<T,C>, Poly<T,C>> t2 = e.r.accept(env, this);
			Poly<T,C> src1 = t1.first;
			Poly<T,C> dst1 = t1.second;
			Poly<T,C> src2 = t2.first;
			Poly<T,C> dst2 = t2.second;
			if (!dst1.equals(src2)) {
				throw new TypeCheckError("Source and Target do not match: " + dst1 + " and " + src2);
			}
			return new Pair<Poly<T,C>, Poly<T,C>>(src1, dst2);
		} catch (TypeCheckError ex) {
			ex.add("In " + e);
			throw ex;
		}
	}

	//a * (b + c) = (a * b) + (a * c)
	@Override
	public Pair<Poly<T,C>, Poly<T,C>> visit(TypingCtx<T, C, D> env, Dist1<T, C, D> e) {
		Poly<T,C> a = e.a;
		Poly<T,C> b = e.b;
		Poly<T,C> c = e.c;
		
		Poly<T,C> lhs = new Poly.Times<T, C>(a, new Poly.Plus<>(b, c));
		Poly<T,C> rhs = new Poly.Plus<T, C>(new Poly.Times<T, C>(a, b), new Poly.Times<T, C>(a, c));
		return new Pair<Poly<T,C>, Poly<T,C>>(lhs, rhs); 
	}

	@Override
	public Pair<Poly<T,C>, Poly<T,C>> visit(TypingCtx<T, C, D> env, Dist2<T, C, D> e) {
		Poly<T,C> a = e.a;
		Poly<T,C> b = e.b;
		Poly<T,C> c = e.c;
		
		Poly<T,C> lhs = new Poly.Times<T, C>(a, new Poly.Plus<>(b, c));
		Poly<T,C> rhs = new Poly.Plus<T, C>(new Poly.Times<T, C>(a, b), new Poly.Times<T, C>(a, c));
		return new Pair<Poly<T,C>, Poly<T,C>>(rhs, lhs); 
	}

	///////
	
	@Override
	public T visit(TypingCtx<T, C, D> env, Zero<T, C> e) {
		return e.t;
	}

	@Override
	public T visit(TypingCtx<T, C, D> env, One<T, C> e) {
		return e.t;
	}

	@Override
	public T visit(TypingCtx<T, C, D> env, Plus<T, C> e) {
		try {
			T ta = e.a.accept(env, this);
			T tb = e.b.accept(env, this);
			if (!ta.equals(tb)) {
				throw new TypeCheckError("Cannot plus " + ta + " and " + tb);
			}
			return ta;
		} catch (TypeCheckError ex) {
			ex.add("In " + e);
			throw ex;
		}
	}

	@Override
	public T visit(TypingCtx<T, C, D> env, Times<T, C> e) {
		try {
			T ta = e.a.accept(env, this);
			T tb = e.b.accept(env, this);
			if (!ta.equals(tb)) {
				throw new TypeCheckError("Cannot times " + ta + " and " + tb);
			}
			return ta;
		} catch (TypeCheckError ex) {
			ex.add("In " + e);
			throw ex;
		}

	}

	@Override
	public T visit(TypingCtx<T, C, D> env, Exp<T, C> e) {
		try {
			T ta = e.a.accept(env, this);
			T tb = e.b.accept(env, this);
			if (!ta.equals(tb)) {
				throw new TypeCheckError("Cannot exp " + ta + " and " + tb);
			}
			return ta;
		} catch (TypeCheckError ex) {
			ex.add("In " + e);
			throw ex;
		}

	}

	@Override
	public T visit(TypingCtx<T, C, D> env, Two<T, C> e) {
		return e.t;
	}

	@Override
	public T visit(TypingCtx<T, C, D> env, fql.decl.Poly.Var<T, C> e) {
		throw new TypeCheckError("Encountered unbound variable " + e);
	}

	@Override
	public T visit(TypingCtx<T, C, D> env, fql.decl.Poly.Const<T, C> e) {
		env.g.of(e);
		return e.t;
	}

}
