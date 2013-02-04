package fql;

import fql.parse.BadSyntax;
import fql.parse.IllTyped;
import fql.parse.KeywordParser;
import fql.parse.Parser;
import fql.parse.Partial;
import fql.parse.StringParser;
import fql.parse.Tokens;

public class DirectInstanceDeclParser implements Parser<Decl> {

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
			
			
			return new Partial<Decl>(s, new DirectInstanceDecl(s1,"delta",s2,s3));
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
			
			
			return new Partial<Decl>(s, new DirectInstanceDecl(s1,"sigma",s2,s3));
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
			
			
			return new Partial<Decl>(s, new DirectInstanceDecl(s1,"pi",s2,s3));
		} catch (Exception e) { }
		
		throw new BadSyntax("Could not parse direct instance decl from " + s0);
	}

}
