package fql.decl;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fql.DEBUG;
import fql.FQLException;
import fql.LineException;
import fql.Pair;
import fql.decl.InstExp.Delta;
import fql.decl.InstExp.External;
import fql.decl.InstExp.FullSigma;
import fql.decl.InstExp.InstExpVisitor;
import fql.decl.InstExp.Pi;
import fql.decl.InstExp.Relationalize;
import fql.decl.InstExp.Sigma;
import fql.decl.InstExp.Two;
import fql.decl.MapExp.Apply;
import fql.decl.MapExp.Case;
import fql.decl.MapExp.Comp;
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
import fql.decl.SigExp.Const;
import fql.decl.SigExp.Exp;
import fql.decl.SigExp.One;
import fql.decl.SigExp.Plus;
import fql.decl.SigExp.SigExpVisitor;
import fql.decl.SigExp.Times;
import fql.decl.SigExp.Var;
import fql.decl.SigExp.Zero;
import fql.sql.PSM;
import fql.sql.PSMGen;
import fql.sql.PSMInterp;
import fql.sql.Relationalizer;

public class Driver {

	public static void check(NewestFQLProgram p) {
		for (String k : p.sigs.keySet()) {
			try {
				SigExp v = p.sigs.get(k);
				v.typeOf(p.sigs);
			} catch (RuntimeException re) {
				re.printStackTrace();
				throw new LineException(re.getLocalizedMessage(), k, "schema");
			}
		}

		for (String k : p.maps.keySet()) {
			try {
				MapExp m = p.maps.get(k);
				m.type(p.sigs, p.maps);
			} catch (RuntimeException ex) {
				ex.printStackTrace();
				throw new LineException(ex.getLocalizedMessage(), k, "mapping");
			}
		}

		for (String k : p.insts.keySet()) {
			try {
				InstExp i = p.insts.get(k);
				i.type(p.sigs, p.maps, p.insts);
			} catch (RuntimeException ex) {
				ex.printStackTrace();
				throw new LineException(ex.getLocalizedMessage(), k, "instance");
			}
		}
	}

	public static Pair<Environment, String> makeEnv(NewestFQLProgram prog) {

		Map<String, Signature> sigs = new HashMap<>();
		Map<String, Mapping> maps = new HashMap<>();
		Map<String, Instance> insts = new HashMap<>();

		for (String k : prog.sigs.keySet()) {
			SigExp v = prog.sigs.get(k);
			try {
				sigs.put(k, v.toSig(prog.sigs));
			} catch (RuntimeException re) {
				throw new LineException(re.getLocalizedMessage(), k, "schema");
			}
		}

		for (String k : prog.maps.keySet()) {
			MapExp v = prog.maps.get(k);
			try {
				maps.put(k, v.toMap(prog.sigs, prog.maps));
			} catch (RuntimeException re) {
				re.printStackTrace();
				throw new LineException(re.getLocalizedMessage(), k, "mapping");
			}
		}

		List<PSM> psm = new LinkedList<PSM>();
		for (String k : prog.insts.keySet()) {
			InstExp v = prog.insts.get(k);
			try {
				psm.addAll(PSMGen.makeTables(k, v.type(prog.sigs, prog.maps, prog.insts).toSig(prog.sigs), false));
				psm.addAll(v.accept(k, new ToInstVisitor(prog)));
			} catch (RuntimeException re) {
				re.printStackTrace();
				throw new LineException(re.getLocalizedMessage(), k, "instance");
			}
		}

		Map<String, Set<Map<String, Object>>> res = PSMInterp.interp(psm);
		//System.out.println(res);
		for (String k : prog.insts.keySet()) {
			try {
				Signature s = prog.insts.get(k).type(prog.sigs, prog.maps, prog.insts).toSig(prog.sigs);
				List<Pair<String, List<Pair<Object, Object>>>> b = PSMGen.gather(k, s, res);
				insts.put(k, new Instance(s, b));
			} catch (FQLException re) {
				throw new LineException(re.getLocalizedMessage(), k, "instance");
			}
		}

		String str;
		try {
			str =  DEBUG.prelude + "\n\n" + PSMGen.prettyPrint(psm);
		} catch (Exception ex) {
			str = ex.getMessage();
		}
		return new Pair<>(new Environment(sigs, maps, insts), str);
	}

	
	
	public static class ToInstVisitor implements InstExpVisitor<List<PSM>, String> {
		
		NewestFQLProgram prog;
		int counter = 0;
		String next() {
			return "temp_table_" + counter++;
		}

		public ToInstVisitor(NewestFQLProgram prog) {
			this.prog = prog;
		}

		@Override
		public List<PSM> visit(String dst, fql.decl.InstExp.Zero e) {
			throw new RuntimeException();
		}

		@Override
		public List<PSM> visit(String dst, fql.decl.InstExp.One e) {
			throw new RuntimeException();
		}

		@Override
		public List<PSM> visit(String dst, Two e) {
			throw new RuntimeException();
		}

		@Override
		public List<PSM> visit(String dst, fql.decl.InstExp.Plus e) {
			throw new RuntimeException();
		}

		@Override
		public List<PSM> visit(String dst, fql.decl.InstExp.Times e) {
			throw new RuntimeException();
		}

		@Override
		public List<PSM> visit(String dst, fql.decl.InstExp.Exp e) {
			throw new RuntimeException();
		}

		@Override
		public List<PSM> visit(String dst, fql.decl.InstExp.Var e) {
			return prog.insts.get(e.toString()).accept(dst, this);
		}

		@Override
		public List<PSM> visit(String dst, fql.decl.InstExp.Const e) {
			try {
				List<PSM> ret = new LinkedList<>();
				Signature sig = e.sig.toSig(prog.sigs);
				ret.addAll(PSMGen.doConst(dst, sig, e.data));
				ret.addAll(PSMGen.guidify(dst, sig)); 
				return ret;
			} catch (FQLException fe) {
				fe.printStackTrace();
				throw new RuntimeException(fe.getLocalizedMessage());
			}
		}

		@Override
		public List<PSM> visit(String dst, Delta e) {
				String next = next();
				List<PSM> ret = new LinkedList<>();
				Mapping F0 = e.F.toMap(prog.sigs,  prog.maps);
								
				ret.addAll(PSMGen.makeTables(next, F0.target, false));
				ret.addAll(e.I.accept(next, this));
				ret.addAll(PSMGen.delta(F0, next, dst));
				ret.addAll(PSMGen.dropTables(next, F0.target));
				try {
					ret.addAll(PSMGen.guidify(dst, F0.source));
					return ret;
				} catch (FQLException fe) {
					throw new RuntimeException(fe.getLocalizedMessage());
				}
		}

		@Override
		public List<PSM> visit(String dst, Sigma e) {
			String next = next();
			List<PSM> ret = new LinkedList<>();
			Mapping F0 = e.F.toMap(prog.sigs,  prog.maps);
							
			ret.addAll(PSMGen.makeTables(next, F0.source, false));
			ret.addAll(e.I.accept(next, this));
			try {
				ret.addAll(PSMGen.sigma(F0, dst, next)); //yes, sigma is backwards
				ret.addAll(PSMGen.dropTables(next, F0.source));
				ret.addAll(PSMGen.guidify(dst, F0.target));
				return ret;
			} catch (FQLException fe) {
				fe.printStackTrace();
				throw new RuntimeException(fe.getLocalizedMessage());
			}
		}

		@Override
		public List<PSM> visit(String dst, Pi e) {
			String next = next();
			List<PSM> ret = new LinkedList<>();
			Mapping F0 = e.F.toMap(prog.sigs,  prog.maps);
							
			ret.addAll(PSMGen.makeTables(next, F0.source, false));
			ret.addAll(e.I.accept(next, this));
			try {
				ret.addAll(PSMGen.pi(F0, next, dst)); 
				ret.addAll(PSMGen.dropTables(next, F0.source));
				ret.addAll(PSMGen.guidify(dst, F0.target)); //TODO when is guidify necessary?
				return ret;
			} catch (FQLException fe) {
				fe.printStackTrace();
				throw new RuntimeException(fe.getLocalizedMessage());
			}

		}

		@Override
		public List<PSM> visit(String dst, FullSigma e) {
			String next = next();
			List<PSM> ret = new LinkedList<>();
			Mapping F0 = e.F.toMap(prog.sigs,  prog.maps);
							
			ret.addAll(PSMGen.makeTables(next, F0.source, false));
			ret.addAll(e.I.accept(next, this));
			try {
				ret.addAll(PSMGen.SIGMA(F0, dst, next)); //yes, backwards 
				ret.addAll(PSMGen.dropTables(next, F0.source));
				ret.addAll(PSMGen.guidify(dst, F0.target));
				return ret;
			} catch (FQLException fe) {
				fe.printStackTrace();
				throw new RuntimeException(fe.getLocalizedMessage());
			}

		}

		@Override
		public List<PSM> visit(String dst, Relationalize e) { //out then in
			String next = next();
			List<PSM> ret = new LinkedList<>();
			Signature sig = e.I.type(prog.sigs, prog.maps, prog.insts).toSig(prog.sigs);
			
			try {
				ret.addAll(PSMGen.makeTables(next, sig, false)); //output mktable done by relationalizer
				ret.addAll(e.I.accept(next, this));
				ret.addAll(Relationalizer.compile(sig, dst, next, false)); 
				ret.addAll(PSMGen.guidify(dst, sig));
				ret.addAll(PSMGen.dropTables(next, sig));
			} catch (FQLException fe) {
				fe.printStackTrace();
				throw new RuntimeException(fe.getLocalizedMessage());
			}
			return ret;
		}

		@Override
		public List<PSM> visit(String dst, External e) {
			try {	
				List<PSM> ret = new LinkedList<>();
				Signature sig = e.sig.toSig(prog.sigs);
				ret.addAll(PSMGen.doExternal(sig, e.name, dst));
				ret.addAll(PSMGen.guidify(dst, sig));
				return ret;
			} catch (FQLException fe) {
				fe.printStackTrace();
				throw new RuntimeException(fe.getLocalizedMessage());
			}
		}
		
	}
/*
	public static class ToMapVisitor
			implements
			MapExpVisitor<Mapping, Pair<Map<String, SigExp>, Map<String, MapExp>>> {

		@Override
		public Mapping visit(
				Pair<Map<String, SigExp>, Map<String, MapExp>> env, Id e) {
			throw new RuntimeException();
		}

		@Override
		public Mapping visit(
				Pair<Map<String, SigExp>, Map<String, MapExp>> env, Comp e) {
			throw new RuntimeException();
		}

		@Override
		public Mapping visit(
				Pair<Map<String, SigExp>, Map<String, MapExp>> env, Dist1 e) {
			throw new RuntimeException();
		}

		@Override
		public Mapping visit(
				Pair<Map<String, SigExp>, Map<String, MapExp>> env, Dist2 e) {
			throw new RuntimeException();
		}

		@Override
		public Mapping visit(
				Pair<Map<String, SigExp>, Map<String, MapExp>> env,
				fql.decl.MapExp.Var e) {
			return env.second.get(e.v).accept(env, this);
		}

		@Override
		public Mapping visit(
				Pair<Map<String, SigExp>, Map<String, MapExp>> env,
				fql.decl.MapExp.Const e) {
			try {
				return new Mapping(e.src.toSig(env.first),
						e.dst.toSig(env.first), e.objs,
						e.attrs, e.arrows);
			} catch (FQLException fe) {
				throw new RuntimeException(fe.getLocalizedMessage());
			}
		}

		@Override
		public Mapping visit(
				Pair<Map<String, SigExp>, Map<String, MapExp>> env, TT e) {
			throw new RuntimeException();
		}

		@Override
		public Mapping visit(
				Pair<Map<String, SigExp>, Map<String, MapExp>> env, FF e) {
			throw new RuntimeException();
		}

		@Override
		public Mapping visit(
				Pair<Map<String, SigExp>, Map<String, MapExp>> env, Fst e) {
			throw new RuntimeException();
		}

		@Override
		public Mapping visit(
				Pair<Map<String, SigExp>, Map<String, MapExp>> env, Snd e) {
			throw new RuntimeException();
		}

		@Override
		public Mapping visit(
				Pair<Map<String, SigExp>, Map<String, MapExp>> env, Inl e) {
			throw new RuntimeException();
		}

		@Override
		public Mapping visit(
				Pair<Map<String, SigExp>, Map<String, MapExp>> env, Inr e) {
			throw new RuntimeException();
		}

		@Override
		public Mapping visit(
				Pair<Map<String, SigExp>, Map<String, MapExp>> env, Apply e) {
			throw new RuntimeException();
		}

		@Override
		public Mapping visit(
				Pair<Map<String, SigExp>, Map<String, MapExp>> env, Curry e) {
			throw new RuntimeException();
		}

		@Override
		public Mapping visit(
				Pair<Map<String, SigExp>, Map<String, MapExp>> env, Case e) {
			throw new RuntimeException();
		}

		@Override
		public Mapping visit(
				Pair<Map<String, SigExp>, Map<String, MapExp>> env, Prod e) {
			throw new RuntimeException();
		}

	}
	*/
/*
	public static class ToSigVisitor implements
			SigExpVisitor<Signature, Map<String, SigExp>> {

		@Override
		public Signature visit(Map<String, SigExp> env, Zero e) {
			throw new RuntimeException();
		}

		@Override
		public Signature visit(Map<String, SigExp> env, One e) {
			throw new RuntimeException();
		}

		@Override
		public Signature visit(Map<String, SigExp> env, Plus e) {
			throw new RuntimeException();
		}

		@Override
		public Signature visit(Map<String, SigExp> env, Times e) {
			throw new RuntimeException();
		}

		@Override
		public Signature visit(Map<String, SigExp> env, Exp e) {
			throw new RuntimeException();
		}

		@Override
		public Signature visit(Map<String, SigExp> env, Var e) {
			return env.get(e.v).accept(env, this);
		}

		@Override
		public Signature visit(Map<String, SigExp> env, Const e) {
			try {
				return new Signature(e.nodes, e.attrs, e.arrows, e.eqs);
			} catch (FQLException fe) {
				fe.printStackTrace();
				throw new RuntimeException(fe.getLocalizedMessage());
			}
		}
	}
	*/
	public static class Unresolver implements SigExpVisitor<SigExp, Map<String, SigExp>> {

		@Override
		public SigExp visit(Map<String, SigExp> env, Zero e) {
			return e;
		}

		@Override
		public SigExp visit(Map<String, SigExp> env, One e) {
			return e;
		}

		@Override
		public SigExp visit(Map<String, SigExp> env, Plus e) {
			return new SigExp.Plus(e.a.accept(env, this), e.b.accept(env, this));
		}

		@Override
		public SigExp visit(Map<String, SigExp> env, Times e) {
			return new SigExp.Times(e.a.accept(env, this), e.b.accept(env, this));
		}

		@Override
		public SigExp visit(Map<String, SigExp> env, Exp e) {
			return new SigExp.Exp(e.a.accept(env, this), e.b.accept(env, this));
		}

		@Override
		public SigExp visit(Map<String, SigExp> env, Var e) {
			return e;
		}

		@Override
		public SigExp visit(Map<String, SigExp> env, Const e) {
			for (String k : env.keySet()) {
				SigExp e0 = env.get(k);
				if (e0.equals(e)) {
					return new SigExp.Var(k);
				}
			}
			return e;
		}
		
	}

}
