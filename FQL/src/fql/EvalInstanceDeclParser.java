package fql;

import fql.parse.BadSyntax;
import fql.parse.IllTyped;
import fql.parse.KeywordParser;
import fql.parse.Parser;
import fql.parse.Partial;
import fql.parse.StringParser;
import fql.parse.Tokens;

/**
 * 
 * @author ryan
 *
 * Parser for instances given by query evaluation.
 */
public class EvalInstanceDeclParser implements Parser<Decl> {

	@Override
	public Partial<Decl> parse(Tokens s) throws BadSyntax, IllTyped {
		Parser<?> pre = new KeywordParser("instance");
		Parser<?> e = new KeywordParser("=");
		Parser<?> ev = new KeywordParser("eval");
		Parser<String> p1 = new StringParser();
		Parser<String> p2 = new StringParser();
		Parser<String> p3 = new StringParser();
		String s1, s2, s3;
		
		Partial<?> x = pre.parse(s);
		s = x.tokens;
		Partial<String> y = p1.parse(s);
		s = y.tokens;
		s1 = y.value;
		x = e.parse(s);
		s = x.tokens;
		x = ev.parse(s);
		s = x.tokens;
		y = p2.parse(s);
		s = y.tokens;
		s2 = y.value;
		y = p3.parse(s);
		s = y.tokens;
		s3 = y.value;
		
		
		return new Partial<Decl>(s, new EvalInstanceDecl(s1,s2,s3));
	}

}
