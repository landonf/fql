package fql.sql;

import java.util.Map;
import java.util.Set;

public abstract class SQL {

	public  abstract Set<Object[]> eval(Map<String, Set<Object[]>> state);
	
	public  abstract String toPSM(); 
}
