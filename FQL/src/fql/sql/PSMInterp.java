package fql.sql;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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


	public  Map<String, Set<Map<Object, Object>>> interpX(List<PSM> prog,
			Map<String, Set<Map<Object, Object>>> state) {
		for (PSM cmd : prog) {
			cmd.exec(this, state);
		}
		return state;
	}

	public Map<String, Set<Map<Object, Object>>> interp(List<PSM> prog) {
		guid = 0;
		return interpX(prog, new HashMap<String, Set<Map<Object, Object>>>());
	}

}
