package fql.sql;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections15.map.LinkedMap;

import fql.FQLException;
import fql.Fn;
import fql.Pair;
import fql.Triple;
import fql.Unit;
import fql.cat.Arr;
import fql.cat.FinCat;
import fql.decl.Attribute;
import fql.decl.Edge;
import fql.decl.FQLProgram;
import fql.decl.InstExp;
import fql.decl.Node;
import fql.decl.Path;
import fql.decl.SigExp;
import fql.decl.Signature;
import fql.decl.Type;

/**
 * 
 * @author ryan
 *
 * Implements relationalization and observation using SQL.
 * The terminal instance construction is here also.
 */
public class Relationalizer {

	public static InstExp.Const terminal(FQLProgram prog, SigExp.Const sig0) {
		try {
			Signature sig = sig0.toSig(prog);
			Pair<FinCat<Node, Path>, Fn<Path, Arr<Node, Path>>> start = sig
					.toCategory2();
			FinCat<Node, Path> cat = start.first;
			Fn<Path, Arr<Node, Path>> map = start.second;
			Map<Node, Set<Set<Triple<Arr<Node, Path>, Attribute<Node>, Object>>>> inst = new HashMap<>();
			Map<Set<Triple<Arr<Node, Path>, Attribute<Node>, Object>>, Integer> ids = new HashMap<>();
			int count = 0;
			List<Pair<String, List<Pair<Object, Object>>>> nodes0 = new LinkedList<>();
			List<Pair<String, List<Pair<Object, Object>>>> attrs0 = new LinkedList<>();
			List<Pair<String, List<Pair<Object, Object>>>> arrows0 = new LinkedList<>();
			for (Node n : sig.nodes) {
				Set<Set<Triple<Arr<Node, Path>, Attribute<Node>, Object>>> set = new HashSet<>();
				Set<Triple<Arr<Node, Path>, Attribute<Node>, Object>> set0 = new HashSet<>();
				set.add(set0);
				set0.add(new Triple<>(map.of(new Path(sig, n)),
						(Attribute<Node>) null, (Object) new Unit())); // null
																		// ok
				// create 1 element set
				for (Arr<Node, Path> p : cat.arrows) {
					if (!p.src.equals(n)) {
						continue;
					}
					List<Attribute<Node>> k = sig.attrsFor(p.dst);
					for (Attribute<Node> a : k) {
						if (!(a.target instanceof Type.Enum)) {
							throw new FQLException(
									"Cannot compute terminal instances with string or int");
						}

						Set<Set<Triple<Arr<Node, Path>, Attribute<Node>, Object>>> newset = new HashSet<>();
						for (Object o : ((Type.Enum) a.target).values) {
							Triple<Arr<Node, Path>, Attribute<Node>, Object> kk = new Triple<>(
									p, a, o);

							for (Set<Triple<Arr<Node, Path>, Attribute<Node>, Object>> s : set) {
								Set<Triple<Arr<Node, Path>, Attribute<Node>, Object>> newS = new HashSet<>(
										s);
								newS.add(kk);
								newset.add(newS);
							}
						}
						set = newset;
					}
				}
				inst.put(n, set);
				// System.out.println("set for " + n + ": " + set);
			}

			for (Node n : sig.nodes) {
				List<Pair<Object, Object>> xxx = new LinkedList<>();
				Set<Set<Triple<Arr<Node, Path>, Attribute<Node>, Object>>> v = inst
						.get(n);
				for (Set<Triple<Arr<Node, Path>, Attribute<Node>, Object>> k : v) {
					xxx.add(new Pair<Object, Object>(count, count));
					ids.put(k, count++);
				}
				nodes0.add(new Pair<>(n.string, xxx));
			}
			for (Attribute<Node> n : sig.attrs) {
				List<Pair<Object, Object>> f0 = new LinkedList<>();
				Set<Set<Triple<Arr<Node, Path>, Attribute<Node>, Object>>> src = inst
						.get(n.source);
				Set<Object> dst = ((Type.Enum) n.target).values;
				// System.out.println("*** doing " + n);
				for (Object k : dst) {
					Triple<Arr<Node, Path>, Attribute<Node>, Object> v = new Triple<>(
							map.of(new Path(sig, n.source)), n, k);
					// System.out.println("dst " + v);
					for (Set<Triple<Arr<Node, Path>, Attribute<Node>, Object>> k0 : src) {
						// System.out.println("src " + k0);
						if (k0.contains(v)) {
							// System.out.println("Match");
							f0.add(new Pair<Object, Object>(ids.get(k0), k));
						}
					}
				}
				// atts.put(n, f);
				attrs0.add(new Pair<>(n.name, f0));
			}
			for (Edge n : sig.edges) {
				List<Pair<Object, Object>> f0 = new LinkedList<>();
				arrows0.add(new Pair<>(n.name, f0));
				Set<Set<Triple<Arr<Node, Path>, Attribute<Node>, Object>>> dst = inst
						.get(n.target);
				Set<Set<Triple<Arr<Node, Path>, Attribute<Node>, Object>>> src = inst
						.get(n.source);
				// System.out.println(src.size());
				// System.out.println("Edge " + n);
				// System.out.println("source " + src);
				// System.out.println("target " + dst);
				// System.out.println("ids are " + ids);

				for (Set<Triple<Arr<Node, Path>, Attribute<Node>, Object>> k : dst) {
					Set<Triple<Arr<Node, Path>, Attribute<Node>, Object>> newK = new HashSet<>(); 
					for (Triple<Arr<Node, Path>, Attribute<Node>, Object> xxx : k) {
						if (xxx.second == null) {
							continue;
						}
						Triple<Arr<Node, Path>, Attribute<Node>, Object> yyy = new Triple<>(
								cat.compose(map.of(new Path(sig, n)), xxx.first),
								xxx.second, xxx.third);
						newK.add(yyy);
					}
					// System.out.println("newK " + newK);
					for (Set<Triple<Arr<Node, Path>, Attribute<Node>, Object>> v : src) {
						// System.out.println("v " + v);
						if (v.containsAll(newK)) {
							// System.out.println("match");
							f0.add(new Pair<Object, Object>(ids.get(v), ids
									.get(k)));
						}
					}
				}
				// System.out.println(f0.size() + "&&&");
				// fns.put(n, f);
			}

			InstExp.Const ret = new InstExp.Const(nodes0, attrs0, arrows0, sig0);
			//System.out.println(ret);
			return ret;
		} catch (FQLException fe) {
			throw new RuntimeException(fe.getLocalizedMessage());
		}
	}

	public static Pair<Map<Node, List<String>>, List<PSM>> observations(Signature sig, String out, String in, boolean relationalize) throws FQLException {
		List<PSM> ret = new LinkedList<>();
		Map<Node, List<String>> attrs = new HashMap<>();
		attrs = new HashMap<Node, List<String>>();
		Map<String, String> edge_types = new HashMap<>();
		edge_types.put("c0", PSM.VARCHAR());
		edge_types.put("c1", PSM.VARCHAR());

		// copy in to out, to start with
		ret.addAll(copy(sig, out, in));

		FinCat<Node, Path> cat = sig.toCategory2().first;
		for (Node n : sig.nodes) {
			attrs.put(n, new LinkedList<String>());
			int count = 0;
			List<Map<String, String>> alltypes = new LinkedList<>();
			for (Arr<Node, Path> p : cat.arrows) {
				// if (cat.isId(p)) {
				// continue;
				// }
				// need identity path to get attributes from n
				if (!p.src.equals(n)) {
					continue;
				}
				Flower f = PSMGen.compose(in, p.arr);

				ret.add(new CreateTable(out + "_" + n.string + "tempNoAttrs" + count,
						edge_types, false));
				InsertSQL f0 = new InsertSQL(out + "_" +n.string + "tempNoAttrs" + count,
						f, "c0", "c1");
				ret.add(f0);

				LinkedHashMap<String, Pair<String, String>> select = new LinkedHashMap<>();
				Map<String, String> from = new HashMap<>();
				List<Pair<Pair<String, String>, Pair<String, String>>> where = new LinkedList<>();
				List<Attribute<Node>> l = sig.attrsFor(p.arr.target);
				from.put(n.string, out + "_" +n.string + "tempNoAttrs" + count);
				select.put("c0", new Pair<>(n.string, "c0"));
				// select.put("c1", new Pair<>(n.string + "tempNoAttrs", "c1"));
				int i = 1;
				LinkedHashMap<String, String> types = new LinkedHashMap<>();
				types.put("c0", PSM.VARCHAR());
				// types.put("c1", PSM.VARCHAR());
				for (Attribute<Node> a : l) {
					from.put(a.name, in + "_" + a.name);
					Pair<String, String> lhs = new Pair<>(n.string, "c1");
					Pair<String, String> rhs = new Pair<>(a.name, "c0");
					where.add(new Pair<>(lhs, rhs));
					select.put("c" + i, new Pair<>(a.name, "c1"));
					types.put("c" + i, a.target.psm());
					attrs.get(n).add(p.toString() + "." + a.name);
					i++;
				}
				alltypes.add(types);
				Flower g = new Flower(select, from, where);
				// System.out.println("&&&Flower is " + g);

				ret.add(new CreateTable(out + "_" +n.string + "temp" + count, types, false));
				ret.add(new InsertSQL2(out + "_" +n.string + "temp" + count, g, new LinkedList<>(types.keySet())));
				count++;
				
			}

			LinkedHashMap<String, Pair<String, String>> select = new LinkedHashMap<>();
			List<Pair<Pair<String, String>, Pair<String, String>>> where = new LinkedList<>();
			Map<String, String> from = new HashMap<>();
			from.put(in + "_" + n.string, in + "_" + n.string);
			Map<String, String> ty = new HashMap<>();
			int u = 0;
			ty.put("id", PSM.VARCHAR());
			select.put("id", new Pair<>(in + "_" + n.string, "c0"));
			for (int i = 0; i < count; i++) {
				Map<String, String> types = alltypes.get(i);
				for (int v = 0; v < types.size() - 1; v++) {
					ty.put("c" + u, types.get("c" + (v + 1))); // was c1
					select.put("c" + u, new Pair<>(n.string + "temp" + i, "c"
							+ (v + 1)));
					u++;
				}
				from.put(n.string + "temp" + i, out + "_" +n.string + "temp" + i);
				where.add(new Pair<>(new Pair<>(n.string + "temp" + i, "c0"),
						new Pair<>(in + "_" + n.string, "c0")));

			}
			if (select.size() == 0) {
				throw new RuntimeException("No observable for " + n.string);
			}// } else if (select.size() == 0 && suppress) {
			// continue;
			// }
			Flower j = new Flower(select, from, where);
			ret.add(new CreateTable(out + "_" +n.string + "_observables", ty, false));
			ret.add(new InsertSQL2(out + "_" +n.string + "_observables", j, new LinkedList<>(j.select.keySet())));
			
			if (relationalize) {
				ret.addAll(relationalize(select, from, where, sig, out, ty, n, u, edge_types));
			}

			for (int count0 = 0; count0 < count; count0++) {
				ret.add(new DropTable(out + "_" +n.string + "temp" + count0));
				ret.add(new DropTable(out + "_" +n.string + "tempNoAttrs" + count0));
			}
			
		}
		
		return new Pair<>(attrs, ret);
	}

	// suppress = true in the jpanel, so we can leave the observables table
	// around
	// suppress = false for the compiler, since we just want the relationalized
	// result
	public static Pair<Map<Node, List<String>>, List<PSM>> compile(Signature sig, String out, String in) throws FQLException {
		return observations(sig, out, in, true);
	}

	public static List<PSM> relationalize(LinkedHashMap<String, Pair<String, String>> select, Map<String, String> from, List<Pair<Pair<String, String>, Pair<String, String>>> where, Signature sig, String out, Map<String, String> ty, Node n, int u, Map<String, String> edge_types) throws FQLException {
		List<PSM> ret = new LinkedList<>();

		LinkedHashMap<String, Pair<String, String>> select0 = new LinkedHashMap<>(select);
		Map<String, String> ty0 = new HashMap<>(ty);
		ty0.remove("id");
		select0.remove("id");
		Flower j0 = new Flower(select0, from, where);
		ret.add(new CreateTable(out + "_" +n.string + "_observables_proj", ty0, false));
		if (ty0.size() > 0) {
		ret.add(new InsertSQL2(out + "_" +n.string + "_observables_proj", j0, new LinkedList<>(j0.select.keySet())));
		}
		ret.add(new CreateTable(out + "_" +n.string + "_observables_guid", ty, false));
		ret.add(new InsertKeygen(out + "_" +n.string + "_observables_guid", "id", out + "_" + n.string
				+ "_observables_proj", new LinkedList<>(ty0.keySet())));

//		if (ty.keySet().size() == 0) {
	//		throw new RuntimeException("Cannot compute observables for " + n + ": no attributes");
//		}
		
		select = new LinkedHashMap<>();
		where = new LinkedList<>();
		from = new HashMap<>();
		from.put(n.string + "_observables", out + "_" +n.string + "_observables");
		from.put(n.string + "_observables_guid", out + "_" +n.string + "_observables_guid");
		for (int u0 = 0; u0 < u; u0++) {
			where.add(new Pair<>(
					new Pair<>(n.string + "_observables", "c" + u0),
					new Pair<>(n.string + "_observables_guid", "c" + u0)));
		}
		select.put("c0", new Pair<>(n.string + "_observables", "id"));
		select.put("c1", new Pair<>(n.string + "_observables_guid", "id"));

		Flower k = new Flower(select, from, where);
		ret.add(new CreateTable(out + "_" +n.string + "_squash", edge_types, false));
		ret.add(new InsertSQL2(out + "_" +n.string + "_squash", k, new LinkedList<>(k.select.keySet())));
		
		//TODO drops for observables
	//	ret.add(new DropTable(n.string + "_observables"));
	//	ret.add(new DropTable(n.string + "_observables_guid"));
	//	ret.add(new DropTable(n.string + "_observables_proj"));
		
		ret.addAll(applySubst(sig, n, out));

//		ret.add(new DropTable(out + "_" +n.string + "_squash"));

		return ret;
	}

	private static Collection<PSM> applySubst(Signature sig, Node N, String out) {
		List<PSM> ret = new LinkedList<>();

		Map<String, String> attrs = new HashMap<>();
		attrs.put("c0", PSM.VARCHAR());
		attrs.put("c1", PSM.VARCHAR());

		List<Pair<Pair<String, String>, Pair<String, String>>> where = new LinkedList<>();
		Map<String, String> from = new HashMap<>();
		LinkedHashMap<String, Pair<String, String>> select = new LinkedHashMap<>();

		from.put(N + "_squash", out + "_" +N + "_squash");
		select.put("c0", new Pair<>(N + "_squash", "c1"));
		select.put("c1", new Pair<>(N + "_squash", "c1"));

		Flower f = new Flower(select, from, where);

		ret.add(new CreateTable(out + "_" + N + "_relationalize_temp", attrs, false));
		ret.add(new InsertSQL(out + "_" + N +"_relationalize_temp", f, "c0", "c1"));

		ret.add(new DropTable(out + "_" + N.string));
		ret.add(new CreateTable(out + "_" + N.string, attrs, false));
		ret.add(new InsertSQL(out + "_" + N.string, new CopyFlower(
				out + "_" + N + "_relationalize_temp", "c0", "c1"), "c0", "c1"));

		ret.add(new DropTable(out + "_" + N + "_relationalize_temp"));

		for (Edge n : sig.edges) {
			if (!n.source.equals(N)) {
				continue;
			}

			where = new LinkedList<>();
			from = new HashMap<>();
			select = new LinkedHashMap<>();

			from.put(N + "_squash", out + "_" +N + "_squash");
			from.put(out + "_" + n.name, out + "_" + n.name);
			where.add(new Pair<>(new Pair<>(N + "_squash", "c0"), new Pair<>(out
					+ "_" + n.name, "c0")));
			select.put("c0", new Pair<>(N + "_squash", "c1"));
			select.put("c1", new Pair<>(out + "_" + n.name, "c1"));

			f = new Flower(select, from, where);

			ret.add(new CreateTable(out + "_" + n.name + "_relationalize_temp", attrs, false));
			ret.add(new InsertSQL(out + "_" + n.name + "_relationalize_temp", f, "c0", "c1"));

			ret.add(new DropTable(out + "_" + n.name));
			ret.add(new CreateTable(out + "_" + n.name, attrs, false));
			ret.add(new InsertSQL(out + "_" + n.name, new CopyFlower(
					out + "_" + n.name + "_relationalize_temp", "c0", "c1"), "c0", "c1"));

			ret.add(new DropTable(out + "_" + n.name + "_relationalize_temp"));
		}
		for (Attribute<Node> n : sig.attrs) {
			if (!n.source.equals(N)) {
				continue;
			}

			where = new LinkedList<>();
			from = new HashMap<>();
			select = new LinkedHashMap<>();

			from.put(N + "_squash", out + "_" +N + "_squash");
			from.put(out + "_" + n.name, out + "_" + n.name);
			where.add(new Pair<>(new Pair<>(N + "_squash", "c0"), new Pair<>(out
					+ "_" + n.name, "c0")));
			select.put("c0", new Pair<>(N + "_squash", "c1"));
			select.put("c1", new Pair<>(out + "_" + n.name, "c1"));

			f = new Flower(select, from, where);

			ret.add(new CreateTable(out + "_" +"relationalize_temp", attrs, false));
			ret.add(new InsertSQL(out + "_" +"relationalize_temp", f, "c0", "c1"));

			ret.add(new DropTable(out + "_" + n.name));
			ret.add(new CreateTable(out + "_" + n.name, attrs, false));
			ret.add(new InsertSQL(out + "_" + n.name, new CopyFlower(
					out + "_" +"relationalize_temp", "c0", "c1"), "c0", "c1"));

			ret.add(new DropTable(out + "_" +"relationalize_temp"));
		}
		for (Edge n : sig.edges) {
			if (!n.target.equals(N)) {
				continue;
			}

			where = new LinkedList<>();
			from = new HashMap<>();
			select = new LinkedHashMap<>();

			from.put(N + "_squash", out + "_" +N + "_squash");
			from.put(out + "_" + n.name, out + "_" + n.name);
			where.add(new Pair<>(new Pair<>(N + "_squash", "c0"), new Pair<>(out
					+ "_" + n.name, "c1")));
			select.put("c0", new Pair<>(out + "_" + n.name, "c0"));
			select.put("c1", new Pair<>(N + "_squash", "c1"));

			
			f = new Flower(select, from, where);

			ret.add(new CreateTable(out + "_" +"relationalize_temp", attrs, false));
			ret.add(new InsertSQL(out + "_" +"relationalize_temp", f, "c0", "c1"));

			ret.add(new DropTable(out + "_" + n.name));
			ret.add(new CreateTable(out + "_" + n.name, attrs, false));
			ret.add(new InsertSQL(out + "_" + n.name, new CopyFlower(
					out + "_" +"relationalize_temp", "c0", "c1"), "c0", "c1"));

			ret.add(new DropTable(out + "_" +"relationalize_temp"));
		}

		return ret;
	}

	private static List<PSM> copy(Signature sig, String out, String in) {
		List<PSM> ret = new LinkedList<>();

		for (Node n : sig.nodes) {
			Map<String, String> attrs = new HashMap<>();
			attrs.put("c0", PSM.VARCHAR());
			attrs.put("c1", PSM.VARCHAR());
			// ret.add(new CreateTable(out + "_" + n.string, attrs, false));
			ret.add(new InsertSQL(out + "_" + n.string, new CopyFlower(in + "_"
					+ n.string, "c0", "c1"), "c0", "c1"));
		}
		for (Attribute<Node> n : sig.attrs) {
			Map<String, String> attrs = new HashMap<>();
			attrs.put("c0", PSM.VARCHAR());
			attrs.put("c1", n.target.psm());
			// ret.add(new CreateTable(out + "_" + n.name, attrs, false));
			ret.add(new InsertSQL(out + "_" + n.name, new CopyFlower(in + "_"
					+ n.name, "c0", "c1"), "c0", "c1"));
		}
		for (Edge n : sig.edges) {
			Map<String, String> attrs = new HashMap<>();
			attrs.put("c0", PSM.VARCHAR());
			attrs.put("c1", PSM.VARCHAR());
			// ret.add(new CreateTable(out + "_" + n.name, attrs, false));
			ret.add(new InsertSQL(out + "_" + n.name, new CopyFlower(in + "_"
					+ n.name, "c0", "c1"), "c0", "c1"));
		}

		return ret;
	}

}
