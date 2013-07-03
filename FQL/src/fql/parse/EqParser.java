package fql.parse;

import java.util.List;

import fql.Pair;

/**
 * 
 * @author ryan
 *
 * Parser for path equalities
 */
public class EqParser implements RyanParser<Pair<List<String>, List<String>>> {

	@Override
	public Partial<Pair<List<String>, List<String>>> parse(Tokens s)
			throws BadSyntax, IllTyped {
		RyanParser<List<String>> p = new PathParser();
		RyanParser<?> e = new KeywordParser("=");
		
		Partial<List<String>> x = p.parse(s);
		
		Partial<?> y = e.parse(x.tokens);
		
		Partial<List<String>> z = p.parse(y.tokens);
		
		return new Partial<Pair<List<String>, List<String>>>(z.tokens, new Pair<List<String>, List<String>>(x.value,z.value));
	}

}
