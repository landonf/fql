package fql.decl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fql.Pair;
import fql.Triple;
import fql.Unit;

public class NewFQLProgram {
	
	NewProgram<NewSigConst, NewMapConst, NewInstConst, NewTransConst> input;
	
	NewProgram<NewSigConst, NewMapConst, NewInstConst, NewTransConst> types_inlined;
	// type check
	NewProgram<Signature, NewMapConst, NewInstConst, NewTransConst> sigs_compiled;
	NewProgram<Signature, Mapping, NewInstConst, NewTransConst> maps_compiled;
	NewProgram<Signature, Mapping, Instance, NewTransConst> insts_compiled;
	
	public static class DSP {
		Caml<Unit, NewSigConst, NewMapConst> a, b, c, d, e, f;

		public DSP(Caml<Unit, NewSigConst, NewMapConst> a,
				Caml<Unit, NewSigConst, NewMapConst> b,
				Caml<Unit, NewSigConst, NewMapConst> c,
				Caml<Unit, NewSigConst, NewMapConst> d,
				Caml<Unit, NewSigConst, NewMapConst> e,
				Caml<Unit, NewSigConst, NewMapConst> f) {
			super();
			this.a = a;
			this.b = b;
			this.c = c;
			this.d = d;
			this.e = e;
			this.f = f;
		}

		@Override
		public String toString() {
			return "DSP [a=" + a + ", b=" + b + ", c=" + c + ", d=" + d
					+ ", e=" + e + ", f=" + f + "]";
		}
		
	}
	
	public static class NewDecl {
		String name;
		Poly<Unit, NewSigConst> sig;
		Caml<Unit, NewSigConst, NewMapConst> map;
		Poly<Poly<Unit,NewSigConst>, NewInstConst> inst;
		Caml<Poly<Unit,NewSigConst>, NewInstConst, NewTransConst> trans;
		DSP query;
		
		Integer line;
		
		Pair<Poly<Unit,NewSigConst>, Poly<Unit,NewSigConst>> map_t;
		Poly<Unit,NewSigConst> inst_t;
		Pair<Poly<Poly<Unit,NewSigConst>, NewInstConst>, Poly<Poly<Unit,NewSigConst>, NewInstConst>> trans_t;
		Pair<Poly<Unit, NewSigConst>, Poly<Unit, NewSigConst>> query_t;

		public static NewDecl sigDecl(String name, Integer line, Poly<Unit, NewSigConst> sig) {
			NewDecl ret = new NewDecl(name, line);
			ret.sig = sig;
			return ret;
		}
		
		public static NewDecl mapDecl(String name, Integer line, Caml<Unit, NewSigConst, NewMapConst> map, Pair<Poly<Unit,NewSigConst>, Poly<Unit,NewSigConst>> map_t) {
			NewDecl ret = new NewDecl(name, line);
			ret.map = map; ret.map_t = map_t;
			return ret;
		}
		

		public static NewDecl instDecl(String name, Integer line, Poly<Poly<Unit,NewSigConst>, NewInstConst> inst, Poly<Unit,NewSigConst> inst_t) {
			NewDecl ret = new NewDecl(name, line);
			ret.inst = inst; ret.inst_t = inst_t;
			return ret;
		}
		
		public static NewDecl transDecl(String name, Integer line, Caml<Poly<Unit,NewSigConst>, NewInstConst, NewTransConst> trans, Pair<Poly<Poly<Unit,NewSigConst>, NewInstConst>, Poly<Poly<Unit,NewSigConst>, NewInstConst>> trans_t) {
			NewDecl ret = new NewDecl(name, line);
			ret.trans = trans; ret.trans_t = trans_t;
			return ret;
		}
		
		public static NewDecl queryDecl(String name, Integer line, DSP query, Pair<Poly<Unit, NewSigConst>,Poly<Unit, NewSigConst>> query_t) {
			NewDecl ret = new NewDecl(name, line);
			ret.query = query; ret.query_t = query_t;
			return ret;
	
		}
		public NewDecl(String name, Integer line) {
			this.name = name;
			this.line = line;
		}
	}
/*	
	Map<String, Poly<Unit,A>> sigs;
	Map<String, Caml<Unit,A, B>> maps;
	Map<String, Poly<Poly<Unit,A>, C>> insts;
	Map<String, Caml<Poly<Unit,A>, C, D>> trans;
*/

	Map<String, DSP> queries = new HashMap<>();
	Map<String, Pair<Poly<Unit, NewSigConst>, Poly<Unit, NewSigConst>>> queries_t = new HashMap<>();
	Map<String, Integer> queries_lines = new HashMap<>();
	
	public NewFQLProgram(List<NewDecl> decls) {
		Map<String, Poly<Unit, NewSigConst>> sigs = new HashMap<>();
		Map<String, Caml<Unit, NewSigConst, NewMapConst>> maps = new HashMap<>();
		Map<String, Poly<Poly<Unit, NewSigConst>, NewInstConst>> insts = new HashMap<>();
		Map<String, Caml<Poly<Unit, NewSigConst>, NewInstConst, NewTransConst>> trans = new HashMap<>();
		Map<String, Pair<Poly<Unit, NewSigConst>, Poly<Unit, NewSigConst>>> maps_t = new HashMap<>();
		Map<String, Poly<Unit, NewSigConst>> insts_t = new HashMap<>();
		Map<String, Pair<Poly<Poly<Unit, NewSigConst>, NewInstConst>, Poly<Poly<Unit, NewSigConst>, NewInstConst>>> trans_t = new HashMap<>();
		Map<String, Integer> sigs_lines = new HashMap<>();
		Map<String, Integer> maps_lines = new HashMap<>();
		Map<String, Integer> insts_lines = new HashMap<>();
		Map<String, Integer> trans_lines = new HashMap<>();
		
		
		for (NewDecl decl : decls) {
			if (decl.sig != null) {
				checkDup(decl.name, sigs, "signature");
				sigs.put(decl.name, decl.sig);
				sigs_lines.put(decl.name, decl.line);
			} else if (decl.inst != null) {
				checkDup(decl.name, insts, "instance");
				insts.put(decl.name, decl.inst);
				insts_t.put(decl.name, decl.inst_t);
				insts_lines.put(decl.name, decl.line);
			} else if (decl.map != null) {
				checkDup(decl.name, maps, "mapping");
				maps.put(decl.name, decl.map);
				maps_t.put(decl.name, decl.map_t);
				maps_lines.put(decl.name, decl.line);
			} else if (decl.trans != null) {
				checkDup(decl.name, maps, "transform");
				trans.put(decl.name, decl.trans);
				trans_t.put(decl.name, decl.trans_t);	
				trans_lines.put(decl.name, decl.line);
			} else if (decl.query != null) {
				checkDup(decl.name, maps, "query");
				queries.put(decl.name, decl.query);
				queries_t.put(decl.name, decl.query_t);	
				queries_lines.put(decl.name, decl.line);
			}
			
			else {
				throw new RuntimeException();
			}
		}
		
		input = new NewProgram<NewSigConst, NewMapConst, NewInstConst, NewTransConst>(sigs, maps, insts, trans, maps_t, insts_t, trans_t, sigs_lines, maps_lines, insts_lines, trans_lines);
	}
	private static <X,Y> void checkDup(X name, Map<X, Y> sigs, String s) {
		if (sigs.containsKey(name)) {
			throw new RuntimeException("Duplicate " + s + " " + name);
		}
	
}
	public static Environment run(String s) {
		
		return null;
	}
	
	public static class NewSigConst {
		List<String> nodes;
		List<Triple<String, String, String>> attrs;
		List<Triple<String, String, String>> arrows;
		List<Pair<List<String>, List<String>>> eqs;
		
		public NewSigConst(List<String> nodes,
				List<Triple<String, String, String>> attrs,
				List<Triple<String, String, String>> arrows,
				List<Pair<List<String>, List<String>>> eqs) {
			super();
			this.nodes = nodes;
			this.attrs = attrs;
			this.arrows = arrows;
			this.eqs = eqs;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((arrows == null) ? 0 : arrows.hashCode());
			result = prime * result + ((attrs == null) ? 0 : attrs.hashCode());
			result = prime * result + ((eqs == null) ? 0 : eqs.hashCode());
			result = prime * result + ((nodes == null) ? 0 : nodes.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			NewSigConst other = (NewSigConst) obj;
			if (arrows == null) {
				if (other.arrows != null)
					return false;
			} else if (!arrows.equals(other.arrows))
				return false;
			if (attrs == null) {
				if (other.attrs != null)
					return false;
			} else if (!attrs.equals(other.attrs))
				return false;
			if (eqs == null) {
				if (other.eqs != null)
					return false;
			} else if (!eqs.equals(other.eqs))
				return false;
			if (nodes == null) {
				if (other.nodes != null)
					return false;
			} else if (!nodes.equals(other.nodes))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "NewSigConst [nodes=" + nodes + ", attrs=" + attrs
					+ ", arrows=" + arrows + ", eqs=" + eqs + "]";
		}
	
		
	}

	
	public static class NewMapConst {

		
		private List<Pair<String, List<String>>> arrows;
		private List<Pair<String, String>> attrs;
		private List<Pair<String, String>> objs;

		public NewMapConst(List<Pair<String, String>> objs,
				List<Pair<String, String>> attrs,
				List<Pair<String, List<String>>> arrows) {
			this.objs = objs;
			this.attrs = attrs;
			this.arrows = arrows;
		}

		@Override
		public String toString() {
			return "NewMapConst [arrows=" + arrows + ", attrs=" + attrs
					+ ", objs=" + objs + "]";
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((arrows == null) ? 0 : arrows.hashCode());
			result = prime * result + ((attrs == null) ? 0 : attrs.hashCode());
			result = prime * result + ((objs == null) ? 0 : objs.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			NewMapConst other = (NewMapConst) obj;
			if (arrows == null) {
				if (other.arrows != null)
					return false;
			} else if (!arrows.equals(other.arrows))
				return false;
			if (attrs == null) {
				if (other.attrs != null)
					return false;
			} else if (!attrs.equals(other.attrs))
				return false;
			if (objs == null) {
				if (other.objs != null)
					return false;
			} else if (!objs.equals(other.objs))
				return false;
			return true;
		}
		
	}
	
	public static abstract class NewInstConst {
		
	}
	
	public static class NewTransConst {

		private List<Pair<String, List<Pair<String, String>>>> data;

		public NewTransConst(List<Pair<String, List<Pair<String, String>>>> data) {
			this.data = data;
		}

		@Override
		public String toString() {
			return "NewTransConst [data=" + data + "]";
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((data == null) ? 0 : data.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			NewTransConst other = (NewTransConst) obj;
			if (data == null) {
				if (other.data != null)
					return false;
			} else if (!data.equals(other.data))
				return false;
			return true;
		}
		
	}
	
	
	public static class Fin extends NewInstConst {
		private Poly<Unit, NewSigConst> ty;
		private List<Pair<String, List<Pair<Object, Object>>>> data;

		public Fin(Poly<Unit,NewSigConst> ty, List<Pair<String, List<Pair<Object, Object>>>> data) {
			this.data = data;
			this.ty = ty;
		}
	}
	public static class Delta extends NewInstConst {
		public Delta(Caml<Unit,NewSigConst, NewMapConst> F, Poly<Poly<Unit,NewSigConst>, NewInstConst> I) {
			
		}
	}
	public static class Sigma extends NewInstConst {
		public Sigma(Caml<Unit, NewSigConst, NewMapConst> F, Poly<Poly<Unit,NewSigConst>, NewInstConst> I) {
			
		}
	}
	public static class External extends NewInstConst {
		public External(Poly<Unit, NewSigConst> T) {
			
		}
	}
	public static class FullSigma extends NewInstConst {
		public FullSigma(Caml<Unit, NewSigConst, NewMapConst> F, Poly<Poly<Unit,NewSigConst>, NewInstConst> I) {
			
		}
	}
	public static class Pi extends NewInstConst {
		public Pi(Caml<Unit, NewSigConst, NewMapConst> F, Poly<Poly<Unit,NewSigConst>, NewInstConst> I) {
			
		}
	}
	public static class Relationalize extends NewInstConst {
		public Relationalize(Poly<Poly<Unit,NewSigConst>, NewInstConst> I) {
			
		}
	}

	@Override
	public String toString() {
		return "NewFQLProgram [input=" + input + ", types_inlined="
				+ types_inlined + ", sigs_compiled=" + sigs_compiled
				+ ", maps_compiled=" + maps_compiled + ", insts_compiled="
				+ insts_compiled + ", queries=" + queries + ", queries_t="
				+ queries_t + ", queries_lines=" + queries_lines + "]";
	}
	

}
