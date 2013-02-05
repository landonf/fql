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
 * Parser for tuples.
 */
public class TupleParser implements Parser<Pair<String, String>> {

	@Override
	public Partial<Pair<String, String>> parse(Tokens s) throws BadSyntax,
			IllTyped {
		try {
			Parser<?> l = new KeywordParser("(");
			Parser<?> r = new KeywordParser(")");
		
			Parser<?> c = new KeywordParser(",");
		
			Parser<String> d = new StringParser();
			Parser<String> e = new StringParser();
		
			Parser<Pair<String, String>> p = ParserUtils.inside(d, c, e);
			Parser<Pair<String, String>> q = ParserUtils.outside(l, p, r);
		
			return q.parse(s);
		} catch (BadSyntax e) {
		} catch (IllTyped e) {
		} try {
			StringParser p = new StringParser();
			Partial<String> x = p.parse(s);
			Pair<String, String> y = new Pair<String, String>(x.value,x.value);
			return new Partial<Pair<String, String>>(x.tokens, y);
		} catch (BadSyntax e) {
		} catch (IllTyped e) {
		}
		throw new BadSyntax("Could not parse tuple from " + s);
	}

}
