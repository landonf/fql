package fql.parse;

/**
 * 
 * @author ryan
 *
 * Parser for quoted strings.
 */
public class QuotedParser implements Parser<String> {

	@Override
	public Partial<String> parse(Tokens s) throws BadSyntax, IllTyped {
		return ParserUtils.outside(new KeywordParser("\""), new StringParser(), new KeywordParser("\"")).parse(s);
	}

}
