package fql.sql;

import java.util.Map;
import java.util.Set;

public abstract class PSM {
	
	public final static String INTEGER = "INTEGER";
	public final static String VARCHAR = "VARCHAR(128)";

	public  abstract void exec(Map<String, Set<Map<String, Object>>> state);
	
	public  abstract String toPSM(); 

}
