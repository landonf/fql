package fql.sql;

import java.util.Map;
import java.util.Set;

public class ProjectFlower extends Flower {

	String name;
	int[] cols;
	
	public ProjectFlower(String name, int[] cols) {
		super();
		this.name = name;
		this.cols = cols;
	}
	
	@Override
	public String toPSM() {
		String s = "";
		for (int i = 0; i < cols.length; i++) {
			if (i > 0) {
				s += ", ";
			}
			s += cols[i] + " AS " + "c" + i;
		}
		
		return "SELECT " + s + " FROM " + name;
	}
	
	//@Override
//	public Set<Map<String, Object>> eval(
//			Map<String, Set<Map<String, Object>>> state) {
//		Set<Map<String, Object>> v = state.get(name);
//		
//		Set<Map<String, Object>> ret = new HashMap<>();
//		
//		for (Map<String, Object> row : v) {
//			Map<String, Object> newrow = new HashMap<>();
//			for (int i = 0; i < cols.length; i++) {
//				
//			}
//		}
//		
//		return ret;
//	}
	
}
