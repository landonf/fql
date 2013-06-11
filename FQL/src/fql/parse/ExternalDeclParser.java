package fql.parse;

import fql.decl.Decl;

/**
 * 
 * @author ryan
 * 
 *         Parser for external instances.
 */
public class ExternalDeclParser implements Parser<Decl> {

	@Override
	public Partial<Decl> parse(Tokens s) throws BadSyntax, IllTyped {
		String name;
		String type;

		Parser<?> pre = new KeywordParser("instance");
		Parser<?> q = new KeywordParser(":");
		Parser<?> e = new KeywordParser("=");

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

		x = new KeywordParser("external").parse(s);
		s = x.tokens;

		ExternalDecl d = new ExternalDecl(name, type);

		return new Partial<Decl>(s, d);
	}

}
