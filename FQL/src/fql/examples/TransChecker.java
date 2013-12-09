package fql.examples;

import java.util.LinkedList;
import java.util.List;

import fql.FQLException;
import fql.Pair;
import fql.decl.FQLProgram;
import fql.decl.InstExp;
import fql.decl.Instance;
import fql.decl.SigExp;
import fql.decl.Signature;
import fql.decl.TransExp.Case;
import fql.decl.TransExp.Comp;
import fql.decl.TransExp.Const;
import fql.decl.TransExp.Delta;
import fql.decl.TransExp.FF;
import fql.decl.TransExp.Fst;
import fql.decl.TransExp.FullSigma;
import fql.decl.TransExp.Id;
import fql.decl.TransExp.Inl;
import fql.decl.TransExp.Inr;
import fql.decl.TransExp.Pi;
import fql.decl.TransExp.Prod;
import fql.decl.TransExp.Relationalize;
import fql.decl.TransExp.Sigma;
import fql.decl.TransExp.Snd;
import fql.decl.TransExp.Squash;
import fql.decl.TransExp.TT;
import fql.decl.TransExp.TransExpVisitor;
import fql.decl.TransExp.Var;
import fql.decl.Transform;

public class TransChecker implements TransExpVisitor<Pair<String, String>, FQLProgram>{

	List<String> seen = new LinkedList<>();
	
	@Override
	public Pair<String, String> visit(FQLProgram env, Id e) {
		if (!env.insts.keySet().contains(e.t)) {
			throw new RuntimeException("Missing instance " + e.t + " in " + e);
		}
		return new Pair<>(e.t, e.t);
	}

	@Override
	public Pair<String, String> visit(FQLProgram env, Comp e) {
		List<String> x = new LinkedList<>(seen);
		Pair<String, String> a = e.l.accept(env, this);
		seen = x;
		Pair<String, String> b = e.r.accept(env, this);
		seen = x;
		if (!a.second.equals(b.first)) {
			throw new RuntimeException("Not equal in " + e + ": " + a.second + " and " + b.second);
		}
		return new Pair<>(a.first, b.second);
	}

	@Override
	public Pair<String, String> visit(FQLProgram env, Var e) {
		if (seen.contains(e.v)) {
			throw new RuntimeException("Circular transform " + e);
		}
		if (env.transforms.get(e.v) == null) {
			throw new RuntimeException("Transform not found " + e);
		}
		seen.add(e.v);
		return env.transforms.get(e.v).accept(env, this);
	}

	@Override
	public Pair<String, String> visit(FQLProgram env, Const e) {
		InstExp src = env.insts.get(e.src);
		if (src == null) {
			throw new RuntimeException("Missing instance " + e.src);
		}
		InstExp dst = env.insts.get(e.dst);
		if (dst == null) {
			throw new RuntimeException("Missing instance " + e.src);
		}
		if (!(src instanceof InstExp.Const)) {
			throw new RuntimeException(e.src + " is not a constant.");
		}
		if (!(dst instanceof InstExp.Const)) {
			throw new RuntimeException(e.dst + " is not a constant.");
		}
		InstExp.Const src0 = (InstExp.Const) src;
		InstExp.Const dst0 = (InstExp.Const) dst;

		SigExp srct = src0.type(env);
		SigExp dstt = dst0.type(env);
		if (!srct.equals(dstt)) {
			throw new RuntimeException("Instances not of same type on " + e + " are " + srct + " and " + dstt);
		}

		Signature sig = srct.toSig(env);
			List<Pair<String, List<Pair<Object, Object>>>> bbb = e.objs;
		try {	
			new Transform(new Instance(sig, src0.data), new Instance(sig, dst0.data), bbb );
		} catch (FQLException fe) {
			fe.printStackTrace();
			throw new RuntimeException(fe.getLocalizedMessage());
		}
		
		return new Pair<>(e.src, e.dst);
	}

	@Override
	public Pair<String, String> visit(FQLProgram env, TT e) {
		InstExp x = env.insts.get(e.obj);
		if (x == null) {
			throw new RuntimeException("Missing " + e.obj + " in " + e);
		}
		if (!(x instanceof InstExp.One)) {
			throw new RuntimeException(e.obj + " is not a unit: " + x);
		}
		//InstExp.One y = (InstExp.One) x;
		
		InstExp z = env.insts.get(e.tgt);
		if (z == null) {
			throw new RuntimeException("Missing " + e.tgt + " in " + e);
		}
		SigExp xt = x.type(env);
		SigExp yt = z.type(env);
		if (!xt.equals(yt)) {
			throw new RuntimeException("Instances have different types in " + e + ": " + xt + " and " + yt);
		}
		return new Pair<>(e.tgt, e.obj);		
	}

	@Override
	public Pair<String, String> visit(FQLProgram env, FF e) {
		InstExp x = env.insts.get(e.obj);
		if (x == null) {
			throw new RuntimeException("Missing " + e.obj + " in " + e);
		}
		if (!(x instanceof InstExp.Zero)) {
			throw new RuntimeException(e.obj + " is not void: " + x);
		}
		//InstExp.One y = (InstExp.One) x;
		
		InstExp z = env.insts.get(e.tgt);
		if (z == null) {
			throw new RuntimeException("Missing " + e.tgt + " in " + e);
		}
		SigExp xt = x.type(env);
		SigExp yt = z.type(env);
		if (!xt.equals(yt)) {
			throw new RuntimeException("Instances have different types in " + e + ": " + xt + " and " + yt);
		}
		return new Pair<>(e.obj, e.tgt);
	}

	@Override
	public Pair<String, String> visit(FQLProgram env, Fst e) {
		InstExp x = env.insts.get(e.obj);
		if (x == null) {
			throw new RuntimeException("Missing " + e.obj + " in " + e);
		}
		if (!(x instanceof InstExp.Times)) {
			throw new RuntimeException(e.obj + " is not a times: " + x);
		}
		InstExp.Times y = (InstExp.Times) x;
		
		return new Pair<>(e.obj, y.a);
	}

	@Override
	public Pair<String, String> visit(FQLProgram env, Snd e) {
		InstExp x = env.insts.get(e.obj);
		if (x == null) {
			throw new RuntimeException("Missing " + e.obj + " in " + e);
		}
		if (!(x instanceof InstExp.Times)) {
			throw new RuntimeException(e.obj + " is not a times: " + x);
		}
		InstExp.Times y = (InstExp.Times) x;
		
		return new Pair<>(e.obj, y.b);
	}

	@Override
	public Pair<String, String> visit(FQLProgram env, Inl e) {
		InstExp x = env.insts.get(e.obj);
		if (x == null) {
			throw new RuntimeException("Missing " + e.obj + " in " + e);
		}
		if (!(x instanceof InstExp.Plus)) {
			throw new RuntimeException(e.obj + " is not a plus: " + x);
		}
		InstExp.Plus y = (InstExp.Plus) x;
		
		return new Pair<>(y.a, e.obj);
	}

	@Override
	public Pair<String, String> visit(FQLProgram env, Inr e) {
		InstExp x = env.insts.get(e.obj);
		if (x == null) {
			throw new RuntimeException("Missing " + e.obj + " in " + e);
		}
		if (!(x instanceof InstExp.Plus)) {
			throw new RuntimeException(e.obj + " is not a plus: " + x);
		}
		InstExp.Plus y = (InstExp.Plus) x;
		
		return new Pair<>(y.b, e.obj);
	}

	@Override
	public Pair<String, String> visit(FQLProgram env, Case e) {
		InstExp x = env.insts.get(e.obj);
		if (x == null) {
			throw new RuntimeException("Missing " + e.obj + " in " + e);
		}
		if (!(x instanceof InstExp.Plus)) {
			throw new RuntimeException(e.obj + " is not a plus: " + x);
		}
//		InstExp.Plus y = (InstExp.Plus) x;
		
		Pair<String, String> a = e.l.accept(env, this);
		Pair<String, String> b = e.r.accept(env, this);

		if (!a.second.equals(b.second)) {
			throw new RuntimeException("Codomain mismatch: " + e + " with " + a.second + " and " + b.second);
		}

		return new Pair<>(e.obj, a.second);
	}

	@Override
	public Pair<String, String> visit(FQLProgram env, Prod e) {
		InstExp x = env.insts.get(e.obj);
		if (x == null) {
			throw new RuntimeException("Missing " + e.obj + " in " + e);
		}
		if (!(x instanceof InstExp.Times)) {
			throw new RuntimeException(e.obj + " is not a times: " + x);
		}
		//InstExp.Times y = (InstExp.Times) x;
		
		Pair<String, String> a = e.l.accept(env, this);
		Pair<String, String> b = e.r.accept(env, this);

		if (!a.first.equals(b.first)) {
			throw new RuntimeException("Domain mismatch: " + e + " with " + a.first + " and " + b.first);
		}
		
		return new Pair<>(a.first, e.obj);
	}

	@Override
	public Pair<String, String> visit(FQLProgram env, Delta e) {
		Pair<String, String> ht = e.h.type(env);
		InstExp i1 = env.insts.get(e.src);		
		if (!(i1 instanceof InstExp.Delta)) {
			throw new RuntimeException(i1 + " is not a delta in " + e);
		}
		String i1x = ((InstExp.Delta) i1).I;

		if (!i1x.equals(ht.first)) {
 			throw new RuntimeException("Source mismatch on " + e + ": " + i1x + " and " + ht.first);
		}
		InstExp i2 = env.insts.get(e.dst);		
		if (!(i2 instanceof InstExp.Delta)) {
			throw new RuntimeException(i2 + " is not a delta in " + e);
		}
		String i2x = ((InstExp.Delta) i2).I;

		if (!i2x.equals(ht.second)) {
 			throw new RuntimeException("Target mismatch on " + e + ": " + i2x + " and " + ht.second);
		}
		return new Pair<>(e.src, e.dst);
	}

	@Override
	public Pair<String, String> visit(FQLProgram env, Sigma e) {
		Pair<String, String> ht = e.h.type(env);
		InstExp i1 = env.insts.get(e.src);		
		if (!(i1 instanceof InstExp.Sigma)) {
			throw new RuntimeException(i1 + " is not a sigma in " + e);
		}
		String i1x = ((InstExp.Sigma) i1).I;

		if (!i1x.equals(ht.first)) {
 			throw new RuntimeException("Source mismatch on " + e + ": " + i1x + " and " + ht.first);
		}
		InstExp i2 = env.insts.get(e.dst);		
		if (!(i2 instanceof InstExp.Sigma)) {
			throw new RuntimeException(i2 + " is not a sigma in " + e);
		}
		String i2x = ((InstExp.Sigma) i2).I;

		if (!i2x.equals(ht.second)) {
 			throw new RuntimeException("Target mismatch on " + e + ": " + i2x + " and " + ht.second);
		}
		return new Pair<>(e.src, e.dst);
	}

	@Override
	public Pair<String, String> visit(FQLProgram env, FullSigma e) {
		Pair<String, String> ht = e.h.type(env);
		InstExp i1 = env.insts.get(e.src);		
		if (!(i1 instanceof InstExp.FullSigma)) {
			throw new RuntimeException(i1 + " is not a full sigma in " + e);
		}
		String i1x = ((InstExp.FullSigma) i1).I;

		if (!i1x.equals(ht.first)) {
 			throw new RuntimeException("Source mismatch on " + e + ": " + i1x + " and " + ht.first);
		}
		InstExp i2 = env.insts.get(e.dst);		
		if (!(i2 instanceof InstExp.FullSigma)) {
			throw new RuntimeException(i2 + " is not a fullsigma in " + e);
		}
		String i2x = ((InstExp.FullSigma) i2).I;

		if (!i2x.equals(ht.second)) {
 			throw new RuntimeException("Target mismatch on " + e + ": " + i2x + " and " + ht.second);
		}
		return new Pair<>(e.src, e.dst);
	}

	@Override
	public Pair<String, String> visit(FQLProgram env, Pi e) {
		Pair<String, String> ht = e.h.type(env);
		InstExp i1 = env.insts.get(e.src);		
		if (!(i1 instanceof InstExp.Pi)) {
			throw new RuntimeException(i1 + " is not a pi in " + e);
		}
		String i1x = ((InstExp.Pi) i1).I;
		if (!i1x.equals(ht.first)) {
 			throw new RuntimeException("Source mismatch on " + e + ": " + i1x + " and " + ht.first);
		}
		InstExp i2 = env.insts.get(e.dst);		
		if (!(i2 instanceof InstExp.Pi)) {
			throw new RuntimeException(i2 + " is not a pi in " + e);
		}
		String i2x = ((InstExp.Pi) i2).I;

		if (!i2x.equals(ht.second)) {
 			throw new RuntimeException("Target mismatch on " + e + ": " + i2x + " and " + ht.second);
		}
		return new Pair<>(e.src, e.dst);
	}

	@Override
	public Pair<String, String> visit(FQLProgram env, Relationalize e) {
		Pair<String, String> ht = e.h.type(env);
		InstExp i1 = env.insts.get(e.src);		
		if (!(i1 instanceof InstExp.Relationalize)) {
			throw new RuntimeException(i1 + " is not a relationalize in " + e);
		}
		String i1x = ((InstExp.Relationalize) i1).I;

		if (!i1x.equals(ht.first)) {
 			throw new RuntimeException("Source mismatch on " + e + ": " + i1x + " and " + ht.first);
		}
		InstExp i2 = env.insts.get(e.dst);		
		if (!(i2 instanceof InstExp.Relationalize)) {
			throw new RuntimeException(i2 + " is not a relationalize in " + e);
		}
		String i2x = ((InstExp.Relationalize) i2).I;

		if (!i2x.equals(ht.second)) {
 			throw new RuntimeException("Target mismatch on " + e + ": " + i2x + " and " + ht.second);
		}
		return new Pair<>(e.src, e.dst);
	}

	@Override
	public Pair<String, String> visit(FQLProgram env, Squash e) {
		InstExp s = env.insts.get(e.src);
		if (!(s instanceof fql.decl.InstExp.Relationalize)) {
			throw new RuntimeException("Not a relationalize: " + e);
		}
		fql.decl.InstExp.Relationalize xxx = (fql.decl.InstExp.Relationalize) s;
		return new Pair<>(xxx.I, e.src);
	}

}
