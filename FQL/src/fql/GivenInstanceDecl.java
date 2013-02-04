package fql;

import java.util.LinkedList;
import java.util.List;

public class GivenInstanceDecl extends InstanceDecl {

	List<Pair<String, List<Pair<String, String>>>> data;
	String type;
	
	public GivenInstanceDecl(String name, String type,
			List<Pair<String, List<Pair<String, String>>>> data) {
		super(name);
		this.type = type;
		this.data = new LinkedList<Pair<String, List<Pair<String, String>>>>(data);
	}

}
