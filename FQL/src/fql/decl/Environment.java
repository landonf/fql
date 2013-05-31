package fql.decl;

import java.awt.Color;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fql.FQLException;
import fql.Pair;
import fql.Triple;
import fql.gui.Viewable;

/**
 * 
 * @author ryan
 *
 * The environment keeps a list of schemas, mappings, instances, and queries.
 * It also keeps a global static assignment of colors to schema names.
 * It wraps colors after they've been used.
 */
public class Environment {

	static Map<String, Color> colors;
	static Map<String, Object> graphs;

	public Environment(Program p) throws FQLException {
		signatures = new HashMap<String, Signature>();
		mappings = new HashMap<String, Mapping>();
		queries = new HashMap<String, Query>();
		instances = new HashMap<String, Instance>();		
		Set<String> names = new HashSet<String>();
		colors =  new HashMap<String, Color>();
		for (Decl d : p.decls) {
			if (names.contains(d.name)) {
				throw new FQLException("Duplicate schema: " + d.name);
			}
			names.add(d.name);

			if (d instanceof SignatureDecl) {
				addSchema((SignatureDecl) d);
			} else if (d instanceof GivenInstanceDecl) {
				addInstance((GivenInstanceDecl) d);
			} else if (d instanceof MappingDecl) {
				addMapping((MappingDecl) d);
			} else if (d instanceof QueryDecl) {
				addQuery((QueryDecl) d);
			} else if (d instanceof EvalInstanceDecl) {
				addInstance((EvalInstanceDecl) d);
			} else if (d instanceof EvalDSPInstanceDecl) {
				addInstance((EvalDSPInstanceDecl) d);
			}
			
			else {
				throw new FQLException("Unknown declaration " + d);
			}
		}
		
		for (String s : signatures.keySet()) {
			colors.put(s, nextColor());
		}
		
	}
	
	int index = 0;
	static Color[] colors0 = new Color[] { Color.YELLOW, Color.GREEN, Color.BLUE, Color.RED, Color.GRAY, Color.ORANGE, Color.PINK, Color.WHITE
		, Color.DARK_GRAY, Color.CYAN, Color.MAGENTA, Color.PINK
	};
	private Color nextColor() {
		if (index == colors0.length) {
			index = 0;
		}
		return colors0[index++];
	}

	private void addInstance(EvalDSPInstanceDecl e) throws FQLException {
		Mapping m = mappings.get(e.mapping);
		Instance i = instances.get(e.inst);
		if (m == null) {
			throw new FQLException("Cannot find mapping " + e.mapping);
		}
		if (i == null) {
			throw new FQLException("Cannot find instance " + e.inst);
		}
		instances.put(e.name, new Instance(e.name, m, i, e.type));
	}

	private void addInstance(EvalInstanceDecl d) throws FQLException {
		//instance x = eval q I
		Query thequery = queries.get(d.query);
		Instance theinstance = instances.get(d.inst);
		if (thequery == null) {
			throw new FQLException("Cannot find query " + d.query);
		}
		if (theinstance == null) {
			throw new FQLException("Cannot find instance " + d.inst);
		}
		instances.put(d.name, new Instance(d.name, thequery, theinstance));
	}

	private void addSchema(SignatureDecl schemaDecl) throws FQLException {
		List<Triple<String, String, String>> arrows = schemaDecl.arrows;
		String name = schemaDecl.name;
		List<Pair<List<String>, List<String>>> eqs = schemaDecl.eqs;

		Signature signature = new Signature(schemaDecl.name, arrows, eqs);
		signatures.put(name, signature);		
	}

	private void addQuery(QueryDecl queryDecl) throws FQLException {
		Query q = new Query(queryDecl.name, this, queryDecl);
		queries.put(queryDecl.name, q);
	}

	private void addMapping(MappingDecl mappingDecl) throws FQLException {
		Mapping m = new Mapping(mappingDecl.name, this, mappingDecl);
		mappings.put(mappingDecl.name, m);
	}

	private void addInstance(GivenInstanceDecl instanceDecl) throws FQLException {
		Signature thesig = signatures.get(instanceDecl.type);
		instances.put(instanceDecl.name, new Instance(instanceDecl.name, thesig, instanceDecl.data));
	}


	public Map<String, Signature>  signatures;
	public Map<String, Mapping> mappings;
	public Map<String, Query> queries;
	public Map<String, Instance> instances;
	
	public Viewable<?> get(String name) throws FQLException {
		Viewable<?> v = null;

		v = signatures.get(name);
		if (v != null) {
			return v;
		}
		
		v = mappings.get(name);
		if (v != null) {
			return v;
		}
		
		v = queries.get(name);
		if (v != null) {
			return v;
		}
		
		v = instances.get(name);
		if (v != null) {
			return v;
		}
		
		throw new FQLException("Cannot find " + name);		
	}

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
			throw new FQLException("Cannot find mapping " + s0);
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
