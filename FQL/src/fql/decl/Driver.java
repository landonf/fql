package fql.decl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fql.DEBUG;
import fql.FQLException;
import fql.Fn;
import fql.JDBCBridge;
import fql.LineException;
import fql.Pair;
import fql.Quad;
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
import fql.decl.TransExp.Case;
import fql.decl.TransExp.Comp;
import fql.decl.TransExp.FF;
import fql.decl.TransExp.Fst;
import fql.decl.TransExp.Id;
import fql.decl.TransExp.Inl;
import fql.decl.TransExp.Inr;
import fql.decl.TransExp.Prod;
import fql.decl.TransExp.Snd;
import fql.decl.TransExp.TT;
import fql.decl.TransExp.TransExpVisitor;
import fql.decl.TransExp.Var;
import fql.sql.CopyFlower;
import fql.sql.CreateTable;
import fql.sql.DropTable;
import fql.sql.Flower;
import fql.sql.InsertKeygen;
import fql.sql.InsertSQL;
import fql.sql.InsertValues;
import fql.sql.PSM;
import fql.sql.PSMGen;
import fql.sql.PSMInterp;
import fql.sql.Relationalizer;
import fql.sql.SQL;
import fql.sql.Union;

public class Driver {

	public static String checkReport(FQLProgram p) {
		String ret = "";

		for (String k : p.maps.keySet()) {
			try {
				MapExp m = p.maps.get(k);
				Pair<SigExp, SigExp> v = m.type(p.sigs, p.maps);
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

		return ret.trim() + "\n\n";
	}

	public static void check(FQLProgram p) {
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

		Map<String, InstExp> m = new HashMap<>();
		for (String k : p.insts.keySet()) {
			try {
				InstExp i = p.insts.get(k);
				i.type(p.sigs, p.maps, m, p.queries);
				m.put(k, i);
			} catch (RuntimeException ex) {
				ex.printStackTrace();
				throw new LineException(ex.getLocalizedMessage(), k, "instance");
			}
		}

		for (String k : p.transforms.keySet()) {
			try {
				p.transforms.get(k).type(p);
			} catch (RuntimeException re) {
				re.printStackTrace();
				throw new LineException(re.getLocalizedMessage(), k, "instance");
			}
		}

	}

	public static Pair<Environment, String> makeEnv(FQLProgram prog) {

		Map<String, Signature> sigs = new HashMap<>();
		Map<String, Mapping> maps = new HashMap<>();
		Map<String, Instance> insts = new HashMap<>();
		Map<String, Query> queries = new HashMap<>();

		for (String k : prog.sigs.keySet()) {
			try {
				SigExp v = prog.sigs.get(k);
				sigs.put(k, v.toSig(prog.sigs));
			} catch (RuntimeException re) {
				re.printStackTrace();
				throw new LineException(re.getLocalizedMessage(), k, "schema");
			}

		}

		for (String k : prog.maps.keySet()) {
			try {
				MapExp v = prog.maps.get(k);
				maps.put(k, v.toMap(prog.sigs, prog.maps));

			} catch (RuntimeException re) {
				re.printStackTrace();
				throw new LineException(re.getLocalizedMessage(), k, "mapping");
			}
		}
		for (String k : prog.queries.keySet()) {
			try {

				QueryExp v = prog.queries.get(k);
				queries.put(k,
						Query.toQuery(prog.sigs, prog.maps, prog.queries, v));
			} catch (RuntimeException re) {
				re.printStackTrace();
				throw new LineException(re.getLocalizedMessage(), k, "query");
			}
		}

		List<PSM> psm = new LinkedList<PSM>();
		for (String k : prog.insts.keySet()) {
			try {
				InstExp v = prog.insts.get(k);
				psm.addAll(PSMGen.makeTables(k,
						v.type(prog.sigs, prog.maps, prog.insts, prog.queries)
								.toSig(prog.sigs), false));
				psm.addAll(v.accept(k, new ToInstVisitor(prog)).first);
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
				Signature s = i.type(prog.sigs, prog.maps, prog.insts,
						prog.queries).toSig(prog.sigs);
				psm.addAll(PSMGen.makeTables(k, s, false));
				psm.addAll(v.accept(k, new ToTransVisitor(prog)));
			} catch (RuntimeException re) {
				re.printStackTrace();
				throw new LineException(re.getLocalizedMessage(), k, "instance");
			}
		}
		List<PSM> drops = computeDrops(prog);

		Map<String, Set<Map<Object, Object>>> res;
		if (DEBUG.useJDBC) {
			res = JDBCBridge.go(psm, drops, prog);
		} else {
			res = PSMInterp.interp(psm);
		}
		// System.out.println(res);
		for (String k : prog.insts.keySet()) {
			try {
				Signature s = prog.insts.get(k)
						.type(prog.sigs, prog.maps, prog.insts, prog.queries)
						.toSig(prog.sigs);
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
				Signature s = i.type(prog.sigs, prog.maps, prog.insts,
						prog.queries).toSig(prog.sigs);
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


		String str = DEBUG.prelude + "\n\n" + PSMGen.prettyPrint(psm) + "\n\n"
				+ PSMGen.prettyPrint(drops) + "\n\n";
		return new Pair<>(new Environment(sigs, maps, insts, queries,
				transforms), str.trim());
	}

	public static List<PSM> computeDrops(FQLProgram prog) {
		List<PSM> drops = new LinkedList<>();
		for (String k : prog.drop) {
			if (prog.insts.containsKey(k)) {
				InstExp i = prog.insts.get(k);
				Signature s = i.type(prog.sigs, prog.maps, prog.insts,
						prog.queries).toSig(prog.sigs);
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
				Signature s = i.type(prog.sigs, prog.maps, prog.insts,
						prog.queries).toSig(prog.sigs);
				drops.addAll(PSMGen.dropTables(k, s));
			} else {
				throw new RuntimeException("for drop, not found: " + k + " in "
						+ prog);
			}
		}
		return drops;
	}

	public static class ToTransVisitor implements
			TransExpVisitor<List<PSM>, String> {

		FQLProgram prog;
		int count = 0;

		public String next() {
			return "totrans_temp" + count++;
		}

		public ToTransVisitor(FQLProgram prog) {
			this.prog = prog;
		}

		@Override
		public List<PSM> visit(String dst, Id e) {
			List<PSM> ret = new LinkedList<>();

			for (Node k : prog.insts.get(e.t)
					.type(prog.sigs, prog.maps, prog.insts, prog.queries)
					.toSig(prog.sigs).nodes) {
				ret.add(new InsertSQL(dst + "_" + k.string, new CopyFlower(e.t
						+ "_" + k.string)));
			}

			return ret;
		}

		@Override
		public List<PSM> visit(String dst, Comp e) {
			List<PSM> ret = new LinkedList<>();

			Pair<String, String> ty = e.l.type(prog);
			InstExp inst = prog.insts.get(ty.first);
			Signature inst_type = inst.type(prog.sigs, prog.maps, prog.insts,
					prog.queries).toSig(prog.sigs);

			String el = next();
			ret.addAll(PSMGen.makeTables(el, inst_type, false));
			ret.addAll(e.l.accept(el, this));
			String er = next();
			ret.addAll(PSMGen.makeTables(er, inst_type, false));
			ret.addAll(e.r.accept(er, this));

			for (Node k : inst_type.nodes) {
				Map<String, String> from = new HashMap<>();
				from.put("left", el + "_" + k);
				from.put("right", er + "_" + k);

				List<Pair<Pair<String, String>, Pair<String, String>>> where = new LinkedList<>();
				where.add(new Pair<>(new Pair<>("left", "c1"), new Pair<>(
						"right", "c0")));

				Map<String, Pair<String, String>> select = new HashMap<>();
				select.put("c0", new Pair<>("left", "c0"));
				select.put("c1", new Pair<>("right", "c1"));

				Flower f = new Flower(select, from, where);

				ret.add(new InsertSQL(dst + "_" + k, f));
			}

			ret.addAll(PSMGen.dropTables(el, inst_type));
			ret.addAll(PSMGen.dropTables(er, inst_type));

			return ret;
		}

		@Override
		public List<PSM> visit(String dst, Var e) {
			return prog.transforms.get(e.v).accept(dst, this);
		}

		@Override
		public List<PSM> visit(String dst, fql.decl.TransExp.Const e) {
			List<PSM> ret = new LinkedList<>();

			Signature s = prog.insts.get(e.src)
					.type(prog.sigs, prog.maps, prog.insts, prog.queries)
					.toConst(prog.sigs).toSig(prog.sigs);

			List<String> attrs = new LinkedList<>();
			attrs.add("c0");
			attrs.add("c1");

			ret.addAll(PSMGen.makeTables("pre_" + dst, s, false));
			for (Node k : s.nodes) {
				Set<Map<Object, Object>> values = convert(lookup(k.string,
						e.objs));
				ret.add(new InsertValues("pre_" + dst + "_" + k.string, attrs,
						values));

				SQL f = PSMGen.compose(new String[] {
						e.src + "_" + k.string + "_subst_inv",
						"pre_" + dst + "_" + k.string,
						e.dst + "_" + k.string + "_subst" });
				ret.add(new InsertSQL(dst + "_" + k.string, f));
			}

			ret.addAll(PSMGen.dropTables("pre_" + dst, s));
			return ret;
		}

		private List<Pair<Object, Object>> lookup(String string,
				List<Pair<String, List<Pair<Object, Object>>>> objs) {
			for (Pair<String, List<Pair<Object, Object>>> k : objs) {
				if (k.first.equals(string)) {
					return k.second;
				}
			}
			throw new RuntimeException(string + " not found in " + objs);
		}

		private Set<Map<Object, Object>> convert(List<Pair<Object, Object>> list) {
			Set<Map<Object, Object>> ret = new HashSet<>();

			for (Pair<Object, Object> k : list) {
				Map<Object, Object> map = new HashMap<>();
				map.put("c0", k.first);
				map.put("c1", k.second);
				ret.add(map);
			}
			return ret;
		}

		@Override
		public List<PSM> visit(String dst, TT e) {
			List<PSM> ret = new LinkedList<>();
			Signature s = prog.insts.get(e.obj)
					.type(prog.sigs, prog.maps, prog.insts, prog.queries)
					.toSig(prog.sigs);

			for (Node n : s.nodes) {
				Map<String, String> from = new HashMap<>();
				from.put("t1", e.tgt + "_" + n);
				from.put("t2", e.obj + "_" + n);
				List<Pair<Pair<String, String>, Pair<String, String>>> where = new LinkedList<>();
				Map<String, Pair<String, String>> select = new HashMap<>();
				select.put("c0", new Pair<>("t1", "c0"));
				select.put("c1", new Pair<>("t2", "c0"));
				Flower f = new Flower(select, from, where);
				ret.add(new InsertSQL(dst + "_" + n, f));
			}

			return ret;
		}

		@Override
		public List<PSM> visit(String dst, FF e) {
			return new LinkedList<>();
		}

		@Override
		public List<PSM> visit(String dst, Fst e) {
			List<PSM> ret = new LinkedList<>();

			InstExp k = prog.insts.get(e.obj);
			Signature t = k
					.type(prog.sigs, prog.maps, prog.insts, prog.queries)
					.toSig(prog.sigs);

			for (Node n : t.nodes) {
				ret.add(new InsertSQL(dst + "_" + n.string, new CopyFlower(
						e.obj + "_fst_" + n.string)));
			}

			return ret;
		}

		@Override
		public List<PSM> visit(String dst, Snd e) {
			List<PSM> ret = new LinkedList<>();

			InstExp k = prog.insts.get(e.obj);
			Signature t = k
					.type(prog.sigs, prog.maps, prog.insts, prog.queries)
					.toSig(prog.sigs);

			for (Node n : t.nodes) {
				ret.add(new InsertSQL(dst + "_" + n.string, new CopyFlower(
						e.obj + "_snd_" + n.string)));
			}

			return ret;
		}

		@Override
		public List<PSM> visit(String dst, Inl e) {
			List<PSM> ret = new LinkedList<>();

			InstExp k = prog.insts.get(e.obj);
			Signature t = k
					.type(prog.sigs, prog.maps, prog.insts, prog.queries)
					.toSig(prog.sigs);

			for (Node n : t.nodes) {
				ret.add(new InsertSQL(dst + "_" + n.string, new CopyFlower(
						e.obj + "_inl_" + n.string)));
			}

			return ret;
		}

		@Override
		public List<PSM> visit(String dst, Inr e) {
			List<PSM> ret = new LinkedList<>();

			InstExp k = prog.insts.get(e.obj);
			Signature t = k
					.type(prog.sigs, prog.maps, prog.insts, prog.queries)
					.toSig(prog.sigs);

			for (Node n : t.nodes) {
				ret.add(new InsertSQL(dst + "_" + n.string, new CopyFlower(
						e.obj + "_inr_" + n.string)));
			}

			return ret;

		}

		@SuppressWarnings("unchecked")
		@Override
		public List<PSM> visit(String dst, Case e) {

			Fn<Quad<String, String, String, String>, List<PSM>> fn = (Fn<Quad<String, String, String, String>, List<PSM>>) prog.insts
					.get(e.obj).accept(e.obj, new ToInstVisitor(prog)).second;

			List<PSM> ret = new LinkedList<>();

			Signature inst_type = prog.insts.get(e.obj)
					.type(prog.sigs, prog.maps, prog.insts, prog.queries)
					.toSig(prog.sigs);

			String el = next();
			ret.addAll(PSMGen.makeTables(el, inst_type, false));
			ret.addAll(e.l.accept(el, this));
			String er = next();
			ret.addAll(PSMGen.makeTables(er, inst_type, false));
			ret.addAll(e.r.accept(er, this));

			ret.addAll(fn.of(new Quad<String, String, String, String>(el, er,
					null, dst)));

			ret.addAll(PSMGen.dropTables(el, inst_type));
			ret.addAll(PSMGen.dropTables(er, inst_type));

			return ret;

		}

		@SuppressWarnings("unchecked")
		@Override
		public List<PSM> visit(String dst, Prod e) {
			Fn<Quad<String, String, String, String>, List<PSM>> fn = (Fn<Quad<String, String, String, String>, List<PSM>>) prog.insts
					.get(e.obj).accept(e.obj, new ToInstVisitor(prog)).second;

			List<PSM> ret = new LinkedList<>();

			Signature inst_type = prog.insts.get(e.obj)
					.type(prog.sigs, prog.maps, prog.insts, prog.queries)
					.toSig(prog.sigs);

			String el = next();
			ret.addAll(PSMGen.makeTables(el, inst_type, false));
			ret.addAll(e.l.accept(el, this));
			String er = next();
			ret.addAll(PSMGen.makeTables(er, inst_type, false));
			ret.addAll(e.r.accept(er, this));

			ret.addAll(fn.of(new Quad<String, String, String, String>(el, er,
					null, dst)));

			ret.addAll(PSMGen.dropTables(el, inst_type));
			ret.addAll(PSMGen.dropTables(er, inst_type));

			return ret;

		}

	}

	public static class ToInstVisitor implements
			InstExpVisitor<Pair<List<PSM>, Object>, String> {

		FQLProgram prog;

		/*
		 * int counter = 0; String next() { return "temp_table_" + counter++; }
		 */
		public ToInstVisitor(FQLProgram prog) {
			this.prog = prog;
		}

		@Override
		public Pair<List<PSM>, Object> visit(String dst, fql.decl.InstExp.Zero e) {
			return new Pair<List<PSM>, Object>(new LinkedList<PSM>(),
					new Object());
		}

		@Override
		public Pair<List<PSM>, Object> visit(String dst, fql.decl.InstExp.One e) {
			Const c = e.sig.toConst(prog.sigs);
			if (c.attrs.size() > 0) {
				throw new RuntimeException(
						"Cannot create unit instance for schemas with attributes: "
								+ e);
			}
			List<PSM> ret = new LinkedList<>();
			List<String> attrs = new LinkedList<>();
			attrs.add("c0");
			attrs.add("c1");
			Set<Map<Object, Object>> values = new HashSet<>();
			Map<Object, Object> o = new HashMap<>();
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
			return new Pair<>(ret, new Object());
		}

		@Override
		public Pair<List<PSM>, Object> visit(String dst, Two e) {
			throw new RuntimeException();
		}

		@Override
		public Pair<List<PSM>, Object> visit(final String dst,
				final fql.decl.InstExp.Plus e) {
			try {
				SigExp k = e.type(prog.sigs, prog.maps, prog.insts,
						prog.queries);
				final Signature s = k.toSig(prog.sigs);
				List<PSM> ret = new LinkedList<>();

				for (Node n : s.nodes) {
					List<Flower> l = new LinkedList<>();
					l.add(new CopyFlower(e.a + "_" + n.string));
					l.add(new CopyFlower(e.b + "_" + n.string));
					ret.add(new InsertSQL(dst + "_" + n.string, new Union(l)));
				}
				for (Attribute<Node> n : s.attrs) {
					List<Flower> l = new LinkedList<>();
					l.add(new CopyFlower(e.a + "_" + n.name));
					l.add(new CopyFlower(e.b + "_" + n.name));
					ret.add(new InsertSQL(dst + "_" + n.name, new Union(l)));
				}
				for (Edge n : s.edges) {
					List<Flower> l = new LinkedList<>();
					l.add(new CopyFlower(e.a + "_" + n.name));
					l.add(new CopyFlower(e.b + "_" + n.name));
					ret.add(new InsertSQL(dst + "_" + n.name, new Union(l)));
				}

				ret.addAll(PSMGen.guidify(dst, s, true));

				ret.addAll(PSMGen.makeTables(dst + "_inl", s, false));
				ret.addAll(PSMGen.makeTables(dst + "_inr", s, false));

				for (Node n : s.nodes) {
					SQL f = PSMGen.compose(new String[] {
					/* dst + "_" + n.string + "_subst_inv", */
					e.a + "_" + n.string, dst + "_" + n.string + "_subst" });
					ret.add(new InsertSQL(dst + "_inl_" + n.string, f));
					SQL f0 = PSMGen.compose(new String[] {
					/* dst + "_" + n.string + "_subst_inv", */
					e.b + "_" + n.string, dst + "_" + n.string + "_subst" });
					ret.add(new InsertSQL(dst + "_inr_" + n.string, f0));
				}
				// (f+g) : A+B -> C f : A -> C g : B -> C
				Fn<Quad<String, String, String, String>, List<PSM>> fn = new Fn<Quad<String, String, String, String>, List<PSM>>() {
					@Override
					public List<PSM> of(Quad<String, String, String, String> x) { // f
																					// g
																					// C
																					// dst
						String f = x.first; // e.a -> x.third
						String g = x.second; // e.b -> x.third
						String C = x.third;
						String dst0 = x.fourth;

						// must be a map dst -> x.third

						List<PSM> ret = new LinkedList<>();
						for (Node n : s.nodes) {
							Flower sql1 = PSMGen.compose(new String[] {
									dst + "_" + n.string + "_subst_inv",
									f + "_" + n.string /*
														 * , dst + "_" +
														 * n.string + "_subst"
														 */});
							Flower sql2 = PSMGen.compose(new String[] {
									dst + "_" + n.string + "_subst_inv",
									g + "_" + n.string /*
														 * , dst + "_" +
														 * n.string + "_subst"
														 */});
							List<Flower> flowers = new LinkedList<>();
							flowers.add(sql1);
							flowers.add(sql2);
							ret.add(new InsertSQL(dst0 + "_" + n.string,
									new Union(flowers)));
						}

						return ret;
					}
				};
				return new Pair<List<PSM>, Object>(ret, fn);
			} catch (FQLException fe) {
				throw new RuntimeException(fe.getLocalizedMessage());
			}
		}

		@Override
		public Pair<List<PSM>, Object> visit(final String dst,
				fql.decl.InstExp.Times e) {
			try {
				SigExp k = e.type(prog.sigs, prog.maps, prog.insts,
						prog.queries);
				final Signature s = k.toSig(prog.sigs);
				List<PSM> ret = new LinkedList<>();
				ret.addAll(PSMGen.makeTables(dst + "_fst", s, false));
				ret.addAll(PSMGen.makeTables(dst + "_snd", s, false));
				for (Node n : s.nodes) {
					Map<String, Pair<String, String>> select = new HashMap<>();
					Map<String, String> from = new HashMap<>();
					List<String> attrs = new LinkedList<>();
					Map<String, String> attrsM = new HashMap<>();
					List<Pair<Pair<String, String>, Pair<String, String>>> where = new LinkedList<>();
					from.put("left", e.a + "_" + n.string);
					from.put("right", e.b + "_" + n.string);
					attrs.add("left");
					attrs.add("right");
					attrsM.put("left", PSM.VARCHAR());
					attrsM.put("left", PSM.VARCHAR());
					select.put("left", new Pair<>("left", "c0"));
					select.put("right", new Pair<>("right", "c0"));
					for (Attribute<Node> a : s.attrsFor(n)) {
						from.put("left_" + a.name, e.a + "_" + a.name);
						from.put("right_" + a.name, e.b + "_" + a.name);
						where.add(new Pair<>(
								new Pair<>("left_" + a.name, "c0"), new Pair<>(
										"left", "c0")));
						where.add(new Pair<>(
								new Pair<>("right_" + a.name, "c0"),
								new Pair<>("right", "c0")));
						where.add(new Pair<>(
								new Pair<>("left_" + a.name, "c1"), new Pair<>(
										"right_" + a.name, "c1")));
						select.put(a.name, new Pair<>("left_" + a.name, "c1"));
						attrs.add(a.name);
						attrsM.put(a.name, a.target.toString());
					}
					Flower f = new Flower(select, from, where);
					ret.add(new CreateTable(dst + "_prod_temp_" + n.string,
							attrsM, false));
					ret.add(new InsertSQL(dst + "_prod_temp_" + n.string, f));
					Map<String, String> attrsM0 = new HashMap<>(attrsM);
					attrsM0.put("guid", PSM.VARCHAR());
					ret.add(new CreateTable(dst + "_prod_guid_" + n.string,
							attrsM0, false));
					ret.add(new InsertKeygen(dst + "_prod_guid_" + n.string,
							"guid", dst + "_prod_temp_" + n.string, attrs));

					List<Pair<Pair<String, String>, Pair<String, String>>> where0 = new LinkedList<>();

					from = new HashMap<>();
					from.put("t", dst + "_prod_guid_" + n.string);
					select = new HashMap<>();
					select.put("c0", new Pair<>("t", "guid"));
					select.put("c1", new Pair<>("t", "left"));
					f = new Flower(select, from, where0);
					ret.add(new InsertSQL(dst + "_fst_" + n, f));

					from = new HashMap<>();
					from.put("t", dst + "_prod_guid_" + n.string);
					select = new HashMap<>();
					select.put("c0", new Pair<>("t", "guid"));
					select.put("c1", new Pair<>("t", "right"));
					f = new Flower(select, from, where0);
					ret.add(new InsertSQL(dst + "_snd_" + n, f));

					Map<String, Pair<String, String>> select0 = new HashMap<>();
					select0.put("c0", new Pair<>("t", "guid"));
					select0.put("c1", new Pair<>("t", "guid"));
					Map<String, String> from0 = new HashMap<>();
					from0.put("t", dst + "_prod_guid_" + n.string);
					Flower sql = new Flower(select0, from0, where0);
					ret.add(new InsertSQL(dst + "_" + n.string, sql));
					for (Attribute<Node> a : s.attrsFor(n)) {
						select0 = new HashMap<>();
						select0.put("c0", new Pair<>("t", "guid"));
						select0.put("c1", new Pair<>("t", a.name));
						from0 = new HashMap<>();
						from0.put("t", dst + "_prod_guid_" + n.string);
						sql = new Flower(select0, from0, where0);
						ret.add(new InsertSQL(dst + "_" + a.name, sql));
					}
					ret.add(new DropTable(dst + "_prod_temp_" + n));

				}

				for (Edge edge : s.edges) {
					Map<String, String> from = new HashMap<>();
					from.put("leftEdge", e.a + "_" + edge.name);
					from.put("rightEdge", e.b + "_" + edge.name);
					from.put("srcGuid", dst + "_prod_guid_" + edge.source);
					from.put("dstGuid", dst + "_prod_guid_" + edge.target);
					List<Pair<Pair<String, String>, Pair<String, String>>> where = new LinkedList<>();
					where.add(new Pair<>(new Pair<>("leftEdge", "c0"),
							new Pair<>("srcGuid", "left")));
					where.add(new Pair<>(new Pair<>("rightEdge", "c0"),
							new Pair<>("srcGuid", "right")));
					where.add(new Pair<>(new Pair<>("leftEdge", "c1"),
							new Pair<>("dstGuid", "left")));
					where.add(new Pair<>(new Pair<>("rightEdge", "c1"),
							new Pair<>("dstGuid", "right")));
					Map<String, Pair<String, String>> select = new HashMap<>();
					select.put("c0", new Pair<>("srcGuid", "guid"));
					select.put("c1", new Pair<>("dstGuid", "guid"));
					Flower f = new Flower(select, from, where);
					ret.add(new InsertSQL(dst + "_" + edge.name, f));
				}

				Fn<Quad<String, String, String, String>, List<PSM>> fn = new Fn<Quad<String, String, String, String>, List<PSM>>() {
					@Override
					public List<PSM> of(Quad<String, String, String, String> x) {
						String f = x.first; // x.third -> e.a
						String g = x.second; // x.third -> e.b
						String C = x.third;

						String dst0 = x.fourth;

						// must be a map x.third -> dst
						List<PSM> ret = new LinkedList<>();
						for (Node n : s.nodes) {
							List<Pair<Pair<String, String>, Pair<String, String>>> where = new LinkedList<>();
							Map<String, String> from = new HashMap<>();
							from.put("f", f + "_" + n.string);
							from.put("g", g + "_" + n.string);
							from.put("lim", dst + "_prod_guid_" + n.string);
							where.add(new Pair<>(new Pair<>("f", "c0"),
									new Pair<>("g", "c0")));
							where.add(new Pair<>(new Pair<>("lim", "left"),
									new Pair<>("f", "c1")));
							where.add(new Pair<>(new Pair<>("lim", "right"),
									new Pair<>("g", "c1")));
							Map<String, Pair<String, String>> select = new HashMap<>();
							select.put("c0", new Pair<>("f", "c0"));
							select.put("c1", new Pair<>("lim", "guid"));
							Flower flower = new Flower(select, from, where);
							ret.add(new InsertSQL(dst0 + "_" + n.string, flower));
						}

						return ret;
					}
				};
				return new Pair<List<PSM>, Object>(ret, fn);
			} catch (FQLException fe) {
				throw new RuntimeException(fe.getLocalizedMessage());
			}
		}

		@Override
		public Pair<List<PSM>, Object> visit(String dst, fql.decl.InstExp.Exp e) {
			throw new RuntimeException();
		}

		/*
		 * @Override public List<PSM> visit(String dst, fql.decl.InstExp.Var e)
		 * { return prog.insts.get(e.toString()).accept(dst, this); }
		 */
		@Override
		public Pair<List<PSM>, Object> visit(String dst,
				fql.decl.InstExp.Const e) {
			try {
				List<PSM> ret = new LinkedList<>();
				Signature sig = e.sig.toSig(prog.sigs);
				ret.addAll(PSMGen.doConst(dst, sig, e.data));
				ret.addAll(PSMGen.guidify(dst, sig, true));
				return new Pair<>(ret, new Object());
			} catch (FQLException fe) {
				fe.printStackTrace();
				throw new RuntimeException(fe.getLocalizedMessage());
			}
		}

		@Override
		public Pair<List<PSM>, Object> visit(String dst, Delta e) {
			// String next = next();
			List<PSM> ret = new LinkedList<>();
			Mapping F0 = e.F.toMap(prog.sigs, prog.maps);

			// ret.addAll(PSMGen.makeTables(next, F0.target, false));
			// ret.addAll(e.I.accept(next, this));
			ret.addAll(PSMGen.delta(F0, e.I, dst));
			// ret.addAll(PSMGen.dropTables(next, F0.target));
			try {
				ret.addAll(PSMGen.guidify(dst, F0.source));
				return new Pair<>(ret, new Object());
			} catch (FQLException fe) {
				throw new RuntimeException(fe.getLocalizedMessage());
			}
		}

		@Override
		public Pair<List<PSM>, Object> visit(String dst, Sigma e) {
			// String next = next();
			List<PSM> ret = new LinkedList<>();
			Mapping F0 = e.F.toMap(prog.sigs, prog.maps);

			try {
				F0.okForSigma();
				// ret.addAll(PSMGen.makeTables(next, F0.source, false));
				// ret.addAll(e.I.accept(next, this));
				ret.addAll(PSMGen.sigma(F0, dst, e.I)); // yes, sigma is
														// backwards
				// ret.addAll(PSMGen.dropTables(next, F0.source));
				ret.addAll(PSMGen.guidify(dst, F0.target));
				return new Pair<>(ret, new Object());
			} catch (FQLException fe) {
				fe.printStackTrace();
				throw new RuntimeException(fe.getLocalizedMessage());
			}
		}

		@Override
		public Pair<List<PSM>, Object> visit(String dst, Pi e) {
			// String next = next();
			List<PSM> ret = new LinkedList<>();
			Mapping F0 = e.F.toMap(prog.sigs, prog.maps);

			try {
				F0.okForPi();
				// ret.addAll(PSMGen.makeTables(next, F0.source, false));
				// ret.addAll(e.I.accept(next, this));
				ret.addAll(PSMGen.pi(F0, e.I, dst));
				// ret.addAll(PSMGen.dropTables(next, F0.source));
				// ret.addAll(PSMGen.guidify(dst, F0.target)); //not necessary,
				// pi creates all new guids
				return new Pair<>(ret, new Object());
			} catch (FQLException fe) {
				fe.printStackTrace();
				throw new RuntimeException(fe.getLocalizedMessage());
			}

		}

		@Override
		public Pair<List<PSM>, Object> visit(String dst, FullSigma e) {
			// String next = next();
			List<PSM> ret = new LinkedList<>();
			Mapping F0 = e.F.toMap(prog.sigs, prog.maps);

			// ret.addAll(PSMGen.makeTables(next, F0.source, false));
			// ret.addAll(e.I.accept(next, this));
			try {
				ret.addAll(PSMGen.SIGMA(F0, dst, e.I)); // yes, backwards
				// ret.addAll(PSMGen.dropTables(next, F0.source));
				ret.addAll(PSMGen.guidify(dst, F0.target));
				return new Pair<>(ret, new Object());
			} catch (FQLException fe) {
				fe.printStackTrace();
				throw new RuntimeException(fe.getLocalizedMessage());
			}

		}

		@Override
		public Pair<List<PSM>, Object> visit(String dst, Relationalize e) { // out
																			// then
																			// in
			// String next = next();
			List<PSM> ret = new LinkedList<>();
			Signature sig = prog.insts.get(e.I)
					.type(prog.sigs, prog.maps, prog.insts, prog.queries)
					.toSig(prog.sigs);

			try {
				// ret.addAll(PSMGen.makeTables(next, sig, false)); //output
				// mktable done by relationalizer
				// ret.addAll(e.I.accept(next, this));
				ret.addAll(Relationalizer.compile(sig, dst, e.I, false));
				ret.addAll(PSMGen.guidify(dst, sig));
				// ret.addAll(PSMGen.dropTables(next, sig));
			} catch (FQLException fe) {
				fe.printStackTrace();
				throw new RuntimeException(fe.getLocalizedMessage());
			}
			return new Pair<>(ret, new Object());
		}

		@Override
		public Pair<List<PSM>, Object> visit(String dst, External e) {
			try {
				List<PSM> ret = new LinkedList<>();
				Signature sig = e.sig.toSig(prog.sigs);
				ret.addAll(PSMGen.doExternal(sig, e.name, dst));
				ret.addAll(PSMGen.guidify(dst, sig, true));
				return new Pair<>(ret, new Object());
			} catch (FQLException fe) {
				fe.printStackTrace();
				throw new RuntimeException(fe.getLocalizedMessage());
			}
		}

		@Override
		public Pair<List<PSM>, Object> visit(String dst, Eval e) {
			Query q = Query.toQuery(prog.sigs, prog.maps, prog.queries, e.q);

			String next = e.e;
			String next1 = "query_temp1";
			String next2 = "query_temp2";

			List<PSM> ret = new LinkedList<>();

			try {
				q.join.okForPi(); // TODO maybe redundant?
				q.union.okForSigma();

				// ret.addAll(PSMGen.makeTables(next, q.project.target, false));
				ret.addAll(PSMGen.makeTables(next1, q.project.source, false));
				ret.addAll(PSMGen.makeTables(next2, q.join.target, false));

				// ret.addAll(e.e.accept(next, this));

				ret.addAll(PSMGen.delta(q.project, next, next1));
				ret.addAll(PSMGen.guidify(next1, q.project.source));

				ret.addAll(PSMGen.pi(q.join, next1, next2));

				ret.addAll(PSMGen.sigma(q.union, dst, next2)); // backwards
				ret.addAll(PSMGen.guidify(dst, q.union.target));

				// ret.addAll(PSMGen.dropTables(next, q.project.target));
				ret.addAll(PSMGen.dropTables(next1, q.project.source));
				ret.addAll(PSMGen.dropTables(next2, q.join.target));

				return new Pair<>(ret, new Object());
			} catch (FQLException fe) {
				throw new RuntimeException(fe.getLocalizedMessage());
			}
		}

	}

}
