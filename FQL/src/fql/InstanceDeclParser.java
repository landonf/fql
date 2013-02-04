package fql;

import fql.parse.BadSyntax;
import fql.parse.IllTyped;
import fql.parse.Parser;
import fql.parse.Partial;
import fql.parse.Tokens;

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
