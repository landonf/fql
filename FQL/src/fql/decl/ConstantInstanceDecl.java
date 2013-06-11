package fql.decl;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fql.Pair;

/**
 * @author ryan An instance given by explicit tuples.
 */
public class ConstantInstanceDecl extends InstanceDecl {

	public List<Pair<String, List<Pair<Object, Object>>>> data;

	public ConstantInstanceDecl(String name, String type,
			List<Pair<String, List<Pair<Object, Object>>>> data) {
		super(name, type);
		this.data = new LinkedList<Pair<String, List<Pair<Object, Object>>>>(
				data);
	}

	public ConstantInstanceDecl(String name, String type,
			Set<Map<String, Object>> data) {
		this(name, type, conv(data));

	}

	private static List<Pair<String, List<Pair<Object, Object>>>> conv(
			Set<Map<String, Object>> data2) {
		throw new RuntimeException();
	}

}
