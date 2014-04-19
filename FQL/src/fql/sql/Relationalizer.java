package fql.sql;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import fql.FQLException;
import fql.Fn;
import fql.Pair;
import fql.Triple;
import fql.cat.Arr;
import fql.cat.FinCat;
import fql.decl.Attribute;
import fql.decl.Edge;
import fql.decl.FQLProgram;
import fql.decl.InstExp;
import fql.decl.InstExp.Const;
import fql.decl.Node;
import fql.decl.Path;
import fql.decl.SigExp;
import fql.decl.Signature;

/**
 * 
 * @author ryan
 * 
 *         Implements relationalization and observation using SQL. The terminal
 *         instance construction is here also.
 */
public class Relationalizer {

	public static Map<Pair<FQLProgram, SigExp.Const>, Triple<InstExp.Const, 
	Map<Node, Map<Object, LinkedHashMap<Pair<Arr<Node, Path>, Attribute<Node>>, Object>>>,
	Map<Node, Map<LinkedHashMap<Pair<Arr<Node, Path>, Attribute<Node>>, Object>, Object>>> > cache = new HashMap<>();
	
	public static Triple<InstExp.Const, 
	Map<Node, Map<Object, LinkedHashMap<Pair<Arr<Node, Path>, Attribute<Node>>, Object>>>,
	Map<Node, Map<LinkedHashMap<Pair<Arr<Node, Path>, Attribute<Node>>, Object>, Object>>> 
	terminal(FQLProgram prog, SigExp.Const sig0) {
		Triple<Const, Map<Node, Map<Object, LinkedHashMap<Pair<Arr<Node, Path>, Attribute<Node>>, Object>>>, Map<Node, Map<LinkedHashMap<Pair<Arr<Node, Path>, Attribute<Node>>, Object>, Object>>> jjj = cache.get(new Pair<>(prog, sig0));
		if (jjj != null) {
			return jjj; //so do not have to recompute when doing omega operations
		}
		
		try {
			Signature sig = sig0.toSig(prog);
			Pair<FinCat<Node, Path>, Fn<Path, Arr<Node, Path>>> start = sig
					.toCategory2();
			//FinCat<Node, Path> cat = start.first;
			Fn<Path, Arr<Node, Path>> map = start.second;
			Map<Node, List<Pair<Arr<Node, Path>, Attribute<Node>>>> obs = sig.obs();
			
			Map<Node, List<LinkedHashMap<Pair<Arr<Node, Path>, Attribute<Node>>, Object>>> m = sig.obsbar();
			
			List<Pair<String, List<Pair<Object, Object>>>> nodes = new LinkedList<>();
			List<Pair<String, List<Pair<Object, Object>>>> attrs = new LinkedList<>();
			List<Pair<String, List<Pair<Object, Object>>>> arrows = new LinkedList<>();
			
		//	Map<String, Set<Pair<Object, Object>>> data = new HashMap<>();			
			Map<Node, Map<Object, LinkedHashMap<Pair<Arr<Node, Path>, Attribute<Node>>, Object>>> m1 = new HashMap<>();
			Map<Node, Map<LinkedHashMap<Pair<Arr<Node, Path>, Attribute<Node>>, Object>, Object>> m2 = new HashMap<>();

			int i = 0;

			for (Node n : sig.nodes) {
				Map<Object, LinkedHashMap<Pair<Arr<Node, Path>, Attribute<Node>>, Object>> map1 = new HashMap<>();
				Map<LinkedHashMap<Pair<Arr<Node, Path>, Attribute<Node>>, Object>, Object> map2 = new HashMap<>();
				List<Pair<Object, Object>> set = new LinkedList<>();
				m1.put(n, map1);
				m2.put(n, map2);				
				for (LinkedHashMap<Pair<Arr<Node, Path>, Attribute<Node>>, Object> i2 : m.get(n)) {
					Object o = Integer.toString(++i);
					map1.put(o, i2);
					map2.put(i2, o);
					set.add(new Pair<Object, Object>(o, o));
				}
				nodes.add(new Pair<>(n.string, set));
			}
			for (Attribute<Node> a : sig.attrs) {
				List<Pair<Object, Object>> set = new LinkedList<>();
				for (Pair<Object, Object> k : PropPSM.lookup(nodes, a.source.string)) {
					LinkedHashMap<Pair<Arr<Node, Path>, Attribute<Node>>, Object> new_id = m1.get(a.source).get(k.first);
					set.add(new Pair<Object, Object>(k.first, new_id.get(new Pair<>(map.of(new Path(sig, a.source)), a))));
				}
				attrs.add(new Pair<>(a.name, set));
			}
			for (Edge a : sig.edges) {
				List<Pair<Object, Object>> set = new LinkedList<>();
				for (Pair<Object, Object> k : PropPSM.lookup(nodes, a.source.string)) {
					LinkedHashMap<Pair<Arr<Node, Path>, Attribute<Node>>, Object> new_id = m1.get(a.source).get(k.first);
					LinkedHashMap<Pair<Arr<Node, Path>, Attribute<Node>>, Object> new_id0 = PropPSM.truncate2(sig, new_id, new Arr<>(new Path(sig, a), a.source, a.target), obs.get(a.target));
				//	LinkedHashMap<Pair<Arr<Node, Path>, Attribute<Node>>, Object> new_id0 = PropPSM.truncate(sig, new_id, a, m.get(a.target));
				//	System.out.println("new " + new_id1);
				//	System.out.println("old " + new_id0);
					Object o = m2.get(a.target).get(new_id0);
					set.add(new Pair<>(k.first, o));
				}
				arrows.add(new Pair<>(a.name, set));
			}
			//			Instance ret0 = new Instance(sig, data);
			InstExp.Const retX = new InstExp.Const(nodes, attrs, arrows, sig.toConst());
			Triple<Const, Map<Node, Map<Object, LinkedHashMap<Pair<Arr<Node, Path>, Attribute<Node>>, Object>>>, Map<Node, Map<LinkedHashMap<Pair<Arr<Node, Path>, Attribute<Node>>, Object>, Object>>> ret = new Triple<>(retX, m1, m2);

			// System.out.println(ret);
			cache.put(new Pair<>(prog, sig0), ret);
			return ret;
		} catch (FQLException fe) {
			throw new RuntimeException(fe.getLocalizedMessage());
		}
	}
/*
	public static InstExp.Const terminal(FQLProgram prog, SigExp.Const sig0) {
		try {
			Signature sig = sig0.toSig(prog);
			Pair<FinCat<Node, Path>, Fn<Path, Arr<Node, Path>>> start = sig
					.toCategory2();
			FinCat<Node, Path> cat = start.first;
			Fn<Path, Arr<Node, Path>> map = start.second;

			Map<Node, Set<Set<Triple<Arr<Node, Path>, Attribute<Node>, Object>>>> inst = obsbar(sig);
			// System.out.println("inst is " + inst);
			Map<Set<Triple<Arr<Node, Path>, Attribute<Node>, Object>>, Integer> ids = new HashMap<>();
			int count = 0;

			List<Pair<String, List<Pair<Object, Object>>>> nodes0 = new LinkedList<>();
			List<Pair<String, List<Pair<Object, Object>>>> attrs0 = new LinkedList<>();
			List<Pair<String, List<Pair<Object, Object>>>> arrows0 = new LinkedList<>();
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
				Set<String> dst = ((Type.Enum) n.target).values;
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
				//System.out.println("edge " + n);
				List<Pair<Object, Object>> f0 = new LinkedList<>();
				List<Pair<Object, Object>> f1 = new LinkedList<>();

				arrows0.add(new Pair<>(n.name, f0));
				Set<Set<Triple<Arr<Node, Path>, Attribute<Node>, Object>>> dst = inst
						.get(n.target);
				Set<Set<Triple<Arr<Node, Path>, Attribute<Node>, Object>>> src = inst
						.get(n.source);
				// System.out.println(src.size());
				 System.out.println("Edge " + n);
				 System.out.println("source " + src);
				 System.out.println("target " + dst);
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
							// System.out.println("match on " + v);
							f0.add(new Pair<Object, Object>(ids.get(v), ids
									.get(k)));
						}
					}
				}
				for (Set<Triple<Arr<Node, Path>, Attribute<Node>, Object>> k : src) {
					 System.out.println("doing " + k);

					for (Set<Triple<Arr<Node, Path>, Attribute<Node>, Object>> v : dst) {
						 System.out.println("checking " + v);

						Set<Triple<Arr<Node, Path>, Attribute<Node>, Object>> newK = new HashSet<>();
						for (Triple<Arr<Node, Path>, Attribute<Node>, Object> xxx : v) {
							if (xxx.second == null) {
								continue;
							}
							Triple<Arr<Node, Path>, Attribute<Node>, Object> yyy = new Triple<>(
									cat.compose(map.of(new Path(sig, n)),
											xxx.first), xxx.second, xxx.third);
							newK.add(yyy);
						}
						 System.out.println("cand " + newK);
						if (k.containsAll(newK)) {
							 System.out.println("match ");
							f1.add(new Pair<Object, Object>(ids.get(k), ids
									.get(v)));
						}
					}

				}
				System.out.println("f0 " + f0);
				System.out.println("f1 " + f1);


				// System.out.println(f0.size() + "&&&");
				// fns.put(n, f);
			}

			InstExp.Const ret = new InstExp.Const(nodes0, attrs0, arrows0, sig0);
			// System.out.println(ret);
			return ret;
		} catch (FQLException fe) {
			throw new RuntimeException(fe.getLocalizedMessage());
		}
	}
*/
	/*
	 * public static InstExp.Const terminal(FQLProgram prog, SigExp.Const sig0)
	 * { try { Signature sig = sig0.toSig(prog); Pair<FinCat<Node, Path>,
	 * Fn<Path, Arr<Node, Path>>> start = sig .toCategory2(); FinCat<Node, Path>
	 * cat = start.first; Fn<Path, Arr<Node, Path>> map = start.second;
	 * 
	 * Map<Node, List<Map<Arr<Node, Path>, Map<Attribute<Node>, Object>>>> inst
	 * = fast_obs(sig); System.out.println("inst is " + inst); Map<Node,
	 * Set<Set<Triple<Arr<Node, Path>, Attribute<Node>, Object>>>> inst2 =
	 * obsbar(sig); System.out.println("old is " + inst2); Map<Map<Arr<Node,
	 * Path>, Map<Attribute<Node>, Object>>, Object> ids = new HashMap<>(); int
	 * count = 0;
	 * 
	 * List<Pair<String, List<Pair<Object, Object>>>> nodes0 = new
	 * LinkedList<>(); List<Pair<String, List<Pair<Object, Object>>>> attrs0 =
	 * new LinkedList<>(); List<Pair<String, List<Pair<Object, Object>>>>
	 * arrows0 = new LinkedList<>(); for (Node n : sig.nodes) {
	 * List<Pair<Object, Object>> xxx = new LinkedList<>(); List<Map<Arr<Node,
	 * Path>, Map<Attribute<Node>, Object>>> v = inst .get(n); for
	 * (Map<Arr<Node, Path>, Map<Attribute<Node>, Object>> k : v) { xxx.add(new
	 * Pair<Object, Object>(count, count)); ids.put(k, Integer.toString(count));
	 * count++; } nodes0.add(new Pair<>(n.string, xxx)); } for (Attribute<Node>
	 * n : sig.attrs) { List<Pair<Object, Object>> f0 = new LinkedList<>(); for
	 * (Map<Arr<Node, Path>, Map<Attribute<Node>, Object>> k :
	 * inst.get(n.source)) { for (Entry<Arr<Node, Path>, Map<Attribute<Node>,
	 * Object>> v : k.entrySet()) { if (cat.isId(v.getKey())) { f0.add(new
	 * Pair<>(ids.get(k), v.getValue().get(n))); } } } attrs0.add(new
	 * Pair<>(n.name, f0)); } for (Edge n : sig.edges) { List<Pair<Object,
	 * Object>> f0 = new LinkedList<>(); arrows0.add(new Pair<>(n.name, f0));
	 * List<Map<Arr<Node, Path>, Map<Attribute<Node>, Object>>> dst = inst
	 * .get(n.target); List<Map<Arr<Node, Path>, Map<Attribute<Node>, Object>>>
	 * src = inst .get(n.source); // System.out.println(src.size()); //
	 * System.out.println("Edge " + n); // System.out.println("source " + src);
	 * // System.out.println("target " + dst); // System.out.println("ids are "
	 * + ids);
	 * 
	 * for (Map<Arr<Node, Path>, Map<Attribute<Node>, Object>> k : src) {
	 * Map<Arr<Node, Path>, Map<Attribute<Node>, Object>> v =
	 * PropPSM.truncate(sig, k, n, dst); f0.add(new Pair<>(ids.get(k),
	 * ids.get(v))); }
	 * 
	 * /* for (Map<Arr<Node, Path>, Map<Attribute<Node>, Object>> k : dst) {
	 * Set<Triple<Arr<Node, Path>, Attribute<Node>, Object>> newK = new
	 * HashSet<>(); for (Entry<Arr<Node, Path>, Map<Attribute<Node>, Object>>
	 * xxx : k.entrySet()) { Triple<Arr<Node, Path>, Attribute<Node>, Object>
	 * yyy = new Triple<>( cat.compose(map.of(new Path(sig, n)), xxx.first),
	 * xxx.second, xxx.third); newK.add(yyy); } // System.out.println("newK " +
	 * newK); for (Set<Triple<Arr<Node, Path>, Attribute<Node>, Object>> v :
	 * src) { // System.out.println("v " + v); if (v.containsAll(newK)) { //
	 * System.out.println("match"); f0.add(new Pair<Object, Object>(ids.get(v),
	 * ids .get(k))); } } } // System.out.println(f0.size() + "&&&"); //
	 * fns.put(n, f); // fns.put(n,f); }
	 * 
	 * InstExp.Const ret = new InstExp.Const(nodes0, attrs0, arrows0, sig0); //
	 * System.out.println(ret); return ret; } catch (FQLException fe) { throw
	 * new RuntimeException(fe.getLocalizedMessage()); } }
	 */
	/*
	 * public static Map<Node, Set<Set<Triple<Arr<Node, Path>, Attribute<Node>,
	 * Object>>>> obsbar1( Signature sig) throws FQLException { Map<Node,
	 * Set<Set<Triple<Arr<Node, Path>, Attribute<Node>, Object>>>> ret = new
	 * HashMap<>();
	 * 
	 * Map<Node, List<Map<Arr<Node, Path>, Map<Attribute<Node>, Object>>>> xxx =
	 * fast_obs(sig); //System.out.println("starting obsbar1 " + xxx); for (Node
	 * n : sig.nodes) { Set<Set<Triple<Arr<Node, Path>, Attribute<Node>,
	 * Object>>> set = new HashSet<>(); List<Map<Arr<Node, Path>,
	 * Map<Attribute<Node>, Object>>> y = xxx.get(n);
	 * 
	 * for (Map<Arr<Node, Path>, Map<Attribute<Node>, Object>> k : y) {
	 * Set<Triple<Arr<Node, Path>, Attribute<Node>, Object>> s = new
	 * HashSet<>(); for (Entry<Arr<Node, Path>, Map<Attribute<Node>, Object>> m
	 * : k.entrySet()) { for (Entry<Attribute<Node>, Object> o :
	 * m.getValue().entrySet()) { s.add(new Triple<>(m.getKey(), o.getKey(),
	 * o.getValue())); } } set.add(s); } ret.put(n, set); }
	 * //System.out.println("end");
	 * 
	 * return ret; }
	 */

	

	

	// Set<Pair<Path, Set<Pair<Attribue
	/*
	 * public static Map<Node, List<Map<Arr<Node, Path>, Map<Attribute<Node>,
	 * Object>>>> fast_obs(Signature sig) throws FQLException { Map<Node,
	 * List<Map<Arr<Node, Path>, Map<Attribute<Node>, Object>>>> ret = new
	 * HashMap<>();
	 * 
	 * Pair<FinCat<Node, Path>, Fn<Path, Arr<Node, Path>>> xxx =
	 * sig.toCategory2(); FinCat<Node, Path> cat = xxx.first;
	 * 
	 * //Inst<Obj, Arrow, Y, X>.homomorphs(L)
	 * 
	 * for (Node c : sig.nodes) { // System.out.println("starting node " + c);
	 * Map<Arr<Node, Path>, List<Map<Attribute<Node>, Object>>> set = new
	 * HashMap<>(); for (Node d : sig.nodes) { for (Arr<Node, Path> p :
	 * cat.hom(c, d)) { Map<Attribute<Node>, List<Object>> m = new HashMap<>();
	 * for (Attribute<Node> a : sig.attrsFor(d)) { Type.Enum en = (Type.Enum)
	 * a.target; m.put(a, new LinkedList<Object>(en.values)); } Object yyy =
	 * Inst.homomorphs(m); List<Map<Attribute<Node>, Object>> mx =
	 * (List<Map<Attribute<Node>, Object>>) yyy;
	 * 
	 * //System.out.println("size of list " + mx.size()); set.put(p, mx); } }
	 * Object t0 = Inst.homomorphs(set); List<Map<Arr<Node, Path>,
	 * Map<Attribute<Node>, Object>>> t = (List<Map<Arr<Node, Path>,
	 * Map<Attribute<Node>, Object>>>) t0; ret.put(c, t);
	 * System.out.println("done"); }
	 * 
	 * return ret; }
	 */
	/*
	 */
	/*
	public static Map<Node, Set<Set<Triple<Arr<Node, Path>, Attribute<Node>, Object>>>> obsbar0(
			Signature sig) throws FQLException {
		Map<Node, Set<Set<Triple<Arr<Node, Path>, Attribute<Node>, Object>>>> m = obsbar(sig);
		for (Node k : m.keySet()) {
			Set<Set<Triple<Arr<Node, Path>, Attribute<Node>, Object>>> v = m
					.get(k);
			for (Set<Triple<Arr<Node, Path>, Attribute<Node>, Object>> s : v) {
				Iterator<Triple<Arr<Node, Path>, Attribute<Node>, Object>> it = s
						.iterator();
				while (it.hasNext()) {
					Triple<Arr<Node, Path>, Attribute<Node>, Object> t = it
							.next();
					if (t.second == null) {
						it.remove();
					}
				}
			}
		}
		return m;
	} */
/*
	private static Map<Node, Set<Set<Triple<Arr<Node, Path>, Attribute<Node>, Object>>>> obsbar(
			Signature sig) throws FQLException {
		Map<Node, Set<Set<Triple<Arr<Node, Path>, Attribute<Node>, Object>>>> inst = new HashMap<>();
		Pair<FinCat<Node, Path>, Fn<Path, Arr<Node, Path>>> start = sig
				.toCategory2();
		FinCat<Node, Path> cat = start.first;
		Fn<Path, Arr<Node, Path>> map = start.second;
		for (Node n : sig.nodes) {
			// System.out.println("doing node " + n);
			Set<Set<Triple<Arr<Node, Path>, Attribute<Node>, Object>>> set = new HashSet<>();
			Set<Triple<Arr<Node, Path>, Attribute<Node>, Object>> set0 = new HashSet<>();
			set.add(set0);
			set0.add(new Triple<>(map.of(new Path(sig, n)),
					(Attribute<Node>) null, (Object) new Unit())); // null
																	// ok
			// create 1 element set
			for (Arr<Node, Path> p : cat.arrows) {
				// System.out.println("doing arrow " + p);
				if (!p.src.equals(n)) {
					continue;
				}
				List<Attribute<Node>> k = sig.attrsFor(p.dst);
				for (Attribute<Node> a : k) {
					// System.out.println("Doing attr " + a);
					if (!(a.target instanceof Type.Enum)) {
						throw new FQLException(
								"Cannot take observations with string or int");
					}

					Set<Set<Triple<Arr<Node, Path>, Attribute<Node>, Object>>> newset = new HashSet<>();
					for (Object o : ((Type.Enum) a.target).values) {
						// System.out.println("doing value " + o);
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
		return inst;
	}
*/
	public static Pair<Map<Node, List<Pair<Path, Attribute<Node>>>>, List<PSM>> observations(
			Signature sig, String out, String in, boolean relationalize)
			throws FQLException {
		List<PSM> ret = new LinkedList<>();
		Map<Node, List<Pair<Path, Attribute<Node>>>> attrs = new HashMap<>();
		//attrs = new HashMap<Node, List<Pair<Path, Attribute<Node>>>>();
		Map<String, String> edge_types = new HashMap<>();
		edge_types.put("c0", PSM.VARCHAR());
		edge_types.put("c1", PSM.VARCHAR());

		// copy in to out, to start with
		ret.addAll(copy(sig, out, in));

		FinCat<Node, Path> cat = sig.toCategory2().first;
		for (Node n : sig.nodes) {
			attrs.put(n, new LinkedList<Pair<Path, Attribute<Node>>>());
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

				ret.add(new CreateTable(out + "_" + n.string + "tempNoAttrs"
						+ count, edge_types, false));
				InsertSQL f0 = new InsertSQL(out + "_" + n.string
						+ "tempNoAttrs" + count, f, "c0", "c1");
				ret.add(f0);

				LinkedHashMap<String, Pair<String, String>> select = new LinkedHashMap<>();
				Map<String, String> from = new HashMap<>();
				List<Pair<Pair<String, String>, Pair<String, String>>> where = new LinkedList<>();
				List<Attribute<Node>> l = sig.attrsFor(p.arr.target);
				from.put(n.string, out + "_" + n.string + "tempNoAttrs" + count);
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
					attrs.get(n).add(new Pair<>(p.arr, a));
					i++;
				}
				alltypes.add(types);
				Flower g = new Flower(select, from, where);
				// System.out.println("&&&Flower is " + g);

				ret.add(new CreateTable(out + "_" + n.string + "temp" + count,
						types, false));
				ret.add(new InsertSQL2(out + "_" + n.string + "temp" + count,
						g, new LinkedList<>(types.keySet())));
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
				from.put(n.string + "temp" + i, out + "_" + n.string + "temp"
						+ i);
				where.add(new Pair<>(new Pair<>(n.string + "temp" + i, "c0"),
						new Pair<>(in + "_" + n.string, "c0")));

			}
			if (select.size() == 0) {
				throw new RuntimeException("No observable for " + n.string);
			}// } else if (select.size() == 0 && suppress) {
				// continue;
				// }
			Flower j = new Flower(select, from, where);
			ret.add(new CreateTable(out + "_" + n.string + "_observables", ty,
					false));
			ret.add(new InsertSQL2(out + "_" + n.string + "_observables", j,
					new LinkedList<>(j.select.keySet())));

			if (relationalize) {
				ret.addAll(relationalize(select, from, where, sig, out, ty, n,
						u, edge_types));
			}

			for (int count0 = 0; count0 < count; count0++) {
				ret.add(new DropTable(out + "_" + n.string + "temp" + count0));
				ret.add(new DropTable(out + "_" + n.string + "tempNoAttrs"
						+ count0));
			}

		}

		return new Pair<>(attrs, ret);
	}

	// suppress = true in the jpanel, so we can leave the observables table
	// around
	// suppress = false for the compiler, since we just want the relationalized
	// result
	public static Pair<Map<Node, List<Pair<Path, Attribute<Node>>>>, List<PSM>> compile(
			Signature sig, String out, String in) throws FQLException {
		return observations(sig, out, in, true);
	}

	public static List<PSM> relationalize(
			LinkedHashMap<String, Pair<String, String>> select,
			Map<String, String> from,
			List<Pair<Pair<String, String>, Pair<String, String>>> where,
			Signature sig, String out, Map<String, String> ty, Node n, int u,
			Map<String, String> edge_types) throws FQLException {
		List<PSM> ret = new LinkedList<>();

		LinkedHashMap<String, Pair<String, String>> select0 = new LinkedHashMap<>(
				select);
		Map<String, String> ty0 = new HashMap<>(ty);
		ty0.remove("id");
		select0.remove("id");
		Flower j0 = new Flower(select0, from, where);
		ret.add(new CreateTable(out + "_" + n.string + "_observables_proj",
				ty0, false));
		if (ty0.size() > 0) {
			ret.add(new InsertSQL2(out + "_" + n.string + "_observables_proj",
					j0, new LinkedList<>(j0.select.keySet())));
		}
		ret.add(new CreateTable(out + "_" + n.string + "_observables_guid", ty,
				false));
		ret.add(new InsertKeygen(out + "_" + n.string + "_observables_guid",
				"id", out + "_" + n.string + "_observables_proj",
				new LinkedList<>(ty0.keySet())));

		// if (ty.keySet().size() == 0) {
		// throw new RuntimeException("Cannot compute observables for " + n +
		// ": no attributes");
		// }

		select = new LinkedHashMap<>();
		where = new LinkedList<>();
		from = new HashMap<>();
		from.put(n.string + "_observables", out + "_" + n.string
				+ "_observables");
		from.put(n.string + "_observables_guid", out + "_" + n.string
				+ "_observables_guid");
		for (int u0 = 0; u0 < u; u0++) {
			where.add(new Pair<>(
					new Pair<>(n.string + "_observables", "c" + u0),
					new Pair<>(n.string + "_observables_guid", "c" + u0)));
		}
		select.put("c0", new Pair<>(n.string + "_observables", "id"));
		select.put("c1", new Pair<>(n.string + "_observables_guid", "id"));

		Flower k = new Flower(select, from, where);
		ret.add(new CreateTable(out + "_" + n.string + "_squash", edge_types,
				false));
		ret.add(new InsertSQL2(out + "_" + n.string + "_squash", k,
				new LinkedList<>(k.select.keySet())));

		// TODO drops for observables
		// ret.add(new DropTable(n.string + "_observables"));
		// ret.add(new DropTable(n.string + "_observables_guid"));
		// ret.add(new DropTable(n.string + "_observables_proj"));

		ret.addAll(applySubst(sig, n, out));

		// ret.add(new DropTable(out + "_" +n.string + "_squash"));

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

		from.put(N + "_squash", out + "_" + N + "_squash");
		select.put("c0", new Pair<>(N + "_squash", "c1"));
		select.put("c1", new Pair<>(N + "_squash", "c1"));

		Flower f = new Flower(select, from, where);

		ret.add(new CreateTable(out + "_" + N + "_relationalize_temp", attrs,
				false));
		ret.add(new InsertSQL(out + "_" + N + "_relationalize_temp", f, "c0",
				"c1"));

		ret.add(new DropTable(out + "_" + N.string));
		ret.add(new CreateTable(out + "_" + N.string, attrs, false));
		ret.add(new InsertSQL(out + "_" + N.string, new CopyFlower(out + "_"
				+ N + "_relationalize_temp", "c0", "c1"), "c0", "c1"));

		ret.add(new DropTable(out + "_" + N + "_relationalize_temp"));

		for (Edge n : sig.edges) {
			if (!n.source.equals(N)) {
				continue;
			}

			where = new LinkedList<>();
			from = new HashMap<>();
			select = new LinkedHashMap<>();

			from.put(N + "_squash", out + "_" + N + "_squash");
			from.put(out + "_" + n.name, out + "_" + n.name);
			where.add(new Pair<>(new Pair<>(N + "_squash", "c0"), new Pair<>(
					out + "_" + n.name, "c0")));
			select.put("c0", new Pair<>(N + "_squash", "c1"));
			select.put("c1", new Pair<>(out + "_" + n.name, "c1"));

			f = new Flower(select, from, where);

			ret.add(new CreateTable(out + "_" + n.name + "_relationalize_temp",
					attrs, false));
			ret.add(new InsertSQL(out + "_" + n.name + "_relationalize_temp",
					f, "c0", "c1"));

			ret.add(new DropTable(out + "_" + n.name));
			ret.add(new CreateTable(out + "_" + n.name, attrs, false));
			ret.add(new InsertSQL(out + "_" + n.name, new CopyFlower(out + "_"
					+ n.name + "_relationalize_temp", "c0", "c1"), "c0", "c1"));

			ret.add(new DropTable(out + "_" + n.name + "_relationalize_temp"));
		}
		for (Attribute<Node> n : sig.attrs) {
			if (!n.source.equals(N)) {
				continue;
			}

			where = new LinkedList<>();
			from = new HashMap<>();
			select = new LinkedHashMap<>();

			from.put(N + "_squash", out + "_" + N + "_squash");
			from.put(out + "_" + n.name, out + "_" + n.name);
			where.add(new Pair<>(new Pair<>(N + "_squash", "c0"), new Pair<>(
					out + "_" + n.name, "c0")));
			select.put("c0", new Pair<>(N + "_squash", "c1"));
			select.put("c1", new Pair<>(out + "_" + n.name, "c1"));

			f = new Flower(select, from, where);

			ret.add(new CreateTable(out + "_" + "relationalize_temp", attrs,
					false));
			ret.add(new InsertSQL(out + "_" + "relationalize_temp", f, "c0",
					"c1"));

			ret.add(new DropTable(out + "_" + n.name));
			ret.add(new CreateTable(out + "_" + n.name, attrs, false));
			ret.add(new InsertSQL(out + "_" + n.name, new CopyFlower(out + "_"
					+ "relationalize_temp", "c0", "c1"), "c0", "c1"));

			ret.add(new DropTable(out + "_" + "relationalize_temp"));
		}
		for (Edge n : sig.edges) {
			if (!n.target.equals(N)) {
				continue;
			}

			where = new LinkedList<>();
			from = new HashMap<>();
			select = new LinkedHashMap<>();

			from.put(N + "_squash", out + "_" + N + "_squash");
			from.put(out + "_" + n.name, out + "_" + n.name);
			where.add(new Pair<>(new Pair<>(N + "_squash", "c0"), new Pair<>(
					out + "_" + n.name, "c1")));
			select.put("c0", new Pair<>(out + "_" + n.name, "c0"));
			select.put("c1", new Pair<>(N + "_squash", "c1"));

			f = new Flower(select, from, where);

			ret.add(new CreateTable(out + "_" + "relationalize_temp", attrs,
					false));
			ret.add(new InsertSQL(out + "_" + "relationalize_temp", f, "c0",
					"c1"));

			ret.add(new DropTable(out + "_" + n.name));
			ret.add(new CreateTable(out + "_" + n.name, attrs, false));
			ret.add(new InsertSQL(out + "_" + n.name, new CopyFlower(out + "_"
					+ "relationalize_temp", "c0", "c1"), "c0", "c1"));

			ret.add(new DropTable(out + "_" + "relationalize_temp"));
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
