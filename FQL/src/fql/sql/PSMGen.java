package fql.sql;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import fql.DEBUG;
import fql.FQLException;
import fql.Fn;
import fql.Pair;
import fql.Triple;
import fql.cat.Arr;
import fql.cat.CommaCat;
import fql.cat.FinCat;
import fql.cat.FinFunctor;
import fql.decl.Attribute;
import fql.decl.ConstantInstanceDecl;
import fql.decl.Decl;
import fql.decl.Edge;
import fql.decl.Environment;
import fql.decl.EvalDSPInstanceDecl;
import fql.decl.EvalInstanceDecl;
import fql.decl.Instance;
import fql.decl.Int;
import fql.decl.Mapping;
import fql.decl.Node;
import fql.decl.Path;
import fql.decl.Program;
import fql.decl.Query;
import fql.decl.Signature;
import fql.decl.Type;
import fql.decl.Varchar;
import fql.parse.ExternalDecl;

/**
 * 
 * @author ryan
 *
 * PSM generator.
 */
public class PSMGen {

	public static List<PSM> guidify(String pre0, Signature sig) {	
		// System.out.println("GUIDifying " + pre0);
		List<PSM> ret = new LinkedList<>();

		if (DEBUG.DO_NOT_GUIDIFY) {
			return ret;
		}
		
		Map<String, String> guid_attrs = new HashMap<>();
		Map<String, String> twocol_attrs = new HashMap<>();

		twocol_attrs.put("c0", PSM.VARCHAR());
		twocol_attrs.put("c1", PSM.VARCHAR());
		guid_attrs.put("c0", PSM.VARCHAR());
		guid_attrs.put("c1", PSM.VARCHAR());
		guid_attrs.put("guid", PSM.VARCHAR());

		List<String> attrs_foo = new LinkedList<>();
		attrs_foo.add("c0");
		attrs_foo.add("c1");

		for (Node n : sig.nodes) {
			String pre = pre0 + "_" + n;

			// make new table with GUID
			ret.add(new CreateTable(pre + "_guid", guid_attrs, false));
			ret.add(new InsertKeygen(pre + "_guid", "guid", pre, attrs_foo));

			// make a substitution table
			ret.add(new CreateTable(pre + "_subst", twocol_attrs, false));
			ret.add(new InsertSQL(pre + "_subst", makeSubst(pre0, n)));

			ret.add(new CreateTable(pre + "_subst_inv", twocol_attrs, false));
			ret.add(new InsertSQL(pre + "_subst_inv", invertSubst(pre0, n)));

			// create a new table that applies the substitution
			ret.add(new CreateTable(pre + "_applied", twocol_attrs, false));
			ret.add(new InsertSQL(pre + "_applied", makeApplyNode(pre0, n)));

			// drop guid table
			ret.add(new DropTable(pre + "_guid"));

			// drop original table
			ret.add(new DropTable(pre));

			// copy the new table
			ret.add(new CreateTable(pre, twocol_attrs, false));
			ret.add(new InsertSQL(pre, new CopyFlower(pre + "_applied")));

			// drop the new table
			ret.add(new DropTable(pre + "_applied"));
		}

		for (Edge e : sig.edges) {
			String pre = pre0 + "_" + e.name;

			// create a new table that applies the substitution
			ret.add(new CreateTable(pre + "_applied", twocol_attrs, false));
			ret.add(new InsertSQL(pre + "_applied", makeApplyEdge(pre0, e)));

			// drop original table
			ret.add(new DropTable(pre));

			// copy the new table
			ret.add(new CreateTable(pre, twocol_attrs, false));
			ret.add(new InsertSQL(pre, new CopyFlower(pre + "_applied")));

			// drop the new table
			ret.add(new DropTable(pre + "_applied"));

		}

		for (Attribute<Node> a : sig.attrs) {
			String pre = pre0 + "_" + a.name;

			// create a new table that applies the substitution

			ret.add(new CreateTable(pre + "_applied", colattrs(a), false));
			ret.add(new InsertSQL(pre + "_applied", makeAttr(pre0, a)));

			// drop original table
			ret.add(new DropTable(pre));

			// copy the new table
			ret.add(new CreateTable(pre, twocol_attrs, false));
			ret.add(new InsertSQL(pre, new CopyFlower(pre + "_applied")));

			// drop the new table
			ret.add(new DropTable(pre + "_applied"));

		}

		// same for attributes, but one sided

		for (Node n : sig.nodes) {
			String pre = pre0 + "_" + n;
			ret.add(new DropTable(pre + "_subst"));
			ret.add(new DropTable(pre + "_subst_inv"));
		}
		// System.out.println("&&&&&&&&&&&&");
		// System.out.println(ret);
		return ret;
	}

	private static Map<String, String> colattrs(Attribute<Node> a) {
		Map<String, String> twocol_attrs = new HashMap<>();
		twocol_attrs.put("c0", PSM.VARCHAR());
		twocol_attrs.put("c1", typeTrans(a.target));
		return twocol_attrs;
	}

	private static SQL invertSubst(String pre0, Node n) {
		String pre = pre0 + "_" + n;
		Map<String, Pair<String, String>> select = new HashMap<>();
		select.put("c0", new Pair<>(pre + "_subst", "c1"));
		select.put("c1", new Pair<>(pre + "_subst", "c0"));

		Map<String, String> from = new HashMap<>();
		from.put(pre + "_subst", pre + "_subst");

		List<Pair<Pair<String, String>, Pair<String, String>>> where = new LinkedList<>();

		Flower f = new Flower(select, from, where);

		return f;
	}

	private static SQL makeApplyEdge(String i, Edge e) {

		String src = e.source.string;
		String dst = e.target.string;

		SQL f = compose(new String[] { i + "_" + src + "_subst_inv",
				i + "_" + e.name, i + "_" + dst + "_subst" });

		// System.out.println("apply edge for " + e + " on " + i + " is " + f);

		return f;
	}

	private static SQL makeAttr(String i, Attribute<Node> a) {

		String src = a.source.string;

		SQL f = compose(new String[] { i + "_" + src + "_subst_inv",
				i + "_" + a.name });

		// System.out.println("apply aatr for " + a + " on " + i + " is " + f);

		return f;
	}

	private static SQL makeApplyNode(String i, Node n) {
		List<Pair<Pair<String, String>, Pair<String, String>>> where = new LinkedList<>();

		Map<String, String> from = new HashMap<>();
		from.put(i + "_" + n.string + "_guid", i + "_" + n.string + "_guid");

		Map<String, Pair<String, String>> select = new HashMap<>();
		select.put("c0", new Pair<>(i + "_" + n.string + "_guid", "guid"));
		select.put("c1", new Pair<>(i + "_" + n.string + "_guid", "guid"));

		return new Flower(select, from, where);
	}

	// project guid as c0, c0 as c1 from i_n_guid
	private static SQL makeSubst(String i, Node n) {
		List<Pair<Pair<String, String>, Pair<String, String>>> where = new LinkedList<>();

		Map<String, String> from = new HashMap<>();
		from.put(i + "_" + n.string + "_guid", i + "_" + n.string + "_guid");

		Map<String, Pair<String, String>> select = new HashMap<>();
		select.put("c1", new Pair<>(i + "_" + n.string + "_guid", "guid"));
		select.put("c0", new Pair<>(i + "_" + n.string + "_guid", "c0"));

		return new Flower(select, from, where);
	}

	public static List<PSM> compile0(Environment env, Program prog)
			throws FQLException {
		List<PSM> ret = new LinkedList<>();
		for (Decl d : prog.decls) {
			if (d instanceof ConstantInstanceDecl) {
				ret.addAll(addInstance(env, (ConstantInstanceDecl) d));
			} else if (d instanceof EvalInstanceDecl) {
				List<PSM> xxx = addInstance(env, (EvalInstanceDecl) d);
			//	System.out.println("xxxxxxxxx" + xxx);
				ret.addAll(xxx);
			} else if (d instanceof EvalDSPInstanceDecl) {
				ret.addAll(addInstance(env, (EvalDSPInstanceDecl) d));
			} else if (d instanceof ExternalDecl) {
				ret.addAll(addInstance(env, (ExternalDecl)d));
			}
		}
		return ret;
	}

	public static String compile(Environment env, Program prog)
			throws FQLException {
		return DEBUG.prelude + "\n\n" + prettyPrint(compile0(env, prog));
	}

	private static String prettyPrint(List<PSM> l) {
		String ret = "";
		for (PSM p : l) {
			String s = p.toPSM();
			if (s.trim().length() == 0) {
				continue;
			}
			ret += p.toPSM() + "\n\n";
		}
		return ret;
	}

	private static List<PSM> addInstance(Environment env, EvalInstanceDecl d) throws FQLException {
		List<PSM> ret = new LinkedList<>();

		String inst = d.inst;
		String name = d.name;
		Query q = env.queries.get(d.query);
		//Signature t = env.signatures.get(d.type);
		
		Mapping proj = q.project;
		Mapping join = q.join;
		Mapping un = q.union;
		
		//System.out.println(q);
		
		EvalDSPInstanceDecl delta = new EvalDSPInstanceDecl(name + "_tempdelta", "delta", proj.name, inst, proj.source.name0);
		EvalDSPInstanceDecl pi = new EvalDSPInstanceDecl(name + "_temppi", "pi", join.name, name + "_tempdelta", join.target.name0);
		EvalDSPInstanceDecl sigma = new EvalDSPInstanceDecl(name, "sigma", un.name, name + "_temppi", un.target.name0);
		
		
//		System.out.println(delta);
//		System.out.println(pi);
//		System.out.println(sigma);
//		//ret.addAll(makeTables(name + "_tempdelta", proj.source));
		ret.addAll(addInstance(env, delta));
		//ret.addAll(makeTables(name + "_temppi", join.target));
		ret.addAll(addInstance(env, pi));
		//ret.addAll(makeTables(name, un.target));
		ret.addAll(addInstance(env, sigma));
		ret.addAll(dropTables(name + "_tempdelta", proj.source));
		ret.addAll(dropTables(name + "_temppi", join.target));
		ret.addAll(dropTables(name + "_tempsigma", un.target));
		//System.out.println("zzzzzzz" + ret);
		return ret;
	}

	private static List<PSM> dropTables(String name, Signature sig) {
		List<PSM> ret = new LinkedList<>();

		for (Node n : sig.nodes) {		
			ret.add(new DropTable(name + "_" + n.string));
		}
		for (Edge e : sig.edges) {
			ret.add(new DropTable(name + "_" + e.name));
		}
		for (Attribute<Node> a : sig.attrs) {
			ret.add(new DropTable(name + "_" + a.name));
		}

		return ret;
	}

	private static List<PSM> addInstance(Environment env, EvalDSPInstanceDecl d)
			throws FQLException {
		List<PSM> ret = new LinkedList<>();

		String inst = d.inst;
		String name = d.name;
		
		if (d.kind.equals("relationalize")) {
			Signature s = env.signatures.get(d.type);
			//sig, outname, inname
			return Relationalizer.compile(s, d.name, d.mapping);
		}

		Mapping f = env.getMapping(d.mapping);

		if (d.kind.equals("delta")) {
			ret.addAll(makeTables(name, f.source, false));
			ret.addAll(delta(f, inst, name));
			ret.addAll(guidify(name, f.source));
			// System.out.println("adding " + delta(f, inst, name));
		} else if (d.kind.equals("sigma")) {
			f.okForSigma();
			ret.addAll(makeTables(name, f.target, false));
			ret.addAll(sigma(f, name, inst));
		//	not needed ret.addAll(guidify(name, f.target));
		} else if (d.kind.equals("SIGMA")) {
			ret.addAll(makeTables(name, f.target, false));
			ret.addAll(SIGMA(env, f, name, inst));
			ret.addAll(guidify(name, f.target));
		}
		else if (d.kind.equals("pi")) {
			f.okForPi();
			ret.addAll(makeTables(name, f.target, false));
			ret.addAll(pi(f, inst, name));
			// not needed ret.addAll(guidify(name, f.target));
		} else {
			throw new RuntimeException(d.kind);
		}

		return ret;
	}

	private static List<PSM> addInstance(Environment env, ConstantInstanceDecl d)
			throws FQLException {
		List<PSM> ret = new LinkedList<>();

		Signature sig = env.signatures.get(d.type);
		Instance inst = new Instance(d.name, sig, d.data);

		ret.addAll(makeTables(d.name, sig, false));

		for (Node n : sig.nodes) {
			ret.add(populateTable(d.name, n.string, inst.data.get(n.string)));
		}
		for (Edge e : sig.edges) {
			ret.add(populateTable(d.name, e.name, inst.data.get(e.name)));
		}
		for (Attribute<Node> a : sig.attrs) {
			ret.add(populateTable(d.name, a.name, inst.data.get(a.name)));
		}

		 ret.addAll(guidify(d.name, sig));

		return ret;
	}

	private static List<PSM> addInstance(Environment env, ExternalDecl d)
			throws FQLException {
		List<PSM> ret = new LinkedList<>();

		Signature sig = env.signatures.get(d.type);
		Instance inst = new Instance(d.name, sig);

		ret.addAll(makeTables(d.name, sig, true));

		for (Node n : sig.nodes) {
			ret.add(populateTable(d.name, n.string, inst.data.get(n.string)));
		}
		for (Edge e : sig.edges) {
			ret.add(populateTable(d.name, e.name, inst.data.get(e.name)));
		}
		for (Attribute<Node> a : sig.attrs) {
			ret.add(populateTable(d.name, a.name, inst.data.get(a.name)));
		}

		 ret.addAll(guidify(d.name, sig));

		return ret;
	}
	
	private static PSM populateTable(String iname, String tname,
			Set<Pair<Object, Object>> data) {

		List<String> attrs = new LinkedList<>();
		attrs.add("c0");
		attrs.add("c1");
		Set<Map<String, Object>> values = new HashSet<>();

		for (Pair<Object, Object> row : data) {
			Map<String, Object> m = new HashMap<>();
			m.put("c0", row.first);
			m.put("c1", row.second);
			values.add(m);
		}

		return new InsertValues(iname + "_" + tname, attrs, values);
	}

	private static List<PSM> makeTables(String name, Signature sig, boolean suppress) {
		List<PSM> ret = new LinkedList<>();

		for (Node n : sig.nodes) {
			Map<String, String> attrs = new HashMap<>();
			attrs.put("c0", PSM.VARCHAR());
			attrs.put("c1", PSM.VARCHAR());
			ret.add(new CreateTable(name + "_" + n.string, attrs, suppress));
		}
		for (Edge e : sig.edges) {
			Map<String, String> attrs = new HashMap<>();
			attrs.put("c0", PSM.VARCHAR());
			attrs.put("c1", PSM.VARCHAR());
			ret.add(new CreateTable(name + "_" + e.name, attrs, suppress));
		}
		for (Attribute<Node> a : sig.attrs) {
			Map<String, String> attrs = new HashMap<>();
			attrs.put("c0", PSM.VARCHAR());
			attrs.put("c1", typeTrans(a.target));
			ret.add(new CreateTable(name + "_" + a.name, attrs, suppress));
		}

		return ret;
	}

	private static String typeTrans(Type t) {
		if (t instanceof Int) {
			return PSM.INTEGER;
		} else if (t instanceof Varchar) {
			return PSM.VARCHAR();
		}
		throw new RuntimeException();
	}

//	static String preamble = "DROP DATABASE FQL; CREATE DATABASE FQL; USE FQL; SET @guid := 0;\n\n";

	public static List<PSM> delta(Mapping m, String src, String dst) {
		//System.out.println("doing delta for " + m + " and " + src + " and " + dst + " and "+ m.name);
		Map<String, SQL> ret = new HashMap<>();
		for (Entry<Node, Node> n : m.nm.entrySet()) {
			ret.put(dst + "_" + n.getKey().string,
					new CopyFlower(src + "_" + n.getValue().string));
		}
		for (Entry<Edge, Path> e : m.em.entrySet()) {
			ret.put(dst + "_" + e.getKey().name, compose(src, e.getValue()));
		}
		for (Entry<Attribute<Node>, Attribute<Node>> a : m.am.entrySet()) {
			ret.put(dst + "_" + a.getKey().name,
					new CopyFlower(src + "_" + a.getValue().name));
		}
		List<PSM> ret0 = new LinkedList<>();
		for (String k : ret.keySet()) {
			SQL v = ret.get(k);
			ret0.add(new InsertSQL(k, v));
		}
		//System.out.println("result " + ret0);
		return ret0;
	}

	private static Flower compose(String[] p) {
		Map<String, Pair<String, String>> select = new HashMap<>();
		Map<String, String> from = new HashMap<>();
		List<Pair<Pair<String, String>, Pair<String, String>>> where = new LinkedList<>();

		// from.put("t0", pre + "_" + p.source.string);

		from.put("t0", p[0]);

		for (int i = 1; i < p.length; i++) {
			from.put("t" + i, p[i]);
			where.add(new Pair<>(new Pair<>("t" + (i - 1), "c1"), new Pair<>(
					"t" + i, "c0")));
		}

		select.put("c0", new Pair<>("t0", "c0"));
		select.put("c1", new Pair<>("t" + (p.length - 1), "c1"));

		return new Flower(select, from, where);
	}

	public static Flower compose(String pre, Path p) {
		Map<String, Pair<String, String>> select = new HashMap<>();
		Map<String, String> from = new HashMap<>();
		List<Pair<Pair<String, String>, Pair<String, String>>> where = new LinkedList<>();

		from.put("t0", pre + "_" + p.source.string);

		int i = 1;
		for (Edge e : p.path) {
			from.put("t" + i, pre + "_" + e.name);
			where.add(new Pair<>(new Pair<>("t" + (i - 1), "c1"), new Pair<>(
					"t" + i, "c0")));
			i++;
		}

		select.put("c0", new Pair<>("t0", "c0"));
		select.put("c1", new Pair<>("t" + (i - 1), "c1"));

		return new Flower(select, from, where);
	}

	public static List<PSM> SIGMA(Environment env, Mapping F, String pre, String inst)
			throws FQLException {
		List<PSM> ret = new LinkedList<>();
		
		ret.add(new FullSigma(F, pre, inst));
		
		return ret;
	}
	public static List<PSM> sigma(Mapping F, String pre, String inst)
			throws FQLException {
		Signature C = F.source;
		Signature D = F.target;
		List<PSM> ret = new LinkedList<>();

		if (!FinFunctor.isDiscreteOpFib(F.toFunctor2().first)) {
			throw new FQLException("Not a discrete op-fibration: " + F.name);
		}

		for (Node d : D.nodes) {
			List<Flower> tn = new LinkedList<>();
			for (Node c : C.nodes) {
				if (F.nm.get(c).equals(d)) {
					tn.add(new CopyFlower(inst + "_" + c.string));
				}
			}

			if (tn.size() == 0) {
				continue;
			}
			SQL y = foldUnion(tn);
			ret.add(new InsertSQL(pre + "_" + d.string, y));
		}

		for (Edge e : D.edges) {
			Node d = e.source;
			// Node d0 = e.target;
			List<Flower> tn = new LinkedList<>();
			for (Node c : C.nodes) {
				if (F.nm.get(c).equals(d)) {
					Path pc = findEquiv(c, F, e);
					Flower q = compose(inst, pc);
					tn.add(q);
				}
			}

			SQL y = foldUnion(tn);
			ret.add(new InsertSQL(pre + "_" + e.name, y));
		}

		for (Attribute<Node> a : D.attrs) {
			Node d = a.source;
			// Node d0 = e.target;
			List<Flower> tn = new LinkedList<>();
			for (Node c : C.nodes) {
				if (F.nm.get(c).equals(d)) {
					Attribute<Node> pc = findEquiv(c, F, a);
					Flower q = new CopyFlower(inst + "_" + pc.name);
					tn.add(q);
				}
			}

			SQL y = foldUnion(tn);
			ret.add(new InsertSQL(pre + "_" + a.name, y));
		}

		return ret;
	}

	private static SQL foldUnion(List<Flower> tn) {
		if (tn.size() == 0) {
			throw new RuntimeException("Empty Union");
		}
		if (tn.size() == 1) {
			return tn.get(0);
		}
		return new Union(tn);
	}

	private static Attribute<Node> findEquiv(Node c, Mapping f, Attribute<Node> a)
			throws FQLException {
	//	Signature C = f.source;
		for (Attribute<Node> peqc : f.source.attrs) {
			if (!peqc.source.equals(c)) {
				continue;
			}
			if (f.am.get(peqc).equals(a)) {
				return peqc;
			}
		}
		throw new FQLException("Could not find attribute mapping to " + a);
	}

	private static Path findEquiv(Node c, Mapping f, Edge e)
			throws FQLException {
		Signature C = f.source;
		Signature D = f.target;
		FinCat<Node, Path> C0 = C.toCategory2().first;
		for (Arr<Node, Path> peqc : C0.arrows) {
			Path path = peqc.arr;
			// Path path = new Path(f.source, p);
			if (!path.source.equals(c)) {
				continue;
			}
			Path path_f = f.appy(path);
			Fn<Path, Arr<Node, Path>> F = D.toCategory2().second;
			if (F.of(path_f).equals(F.of(new Path(D, e)))) {
				return path;
			}
		}
		throw new FQLException("Could not find path mapping to " + e);
	}

	// int

	public static List<PSM> pi(Mapping F0, String src, String dst)
			throws FQLException {
		tempTables = 0;
		Signature D0 = F0.target;
		Signature C0 = F0.source;
		Pair<FinCat<Node, Path>, Fn<Path, Arr<Node, Path>>> kkk = D0
				.toCategory2();
		FinCat<Node, Path> D = kkk.first;
		FinCat<Node, Path> C = C0.toCategory2().first;
		FinFunctor<Node, Path, Node, Path> F = F0.toFunctor2().first;
		List<PSM> ret = new LinkedList<>();

		Map<String, Triple<Node, Node, Arr<Node, Path>>[]> colmap = new HashMap<>();
		Map<String, Attribute<Node>[]> amap = new HashMap<>();
		// Map<Node, CommCat>
		for (Node d0 : D.objects) {
			CommaCat<Node, Path, Node, Path, Node, Path> B = doComma(D, C, F,
					d0, D0);

		//	System.out.println("Comma cat is " + B);
			
			Map<Triple<Node, Node, Arr<Node, Path>>, String> xxx1 = new HashMap<>();
			Map<Pair<Arr<Node, Path>, Arr<Node, Path>>, String> xxx2 = new HashMap<>();
			List<PSM> xxx3 = deltaX(src, xxx1, xxx2, B.projB);
			ret.addAll(xxx3);

			//System.out.println("doing limit for " + d0);
			Triple<Flower, Triple<Node, Node, Arr<Node, Path>>[], Attribute<Node>[]> 
			xxx = lim(src, C0, D, B, xxx1, xxx2);
			
			//comma cat is empty, need unit for product
			if (xxx == null) {
				Map<String, String> attrs2 = new HashMap<>();
				attrs2.put("guid", PSM.VARCHAR());
				
				ret.add(new CreateTable(dst + "_" + d0.string + "_limit", attrs2, false));
				ret.add(new InsertEmptyKeygen(dst + "_" + d0.string + "_limit"));
				ret.add(new InsertSQL(dst + "_" + d0.string, new SquishFlower(dst
						+ "_" + d0.string + "_limit")));
				continue;
			}
			
			Triple<Node, Node, Arr<Node, Path>>[] cols = xxx.second;
//			String ggg = "";
//			for (Triple<Node, Node, Arr<Node, Path>> t : cols) {
//				ggg += " " + t;
//			}
			//System.out.println("Cols are " + ggg);

	//		System.out.println("done with limit");
			Flower r = xxx.first;

			colmap.put(d0.string, cols);
			//System.out.println("adding amap " + d0.string + " and ");
//			for (Attribute x : xxx.third) {
//				System.out.println("zzz" + x);
//			}
			amap.put(d0.string, xxx.third);

			Map<String, String> attrs1 = new HashMap<>();
			for (int i = 0; i < xxx.second.length; i++) {
				attrs1.put("c" + i, PSM.VARCHAR());
			}
			for (int j = 0; j < xxx.third.length; j++) {
				attrs1.put("c" + (xxx.second.length + j), typeTrans(xxx.third[j].target));
			}
			Map<String, String> attrs2 = new HashMap<>(attrs1);
			attrs2.put("guid", PSM.VARCHAR());

			List<String> attcs = new LinkedList<>(attrs1.keySet());

			ret.add(new CreateTable(dst + "_" + d0.string + "_limnoguid",
					attrs1, false));
			ret.add(new InsertSQL(dst + "_" + d0.string + "_limnoguid", r));

			ret.add(new CreateTable(dst + "_" + d0.string + "_limit", attrs2, false));
			ret.add(new InsertKeygen(dst + "_" + d0.string + "_limit", "guid",
					dst + "_" + d0.string + "_limnoguid", attcs));

			// craeted by createTables
			// ret.add(new CreateTable(dst + "_" + d0.string, twocol_attrs));
			ret.add(new InsertSQL(dst + "_" + d0.string, new SquishFlower(dst
					+ "_" + d0.string + "_limit")));

			
		}

		for (Edge s : F0.target.edges) {
			Node dA = s.source;

			Node dB = s.target;

			String q2 = dB.string;
			String q1 = dA.string;

			Triple<Node, Node, Arr<Node, Path>>[] q2cols = colmap.get(q2);
			Triple<Node, Node, Arr<Node, Path>>[] q1cols = colmap.get(q1);

			// List<Pair<Pair<String, String>, Pair<String, String>>> where =
			// subset(dst, q1cols, q2cols, q1, q2);

			// List<Pair<Pair<String, String>, Pair<String, String>>> where =
			// subset(D, kkk.second.of(new Path(D0, s)), dst, q1cols, q2cols,
			 //q1, q2);

			List<Pair<Pair<String, String>, Pair<String, String>>> where = subset(
					D, kkk.second.of(new Path(D0, s)), dst, q2cols, q1cols, q2,
					q1);
			Map<String, String> from = new HashMap<>();
			from.put(dst + "_" + q1 + "_limit_1", dst + "_" + q1 + "_limit");
			from.put(dst + "_" + q2 + "_limit_2", dst + "_" + q2 + "_limit");

			Map<String, Pair<String, String>> select = new HashMap<>();
			select.put("c0", new Pair<>(dst + "_" + q1 + "_limit_1", "guid"));
			select.put("c1", new Pair<>(dst + "_" + q2 + "_limit_2", "guid"));

			Flower f = new Flower(select, from, where);

		//	System.out.println("flower is " + f);

			ret.add(new InsertSQL(dst + "_" + s.name, f));

		}
		
		for (Attribute<Node> a : F0.target.attrs) {
			int i = colmap.get(a.source.string).length;
			Attribute<Node>[] y = amap.get(a.source.string);
			//System.out.println("&&&& doing attr " + a);
			//System.out.println("amap is ");
//			for (Attribute z : y) {
//				System.out.println(z);
//			}
			boolean found = false;
			int u = 0;
			int j = -1;
			for (Attribute<Node> b : y) {
				if (!F0.am.get(b).equals(a)) {
					u++;
					continue;
				}
				if (found) {
					throw new FQLException("Attribute mapping not bijection " + a);
				}
				found = true;
				j = u;
				u++;
			}
			if (!found) {
				throw new FQLException("Attribute mapping not found " + a);
			}
			//System.out.println("i is " + i);
			//System.out.println("u is " + u);
			Map<String, Pair<String, String>> select = new HashMap<>();
			Map<String, String> from = new HashMap<>();
			from.put(dst + "_" + a.source + "_limit", dst + "_" + a.source + "_limit");
			select.put("c0", new Pair<>(dst + "_" + a.source + "_limit", "guid"));
			select.put("c1", new Pair<>(dst + "_" + a.source + "_limit", "c" + (j+i)));
			List<Pair<Pair<String, String>, Pair<String, String>>> where = new LinkedList<>();
			Flower f = new Flower(select, from, where);

			//System.out.println("attr flower is " + f);
			//System.out.println("inserting into " + dst + "_" + a.name);

			ret.add(new InsertSQL(dst + "_" + a.name, f));
			//project guid and u+i
		}
		
		for (Node d0 : D.objects) {
			ret.add(new DropTable(dst + "_" + d0.string + "_limnoguid")); 
	//		ret.add(new DropTable(dst + "_" + d0.string + "_limit"));
		}
		
		for (int ii = 0; ii < tempTables; ii++) {
			ret.add(new DropTable("temp" + ii));
		}

		return ret;
	}

	private static List<Pair<Pair<String, String>, Pair<String, String>>> subset(
			FinCat<Node, Path> cat, Arr<Node, Path> e, String pre,
			Triple<Node, Node, Arr<Node, Path>>[] q2cols,
			Triple<Node, Node, Arr<Node, Path>>[] q1cols, String q2name,
			String q1name) throws FQLException {
//		 System.out.println("trying subset " + print(q1cols) + " in " +
//		 print(q2cols));
		List<Pair<Pair<String, String>, Pair<String, String>>> ret = new LinkedList<>();
		//System.out.println("Arr " + e);
		//System.out.println("Cat" + cat);
		// turn e into arrow e', compute e' ; q2col, look for that
		/* a: */ for (int i = 0; i < q2cols.length; i++) {
			boolean b = false;
			for (int j = 0; j < q1cols.length; j++) {
				Triple<Node, Node, Arr<Node, Path>> q2c = q2cols[i];
				Triple<Node, Node, Arr<Node, Path>> q1c = q1cols[j];
//				System.out.println("^^^" + q1c);
//				System.out.println("^^^" + q2c);
//				System.out.println("compose " + cat.compose(e, q2c.third));
//				System.out.println("compose " + cat.compose(q2c.third, e));
//				System.out.println("compose " + cat.compose(e, q1c.third));
//				System.out.println("compose " + cat.compose(q1c.third, e));
//				// if (q1c.equals(q2c)) {
				
				if (q1c.third.equals(cat.compose(e, q2c.third)) && q1c.second.equals(q2c.second)) {
				//	System.out.println("hit on " + q2c.third);
					Pair<Pair<String, String>, Pair<String, String>> retadd = new Pair<>(new Pair<>(
							pre + "_" + q1name + "_limit_1", "c" + j),
							new Pair<String, String>(pre + "_" + q2name + "_limit_2", "c" + i));
					ret.add(retadd);
				//	if (b) throw new FQLException("not uniq: " + "lookup for " + q2c + " and " + q1c);
					
					b = true;
					
					//System.out.println("added to where: " +  retadd);
					//continue a;
				}
			}
			if (b) continue;
			String xxx = "";
			for (Triple<Node, Node, Arr<Node, Path>> yyy : q1cols) {
				xxx += ", " + yyy;
			}
			throw new RuntimeException("No col " + q2cols[i] + " in " + xxx
					+ " pre " + pre);

		}
		//System.out.println("where is " + ret);
		return ret;

		// a: for (int i = 0; i < q1cols.length; i++) {
		// for (int j = 0; j < q2cols.length; j++) {
		// if (q1cols[i].equals(q2cols[j])) {
		// int col1 = i+1;
		// int col2 = j+
		// // int col2 = j+2+q1cols.length;
		// ret.add(new Pair<>(new Pair<>(),new Pair<>()));
		// q1q2 = new Select(q1q2, i+1, j+2+q1cols.length);
		// continue a;
		// }
		// }
		// throw new RuntimeException("No col " + q1cols[i] + " in " + q2cols);
		// }
		// return ret;
	}

	private static CommaCat<Node, Path, Node, Path, Node, Path> doComma(
			FinCat<Node, Path> d2, FinCat<Node, Path> c,
			FinFunctor<Node, Path, Node, Path> f, Node d0, Signature S)
			throws FQLException {
		// List<String> x = new LinkedList<String>();
		// x.add(d0);
		// List<List<String>> y = new LinkedList<List<String>>();
		// y.add(x);

		FinFunctor<Node, Path, Node, Path> d = FinFunctor.singleton(d2, d0,
				new Arr<>(d2.identities.get(d0).arr, d0, d0));
		CommaCat<Node, Path, Node, Path, Node, Path> B = new CommaCat<>(
				d.srcCat, c, d2, d, f);
		return B;
	}

	public static Flower squish(String s) {
		return new SquishFlower(s);
	}

	@SuppressWarnings("unchecked")
	public static <Arrow> Triple<Flower, Triple<Node, Node, Arr<Node, Path>>[], Attribute<Node>[]> lim(
			String pre,
			Signature sig,
			FinCat<Node, Path> cat,
			CommaCat<Node, Path, Node, Path, Node, Path> b,
			Map<Triple<Node, Node, Arr<Node, Path>>, String> map,
			Map<Pair<Arr<Node, Path>, Arr<Node, Path>>, String> map2)
			throws FQLException {

		List<Pair<Pair<String, String>, Pair<String, String>>> where = new LinkedList<>();
		Map<String, String> from = new HashMap<>();
		Map<String, Pair<String, String>> select = new HashMap<>();

		int m = b.objects.size();
		// String[] cnames = new String[m];
		
		if (m == 0) {
			return null;
		}

		// String[] cnames2 = new String[b.arrows.size() - m];
		int temp = 0;
		Triple<Node, Node, Arr<Node, Path>>[] cnames = new Triple[m];

		List<Attribute<Node>> anames0 = new LinkedList<>();
		
		for (Triple<Node, Node, Arr<Node, Path>> n : b.objects) {
			from.put("t" + temp, map.get(n));
			// cnames[temp] = n.second.string;
			cnames[temp] = n;

			select.put("c" + temp, new Pair<>("t" + temp, "c0"));
			temp++;
			//System.out.println("&&&" + n);
		
		}
		
		for (Triple<Node, Node, Arr<Node, Path>> n : b.objects) {
			//System.out.println("doing triple " + n);
			if (cat.isId(n.third)) {
//				System.out.println("is id " + n);
//				System.out.println("attrs for " + n.second + " are " + sig.attrsFor(n.second) + " sig " + sig);
				for (Attribute<Node> a : sig.attrsFor(n.second)) {
					anames0.add(a);
				//	System.out.println("adding " + a);
					from.put("t" + temp, pre + "_" + a.name);
				// cnames[temp] = n.second.string;

					select.put("c" + temp, new Pair<>("t" + temp, "c1"));
				
					where.add(new Pair<>(new Pair<>("t" + cnamelkp(cnames, n), "c0"),
							new Pair<>("t" + temp,                    "c0")));
					temp++;
				}
			}
		}

		// Set<String> cnames_set = new HashSet<>();
		// System.out.println("***");
		// for (String s : cnames) {
		// System.out.println(s);
		// cnames_set.add(s);
		// }
		// System.out.println("***");
		//
		// if (cnames_set.size() != cnames.length) {
		// throw new RuntimeException();
		// }

		//temp = 0; VERY VERY BAD
		for (Arr<Triple<Node, Node, Arr<Node, Path>>, Pair<Arr<Node, Path>, Arr<Node, Path>>> e : b.arrows) {
			if (b.isId(e)) {
				continue;
			}
			//System.out.println("DOING ARR " + e);
			from.put("t" + temp, map2.get(e.arr));
			where.add(new Pair<>(new Pair<>("t" + temp, "c0"), new Pair<>("t"
					+ cnamelkp(cnames, e.src), "c0")));
			where.add(new Pair<>(new Pair<>("t" + temp, "c1"), new Pair<>("t"
					+ cnamelkp(cnames, e.dst), "c1")));
			temp++;
		}

		
		Flower f = new Flower(select, from, where);
		// System.out.println("flower is " + f);

		return new Triple<>(f, cnames, anames0.toArray((Attribute<Node>[])new Attribute[] { }));

	
	}

	private static <Obj> int cnamelkp(Obj[] cnames, Obj s) throws FQLException {
		for (int i = 0; i < cnames.length; i++) {
			if (s.equals(cnames[i])) {
				return i;
			}
		}
		throw new FQLException("Cannot lookup position of " + s + " in "
				+ cnames.toString());
	}

	static int tempTables = 0;

	private static List<PSM> deltaX(
			String pre,
			Map<Triple<Node, Node, Arr<Node, Path>>, String> ob,
			Map<Pair<Arr<Node, Path>, Arr<Node, Path>>, String> ar,
			FinFunctor<Triple<Node, Node, Arr<Node, Path>>, Pair<Arr<Node, Path>, Arr<Node, Path>>, Node, Path> projB) {
		Map<String, String> twocol_attrs = new HashMap<>();

		twocol_attrs.put("c0", PSM.VARCHAR());
		twocol_attrs.put("c1", PSM.VARCHAR());
		List<PSM> ret = new LinkedList<>();

		// Map<Triple<Node, Node, Arr<Node, Path>>, String> ret = new
		// HashMap<>();
		for (Entry<Triple<Node, Node, Arr<Node, Path>>, Node> p : projB.objMapping
				.entrySet()) {
			ob.put(p.getKey(), pre + "_" + p.getKey().second.string);
		}
		for (Entry<Arr<Triple<Node, Node, Arr<Node, Path>>, Pair<Arr<Node, Path>, Arr<Node, Path>>>, Arr<Node, Path>> p : projB.arrowMapping
				.entrySet()) {
			Path x = p.getKey().arr.second.arr;
			ret.add(new CreateTable("temp" + tempTables, twocol_attrs, false));
			ret.add(new InsertSQL("temp" + tempTables, compose(pre, x)));
			ar.put(p.getKey().arr, "temp" + tempTables++);
		}
		//System.out.println("DeltaX ret " + ret);
		return ret;
	}

	// private static Map<Triple<Node, Node, Arr<Node, Path>>, String> deltaObj(
	// FinFunctor<Triple<Node, Node, Arr<Node, Path>>, Pair<Arr<Node, Path>,
	// Arr<Node, Path>>, Node, Path> projB) {
	// Map<Triple<Node, Node, Arr<Node, Path>>, String> ret = new HashMap<>();
	// for (Entry<Triple<Node, Node, Arr<Node, Path>>, Node> p :
	// projB.objMapping
	// .entrySet()) {
	// ret.put(p.getKey(), new Relvar(p.getKey().second.string));
	// }
	// return ret;
	// }
	//
	// /** these bastardized versions of delta only works in support of pi
	// */
	// private static Map<Pair<Arr<Node, Path>, Arr<Node, Path>>, RA> deltaArr(
	// FinFunctor<Triple<Node, Node, Arr<Node, Path>>, Pair<Arr<Node, Path>,
	// Arr<Node, Path>>, Node, Path> projB) {
	// Map<Pair<Arr<Node, Path>, Arr<Node, Path>>, RA> ret = new HashMap<>();
	// for (Entry<Arr<Triple<Node, Node, Arr<Node, Path>>, Pair<Arr<Node, Path>,
	// Arr<Node, Path>>>, Arr<Node, Path>> p : projB.arrowMapping.entrySet()) {
	// Path x = p.getKey().arr.second.arr;
	// ret.put(p.getKey().arr, compose(x));
	// }
	//
	// return ret;
	// }

}
