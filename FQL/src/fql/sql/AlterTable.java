package fql.sql;

import java.util.Map;
import java.util.Set;

import fql.Pair;

//TODO select distinct and select 
//TODO union vs union all
//TODO run fql button
//TODO 2 options for create table - plain RA, or use keys with "clobber" semantics
public class AlterTable extends PSM {
	
	String name, key;
	
	Map<String, String> cols;
	
	Map<String, Pair<String, String>> fks;

	@Override
	public void exec(PSMInterp interp,
			Map<String, Set<Map<Object, Object>>> state) {
		// TODO exec for alter table
		
	}

	@Override
	public String toPSM() {
		// TODO to psm for alter table
		return null;
	}
	
	
}
