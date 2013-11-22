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

	String name;
	SQL sql;

	public InsertSQL(String name, SQL sql) {
		this.name = name;
		this.sql = sql;
	}

	@Override
	public String toPSM() {
		return "INSERT INTO " + name + " " + sql.toPSM();
	}

	@Override
	public void exec(PSMInterp interp, Map<String, Set<Map<Object, Object>>> state) {
		if (!state.containsKey(name)) {
			throw new RuntimeException("does not contain key " + name + "\n\n" + state);
		}
		if (state.get(name).size() > 0) {
			throw new RuntimeException(name + ": already " + state.get(name) + " in " + this);
		}
		state.put(name, sql.eval(state));
	}

}
