package fql.sql;

import java.util.Map;
import java.util.Set;

public class InsertSQL extends PSM {

	String name;
	SQL sql;
	
	public InsertSQL(String name, SQL sql) {
		this.name = name;
		this.sql = sql;
	}
 
	@Override
	public String toPSM() {
		return "INSERT INTO " + name + " " + sql.toPSM() + ";";
	}

	@Override
	public void exec(Map<String, Set<Map<String, Object>>> state) {
		if (!state.containsKey(name)) {
			throw new RuntimeException(this.toString());
		}
		if (state.get(name).size() > 0) {
			throw new RuntimeException();
		}
		state.put(name, sql.eval(state));
	}

}
