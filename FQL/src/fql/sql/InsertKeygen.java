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
 * PSM for keygeneration
 */
public class InsertKeygen extends PSM {

	String name;
	String col;
	String r;
	List<String> attrs;
	
	public InsertKeygen() {
		
	}
	
	public InsertKeygen(String name, String col, String r, List<String> attrs) throws FQLException {
		this.col = col;
		this.r = r;
		this.name = name;
		this.attrs = attrs;
		if (attrs.size() == 0) {
			throw new FQLException("Empty attrs in " + this);
		}
	}

	@Override
	public String toPSM() {
		String a = "";
		int i = 0;
		for (String attr : attrs) {
			if (i++ > 0) {
				a += ",";
			}
			a += attr;
		}
		
	
		return "INSERT INTO " + name + "(" + a + ", guid) SELECT " + a + ", @guid:=@guid+1 AS " + col + " FROM " + r + ";"; 
		
	}

	@Override
	public void exec(Map<String, Set<Map<String, Object>>> state) {
		//System.out.println("Exec " + this + " on " + state);
		if (!state.containsKey(name)) {
			throw new RuntimeException(name + "\n\n" + state);
		}
		if (state.get(r) == null) {
			throw new RuntimeException(r + "\n\n" + state);
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
			row0.put(col, ++PSMInterp.guid);
			ret.add(row0);
		}
		state.put(name, ret);
	//	System.out.println("result " + state);
	}

}
