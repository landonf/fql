package fql.parse;

import java.util.List;

import fql.Pair;

/**
 * 
 * @author ryan
 *
 * Parser for sets of equations
 */
public class EqsParser implements RyanParser<List<Pair<List<String>, List<String>>>> {

	@Override
	public Partial<List<Pair<List<String>, List<String>>>> parse(Tokens s)
			throws BadSyntax, IllTyped {
		RyanParser<Pair<List<String>, List<String>>> p = new EqParser();
		RyanParser<?> sep = new KeywordParser(",");
		RyanParser<List<Pair<List<String>, List<String>>>> p2 = ParserUtils.manySep(p, sep);
		
		return p2.parse(s);
	}

}
