package fql.decl;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import fql.Pair;
import fql.Triple;

public abstract class InstExp {
	
	public final SigExp type(Map<String, SigExp> env,
			Map<String, MapExp> ctx, Map<String, InstExp> insts) {
		return accept(new Triple<>(env, ctx, insts), new InstChecker(new LinkedList<String>()));
	}


	//TODO Const equality for instances
	public static class Const extends InstExp {
		public List<Pair<String, List<Pair<Object, Object>>>> data;
		public SigExp sig;
		
		public Const(List<Pair<String, List<Pair<Object, Object>>>> data,
				SigExp sig) {
			super();
			this.data = data;
			this.sig = sig;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((data == null) ? 0 : data.hashCode());
			result = prime * result + ((sig == null) ? 0 : sig.hashCode());
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
			Const other = (Const) obj;
			if (data == null) {
				if (other.data != null)
					return false;
			} else if (!data.equals(other.data))
				return false;
			if (sig == null) {
				if (other.sig != null)
					return false;
			} else if (!sig.equals(other.sig))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return data.toString();
		}
		

		@Override
		public <R, E> R accept(E env, InstExpVisitor<R, E> v) {
			return v.visit(env, this);
		}

		

		
	}

	public static class Var extends InstExp {
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
		public <R, E> R accept(E env, InstExpVisitor<R, E> v) {
			return v.visit(env, this);
		}

		
	}

	public static class Zero extends InstExp {

		SigExp sig;
		
		public Zero(SigExp sig) {
			super();
			this.sig = sig;
		}


		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((sig == null) ? 0 : sig.hashCode());
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
			if (sig == null) {
				if (other.sig != null)
					return false;
			} else if (!sig.equals(other.sig))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "void " + sig;
		}

		@Override
		public <R, E> R accept(E env, InstExpVisitor<R, E> v) {
			return v.visit(env, this);
		}


		
	}

	public static class One extends InstExp {
		
		SigExp sig;
		
		public One(SigExp sig) {
			super();
			this.sig = sig;
		}


		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((sig == null) ? 0 : sig.hashCode());
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
			if (sig == null) {
				if (other.sig != null)
					return false;
			} else if (!sig.equals(other.sig))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "unit " + sig;
		}

		@Override
		public <R, E> R accept(E env, InstExpVisitor<R, E> v) {
			return v.visit(env, this);
		}
		
		
	}
	
	public static class Two extends InstExp {
		
		SigExp sig;
		
		public Two(SigExp sig) {
			super();
			this.sig = sig;
		}


		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((sig == null) ? 0 : sig.hashCode());
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
			if (sig == null) {
				if (other.sig != null)
					return false;
			} else if (!sig.equals(other.sig))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "prop " + sig;
		}

		@Override
		public <R, E> R accept(E env, InstExpVisitor<R, E> v) {
			return v.visit(env, this);
		}
		
		
	}

	public static class Plus extends InstExp {
		InstExp a, b;

		public Plus(InstExp a, InstExp b) {
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
		public <R, E> R accept(E env, InstExpVisitor<R, E> v) {
			return v.visit(env, this);
		}

		
	}

	public static class Times extends InstExp {
		InstExp a, b;

		public Times(InstExp a, InstExp b) {
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
		public <R, E> R accept(E env, InstExpVisitor<R, E> v) {
			return v.visit(env, this);
		}
		
		
	}

	public static class Exp extends InstExp {
		InstExp a, b;

		public Exp(InstExp a, InstExp b) {
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
		public <R, E> R accept(E env, InstExpVisitor<R, E> v) {
			return v.visit(env, this);
		}
		
		

	}
	
	public static class Delta extends InstExp {
		
		MapExp F;
		InstExp I;
		public Delta(MapExp f, InstExp i) {
			super();
			F = f;
			I = i;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((F == null) ? 0 : F.hashCode());
			result = prime * result + ((I == null) ? 0 : I.hashCode());
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
			Delta other = (Delta) obj;
			if (F == null) {
				if (other.F != null)
					return false;
			} else if (!F.equals(other.F))
				return false;
			if (I == null) {
				if (other.I != null)
					return false;
			} else if (!I.equals(other.I))
				return false;
			return true;
		}
		@Override
		public String toString() {
			return "delta " + F + " " + I;
		}
		@Override
		public <R, E> R accept(E env, InstExpVisitor<R, E> v) {
			return v.visit(env, this);
		}

		
		
	}
	
public static class Sigma extends InstExp {
		
		MapExp F;
		InstExp I;
		public Sigma(MapExp f, InstExp i) {
			super();
			F = f;
			I = i;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((F == null) ? 0 : F.hashCode());
			result = prime * result + ((I == null) ? 0 : I.hashCode());
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
			Sigma other = (Sigma) obj;
			if (F == null) {
				if (other.F != null)
					return false;
			} else if (!F.equals(other.F))
				return false;
			if (I == null) {
				if (other.I != null)
					return false;
			} else if (!I.equals(other.I))
				return false;
			return true;
		}
		@Override
		public String toString() {
			return "sigma " + F + " " + I;
		}
		@Override
		public <R, E> R accept(E env, InstExpVisitor<R, E> v) {
			return v.visit(env, this);
		}
		
		

		
	}

public static class Pi extends InstExp {
	
	MapExp F;
	InstExp I;
	public Pi(MapExp f, InstExp i) {
		super();
		F = f;
		I = i;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((F == null) ? 0 : F.hashCode());
		result = prime * result + ((I == null) ? 0 : I.hashCode());
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
		Pi other = (Pi) obj;
		if (F == null) {
			if (other.F != null)
				return false;
		} else if (!F.equals(other.F))
			return false;
		if (I == null) {
			if (other.I != null)
				return false;
		} else if (!I.equals(other.I))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "pi " + F + " " + I;
	}
	@Override
	public <R, E> R accept(E env, InstExpVisitor<R, E> v) {
		return v.visit(env, this);
	}
	
	

	
}

public static class FullSigma extends InstExp {
	
	MapExp F;
	InstExp I;
	public FullSigma(MapExp f, InstExp i) {
		super();
		F = f;
		I = i;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((F == null) ? 0 : F.hashCode());
		result = prime * result + ((I == null) ? 0 : I.hashCode());
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
		FullSigma other = (FullSigma) obj;
		if (F == null) {
			if (other.F != null)
				return false;
		} else if (!F.equals(other.F))
			return false;
		if (I == null) {
			if (other.I != null)
				return false;
		} else if (!I.equals(other.I))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "SIGMA " + F + " " + I;
	}
	@Override
	public <R, E> R accept(E env, InstExpVisitor<R, E> v) {
		return v.visit(env, this);
	}
	
	
	
}

public static class Relationalize extends InstExp {
	
	public Relationalize(InstExp i) {
		super();
		I = i;
	}
	InstExp I;
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((I == null) ? 0 : I.hashCode());
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
		Relationalize other = (Relationalize) obj;
		if (I == null) {
			if (other.I != null)
				return false;
		} else if (!I.equals(other.I))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "relationalize " + I;
	}
	@Override
	public <R, E> R accept(E env, InstExpVisitor<R, E> v) {
		return v.visit(env, this);
	}

	
}

public static class External extends InstExp {
	
	SigExp sig;
	String name;
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((sig == null) ? 0 : sig.hashCode());
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
		External other = (External) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (sig == null) {
			if (other.sig != null)
				return false;
		} else if (!sig.equals(other.sig))
			return false;
		return true;
	}
	public External(SigExp sig, String name) {
		super();
		this.sig = sig;
		this.name = name;
	}
	
	@Override
	public String toString() {
		return "external " + name + " " + sig;
	}
	
	@Override
	public <R, E> R accept(E env, InstExpVisitor<R, E> v) {
		return v.visit(env, this);
	}
	
	
}

	@Override
	public abstract boolean equals(Object o);

	public abstract <R, E> R accept(E env, InstExpVisitor<R, E> v);

	@Override
	public abstract int hashCode();
	
	
	
	public interface InstExpVisitor<R, E> {
		public R visit (E env, Zero e) ;
		public R visit (E env, One e) ;
		public R visit (E env, Two e) ;
		public R visit (E env, Plus e) ;
		public R visit (E env, Times e) ;
		public R visit (E env, Exp e) ;
		public R visit (E env, Var e) ;
		public R visit (E env, Const e) ;
		public R visit (E env, Delta e) ;
		public R visit (E env, Sigma e) ;
		public R visit (E env, Pi e) ;
		public R visit (E env, FullSigma e) ;
		public R visit (E env, Relationalize e) ;
		public R visit (E env, External e) ;
	}

}
