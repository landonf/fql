package fql;

import fql.parse.BadSyntax;
import fql.parse.IllTyped;
import fql.parse.KeywordParser;
import fql.parse.Parser;
import fql.parse.ParserUtils;
import fql.parse.Partial;
import fql.parse.StringParser;
import fql.parse.Tokens;

/**
 * 
 * @author ryan
 *
 * Parser for equality commands.
 */
public class EqCommandParser implements Parser<Command> {

	@Override
	public Partial<Command> parse(Tokens s) throws BadSyntax, IllTyped {
		Parser<?> sep = new KeywordParser("=");
		Parser<String> p1 = new StringParser();
		Parser<String> p2 = new StringParser();
		
		Partial<Pair<String, String>> x = ParserUtils.inside(p1, sep, p2).parse(s);
		
		String lhs = x.value.first;
		String rhs = x.value.second;
		return new Partial<Command>(x.tokens, new EqCommand(lhs + " = " + rhs, lhs, rhs));
	}

}
