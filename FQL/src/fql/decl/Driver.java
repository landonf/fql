package fql.decl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fql.DEBUG;
import fql.FQLException;
import fql.LineException;
import fql.Pair;
import fql.Triple;
import fql.decl.InstExp.Delta;
import fql.decl.InstExp.Eval;
import fql.decl.InstExp.External;
import fql.decl.InstExp.FullSigma;
import fql.decl.InstExp.InstExpVisitor;
import fql.decl.InstExp.Pi;
import fql.decl.InstExp.Relationalize;
import fql.decl.InstExp.Sigma;
import fql.decl.InstExp.Two;
import fql.decl.SigExp.Const;
import fql.sql.CopyFlower;
import fql.sql.Flower;
import fql.sql.InsertSQL;
import fql.sql.InsertValues;
import fql.sql.PSM;
import fql.sql.PSMGen;
import fql.sql.PSMInterp;
import fql.sql.Relationalizer;
import fql.sql.Union;

public class Driver {
	
	public static String checkReport(NewestFQLProgram p) {
		String ret = "";

		for (String k : p.maps.keySet()) {
			try {
				MapExp m = p.maps.get(k);
				Pair<SigExp, SigExp> v = m.type(p.sigs, p.maps);
				ret += k + ": " + v.first.unresolve(p.sigs) + " -> " + v.second.unresolve(p.sigs) + "\n\n";
			} catch (RuntimeException ex) {
				ret += k + ": " + ex.getLocalizedMessage() + "\n\n";				
			}
		}
		return ret;
	}


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
				i.type(p.sigs, p.maps, p.insts, p.queries);
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
		Map<String, Query> queries = new HashMap<>();

		for (String k : prog.sigs.keySet()) {
			SigExp v = prog.sigs.get(k);
			try {
				sigs.put(k, v.toSig(prog.sigs));
			} catch (RuntimeException re) {
				re.printStackTrace();
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
		
		for (String k : prog.queries.keySet()) {
			QueryExp v = prog.queries.get(k);
			try {
				queries.put(k, Query.toQuery(prog.sigs, prog.maps, prog.queries, v));
			} catch (RuntimeException re) {
				re.printStackTrace();
				throw new LineException(re.getLocalizedMessage(), k, "query");
			}
		}

		List<PSM> psm = new LinkedList<PSM>();
		for (String k : prog.insts.keySet()) {
			InstExp v = prog.insts.get(k);
			try {
				psm.addAll(PSMGen.makeTables(k, v.type(prog.sigs, prog.maps, prog.insts, prog.queries).toSig(prog.sigs), false));
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
				Signature s = prog.insts.get(k).type(prog.sigs, prog.maps, prog.insts, prog.queries).toSig(prog.sigs);
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
		return new Pair<>(new Environment(sigs, maps, insts, queries), str);
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
			return new LinkedList<>();
		}

		@Override
		public List<PSM> visit(String dst, fql.decl.InstExp.One e) {
			Const c = e.sig.toConst(prog.sigs);
			if (c.attrs.size() > 0) {
				throw new RuntimeException("Cannot create unit instance for schemas with attributes: " + e);
			}
			List<PSM> ret = new LinkedList<>();
			List<String> attrs = new LinkedList<>();
			attrs.add("c0");
			attrs.add("c1");
			Set<Map<String, Object>> values = new HashSet<>();
			Map<String, Object> o = new HashMap<>();
			o.put("unit", "unit");
			values.add(o);
			
			for (String k : c.nodes) {
				ret.add(new InsertValues(dst + "_" + k, attrs, values));				
			}
			for (Triple<String, String, String> k : c.arrows) {
				ret.add(new InsertValues(dst + "_" + k.first, attrs, values));				
			}
			try {
				ret.addAll(PSMGen.guidify(dst, c.toSig(prog.sigs)));
			} catch (FQLException fe) {
				throw new RuntimeException(fe.getLocalizedMessage());
			}
			return ret;
		}

		@Override
		public List<PSM> visit(String dst, Two e) {
			throw new RuntimeException();
		}

		@Override
		public List<PSM> visit(String dst, fql.decl.InstExp.Plus e) {
			SigExp k = e.type(prog.sigs, prog.maps, prog.insts, prog.queries);
			Signature s = k.toSig(prog.sigs);
			List<PSM> ret = new LinkedList<>();
			String t1 = next();
			ret.addAll(PSMGen.makeTables(t1, s, false));
			ret.addAll(e.a.accept(t1, this));
			String t2 = next();
			ret.addAll(PSMGen.makeTables(t2, s, false));
			ret.addAll(e.b.accept(t2, this));
			
			for (Node n : s.nodes) {
				List<Flower> l = new LinkedList<>();
				l.add(new CopyFlower(t1 + "_" + n.string));
				l.add(new CopyFlower(t2 + "_" + n.string));
				ret.add(new InsertSQL(dst + "_" + n.string, new Union(l)));
			}
			for (Attribute<Node> n : s.attrs) {
				List<Flower> l = new LinkedList<>();
				l.add(new CopyFlower(t1 + "_" + n.name));
				l.add(new CopyFlower(t2 + "_" + n.name));
				ret.add(new InsertSQL(dst + "_" + n.name, new Union(l)));
			}
			for (Edge n : s.edges) {
				List<Flower> l = new LinkedList<>();
				l.add(new CopyFlower(t1 + "_" + n.name));
				l.add(new CopyFlower(t2 + "_" + n.name));
				ret.add(new InsertSQL(dst + "_" + n.name, new Union(l)));
			}
			
			ret.addAll(PSMGen.dropTables(t1, s));
			ret.addAll(PSMGen.dropTables(t2, s));
			try {
				ret.addAll(PSMGen.guidify(dst, s));
			} catch (FQLException fe) {
				throw new RuntimeException(fe.getLocalizedMessage());
			}
			return ret;
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
							
			try {
				F0.okForSigma();
				ret.addAll(PSMGen.makeTables(next, F0.source, false));
				ret.addAll(e.I.accept(next, this));
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
		
			try {
				F0.okForPi();
				ret.addAll(PSMGen.makeTables(next, F0.source, false));
				ret.addAll(e.I.accept(next, this));				
				ret.addAll(PSMGen.pi(F0, next, dst)); 
				ret.addAll(PSMGen.dropTables(next, F0.source));
				//ret.addAll(PSMGen.guidify(dst, F0.target)); //not necessary, pi creates all new guids
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
			Signature sig = e.I.type(prog.sigs, prog.maps, prog.insts, prog.queries).toSig(prog.sigs);
			
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

		@Override
		public List<PSM> visit(String dst, Eval e) {
			Query q = Query.toQuery(prog.sigs, prog.maps, prog.queries, e.q);

			String next = next();
			String next1 = next();
			String next2 = next();
			
			List<PSM> ret = new LinkedList<>();

			try {	
				q.join.okForPi(); //TODO maybe redundant?
				q.union.okForSigma();

				ret.addAll(PSMGen.makeTables(next, q.project.target, false));
				ret.addAll(PSMGen.makeTables(next1, q.project.source, false));
				ret.addAll(PSMGen.makeTables(next2, q.join.target, false));

				ret.addAll(e.e.accept(next, this));
				
				ret.addAll(PSMGen.delta(q.project, next, next1));
				ret.addAll(PSMGen.guidify(next1, q.project.source));
				
				ret.addAll(PSMGen.pi(q.join, next1, next2)); 
			
				ret.addAll(PSMGen.sigma(q.union, dst, next2)); //backwards 
				ret.addAll(PSMGen.guidify(dst, q.union.target)); 
				
				ret.addAll(PSMGen.dropTables(next, q.project.target));
				ret.addAll(PSMGen.dropTables(next1, q.project.source));
				ret.addAll(PSMGen.dropTables(next2, q.join.target));

				return ret;
			} catch (FQLException fe) {
				throw new RuntimeException(fe.getLocalizedMessage());
			}
		}
		
	}

}
