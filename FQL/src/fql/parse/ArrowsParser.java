package fql.parse;

import java.util.List;

import fql.Triple;

/**
 * 
 * @author ryan
 * 
 *         Parses arrows
 */
public class ArrowsParser implements
		RyanParser<List<Triple<String, String, String>>> {

	@Override
	public Partial<List<Triple<String, String, String>>> parse(Tokens s)
			throws BadSyntax, IllTyped {
		RyanParser<Triple<String, String, String>> ap = new ArrowParser();

		RyanParser<?> sep = new KeywordParser(",");
		RyanParser<List<Triple<String, String, String>>> p = ParserUtils.manySep(
				ap, sep);

		return p.parse(s);
	}

}
