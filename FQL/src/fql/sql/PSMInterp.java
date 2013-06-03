package fql.sql;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PSMInterp {
	
	public static int guid = 0;

	//wraps with binary tables
	public static String interp0(List<PSM> prog) {
		Map<String, Set<Map<String, Object>>> s = interp(prog);
		
		return s.toString();
	}
	
	public static Map<String, Set<Map<String, Object>>> interpX(List<PSM> prog, Map<String, Set<Map<String, Object>>> state ) {
		for (PSM cmd : prog) {
				System.out.println("Executing ");
				System.out.println(cmd);
				System.out.println(state);
				cmd.exec(state);
				System.out.println("RESULT " + state);
				System.out.println("DONE EXECUTING");
				checkFunctions(state);
			}
			
			return state;
	}
	
	public static Map<String, Set<Map<String, Object>>> interp(List<PSM> prog) {
		guid = 0;
		return interpX(prog, new HashMap<String, Set<Map<String, Object>>>());
	}

	private static void checkFunctions(
			Map<String, Set<Map<String, Object>>> state) {
		
		
//		for (String k : state.keySet()) {
//			Set<Map<String, Object>> v0 = state.get(k);
//			Set<String> keyset = null;
//			Map<Object, Object> f = new HashMap<>();
//			for (Map<String, Object> row : v0) {
//				if (keyset == null) {
//					keyset = row.keySet();
//				}
//				Set<String> keyset2 = row.keySet();
//				if (!keyset2.equals(keyset)) {
//					throw new RuntimeException();
//				}
//				if (keyset.contains("c0") && keyset.contains("c1") && keyset.size() == 2) {
//					if (f.containsKey(row.get("c0"))) {
//					//	if (!row.get("c0").equals(f))
//						throw new RuntimeException(v0.toString()); 
//					}
//					f.put(row.get("c0"), row.get("c1"));
//				}
//			}
//		}
	}

}
