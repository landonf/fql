package fql;

/**
 * 
 * @author ryan
 *
 * Syntax for equality of paths.
 */
public class Eq implements Jsonable {

	Path lhs, rhs;
	
	public Eq(Path lhs, Path rhs) {
		this.lhs = lhs;
		this.rhs = rhs;		
	}
	
	public String toString() {
		return lhs + " = " + rhs;
	}

	@Override
	public String tojson() {
		String s = "\"left\" : " + lhs.tojson();
		String r = "\"right\" : " + rhs.tojson();
		return s + " , " + r;
	}

}
