package fql.parse;

import fql.decl.Decl;
import fql.decl.EvalDSPInstanceDecl;

/**
 * 
 * @author ryan
 *
 * Parser for instances given by delta, sigma, pi
 */
public class DSPInstanceDeclParser implements Parser<Decl> {

	@Override
	public Partial<Decl> parse(Tokens s0) throws BadSyntax, IllTyped {
		try {
			Tokens s = s0;
			
			Parser<?> pre = new KeywordParser("instance");
			Parser<?> e = new KeywordParser("=");
			Parser<?> ev = new KeywordParser("delta");
			Parser<String> p1 = new StringParser();
			Parser<String> p2 = new StringParser();
			Parser<String> p3 = new StringParser();
			String s1, s2, s3;
			
			Partial<?> x = pre.parse(s);
			s = x.tokens;
			Partial<String> y = p1.parse(s);
			s = y.tokens;
			s1 = y.value;
			
			String type;
			Parser<String> typ = new StringParser();
			Parser<String> typ0 = ParserUtils.seq(new KeywordParser(":"), typ);
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
			
			System.out.println("type is " + type);
			
			return new Partial<Decl>(s, new EvalDSPInstanceDecl(s1,"delta",s2,s3, type));
		} catch (Exception e) { }
		try {
			Tokens s = s0;

			Parser<?> pre = new KeywordParser("instance");
			Parser<?> e = new KeywordParser("=");
			Parser<?> ev = new KeywordParser("sigma");
			Parser<String> p1 = new StringParser();
			Parser<String> p2 = new StringParser();
			Parser<String> p3 = new StringParser();
			String s1, s2, s3;
			
			Partial<?> x = pre.parse(s);
			s = x.tokens;
			Partial<String> y = p1.parse(s);
			s = y.tokens;
			s1 = y.value;
			
			String type;
			Parser<String> typ = new StringParser();
			Parser<String> typ0 = ParserUtils.seq(new KeywordParser(":"), typ);
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
			
			
			return new Partial<Decl>(s, new EvalDSPInstanceDecl(s1,"sigma",s2,s3, type));
		} catch (Exception e) { }
		try {
			Tokens s = s0;

			Parser<?> pre = new KeywordParser("instance");
			Parser<?> e = new KeywordParser("=");
			Parser<?> ev = new KeywordParser("pi");
			Parser<String> p1 = new StringParser();
			Parser<String> p2 = new StringParser();
			Parser<String> p3 = new StringParser();
			String s1, s2, s3;
			
			Partial<?> x = pre.parse(s);
			s = x.tokens;
			Partial<String> y = p1.parse(s);
			s = y.tokens;
			s1 = y.value;
			
			String type;
			Parser<String> typ = new StringParser();
			Parser<String> typ0 = ParserUtils.seq(new KeywordParser(":"), typ);
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
			

			
			return new Partial<Decl>(s, new EvalDSPInstanceDecl(s1,"pi",s2,s3, type));
		} catch (Exception e) { }
		
		throw new BadSyntax("Could not parse delta/sigma/pi instance decl from " + s0);
	}

}
