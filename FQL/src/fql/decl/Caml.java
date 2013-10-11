package fql.decl;


public abstract class Caml<T, C, D> {
	
	public abstract <R,E> R accept(E env, CamlVisitor<T, C, D, R, E> v);
	
	public static class Id<T,C,D> extends Caml<T,C,D> {
		Poly<T,C> t;
		public Id(Poly<T,C> t) {
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
			Id<?,?, ?> other = (Id<?,?, ?>) obj;
			if (t == null) {
				if (other.t != null)
					return false;
			} else if (!t.equals(other.t))
				return false;
			return true;
		}
		@Override
		public <R, E> R accept(E env, CamlVisitor<T, C, D, R, E> v) {
			return v.visit(env, this);
		}
		@Override
		public String toString() {
			return "id";
		}
	}

	//also id, composition
	
	public static class Dist2<T, C, D> extends Caml<T, C, D> {
		@Override
		public <R, E> R accept(E env, CamlVisitor<T, C, D, R, E> v) {
			return v.visit(env, this);
		}

		public Poly<T, C> a, b, c;
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + ((a == null) ? 0 : a.hashCode());
			result = prime * result + ((b == null) ? 0 : b.hashCode());
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
			Dist2<?, ?, ?> other = (Dist2<?, ?, ?>) obj;
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
			if (c == null) {
				if (other.c != null)
					return false;
			} else if (!c.equals(other.c))
				return false;
			return true;
		}
		public Dist2(Poly<T, C> a, Poly<T, C> b, Poly<T, C> c) {
			this.a = a; this.b = b; this.c = c;
		}
		@Override
		public String toString() {
			return "dist2";
		}
	}
	
	public static class Dist1<T, C, D> extends Caml<T, C, D> {
		@Override
		public <R, E> R accept(E env, CamlVisitor<T, C, D, R, E> v) {
			return v.visit(env, this);
		}

		public Poly<T, C> a, b, c;
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + ((a == null) ? 0 : a.hashCode());
			result = prime * result + ((b == null) ? 0 : b.hashCode());
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
			Dist1<?, ?, ?> other = (Dist1<?, ?, ?>) obj;
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
			if (c == null) {
				if (other.c != null)
					return false;
			} else if (!c.equals(other.c))
				return false;
			return true;
		}
		public Dist1(Poly<T, C> a, Poly<T, C> b, Poly<T, C> c) {
			this.a = a; this.b = b; this.c = c;
		}
		@Override
		public String toString() {
			return "dist1";
		}
	}
	
	public static class Const<T, C, D> extends Caml<T, C, D> {
		D d;
		Poly<T, C> src, dst;
		public Const(Poly<T, C> src, Poly<T, C> dst, D d) {
			this.src = src;
			this.dst = dst;
			this.d = d;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + ((d == null) ? 0 : d.hashCode());
			result = prime * result + ((dst == null) ? 0 : dst.hashCode());
			result = prime * result + ((src == null) ? 0 : src.hashCode());
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
			Const<?, ?, ?> other = (Const<?, ?, ?>) obj;
			if (d == null) {
				if (other.d != null)
					return false;
			} else if (!d.equals(other.d))
				return false;
			if (dst == null) {
				if (other.dst != null)
					return false;
			} else if (!dst.equals(other.dst))
				return false;
			if (src == null) {
				if (other.src != null)
					return false;
			} else if (!src.equals(other.src))
				return false;
			return true;
		}
		@Override
		public String toString() {
			return "Const [d=" + d + ", src=" + src + ", dst=" + dst + "]";
		}
		@Override
		public <R, E> R accept(E env, CamlVisitor<T, C, D, R, E> v) {
			return v.visit(env, this);
		}
	}
	
	public static class Var<T, C, D> extends Caml<T, C, D> {
		String v;
		//Poly t;
		public Var(String v) {
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
			Var<?, ?, ?> other = (Var<?, ?, ?>) obj;
			if (v == null) {
				if (other.v != null)
					return false;
			} else if (!v.equals(other.v))
				return false;
			return true;
		}
		@Override
		public <R, E> R accept(E env, CamlVisitor<T, C, D, R, E> v) {
			return v.visit(env, this);
		}
			
	}
	
	public static class TT<T, C,D> extends Caml<T, C,D> {
		Poly<T, C> t;
		public TT(Poly<T, C> t) {
			this.t = t;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
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
			TT<?,?, ?> other = (TT<?,?, ?>) obj;
			if (t == null) {
				if (other.t != null)
					return false;
			} else if (!t.equals(other.t))
				return false;
			return true;
		}
		@Override public String toString() {
			return "tt";
		}
		@Override
		public <R, E> R accept(E env, CamlVisitor<T, C,D,R, E> v) {
			return v.visit(env, this);
		}

	}
	
	public static class FF<T,C,D> extends Caml<T,C,D> {
		Poly<T,C> t;
		public FF(Poly<T,C> t) {
			this.t = t;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
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
			FF<?,?, ?> other = (FF<?,?, ?>) obj;
			if (t == null) {
				if (other.t != null)
					return false;
			} else if (!t.equals(other.t))
				return false;
			return true;
		}
		@Override public String toString() {
			return "ff";
		}
		@Override
		public <R, E> R accept(E env, CamlVisitor<T,C, D, R, E> v) {
			return v.visit(env, this);
		}

	}
	
	public static class Eq<T,C, D> extends Caml<T,C, D> {
		Poly<T,C> t;
		public Eq(Poly<T,C> t) {
			this.t = t;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
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
			Eq<?,?, ?> other = (Eq<?,?, ?>) obj;
			if (t == null) {
				if (other.t != null)
					return false;
			} else if (!t.equals(other.t))
				return false;
			return true;
		}
		@Override public String toString() {
			return "eq";
		}
		@Override
		public <R, E> R accept(E env, CamlVisitor<T,C, D, R, E> v) {
			return v.visit(env, this);
		}

	}
	
	public static class Fst<T,C,D> extends Caml<T,C,D> {
		Poly<T,C> s, t;
		public Fst(Poly<T,C> s, Poly<T,C> t) {
			this.s = s;
			this.t = t;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((s == null) ? 0 : s.hashCode());
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
			Fst<?,?, ?> other = (Fst<?,?, ?>) obj;
			if (s == null) {
				if (other.s != null)
					return false;
			} else if (!s.equals(other.s))
				return false;
			if (t == null) {
				if (other.t != null)
					return false;
			} else if (!t.equals(other.t))
				return false;
			return true;
		}
		@Override public String toString() {
			return "fst";
		}
		@Override
		public <R, E> R accept(E env, CamlVisitor<T,C, D, R, E> v) {
			return v.visit(env, this);
		}

	}
	
	public static class Snd<T,C,D> extends Caml<T,C, D> {
		Poly<T,C> s, t;
		public Snd(Poly<T,C> s, Poly<T,C> t) {
			this.s = s;
			this.t = t;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((s == null) ? 0 : s.hashCode());
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
			Snd<?,?, ?> other = (Snd<?,?, ?>) obj;
			if (s == null) {
				if (other.s != null)
					return false;
			} else if (!s.equals(other.s))
				return false;
			if (t == null) {
				if (other.t != null)
					return false;
			} else if (!t.equals(other.t))
				return false;
			return true;
		}
		@Override public String toString() {
			return "snd";
		}
		@Override
		public <R, E> R accept(E env, CamlVisitor<T,C, D, R, E> v) {
			return v.visit(env, this);
		}

	}
	
	public static class Inl<T,C, D> extends Caml<T,C, D> {
		Poly<T,C> s, t;
		public Inl(Poly<T,C> s, Poly<T,C> t) {
			this.s = s;
			this.t = t;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((s == null) ? 0 : s.hashCode());
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
			Inl<?,?, ?> other = (Inl<?,?, ?>) obj;
			if (s == null) {
				if (other.s != null)
					return false;
			} else if (!s.equals(other.s))
				return false;
			if (t == null) {
				if (other.t != null)
					return false;
			} else if (!t.equals(other.t))
				return false;
			return true;
		}
		@Override public String toString() {
			return "inl";
		}
		@Override
		public <R, E> R accept(E env, CamlVisitor<T,C, D, R, E> v) {
			return v.visit(env, this);
		}

	}
	
	public static class Inr<T,C, D> extends Caml<T,C, D> {
		Poly<T,C> s, t;
		public Inr(Poly<T,C> s, Poly<T,C> t) {
			this.s = s;
			this.t = t;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((s == null) ? 0 : s.hashCode());
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
			Inr<?,?, ?> other = (Inr<?,?, ?>) obj;
			if (s == null) {
				if (other.s != null)
					return false;
			} else if (!s.equals(other.s))
				return false;
			if (t == null) {
				if (other.t != null)
					return false;
			} else if (!t.equals(other.t))
				return false;
			return true;
		}
		@Override public String toString() {
			return "inr";
		}
		@Override
		public <R, E> R accept(E env, CamlVisitor<T,C, D, R, E> v) {
			return v.visit(env, this);
		}

	}
	
	public static class Apply<T,C, D> extends Caml<T,C, D> {
		Poly<T,C> s, t;
		public Apply(Poly<T,C> s, Poly<T,C> t) {
			this.s = s;
			this.t = t;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((s == null) ? 0 : s.hashCode());
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
			Apply<?,?, ?> other = (Apply<?,?, ?>) obj;
			if (s == null) {
				if (other.s != null)
					return false;
			} else if (!s.equals(other.s))
				return false;
			if (t == null) {
				if (other.t != null)
					return false;
			} else if (!t.equals(other.t))
				return false;
			return true;
		}
		@Override public String toString() {
			return "apply";
		}
		@Override
		public <R, E> R accept(E env, CamlVisitor<T,C, D, R, E> v) {
			return v.visit(env, this);
		}

	}
	
	public static class Curry<T,C, D> extends Caml<T,C, D> {
		Caml<T,C, D> f;
		public Curry(Caml<T,C, D> f) {
			this.f = f;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + ((f == null) ? 0 : f.hashCode());
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
			Curry<?,?, ?> other = (Curry<?,?, ?>) obj;
			if (f == null) {
				if (other.f != null)
					return false;
			} else if (!f.equals(other.f))
				return false;
			return true;
		}
		public String toString() {
			return "(curry " + f + ")";
		}
		@Override
		public <R, E> R accept(E env, CamlVisitor<T,C, D, R, E> v) {
			return v.visit(env, this);
		}

	}
	
	public static class Prod<T,C, D> extends Caml<T,C, D> {
		Caml<T,C, D> l, r;
		public Prod(Caml<T, C, D> l, Caml<T, C, D> r) {
			this.l = l;
			this.r = r;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + ((l == null) ? 0 : l.hashCode());
			result = prime * result + ((r == null) ? 0 : r.hashCode());
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
			Prod<?,?, ?> other = (Prod<?,?, ?>) obj;
			if (l == null) {
				if (other.l != null)
					return false;
			} else if (!l.equals(other.l))
				return false;
			if (r == null) {
				if (other.r != null)
					return false;
			} else if (!r.equals(other.r))
				return false;
			return true;
		}
		public String toString() {
			return "(" + l + " , " + r + ")";
		}
		@Override
		public <R, E> R accept(E env, CamlVisitor<T,C, D, R, E> v) {
			return v.visit(env, this);
		}

	}
	
	public static class Comp<T,C, D> extends Caml<T,C, D> {
		Caml<T,C, D> l, r;
		public Comp(Caml<T,C, D> caml, Caml<T,C, D> r) {
			this.l = caml;
			this.r = r;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + ((l == null) ? 0 : l.hashCode());
			result = prime * result + ((r == null) ? 0 : r.hashCode());
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
			Comp<?,?, ?> other = (Comp<?,?, ?>) obj;
			if (l == null) {
				if (other.l != null)
					return false;
			} else if (!l.equals(other.l))
				return false;
			if (r == null) {
				if (other.r != null)
					return false;
			} else if (!r.equals(other.r))
				return false;
			return true;
		}
		public String toString() {
			return "(" + l + " then " + r + ")";
		}
		@Override
		public <R, E> R accept(E env, CamlVisitor<T,C, D, R, E> v) {
			return v.visit(env, this);
		}

	}

	
	public static class Case<T,C, D> extends Caml<T,C, D> {
		Caml<T,C, D> l, r;
		public Case(Caml<T,C, D> l, Caml<T,C, D> r) {
			this.l = l;
			this.r = r;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + ((l == null) ? 0 : l.hashCode());
			result = prime * result + ((r == null) ? 0 : r.hashCode());
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
			Prod<?,?, ?> other = (Prod<?,?, ?>) obj;
			if (l == null) {
				if (other.l != null)
					return false;
			} else if (!l.equals(other.l))
				return false;
			if (r == null) {
				if (other.r != null)
					return false;
			} else if (!r.equals(other.r))
				return false;
			return true;
		}
		public String toString() {
			return "(" + l + " | " + r + ")";
		}
		@Override
		public <R, E> R accept(E env, CamlVisitor<T,C, D, R, E> v) {
			return v.visit(env, this);
		}

	}
	
	@Override 
	public int hashCode() {
		return 0;
	}
}
