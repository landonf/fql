package fql;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class DisjointUnion extends RA {

	public List<String> tags;
	public List<RA> e;
	
	public DisjointUnion(List<RA> e, List<String> tags) {
		assert (e != null);
		this.e = e;
		this.tags = tags;
	}
	
	static Set<String> exec1(Object a, Object b) {
		return null;
	}
	static Map<String, String> exec2(Object a, Object b) {
		return null;
	}
	
	@Override
	public String toString() {
		if (e.size() == 0) {
			return new SingletonRA().toString();
		}
//		if (e.length == 1) {
//			return e.[0].toString();
//		}
		String s = e.get(0).toString();
		for (int i = 1; i < e.size(); i++) {
			s += (" + " + e.get(i).toString());
		}
		return s;
	}

}
