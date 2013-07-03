package fql.decl;

import java.util.LinkedList;
import java.util.List;

import fql.Pair;
import fql.Triple;

/**
 * 
 * @author ryan
 * 
 *         Declarations for signatures.
 */
public class SignatureDecl extends Decl {

	@Override
	public String toString() {
		return "SignatureDecl [nodes=" + nodes + ", arrows=" + arrows
				+ ", attrs=" + attrs + ", eqs=" + eqs + "]";
	}

	public List<String> nodes;

	public List<Triple<String, String, String>> arrows, attrs;

	public List<Pair<List<String>, List<String>>> eqs;

	public SignatureDecl(String name, List<String> nodes,
			List<Triple<String, String, String>> attrs,
			List<Triple<String, String, String>> arrows,
			List<Pair<List<String>, List<String>>> eqs) {
		super(name);
		this.nodes = nodes;
		this.attrs = attrs;
		this.arrows = new LinkedList<Triple<String, String, String>>(arrows);
		this.eqs = new LinkedList<Pair<List<String>, List<String>>>(eqs);
	}
}
