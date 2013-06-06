package fql.parse;

import java.util.LinkedList;
import java.util.List;

import fql.Pair;
import fql.decl.ConstantInstanceDecl;
import fql.decl.Decl;

/**
 * 
 * @author ryan
 * 
 * Parser for explicit instances.
 */
public class ConstantInstanceDeclParser implements Parser<Decl> {

	@Override
	public Partial<Decl> parse(Tokens s) throws BadSyntax, IllTyped {
		String name;
		String type;
		
		Parser<?> pre = new KeywordParser("instance");
		Parser<?> q = new KeywordParser(":");
		Parser<?> e = new KeywordParser("=");
		
		Parser<?> sep = new KeywordParser(",");
		Parser<Pair<String, List<Pair<String, String>>>> p1 = new TableDeclParser();
		Parser<List<Pair<String, List<Pair<String, String>>>>> u = ParserUtils.manySep(p1, sep);
		
		Parser<?> lx = new KeywordParser("{");
		
		Parser<List<Pair<String, List<Pair<String, String>>>>> uX = ParserUtils.seq(lx, ParserUtils.manySep(new TableDeclParser2(), sep));
		Parser<?> l = new KeywordParser(";");
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
		
		Partial<List<Pair<String, List<Pair<String, String>>>>> aaa0 = uX.parse(s);
		s = aaa0.tokens;
		Partial<List<Pair<String, List<Pair<String, String>>>>> aaa = u0.parse(s);
		aaa.value.addAll(aaa0.value);
		
		List<Pair<String, List<Pair<Object, Object>>>> ooo  = 
				new LinkedList<>();
				
		for (Pair<String, List<Pair<String, String>>> k : aaa.value) {
			
			List<Pair<Object, Object>> v = new LinkedList<>();
			for (Pair<String,String> vvv : k.second) {
				v.add(new Pair<Object, Object>(vvv.first, vvv.second));
			}
			
			ooo.add(new Pair<String, List<Pair<Object, Object>>>(k.first, v));
			
		}
				
		
///		
		ConstantInstanceDecl d = new ConstantInstanceDecl(name, type, ooo);
		
		return new Partial<Decl>(aaa.tokens, d);
	}

}
