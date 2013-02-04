package fql.parse;

public class StringParser implements Parser<String> {

	@Override
	public Partial<String> parse(Tokens s) throws BadSyntax, IllTyped {
		return new Partial<String>(s.pop(), s.head());
	}

}
