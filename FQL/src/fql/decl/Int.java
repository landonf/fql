package fql.decl;

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
