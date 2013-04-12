package fql.decl;

import java.util.LinkedList;
import java.util.List;

import fql.Pair;

/**
 * @author ryan
 * An instance given by explicit tuples.
 */
public class GivenInstanceDecl extends InstanceDecl {

	public List<Pair<String, List<Pair<String, String>>>> data;
	public String type;
	
	public GivenInstanceDecl(String name, String type,
			List<Pair<String, List<Pair<String, String>>>> data) {
		super(name);
		this.type = type;
		this.data = new LinkedList<Pair<String, List<Pair<String, String>>>>(data);
	}

}
