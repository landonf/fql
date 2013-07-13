package fql;

/**
 * 
 * @author ryan
 *
 * Pairs
 * @param <T1> the type of left
 * @param <T2> the type of right
 */
public class Pair<T1, T2> {

	public T1 first;
	public T2 second;

	public Pair(T1 value, T2 value2) {
		first = value;
		second = value2;
	}
	@Override
	public String toString() {
		return "(" + first + "," + second + ")";
		
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((first == null) ? 0 : first.hashCode());
		result = prime * result + ((second == null) ? 0 : second.hashCode());
		return result;
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Pair other = (Pair) obj;
		if (first == null) {
			if (other.first != null)
				return false;
		} else if (!first.equals(other.first))
			return false;
		if (second == null) {
			if (other.second != null)
				return false;
		} else if (!second.equals(other.second))
			return false;
		return true;
	}
	public Pair<T2, T1> reverse() {
		return new Pair<>(second, first);
	}
	
}
