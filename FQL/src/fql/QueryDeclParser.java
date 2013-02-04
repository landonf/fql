package fql;

import fql.parse.BadSyntax;
import fql.parse.IllTyped;
import fql.parse.KeywordParser;
import fql.parse.Parser;
import fql.parse.Partial;
import fql.parse.StringParser;
import fql.parse.Tokens;

public class QueryDeclParser implements Parser<Decl> {

	@Override
	public Partial<Decl> parse(Tokens s) throws BadSyntax, IllTyped {
		Tokens olds = new Tokens(s.toString());
		
		try {
			Parser<?> p1 = new KeywordParser("query");
			Parser<?> p2 = new KeywordParser("=");
			Parser<?> p3 = new KeywordParser("id");
			Parser<String> p4 = new StringParser();
			Parser<String> p5 = new StringParser();

			String ret1;
			Partial<?> p;
			p = p1.parse(s);
			s = p.tokens;
			Partial<String> q = p4.parse(s);
			s = q.tokens;
			ret1 = q.value;
			p = p2.parse(s);
			s = p.tokens;
			p = p3.parse(s);
			s = p.tokens;
			q = p5.parse(s);

			return new Partial<Decl>(q.tokens, new QueryDecl(ret1, q.value));

		} catch (BadSyntax e) {
		} catch (IllTyped e) {
		}
		try {
			s = olds;
			Parser<?> p1 = new KeywordParser("query");
			Parser<?> p2 = new KeywordParser("=");
			Parser<?> p3 = new KeywordParser("o");
			Parser<String> p4 = new StringParser();
			Parser<String> p5 = new StringParser();
			Parser<String> p6 = new StringParser();

			String ret1, ret2, ret3;
			Partial<?> p;
			p = p1.parse(s);
			s = p.tokens;
			Partial<String> q = p4.parse(s);
			s = q.tokens;
			ret1 = q.value;
			p = p2.parse(s);
			s = p.tokens;

			q = p5.parse(s);
			s = q.tokens;
			ret2 = q.value;

			p = p3.parse(s);
			s = p.tokens;
			q = p5.parse(s);

			q = p6.parse(s);
			s = q.tokens;
			ret3 = q.value;

			return new Partial<Decl>(s, new QueryDecl(ret1, ret2, ret3));
		} catch (BadSyntax e) {
		} catch (IllTyped e) {
		}
		try {
			s = olds;
			Parser<?> p1 = new KeywordParser("query");
			Parser<?> p2 = new KeywordParser("=");
			Parser<?> p3 = new KeywordParser("delta");
			Parser<?> p7 = new KeywordParser("pi");
			Parser<?> p8 = new KeywordParser("sigma");
			
			Parser<String> p4 = new StringParser();
			Parser<String> p5 = new StringParser();
			Parser<String> p6 = new StringParser();
			Parser<String> p9 = new StringParser();
	
			String ret1, ret2, ret3, ret4;
			Partial<?> p;
			p = p1.parse(s);
			s = p.tokens;
			
			Partial<String> q = p4.parse(s);
			s = q.tokens;
			ret1 = q.value;
			
			p = p2.parse(s);
			s = p.tokens; // finished with query x =
			
			p = p3.parse(s);
			s = p.tokens;
			
			q = p5.parse(s);
			s = q.tokens;
			ret2 = q.value; // project
			
			p = p7.parse(s);
			s = p.tokens;
			
			q = p6.parse(s);
			s = q.tokens;
			ret3 = q.value; // join
			
			p = p8.parse(s);
			s = p.tokens;
			
			q = p9.parse(s);
			s = q.tokens;
			ret4 = q.value; // union

			return new Partial<Decl>(s, new QueryDecl(ret1, ret2, ret3, ret4));
		} catch (BadSyntax e) {
		} catch (IllTyped e) {
		}

		throw new BadSyntax("Could not parse a query from " + s);
	}
}
