package fql.sql;

/**
 * 
 * @author ryan
 * 
 *         Projection as RA syntax.
 */
public class Project extends RA {

	RA e;
	public int[] cols;

	public Project(RA e, int[] cols) {
		this.e = e;
		this.cols = cols;
	}

	@Override
	public String toString() {
		return "(PROJECT " + toNice(cols) + " " + e + ")";
	}

	private String toNice(int[] c) {
		if (c.length == 0) {
			return "";
		}
		String s = Integer.toString(c[0]);
		if (c.length == 1) {
			return s;
		}
		for (int i = 1; i < c.length; i++) {
			s += ("," + c[i]);
		}
		return s;
	}

}
