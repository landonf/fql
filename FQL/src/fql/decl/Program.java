package fql.decl;

import java.util.LinkedList;
import java.util.List;

import fql.parse.BadSyntax;
import fql.parse.FqlTokenizer;
import fql.parse.IllTyped;
import fql.parse.Partial;
import fql.parse.ProgramParser;
import fql.parse.Tokens;

/**
 * 
 * @author ryan
 *
 * Syntax for FQL programs.
 */
public class Program {
	
	public static Program parse(String program) throws BadSyntax, IllTyped {
		Tokens t = new FqlTokenizer(program);
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

	public List<Decl> decls;

	public boolean hasKey(String s) {
		for (Decl d : decls) {
			if (d.name.equals(s)) {
				return true;
			}
		}
		return false;
	}

	public List<String> inorder() {
		List<String> ret = new LinkedList<>();
		for (Decl d : decls) {
			ret.add(d.name);
		}
		return ret;
	}

	public boolean isMapping(String s) {
		for (Decl d : decls) {
			if (d.name.equals(s)) {
				if (d instanceof MappingDecl) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean isSignature(String s) {
		for (Decl d : decls) {
			if (d.name.equals(s)) {
				if (d instanceof SignatureDecl) {
					return true;
				}
			}
		}
		return false;
	}
	
		
}
