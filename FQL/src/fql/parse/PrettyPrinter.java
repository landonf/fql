package fql.parse;

import java.util.List;

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

	

}
