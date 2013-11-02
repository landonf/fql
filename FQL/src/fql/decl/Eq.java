package fql.decl;


/**
 * 
 * @author ryan
 * 
 *         Syntax for equality of paths.
 */
public class Eq  {

	public Path lhs;
	public Path rhs;

	public Eq(Path lhs, Path rhs) {
		this.lhs = lhs;
		this.rhs = rhs;
	}

	public String toString() {
		return lhs + " = " + rhs;
	}

	

}
