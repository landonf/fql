package fql.parse;

public class Partial<T> {
	public Tokens tokens;

	public T value;

	public Partial(Tokens t, T v) {
		tokens = t;
		value = v;
	}

}
