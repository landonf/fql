package fql.decl;

import java.util.Map;
import java.util.Set;

import fql.FQLException;

/**
 * 
 * @author ryan
 * 
 *         The environment keeps a list of schemas, mappings, instances, and
 *         queries. It also keeps a global static assignment of colors to schema
 *         names. It wraps colors after they've been used.
 */
public class Environment {

	public Environment(Map<String, Signature> signatures,
			Map<String, Mapping> mappings, Map<String, Instance> instances,
			Map<String, Query> queries, Map<String, Transform> transforms,
			Map<String, FullQuery> full_queries) {
		super();
		this.signatures = signatures;
		this.mappings = mappings;
		this.instances = instances;
		this.queries = queries;
		this.transforms = transforms;
		this.full_queries = full_queries;
		//doColors();
	}

//	public Map<String, Color> colors;

	//public static Color[] colors_arr = new Color[] { Color.RED, Color.GREEN, Color.BLUE, Color.MAGENTA, Color.yellow, Color.CYAN, Color.GRAY, Color.ORANGE, Color.PINK, Color.BLACK};
	
	public Set<Eq> eqs;
//	public String name0;
/*
	public void doColors() {
		colors = new HashMap<>();
		int i = 0;
		for (String n : instances.keySet()) {
			if (i == colors_arr.length) {
				colors.put(n, Color.WHITE);
			} else {
				colors.put(n, colors_arr[i++]);
			}
		}
	}
	*/

	public Map<String, Signature> signatures;
	public Map<String, Mapping> mappings;
	public Map<String, Query> queries;
	public Map<String, FullQuery> full_queries;
	public Map<String, Instance> instances;
	public Map<String, Transform> transforms;

	
	public Signature getSchema(String s0) throws FQLException {
		Signature s = signatures.get(s0);
		if (s == null) {
			throw new FQLException("Cannot find schema " + s0);
		}
		return s;
	}

	public Mapping getMapping(String s0) throws FQLException {
		Mapping s = mappings.get(s0);
		if (s == null) {
			throw new FQLException("Cannot find mapping " + s0 + " in "
					+ mappings);
		}
		return s;
	}

	public Query getQuery(String s0) throws FQLException {
		Query s = queries.get(s0);
		if (s == null) {
			throw new FQLException("Cannot find query " + s0);
		}
		return s;
	}

}
