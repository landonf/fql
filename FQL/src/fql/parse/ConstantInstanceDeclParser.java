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
 *         Parser for explicit instances.
 */
public class ConstantInstanceDeclParser implements RyanParser<Decl> {

	RyanParser<List<Pair<String, List<Pair<String, String>>>>> make(String name,
			boolean refl) {
		RyanParser<?> sep = new KeywordParser(",");
		RyanParser<Pair<String, List<Pair<String, String>>>> p1 = new TableDeclParser();
		if (refl) {
			p1 = new TableDeclParser2();
		}
		RyanParser<List<Pair<String, List<Pair<String, String>>>>> u = ParserUtils
				.manySep(p1, sep);
		return ParserUtils.outside(new KeywordParser(name), u,
				new KeywordParser(";"));
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Partial<Decl> parse(Tokens s) throws BadSyntax, IllTyped {
		String name;
		String type;

		RyanParser<?> pre = new KeywordParser("instance");
		RyanParser<?> q = new KeywordParser(":");
		RyanParser<?> e = new KeywordParser("=");

		// Parser<?> sep = new KeywordParser(",");
		// Parser<Pair<String, List<Pair<String, String>>>> p1 = new
		// TableDeclParser();
		// Parser<List<Pair<String, List<Pair<String, String>>>>> u =
		// ParserUtils.manySep(p1, sep);
		//
		// Parser<?> lx = new KeywordParser("{");
		//
		// Parser<List<Pair<String, List<Pair<String, String>>>>> uX =
		// ParserUtils.manySep(new TableDeclParser2(), sep);
		// Parser<?> l = new KeywordParser(";");
		//
		// Parser<?> r = new KeywordParser("}");
		// Parser<List<Pair<String, List<Pair<String, String>>>>> u0
		// =ParserUtils.outside(l, u, r);

		Partial<?> x = pre.parse(s);
		s = x.tokens;

		Partial<String> y = new StringParser().parse(s);
		name = y.value;
		s = y.tokens;
		// System.out.println("AAAAA");
		x = q.parse(s);
		s = x.tokens;

		y = new StringParser().parse(s);
		type = y.value;
		s = y.tokens;
		// System.out.println("BBBBBB");
		x = e.parse(s);
		s = x.tokens;
		// System.out.println("CCCCCC");
		x = new KeywordParser("{").parse(s);
		s = x.tokens;
		// System.out.println("DDDDDDD");
		x = make("nodes", true).parse(s);
		s = x.tokens;
		List<?> nodes = (List<?>) x.value;
		// System.out.println("EEEEEEE");
		x = make("attributes", false).parse(s);
		s = x.tokens;
		List<?> attrs = (List<?>) x.value;
		// System.out.println("FFFFFFF");
		x = make("arrows", false).parse(s);
		s = x.tokens;
		List<?> arrows = (List<?>) x.value;
		// System.out.println("GGGGGG");
		LinkedList ooo = new LinkedList<>();
		ooo.addAll(attrs);
		ooo.addAll(arrows);
		ooo.addAll(nodes);

		x = new KeywordParser("}").parse(s);
		s = x.tokens;
		// System.out.println("HHHHHH");
		//
		// Partial<List<Pair<String, List<Pair<String, String>>>>> aaa0 =
		// uX.parse(s);
		// s = aaa0.tokens;
		// Partial<List<Pair<String, List<Pair<String, String>>>>> aaa =
		// u0.parse(s);
		// aaa.value.addAll(aaa0.value);
		//
		// List<Pair<String, List<Pair<Object, Object>>>> ooo =
		// new LinkedList<>();
		//
		// for (Pair<String, List<Pair<String, String>>> k : aaa.value) {
		//
		// List<Pair<Object, Object>> v = new LinkedList<>();
		// for (Pair<String,String> vvv : k.second) {
		// v.add(new Pair<Object, Object>(vvv.first, vvv.second));
		// }
		//
		// ooo.add(new Pair<String, List<Pair<Object, Object>>>(k.first, v));
		//
		// }
		//

		// /
		ConstantInstanceDecl d = new ConstantInstanceDecl(name, type, ooo);

		return new Partial<Decl>(s, d);
	}

}
