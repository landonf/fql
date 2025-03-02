package fql.examples;

import java.util.LinkedList;
import java.util.List;

import fql.FQLException;
import fql.Pair;
import fql.decl.FQLProgram;
import fql.decl.InstExp;
import fql.decl.InstExp.Times;
import fql.decl.Instance;
import fql.decl.SigExp;
import fql.decl.Signature;
import fql.decl.TransExp;
import fql.decl.TransExp.And;
import fql.decl.TransExp.Bool;
import fql.decl.TransExp.Case;
import fql.decl.TransExp.Chi;
import fql.decl.TransExp.Comp;
import fql.decl.TransExp.Const;
import fql.decl.TransExp.Coreturn;
import fql.decl.TransExp.Delta;
import fql.decl.TransExp.External;
import fql.decl.TransExp.FF;
import fql.decl.TransExp.Fst;
import fql.decl.TransExp.FullSigma;
import fql.decl.TransExp.Id;
import fql.decl.TransExp.Implies;
import fql.decl.TransExp.Inl;
import fql.decl.TransExp.Inr;
import fql.decl.TransExp.Not;
import fql.decl.TransExp.Or;
import fql.decl.TransExp.Pi;
import fql.decl.TransExp.Prod;
import fql.decl.TransExp.Relationalize;
import fql.decl.TransExp.Return;
import fql.decl.TransExp.Sigma;
import fql.decl.TransExp.Snd;
import fql.decl.TransExp.Squash;
import fql.decl.TransExp.TT;
import fql.decl.TransExp.TransCurry;
import fql.decl.TransExp.TransEval;
import fql.decl.TransExp.TransExpVisitor;
import fql.decl.TransExp.TransIso;
import fql.decl.TransExp.UnChi;
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
		if (seen.contains(e.h)) {
			throw new RuntimeException("Circular transform " + e);
		}
		seen.add(e.h);
		if (env.transforms.get(e.h) == null) {
			throw new RuntimeException("Transform not found " + e);
		}
		Pair<String, String> ht = env.transforms.get(e.h).type(env);
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

	@Override
	public Pair<String, String> visit(FQLProgram env, External e) {
		InstExp src = env.insts.get(e.src);
		if (src == null) {
			throw new RuntimeException("Missing instance " + e.src);
		}
		InstExp dst = env.insts.get(e.dst);
		if (dst == null) {
			throw new RuntimeException("Missing instance " + e.src);
		}
		if (!(src instanceof InstExp.External)) {
			throw new RuntimeException(e.src + " is not external.");
		}
		if (!(dst instanceof InstExp.External)) {
			throw new RuntimeException(e.dst + " is not external.");
		}
		InstExp.External src0 = (InstExp.External) src;
		InstExp.External dst0 = (InstExp.External) dst;

		SigExp srct = src0.type(env);
		SigExp dstt = dst0.type(env);
		if (!srct.equals(dstt)) {
			throw new RuntimeException("Instances not of same type on " + e + " are " + srct + " and " + dstt);
		}
		
		return new Pair<>(e.src, e.dst);

	}

	@Override
	public Pair<String, String> visit(FQLProgram env, Return e) {
		InstExp i1 = env.insts.get(e.inst);
		if (i1 == null) {
			throw new RuntimeException("Missing instance " + e.inst);
		}
		if (i1 instanceof InstExp.Delta) {
			InstExp i2 = env.insts.get(((InstExp.Delta) i1).I); //can't be null
			if (i2 instanceof InstExp.Sigma) {
				if (!((InstExp.Sigma) i2).F.equals(((InstExp.Delta) i1).F)) {
					throw new RuntimeException();
				}
				return new Pair<>(((InstExp.Sigma) i2).I, e.inst);
			} else if (i2 instanceof InstExp.FullSigma) {
				if (!((InstExp.FullSigma) i2).F.equals(((InstExp.Delta) i1).F)) {
					throw new RuntimeException("Mappings not equal.");
				}
				return new Pair<>(((InstExp.FullSigma) i2).I, e.inst);
			} else {
				throw new RuntimeException("Not a delta of a sigma (or SIGMA).");
			} 
		} else if (i1 instanceof InstExp.Pi) {	 
			InstExp i2 = env.insts.get(((InstExp.Pi) i1).I); //can't be null
			if (i2 instanceof InstExp.Delta) {
				if (!((InstExp.Delta) i2).F.equals(((InstExp.Pi) i1).F)) {
					throw new RuntimeException();
				}
				return new Pair<>(((InstExp.Delta) i2).I, e.inst);
			} else {
				throw new RuntimeException("Not a pi of a delta.");
			} 
		} else {
			throw new RuntimeException("Can only return a delta of a sigma or a pi of a delta."); 
		}
	}

	@Override
	public Pair<String, String> visit(FQLProgram env, Coreturn e) {
		InstExp i1 = env.insts.get(e.inst);
		if (i1 == null) {
			throw new RuntimeException("Missing instance " + e.inst);
		}
		if (i1 instanceof InstExp.Sigma) {
			InstExp i2 = env.insts.get(((InstExp.Sigma) i1).I); //can't be null
			if (i2 instanceof InstExp.Delta) {
				if (!((InstExp.Delta) i2).F.equals(((InstExp.Sigma) i1).F)) {
					throw new RuntimeException("Mappings not equal.");
				}
				return new Pair<>(e.inst, ((InstExp.Delta) i2).I);
			} else {
				throw new RuntimeException("Not a sigma of a delta.");
			} 
		} else if (i1 instanceof InstExp.FullSigma) {
			e.isFull = true;
			InstExp i2 = env.insts.get(((InstExp.FullSigma) i1).I); //can't be null
			if (i2 instanceof InstExp.Delta) {
				if (!((InstExp.Delta) i2).F.equals(((InstExp.FullSigma) i1).F)) {
					throw new RuntimeException("Mappings not equal.");
				}
				return new Pair<>(e.inst, ((InstExp.Delta) i2).I);
			} else {
				throw new RuntimeException("Not a sigma of a delta.");
			} 
		}
		else if (i1 instanceof InstExp.Delta) {
			InstExp i2 = env.insts.get(((InstExp.Delta) i1).I); //can't be null
			if (i2 instanceof InstExp.Pi) {
				if (!((InstExp.Pi) i2).F.equals(((InstExp.Delta) i1).F)) {
					throw new RuntimeException("Mappings not equal.");
				}
				return new Pair<>(e.inst, ((InstExp.Pi) i2).I);
			} else {
				throw new RuntimeException("Not a delta of a pi.");
			} 			
		} else {
			throw new RuntimeException("Can only coreturn a sigma of a delta or a delta of a pi."); 
		}
	}

	@Override
	public Pair<String, String> visit(FQLProgram env, TransEval e) {
		InstExp k = env.insts.get(e.inst);
		if (k == null) {
			throw new RuntimeException("Missing instance: " + e.inst);
		}
		if (!(k instanceof InstExp.Times)) {
			throw new RuntimeException("Not a product: " + k + " in " + e);
		}
		InstExp.Times t = (InstExp.Times) k;
		InstExp v = env.insts.get(t.a);
		if (!(v instanceof InstExp.Exp)) {
			throw new RuntimeException("Not an exponential: " + v + " in " + e);
		}
		InstExp.Exp i = (InstExp.Exp) v;
		if (!(t.b.equals(i.b))) {
			throw new RuntimeException("Exponent and product do not match in " + e);
		}
		return new Pair<>(e.inst, i.a);
	}

	@Override
	public Pair<String, String> visit(FQLProgram env, TransCurry e) {
		InstExp inst = env.insts.get(e.inst);
		if (inst == null) {
			throw new RuntimeException("Missing instance: " + inst);
		}
		if (!(inst instanceof InstExp.Exp)) {
			throw new RuntimeException("Instance is not an exponential");
		}
		InstExp.Exp i = (InstExp.Exp) inst;

		if (seen.contains(e.trans)) {
			throw new RuntimeException("Circular transform " + e);
		}
		seen.add(e.trans);
		Pair<String, String> k = env.transforms.get(e.trans).accept(env, this);
		InstExp ab = env.insts.get(k.first);
		if (!(ab instanceof InstExp.Times)) {
			throw new RuntimeException("Source is not a product");
		}
		Times t = (InstExp.Times) ab;
		
		if (!i.a.equals(k.second)) {
			throw new RuntimeException("Bases do not match");
		}
		if (!i.b.equals(t.b)) {
			throw new RuntimeException("Exponents do not match");
		}
		
		return new Pair<>(t.a, e.inst);
	}

	@Override
	public Pair<String, String> visit(FQLProgram env, TransIso e) {
		InstExp l = env.insts.get(e.l);
		if (l == null) {
			throw new RuntimeException("Missing instance: " + l);
		}
		InstExp r = env.insts.get(e.r);
		if (r == null) {
			throw new RuntimeException("Missing instance: " + r);
		}
		if (e.lToR) {
			return new Pair<>(e.l, e.r);
		} else {
			return new Pair<>(e.r, e.l);
		}
	}

	@Override
	public Pair<String, String> visit(FQLProgram env, Bool e) {
		InstExp u = env.insts.get(e.unit);
		if (u == null) {
			throw new RuntimeException("Missing instance: " + e.unit);
		}
		if (!(u instanceof InstExp.One)) {
			throw new RuntimeException("Not a unit in " + e);
		}
		InstExp v = env.insts.get(e.prop);
		if (v == null) {
			throw new RuntimeException("Missing instance: " + e.prop);
		}
		if (!(v instanceof InstExp.Two)) {
			throw new RuntimeException("Not a prop in " + e);
		}
		
		return new Pair<>(e.unit, e.prop);
	}

	@Override
	public Pair<String, String> visit(FQLProgram env, Not e) {
		InstExp v = env.insts.get(e.prop);
		if (v == null) {
			throw new RuntimeException("Missing instance: " + e.prop);
		}
		if (!(v instanceof InstExp.Two)) {
			throw new RuntimeException("Not a prop in " + e);
		}
		
		return new Pair<>(e.prop, e.prop);
	}
	
	@Override
	public Pair<String, String> visit(FQLProgram env, And e) {
		InstExp v = env.insts.get(e.prop);
		if (v == null) {
			throw new RuntimeException("Missing instance: " + e.prop);
		}
		if (!(v instanceof InstExp.Times)) {
			throw new RuntimeException("Not a product in " + e);
		}
		InstExp.Times v0 = (InstExp.Times) v;
		if (!v0.a.equals(v0.b)) {
			throw new RuntimeException("Not the same prop in " + e);
		}
		if (!(env.insts.get(v0.a) instanceof InstExp.Two)) {
			throw new RuntimeException("Not a prop in " + e);
		}
		
		return new Pair<>(e.prop, v0.a);
	}
	
	public Pair<String, String> visit(FQLProgram env, Or e) {
		return new And(e.prop).accept(env, this);
	}
	
	public Pair<String, String> visit(FQLProgram env, Implies e) {
		return new And(e.prop).accept(env, this);
	}

	
	@Override
	public Pair<String, String> visit(FQLProgram env, Chi e) {
		InstExp prop = env.insts.get(e.prop);
		if (prop == null) {
			throw new RuntimeException("Missing instance " + e.prop);
		}
		if (!(prop instanceof InstExp.Two)) {
			throw new RuntimeException("Not a prop " + e);
		}
		if (seen.contains(e.trans)) {
			throw new RuntimeException("Circular transform " + e);
		}
		seen.add(e.trans);
		TransExp t = env.transforms.get(e.trans);
		if (t == null) {
			throw new RuntimeException("Missing transform " + e.trans);
		}
		Pair<String, String> k = t.accept(env, this);
		return new Pair<>(k.second, e.prop);
	}

	@Override
	public Pair<String, String> visit(FQLProgram env, UnChi e) {
		InstExp p = env.insts.get(e.a);
		if (p == null) {
			throw new RuntimeException("Missing instance " + e.a);
		}
		if (!(p instanceof InstExp.Kernel)) {
			throw new RuntimeException("Not a kernel " + e);
		}
		InstExp.Kernel k = (InstExp.Kernel) p;
		Pair<String, String> trans = env.transforms.get(k.trans).type(env);
		
		return new Pair<>(e.a, trans.first); 
	}
	
	//TODO check circularity

}
