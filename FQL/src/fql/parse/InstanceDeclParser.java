package fql.parse;

import fql.decl.Decl;

/**
 * 
 * @author ryan
 *
 * Parser for various kinds of instances.
 */
public class InstanceDeclParser implements Parser<Decl> {

	@Override
	public Partial<Decl> parse(Tokens s) throws BadSyntax, IllTyped {
		Parser<Decl> p;
		try {
			p = new GivenInstanceDeclParser();
			return p.parse(s);
		} catch (Exception e) {
			
		}
		try {
			p = new EvalInstanceDeclParser();
			return p.parse(s);
		} catch (Exception e) {
			
		}
		
		try {
			p = new DirectInstanceDeclParser();
			return p.parse(s);
		} catch (Exception e) {
			
		}

		throw new BadSyntax("Cannot parse instance decl");
	}

}
