package fql;

/**
 * 
 * @author ryan
 *
 * Triples
 *
 * @param <S1>
 * @param <S2>
 * @param <S3>
 */
public class Triple<S1, S2, S3> implements Comparable<Triple<S1, S2, S3>> {

	public Triple(S1 a, S2 b, S3 c) {
		first = a; second = b; third = c;
	}
	@Override
	public int hashCode() {
		return 0;
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
		Triple other = (Triple) obj;
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
		if (third == null) {
			if (other.third != null)
				return false;
		} else if (!third.equals(other.third))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "(" + first + ", " + second + ", "
				+ third + ")";
	}
	public S1 first;
	public S2 second;
	public S3 third;
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public int compareTo(Triple<S1, S2, S3> o) {
		Comparable x = (Comparable) first;
		Comparable y = (Comparable) o.first;
		int c = x.compareTo(y);
		if (c == 0) {
			Comparable a = (Comparable) second;
			Comparable b = (Comparable) o.second;
			c = a.compareTo(b);
			if (c == 0) {
				Comparable i = (Comparable) third;
				Comparable j = (Comparable) o.third;
				return i.compareTo(j);
			} else {
				return c;
			}
		} else {
			return c;
		}
	}
}
