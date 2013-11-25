package fql.sql;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fql.FQLException;

/**
 * 
 * @author ryan
 * 
 *         PSM for keygeneration
 */
public class InsertKeygen extends PSM {

	String name;
	String col;
	String r;
	List<String> attrs;

	public InsertKeygen() {

	}

	public InsertKeygen(String name, String col, String r, List<String> attrs)
			throws FQLException {
		this.col = col;
		this.r = r;
		this.name = name;
		this.attrs = attrs;
	}

	@Override
	public String toPSM() {
		if (attrs.size() == 0) {
			return "INSERT INTO " + name + "(" + col + ") VALUES (@guid := @guid+1)";
		}
		String a = "";
		int i = 0;
		for (String attr : attrs) {
			if (i++ > 0) {
				a += ",";
			}
			a += attr;
		}

		return "INSERT INTO " + name + "(" + a + ", " + col + " SELECT " + a
				+ ", @guid:=@guid+1 AS " + col + " FROM " + r;

	}

	@Override
	public void exec(PSMInterp interp,
			Map<String, Set<Map<Object, Object>>> state) {
		// System.out.println("Exec " + this + " on " + state);
		if (!state.containsKey(name)) {
			throw new RuntimeException(name + "\n\n" + state);
		}
		if (state.get(r) == null) {
			throw new RuntimeException(r + "\n\n" + state);
		}
		if (state.get(name).size() > 0) {
			throw new RuntimeException(this.toString());
		}
		
		Set<Map<Object, Object>> ret = new HashSet<>();
		if (attrs.size() == 0) {
			Map<Object, Object> m = new HashMap<>();
			ret.add(m);
			m.put(col, interp.guid++);
			state.put(name, ret);
		} else {
			for (Map<Object, Object> row : state.get(r)) {
				Map<Object, Object> row0 = new HashMap<>();
				for (Object s : row.keySet()) {
					row0.put(s, row.get(s));
				}
				row0.put(col, interp.guid++);
				ret.add(row0);
			}
			state.put(name, ret);
		}
		// System.out.println("result " + state);
	}

}
