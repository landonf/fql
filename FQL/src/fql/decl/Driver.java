package fql.decl;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fql.DEBUG;
import fql.FQLException;
import fql.JDBCBridge;
import fql.LineException;
import fql.Pair;
import fql.Triple;
import fql.decl.FullQueryExp.Comp;
import fql.decl.FullQueryExp.Delta;
import fql.decl.FullQueryExp.FullQueryExpVisitor;
import fql.decl.FullQueryExp.Match;
import fql.decl.FullQueryExp.Pi;
import fql.decl.FullQueryExp.Sigma;
import fql.decl.FullQueryExp.Var;
import fql.sql.Chase;
import fql.sql.PSM;
import fql.sql.PSMGen;

public class Driver {

	public static String checkReport(FQLProgram p) {
		String ret = "";

		for (String k : p.sigs.keySet()) {
			try {
				SigExp v = p.sigs.get(k);
				v.typeOf(p);
			} catch (RuntimeException re) {
				re.printStackTrace();
				throw new LineException(re.getLocalizedMessage(), k, "schema");
			}
		}

		for (String k : p.maps.keySet()) {
			try {
				MapExp m = p.maps.get(k);
				Pair<SigExp, SigExp> v = m.type(p);
				ret += "mapping " + k + ": " + v.first.unresolve(p.sigs)
						+ " -> " + v.second.unresolve(p.sigs) + "\n\n";
			} catch (RuntimeException ex) {
				ret += k + ": " + ex.getLocalizedMessage() + "\n\n";
			}
		}
		ret += "\n\n";
		for (String k : p.transforms.keySet()) {
			try {
				Pair<String, String> y = p.transforms.get(k).type(p);
				ret += "transform " + k + ": " + y.first + " -> " + y.second
						+ "\n\n";
			} catch (RuntimeException ex) {
				ret += k + ": " + ex.getLocalizedMessage() + "\n\n";
			}
		}
		ret += "\n\n";
		for (String k : p.insts.keySet()) {
			try {
				InstExp i = p.insts.get(k);
				SigExp y = i.type(p);
				ret += "instance " + k + ": " + y.unresolve(p.sigs) + "\n\n";
			} catch (RuntimeException ex) {
				ex.printStackTrace();
				throw new LineException(ex.getLocalizedMessage(), k, "instance");
			}
		}

		for (String k : p.queries.keySet()) {
			try {
				QueryExp v = p.queries.get(k);
				Pair<SigExp, SigExp> t = v.type(p);
				ret += "query " + k + ": " + t.first.unresolve(p.sigs) + " -> "
						+ t.second.unresolve(p.sigs) + "\n\n";
			} catch (RuntimeException re) {
				re.printStackTrace();
				throw new LineException(re.getLocalizedMessage(), k, "query");
			}
		}

		for (String k : p.full_queries.keySet()) {
			try {
				FullQueryExp v = p.full_queries.get(k);
				Pair<SigExp, SigExp> t = v.type(p);
				ret += "QUERY " + k + ": " + t.first.unresolve(p.sigs) + " -> "
						+ t.second.unresolve(p.sigs) + "\n\n";
			} catch (RuntimeException re) {
				re.printStackTrace();
				throw new LineException(re.getLocalizedMessage(), k, "query");
			}
		}

		return ret.trim() + "\n\n";
	}

	public static Triple<Environment, String, List<Throwable>> makeEnv(
			FQLProgram prog) {

		List<Throwable> exns = new LinkedList<>();

		Map<String, Signature> sigs = new HashMap<>();
		Map<String, Mapping> maps = new HashMap<>();
		Map<String, Instance> insts = new HashMap<>();
		Map<String, Query> queries = new HashMap<>();
		Map<String, FullQuery> full_queries = new HashMap<>();

		for (String k : prog.sigs.keySet()) {
			try {
				SigExp v = prog.sigs.get(k);
				sigs.put(k, v.toSig(prog));
			} catch (RuntimeException re) {
				re.printStackTrace();
				LineException exn = new LineException(re.getLocalizedMessage(),
						k, "schema");
				if (DEBUG.debug.continue_on_error) {
					exns.add(exn);
				} else {
					throw exn;
				}
			}

		}

		for (String k : prog.maps.keySet()) {
			try {
				MapExp v = prog.maps.get(k);
				maps.put(k, v.toMap(prog));
			} catch (RuntimeException re) {
				re.printStackTrace();
				LineException exn = new LineException(re.getLocalizedMessage(),
						k, "mapping");
				if (DEBUG.debug.continue_on_error) {
					exns.add(exn);
				} else {
					throw exn;
				}
			}
		}
		for (String k : prog.queries.keySet()) {
			try {

				QueryExp v = prog.queries.get(k);
				queries.put(k, Query.toQuery(prog, v));
			} catch (RuntimeException re) {
				re.printStackTrace();
				LineException exn = new LineException(re.getLocalizedMessage(),
						k, "query");
				if (DEBUG.debug.continue_on_error) {
					exns.add(exn);
				} else {
					throw exn;
				}
			}
		}
		for (String k : prog.full_queries.keySet()) {
			try {
				FullQueryExp v = prog.full_queries.get(k);
				v.type(prog);
				full_queries.put(k, FullQuery.toQuery(prog, v));
			} catch (RuntimeException re) {
				re.printStackTrace();
				LineException exn = new LineException(re.getLocalizedMessage(),
						k, "QUERY");
				if (DEBUG.debug.continue_on_error) {
					exns.add(exn);
				} else {
					throw exn;
				}
			}
		}
		for (String k : prog.insts.keySet()) {
			try {
				InstExp v = prog.insts.get(k);
				v.type(prog);
			} catch (RuntimeException re) {
				re.printStackTrace();
				LineException exn = new LineException(re.getLocalizedMessage(),
						k, "instance");
				if (DEBUG.debug.continue_on_error) {
					exns.add(exn);
				} else {
					throw exn;
				}
			}
		}
		for (String k : prog.transforms.keySet()) {
			try {

				TransExp v = prog.transforms.get(k);
				v.type(prog);
			} catch (RuntimeException re) {
				re.printStackTrace();
				LineException exn = new LineException(re.getLocalizedMessage(),
						k, "transform");
				if (DEBUG.debug.continue_on_error) {
					exns.add(exn);
				} else {
					throw exn;
				}
			}
		}

//		System.out.println("before " + prog);
		prog = rewriteQueries(prog);
	//	System.out.println("after " + prog);
		
		Triple<Map<String, Set<Map<Object, Object>>>, String, List<Throwable>> res = JDBCBridge
				.run(prog);

		exns.addAll(res.third);
		for (String k : prog.insts.keySet()) {
			try {
				Signature s = prog.insts.get(k).type(prog).toSig(prog);
				List<Pair<String, List<Pair<Object, Object>>>> b = PSMGen
						.gather(k, s, res.first);
				insts.put(k, new Instance(s, b));
			} catch (Exception re) {
				re.printStackTrace();
				LineException exn = new LineException(re.getLocalizedMessage(),
						k, "instance");
				if (DEBUG.debug.continue_on_error) {
					exns.add(exn);
				} else {
					throw exn;
				}
			}
		}
		Map<String, Transform> transforms = new HashMap<>();

		for (String k : prog.transforms.keySet()) {
			try {
				Pair<String, String> val = prog.transforms.get(k).type(prog);
				InstExp i = prog.insts.get(val.first);
				Signature s = i.type(prog).toSig(prog);
				List<Pair<String, List<Pair<Object, Object>>>> b = PSMGen
						.gather(k, s, res.first);
				transforms.put(
						k,
						new Transform(insts.get(val.first), insts
								.get(val.second), b));
			} catch (Exception re) {
				re.printStackTrace();
				LineException exn = new LineException(re.getLocalizedMessage(),
						k, "transform");
				if (DEBUG.debug.continue_on_error) {
					exns.add(exn);
				} else {
					throw exn;
				}
			}
		}
		// check full sigmas with EDs
		if (DEBUG.debug.VALIDATE_WITH_EDS) {
			try {
				validateWithEds(prog, insts);
			} catch (FQLException fe) {
				fe.printStackTrace();
				throw new RuntimeException(fe.getLocalizedMessage());
			}
		}

		String toRetStr = res.second.trim();
		if (containsFullSigma(prog)) {
			toRetStr = "Cannot generate SQL for full sigma";
		}

		return new Triple<>(new Environment(sigs, maps, insts, queries,
				transforms, full_queries), toRetStr, dedup(exns));
	}

	private static FQLProgram rewriteQueries(FQLProgram prog) {
		
		LinkedHashMap<String, InstExp> insts = new LinkedHashMap<>();
		for (String k : prog.insts.keySet()) {
			InstExp v = prog.insts.get(k);
			if (v instanceof InstExp.FullEval) {
				InstExp.FullEval u = (InstExp.FullEval) v;
				List<Pair<String, InstExp>> n = u.q.toFullQueryExp(prog).accept(u.e, new ExpandFull(prog.full_queries));
				n.get(n.size()-1).first = k;
				for (Pair<String, InstExp> x : n) {
					insts.put(x.first, x.second);
				}
			} else {
				insts.put(k, v);
			}
		}
		
		return new FQLProgram(prog.enums, prog.sigs, prog.maps, insts,
				prog.full_queries, prog.queries, prog.transforms, prog.lines,
				prog.drop, prog.order);
	}
	
	private static class ExpandFull implements FullQueryExpVisitor<List<Pair<String, InstExp>>, String> {

		static int count = 0;
		Map<String, FullQueryExp> prog;
		
		public ExpandFull(Map<String, FullQueryExp> prog) {
			this.prog = prog;
		}
		
		@Override
		public List<Pair<String, InstExp>> visit(String env, Var e) {
			return prog.get(e.v).accept(env, this);
		}

		@Override
		public List<Pair<String, InstExp>> visit(String env, Match e) {
			throw new RuntimeException(); //eliminated by now
		}

		@Override
		public List<Pair<String, InstExp>> visit(String env, Delta e) {
			List<Pair<String, InstExp>> ret = new LinkedList<>();
			InstExp i = new InstExp.Delta(e.f, env);
			ret.add(new Pair<>("fet_" + count++, i));
			return ret;
		}

		@Override
		public List<Pair<String, InstExp>> visit(String env, Sigma e) {
			List<Pair<String, InstExp>> ret = new LinkedList<>();
			InstExp i = new InstExp.FullSigma(e.f, env);
			ret.add(new Pair<>("fet_" + count++, i));
			return ret;
		}

		@Override
		public List<Pair<String, InstExp>> visit(String env, Pi e) {
			List<Pair<String, InstExp>> ret = new LinkedList<>();
			InstExp i = new InstExp.Pi(e.f, env);
			ret.add(new Pair<>("fet_" + count++, i));
			return ret;
		}
		
		@Override
		public List<Pair<String, InstExp>> visit(String env, Comp e) {
			List<Pair<String, InstExp>> ret = new LinkedList<>();
			
			List<Pair<String, InstExp>> l = e.l.accept(env, this);
			ret.addAll(l);
			String temp = l.get(l.size()-1).first;
			List<Pair<String, InstExp>> r = e.r.accept(temp, this);
			ret.addAll(r);
			
			return ret;
		}

	}


	private static boolean containsFullSigma(FQLProgram prog) {
		for (String k : prog.insts.keySet()) {
			InstExp v = prog.insts.get(k);
			if (v instanceof InstExp.FullSigma) {
				return true;
			}
		}
		for (String k : prog.transforms.keySet()) {
			TransExp v = prog.transforms.get(k);
			if (v instanceof TransExp.FullSigma) {
				return true;
			}
		}

		return false;
	}

	private static <X> List<X> dedup(List<X> l) {
		List<X> ret = new LinkedList<X>();
		for (X x : l) {
			if (!ret.contains(x)) {
				ret.add(x);
			}
		}
		return ret;
	}

	private static void validateWithEds(FQLProgram prog,
			Map<String, Instance> insts) throws FQLException {
		for (String k : prog.insts.keySet()) {
			InstExp v = prog.insts.get(k);
			if (v instanceof InstExp.FullSigma) {
				InstExp.FullSigma v0 = (InstExp.FullSigma) v;
				Instance x = Chase.sigma(v0.F.toMap(prog), insts.get(v0.I));
				if (!Instance.quickCompare(x, insts.get(k))) {
					throw new RuntimeException(
							"Bad sigma ED compare: ED gives\n\n" + x
									+ "\n\nbut SIGMA gives\n\n" + insts.get(k));
				}
			}
			if (v instanceof InstExp.Sigma) {
				InstExp.Sigma v0 = (InstExp.Sigma) v;
				Instance x = Chase.sigma(v0.F.toMap(prog), insts.get(v0.I));
				if (!Instance.quickCompare(x, insts.get(k))) {
					throw new RuntimeException(
							"Bad sigma ED compare: ED gives\n\n" + x
									+ "\n\nbut SIGMA gives\n\n" + insts.get(k));
				}
			}
			if (v instanceof InstExp.Delta) {
				InstExp.Delta v0 = (InstExp.Delta) v;
				Instance x = Chase.delta(v0.F.toMap(prog), insts.get(v0.I));
				// Instance x = Chase.sigmaDirect(KIND.STANDARD,
				// v0.F.toMap(prog), insts.get(v0.I));
				if (!Instance.quickCompare(x, insts.get(k))) {
					throw new RuntimeException(
							"Bad sigma ED compare: ED gives\n\n" + x
									+ "\n\nbut SIGMA gives\n\n" + insts.get(k));
				}
			}
		}
	}

	public static List<PSM> computeDrops(FQLProgram prog) {
		List<PSM> drops = new LinkedList<>();
		for (String k : prog.drop) {
			if (prog.insts.containsKey(k)) {
				InstExp i = prog.insts.get(k);
				Signature s = i.type(prog).toSig(prog);
				drops.addAll(PSMGen.dropTables(k, s));

				// TODO add other drops
				if (i instanceof InstExp.Const || i instanceof InstExp.Plus
						|| i instanceof InstExp.Times) {
					drops.addAll(PSMGen.dropTables(k + "_subst", s));
					drops.addAll(PSMGen.dropTables(k + "_subst_inv", s));
				}
			} else if (prog.transforms.containsKey(k)) {
				TransExp t = prog.transforms.get(k);
				Pair<String, String> val = t.type(prog);
				InstExp i = prog.insts.get(val.first);
				Signature s = i.type(prog).toSig(prog);
				drops.addAll(PSMGen.dropTables(k, s));
			} else {
				throw new RuntimeException("for drop, not found: " + k + " in "
						+ prog);
			}
		}
		return drops;
	}

}
