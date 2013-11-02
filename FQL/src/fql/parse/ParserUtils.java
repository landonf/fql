package fql.parse;

import java.util.ArrayList;
import java.util.List;

import fql.Pair;

/**
 * 
 * @author ryan
 * 
 *         Combine parsers to make new parsers
 */
public class ParserUtils {

	public static <T> RyanParser<List<T>> many(final RyanParser<T> p) {
		return new RyanParser<List<T>>() {
			public Partial<List<T>> parse(Tokens s) throws BadSyntax, IllTyped {
				List<T> ret = new ArrayList<T>();
				try {
					for (;;) {
						Partial<? extends T> x = p.parse(s);
						s = x.tokens;
						ret.add(x.value);
					}
				} catch (BadSyntax e) {
				}
				return new Partial<List<T>>(s, ret);
			}
		};
	}

	/**
	 * 
	 * @param <T>
	 * @param <V>
	 * @param p1
	 * @param p2
	 * @return a parser that matches and ignores p1, followed by p2
	 */
	public static <T> RyanParser<T> seq(final RyanParser<?> p1,
			final RyanParser<T> p2) {
		return new RyanParser<T>() {
			public Partial<T> parse(Tokens s) throws BadSyntax, IllTyped {
				Partial<?> x = p1.parse(s);
				Partial<T> y = p2.parse(x.tokens);
				return new Partial<T>(y.tokens, y.value);
			}
		};
	}

	public static <T> RyanParser<List<T>> manySep(final RyanParser<T> p,
			final RyanParser<?> sep) {
		return new RyanParser<List<T>>() {
			public Partial<List<T>> parse(Tokens s) throws BadSyntax, IllTyped {
				try {
					Partial<T> x = p.parse(s);

					RyanParser<T> pair_p = seq(sep, p);
					RyanParser<List<T>> pr = many(pair_p);
					Partial<List<T>> y = pr.parse(x.tokens);

					y.value.add(0, x.value);
					return new Partial<List<T>>(y.tokens, y.value);
				} catch (BadSyntax e) {
					return new Partial<List<T>>(s, new ArrayList<T>());
				}
			}
		};
	}

	/**
	 * @param <T>
	 * @param <U>
	 * @param l
	 * @param u
	 * @param r
	 * @return a parser that matches l u r and returns (l,r)
	 */
	public static <T, U> RyanParser<Pair<T, U>> inside(final RyanParser<T> l,
			final RyanParser<?> u, final RyanParser<U> r) {
		return new RyanParser<Pair<T, U>>() {
			public Partial<Pair<T, U>> parse(Tokens s) throws BadSyntax,
					IllTyped {
				Partial<? extends T> l0 = l.parse(s);
				Partial<?> u0 = u.parse(l0.tokens);
				Partial<? extends U> r0 = r.parse(u0.tokens);
				return new Partial<Pair<T, U>>(r0.tokens, new Pair<T, U>(
						l0.value, r0.value));
			}
		};
	}

	/**
	 * @param <T>
	 * @param l
	 * @param u
	 * @param r
	 * @return a parser that matches l u r and returns u
	 */
	public static <T> RyanParser<T> outside(final RyanParser<?> l,
			final RyanParser<T> u, final RyanParser<?> r) {
		return new RyanParser<T>() {
			public Partial<T> parse(Tokens s) throws BadSyntax, IllTyped {
				Partial<?> l0 = l.parse(s);
				Partial<? extends T> u0 = u.parse(l0.tokens);
				Partial<?> r0 = r.parse(u0.tokens);
				return new Partial<T>(r0.tokens, u0.value);
			}
		};
	}

	@SuppressWarnings("rawtypes")
	public static RyanParser<Object> or(final RyanParser p, final RyanParser q) {

		return new RyanParser<Object>() {

			@SuppressWarnings("unchecked")
			@Override
			public Partial<Object> parse(Tokens s) throws BadSyntax, IllTyped {
				try {
					return p.parse(s);
				} catch (Exception e) {
				}
					return q.parse(s);
			}

		};

	}

}
