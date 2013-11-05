package fql.decl;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

public class NewestFQLProgram {


		public static class NewDecl {
			String name;
			SigExp sig;
			MapExp map;
			InstExp inst;
			Integer line;
			QueryExp query;

//			Pair<SigExp, SigExp> map_t;
	//		SigExp inst_t;
			
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

		public LinkedHashMap<String, SigExp> sigs = new LinkedHashMap<>();
		public LinkedHashMap<String, MapExp> maps = new LinkedHashMap<>();
		public LinkedHashMap<String, InstExp> insts = new LinkedHashMap<>();
		public LinkedHashMap<String, QueryExp> queries = new LinkedHashMap<>();

		public LinkedHashMap<String, Integer> sigs_lines = new LinkedHashMap<>();
		public LinkedHashMap<String, Integer> maps_lines = new LinkedHashMap<>();
		public LinkedHashMap<String, Integer> insts_lines = new LinkedHashMap<>();
		public LinkedHashMap<String, Integer> queries_lines = new LinkedHashMap<>();
		
		public List<String> order = new LinkedList<>();
		
		public NewestFQLProgram(List<NewDecl> decls) {
			for (NewDecl decl : decls) {
				order.add(decl.name);
				if (decl.sig != null) {
					checkDup(decl.name, "signature");
					sigs.put(decl.name, decl.sig);
					sigs_lines.put(decl.name, decl.line);
				} else if (decl.inst != null) {
					checkDup(decl.name, "instance");
					insts.put(decl.name, decl.inst);
					insts_lines.put(decl.name, decl.line);
				} else if (decl.map != null) {
					checkDup(decl.name, "mapping");
					maps.put(decl.name, decl.map);
					maps_lines.put(decl.name, decl.line);
				} else if (decl.query != null) {
					checkDup(decl.name, "query");
					queries.put(decl.name, decl.query);
					queries_lines.put(decl.name, decl.line);
				} else {
					throw new RuntimeException();
				}
			}

		}

		private <X, Y> void checkDup(X name, String s) {
			if (sigs.containsKey(name) || maps.containsKey(name) || insts.containsKey(name) || queries.containsKey(name)) {
				throw new RuntimeException("Duplicate " + s + " " + name);
			}

		}

		

}
