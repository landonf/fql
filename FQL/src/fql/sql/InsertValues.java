package fql.sql;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class InsertValues extends PSM {

	String name;
	List<String> attrs;
	Set<Map<String, Object>> values;
	
	public InsertValues(String name, List<String> attrs, Set<Map<String, Object>> values) {
		this.name = name;
		this.attrs = attrs;
		this.values = values;
	}

	/*
	 * INSERT INTO TABLE (column-a, [column-b, ...])
     * VALUES ('value-1a', ['value-1b', ...]),
     *        ('value-2a', ['value-2b', ...]),
	 */
	@Override
	public String toPSM() {
		String attrsStr = "";
		for (int i = 0; i < attrs.size(); i++) {
			if (i > 0) {
				attrsStr += ", ";
			}
			attrsStr += attrs.get(i);
		}
		
		int i = 0;
		String valuesStr = "";
		for (Map<String, Object> row : values) {
			String rowStr = "";
			for (int j = 0; j < attrs.size(); j++) {
				if (j > 0) {
					rowStr += ", ";
				}
				rowStr += row.get(attrs.get(j));
			}
			if (i++ > 0) {
				valuesStr += ", ";
			}
			valuesStr += "(" + rowStr + ")";
		}
		for (i = 0; i < attrs.size(); i++) {
			if (i > 0) {
				attrsStr += ", ";
			}
			attrsStr += "'" + attrs.get(i) + "'";
		}
		
		
		return "INSERT INTO " + name + "(" + attrsStr + ") VALUES " + valuesStr + ";";
	}

	@Override
	public void exec(Map<String, Set<Map<String, Object>>> state) {
		if (state.get(name).size() != 0) {
			throw new RuntimeException("table not empty: " + name);
		}
		state.put(name,  values);
	}

}
