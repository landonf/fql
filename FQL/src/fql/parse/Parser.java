package fql.parse;


public interface Parser<T> {

	public Partial<T> parse(Tokens s) throws BadSyntax, IllTyped;

}
