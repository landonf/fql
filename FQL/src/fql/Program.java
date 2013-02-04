package fql;

import java.util.LinkedList;
import java.util.List;

import fql.parse.BadSyntax;
import fql.parse.IllTyped;
import fql.parse.Partial;
import fql.parse.Tokens;

public class Program {
	
	public static Program parse(String program) throws BadSyntax, IllTyped {
		Tokens t = new Tokens(program);
		Partial<Program> p = new ProgramParser().parse(t);
		if (p.tokens.toString().trim().length() > 0) {
			throw new BadSyntax("Uncomsumed input: " + p.tokens.toString().trim());
		}
		return p.value;
	}
	
	@Override
	public String toString() {
		return "Program [decls=" + decls + "]";
	}

	public Program(List<Decl> decls) {
		this.decls = new LinkedList<Decl>(decls);
	}

	List<Decl> decls;
		
}
