package fql.parse;

import java.util.List;

import fql.Triple;

public class ArrowsParser implements
		Parser<List<Triple<String, String, String>>> {

	@Override
	public Partial<List<Triple<String, String, String>>> parse(Tokens s)
			throws BadSyntax, IllTyped {
		Parser<Triple<String,String,String>> ap = new ArrowParser();
		
		Parser<?> sep = new KeywordParser(",");
		Parser<List<Triple<String, String, String>>> p = ParserUtils.manySep(ap, sep);
		
		return p.parse(s);
	}

}
