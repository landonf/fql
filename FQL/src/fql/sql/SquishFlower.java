package fql.sql;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SquishFlower extends Flower {

	String name;
	
	public SquishFlower(String name) {
		super();
		this.name = name;
	}
	
	@Override
	public String toPSM() {
		return "SELECT guid AS c0, guid as c1 FROM " + name;
	}
	
	@Override
	public Set<Map<String, Object>> eval(
			Map<String, Set<Map<String, Object>>> state) {
		Set<Map<String, Object>> v = state.get(name);
		
		Set<Map<String, Object>> ret = new HashSet<>();
		
		for (Map<String, Object> row : v) {
			Map<String, Object> newrow = new HashMap<>();
			newrow.put("c0", row.get("guid"));
			newrow.put("c1",row.get("guid"));
			ret.add(newrow);
		}
		return ret;
	}
		
}
