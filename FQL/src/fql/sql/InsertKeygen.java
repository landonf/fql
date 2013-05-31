package fql.sql;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class InsertKeygen extends PSM {

	public static int guid = 0;
	
	String name;
	String col;
	String r;
	
	public InsertKeygen(String name, String col, String r) {
		this.col = col;
		this.r = r;
		this.name = name;
	}

	@Override
	public String toPSM() {
		return "INSERT INTO " + name + "(c0,c1,guid) SELECT *, @guid:=@guid+1 AS " + col + " FROM " + r + ";"; 
	}

	@Override
	public void exec(Map<String, Set<Map<String, Object>>> state) {
		//System.out.println("Exec " + this + " on " + state);
		if (!state.containsKey(name)) {
			throw new RuntimeException(this.toString());
		}
		if (state.get(name).size() > 0) {
			throw new RuntimeException(this.toString());
		}
		Set<Map<String, Object>> ret = new HashSet<>();
		for (Map<String, Object> row : state.get(r)) {
			Map<String, Object> row0 = new HashMap<>();
			for (String s : row.keySet()) {
				row0.put(s, row.get(s));
			}
			row0.put(col, guid++);
			ret.add(row0);
		}
		state.put(name, ret);
	//	System.out.println("result " + state);
	}

}
