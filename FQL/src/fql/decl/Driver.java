package fql.decl;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fql.DEBUG;
import fql.JDBCBridge;
import fql.LineException;
import fql.Pair;
import fql.parse.PrettyPrinter;
import fql.sql.PSM;
import fql.sql.PSMGen;
import fql.sql.PSMInterp;

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

	public static Pair<Environment, String> makeEnv(FQLProgram prog) {

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
				throw new LineException(re.getLocalizedMessage(), k, "schema");
			}

		}

		for (String k : prog.maps.keySet()) {
			try {
				MapExp v = prog.maps.get(k);
				maps.put(k, v.toMap(prog));
			} catch (RuntimeException re) {
				re.printStackTrace();
				throw new LineException(re.getLocalizedMessage(), k, "mapping");
			}
		}
		for (String k : prog.queries.keySet()) {
			try {

				QueryExp v = prog.queries.get(k);
				queries.put(k, Query.toQuery(prog, v));
			} catch (RuntimeException re) {
				re.printStackTrace();
				throw new LineException(re.getLocalizedMessage(), k, "query");
			}
		}
		for (String k : prog.full_queries.keySet()) {
			try {
				FullQueryExp v = prog.full_queries.get(k);
				v.type(prog);
				full_queries.put(k, FullQuery.toQuery(prog, v));
			} catch (RuntimeException re) {
				re.printStackTrace();
				throw new LineException(re.getLocalizedMessage(), k, "query");
			}
		}
		for (String k : prog.insts.keySet()) {
			try {
				InstExp v = prog.insts.get(k);
				v.type(prog);
			} catch (RuntimeException re) {
				re.printStackTrace();
				throw new LineException(re.getLocalizedMessage(), k, "instance");
			}
		}
		for (String k : prog.transforms.keySet()) {
			try {

				TransExp v = prog.transforms.get(k);
				v.type(prog);
			} catch (RuntimeException re) {
				re.printStackTrace();
				throw new LineException(re.getLocalizedMessage(), k,
						"transform");
			}
		}

		List<PSM> psm = new LinkedList<PSM>();
		InstOps ops = new InstOps(prog);
		for (String k : prog.insts.keySet()) {
			try {
				InstExp v = prog.insts.get(k);
				psm.addAll(PSMGen
						.makeTables(k, v.type(prog).toSig(prog), false));
				psm.addAll(v.accept(k, ops).first);
			} catch (RuntimeException re) {
				re.printStackTrace();
				throw new LineException(re.getLocalizedMessage(), k, "instance");
			}
		}

		for (String k : prog.transforms.keySet()) {
			try {
				TransExp v = prog.transforms.get(k);
				Pair<String, String> val = prog.transforms.get(k).type(prog);
				InstExp i = prog.insts.get(val.first);
				Signature s = i.type(prog).toSig(prog);
				psm.addAll(PSMGen.makeTables(k, s, false));
				psm.addAll(v.accept(k, ops));
			} catch (RuntimeException re) {
				re.printStackTrace();
				throw new LineException(re.getLocalizedMessage(), k, "instance");
			}
		}
		List<PSM> drops = computeDrops(prog);

		Map<String, Set<Map<Object, Object>>> res;
		if (DEBUG.debug.useJDBC) {
			res = JDBCBridge.go(psm, drops, prog);
		} else {
			res = new PSMInterp().interp(psm);
		}
		//PrettyPrinter.printDB(res);

		for (String k : prog.insts.keySet()) {
			try {
				Signature s = prog.insts.get(k).type(prog).toSig(prog);
				List<Pair<String, List<Pair<Object, Object>>>> b = PSMGen
						.gather(k, s, res);
				insts.put(k, new Instance(s, b));
			} catch (Exception re) {
				re.printStackTrace();
				throw new LineException(re.getLocalizedMessage(), k, "instance");
			}
		}
		Map<String, Transform> transforms = new HashMap<>();

		for (String k : prog.transforms.keySet()) {
			try {
				Pair<String, String> val = prog.transforms.get(k).type(prog);
				InstExp i = prog.insts.get(val.first);
				Signature s = i.type(prog).toSig(prog);
				List<Pair<String, List<Pair<Object, Object>>>> b = PSMGen
						.gather(k, s, res);
				transforms.put(
						k,
						new Transform(insts.get(val.first), insts
								.get(val.second), b));
			} catch (Exception re) {
				re.printStackTrace();
				throw new LineException(re.getLocalizedMessage(), k, "instance");
			}
		}
		String str = "";
		try {
			str = DEBUG.debug.prelude + "\n\n" + PSMGen.prettyPrint(psm) + "\n\n"
					+ PSMGen.prettyPrint(drops) + "\n\n" + DEBUG.debug.afterlude + "\n\n";			
		} catch (RuntimeException re) {
			str = re.getLocalizedMessage();
		}
		return new Pair<>(new Environment(sigs, maps, insts, queries, 
				transforms, full_queries), str.trim());
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
