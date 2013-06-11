package fql.sql;

import java.util.List;

import fql.sql.RA;
import fql.sql.SingletonRA;

/**
 * 
 * @author ryan
 * 
 *         Syntax for disjoint union as an RA operation. Takes 2n tags for n
 *         tables.
 */
public class DisjointUnion extends RA {

	public List<String> tags;
	public List<RA> e;

	public DisjointUnion(List<RA> e, List<String> tags) {
		assert (e != null);
		this.e = e;
		this.tags = tags;
	}

	@Override
	public String toString() {
		if (e.size() == 0) {
			return new SingletonRA().toString();
		}
		// if (e.length == 1) {
		// return e.[0].toString();
		// }
		String s = e.get(0).toString();
		for (int i = 1; i < e.size(); i++) {
			s += (" + " + e.get(i).toString());
		}
		return s;
	}

}
