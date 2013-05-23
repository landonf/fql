package fql.sql;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PSMInterp {
	
	public static Map<String, Set<Map<String, Object>>> interp(List<PSM> prog) {
		Map<String, Set<Map<String, Object>>> state = new HashMap<>();
		
		for (PSM cmd : prog) {
			cmd.exec(state);
		}
		
		return state;
	}

}
