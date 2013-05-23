package fql.sql;

import java.util.Map;
import java.util.Set;

public class DropTable extends PSM {

	String name;
	
	public DropTable(String name) {
		this.name = name;
	}
	

	@Override
	public String toPSM() {
		return "DROP TABLE " + name + ";";
	}

	@Override
	public void exec(Map<String, Set<Map<String, Object>>> state) {
		if (state.get(name) == null) {
			throw new RuntimeException("Table does not exist: " + name);
		}
		state.remove(name);
	}

}
