package fql.sql;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
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

	public Flower(LinkedHashMap<String, Pair<String, String>> select,
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
	public Set<Map<Object, Object>> eval(
			Map<String, Set<Map<Object, Object>>> state) {

//		 System.out.println("********");
//		 System.out.println("Evaluating " + this);
//		 System.out.println("state " + state);
//		

		Set<Map<Pair<Object, Object>, Object>> tableau = evalFrom(state);

		// System.out.println("tableau " + tableau);

		// Set<Map<Pair<String, String>, Object>> filtered = evalWhere(tableau);

		// System.out.println("tableau " + filtered);

		Set<Map<Object, Object>> projected = evalSelect(tableau);

		// System.out.println("tableau " + projected);

		return projected;
	}

	private Set<Map<Pair<Object, Object>, Object>> evalWhere2(
			Set<Map<Pair<Object, Object>, Object>> tableau) {
		Set<Map<Pair<Object, Object>, Object>> ret = new HashSet<>();
		a: for (Map<Pair<Object, Object>, Object> row : tableau) {
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

	private Set<Map<Object, Object>> evalSelect(
			Set<Map<Pair<Object, Object>, Object>> filtered) {
		Set<Map<Object, Object>> ret = new HashSet<>();
		for (Map<Pair<Object, Object>, Object> row : filtered) {
			Map<Object, Object> row0 = new HashMap<>();
			for (String k : select.keySet()) {
				row0.put(k, row.get(select.get(k)));
			}
			ret.add(row0);
		}
		return ret;
	}

	int timesInWhere(String x) {
		int count = 0;
		
		for (Pair<Pair<String, String>, Pair<String, String>> eq : where) {
			if (eq.first.first.equals(x)) {
				count++;
			}
			if (eq.second.first.equals(x)) {
				count++;
			}
		}

		return count;
	}
	
	private Set<Map<Pair<Object, Object>, Object>> evalFrom(
			final Map<String, Set<Map<Object, Object>>> state) {
		Set<Map<Pair<Object, Object>, Object>> ret = null; // ok

		// int sz = 1;
		// for (String k : from.keySet()) {
		// System.out.println(state.get(from.get(k)).size());
		// sz *= state.get(from.get(k)).size();
		// }
		// if (sz > DEBUG.MAX_JOIN_SIZE) {
		// throw new RuntimeException("Maximum of " + sz +
		// " tuples exceeds limit on " + this);
		// }
		
		List<String> ordered = new LinkedList<>(from.keySet());
		Comparator<String> c = new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				int xxx1 = timesInWhere(o1);
				int xxx2 = timesInWhere(o2);
				if (xxx1 == xxx2) {
					if (from.get(o1) == null || state.get(from.get(o1)) == null) {
						throw new RuntimeException("Missing: " + o1);
					}
					if (from.get(o2) == null || state.get(from.get(o2)) == null) {
						throw new RuntimeException("Missing: " + o2);
					}
				return Integer.compare(state.get(from.get(o1)).size(), state.get(from.get(o2)).size()); 
				} else if (xxx1 > xxx2) {
					return -1;
				} else {
					return 1;
				}
			}
		};
		Collections.sort(ordered, c); 
		
		
	//	System.out.println("***");
		for (String k : ordered) {
		//	System.out.println(state.get(from.get(k)).size());
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
				//System.gc();
			//	ret = evalWhere2(ret);
			}

		}
		return ret;
	}

	private Set<Map<Pair<Object, Object>, Object>> cartProd(String k,
			Set<Map<Pair<Object, Object>, Object>> x,
			Set<Map<Object, Object>> y0, String v) {

		Set<Map<Pair<Object, Object>, Object>> y = unit(k, y0, v);

		Set<Map<Pair<Object, Object>, Object>> ret = new HashSet<>();
		// System.out.println("doing cartprod for " + x + " and " + y0 + " and "
		// + v + " k " + k);
		for (Map<Pair<Object, Object>, Object> row1 : x) {
			a: for (Map<Pair<Object, Object>, Object> row2 : y) {
				Map<Pair<Object, Object>, Object> row = new HashMap<>();
				for (Pair<Object, Object> s : row1.keySet()) {
					row.put(s, row1.get(s));
				}
				for (Pair<Object, Object> s : row2.keySet()) {
					row.put(s, row2.get(s));
				}
				for (Pair<Pair<String, String>, Pair<String, String>> eq : where) {
				if (row.get(eq.first) != null & row.get(eq.second) != null) {
					if (!row.get(eq.first).equals(row.get(eq.second))) {
						// System.out.println("failed");
						continue a;
					}
				}
				}
				ret.add(row);
			}
		}
		// System.out.println("result " + ret);
		return ret;
	}

	private Set<Map<Pair<Object, Object>, Object>> unit(String k,
			Set<Map<Object, Object>> set, String v) {
		Set<Map<Pair<Object, Object>, Object>> ret = new HashSet<>();
		// System.out.println("doing unit for " + set + " and " + v + " and k "
		// + k);
		for (Map<Object, Object> row : set) {
			Map<Pair<Object, Object>, Object> row0 = new HashMap<>();
			for (Object attr : row.keySet()) {
				row0.put(new Pair<>((Object)k, attr), row.get(attr));
			}
			ret.add(row0);
		}
		// System.out.println("result " + ret);
		return ret;
	}

}
