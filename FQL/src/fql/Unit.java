package fql;

/**
 * 
 * @author ryan
 *
 * The unit type
 */
public class Unit {

	@Override
	public String toString() {
		return "";
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof Unit) {
			return true;
		}
		return false;
	}

}
