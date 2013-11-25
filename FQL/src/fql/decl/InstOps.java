package fql.decl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fql.FQLException;
import fql.Fn;
import fql.Pair;
import fql.Quad;
import fql.Triple;
import fql.Unit;
import fql.decl.FullQuery.FullQueryVisitor;
import fql.decl.InstExp.Delta;
import fql.decl.InstExp.Eval;
import fql.decl.InstExp.External;
import fql.decl.InstExp.FullEval;
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
import fql.sql.Relationalizer;
import fql.sql.SQL;
import fql.sql.Union;

public class InstOps implements
		FullQueryVisitor<Pair<List<PSM>, String>, String>,
		TransExpVisitor<List<PSM>, String>,
		InstExpVisitor<Pair<List<PSM>, Object>, String> {

	FQLProgram prog;
	int count = 0;

	public String next() {
		return "inst_ops_temp" + count++;
	}

	public InstOps(FQLProgram prog) {
		this.prog = prog;
	}

	@Override
	public List<PSM> visit(String dst, Id e) {
		List<PSM> ret = new LinkedList<>();

		for (Node k : prog.insts.get(e.t).type(prog).toSig(prog).nodes) {
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
		Signature inst_type = inst.type(prog).toSig(prog);

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
			where.add(new Pair<>(new Pair<>("left", "c1"), new Pair<>("right",
					"c0")));

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

		Signature s = prog.insts.get(e.src).type(prog).toConst(prog)
				.toSig(prog);

		List<String> attrs = new LinkedList<>();
		attrs.add("c0");
		attrs.add("c1");

		ret.addAll(PSMGen.makeTables("pre_" + dst, s, false));
		for (Node k : s.nodes) {
			Set<Map<Object, Object>> values = convert(lookup(k.string, e.objs));
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
		try {
			List<PSM> ret = new LinkedList<>();
			Signature s = prog.insts.get(e.obj).type(prog).toSig(prog);

			String temp1 = next();
			ret.addAll(PSMGen.makeTables(temp1, s, false));
			String temp2 = next();
			ret.addAll(PSMGen.makeTables(temp2, s, false));

			Pair<Map<Node, List<String>>, List<PSM>> xxx = Relationalizer
					.observations(s, temp1, e.tgt, false);
			Pair<Map<Node, List<String>>, List<PSM>> yyy = Relationalizer
					.observations(s, temp2, e.obj, false);
			if (!xxx.first.equals(yyy.first)) {
				throw new RuntimeException("not equal: " + xxx + " and " + yyy);
			}
			ret.addAll(xxx.second);
			ret.addAll(yyy.second);

			for (Node n : s.nodes) {
				List<String> cols = xxx.first.get(n);
				Map<String, String> from = new HashMap<>();
				from.put("t1", e.tgt + "_" + n);
				from.put("t1_obs", temp1 + "_" + n + "_" + "observables");
				from.put("t2", e.obj + "_" + n);
				from.put("t2_obs", temp2 + "_" + n + "_" + "observables");
				List<Pair<Pair<String, String>, Pair<String, String>>> where = new LinkedList<>();
				where.add(new Pair<>(new Pair<>("t1", "c0"), new Pair<>(
						"t1_obs", "id")));
				where.add(new Pair<>(new Pair<>("t2", "c0"), new Pair<>(
						"t2_obs", "id")));
				// System.out.println("77777" + cols);
				for (int i = 0; i < cols.size(); i++) {
					where.add(new Pair<>(new Pair<>("t1_obs", "c" + i),
							new Pair<>("t2_obs", "c" + i)));
				}
				Map<String, Pair<String, String>> select = new HashMap<>();
				select.put("c0", new Pair<>("t1", "c0"));
				select.put("c1", new Pair<>("t2", "c0"));
				Flower f = new Flower(select, from, where);
				ret.add(new InsertSQL(dst + "_" + n, f));

			}

			ret.addAll(PSMGen.dropTables(temp1, s));
			ret.addAll(PSMGen.dropTables(temp2, s));

			return ret;

		} catch (FQLException fe) {
			fe.printStackTrace();
			throw new RuntimeException(fe.getLocalizedMessage());
		}
	}

	@Override
	public List<PSM> visit(String dst, FF e) {
		return new LinkedList<>();
	}

	@Override
	public List<PSM> visit(String dst, Fst e) {
		List<PSM> ret = new LinkedList<>();

		InstExp k = prog.insts.get(e.obj);
		Signature t = k.type(prog).toSig(prog);

		for (Node n : t.nodes) {
			ret.add(new InsertSQL(dst + "_" + n.string, new CopyFlower(e.obj
					+ "_fst_" + n.string)));
		}

		return ret;
	}

	@Override
	public List<PSM> visit(String dst, Snd e) {
		List<PSM> ret = new LinkedList<>();

		InstExp k = prog.insts.get(e.obj);
		Signature t = k.type(prog).toSig(prog);

		for (Node n : t.nodes) {
			ret.add(new InsertSQL(dst + "_" + n.string, new CopyFlower(e.obj
					+ "_snd_" + n.string)));
		}

		return ret;
	}

	@Override
	public List<PSM> visit(String dst, Inl e) {
		List<PSM> ret = new LinkedList<>();

		InstExp k = prog.insts.get(e.obj);
		Signature t = k.type(prog).toSig(prog);

		for (Node n : t.nodes) {
			ret.add(new InsertSQL(dst + "_" + n.string, new CopyFlower(e.obj
					+ "_inl_" + n.string)));
		}

		return ret;
	}

	@Override
	public List<PSM> visit(String dst, Inr e) {
		List<PSM> ret = new LinkedList<>();

		InstExp k = prog.insts.get(e.obj);
		Signature t = k.type(prog).toSig(prog);

		for (Node n : t.nodes) {
			ret.add(new InsertSQL(dst + "_" + n.string, new CopyFlower(e.obj
					+ "_inr_" + n.string)));
		}

		return ret;

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<PSM> visit(String dst, Case e) {

		Fn<Quad<String, String, String, String>, List<PSM>> fn = (Fn<Quad<String, String, String, String>, List<PSM>>) prog.insts
				.get(e.obj).accept(e.obj, this).second;

		List<PSM> ret = new LinkedList<>();

		Signature inst_type = prog.insts.get(e.obj).type(prog).toSig(prog);

		String el = next();
		ret.addAll(PSMGen.makeTables(el, inst_type, false));
		ret.addAll(e.l.accept(el, this));
		String er = next();
		ret.addAll(PSMGen.makeTables(er, inst_type, false));
		ret.addAll(e.r.accept(er, this));

		ret.addAll(fn.of(new Quad<String, String, String, String>(el, er, null,
				dst)));

		ret.addAll(PSMGen.dropTables(el, inst_type));
		ret.addAll(PSMGen.dropTables(er, inst_type));

		return ret;

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<PSM> visit(String dst, Prod e) {
		Fn<Quad<String, String, String, String>, List<PSM>> fn = (Fn<Quad<String, String, String, String>, List<PSM>>) prog.insts
				.get(e.obj).accept(e.obj, this).second;

		List<PSM> ret = new LinkedList<>();

		Signature inst_type = prog.insts.get(e.obj).type(prog).toSig(prog);

		String el = next();
		ret.addAll(PSMGen.makeTables(el, inst_type, false));
		ret.addAll(e.l.accept(el, this));
		String er = next();
		ret.addAll(PSMGen.makeTables(er, inst_type, false));
		ret.addAll(e.r.accept(er, this));

		ret.addAll(fn.of(new Quad<String, String, String, String>(el, er, null,
				dst)));

		ret.addAll(PSMGen.dropTables(el, inst_type));
		ret.addAll(PSMGen.dropTables(er, inst_type));

		return ret;
	}

	//TODO HERE
	@Override
	public List<PSM> visit(String dst, fql.decl.TransExp.Delta e) {		
		List<PSM> ret = new LinkedList<>();
		Pair<String, String> ht = e.h.type(prog);
		Signature sig = prog.insts.get(ht.first).type(prog).toSig(prog);

//		Signature sig = prog.insts.get(e.src).type(prog).toSig(prog);
		
		Mapping F = ((Delta)prog.insts.get(e.src)).F.toMap(prog);
		
		String next = next();
		ret.addAll(PSMGen.makeTables(next, sig, false));
		ret.addAll(e.h.accept(next, this));

		Signature sig2 = prog.insts.get(e.src).type(prog).toSig(prog);

		for (Node n : sig2.nodes) {
			String fc = F.nm.get(n).string;
			ret.add(new InsertSQL(dst + "_" + n.string, PSMGen.compose(new String[] {e.src + "_" + n.string + "_subst_inv", next + "_" + fc, e.dst + "_" + n.string + "_subst"})));
		}
		
		ret.addAll(PSMGen.dropTables(next, sig));
		return ret;
	}

	@Override
	public List<PSM> visit(String env, fql.decl.TransExp.Sigma e) {
		// TODO Auto-generated method stub
		throw new RuntimeException("TBD");
	}

	@Override
	public List<PSM> visit(String env, fql.decl.TransExp.FullSigma e) {
		// TODO Auto-generated method stub
		throw new RuntimeException("TBD");
	}

	@Override
	public List<PSM> visit(String env, fql.decl.TransExp.Pi e) {
		// TODO Auto-generated method stub
		throw new RuntimeException("TBD");
	}

	@Override
	public List<PSM> visit(String env, fql.decl.TransExp.Relationalize e) {
		// TODO Auto-generated method stub
		throw new RuntimeException("TBD");
	}

	@Override
	public Pair<List<PSM>, Object> visit(String dst, fql.decl.InstExp.Zero e) {
		return new Pair<List<PSM>, Object>(new LinkedList<PSM>(), new Object());
	}

	@Override
	public Pair<List<PSM>, Object> visit(String dst, fql.decl.InstExp.One e) {
		fql.decl.InstExp.Const k = Relationalizer.terminal(prog,
				e.sig.toConst(prog));
		return k.accept(dst, this);
	}

	@Override
	public Pair<List<PSM>, Object> visit(String dst, Two e) {
		throw new RuntimeException();
	}

	@Override
	public Pair<List<PSM>, Object> visit(final String dst,
			final fql.decl.InstExp.Plus e) {
		try {
			SigExp k = e.type(prog);
			final Signature s = k.toSig(prog);
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
													 * , dst + "_" + n.string +
													 * "_subst"
													 */});
						Flower sql2 = PSMGen.compose(new String[] {
								dst + "_" + n.string + "_subst_inv",
								g + "_" + n.string /*
													 * , dst + "_" + n.string +
													 * "_subst"
													 */});
						List<Flower> flowers = new LinkedList<>();
						flowers.add(sql1);
						flowers.add(sql2);
						ret.add(new InsertSQL(dst0 + "_" + n.string, new Union(
								flowers)));
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
			SigExp k = e.type(prog);
			final Signature s = k.toSig(prog);
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
					where.add(new Pair<>(new Pair<>("left_" + a.name, "c0"),
							new Pair<>("left", "c0")));
					where.add(new Pair<>(new Pair<>("right_" + a.name, "c0"),
							new Pair<>("right", "c0")));
					where.add(new Pair<>(new Pair<>("left_" + a.name, "c1"),
							new Pair<>("right_" + a.name, "c1")));
					select.put(a.name, new Pair<>("left_" + a.name, "c1"));
					attrs.add(a.name);
					attrsM.put(a.name, a.target.toString());
				}
				Flower f = new Flower(select, from, where);
				ret.add(new CreateTable(dst + "_prod_temp_" + n.string, attrsM,
						false));
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
				where.add(new Pair<>(new Pair<>("leftEdge", "c0"), new Pair<>(
						"srcGuid", "left")));
				where.add(new Pair<>(new Pair<>("rightEdge", "c0"), new Pair<>(
						"srcGuid", "right")));
				where.add(new Pair<>(new Pair<>("leftEdge", "c1"), new Pair<>(
						"dstGuid", "left")));
				where.add(new Pair<>(new Pair<>("rightEdge", "c1"), new Pair<>(
						"dstGuid", "right")));
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
						where.add(new Pair<>(new Pair<>("f", "c0"), new Pair<>(
								"g", "c0")));
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
	 * @Override public List<PSM> visit(String dst, fql.decl.InstExp.Var e) {
	 * return prog.insts.get(e.toString()).accept(dst, this); }
	 */
	@Override
	public Pair<List<PSM>, Object> visit(String dst, fql.decl.InstExp.Const e) {
		try {
			List<PSM> ret = new LinkedList<>();
			Signature sig = e.sig.toSig(prog);
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
		Mapping F0 = e.F.toMap(prog);

		// ret.addAll(PSMGen.makeTables(next, F0.target, false));
		// ret.addAll(e.I.accept(next, this));
		ret.addAll(PSMGen.delta(F0, e.I, dst));
		// ret.addAll(PSMGen.dropTables(next, F0.target));
		try {
			ret.addAll(PSMGen.guidify(dst, F0.source, true));
			return new Pair<>(ret, new Object());
		} catch (FQLException fe) {
			throw new RuntimeException(fe.getLocalizedMessage());
		}
	}
	
	//TODO remember sigma, pi substs

	@Override
	public Pair<List<PSM>, Object> visit(String dst, Sigma e) {
		// String next = next();
		List<PSM> ret = new LinkedList<>();
		Mapping F0 = e.F.toMap(prog);

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
		Mapping F0 = e.F.toMap(prog);

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
		Mapping F0 = e.F.toMap(prog);

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
		Signature sig = prog.insts.get(e.I).type(prog).toSig(prog);

		try {
			// ret.addAll(PSMGen.makeTables(next, sig, false)); //output
			// mktable done by relationalizer
			// ret.addAll(e.I.accept(next, this));
			ret.addAll(Relationalizer.compile(sig, dst, e.I).second);
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
			Signature sig = e.sig.toSig(prog);
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
		Query q = Query.toQuery(prog, e.q);

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

	@Override
	public Pair<List<PSM>, Object> visit(String env, FullEval e) {
		List<PSM> ret = new LinkedList<>();
		Pair<List<PSM>, String> l = e.q.toFullQuery(prog).accept(e.e, this);
		ret.addAll(l.first);

		Const sig = e.q.type(prog).second.toConst(prog);
		for (String n : sig.nodes) {
			ret.add(new InsertSQL(env + "_" + n, new CopyFlower(l.second + "_"
					+ n)));
		}
		for (Triple<String, String, String> n : sig.attrs) {
			ret.add(new InsertSQL(env + "_" + n.first, new CopyFlower(l.second
					+ "_" + n.first)));
		}
		for (Triple<String, String, String> n : sig.arrows) {
			ret.add(new InsertSQL(env + "_" + n.first, new CopyFlower(l.second
					+ "_" + n.first)));
		}

		return new Pair<>(ret, new Object());
	}

	////////////////////////////////////////
	
	@Override
	public Pair<List<PSM>, String> visit(String dst, fql.decl.FullQuery.Comp e) {
		Pair<List<PSM>, String> n1 = e.l.accept(dst, this);
		Pair<List<PSM>, String> n2 = e.r.accept(n1.second, this);
		List<PSM> ret = new LinkedList<>(n1.first);
		ret.addAll(n2.first);
		return new Pair<>(ret, n2.second);
	}

	@Override
	public Pair<List<PSM>, String> visit(String src, fql.decl.FullQuery.Delta e) {
		String dst = next();
		List<PSM> ret = new LinkedList<>();
		Mapping F0 = e.F;

		ret.addAll(PSMGen.makeTables(dst, F0.source, false));
		ret.addAll(PSMGen.delta(F0, src, dst));
		// ret.addAll(PSMGen.dropTables(next, F0.target));
		try {
			ret.addAll(PSMGen.guidify(dst, F0.source));
			return new Pair<>(ret, dst);
		} catch (FQLException fe) {
			throw new RuntimeException(fe.getLocalizedMessage());
		}
	}

	@Override
	public Pair<List<PSM>, String> visit(String src, fql.decl.FullQuery.Sigma e) {
		try {
			String dst = next();
			List<PSM> ret = new LinkedList<>();
			Mapping F0 = e.F;

			ret.addAll(PSMGen.makeTables(dst, F0.target, false));
			try {
				ret.addAll(PSMGen.sigma(F0, dst, src));
			} catch (Exception ex) {
				ex.printStackTrace();
				ret.addAll(PSMGen.SIGMA(F0, dst, src));
			}
			// ret.addAll(PSMGen.dropTables(next, F0.target));
			// ret.addAll(PSMGen.guidify(dst, F0.target));
			return new Pair<>(ret, dst);
		} catch (FQLException fe) {
			throw new RuntimeException(fe.getLocalizedMessage());
		}
	}

	@Override
	public Pair<List<PSM>, String> visit(String src, fql.decl.FullQuery.Pi e) {
		try {
			String dst = next();
			List<PSM> ret = new LinkedList<>();
			Mapping F0 = e.F;

			ret.addAll(PSMGen.makeTables(dst, F0.target, false));
			ret.addAll(PSMGen.pi(F0, src, dst));

			// ret.addAll(PSMGen.dropTables(next, F0.target));
			// ret.addAll(PSMGen.guidify(dst, F0.target));
			return new Pair<>(ret, dst);
		} catch (FQLException fe) {
			fe.printStackTrace();
			throw new RuntimeException(fe.getLocalizedMessage());
		}
	}

}
