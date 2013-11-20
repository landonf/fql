package fql.decl;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

public class FQLProgram {


		public static class NewDecl {
			List<String> drop;
			TransExp trans;
			String name;
			SigExp sig;
			MapExp map;
			InstExp inst;
			Integer line;
			QueryExp query;
			List<String> enums;

//			Pair<SigExp, SigExp> map_t;
	//		SigExp inst_t;
			
			public static NewDecl typeDecl(String name, List<String> values, Integer line) {
				NewDecl ret = new NewDecl(name, line);
				ret.enums = values;
				return ret;
			}

			
			
			public static NewDecl transDecl(String name, Integer line, TransExp trans) {
				NewDecl ret = new NewDecl(name, line);
				ret.trans = trans;
				return ret;		
			}
			
			public static NewDecl dropDecl(List<String> drop) {
				NewDecl ret = new NewDecl(null, null);
				ret.drop = drop;
				return ret;		
			}
			
			public static NewDecl queryDecl(String name, Integer line, QueryExp query) {
				NewDecl ret = new NewDecl(name, line);
				ret.query = query;
				return ret;
			}

			public static NewDecl sigDecl(String name, Integer line,
					SigExp sig) {
				NewDecl ret = new NewDecl(name, line);
				ret.sig = sig;
				return ret;
			}

			public static NewDecl mapDecl(String name, Integer line,
					MapExp map
					/* , Pair<SigExp, SigExp> map_t */) {
				NewDecl ret = new NewDecl(name, line);
				ret.map = map;
			//	ret.map_t = map_t;
				return ret;
			}

			public static NewDecl instDecl(String name, Integer line,
					InstExp inst) {
				NewDecl ret = new NewDecl(name, line);
				ret.inst = inst;
				return ret;
			}

			public NewDecl(String name, Integer line) {
				this.name = name;
				this.line = line;
			}
		}

		public LinkedHashMap<String, Type> enums = new LinkedHashMap<>();
		public LinkedHashMap<String, SigExp> sigs = new LinkedHashMap<>();
		public LinkedHashMap<String, MapExp> maps = new LinkedHashMap<>();
		public LinkedHashMap<String, InstExp> insts = new LinkedHashMap<>();
		public LinkedHashMap<String, QueryExp> queries = new LinkedHashMap<>();
		public LinkedHashMap<String, TransExp> transforms = new LinkedHashMap<>();
		
		public LinkedHashMap<String, Integer> lines = new LinkedHashMap<>();
		public List<String> drop = new LinkedList<>(); 
		public List<String> order = new LinkedList<>();
		
		public FQLProgram(List<NewDecl> decls) {
			for (NewDecl decl : decls) {
				if (decl.name != null) {
					order.add(decl.name); //drops are unnamed
				} 
				if (decl.enums != null) {
					checkDup(decl.name, "enum");
					enums.put(decl.name, new Type.Enum(decl.name, new HashSet<Object>(decl.enums)));
					lines.put(decl.name, decl.line);
				} else if (decl.sig != null) {
					checkDup(decl.name, "signature");
					sigs.put(decl.name, decl.sig);
					lines.put(decl.name, decl.line);
				} else if (decl.inst != null) {
					checkDup(decl.name, "instance");
					insts.put(decl.name, decl.inst);
					lines.put(decl.name, decl.line);
				} else if (decl.map != null) {
					checkDup(decl.name, "mapping");
					maps.put(decl.name, decl.map);
					lines.put(decl.name, decl.line);
				} else if (decl.query != null) {
					checkDup(decl.name, "query");
					queries.put(decl.name, decl.query);
					lines.put(decl.name, decl.line);
				} else if (decl.drop != null) {
					drop.addAll(decl.drop);
				} else if (decl.trans != null) {
					checkDup(decl.name, "transform");
					transforms.put(decl.name, decl.trans);
					lines.put(decl.name, decl.line);
				}
				else {
					throw new RuntimeException();
				}
			}
			enums.put("int", new Type.Int());
			enums.put("string", new Type.Varchar());
		}

		@Override
		public String toString() {
			return "FQLProgram [sigs=" + sigs + ", maps=" + maps + ", insts="
					+ insts + ", queries=" + queries + ", transforms="
					+ transforms + ", lines=" + lines + ", drop=" + drop
					+ ", order=" + order + "]";
		}

		private <X, Y> void checkDup(X name, String s) {
			if (sigs.containsKey(name) || maps.containsKey(name) || insts.containsKey(name) || queries.containsKey(name) || transforms.containsKey(name) || enums.containsKey(name)) {
				throw new RuntimeException("Duplicate " + s + " " + name);
			}

		}
		
}
