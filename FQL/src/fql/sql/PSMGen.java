package fql.sql;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import fql.FQLException;
import fql.Fn;
import fql.Pair;
import fql.cat.Arr;
import fql.cat.FinCat;
import fql.cat.FinFunctor;
import fql.decl.Decl;
import fql.decl.Edge;
import fql.decl.Environment;
import fql.decl.EvalDSPInstanceDecl;
import fql.decl.EvalInstanceDecl;
import fql.decl.GivenInstanceDecl;
import fql.decl.Instance;
import fql.decl.Mapping;
import fql.decl.Node;
import fql.decl.Path;
import fql.decl.Program;
import fql.decl.Signature;

public class PSMGen {
	
	public static List<PSM> guidify(String pre0, Signature sig) {
	//	System.out.println("GUIDifying " + pre0);
		List<PSM> ret = new LinkedList<>();
	
		Map<String, String> guid_attrs = new HashMap<>();
		Map<String, String> twocol_attrs = new HashMap<>();
		twocol_attrs.put("c0", PSM.VARCHAR);
		twocol_attrs.put("c1", PSM.VARCHAR);
		guid_attrs.put("c0", PSM.VARCHAR);
		guid_attrs.put("c1", PSM.VARCHAR);
		guid_attrs.put("guid", PSM.VARCHAR);
		
		for (Node n : sig.nodes) {
			String pre = pre0 + "_" + n;

			//make new table with GUID
			ret.add(new CreateTable(pre + "_guid", guid_attrs));
			ret.add(new InsertKeygen(pre + "_guid", "guid", pre));
		
			//make a substitution table
			ret.add(new CreateTable(pre + "_subst", twocol_attrs));
			ret.add(new InsertSQL(pre + "_subst", makeSubst(pre0, n)));
			
			ret.add(new CreateTable(pre + "_subst_inv", twocol_attrs));
			ret.add(new InsertSQL(pre + "_subst_inv", invertSubst(pre0, n)));
			
			//create a new table that applies the substitution	
			ret.add(new CreateTable(pre + "_applied", twocol_attrs));
			ret.add(new InsertSQL(pre + "_applied", makeApplyNode(pre0, n)));
			
			//drop guid table
			ret.add(new DropTable(pre + "_guid"));
			
			//drop original table
			ret.add(new DropTable(pre));
			
			//copy the new table
			ret.add(new CreateTable(pre, twocol_attrs));
			ret.add(new InsertSQL(pre, new CopyFlower(pre + "_applied")));
			
			//drop the new table
			ret.add(new DropTable(pre + "_applied"));		
		}

		for (Edge e : sig.edges) {
			String pre = pre0 + "_" + e.name;
			
			//create a new table that applies the substitution	
			ret.add(new CreateTable(pre + "_applied", twocol_attrs));
			ret.add(new InsertSQL(pre + "_applied", makeApplyEdge(pre0, e)));
			
			//drop original table
			ret.add(new DropTable(pre));
			
			//copy the new table
			ret.add(new CreateTable(pre, twocol_attrs));
			ret.add(new InsertSQL(pre, new CopyFlower(pre + "_applied")));
			
			//drop the new table
			ret.add(new DropTable(pre + "_applied"));		

		}
		
		//same for attributes, but one sided
				
		for (Node n : sig.nodes) {
			String pre = pre0 + "_" + n;
			ret.add(new DropTable(pre + "_subst"));
			ret.add(new DropTable(pre + "_subst_inv"));
		}
//		System.out.println("&&&&&&&&&&&&");
//		System.out.println(ret);
		return ret;
	}

	private static SQL invertSubst(String pre0, Node n) {
		String pre = pre0 + "_" + n;
		Map<String, Pair<String, String>> select = new HashMap<>();
		select.put("c0", new Pair<>(pre + "_subst", "c1"));
		select.put("c1", new Pair<>(pre + "_subst", "c0"));
		
		Map<String, String> from = new HashMap<>();
		from.put(pre + "_subst", pre + "_subst");
		
		List<Pair<Pair<String, String>, Pair<String, String>>> where
		 = new LinkedList<>();
		
		Flower f = new Flower(select, from, where);
		
		return f;
	}

	private static SQL makeApplyEdge(String i, Edge e) {

		 String src = e.source.string;
		 String dst = e.target.string;

		SQL f = compose(new String[] { i + "_" + src + "_subst_inv",
				i + "_" + e.name, i + "_" + dst + "_subst"
		});
		
	//	System.out.println("apply edge for " + e + " on " + i + " is " + f);

		return f;
		 	}

	private static SQL makeApplyNode(String i, Node n) {
		List<Pair<Pair<String, String>, Pair<String, String>>> where
		 = new LinkedList<>();
		 
		Map<String, String> from = new HashMap<>();
		from.put(i + "_" + n.string + "_guid", i + "_" + n.string + "_guid");
		
		Map<String, Pair<String, String>> select = new HashMap<>();
		select.put("c0", new Pair<>(i + "_" + n.string + "_guid", "guid"));
		select.put("c1", new Pair<>(i + "_" + n.string + "_guid", "guid"));
		
		return new Flower(select, from, where);
	}

	//project guid as c0, c0 as c1 from i_n_guid
	private static SQL makeSubst(String i, Node n) {
		List<Pair<Pair<String, String>, Pair<String, String>>> where
		 = new LinkedList<>();
		 
		Map<String, String> from = new HashMap<>();
		from.put(i + "_" + n.string + "_guid", i + "_" + n.string + "_guid");
		
		Map<String, Pair<String, String>> select = new HashMap<>();
		select.put("c1", new Pair<>(i + "_" + n.string + "_guid", "guid"));
		select.put("c0", new Pair<>(i + "_" + n.string + "_guid", "c0"));
		
		return new Flower(select, from, where);
	}

	public static List<PSM> compile0(Environment env, Program prog) throws FQLException {
		List<PSM> ret = new LinkedList<>();
		for (Decl d : prog.decls) {
			if (d instanceof GivenInstanceDecl) {
				ret.addAll(addInstance(env, (GivenInstanceDecl) d));
			} else if (d instanceof EvalInstanceDecl) {
				ret.addAll(addInstance(env, (EvalInstanceDecl) d));
			} else if (d instanceof EvalDSPInstanceDecl) {
				ret.addAll(addInstance(env, (EvalDSPInstanceDecl) d));
			}
		}
		return ret;
	}
	
	public static String compile(Environment env, Program prog) throws FQLException {
		return preamble + prettyPrint(compile0(env, prog));
	}

	private static String prettyPrint(List<PSM> l) {
		String ret = "";
		for (PSM p : l) {
			ret += p.toPSM() + "\n\n";
		}
		return ret;
	}

	private static List<PSM> addInstance(Environment env, EvalInstanceDecl d) {
		List<PSM> ret = new LinkedList<>();
		
		return ret;
	}

	private static List<PSM> addInstance(Environment env, EvalDSPInstanceDecl d) throws FQLException {
		List<PSM> ret = new LinkedList<>();
		
		String inst = d.inst;
		Mapping f = env.getMapping(d.mapping);
		String name = d.name;
		

		
		if (d.type.equals("delta")) {
			ret.addAll(makeTables(name, f.source));
			ret.addAll(delta(f, inst, name));
			ret.addAll(guidify(name, f.source));
		//	System.out.println("adding " + delta(f, inst, name));
		} else if (d.type.equals("sigma")) {
			ret.addAll(makeTables(name, f.target));
			ret.addAll(sigma(name, inst, f));
			ret.addAll(guidify(name, f.target));
		} else if (d.type.equals("pi")) {
			
		} else {
			throw new RuntimeException(d.type);
		}
		
		return ret;
	}

	private static List<PSM> addInstance(Environment env, GivenInstanceDecl d) {
		List<PSM> ret = new LinkedList<>();
		
		Instance inst = env.instances.get(d.name);
		Signature sig = env.signatures.get(d.type);
		
		ret.addAll(makeTables(d.name, sig));

		for (Node n : sig.nodes) {
			ret.add(populateTable(d.name, n.string, inst.data.get(n.string)));
		}
		for (Edge e : sig.edges) {
			ret.add(populateTable(d.name, e.name, inst.data.get(e.name)));
		}
		
		ret.addAll(guidify(d.name, sig));
		
		return ret;
	}

	private static PSM populateTable(String iname,
			String tname, Set<Pair<String, String>> data) {
		
		List<String> attrs = new LinkedList<>();
		attrs.add("c0");
		attrs.add("c1");
		Set<Map<String, Object>> values = new HashSet<>();
		
		for (Pair<String, String> row : data) {
			Map<String, Object> m = new HashMap<>();
			m.put("c0", row.first);
			m.put("c1", row.second);
			values.add(m);
		}
		
		return new InsertValues(iname + "_" + tname, attrs, values);
	}

	private static List<PSM> makeTables(String name,
			Signature sig) {
		List<PSM> ret = new LinkedList<>();
		
		for (Node n : sig.nodes) {
			Map<String, String> attrs = new HashMap<>();
			attrs.put("c0", PSM.VARCHAR);
			attrs.put("c1", PSM.VARCHAR);
			ret.add(new CreateTable(name + "_" + n.string, attrs));
		}
		for (Edge e : sig.edges) {
			Map<String, String> attrs = new HashMap<>();
			attrs.put("c0", PSM.VARCHAR);
			attrs.put("c1", PSM.VARCHAR);
			ret.add(new CreateTable(name + "_" + e.name, attrs));
		}
		
		return ret;
	}
	
	static String preamble = "DROP DATABASE FQL; CREATE DATABASE FQL; USE FQL; SET @guid := 0;\n\n";

	public static List<PSM> delta(Mapping m, String src, String dst) {
		Map<String, SQL> ret = new HashMap<>();
		for (Entry<Node, Node> n : m.nm.entrySet()) {
			ret.put(dst + "_" + n.getKey().string, new CopyFlower(src + "_" + n.getValue().string));
		}
		for (Entry<Edge, Path> e : m.em.entrySet()) {
			ret.put(dst + "_" + e.getKey().name, compose(src, e.getValue()));
		}
		List<PSM> ret0 = new LinkedList<>();
		for (String k : ret.keySet()) {
			SQL v = ret.get(k);
			ret0.add(new InsertSQL(k, v));
		}
		return ret0;
	}
	
	private static Flower compose(String[] p) {
		Map<String, Pair<String, String>> select = new HashMap<>();
		Map<String, String> from = new HashMap<>();
		List<Pair<Pair<String, String>, Pair<String, String>>> where = new LinkedList<>();

//		from.put("t0", pre + "_" + p.source.string);

		from.put("t0", p[0]);

		for (int i = 1; i < p.length; i++) {
			from.put("t" + i,  p[i]);
			where.add(new Pair<>(new Pair<>("t" + (i - 1), "c1"), new Pair<>("t" + i, "c0")));
		}
		
		select.put("c0", new Pair<>("t0", "c0"));
		select.put("c1", new Pair<>("t" + (p.length - 1), "c1" ));
		
		return new Flower(select, from, where);
	}

	private static Flower compose(String pre, Path p) {
		Map<String, Pair<String, String>> select = new HashMap<>();
		Map<String, String> from = new HashMap<>();
		List<Pair<Pair<String, String>, Pair<String, String>>> where = new LinkedList<>();

		from.put("t0", pre + "_" + p.source.string);
		
		int i = 1;
		for (Edge e : p.path) {
			from.put("t" + i, pre + "_" + e.name);
			where.add(new Pair<>(new Pair<>("t" + (i - 1), "c1"), new Pair<>("t" + i, "c0")));
			i++;
		}
		
		select.put("c0", new Pair<>("t0", "c0"));
		select.put("c1", new Pair<>("t" + (i-1), "c1" ));
		
		return new Flower(select, from, where);
	}
	

	
	public static List<PSM> sigma(String pre, String inst, Mapping F) throws FQLException {
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
			
			SQL y = foldUnion(tn);
			ret.add(new InsertSQL(pre + "_" + d.string, y));
		}

		for (Edge e : D.edges) {
			Node d = e.source;
			//Node d0 = e.target;
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
		return ret;
	}
	
	private static SQL foldUnion(List<Flower> tn) {
		if (tn.size() == 0) {
			throw new RuntimeException();
		}
		if (tn.size() == 1) {
			return tn.get(0);
		}
		return new Union(tn);
	}

	private static Path findEquiv(Node c, Mapping f, Edge e)
			throws FQLException {
		Signature C = f.source;
		Signature D = f.target;
		FinCat<Node, Path> C0 = C.toCategory2().first;
		for (Arr<Node, Path> peqc : C0.arrows) {
			Path path = peqc.arr;
			//Path path = new Path(f.source, p);
			if (!path.source.equals(c)) {
				continue;
			}
			Path path_f = f.appy(path);
			Fn<Path, Arr<Node, Path>> F = D.toCategory2().second;
			if (F.of(path_f).equals(F.of(new Path(D, e)))) {
				return path;
			}
		}
		throw new FQLException("Could not find path mapping to " + e
				+ " under " + f);
	}

}
