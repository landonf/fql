package fql.decl;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import fql.Pair;
import fql.Triple;
import fql.Unit;
import fql.decl.NewFQLProgram.DSP;

public class NewestFQLProgram {


		public static class NewDecl {
			String name;
			SigExp sig;
			MapExp map;
			InstExp inst;
			Integer line;

			Pair<SigExp, SigExp> map_t;
			SigExp inst_t;

			public static NewDecl sigDecl(String name, Integer line,
					SigExp sig) {
				NewDecl ret = new NewDecl(name, line);
				ret.sig = sig;
				return ret;
			}

			public static NewDecl mapDecl(String name, Integer line,
					MapExp map,
					Pair<SigExp, SigExp> map_t) {
				NewDecl ret = new NewDecl(name, line);
				ret.map = map;
				ret.map_t = map_t;
				return ret;
			}

			public static NewDecl instDecl(String name, Integer line,
					InstExp inst,
					SigExp inst_t) {
				NewDecl ret = new NewDecl(name, line);
				ret.inst = inst;
				ret.inst_t = inst_t;
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
		public LinkedHashMap<String, Pair<SigExp, SigExp>> maps_t = new LinkedHashMap<>();
		public LinkedHashMap<String, SigExp> insts_t = new LinkedHashMap<>();
		public LinkedHashMap<String, Integer> sigs_lines = new LinkedHashMap<>();
		public LinkedHashMap<String, Integer> maps_lines = new LinkedHashMap<>();
		public LinkedHashMap<String, Integer> insts_lines = new LinkedHashMap<>();
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
					insts_t.put(decl.name, decl.inst_t);
					insts_lines.put(decl.name, decl.line);
				} else if (decl.map != null) {
					checkDup(decl.name, "mapping");
					maps.put(decl.name, decl.map);
					maps_t.put(decl.name, decl.map_t);
					maps_lines.put(decl.name, decl.line);
				} else {
					throw new RuntimeException();
				}
			}

		}

		private <X, Y> void checkDup(X name, String s) {
			if (sigs.containsKey(name) || maps.containsKey(name) || insts.containsKey(name)) {
				throw new RuntimeException("Duplicate " + s + " " + name);
			}

		}

		

}
