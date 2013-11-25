package fql.sql;

import java.util.Map;
import java.util.Set;

import fql.DEBUG;

/**
 * 
 * @author ryan
 *
 * Class for PSM, which raps SQL.
 */
public abstract class PSM {
	
	public final static String INTEGER = "INTEGER";
	public final static String VARCHAR() {
		return "VARCHAR(" + DEBUG.debug.varlen + ")";
	}

	public  abstract void exec(PSMInterp interp, Map<String, Set<Map<Object, Object>>> state);
	
	public  abstract String toPSM(); 

	@Override
	public String toString() {
		return toPSM();
	}
}
