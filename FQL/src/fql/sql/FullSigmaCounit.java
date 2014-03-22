package fql.sql;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import fql.decl.Attribute;
import fql.decl.Edge;
import fql.decl.Mapping;
import fql.decl.Node;

public class FullSigmaCounit extends PSM {
	
	Mapping F;
	String i1, i2, i3;
	public String trans;
	
	
	public FullSigmaCounit(Mapping F, String i1, String i2, String i3, String trans) {
		this.F = F;
		this.i1 = i1;
		this.i2 = i2;
		this.i3 = i3;
		this.trans = trans;
	}

	@Override
	public void exec(PSMInterp interp,
			Map<String, Set<Map<Object, Object>>> state) {
		Set<Map<Object, Object>> lineage = state.get(i3 + "_lineage");
		//System.out.println(lineage);
		
		for (Node n : F.target.nodes) {
		//	System.out.println("n is " + n);
			Set<Map<Object, Object>> i3i = state.get(i3 + "_" + n);
			Set<Map<Object, Object>> m = new HashSet<>();
			for (Map<Object, Object> row : i3i) {
				Object id = row.get("c0").toString();
			//	System.out.println("id is " + id);
				for (Map<Object, Object> v : lineage) {
					Object id0 = v.get("c0").toString();
				//	System.out.println("checking " + id0);
					if (id.equals(id0)) {
						String node = v.get("c1").toString();
						String idX = v.get("c2").toString();
						String[] cols = v.get("c3").toString().split("\\s+");
						
					//	System.out.println("initial " + node + " idX " + idX + " cols " + Arrays.toString(cols));
						
						Set<Map<Object, Object>> subst_inv = state.get(i2 + "_" + node + "_subst_inv");
					//	System.out.println("i2 is " + i2);
					//	System.out.println("node is " + node);
					//	System.out.println("Fn " + F.nm.get(n));
					//	System.out.println("Fnode " + F.nm.get(node));
					//	System.out.println(state.keySet());
					//	System.out.println("subst_inv: " + subst_inv);
						for (Map<Object, Object> y : subst_inv) {
							if (y.get("c0").toString().equals(idX)) {
								String ret = y.get("c1").toString();
				//				System.out.println("ret " + ret);
								for (String col : cols) {
									if (col.trim().length() == 0) {
										continue;
									}
					//				System.out.println("looking for " + i1 + "_" + col);
									Set<Map<Object, Object>> u = state.get(i1 + "_" + col);
									for (Map<Object, Object> e : u) {
						//				System.out.println("checking " + e);
										if (e.get("c0").toString().equals(ret)) {
											ret = e.get("c1").toString();
							//				System.out.println("updated ret " + ret);
										}
									}
								}
								Map<Object, Object> rowX = new HashMap<>();
								rowX.put("c0", id);
								rowX.put("c1", ret);
								m.add(rowX);
							}
						}
					}
				}
			}
		//	System.out.println("Putting " + n + ":" + m);
			state.put(trans + "_" + n, m);			
		}
		
		for (Attribute<Node> n : F.target.attrs) {
			state.put(trans + "_" + n.name, new HashSet<Map<Object, Object>>());
		}
		for (Edge n : F.target.edges) {
			state.put(trans + "_" + n.name, new HashSet<Map<Object, Object>>());
		}
	}

	@Override
	public String toPSM() {
		throw new RuntimeException("Cannot generate SQL for full sigma counit");
	}

}
