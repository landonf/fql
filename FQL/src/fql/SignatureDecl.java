package fql;

import java.util.LinkedList;
import java.util.List;

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

	List<Triple<String, String, String>> arrows;

	List<Pair<List<String>, List<String>>> eqs;

	public SignatureDecl(String name,
			List<Triple<String, String, String>> arrows,
			List<Pair<List<String>, List<String>>> eqs) {
		super(name);
		this.arrows = new LinkedList<Triple<String, String, String>>(arrows);
		this.eqs = new LinkedList<Pair<List<String>, List<String>>>(eqs);
	}
}
