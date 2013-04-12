package fql.parse;

import java.util.List;

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
	
	public static String sep(String delim, List<? extends Jsonable> o) {
		if (o.size() == 0) {
			return "";
		}
		if (o.size() == 1) {
			return o.get(0).tojson();
		}
		String s = o.get(0).tojson();
		for (int i = 1; i < o.size(); i++) {
			s += delim + o.get(i).tojson();
		}
		return s;
	}
	
	public static String sep(String delim, String l, String r, List<? extends Jsonable> o) {
		return l + sep(delim, o) + r;
	}

	public static String sepX(String delim, String l, String r, List<? extends Jsonable> o, String a, String b) {
		return l + sepX(delim, o, a, b) + r;
	}

	public static String sepX(String delim, List<? extends Jsonable> o, String a, String b) {
		if (o.size() == 0) {
			return "";
		}
		if (o.size() == 1) {
			return a + o.get(0).tojson() + b ;
		}
		String s = o.get(0).tojson();
		for (int i = 1; i < o.size(); i++) {
			s += delim + a + o.get(i).tojson() + b;
		}
		return s;
	}

}
