package fql.parse;

import fql.Pair;

/**
 * 
 * @author ryan
 * 
 *         Parser for tuples.
 */
public class TupleParser implements RyanParser<Pair<String, String>> {

	@Override
	public Partial<Pair<String, String>> parse(Tokens s) throws BadSyntax,
			IllTyped {
		try {
			RyanParser<?> l = new KeywordParser("(");
			RyanParser<?> r = new KeywordParser(")");

			RyanParser<?> c = new KeywordParser(",");

			RyanParser<String> d = new LongStringParser();
			RyanParser<String> e = new LongStringParser();

			RyanParser<Pair<String, String>> p = ParserUtils.inside(d, c, e);
			RyanParser<Pair<String, String>> q = ParserUtils.outside(l, p, r);

			Partial<Pair<String, String>> xxx = q.parse(s);
			if (xxx.value.first.equals("}")) {
				throw new BadSyntax(s, "Cannot parse tuple from }");
			}
			return xxx;
		} catch (BadSyntax e) {
		} catch (IllTyped e) {
		}
		// try {
		// StringParser p = new StringParser();
		// Partial<String> x = p.parse(s);
		// Pair<String, String> y = new Pair<String, String>(x.value,x.value);
		// return new Partial<Pair<String, String>>(x.tokens, y);
		// } catch (BadSyntax e) {
		// } catch (IllTyped e) {
		// }
		throw new BadSyntax(s, "Could not parse tuple from " + s);
	}

}
