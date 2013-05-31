package fql.sql;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PSMInterp {
	
	//wraps with binary tables
	public static String interp0(List<PSM> prog) {
		Map<String, Set<Map<String, Object>>> s = interp(prog);
		
		return s.toString();
	}
	
	public static Map<String, Set<Map<String, Object>>> interp(List<PSM> prog) {
		Map<String, Set<Map<String, Object>>> state = new HashMap<>();
	//	System.out.println(prog);
		
		for (PSM cmd : prog) {
		//	System.out.println("Executing ");
		//	System.out.println(cmd);
		//	System.out.println(state);
			cmd.exec(state);
		//	System.out.println("RESULT " + state);
		//	System.out.println("DONE EXECUTING");
			checkFunctions(state);
		}
		
		return state;
	}

	private static void checkFunctions(
			Map<String, Set<Map<String, Object>>> state) {
		
		
		for (String k : state.keySet()) {
			Set<Map<String, Object>> v = state.get(k);
			Set<String> keyset = null;
			Map<Object, Object> f = new HashMap<>();
			for (Map<String, Object> row : v) {
				if (keyset == null) {
					keyset = row.keySet();
				}
				Set<String> keyset2 = row.keySet();
				if (!keyset2.equals(keyset)) {
					throw new RuntimeException();
				}
				if (keyset.contains("c0") && keyset.contains("c1") && keyset.size() == 2) {
					if (f.containsKey(row.get("c0"))) {
						throw new RuntimeException();
					}
					f.put(row.get("c0"), row.get("c1"));
				}
			}
		}
	}

}
