package fql.sql;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fql.DEBUG;
import fql.Pair;

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
