package fql.sql;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fql.Pair;

/**
 * 
 * @author ryan
 *
 * Select-from-where statements
 */
public class Flower extends SQL {

	Map<String, Pair<String, String>> select;
	Map<String, String> from;
	List<Pair<Pair<String, String>, Pair<String, String>>> where;

	public Flower(Map<String, Pair<String, String>> select,
			Map<String, String> from,
			List<Pair<Pair<String, String>, Pair<String, String>>> where) {
		this.select = select;
		this.from = from;
		this.where = where;
		if (from.size() == 0) {
			throw new RuntimeException("Empty flower " + this);
		}
	}

	public Flower() {

	}

	@Override
	public String toPSM() {
		String as0 = "";
		int i = 0;
		for (String p : select.keySet()) {
			if (i++ > 0) {
				as0 += ", ";
			}
			as0 += select.get(p).first + "." + select.get(p).second + " AS "
					+ p;
		}

		String from0 = "";
		i = 0;
		for (String p : from.keySet()) {
			if (i++ > 0) {
				from0 += ", ";
			}
			from0 += from.get(p) + " AS " + p;
		}

		String where0 = "";
		if (where.size() > 0) {
			where0 = "WHERE ";
		}

		i = 0;
		for (Pair<Pair<String, String>, Pair<String, String>> p : where) {
			if (i++ > 0) {
				where0 += " AND ";
			}
			where0 += p.first.first + "." + p.first.second + " = "
					+ p.second.first + "." + p.second.second;
		}

		return "SELECT DISTINCT " + as0 + " FROM " + from0 + " " + where0;
	}

	@Override
	public Set<Map<String, Object>> eval(
			Map<String, Set<Map<String, Object>>> state) {

//		 System.out.println("********");
//		 System.out.println("Evaluating " + this);
//		 System.out.println("state " + state);
//		

		Set<Map<Pair<String, String>, Object>> tableau = evalFrom(state);

		// System.out.println("tableau " + tableau);

		// Set<Map<Pair<String, String>, Object>> filtered = evalWhere(tableau);

		// System.out.println("tableau " + filtered);

		Set<Map<String, Object>> projected = evalSelect(tableau);

		// System.out.println("tableau " + projected);

		return projected;
	}

	private Set<Map<Pair<String, String>, Object>> evalWhere2(
			Set<Map<Pair<String, String>, Object>> tableau) {
		Set<Map<Pair<String, String>, Object>> ret = new HashSet<>();
		a: for (Map<Pair<String, String>, Object> row : tableau) {
			for (Pair<Pair<String, String>, Pair<String, String>> eq : where) {
				// System.out.println("****" + row);
				// System.out.println("condition " + eq);
				// System.out.println(row.get(eq.first));
				// System.out.println(row.get(eq.second));
				if (row.get(eq.first) != null & row.get(eq.second) != null) {
					if (!row.get(eq.first).equals(row.get(eq.second))) {
						// System.out.println("failed");
						continue a;
					}
				}
			}
			// System.out.println("added " + row);
			ret.add(row);
		}
		return ret;
	}

	// private Set<Map<Pair<String, String>, Object>> evalWhere(
	// Set<Map<Pair<String, String>, Object>> tableau) {
	// Set<Map<Pair<String, String>, Object>> ret = new HashSet<>();
	// a: for (Map<Pair<String, String>, Object> row : tableau) {
	// for (Pair<Pair<String, String>, Pair<String, String>> eq : where) {
	// // System.out.println("****" + row);
	// // System.out.println("condition " + eq);
	// // System.out.println(row.get(eq.first));
	// // System.out.println(row.get(eq.second));
	// if (!row.get(eq.first).equals(row.get(eq.second))) {
	// // System.out.println("failed");
	// continue a;
	// }
	// }
	// // System.out.println("added " + row);
	// ret.add(row);
	// }
	// return ret;
	// }

	private Set<Map<String, Object>> evalSelect(
			Set<Map<Pair<String, String>, Object>> filtered) {
		Set<Map<String, Object>> ret = new HashSet<>();
		for (Map<Pair<String, String>, Object> row : filtered) {
			Map<String, Object> row0 = new HashMap<>();
			for (String k : select.keySet()) {
				row0.put(k, row.get(select.get(k)));
			}
			ret.add(row0);
		}
		return ret;
	}

	private Set<Map<Pair<String, String>, Object>> evalFrom(
			Map<String, Set<Map<String, Object>>> state) {
		Set<Map<Pair<String, String>, Object>> ret = null; // ok

		// int sz = 1;
		// for (String k : from.keySet()) {
		// System.out.println(state.get(from.get(k)).size());
		// sz *= state.get(from.get(k)).size();
		// }
		// if (sz > DEBUG.MAX_JOIN_SIZE) {
		// throw new RuntimeException("Maximum of " + sz +
		// " tuples exceeds limit on " + this);
		// }

		for (String k : from.keySet()) {
			if (ret == null) {
				if (state.get(from.get(k)) == null) {
					throw new RuntimeException("cannot find " + from.get(k)
							+ " in " + state);
				}
				ret = unit(k, state.get(from.get(k)), from.get(k));
				ret = evalWhere2(ret);
			} else {
				if (state.get(from.get(k)) == null) {
					throw new RuntimeException("cannot find " + from.get(k)
							+ " in " + state);
				}
				ret = cartProd(k, new HashSet<>(ret), state.get(from.get(k)),
						from.get(k));

				ret = evalWhere2(ret);
			}

		}
		return ret;
	}

	private Set<Map<Pair<String, String>, Object>> cartProd(String k,
			Set<Map<Pair<String, String>, Object>> x,
			Set<Map<String, Object>> y0, String v) {

		Set<Map<Pair<String, String>, Object>> y = unit(k, y0, v);

		Set<Map<Pair<String, String>, Object>> ret = new HashSet<>();
		// System.out.println("doing cartprod for " + x + " and " + y0 + " and "
		// + v + " k " + k);
		for (Map<Pair<String, String>, Object> row1 : x) {
			for (Map<Pair<String, String>, Object> row2 : y) {
				Map<Pair<String, String>, Object> row = new HashMap<>();
				for (Pair<String, String> s : row1.keySet()) {
					row.put(s, row1.get(s));
				}
				for (Pair<String, String> s : row2.keySet()) {
					row.put(s, row2.get(s));
				}
				ret.add(row);
			}
		}
		// System.out.println("result " + ret);
		return ret;
	}

	private Set<Map<Pair<String, String>, Object>> unit(String k,
			Set<Map<String, Object>> set, String v) {
		Set<Map<Pair<String, String>, Object>> ret = new HashSet<>();
		// System.out.println("doing unit for " + set + " and " + v + " and k "
		// + k);
		for (Map<String, Object> row : set) {
			Map<Pair<String, String>, Object> row0 = new HashMap<>();
			for (String attr : row.keySet()) {
				row0.put(new Pair<>(k, attr), row.get(attr));
			}
			ret.add(row0);
		}
		// System.out.println("result " + ret);
		return ret;
	}

}
