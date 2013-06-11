package fql.sql;

import java.util.Map;
import java.util.Set;

/**
 * 
 * @author ryan
 *
 * SQL expressions
 */
public abstract class SQL {

	public abstract Set<Map<String, Object>> eval(
			Map<String, Set<Map<String, Object>>> state);

	public abstract String toPSM();

	@Override
	public String toString() {
		return toPSM();
	}

}
