package fql.parse;

import java.util.List;

public class PathParser implements Parser<List<String>> {

	public Partial<List<String>> parse(Tokens s) throws BadSyntax, IllTyped {
		Partial<List<String>> ret = ParserUtils.manySep(new StringParser(), new KeywordParser(".")).parse(s);
		if (ret.value.size() == 0) {
			throw new BadSyntax("Error - empty path at " + s);
		}
		
		return ret;
	}

}
