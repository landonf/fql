package fql.sql;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fql.DEBUG;
import fql.Pair;
import fql.Quad;
import fql.Triple;
import fql.decl.Edge;
import fql.decl.Instance;
import fql.decl.Node;
import fql.decl.Path;
import fql.decl.Transform;

/**
 * 
 * @author ryan
 * 
 *         Interpreter for PSM
 */
public class PSMInterp {

	public int guid = 0; 

	//keeps track of where guid was when sigma computations start,
	//so we can replay later for sigma on transform
	public Map<String, Integer> sigmas = new HashMap<>();
	public Map<String, Integer> sigmas2 = new HashMap<>();

	Map<String, Quad<Instance, Map<Node, Map<Object, Transform>>, Map<Node, Triple<Instance, Map<Object, Pair<Object, Object>>, Map<Pair<Object, Object>, Object>>>, Pair<Map<Node, Triple<Instance, Map<Object, Path>, Map<Path, Object>>>, Map<Edge, Transform>>>> exps = new HashMap<>();

	Map<String, Pair<Map<Node, Triple<Instance, Map<Object, Path>, Map<Path, Object>>>, Map<Edge, Transform>>> prop1 = new HashMap<>();
	Map<String, Pair<Instance, Map<Node, Pair<Map<Object, Instance>, Map<Instance, Object>>>>> prop2 = new HashMap<>();

	public  Pair<Map<String, Set<Map<Object, Object>>>, List<Throwable>> interpX(List<PSM> prog,
			Map<String, Set<Map<Object, Object>>> state) {
		List<Throwable> ret = new LinkedList<>();
		for (PSM cmd : prog) {
			try {
				cmd.exec(this, state);
			} catch (Throwable t) {
				if (DEBUG.debug.continue_on_error) {
					ret.add(t);
				} else {
					throw t;
				}
			}
		//	System.out.println("After " + cmd);
		//	System.out.println(state);
		}
		return new Pair<>(state, ret);
	}

	public Pair<Map<String, Set<Map<Object, Object>>>, List<Throwable>> interp(List<PSM> prog) {
		guid = 0;
		return interpX(prog, new HashMap<String, Set<Map<Object, Object>>>());
	}

}
