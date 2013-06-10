package fql.parse;

import fql.decl.Decl;
import fql.decl.QueryDecl;

/**
 * 
 * @author ryan
 *
 * Parser for query declarations.
 */
public class QueryDeclParser implements Parser<Decl> {

	@Override
	public Partial<Decl> parse(Tokens s) throws BadSyntax, IllTyped {
		Tokens olds = new FqlTokenizer(s.toString());
		
		try {
			Parser<?> p1 = new KeywordParser("query");
			Parser<?> p2 = new KeywordParser("=");
			Parser<?> p3 = new KeywordParser("id");
			Parser<String> p4 = new StringParser();
			Parser<String> p5 = new StringParser();
			
			Parser<?> colon = new KeywordParser(":");
			Parser<String> type1 = new StringParser();
			Parser<String> type2 = new StringParser();
			Parser<?> arrow = new KeywordParser("->");

			String ret1;
			Partial<?> p;
			p = p1.parse(s);
			s = p.tokens;
			Partial<String> q = p4.parse(s);
			s = q.tokens;
			ret1 = q.value;
			
			
			Partial<?> colonp = colon.parse(s);
			s = colonp.tokens;
			Partial<String> typep1 = type1.parse(s);
			s = typep1.tokens;
			Partial<?> arrowp = arrow.parse(s);
			s = arrowp.tokens;
			Partial<String> typep2 = type2.parse(s);
			s = typep2.tokens;
			
			p = p2.parse(s);
			s = p.tokens;
			
			p = p3.parse(s);
			s = p.tokens;
			q = p5.parse(s);

			return new Partial<Decl>(q.tokens, new QueryDecl(ret1, q.value, typep1.value, typep2.value, 0));

		} catch (BadSyntax e) {
		} catch (IllTyped e) {
		}
		try {
			s = olds;
			Parser<?> p1 = new KeywordParser("query");
			Parser<?> p2 = new KeywordParser("=");
			Parser<?> p3 = new KeywordParser("then");
			Parser<String> p4 = new StringParser();
			Parser<String> p5 = new StringParser();
			Parser<String> p6 = new StringParser();
			
			Parser<?> colon = new KeywordParser(":");
			Parser<String> type1 = new StringParser();
			Parser<String> type2 = new StringParser();
			Parser<?> arrow = new KeywordParser("->");

			String ret1, ret2, ret3;
			Partial<?> p;
			p = p1.parse(s);
			s = p.tokens;
			Partial<String> q = p4.parse(s);
			s = q.tokens;
			ret1 = q.value;
			
			Partial<?> colonp = colon.parse(s);
			s = colonp.tokens;
			Partial<String> typep1 = type1.parse(s);
			s = typep1.tokens;
			Partial<?> arrowp = arrow.parse(s);
			s = arrowp.tokens;
			Partial<String> typep2 = type2.parse(s);
			s = typep2.tokens;
			
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

			return new Partial<Decl>(s, new QueryDecl(ret1, ret3, ret2, typep1.value, typep2.value));
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
			
			Parser<?> colon = new KeywordParser(":");
			Parser<String> type1 = new StringParser();
			Parser<String> type2 = new StringParser();
			Parser<?> arrow = new KeywordParser("->");
			
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
			
			Partial<?> colonp = colon.parse(s);
			s = colonp.tokens;
			Partial<String> typep1 = type1.parse(s);
			s = typep1.tokens;
			Partial<?> arrowp = arrow.parse(s);
			s = arrowp.tokens;
			Partial<String> typep2 = type2.parse(s);
			s = typep2.tokens;
			
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

			return new Partial<Decl>(s, new QueryDecl(ret1, ret2, ret3, ret4, typep1.value, typep2.value));
		} catch (BadSyntax e) {
		} catch (IllTyped e) {
		}

		throw new BadSyntax(s, "Could not parse a query from " + s);
	}
}
