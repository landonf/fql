package fql.parse;

import java.util.ArrayList;
import java.util.List;

import fql.Pair;

/**
 * 
 * @author ryan
 *
 * Combine parsers to make new parsers
 */
public class ParserUtils {

	public static <T> Parser<List<T>> many(final Parser<T> p) {
		return new Parser<List<T>>() {
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
	public static <T> Parser<T> seq(final Parser<?> p1, final Parser<T> p2) {
		return new Parser<T>() {
			public Partial<T> parse(Tokens s) throws BadSyntax, IllTyped {
				Partial<?> x = p1.parse(s);
				Partial<T> y = p2.parse(x.tokens);
				return new Partial<T>(y.tokens, y.value);
			}
		};
	}

	public static <T> Parser<List<T>> manySep(final Parser<T> p,
			final Parser<?> sep) {
		return new Parser<List<T>>() {
			public Partial<List<T>> parse(Tokens s) throws BadSyntax, IllTyped {
				try {
					Partial<T> x = p.parse(s);

					Parser<T> pair_p = seq(sep, p);
					Parser<List<T>> pr = many(pair_p);
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
	public static <T, U> Parser<Pair<T, U>> inside(final Parser<T> l,
			final Parser<?> u, final Parser<U> r) {
		return new Parser<Pair<T, U>>() {
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
	public static <T> Parser<T> outside(final Parser<?> l, final Parser<T> u,
			final Parser<?> r) {
		return new Parser<T>() {
			public Partial<T> parse(Tokens s) throws BadSyntax, IllTyped {
				Partial<?> l0 = l.parse(s);
				Partial<? extends T> u0 = u.parse(l0.tokens);
				Partial<?> r0 = r.parse(u0.tokens);
				return new Partial<T>(r0.tokens, u0.value);
			}
		};
	}

}
