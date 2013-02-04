package fql;

import java.util.List;

import fql.parse.BadSyntax;
import fql.parse.IllTyped;
import fql.parse.KeywordParser;
import fql.parse.Parser;
import fql.parse.ParserUtils;
import fql.parse.Partial;
import fql.parse.StringParser;
import fql.parse.Tokens;

public class GivenInstanceDeclParser implements Parser<Decl> {

	@Override
	public Partial<Decl> parse(Tokens s) throws BadSyntax, IllTyped {
		String name;
		String type;
		
		Parser<?> pre = new KeywordParser("instance");
		Parser<?> q = new KeywordParser(":");
		Parser<?> e = new KeywordParser("=");
		
		Parser<?> sep = new KeywordParser(";");
		Parser<Pair<String, List<Pair<String, String>>>> p1 = new TableDeclParser();
		Parser<List<Pair<String, List<Pair<String, String>>>>> u = ParserUtils.manySep(p1, sep);
		
		Parser<?> l = new KeywordParser("{");
		Parser<?> r = new KeywordParser("}");
		Parser<List<Pair<String, List<Pair<String, String>>>>> u0 =ParserUtils.outside(l, u, r);
		
		Partial<?> x = pre.parse(s);
		s = x.tokens;
		
		Partial<String> y = new StringParser().parse(s);
		name = y.value;
		s = y.tokens;
		
		x = q.parse(s);
		s = x.tokens;
		
		y = new StringParser().parse(s);
		type = y.value;
		s = y.tokens;
		
		x = e.parse(s);
		s = x.tokens;
		
		Partial<List<Pair<String, List<Pair<String, String>>>>> aaa = u0.parse(s);
		
		GivenInstanceDecl d = new GivenInstanceDecl(name, type, aaa.value);
		
		return new Partial<Decl>(aaa.tokens, d);
	}

}
