package fql.decl;

import java.util.List;
import java.util.Map;

import fql.Pair;
import fql.Triple;
import fql.Unit;
import fql.decl.NewFQLProgram.NewSigConst;

public abstract class SigExp {

	//TODO Const equality should not matter order
	public static class Const extends SigExp {
		List<String> nodes;
		List<Triple<String, String, String>> attrs;
		List<Triple<String, String, String>> arrows;
		List<Pair<List<String>, List<String>>> eqs;

		public Const(List<String> nodes,
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

			return "{nodes " + printNodes + "; attributes " + printAttrs
					+ "; arrows " + printArrows + "; equations " + printEqs
					+ ";}";
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

		@Override
		public <R, E> R accept(E env, SigExpVisitor<R, E> v) {
			return v.visit(env, this);
		}
	}

	public static class Var extends SigExp {
		String v;

		public Var(String v) {
			if (v.contains(" ") || v.equals("void") || v.equals("unit")) {
				throw new RuntimeException();
			}
			this.v = v;
		}

		@Override
		public String toString() {
			return v;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((v == null) ? 0 : v.hashCode());
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
			Var other = (Var) obj;
			if (v == null) {
				if (other.v != null)
					return false;
			} else if (!v.equals(other.v))
				return false;
			return true;
		}

		@Override
		public <R, E> R accept(E env, SigExpVisitor<R, E> v) {
			return v.visit(env, this);
		}
	}

	public static class Zero extends SigExp {

		public Zero() {
		}

		@Override
		public int hashCode() {
			return 0;
		}

		@Override
		public boolean equals(Object obj) {
			return (obj instanceof Zero);
		}

		@Override
		public String toString() {
			return "void";
		}

		@Override
		public <R, E> R accept(E env, SigExpVisitor<R, E> v) {
			return v.visit(env, this);
		}
	}

	public static class One extends SigExp {
		
		public One() {
		}

		@Override
		public int hashCode() {
			return 0;
		}

		@Override
		public boolean equals(Object obj) {
			return (obj instanceof One);
		}

		@Override
		public String toString() {
			return "unit";
		}

		@Override
		public <R, E> R accept(E env, SigExpVisitor<R, E> v) {
			return v.visit(env, this);
		}
	}

	public static class Plus extends SigExp {
		SigExp a, b;

		public Plus(SigExp a, SigExp b) {
			this.a = a;
			this.b = b;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((a == null) ? 0 : a.hashCode());
			result = prime * result + ((b == null) ? 0 : b.hashCode());
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
			Plus other = (Plus) obj;
			if (a == null) {
				if (other.a != null)
					return false;
			} else if (!a.equals(other.a))
				return false;
			if (b == null) {
				if (other.b != null)
					return false;
			} else if (!b.equals(other.b))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "(" + a + " + " + b + ")";
		}

		@Override
		public <R, E> R accept(E env, SigExpVisitor<R, E> v) {
			return v.visit(env, this);
		}
	}

	public static class Times extends SigExp {
		SigExp a, b;

		public Times(SigExp a, SigExp b) {
			this.a = a;
			this.b = b;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((a == null) ? 0 : a.hashCode());
			result = prime * result + ((b == null) ? 0 : b.hashCode());
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
			Times other = (Times) obj;
			if (a == null) {
				if (other.a != null)
					return false;
			} else if (!a.equals(other.a))
				return false;
			if (b == null) {
				if (other.b != null)
					return false;
			} else if (!b.equals(other.b))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "(" + a + " * " + b + ")";
		}

		@Override
		public <R, E> R accept(E env, SigExpVisitor<R, E> v) {
			return v.visit(env, this);
		}
	}

	public static class Exp extends SigExp {
		SigExp a, b;

		public Exp(SigExp a, SigExp b) {
			this.a = a;
			this.b = b;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((a == null) ? 0 : a.hashCode());
			result = prime * result + ((b == null) ? 0 : b.hashCode());
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
			Exp other = (Exp) obj;
			if (a == null) {
				if (other.a != null)
					return false;
			} else if (!a.equals(other.a))
				return false;
			if (b == null) {
				if (other.b != null)
					return false;
			} else if (!b.equals(other.b))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "(" + a + " ^ " + b + ")";
		}

		@Override
		public <R, E> R accept(E env, SigExpVisitor<R, E> v) {
			return v.visit(env, this);
		}

	}

	@Override
	public abstract boolean equals(Object o);

	public abstract <R, E> R accept(E env, SigExpVisitor<R, E> v);

	@Override
	public abstract int hashCode();
	
	public Unit typeOf(Map<String, SigExp> env) {
		return accept(env, new SigExpChecker());
	}
	
	
	public interface SigExpVisitor<R, E> {
		public R visit (E env, Zero e) ;
		public R visit (E env, One e) ;
		public R visit (E env, Plus e) ;
		public R visit (E env, Times e) ;
		public R visit (E env, Exp e) ;
		public R visit (E env, Var e) ;
		public R visit (E env, Const e) ;
	}
	
}
