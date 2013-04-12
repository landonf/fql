package fql.parse;

import fql.parse.BadSyntax;
import fql.parse.IllTyped;
import fql.parse.Parser;
import fql.parse.Partial;
import fql.parse.QuotedParser;
import fql.parse.StringParser;
import fql.parse.Tokens;

public class LongStringParser implements Parser<String> {

	@Override
	public Partial<String> parse(Tokens s) throws BadSyntax, IllTyped {
		try {
			return new QuotedParser().parse(s);
		} catch (Exception e) { }
		return new StringParser().parse(s);
	}

}
