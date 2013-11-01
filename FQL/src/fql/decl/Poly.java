package fql.decl;

public abstract class Poly<T, C> {

	
	public static class Const<T, C> extends Poly<T, C> {
		public C c;
		public T t;
		public Const(C c, T t) {
			this.c = c;
			this.t = t;
			if (c == null) {
				throw new RuntimeException();
			}
			if (t == null) {
				throw new RuntimeException();
			}
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + ((c == null) ? 0 : c.hashCode());
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
			Const<?,?> other = (Const<?,?>) obj;
			if (c == null) {
				if (other.c != null)
					return false;
			} else if (!c.equals(other.c))
				return false;
			return true;
		}
		@Override
		public String toString() {
			return (c + " " + t).trim();
		}
		@Override
		public <R, E> R accept(E env, PolyVisitor<T, C, R, E> v) {
			return v.visit(env, this);
		}
	}
	
	public static class Var<T, C> extends Poly<T, C> {
		String v;
		
		public Var(String v) {
			if (v.contains(" ")) {
				
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
			int result = super.hashCode();
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
			Var<?,?> other = (Var<?,?>) obj;
			if (v == null) {
				if (other.v != null)
					return false;
			} else if (!v.equals(other.v))
				return false;
			return true;
		}
		@Override
		public <R, E> R accept(E env, PolyVisitor<T, C, R, E> v) {
			return v.visit(env, this);
		}
	}
	
	public static class Zero<T, C> extends Poly<T,C> {
		T t;
		public Zero(T t) {
			this.t = t;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + ((t == null) ? 0 : t.hashCode());
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
			Zero other = (Zero) obj;
			if (t == null) {
				if (other.t != null)
					return false;
			} else if (!t.equals(other.t))
				return false;
			return true;
		}
		
		@Override
		public String toString() {
			return ("void " + t).trim();
		}
		@Override
		public <R, E> R accept(E env, PolyVisitor<T, C, R, E> v) {
			return v.visit(env, this);
		}
	}

	public static class One<T, C> extends Poly<T, C> {
		T t;
		public One(T t) {
			this.t = t;
			if (t == null) {
				throw new RuntimeException();
			}
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + ((t == null) ? 0 : t.hashCode());
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
			One other = (One) obj;
			if (t == null) {
				if (other.t != null)
					return false;
			} else if (!t.equals(other.t))
				return false;
			return true;
		}
		
		@Override
		public String toString() {
			return ("unit " + t).trim();
		}
		@Override
		public <R, E> R accept(E env, PolyVisitor<T, C, R, E> v) {
			return v.visit(env, this);
		}
	}
	
	public static class Two<T, C> extends Poly<T, C> {
		T t;
		public Two(T t) {
			this.t = t;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + ((t == null) ? 0 : t.hashCode());
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
			Two other = (Two) obj;
			if (t == null) {
				if (other.t != null)
					return false;
			} else if (!t.equals(other.t))
				return false;
			return true;
		}
		
		@Override public String toString() {
			return ("prop " + t).trim();
		}
		@Override
		public <R, E> R accept(E env, PolyVisitor<T, C, R, E> v) {
			return v.visit(env, this);
		}
	}

	
	public static class Plus<T, C> extends Poly<T, C> {
		Poly<T, C> a, b;
		
		public Plus(Poly<T, C> a, Poly<T, C> b) {
			this.a = a;
			this.b = b;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
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
		
		
		@Override public String toString() {
			return "(" + a + " + " + b + ")";
		}
		@Override
		public <R, E> R accept(E env, PolyVisitor<T, C, R, E> v) {
			return v.visit(env, this);
		}
	}
	
	public static class Times<T, C> extends Poly<T, C> {
		Poly<T, C> a, b;
		
		public Times(Poly<T, C> a, Poly<T, C> b) {
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
			Times<?,?> other = (Times<?,?>) obj;
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
		
		@Override public String toString() {
			return "(" + a + " * " + b + ")";
		}
		
		@Override
		public <R, E> R accept(E env, PolyVisitor<T, C, R, E> v) {
			return v.visit(env, this);
		}
	}
	
	public static class Exp<T, C> extends Poly<T, C> {
		Poly<T, C> a, b;
		
		public Exp(Poly<T, C> a, Poly<T, C> b) {
			this.a = a;
			this.b = b;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
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
			Exp<?,?> other = (Exp<?,?>) obj;
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
		
		@Override public String toString() {
			return "(" + a + " ^ " + b + ")";
		}
		
		@Override
		public <R, E> R accept(E env, PolyVisitor<T, C, R, E> v) {
			return v.visit(env, this);
		}

	}
	
	
	@Override
	public abstract boolean equals(Object o);
	
	public abstract <R, E> R accept(E env, PolyVisitor<T, C, R, E> v);
	
	public int hashCode() {
		return 0;
	}
}
