package fql;

import fql.parse.BadSyntax;
import fql.parse.IllTyped;
import fql.parse.Parser;
import fql.parse.Partial;
import fql.parse.Tokens;

/**
 * 
 * @author ryan
 *
 * Parser for commands.
 */
public class CommandParser implements Parser<Command> {

	@Override
	public Partial<Command> parse(Tokens s) throws BadSyntax, IllTyped {
		Parser<Command> p;
		
		try {
			p = new ShowCommandParser();
				return p.parse(s);
		} catch (Exception e) {
			
		}
		try {
			p = new EqCommandParser();
				return p.parse(s);
		} catch (Exception e) {
			
		}
		try {
			p = new IsoCommandParser();
				return p.parse(s);
		} catch (Exception e) {
			
		}
		try {
			p = new IsosCommandParser();
				return p.parse(s);
		} catch (Exception e) {
			
		}
		try {
			p = new HomosCommandParser();
				return p.parse(s);
		} catch (Exception e) {
			
		}
		throw new BadSyntax("Unknown Command ");
	}

}
