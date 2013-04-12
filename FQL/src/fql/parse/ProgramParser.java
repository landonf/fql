package fql.parse;

import java.util.List;

import fql.decl.Decl;
import fql.decl.Program;

/**
 * 
 * @author ryan
 *
 * Parser for programs.
 */
public class ProgramParser implements Parser<Program> {

	@Override
	public Partial<Program> parse(Tokens s) throws BadSyntax, IllTyped {
		Parser<List<Decl>> p = ParserUtils.many(new DeclParser());
		Partial<List<Decl>> x = p.parse(s);
		
		Program ret = new Program(x.value);
		return new Partial<Program>(x.tokens, ret);
	}

}
