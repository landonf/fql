package fql.decl;

/**
 * 
 * @author ryan
 * 
 *         The type of integers.
 */
public class Int extends Type {

	@Override
	public boolean equals(Object o) {
		if (o instanceof Int) {
			return true;
		}
		return false;
	}

	@Override
	public int hashCode() {

		return 0;
	}

	@Override
	public String toString() {
		return "int";
	}
}
