package fql.decl;

import java.util.LinkedList;
import java.util.List;

import fql.Pair;
import fql.Triple;

/**
 * 
 * @author ryan
 *
 * Declarations for signatures.
 */
public class SignatureDecl extends Decl {

	@Override
	public String toString() {
		return "SignatureDecl [name=" + name + ", arrows=" + arrows + ", eqs="
				+ eqs + "]";
	}

	public List<Triple<String, String, String>> arrows;

	public List<Pair<List<String>, List<String>>> eqs;

	public SignatureDecl(String name,
			List<Triple<String, String, String>> arrows,
			List<Pair<List<String>, List<String>>> eqs) {
		super(name);
		this.arrows = new LinkedList<Triple<String, String, String>>(arrows);
		this.eqs = new LinkedList<Pair<List<String>, List<String>>>(eqs);
	}
}
