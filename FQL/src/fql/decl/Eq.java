package fql.decl;

import fql.parse.Jsonable;

/**
 * 
 * @author ryan
 *
 * Syntax for equality of paths.
 */
public class Eq implements Jsonable {

	public Path lhs;
	public Path rhs;
	
	public Eq(Path lhs, Path rhs) {
		this.lhs = lhs;
		this.rhs = rhs;		
	}
	
	public String toString() {
		return lhs + " = " + rhs;
	}

	@Override
	public String tojson() {
		String s = "{\"left\" : " + lhs.tojson();
		String r = "\"right\" : " + rhs.tojson() + "}";
		return s + " , " + r;
	}

}
