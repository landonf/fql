package fql.parse;

/**
 * 
 * @author ryan
 * 
 * @param <T>
 *            the type of thing to parse
 * 
 *            interface for parser combinators
 */
public interface Parser<T> {

	public Partial<T> parse(Tokens s) throws BadSyntax, IllTyped;

}
