package fql;

import java.util.List;

import fql.parse.BadSyntax;
import fql.parse.IllTyped;
import fql.parse.Parser;
import fql.parse.ParserUtils;
import fql.parse.Partial;
import fql.parse.Tokens;

public class CommandsParser implements Parser<Commands> {

	@Override
	public Partial<Commands> parse(Tokens s) throws BadSyntax, IllTyped {
		Partial<List<Command>> x = ParserUtils.many(new CommandParser()).parse(s);
		
		return new Partial<Commands>(x.tokens, new Commands(x.value));
	}

}
