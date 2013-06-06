package fql.decl;

import java.awt.Color;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fql.FQLException;
import fql.Pair;
import fql.Triple;
import fql.cat.FDM;
import fql.cat.FinFunctor;
import fql.cat.Inst;
import fql.gui.Viewable;
import fql.sql.PSM;
import fql.sql.PSMGen;
import fql.sql.PSMInterp;

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
		
		Map<String, Signature> it = new HashMap<>();
		for (Decl d : p.decls) {
			if (names.contains(d.name)) {
				throw new FQLException("Duplicate schema: " + d.name);
			}
			names.add(d.name);

			if (d instanceof SignatureDecl) {
				addSchema((SignatureDecl) d);
			} else if (d instanceof MappingDecl) {
				addMapping((MappingDecl) d);
			} else if (d instanceof QueryDecl) {
				addQuery((QueryDecl) d);
			} else if (d instanceof EvalInstanceDecl) {
				
		//		addInstance((EvalInstanceDecl) d);
			} else if (d instanceof EvalDSPInstanceDecl) {
		//		addInstance((EvalDSPInstanceDecl) d);
			} else if (d instanceof ConstantInstanceDecl) {
		//		addInstance((GivenInstanceDecl) d);
			}
			
			else {
				throw new FQLException("Unknown declaration " + d);
			}
		}
		
		List<PSM> psm = PSMGen.compile0(this, p);
		Map<String, Set<Map<String, Object>>> output0 = PSMInterp.interp(psm);

		for (Decl d : p.decls) {
			if (d instanceof InstanceDecl) {
				InstanceDecl d0 = (InstanceDecl) d;
				addInstance(new ConstantInstanceDecl(d.name, d0.type, gather(d.name, getSchema(d0.type), output0)));
			}
		}
		
		try {
		for (Decl d : p.decls) { //double check pi
			if (d instanceof EvalDSPInstanceDecl) {
				EvalDSPInstanceDecl x = (EvalDSPInstanceDecl) d;
				if (x.kind.equals("pi")) {
					Mapping m = mappings.get(x.mapping);
					Instance i = instances.get(x.inst);
					
				//	FinFunctor<Object, Object, Object, Object> F;
					//Inst<Object, Object, Object, Object> inst;
						
					Inst<Node, Path, Object, Object> res = FDM.pi(m.toFunctor2().first, i.toFunctor2());
				
					//System.out.println("**********" + d.name);
					//System.out.println(res);
				}
			}
		} } catch (Throwable e) {
			e.printStackTrace();
		}
			
		//go through instance decls, looking up in output as necessary
		
		for (String s : signatures.keySet()) {
			colors.put(s, nextColor());
		}
		
	}
	
	public static List<Pair<String, List<Pair<Object, Object>>>> gather(String pre,
			Signature sig, Map<String, Set<Map<String, Object>>> state) {
		List<Pair<String, List<Pair<Object, Object>>>> ret = new LinkedList<>();
		
		for (Node n : sig.nodes) {
			Set<Map<String, Object>> v = state.get(pre + "_" + n.string);
			if (v == null) {
				throw new RuntimeException("Missing: " + pre + "_" + n.string + " in " + state);
			}
			ret.add(new Pair<>(n.string, gather0(v)));
		}
		for (Edge e : sig.edges) {
			
			Set<Map<String, Object>> v = state.get(pre + "_" + e.name);
			
			ret.add(new Pair<>(e.name, gather0(v)));
		}
		for (Attribute a : sig.attrs) {
			Set<Map<String, Object>> v = state.get(pre + "_" + a.name);
			ret.add(new Pair<>(a.name, gather0(v)));
		}
		
		return ret;
	}

	private static List<Pair<Object, Object>> gather0(Set<Map<String, Object>> v) {
		List<Pair<Object, Object>> ret = new LinkedList<>();
		
		for (Map<String, Object> o : v) {
			ret.add(new Pair<>(o.get("c0"), o.get("c1")));
		}
		
		return ret;
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

	private void addInstance(ConstantInstanceDecl instanceDecl) throws FQLException {
		Signature thesig = signatures.get(instanceDecl.type);
		instances.put(instanceDecl.name, new Instance(instanceDecl.name, thesig,  instanceDecl.data));
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
			throw new FQLException("Cannot find mapping " + s0 + " in " + mappings);
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
