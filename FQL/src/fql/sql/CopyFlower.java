package fql.sql;

import java.util.Map;
import java.util.Set;

/**
 * 
 * @author ryan
 *
 * A Select-from-where that just copies a table.
 */
public class CopyFlower extends Flower {

	String name;
	
	public CopyFlower(String name) {
		super();
		this.name = name;
	}
	
	@Override
	public String toPSM() {
	return "SELECT * FROM " + name;
	}
	
	@Override
	public Set<Map<String, Object>> eval(
			Map<String, Set<Map<String, Object>>> state) {
		return state.get(name);
	}
}
