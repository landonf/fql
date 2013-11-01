package fql.decl;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import fql.Pair;
import fql.Triple;
import fql.Unit;

public class NewFQLProgram {


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
		Poly<Poly<Unit, NewSigConst>, NewInstConst> inst;
		Caml<Poly<Unit, NewSigConst>, NewInstConst, NewTransConst> trans;
		DSP query;

		@Override
		public String toString() {
			return "NewDecl [name=" + name + ", sig=" + sig + ", map=" + map
					+ ", inst=" + inst + ", trans=" + trans + ", query="
					+ query + ", line=" + line + ", map_t=" + map_t
					+ ", inst_t=" + inst_t + ", trans_t=" + trans_t
					+ ", query_t=" + query_t + "]";
		}

		Integer line;

		Pair<Poly<Unit, NewSigConst>, Poly<Unit, NewSigConst>> map_t;
		Poly<Unit, NewSigConst> inst_t;
		Pair<Poly<Poly<Unit, NewSigConst>, NewInstConst>, Poly<Poly<Unit, NewSigConst>, NewInstConst>> trans_t;
		Pair<Poly<Unit, NewSigConst>, Poly<Unit, NewSigConst>> query_t;

		public static NewDecl sigDecl(String name, Integer line,
				Poly<Unit, NewSigConst> sig) {
			NewDecl ret = new NewDecl(name, line);
			ret.sig = sig;
			return ret;
		}

		public static NewDecl mapDecl(String name, Integer line,
				Caml<Unit, NewSigConst, NewMapConst> map,
				Pair<Poly<Unit, NewSigConst>, Poly<Unit, NewSigConst>> map_t) {
			NewDecl ret = new NewDecl(name, line);
			ret.map = map;
			ret.map_t = map_t;
			return ret;
		}

		public static NewDecl instDecl(String name, Integer line,
				Poly<Poly<Unit, NewSigConst>, NewInstConst> inst,
				Poly<Unit, NewSigConst> inst_t) {
			NewDecl ret = new NewDecl(name, line);
			ret.inst = inst;
			ret.inst_t = inst_t;
			return ret;
		}

		public static NewDecl transDecl(
				String name,
				Integer line,
				Caml<Poly<Unit, NewSigConst>, NewInstConst, NewTransConst> trans,
				Pair<Poly<Poly<Unit, NewSigConst>, NewInstConst>, Poly<Poly<Unit, NewSigConst>, NewInstConst>> trans_t) {
			NewDecl ret = new NewDecl(name, line);
			ret.trans = trans;
			ret.trans_t = trans_t;
			return ret;
		}

		public static NewDecl queryDecl(String name, Integer line, DSP query,
				Pair<Poly<Unit, NewSigConst>, Poly<Unit, NewSigConst>> query_t) {
			NewDecl ret = new NewDecl(name, line);
			ret.query = query;
			ret.query_t = query_t;
			return ret;

		}

		public NewDecl(String name, Integer line) {
			this.name = name;
			this.line = line;
		}
	}

	/*
	 * Map<String, Poly<Unit,A>> sigs; Map<String, Caml<Unit,A, B>> maps;
	 * Map<String, Poly<Poly<Unit,A>, C>> insts; Map<String, Caml<Poly<Unit,A>,
	 * C, D>> trans;
	 */

//	Map<String, DSP> queries = new HashMap<>();
//	Map<String, Pair<Poly<Unit, NewSigConst>, Poly<Unit, NewSigConst>>> queries_t = new HashMap<>();
//	Map<String, Integer> queries_lines = new HashMap<>();

	public static NewProgram<NewSigConst, NewMapConst, NewInstConst, NewTransConst> make(List<NewDecl> decls) {
		LinkedHashMap<String, Poly<Unit, NewSigConst>> sigs = new LinkedHashMap<>();
		LinkedHashMap<String, Caml<Unit, NewSigConst, NewMapConst>> maps = new LinkedHashMap<>();
		LinkedHashMap<String, Poly<Poly<Unit, NewSigConst>, NewInstConst>> insts = new LinkedHashMap<>();
		LinkedHashMap<String, Caml<Poly<Unit, NewSigConst>, NewInstConst, NewTransConst>> trans = new LinkedHashMap<>();
		LinkedHashMap<String, Pair<Poly<Unit, NewSigConst>, Poly<Unit, NewSigConst>>> maps_t = new LinkedHashMap<>();
		LinkedHashMap<String, Poly<Unit, NewSigConst>> insts_t = new LinkedHashMap<>();
		LinkedHashMap<String, Pair<Poly<Poly<Unit, NewSigConst>, NewInstConst>, Poly<Poly<Unit, NewSigConst>, NewInstConst>>> trans_t = new LinkedHashMap<>();
		LinkedHashMap<String, Integer> sigs_lines = new LinkedHashMap<>();
		LinkedHashMap<String, Integer> maps_lines = new LinkedHashMap<>();
		LinkedHashMap<String, Integer> insts_lines = new LinkedHashMap<>();
		LinkedHashMap<String, Integer> trans_lines = new LinkedHashMap<>();

		List<String> order = new LinkedList<>();
		for (NewDecl decl : decls) {
			order.add(decl.name);
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
			} 
			/* else if (decl.query != null) {
				checkDup(decl.name, maps, "query");
				queries.put(decl.name, decl.query);
				queries_t.put(decl.name, decl.query_t);
				queries_lines.put(decl.name, decl.line);
			} */

			else {
				throw new RuntimeException();
			}
		}

		return new NewProgram<NewSigConst, NewMapConst, NewInstConst, NewTransConst>(
				sigs, maps, insts, trans, maps_t, insts_t, trans_t, sigs_lines,
				maps_lines, insts_lines, trans_lines, order);
	}

	private static <X, Y> void checkDup(X name, Map<X, Y> sigs, String s) {
		if (sigs.containsKey(name)) {
			throw new RuntimeException("Duplicate " + s + " " + name);
		}

	}

	// public static Environment run(String s) {
	//
	// return null;
	// }

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
			String ret = "";
			String printNodes = "";
			boolean b = false;
			for (String n : nodes) {
				if (b) {
					printNodes += ", ";
				}
				printNodes += n;
				b = true;
			}
			
			String printAttrs = "";
			b = false;
			for (Triple<String, String, String> a : attrs) {
				if (b) {
					printAttrs += ", ";
				}
				printAttrs += a.first + ": " + a.second + " -> " + a.third;
				b = true;
			}
			
			String printArrows = "";
			b = false;
			for (Triple<String, String, String> a : arrows) {
				if (b) {
					printArrows += ", ";
				}
				printArrows += a.first + ": " + a.second + " -> " + a.third;
				b = true;
			}
			
			String printEqs = "";
			b = false;
			for (Pair<List<String>, List<String>> a : eqs) {
				if (b) {
					printEqs += ", ";
				}
				printEqs += printOneEq(a.first) + " = " + printOneEq(a.second);				
				b = true;
			}
			
			return "{nodes " + printNodes + "; attributes " + printAttrs + "; arrows " + printArrows + "; equations " + printEqs + ";}";
		}

		private String printOneEq(List<String> l) {
			String ret = "";
			boolean b = false;
			for (String a : l) {
				if (b) {
					ret += ".";
				}
				ret += a;
			}
			return ret;
		}

	}

	public static class NewMapConst {

		public List<Pair<String, List<String>>> arrows;
		public List<Pair<String, String>> attrs;
		public List<Pair<String, String>> objs;

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
	
	public abstract static interface NewInstConstVisitor<R,E> {
		
		public abstract R visit (E env, Fin e);
		
		public abstract R visit (E env, Delta e);
		
		public abstract R visit (E env, Sigma e);
		
		public abstract R visit (E env, Pi e);
		
		public abstract R visit (E env, FullSigma e);
		
		public abstract R visit (E env, Relationalize e);
		
		public abstract R visit (E env, External e);
		
	}

	public static abstract class NewInstConst {
		
		public abstract <R,E> R accept(E env, NewInstConstVisitor<R, E> v);

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
		public List<Pair<String, List<Pair<Object, Object>>>> data;
		public Poly<Unit, NewSigConst> type;

		public Fin(Poly<Unit, NewSigConst> type,
				List<Pair<String, List<Pair<Object, Object>>>> data) {
			this.data = data;
			this.type = type;
		}

		@Override
		public String toString() {
			return data.toString();
		}

		@Override
		public <R, E> R accept(E env, NewInstConstVisitor<R, E> v) {
			return v.visit(env, this);
		}
		
		
	}

	public static class Delta extends NewInstConst {
		
		public Caml<Unit, NewSigConst, NewMapConst> F;
		public Poly<Poly<Unit, NewSigConst>, NewInstConst> I;
		
		public Delta(Caml<Unit, NewSigConst, NewMapConst> F,
				Poly<Poly<Unit, NewSigConst>, NewInstConst> I) {
			this.F = F;
			this.I = I;
		}
		@Override
		public <R, E> R accept(E env, NewInstConstVisitor<R, E> v) {
			return v.visit(env, this);
		}

	}

	public static class Sigma extends NewInstConst {
		public Caml<Unit, NewSigConst, NewMapConst> F;
		public Poly<Poly<Unit, NewSigConst>, NewInstConst> I;

		public Sigma(Caml<Unit, NewSigConst, NewMapConst> F,
				Poly<Poly<Unit, NewSigConst>, NewInstConst> I) {
			this.F = F;
			this.I = I;
		}
		@Override
		public <R, E> R accept(E env, NewInstConstVisitor<R, E> v) {
			return v.visit(env, this);
		}

	}

	public static class External extends NewInstConst {
		public String name;
		public Poly<Unit, NewSigConst> T;

		public External(String name,
				Poly<Unit, NewSigConst> T) {
			this.name = name;
			this.T = T;
		}
		
		@Override
		public <R, E> R accept(E env, NewInstConstVisitor<R, E> v) {
			return v.visit(env, this);
		}

	}

	public static class FullSigma extends NewInstConst {
		public Caml<Unit, NewSigConst, NewMapConst> F;
		public Poly<Poly<Unit, NewSigConst>, NewInstConst> I;
		public FullSigma(Caml<Unit, NewSigConst, NewMapConst> F,
				Poly<Poly<Unit, NewSigConst>, NewInstConst> I) {
			this.F = F;
			this.I = I;
		}
		@Override
		public <R, E> R accept(E env, NewInstConstVisitor<R, E> v) {
			return v.visit(env, this);
		}

	}

	public static class Pi extends NewInstConst {
		public Caml<Unit, NewSigConst, NewMapConst> F;
		public Poly<Poly<Unit, NewSigConst>, NewInstConst> I;
		public Pi(Caml<Unit, NewSigConst, NewMapConst> F,
				Poly<Poly<Unit, NewSigConst>, NewInstConst> I) {
			this.F = F;
			this.I = I;
		}
		@Override
		public <R, E> R accept(E env, NewInstConstVisitor<R, E> v) {
			return v.visit(env, this);
		}

	}

	public static class Relationalize extends NewInstConst {
		
		public Poly<Poly<Unit, NewSigConst>, NewInstConst> I;
		public Relationalize(Poly<Poly<Unit, NewSigConst>, NewInstConst> I) {
			this.I = I;
		}
		@Override
		public <R, E> R accept(E env, NewInstConstVisitor<R, E> v) {
			return v.visit(env, this);
		}

	}


}
