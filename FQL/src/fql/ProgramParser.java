package fql;

import java.util.List;

import fql.parse.BadSyntax;
import fql.parse.IllTyped;
import fql.parse.Parser;
import fql.parse.ParserUtils;
import fql.parse.Partial;
import fql.parse.Tokens;

public class ProgramParser implements Parser<Program> {

	@Override
	public Partial<Program> parse(Tokens s) throws BadSyntax, IllTyped {
		Parser<List<Decl>> p = ParserUtils.many(new DeclParser());
		Partial<List<Decl>> x = p.parse(s);
		
		Program ret = new Program(x.value);
		return new Partial<Program>(x.tokens, ret);
	}

}
