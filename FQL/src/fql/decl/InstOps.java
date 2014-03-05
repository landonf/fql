package fql.decl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fql.FQLException;
import fql.Fn;
import fql.Pair;
import fql.Quad;
import fql.Triple;
import fql.cat.Arr;
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
import fql.decl.TransExp.Case;
import fql.decl.TransExp.Comp;
import fql.decl.TransExp.Coreturn;
import fql.decl.TransExp.FF;
import fql.decl.TransExp.Fst;
import fql.decl.TransExp.Id;
import fql.decl.TransExp.Inl;
import fql.decl.TransExp.Inr;
import fql.decl.TransExp.Prod;
import fql.decl.TransExp.Return;
import fql.decl.TransExp.Snd;
import fql.decl.TransExp.Squash;
import fql.decl.TransExp.TT;
import fql.decl.TransExp.TransExpVisitor;
import fql.decl.TransExp.Var;
import fql.sql.CopyFlower;
import fql.sql.CreateTable;
import fql.sql.DropTable;
import fql.sql.Flower;
import fql.sql.FullSigmaCounit;
import fql.sql.FullSigmaTrans;
import fql.sql.InsertKeygen;
import fql.sql.InsertSQL;
import fql.sql.InsertSQL2;
import fql.sql.InsertValues;
import fql.sql.PSM;
import fql.sql.PSMGen;
import fql.sql.Relationalizer;
import fql.sql.SQL;
import fql.sql.SimpleCreateTable;
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
					+ "_" + k.string, "c0", "c1"), "c0", "c1"));
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
			from.put("lft", el + "_" + k);
			from.put("rght", er + "_" + k);

			List<Pair<Pair<String, String>, Pair<String, String>>> where = new LinkedList<>();
			where.add(new Pair<>(new Pair<>("lft", "c1"), new Pair<>("rght",
					"c0")));

			LinkedHashMap<String, Pair<String, String>> select = new LinkedHashMap<>();
			select.put("c0", new Pair<>("lft", "c0"));
			select.put("c1", new Pair<>("rght", "c1"));

			Flower f = new Flower(select, from, where);

			ret.add(new InsertSQL(dst + "_" + k, f, "c0", "c1"));
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
			if (values.size() > 0) {
				ret.add(new InsertValues("pre_" + dst + "_" + k.string, attrs,
						values));
			}

			SQL f = PSMGen.compose(new String[] {
					e.src + "_" + k.string + "_subst_inv",
					"pre_" + dst + "_" + k.string,
					e.dst + "_" + k.string + "_subst" });
			ret.add(new InsertSQL(dst + "_" + k.string, f, "c0", "c1"));
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
				LinkedHashMap<String, Pair<String, String>> select = new LinkedHashMap<>();
				select.put("c0", new Pair<>("t1", "c0"));
				select.put("c1", new Pair<>("t2", "c0"));
				Flower f = new Flower(select, from, where);
				ret.add(new InsertSQL(dst + "_" + n, f, "c0", "c1"));

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
					+ "_fst_" + n.string, "c0", "c1"), "c0", "c1"));
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
					+ "_snd_" + n.string, "c0", "c1"), "c0", "c1"));
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
					+ "_inl_" + n.string, "c0", "c1"), "c0", "c1"));
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
					+ "_inr_" + n.string, "c0", "c1"), "c0", "c1"));
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

	@Override
	public List<PSM> visit(String dst, fql.decl.TransExp.Delta e) {
		List<PSM> ret = new LinkedList<>();
		Pair<String, String> ht = e.h.type(prog);
		Signature sig = prog.insts.get(ht.first).type(prog).toSig(prog);

		// Signature sig = prog.insts.get(e.src).type(prog).toSig(prog);

		Mapping F = ((Delta) prog.insts.get(e.src)).F.toMap(prog);

		String next = next();
		ret.addAll(PSMGen.makeTables(next, sig, false));
		ret.addAll(e.h.accept(next, this));

		Signature sig2 = prog.insts.get(e.src).type(prog).toSig(prog);

		for (Node n : sig2.nodes) {
			String fc = F.nm.get(n).string;
			ret.add(new InsertSQL(
					dst + "_" + n.string,
					PSMGen.compose(new String[] {
							e.src + "_" + n.string + "_subst_inv",
							next + "_" + fc, e.dst + "_" + n.string + "_subst" }),
					"c0", "c1"));
		}

		ret.addAll(PSMGen.dropTables(next, sig));
		return ret;
	}

	// TODO remove comma seps in FQL 4 for tuples

	@Override
	public List<PSM> visit(String dst, fql.decl.TransExp.Sigma e) {
		List<PSM> ret = new LinkedList<>();
		Pair<String, String> ht = e.h.type(prog);
		Signature sig = prog.insts.get(ht.first).type(prog).toSig(prog);

		Mapping F = ((Sigma) prog.insts.get(e.src)).F.toMap(prog);

		String next = next();
		ret.addAll(PSMGen.makeTables(next, sig, false));
		ret.addAll(e.h.accept(next, this));

		Signature sig2 = prog.insts.get(e.src).type(prog).toSig(prog);
		String xxx = "sigfunc";
		ret.addAll(PSMGen.makeTables(xxx, sig2, false));

		for (Node n : sig2.nodes) {
			List<Flower> l = new LinkedList<>();
			for (Node m : F.source.nodes) {
				if (F.nm.get(m).equals(n)) {
					l.add(new CopyFlower(next + "_" + m.string, "c0", "c1"));
				}
			}
			String yyy = xxx + "_" + n.string;
			if (l.size() == 0) {

			} else if (l.size() == 0) {
				ret.add(new InsertSQL(yyy, l.get(0), "c0", "c1"));
			} else {
				ret.add(new InsertSQL(yyy, new Union(l), "c0", "c1"));
			}
			ret.add(new InsertSQL(dst + "_" + n.string, PSMGen
					.compose(new String[] {
							e.src + "_" + n.string + "_subst_inv", yyy,
							e.dst + "_" + n.string + "_subst" }), "c0", "c1"));
		}
		ret.addAll(PSMGen.dropTables(xxx, sig2));
		ret.addAll(PSMGen.dropTables(next, sig));
		return ret;

	}

	@Override
	public List<PSM> visit(String dst, fql.decl.TransExp.FullSigma e) {
		List<PSM> ret = new LinkedList<>();

		Mapping F0 = ((FullSigma) prog.insts.get(e.src)).F.toMap(prog);

		Pair<String, String> t = prog.transforms.get(e.h).type(prog);

		// String next = next();
		// ret.addAll(PSMGen.makeTables(next, F0.source, false));
		// ret.addAll(e.h.accept(next, this));
		ret.add(new FullSigmaTrans(F0, t.first, e.src, t.second, e.dst, e.h,
				dst));

		return ret;
	}

	@Override
	public List<PSM> visit(String dst, fql.decl.TransExp.Pi e) {
		try {
			List<PSM> ret = new LinkedList<>();
			Pair<String, String> ht = e.h.type(prog);
			Signature sig = prog.insts.get(ht.first).type(prog).toSig(prog);

			Mapping F = ((Pi) prog.insts.get(e.src)).F.toMap(prog);

			Map<String, Triple<Node, Node, Arr<Node, Path>>[]> colmap1x = PSMGen
					.pi(F, e.h.type(prog).first, e.src).second;

			String next = next();
			ret.addAll(PSMGen.makeTables(next, sig, false));
			ret.addAll(e.h.accept(next, this));

			Signature sig2 = prog.insts.get(e.src).type(prog).toSig(prog);

			for (Node n : sig2.nodes) {
				List<Pair<Pair<String, String>, Pair<String, String>>> where = new LinkedList<>();
				Map<String, String> from = new HashMap<>();
				from.put("limit1", e.src + "_" + n.string + "_limit");
				from.put("limit2", e.dst + "_" + n.string + "_limit");
				LinkedHashMap<String, Pair<String, String>> select = new LinkedHashMap<>();
				int i = 0;
				for (Triple<Node, Node, Arr<Node, Path>> col : colmap1x
						.get(n.string)) {
					from.put("l" + i, next + "_" + col.second.string);
					where.add(new Pair<>(new Pair<>("l" + i, "c0"), new Pair<>(
							"limit1", "c" + i)));
					where.add(new Pair<>(new Pair<>("l" + i, "c1"), new Pair<>(
							"limit2", "c" + i)));
					i++;
				}
				// here a is unused because attributes will be in this order
				// TODO check
				for (@SuppressWarnings("unused")
				Attribute<Node> a : sig2.attrsFor(n)) {
					where.add(new Pair<>(new Pair<>("limit1", "c" + i),
							new Pair<>("limit2", "c" + i)));
					i++;
				}
				select.put("c0", new Pair<>("limit1", "guid"));
				select.put("c1", new Pair<>("limit2", "guid"));
				Flower f = new Flower(select, from, where);
				// System.out.println("flower " + f);
				ret.add(new InsertSQL(dst + "_" + n.string, f, "c0", "c1"));
			}
			// ret.addAll(PSMGen.dropTables(xxx, sig2));
			// ret.addAll(PSMGen.dropTables(next, sig));
			return ret;
		} catch (FQLException fe) {
			fe.printStackTrace();
			throw new RuntimeException(fe.getLocalizedMessage());
		}

	}

	@Override
	public List<PSM> visit(String dst, fql.decl.TransExp.Relationalize e) {
		List<PSM> ret = new LinkedList<>();
		Pair<String, String> ht = e.h.type(prog);
		Signature sig = prog.insts.get(ht.first).type(prog).toSig(prog);

		// Signature sig = prog.insts.get(e.src).type(prog).toSig(prog);

		// Mapping F = ((Relationalize)prog.insts.get(e.src)).F.toMap(prog);

		String next = next();
		ret.addAll(PSMGen.makeTables(next, sig, false));
		ret.addAll(e.h.accept(next, this));

		// Signature sig2 = prog.insts.get(e.src).type(prog).toSig(prog);
		Map<String, String> attrs = new HashMap<>();
		attrs.put("c0", PSM.VARCHAR());
		attrs.put("c1", PSM.VARCHAR());
		for (Node n : sig.nodes) {
			// String fc = F.nm.get(n).string;

			ret.add(new CreateTable(n.string + "xxx_temp", attrs, false));
			ret.add(new CreateTable(n.string + "yyy_temp", attrs, false));

			LinkedHashMap<String, Pair<String, String>> select = new LinkedHashMap<>();
			select.put("c0", new Pair<>("l", "c0"));
			select.put("c1", new Pair<>("r", "c0"));
			Map<String, String> from = new HashMap<>();
			from.put("l", e.src + "_" + n.string + "_subst_inv");
			from.put("r", e.src + "_" + n.string + "_squash");
			List<Pair<Pair<String, String>, Pair<String, String>>> where = new LinkedList<>();
			where.add(new Pair<>(new Pair<>("l", "c1"), new Pair<>("r", "c1")));
			Flower jk = new Flower(select, from, where);
			ret.add(new InsertSQL(n.string + "yyy_temp", jk, "c0", "c1"));
			ret.add(new InsertSQL(n.string + "xxx_temp", PSMGen
					.compose(new String[] { next + "_" + n.string,
							e.dst + "_" + n.string + "_squash",
							e.dst + "_" + n.string + "_subst" }), "c0", "c1"));
			ret.add(new InsertSQL(dst + "_" + n.string, PSMGen
					.compose(new String[] { n.string + "yyy_temp",
							n.string + "xxx_temp" }), "c0", "c1"));
			ret.add(new DropTable(n.string + "xxx_temp"));
			ret.add(new DropTable(n.string + "yyy_temp"));
		}

		ret.addAll(PSMGen.dropTables(next, sig));
		return ret;
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
				l.add(new CopyFlower(e.a + "_" + n.string, "c0", "c1"));
				l.add(new CopyFlower(e.b + "_" + n.string, "c0", "c1"));
				ret.add(new InsertSQL(dst + "_" + n.string, new Union(l), "c0",
						"c1"));
			}
			for (Attribute<Node> n : s.attrs) {
				List<Flower> l = new LinkedList<>();
				l.add(new CopyFlower(e.a + "_" + n.name, "c0", "c1"));
				l.add(new CopyFlower(e.b + "_" + n.name, "c0", "c1"));
				ret.add(new InsertSQL(dst + "_" + n.name, new Union(l), "c0",
						"c1"));
			}
			for (Edge n : s.edges) {
				List<Flower> l = new LinkedList<>();
				l.add(new CopyFlower(e.a + "_" + n.name, "c0", "c1"));
				l.add(new CopyFlower(e.b + "_" + n.name, "c0", "c1"));
				ret.add(new InsertSQL(dst + "_" + n.name, new Union(l), "c0",
						"c1"));
			}

			ret.addAll(PSMGen.guidify(dst, s, true));

			ret.addAll(PSMGen.makeTables(dst + "_inl", s, false));
			ret.addAll(PSMGen.makeTables(dst + "_inr", s, false));

			for (Node n : s.nodes) {
				SQL f = PSMGen.compose(new String[] {
				/* dst + "_" + n.string + "_subst_inv", */
				e.a + "_" + n.string, dst + "_" + n.string + "_subst" });
				ret.add(new InsertSQL(dst + "_inl_" + n.string, f, "c0", "c1"));
				SQL f0 = PSMGen.compose(new String[] {
				/* dst + "_" + n.string + "_subst_inv", */
				e.b + "_" + n.string, dst + "_" + n.string + "_subst" });
				ret.add(new InsertSQL(dst + "_inr_" + n.string, f0, "c0", "c1"));
			}
			// (f+g) : A+B -> C f : A -> C g : B -> C
			Fn<Quad<String, String, String, String>, List<PSM>> fn = new Fn<Quad<String, String, String, String>, List<PSM>>() {
				@Override
				public List<PSM> of(Quad<String, String, String, String> x) {
					String f = x.first; // e.a -> x.third
					String g = x.second; // e.b -> x.third
					// String C = x.third;
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
								flowers), "c0", "c1"));
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
				LinkedHashMap<String, Pair<String, String>> select = new LinkedHashMap<>();
				Map<String, String> from = new HashMap<>();
				List<String> attrs = new LinkedList<>();
				Map<String, String> attrsM = new HashMap<>();
				List<Pair<Pair<String, String>, Pair<String, String>>> where = new LinkedList<>();
				from.put("lft", e.a + "_" + n.string);
				from.put("rght", e.b + "_" + n.string);
				attrs.add("lft");
				attrs.add("rght");
				attrsM.put("lft", PSM.VARCHAR());
				attrsM.put("rght", PSM.VARCHAR());
				select.put("lft", new Pair<>("lft", "c0"));
				select.put("rght", new Pair<>("rght", "c0"));
				for (Attribute<Node> a : s.attrsFor(n)) {
					from.put("lft_" + a.name, e.a + "_" + a.name);
					from.put("right_" + a.name, e.b + "_" + a.name);
					where.add(new Pair<>(new Pair<>("lft_" + a.name, "c0"),
							new Pair<>("lft", "c0")));
					where.add(new Pair<>(new Pair<>("right_" + a.name, "c0"),
							new Pair<>("rght", "c0")));
					where.add(new Pair<>(new Pair<>("lft_" + a.name, "c1"),
							new Pair<>("right_" + a.name, "c1")));
					select.put(a.name, new Pair<>("lft_" + a.name, "c1"));
					attrs.add(a.name);
					attrsM.put(a.name, a.target.psm());
				}
				Flower f = new Flower(select, from, where);
				ret.add(new CreateTable(dst + "_prod_temp_" + n.string, attrsM,
						false));
				ret.add(new InsertSQL2(dst + "_prod_temp_" + n.string, f, attrs));
				Map<String, String> attrsM0 = new HashMap<>(attrsM);
				attrsM0.put("guid", PSM.VARCHAR());
				ret.add(new CreateTable(dst + "_prod_guid_" + n.string,
						attrsM0, false));
				ret.add(new InsertKeygen(dst + "_prod_guid_" + n.string,
						"guid", dst + "_prod_temp_" + n.string, attrs));

				List<Pair<Pair<String, String>, Pair<String, String>>> where0 = new LinkedList<>();

				from = new HashMap<>();
				from.put("t", dst + "_prod_guid_" + n.string);
				select = new LinkedHashMap<>();
				select.put("c0", new Pair<>("t", "guid"));
				select.put("c1", new Pair<>("t", "lft"));
				f = new Flower(select, from, where0);
				ret.add(new InsertSQL(dst + "_fst_" + n, f, "c0", "c1"));

				from = new HashMap<>();
				from.put("t", dst + "_prod_guid_" + n.string);
				select = new LinkedHashMap<>();
				select.put("c0", new Pair<>("t", "guid"));
				select.put("c1", new Pair<>("t", "rght"));
				f = new Flower(select, from, where0);
				ret.add(new InsertSQL(dst + "_snd_" + n, f, "c0", "c1"));

				LinkedHashMap<String, Pair<String, String>> select0 = new LinkedHashMap<>();
				select0.put("c0", new Pair<>("t", "guid"));
				select0.put("c1", new Pair<>("t", "guid"));
				Map<String, String> from0 = new HashMap<>();
				from0.put("t", dst + "_prod_guid_" + n.string);
				Flower sql = new Flower(select0, from0, where0);
				ret.add(new InsertSQL(dst + "_" + n.string, sql, "c0", "c1"));
				for (Attribute<Node> a : s.attrsFor(n)) {
					select0 = new LinkedHashMap<>();
					select0.put("c0", new Pair<>("t", "guid"));
					select0.put("c1", new Pair<>("t", a.name));
					from0 = new HashMap<>();
					from0.put("t", dst + "_prod_guid_" + n.string);
					sql = new Flower(select0, from0, where0);
					ret.add(new InsertSQL(dst + "_" + a.name, sql, "c0", "c1"));
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
						"srcGuid", "lft")));
				where.add(new Pair<>(new Pair<>("rightEdge", "c0"), new Pair<>(
						"srcGuid", "rght")));
				where.add(new Pair<>(new Pair<>("leftEdge", "c1"), new Pair<>(
						"dstGuid", "lft")));
				where.add(new Pair<>(new Pair<>("rightEdge", "c1"), new Pair<>(
						"dstGuid", "rght")));
				LinkedHashMap<String, Pair<String, String>> select = new LinkedHashMap<>();
				select.put("c0", new Pair<>("srcGuid", "guid"));
				select.put("c1", new Pair<>("dstGuid", "guid"));
				Flower f = new Flower(select, from, where);
				ret.add(new InsertSQL(dst + "_" + edge.name, f, "c0", "c1"));
			}

			Fn<Quad<String, String, String, String>, List<PSM>> fn = new Fn<Quad<String, String, String, String>, List<PSM>>() {
				@Override
				public List<PSM> of(Quad<String, String, String, String> x) {
					String f = x.first; // x.third -> e.a
					String g = x.second; // x.third -> e.b
					// String C = x.third;

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
						where.add(new Pair<>(new Pair<>("lim", "lft"),
								new Pair<>("f", "c1")));
						where.add(new Pair<>(new Pair<>("lim", "rght"),
								new Pair<>("g", "c1")));
						LinkedHashMap<String, Pair<String, String>> select = new LinkedHashMap<>();
						select.put("c0", new Pair<>("f", "c0"));
						select.put("c1", new Pair<>("lim", "guid"));
						Flower flower = new Flower(select, from, where);
						ret.add(new InsertSQL(dst0 + "_" + n.string, flower,
								"c0", "c1"));
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
			ret.addAll(PSMGen.guidify(dst, F0.target, true));
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
			Pair<List<PSM>, Map<String, Triple<Node, Node, Arr<Node, Path>>[]>> xxx = PSMGen
					.pi(F0, e.I, dst);
			ret.addAll(xxx.first);
			// ret.addAll(PSMGen.dropTables(next, F0.source));
			// ret.addAll(PSMGen.guidify(dst, F0.target)); //not necessary,
			// pi creates all new guids
			return new Pair<>(ret, (Object) xxx.second);
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
			// ret.addAll(PSMGen.guidify(dst, F0.target));
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
			ret.addAll(PSMGen.guidify(dst, sig, true));
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
		String next1 = next();
		String next2 = next();

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

			ret.addAll(PSMGen.pi(q.join, next1, next2).first);

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
		/*
		 * List<PSM> ret = new LinkedList<>(); Pair<List<PSM>, String> l =
		 * e.q.toFullQuery(prog).accept(e.e, this); ret.addAll(l.first);
		 * 
		 * Const sig = e.q.type(prog).second.toConst(prog); for (String n :
		 * sig.nodes) { ret.add(new InsertSQL(env + "_" + n, new
		 * CopyFlower(l.second + "_" + n, "c0", "c1"), "c0", "c1")); } for
		 * (Triple<String, String, String> n : sig.attrs) { ret.add(new
		 * InsertSQL(env + "_" + n.first, new CopyFlower(l.second + "_" +
		 * n.first, "c0", "c1"), "c0", "c1")); } for (Triple<String, String,
		 * String> n : sig.arrows) { ret.add(new InsertSQL(env + "_" + n.first,
		 * new CopyFlower(l.second + "_" + n.first, "c0", "c1"), "c0", "c1")); }
		 * 
		 * return new Pair<>(ret, new Object());
		 */
		throw new RuntimeException();
	}

	// //////////////////////////////////////

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
				// ex.printStackTrace();
				ret.addAll(PSMGen.SIGMA(F0, dst, src));
			}
			// ret.addAll(PSMGen.dropTables(next, F0.target));
			ret.addAll(PSMGen.guidify(dst, F0.target));
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
			ret.addAll(PSMGen.pi(F0, src, dst).first);

			// ret.addAll(PSMGen.dropTables(next, F0.target));
			// ret.addAll(PSMGen.guidify(dst, F0.target));
			return new Pair<>(ret, dst);
		} catch (FQLException fe) {
			fe.printStackTrace();
			throw new RuntimeException(fe.getLocalizedMessage());
		}
	}

	@Override
	public List<PSM> visit(String dst, Squash e) {
		List<PSM> ret = new LinkedList<>();

		InstExp s = prog.insts.get(e.src);
		Signature ty = s.type(prog).toSig(prog);
		// if (!(s instanceof fql.decl.InstExp.Relationalize)) {
		// throw new RuntimeException("Not a relationalize: " + e);
		// }
		// fql.decl.InstExp.Relationalize xxx = (fql.decl.InstExp.Relationalize)
		// s;

		for (Node n : ty.nodes) {
			ret.add(new InsertSQL(dst + "_" + n.string, PSMGen
					.compose(new String[] { e.src + "_" + n.string + "_squash",
							e.src + "_" + n.string + "_subst" }), "c0", "c1"));
		}

		return ret;
	}

	// src and dst will be guidified, hence, must apply that subst here
	@Override
	public List<PSM> visit(String env, fql.decl.TransExp.External e) {
		List<PSM> ret = new LinkedList<>();
		Signature sig = prog.insts.get(e.src).type(prog).toSig(prog);
		ret.addAll(PSMGen.makeTables(e.name, sig, false));

		for (Node n : sig.nodes) {
			ret.add(new InsertSQL(e.name + "_" + n.string, PSMGen
					.compose(new String[] {
							e.src + "_" + n.string + "_subst_inv",
							e.name + "_" + n.string,
							e.dst + "_" + n.string + "_subst" }), "c0", "c1"));
		}
		return ret;
	}

	@Override
	public List<PSM> visit(String env, Return e) {
	//	String xxx = "return_temp_xxx";
		List<PSM> ret = new LinkedList<>();
		InstExp i1 = prog.insts.get(e.inst);
		if (i1 instanceof InstExp.Delta) {
			String middle = ((InstExp.Delta) i1).I;
			InstExp i2 = prog.insts.get(middle); // can't be null
			Mapping f = ((InstExp.Delta) i1).F.toMap(prog);
			if (i2 instanceof InstExp.Sigma) {
				InstExp.Sigma input0 = ((InstExp.Sigma) i2);
				String input = input0.I;
				for (Node n : f.source.nodes) {
					ret.add(new InsertSQL(env + "_" + n.string, PSMGen
							.compose(new String[] { input + "_" + n.string,
									middle + "_" + f.nm.get(n) + "_subst",
									e.inst + "_" + n.string + "_subst" }),
							"c0", "c1"));
				}
			} else if (i2 instanceof InstExp.FullSigma) {
				InstExp.FullSigma input0 = ((InstExp.FullSigma) i2);
				String input = input0.I;
				for (Node n : f.source.nodes) {
					ret.add(new InsertSQL(env + "_" + n.string, PSMGen
							.compose(new String[] { input + "_" + n.string,
									middle + "_" + n.string + "_e",
									e.inst + "_" + n.string + "_subst" }),
							"c0", "c1"));
				}
			} else {
				throw new RuntimeException();
			}
		} else if (i1 instanceof InstExp.Pi) {
			String middle = ((InstExp.Pi) i1).I;
			InstExp i2 = prog.insts.get(middle); // can't be null
			Mapping f = ((InstExp.Pi) i1).F.toMap(prog);
			if (i2 instanceof InstExp.Delta) {
				InstExp.Delta input0 = ((InstExp.Delta) i2);
				String input = input0.I;
				for (Node n : f.target.nodes) {
					try {
						Map<String, Triple<Node, Node, Arr<Node, Path>>[]> colmap = PSMGen
								.pi(f, middle, e.inst).second;

						Triple<Node, Node, Arr<Node, Path>>[] col = colmap
								.get(n.string);
						if (col.length == 0) {
							LinkedHashMap<String, Pair<String, String>> select = new LinkedHashMap<>();
							Map<String, String> from = new HashMap<>();
							List<Pair<Pair<String, String>, Pair<String, String>>> where = new LinkedList<>();
							from.put("lim", e.inst + "_" + n.string + "_limit");
							from.put("middle", input + "_" + n.string);
							select.put("c0", new Pair<>("middle", "c0"));
							select.put("c1", new Pair<>("lim", "guid"));
							Flower flower = new Flower(select, from, where);
							ret.add(new InsertSQL(env + "_" + n.string, flower, "c0", "c1"));
							return ret;
						}
//						System.out.println("n " + n);
						// System.out.println("m " + m);
	//					System.out.println(Arrays.toString(col));

						LinkedHashMap<String, String> attrs = new LinkedHashMap<>();
						attrs.put("guid", PSM.VARCHAR());
						LinkedHashMap<String, Pair<String, String>> select = new LinkedHashMap<>();
						Map<String, String> from = new HashMap<>();
						from.put("lim", e.inst + "_" + n.string + "_limit");
						List<Pair<Pair<String, String>, Pair<String, String>>> where = new LinkedList<>();
						int i = 0;
						for (Triple<Node, Node, Arr<Node, Path>> col0 : col) {
							from.put("c" + i + "_subst_inv", middle + "_"
									+ col0.second.string + "_subst_inv");
							where.add(new Pair<>(new Pair<>("lim", "c" + i),
									new Pair<>("c" + i + "_subst_inv", "c0")));
							attrs.put("c" + i, PSM.VARCHAR());
							i++;
						}

				//		if (col.length > 1) {
							for (int j = 1; j < col.length; j++) {
								where.add(new Pair<>(new Pair<>("c" + 0
										+ "_subst_inv", "c1"), new Pair<>("c"
										+ j + "_subst_inv", "c1")));
							}
							
							select.put("c" + 0, new Pair<>("c" + 0
									+ "_subst_inv", "c1"));
							select.put("c1", new Pair<>("lim", "guid"));

					//	}

//						ret.add(new CreateTable(xxx, attrs, false));
						Flower flower = new Flower(select, from, where);
						ret.add(new InsertSQL(env + "_" + n.string, flower, "c0", "c1"));
					} catch (FQLException fe) {
						fe.printStackTrace();
						throw new RuntimeException(fe.getMessage());
					}

				}
			} else {
				throw new RuntimeException();
			}
		} else {
			throw new RuntimeException();
		}
		return ret;
	}

	@Override
	public List<PSM> visit(String env, Coreturn e) {
		String xxx = "coreturn_temp_xxx";

		List<PSM> ret = new LinkedList<>();
		InstExp i1 = prog.insts.get(e.inst);
		if (i1 instanceof InstExp.Sigma) {
			String middle = ((InstExp.Sigma) i1).I;
			InstExp i2 = prog.insts.get(middle); // can't be null
			Mapping f = ((InstExp.Sigma) i1).F.toMap(prog);
			if (i2 instanceof InstExp.Delta) {
			//	InstExp.Delta input0 = ((InstExp.Delta) i2);
				//String input = input0.I;
				for (Node n : f.target.nodes) {

					List<Flower> u = new LinkedList<Flower>();
					for (Node m : f.source.nodes) {
						if (!f.nm.get(m).equals(n)) {
							continue;
						}
						u.add(new CopyFlower(middle + "_" + m.string
								+ "_subst_inv", "c0", "c1"));
					}
					ret.add(new SimpleCreateTable(xxx, PSM.VARCHAR(), false));
					ret.add(new InsertSQL(xxx, new Union(u), "c0", "c1"));

					ret.add(new InsertSQL(env + "_" + n.string, PSMGen
							.compose(new String[] {
									e.inst + "_" + n.string + "_subst_inv",
									xxx, }), "c0", "c1"));

					ret.add(new DropTable(xxx));

					// ret.add(new InsertSQL(env + "_" + n.string,
					// PSMGen.compose(new String[] { input + "_" + n.string,
					// middle + "_" + f.nm.get(n) + "_subst",
					// e.inst + "_" + n.string + "_subst" }), "c0", "c1"));
				}
			}
		} else if (i1 instanceof InstExp.FullSigma) {
			String middle = ((InstExp.FullSigma) i1).I;
			InstExp i2 = prog.insts.get(middle); // can't be null
			Mapping f = ((InstExp.FullSigma) i1).F.toMap(prog);
			if (i2 instanceof InstExp.Delta) {
				ret.add(new FullSigmaCounit(f, ((InstExp.Delta) i2).I, middle, e.inst, env));
			} else {
				throw new RuntimeException();
			}
		} else if (i1 instanceof InstExp.Delta) {
			String middle = ((InstExp.Delta) i1).I;
			InstExp i2 = prog.insts.get(middle); // can't be null
			Mapping f = ((InstExp.Delta) i1).F.toMap(prog);
			if (i2 instanceof InstExp.Pi) {
				InstExp.Pi input0 = ((InstExp.Pi) i2);
				String input = input0.I;
				try {
					Map<String, Triple<Node, Node, Arr<Node, Path>>[]> colmap = PSMGen
							.pi(f, input, middle).second;
					for (Node m : f.source.nodes) {
						Node n = f.nm.get(m);
						Triple<Node, Node, Arr<Node, Path>>[] col = colmap
								.get(n.string);
						// System.out.println("n " + n);
						// System.out.println("m " + m);
						// System.out.println(Arrays.toString(col));

						Triple<Node, Node, Arr<Node, Path>> toFind = new Triple<>(
								n, m, new Arr<Node, Path>(
										new Path(f.target, n), n, n));
						int i = 0;
						boolean found = false;
						for (Triple<Node, Node, Arr<Node, Path>> cand : col) {
							if (cand.equals(toFind)) {
								found = true;
								Map<String, String> from = new HashMap<>();
								from.put("lim", middle + "_" + n + "_limit");
								LinkedHashMap<String, Pair<String, String>> select = new LinkedHashMap<>();
								select.put("c0", new Pair<>("lim", "guid"));
								select.put("c1", new Pair<>("lim", "c" + i));
								List<Pair<Pair<String, String>, Pair<String, String>>> where = new LinkedList<>();
								Flower flower = new Flower(select, from, where);
								ret.add(new SimpleCreateTable(xxx, PSM
										.VARCHAR(), false));
								ret.add(new InsertSQL(xxx, flower, "c0", "c1"));

								ret.add(new InsertSQL(
										env + "_" + m,
										PSMGen.compose(new String[] {
												e.inst + "_" + m + "_subst_inv",
												xxx }), "c0", "c1"));
								ret.add(new DropTable(xxx));
								break;
							}
							i++;
						}
						if (!found) {
							throw new RuntimeException();
						}
					}
				} catch (FQLException fe) {
					fe.printStackTrace();
					throw new RuntimeException(fe.getMessage());
				}
			} else {
				throw new RuntimeException();
			}
		} else {
			throw new RuntimeException();
		}

		return ret;
	}

}
