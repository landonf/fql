package fql.parse;

import java.util.List;

/**
 * 
 * @author ryan
 * 
 *         Parser for paths.
 */
public class PathParser implements RyanParser<List<String>> {

	public Partial<List<String>> parse(Tokens s) throws BadSyntax {
		Partial<List<String>> ret = ParserUtils.manySep(new StringParser(),
				new KeywordParser(".")).parse(s);
		if (ret.value.size() == 0) {
			throw new BadSyntax(s, "Error - empty path at " + s);
		}

		return ret;
	}

}
