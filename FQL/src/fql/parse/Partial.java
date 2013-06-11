package fql.parse;

/**
 * 
 * @author ryan
 * 
 * @param <T>
 *            the type of thing to parse
 * 
 *            Represents the intermediate parsing states return from parsing
 *            combinators.
 */
public class Partial<T> {
	public Tokens tokens;

	public T value;

	public Partial(Tokens t, T v) {
		tokens = t;
		value = v;
	}

	@Override
	public String toString() {
		return "Partial [tokens=" + tokens + "\n value=" + value + "]";
	}

}
