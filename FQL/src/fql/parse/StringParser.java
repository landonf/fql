package fql.parse;

public class StringParser implements Parser<String> {

	@Override
	public Partial<String> parse(Tokens s) throws BadSyntax, IllTyped {
		String k = s.peek(0);
		if (!k.equals(";")) {
			return new Partial<String>(s.pop(), s.head());
		}
		throw new BadSyntax(s, "Cannot parse string from ; in " + s);
	}

}
