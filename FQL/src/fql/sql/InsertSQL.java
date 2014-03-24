package fql.sql;

import java.util.Map;
import java.util.Set;

/**
 * 
 * @author ryan
 * 
 *         Insert the result of a SQL query into a table.
 */
public class InsertSQL extends PSM {

	String name, c0, c1;
	SQL sql;

	public InsertSQL(String name, SQL sql, String c0, String c1) {
		this.name = name;
		this.sql = sql;
		this.c0 = c0;
		this.c1 = c1;
	}

	@Override
	public String toPSM() {
		return "INSERT INTO " + name + "(" + c0 + "," + c1 + ") " + sql.toPSM();
	}

	@Override
	public void exec(PSMInterp interp, Map<String, Set<Map<Object, Object>>> state) {
		if (!state.containsKey(name)) {
			throw new RuntimeException("does not contain key " + name + "\n\n" + state + " sql was " + this);
		}
		if (state.get(name).size() > 0) {
			throw new RuntimeException(name + ": already " + state.get(name) + " in " + this);
		}
		state.put(name, sql.eval(state));
	}
	
	@Override
	public String isSql() {
		return null;
	}


}
