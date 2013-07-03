package fql.parse;

import fql.decl.Decl;
import fql.decl.EvalInstanceDecl;

/**
 * 
 * @author ryan
 *
 * Parser for instances given by query evaluation.
 */
public class EvalInstanceDeclParser implements RyanParser<Decl> {

	@Override
	public Partial<Decl> parse(Tokens s) throws BadSyntax, IllTyped {
		RyanParser<?> pre = new KeywordParser("instance");
		
		
		RyanParser<?> e = new KeywordParser("=");
		RyanParser<?> ev = new KeywordParser("eval");
		RyanParser<String> p1 = new StringParser();
		RyanParser<String> p2 = new StringParser();
		RyanParser<String> p3 = new StringParser();
		String s1, s2, s3;
		
		String type;
		RyanParser<String> typ = new StringParser();
		RyanParser<String> typ0 = ParserUtils.seq(new KeywordParser(":"), typ);
		
		Partial<?> x = pre.parse(s);
		s = x.tokens;
		Partial<String> y = p1.parse(s);
		s = y.tokens;
		s1 = y.value;
		
		y = typ0.parse(s);
		s = y.tokens;
		type = y.value;
		
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
		
		//System.out.println("Type is " + type);
		
		
		return new Partial<Decl>(s, new EvalInstanceDecl(s1,s2,s3,type));
	}

}
