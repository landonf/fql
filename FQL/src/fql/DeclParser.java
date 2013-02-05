package fql;

import fql.parse.BadSyntax;
import fql.parse.IllTyped;
import fql.parse.Parser;
import fql.parse.Partial;
import fql.parse.SchemaDeclParser;
import fql.parse.Tokens;

/**
 * 
 * @author ryan
 *
 * Parser for declarations.
 */
public class DeclParser implements Parser<Decl> {

	@Override
	public Partial<Decl> parse(Tokens s) throws BadSyntax, IllTyped {
		try {
			SchemaDeclParser p = new SchemaDeclParser();
			return p.parse(s);
		} catch (BadSyntax e) {
		} catch (IllTyped e) {
		}
		try {
			InstanceDeclParser p = new InstanceDeclParser();
			return p.parse(s);
		} catch (BadSyntax e) {
		} catch (IllTyped e) {
		}
//		try {
//			EvalInstanceDeclParser p = new EvalInstanceDeclParser();
//			return p.parse(s);
//		} catch (BadSyntax e) {
//		} catch (IllTyped e) {
//		}
		
		try {
			MappingDeclParser p = new MappingDeclParser();
			return p.parse(s);
		} catch (BadSyntax e) {
		} catch (IllTyped e) {
		}
		try {
			QueryDeclParser p = new QueryDeclParser();
			return p.parse(s);
		} catch (BadSyntax e) {
		} catch (IllTyped e) {
		}
		
		throw new BadSyntax("Cannot parse from " + s);
		
	}

}
