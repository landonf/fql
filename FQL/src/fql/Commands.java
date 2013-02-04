package fql;

import java.util.LinkedList;
import java.util.List;

import fql.parse.BadSyntax;
import fql.parse.IllTyped;
import fql.parse.Partial;
import fql.parse.Tokens;

public class Commands {

	public static Commands parse(String program) throws BadSyntax, IllTyped {
		Tokens t = new Tokens(program);
		Partial<Commands> p = new CommandsParser().parse(t);
		if (p.tokens.toString().trim().length() > 0) {
			throw new BadSyntax("Uncomsumed input: " + p.tokens.toString().trim());
		}
		return p.value;
	}

	
	List<Command> commands;
	
	public Commands(List<Command> commands) {
		this.commands = new LinkedList<Command>(commands);
	}

	@Override
	public String toString() {
		return "Commands [commands=" + commands + "]";
	}

}
