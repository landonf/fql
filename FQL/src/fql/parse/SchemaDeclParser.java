package fql.parse;

import java.util.List;

import fql.Pair;
import fql.Triple;
import fql.Unit;
import fql.decl.Decl;
import fql.decl.SignatureDecl;

public class SchemaDeclParser implements Parser<Decl> {

	

	
	@Override
	public Partial<Decl> parse(Tokens s) throws BadSyntax, IllTyped {
		
		Parser<Unit> sp = new KeywordParser("schema");
		Partial<Unit> p1 = sp.parse(s);
		
		Parser<String> strp = new StringParser();
		Partial<String> p2 = strp.parse(p1.tokens);
		
		String name = p2.value;
		
		//System.out.println("EEEE");
		
		Parser<Unit> sp2 = new KeywordParser("=");
		Partial<Unit> p3 = sp2.parse(p2.tokens);
		
		sp2 = new KeywordParser("{");
		p3 = sp2.parse(p3.tokens);
		
		//System.out.println("ZZZZ");
		
		Parser<Unit> xxx1 = new KeywordParser("nodes");
		p3 = xxx1.parse(p3.tokens);
		Parser<List<String>> x = ParserUtils.manySep(new StringParser(), new KeywordParser(","));
		Partial<List<String>> y = x.parse(p3.tokens);
		sp2 = new KeywordParser(";");
		p3 = sp2.parse(y.tokens);
		
		//System.out.println("AAAA");
		sp2 = new KeywordParser("attributes");
		p3 = sp2.parse(p3.tokens);
		//System.out.println("BBBB");
		
		Parser<List<Triple<String, String, String>>> ap = new ArrowsParser();
		Partial<List<Triple<String, String, String>>> ap2 = ap.parse(p3.tokens);
		List<Triple<String, String, String>> attrs = ap2.value;
		//System.out.println("CCCCC");
		sp2 = new KeywordParser(";");
		p3 = sp2.parse(ap2.tokens);
		//System.out.println("DDDDD");
		
		//
		//System.out.println("xxAAAA");
		sp2 = new KeywordParser("arrows");
		p3 = sp2.parse(p3.tokens);
		//System.out.println("xxBBBB");
		
		ap = new ArrowsParser();
		 ap2 = ap.parse(p3.tokens);
		List<Triple<String, String, String>> arrows = ap2.value;
		//System.out.println("xxCCCCC");
		sp2 = new KeywordParser(";");
		p3 = sp2.parse(ap2.tokens);
		//System.out.println("xxDDDDD");
		//
		
		sp2 = new KeywordParser("equations");
		p3 = sp2.parse(p3.tokens);
		
		Parser<List<Pair<List<String>, List<String>>>> ep = new EqsParser();
		Partial<List<Pair<List<String>, List<String>>>> ep2 = ep.parse(p3.tokens);
		List<Pair<List<String>, List<String>>> eqs = ep2.value;
		
		sp2 = new KeywordParser(";");
		p3 = sp2.parse(ep2.tokens);
		
		//System.out.println("QQQQQQ");
		
		sp2 = new KeywordParser("}");
		p3 = sp2.parse(p3.tokens);
		
		Decl d = new SignatureDecl(name, y.value, attrs, arrows, eqs);
		Partial<Decl> p = new Partial<Decl>(p3.tokens, d);
		return p;
	}

}
