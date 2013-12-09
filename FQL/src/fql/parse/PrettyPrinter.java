package fql.parse;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 
 * @author ryan
 * 
 *         Some helper pretty printing methods
 */
public class PrettyPrinter {

	public static String sep0(String delim, List<String> o) {
		if (o.size() == 0) {
			return "";
		}
		if (o.size() == 1) {
			return o.get(0);
		}
		String s = o.get(0).toString();
		for (int i = 1; i < o.size(); i++) {
			s += delim + o.get(i);
		}
		return s;
	}

	public static void printDB(Map<String, Set<Map<Object, Object>>> res) {
		for (String k : res.keySet()) {
			Set<Map<Object, Object>> v = res.get(k);
			System.out.println(k + " = ");
			for (Map<Object, Object> g : v) {
				System.out.print(g);
			}
			
			System.out.println("\n");
		}
	}

	

}
