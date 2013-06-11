package fql.decl;

/**
 * 
 * @author ryan
 *
 * FQL string type
 */
public class Varchar extends Type {

	@Override
	public boolean equals(Object o) {
		if (o instanceof Varchar) {
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
		return "string";
	}
}
