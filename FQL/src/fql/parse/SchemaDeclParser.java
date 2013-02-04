package fql.parse;

import java.util.List;

import fql.Decl;
import fql.Pair;
import fql.SignatureDecl;
import fql.Triple;

public class SchemaDeclParser implements Parser<Decl> {

	

	
	@Override
	public Partial<Decl> parse(Tokens s) throws BadSyntax, IllTyped {
		Parser<Unit> sp = new KeywordParser("schema");
		Partial<Unit> p1 = sp.parse(s);
		
		Parser<String> strp = new StringParser();
		Partial<String> p2 = strp.parse(p1.tokens);
		
		String name = p2.value;
		
		Parser<Unit> sp2 = new KeywordParser("=");
		Partial<Unit> p3 = sp2.parse(p2.tokens);
		
		sp2 = new KeywordParser("{");
		p3 = sp2.parse(p3.tokens);
		
		Parser<List<Triple<String, String, String>>> ap = new ArrowsParser();
		Partial<List<Triple<String, String, String>>> ap2 = ap.parse(p3.tokens);
		List<Triple<String, String, String>> arrows = ap2.value;
		
		sp2 = new KeywordParser(";");
		p3 = sp2.parse(ap2.tokens);
		
		Parser<List<Pair<List<String>, List<String>>>> ep = new EqsParser();
		Partial<List<Pair<List<String>, List<String>>>> ep2 = ep.parse(p3.tokens);
		List<Pair<List<String>, List<String>>> eqs = ep2.value;
		
		sp2 = new KeywordParser("}");
		p3 = sp2.parse(ep2.tokens);
		
		Decl d = new SignatureDecl(name, arrows, eqs);
		Partial<Decl> p = new Partial<Decl>(p3.tokens, d);
		return p;
	}

}
