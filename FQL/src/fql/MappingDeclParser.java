package fql;

import java.util.List;

import fql.parse.BadSyntax;
import fql.parse.IllTyped;
import fql.parse.KeywordParser;
import fql.parse.Parser;
import fql.parse.ParserUtils;
import fql.parse.Partial;
import fql.parse.PathParser;
import fql.parse.StringParser;
import fql.parse.Tokens;

/**
 * 
 * @author ryan
 *
 * Parser for mapping declarations.
 */
public class MappingDeclParser implements Parser<Decl> {

	@Override
	public Partial<Decl> parse(Tokens s) throws BadSyntax, IllTyped {
		Tokens olds = new Tokens(s.toString());
		try {
			Parser<?> p1 = new KeywordParser("mapping");
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
			
			return new Partial<Decl>(q.tokens, new MappingDecl(ret1, q.value));
			
		} catch (BadSyntax e) {
		} catch (IllTyped e) {
		}
		try {
			s = olds;
			Parser<?> p1 = new KeywordParser("mapping");
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
			
			q = p6.parse(s);
			s = q.tokens;
			ret3 = q.value;
			
			return new Partial<Decl>(s, new MappingDecl(ret1, ret2, ret3));
		} catch (BadSyntax e) {
		} catch (IllTyped e) {
		} try {
			s = olds;
			Parser<?> mapping = new KeywordParser("mapping");
			Parser<?> equals = new KeywordParser("=");
			Parser<?> colon = new KeywordParser(":");
			Parser<String> name = new StringParser();
			Parser<?> arr = new KeywordParser("->");
			Parser<?> comma = new KeywordParser(",");
			Parser<?> lparen = new KeywordParser("(");
			Parser<?> rparen = new KeywordParser(")");
			Parser<?> semi = new KeywordParser(";");
			Parser<?> lbracket = new KeywordParser("{");
			Parser<?> rbracket = new KeywordParser("}");
			
			Parser<Pair<String,String>> type = 
						ParserUtils.inside(name, arr, name);
			
			Parser<List<String>> path = new PathParser();
			
			Parser<List<Pair<String, String>>> nm = 
					ParserUtils.manySep(
					ParserUtils.outside(lparen, 
						ParserUtils.inside(name, comma, name), 
						rparen), comma);
		
			Parser<List<Pair<String, List<String>>>> em = 
					ParserUtils.manySep(
					ParserUtils.outside(lparen, 
						ParserUtils.inside(name, comma, path), 
						rparen), comma);


Partial<Pair<String, Pair<Pair<String, String>, Pair<List<Pair<String, String>>, List<Pair<String, List<String>>>>>>>  
x = ParserUtils.seq(mapping, 
			ParserUtils.inside(name, colon,
			ParserUtils.inside(type, equals, 
			ParserUtils.outside(lbracket, 
					ParserUtils.inside(nm, semi, em), rbracket)))).parse(s);

			Decl d = new MappingDecl(x.value.first, x.value.second.first.first, x.value.second.first.second, x.value.second.second.first, x.value.second.second.second);
			return new Partial<Decl>(x.tokens, d);
		} catch (BadSyntax e) {
		} catch (IllTyped e) {
		}
		throw new BadSyntax("Cannot Parse Mappign Decl from " + s);
	}

}
