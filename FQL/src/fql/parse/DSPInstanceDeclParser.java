package fql.parse;

import fql.decl.Decl;
import fql.decl.EvalDSPInstanceDecl;

/**
 * 
 * @author ryan
 * 
 *         Parser for instances given by delta, sigma, pi
 */
public class DSPInstanceDeclParser implements RyanParser<Decl> {

	@Override
	public Partial<Decl> parse(Tokens s0) throws BadSyntax, IllTyped {
		try {
			return doParse("delta", s0, false);
		} catch (Exception e) {
		}
		try {
			return doParse("sigma", s0, false);
		} catch (Exception e) {
		}
		try {
			return doParse("SIGMA", s0, false);
		} catch (Exception e) {
		}
		try {
			return doParse("pi", s0, false);
		} catch (Exception e) {
		}
		try {
			return doParse("relationalize", s0, true);
		} catch (Exception e) {
		}

		throw new BadSyntax(s0,
				"Could not parse delta/sigma/pi instance decl from " + s0);
	}

	private Partial<Decl> doParse(String kind, Tokens s0, boolean isRelativize) throws BadSyntax,
			IllTyped {
		Tokens s = s0;

		RyanParser<?> pre = new KeywordParser("instance");

		RyanParser<?> e = new KeywordParser("=");
		RyanParser<?> ev = new KeywordParser(kind);
		RyanParser<String> p1 = new StringParser();
		RyanParser<String> p2 = new StringParser();
		RyanParser<String> p3 = new StringParser();
		String s1, s2, s3;

		Partial<?> x = pre.parse(s);
		s = x.tokens;
		Partial<String> y = p1.parse(s);
		s = y.tokens;
		s1 = y.value;

		String type;
		RyanParser<String> typ = new StringParser();
		RyanParser<String> typ0 = ParserUtils.seq(new KeywordParser(":"), typ);
		y = typ0.parse(s);
		s = y.tokens;
		type = y.value;

		x = e.parse(s);
		s = x.tokens;
		x = ev.parse(s);
		s = x.tokens;
		y = p2.parse(s);
		s = y.tokens;
		s2 = y.value;
		
		if (isRelativize) {
			return new Partial<Decl>(s, new EvalDSPInstanceDecl(s1, kind, s2, null,
					type));
		}
		
		y = p3.parse(s);
		s = y.tokens;
		s3 = y.value;

		// System.out.println("type is " + type);

		return new Partial<Decl>(s, new EvalDSPInstanceDecl(s1, kind, s2, s3,
				type));
	}

}
