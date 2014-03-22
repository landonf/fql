package fql.decl;

import java.util.Collections;
import java.util.List;

import fql.Pair;
import fql.examples.TransChecker;
import fql.parse.PrettyPrinter;

public abstract class TransExp {
	public Pair<String, String> type(FQLProgram p) {
		return accept(p, new TransChecker());
	}
	
	public static class TransCurry extends TransExp {
		
		public String inst;
		public String trans;
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((inst == null) ? 0 : inst.hashCode());
			result = prime * result + ((trans == null) ? 0 : trans.hashCode());
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
			TransCurry other = (TransCurry) obj;
			if (inst == null) {
				if (other.inst != null)
					return false;
			} else if (!inst.equals(other.inst))
				return false;
			if (trans == null) {
				if (other.trans != null)
					return false;
			} else if (!trans.equals(other.trans))
				return false;
			return true;
		}
		public TransCurry(String inst, String trans) {
			super();
			this.inst = inst;
			this.trans = trans;
		}
		public String toString() {
			return inst + ".curry " + trans;
		}
		@Override
		public <R, E> R accept(E env, TransExpVisitor<R, E> v) {
			return v.visit(env, this);
		}
	}	
	
	public static class TransEval extends TransExp {
		
		public String inst;
		
		@Override
		public <R, E> R accept(E env, TransExpVisitor<R, E> v) {
			return v.visit(env, this);
		}

		public TransEval(String inst) {
			super();
			this.inst = inst;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((inst == null) ? 0 : inst.hashCode());
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
			TransEval other = (TransEval) obj;
			if (inst == null) {
				if (other.inst != null)
					return false;
			} else if (!inst.equals(other.inst))
				return false;
			return true;
		}
		
		@Override
		public String toString() {
			return inst + ".eval";
		}
		
	}
	
	public static class Return extends TransExp {
		
		public String inst;
		
		public Return(String inst) {
			this.inst = inst;
		}

		@Override
		public <R, E> R accept(E env, TransExpVisitor<R, E> v) {
			return v.visit(env, this);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Return other = (Return) obj;
			if (inst == null) {
				if (other.inst != null)
					return false;
			} else if (!inst.equals(other.inst))
				return false;
			return true;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((inst == null) ? 0 : inst.hashCode());
			return result;
		}
		
		@Override
		public String toString() {
			return inst + ".return";
		}
		
	}
	
	public static class Coreturn extends TransExp {
		
		public String inst;
		
		public Coreturn(String inst) {
			this.inst = inst;
		}

		@Override
		public <R, E> R accept(E env, TransExpVisitor<R, E> v) {
			return v.visit(env, this);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Coreturn other = (Coreturn) obj;
			if (inst == null) {
				if (other.inst != null)
					return false;
			} else if (!inst.equals(other.inst))
				return false;
			return true;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((inst == null) ? 0 : inst.hashCode());
			return result;
		}
		
		@Override
		public String toString() {
			return inst + ".coreturn";
		}
		
	}
	
	public static class External extends TransExp {
		public String src, dst, name;
		
		@Override
		public <R, E> R accept(E env, TransExpVisitor<R, E> v) {
			return v.visit(env, this);
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
			if (dst == null) {
				if (other.dst != null)
					return false;
			} else if (!dst.equals(other.dst))
				return false;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			if (src == null) {
				if (other.src != null)
					return false;
			} else if (!src.equals(other.src))
				return false;
			return true;
		}
		public External(String src, String dst, String name) {
			super();
			this.src = src;
			this.dst = dst;
			this.name = name;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((dst == null) ? 0 : dst.hashCode());
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			result = prime * result + ((src == null) ? 0 : src.hashCode());
			return result;
		}
		@Override
		public String toString() {
			return "external " + src + " " + dst + " " + name;
		}

	}
	
	public static class Squash extends TransExp {

		public String src;
		
		@Override
		public String toString() {
			return src + ".relationalize";
		}
		
		public Squash(String src) {
			super();
			this.src = src;
		}

		@Override
		public <R, E> R accept(E env, TransExpVisitor<R, E> v) {
			return v.visit(env, this);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Squash other = (Squash) obj;
			if (src == null) {
				if (other.src != null)
					return false;
			} else if (!src.equals(other.src))
				return false;
			return true;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((src == null) ? 0 : src.hashCode());
			return result;
		}
		
	}
	
	public static class Delta extends TransExp {
		public TransExp h;
		public Delta(TransExp h, String src, String dst) {
			super();
			this.h = h;
			this.src = src;
			this.dst = dst;
		}
		public String src, dst;
		
		@Override
		public String toString() {
			return "delta " + src + " " + dst + " " + h;
		}
		
		@Override
		public <R, E> R accept(E env, TransExpVisitor<R, E> v) {
			return v.visit(env, this);
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
			if (dst == null) {
				if (other.dst != null)
					return false;
			} else if (!dst.equals(other.dst))
				return false;
			if (h == null) {
				if (other.h != null)
					return false;
			} else if (!h.equals(other.h))
				return false;
			if (src == null) {
				if (other.src != null)
					return false;
			} else if (!src.equals(other.src))
				return false;
			return true;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((dst == null) ? 0 : dst.hashCode());
			result = prime * result + ((h == null) ? 0 : h.hashCode());
			result = prime * result + ((src == null) ? 0 : src.hashCode());
			return result;
		}
	}
	
	public static class Sigma extends TransExp {
		public TransExp h;
		public String src, dst;
		
		public Sigma(TransExp h, String src, String dst) {
			super();
			this.h = h;
			this.src = src;
			this.dst = dst;
		}

		@Override
		public String toString() {
			return "sigma " + src + " " + dst + " " + h;
		}
		
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((dst == null) ? 0 : dst.hashCode());
			result = prime * result + ((h == null) ? 0 : h.hashCode());
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
			Sigma other = (Sigma) obj;
			if (dst == null) {
				if (other.dst != null)
					return false;
			} else if (!dst.equals(other.dst))
				return false;
			if (h == null) {
				if (other.h != null)
					return false;
			} else if (!h.equals(other.h))
				return false;
			if (src == null) {
				if (other.src != null)
					return false;
			} else if (!src.equals(other.src))
				return false;
			return true;
		}

		@Override
		public <R, E> R accept(E env, TransExpVisitor<R, E> v) {
			return v.visit(env, this);
		}
	}
	
	public static class FullSigma extends TransExp {
		public String h;
		public String src, dst;
		
		@Override
		public String toString() {
			return "SIGMA " + src + " " + dst + " " + h;
		}
		
		public FullSigma(String h, String src, String dst) {
			super();
			this.h = h;
			this.src = src;
			this.dst = dst;
		}

		@Override
		public <R, E> R accept(E env, TransExpVisitor<R, E> v) {
			return v.visit(env, this);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((dst == null) ? 0 : dst.hashCode());
			result = prime * result + ((h == null) ? 0 : h.hashCode());
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
			FullSigma other = (FullSigma) obj;
			if (dst == null) {
				if (other.dst != null)
					return false;
			} else if (!dst.equals(other.dst))
				return false;
			if (h == null) {
				if (other.h != null)
					return false;
			} else if (!h.equals(other.h))
				return false;
			if (src == null) {
				if (other.src != null)
					return false;
			} else if (!src.equals(other.src))
				return false;
			return true;
		}

		

	}
	
	public static class Pi extends TransExp {
		public TransExp h;
		public String src, dst;
		
		@Override
		public String toString() {
			return "pi " + src + " " + dst + " " + h;
		}
		

		public Pi(TransExp h, String src, String dst) {
			super();
			this.h = h;
			this.src = src;
			this.dst = dst;
		}


		@Override
		public <R, E> R accept(E env, TransExpVisitor<R, E> v) {
			return v.visit(env, this);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((dst == null) ? 0 : dst.hashCode());
			result = prime * result + ((h == null) ? 0 : h.hashCode());
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
			Pi other = (Pi) obj;
			if (dst == null) {
				if (other.dst != null)
					return false;
			} else if (!dst.equals(other.dst))
				return false;
			if (h == null) {
				if (other.h != null)
					return false;
			} else if (!h.equals(other.h))
				return false;
			if (src == null) {
				if (other.src != null)
					return false;
			} else if (!src.equals(other.src))
				return false;
			return true;
		}
	}
	
	public static class Relationalize extends TransExp {
		public TransExp h;
		public String src, dst;

		@Override
		public String toString() {
			return "relationalize " + src + " " + dst + " " + h;
		}
		

		public Relationalize(TransExp h, String src, String dst) {
			super();
			this.h = h;
			this.src = src;
			this.dst = dst;
		}


		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((dst == null) ? 0 : dst.hashCode());
			result = prime * result + ((h == null) ? 0 : h.hashCode());
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
			Relationalize other = (Relationalize) obj;
			if (dst == null) {
				if (other.dst != null)
					return false;
			} else if (!dst.equals(other.dst))
				return false;
			if (h == null) {
				if (other.h != null)
					return false;
			} else if (!h.equals(other.h))
				return false;
			if (src == null) {
				if (other.src != null)
					return false;
			} else if (!src.equals(other.src))
				return false;
			return true;
		}

		@Override
		public <R, E> R accept(E env, TransExpVisitor<R, E> v) {
			return v.visit(env, this);
		}

	}
	

	
/*
	public Const toConst(Map<String, SigExp> env, Map<String, TransExp> ctx) {
		return accept(new Pair<>(env, ctx), new SigOps());
	}
	*/
	/*
	public Transform toMap(Map<String, SigExp> env, Map<String, TransExp> ctx) {
		Const e = toConst(env, ctx);
		try {
			return new Mapping(e.src.toSig(env), e.dst.toSig(env), e.objs, e.attrs,
					e.arrows);
		} catch (FQLException fe) {
			fe.printStackTrace();
			throw new RuntimeException(fe.getLocalizedMessage());
		}
	}
	*/

	public abstract <R, E> R accept(E env, TransExpVisitor<R, E> v);

	public abstract boolean equals(Object o);
/*
	public final Pair<SigExp, SigExp> type(Map<String, SigExp> env,
			Map<String, TransExp> ctx) {
		return accept(new Pair<>(env, ctx), new TransExpChecker(
				new LinkedList<String>()));
	}
	*/

	public static class Id extends TransExp {
		public String t;

		public Id(String t) {
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
			Id other = (Id) obj;
			if (t == null) {
				if (other.t != null)
					return false;
			} else if (!t.equals(other.t))
				return false;
			return true;
		}

		@Override
		public <R, E> R accept(E env, TransExpVisitor<R, E> v) {
			return v.visit(env, this);
		}

		@Override
		public String toString() {
			return "id " + t;
		}

	}
/*
	public static class Dist2 extends TransExp {
		@Override
		public <R, E> R accept(E env, TransExpVisitor<R, E> v) {
			return v.visit(env, this);
		}

		public SigExp a, b, c;

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
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
			Dist2 other = (Dist2) obj;
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

		public Dist2(SigExp a, SigExp b, SigExp c) {
			this.a = a;
			this.b = b;
			this.c = c;
		}

		@Override
		public String toString() {
			return "dist2";
		}

	}

	public static class Dist1 extends TransExp {
		@Override
		public <R, E> R accept(E env, TransExpVisitor<R, E> v) {
			return v.visit(env, this);
		}

		public SigExp a, b, c;

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
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
			Dist1 other = (Dist1) obj;
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

		public Dist1(SigExp a, SigExp b, SigExp c) {
			this.a = a;
			this.b = b;
			this.c = c;
		}

		@Override
		public String toString() {
			return "dist1";
		}

	}
*/
	public static class Const extends TransExp {
		public List<Pair<String, List<Pair<Object, Object>>>> objs;
		public String src, dst;

		public Const(List<Pair<String, List<Pair<Object, Object>>>> objs,
				 String src, String dst) {
			this.objs = objs;
			this.src = src;
			this.dst = dst;
			Collections.sort(this.objs);
		}

	
		@Override
		public String toString() {
			
			String nm = "\n nodes\n";
			boolean b = false;
			for (Pair<String, List<Pair<Object, Object>>> k : objs) {
				if (b) {
					nm += ", \n";
				}
				b = true;
				
				boolean c = false;
				nm += "  " + k.first + " -> " + "{";

				for (Pair<Object, Object> k0 : k.second) {
					if (c) {
						nm += ", ";
					}
					c = true;
					nm += "(" + PrettyPrinter.q(k0.first) + ", " + PrettyPrinter.q(k0.second) + ")";
				}
				nm += "}";
			}
			nm = nm.trim();
			nm += ";\n";

			return "{\n " + nm + "}";
		}
		

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((dst == null) ? 0 : dst.hashCode());
			result = prime * result + ((objs == null) ? 0 : objs.hashCode());
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
			Const other = (Const) obj;
			if (dst == null) {
				if (other.dst != null)
					return false;
			} else if (!dst.equals(other.dst))
				return false;
			if (objs == null) {
				if (other.objs != null)
					return false;
			} else if (!objs.equals(other.objs))
				return false;
			if (src == null) {
				if (other.src != null)
					return false;
			} else if (!src.equals(other.src))
				return false;
			return true;
		}

		@Override
		public <R, E> R accept(E env, TransExpVisitor<R, E> v) {
			return v.visit(env, this);
		}

	}

	public static class Var extends TransExp {
		public String v;

		public Var(String v) {
			this.v = v;
			if (v.contains(" ")) {
				throw new RuntimeException();
			}
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
		public <R, E> R accept(E env, TransExpVisitor<R, E> v) {
			return v.visit(env, this);
		}

	}

	public static class TT extends TransExp {
		public String obj;
		public String tgt;

		public TT(String obj, String tgt) {
			this.tgt = tgt;
			this.obj = obj;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((obj == null) ? 0 : obj.hashCode());
			result = prime * result + ((tgt == null) ? 0 : tgt.hashCode());
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
			TT other = (TT) obj;
			if (this.obj == null) {
				if (other.obj != null)
					return false;
			} else if (!this.obj.equals(other.obj))
				return false;
			if (tgt == null) {
				if (other.tgt != null)
					return false;
			} else if (!tgt.equals(other.tgt))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return obj + ".unit " + tgt;
		}

		@Override
		public <R, E> R accept(E env, TransExpVisitor<R, E> v) {
			return v.visit(env, this);
		}

	}

	public static class FF extends TransExp {
		public String tgt, obj;

		public FF(String obj, String tgt) {
			this.tgt = tgt;
			this.obj = obj;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((obj == null) ? 0 : obj.hashCode());
			result = prime * result + ((tgt == null) ? 0 : tgt.hashCode());
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
			FF other = (FF) obj;
			if (this.obj == null) {
				if (other.obj != null)
					return false;
			} else if (!this.obj.equals(other.obj))
				return false;
			if (tgt == null) {
				if (other.tgt != null)
					return false;
			} else if (!tgt.equals(other.tgt))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return obj + ".void " + tgt;
		}

		@Override
		public <R, E> R accept(E env, TransExpVisitor<R, E> v) {
			return v.visit(env, this);
		}

	}

	public static class Fst extends TransExp {
		public String obj;

		public Fst(String obj) {
			this.obj = obj;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((obj == null) ? 0 : obj.hashCode());
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
			Fst other = (Fst) obj;
			if (this.obj == null) {
				if (other.obj != null)
					return false;
			} else if (!this.obj.equals(other.obj))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return obj + ".fst";
		}

		@Override
		public <R, E> R accept(E env, TransExpVisitor<R, E> v) {
			return v.visit(env, this);
		}

	}

	public static class Snd extends TransExp {
		public String obj;

		public Snd(String obj) {
			this.obj = obj;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((obj == null) ? 0 : obj.hashCode());
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
			Snd other = (Snd) obj;
			if (this.obj == null) {
				if (other.obj != null)
					return false;
			} else if (!this.obj.equals(other.obj))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return obj + ".snd";
		}

		@Override
		public <R, E> R accept(E env, TransExpVisitor<R, E> v) {
			return v.visit(env, this);
		}

	}

	public static class Inl extends TransExp {
		public String obj;

		public Inl(String obj) {
			this.obj = obj;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((obj == null) ? 0 : obj.hashCode());
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
			Inl other = (Inl) obj;
			if (this.obj == null) {
				if (other.obj != null)
					return false;
			} else if (!this.obj.equals(other.obj))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return obj + ".inl ";
		}

		@Override
		public <R, E> R accept(E env, TransExpVisitor<R, E> v) {
			return v.visit(env, this);
		}

	}

	public static class Inr extends TransExp {
		public String obj;

		public Inr(String obj) {
			this.obj = obj;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((obj == null) ? 0 : obj.hashCode());
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
			Inr other = (Inr) obj;
			if (this.obj == null) {
				if (other.obj != null)
					return false;
			} else if (!this.obj.equals(other.obj))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return obj + ".inr";
		}

		@Override
		public <R, E> R accept(E env, TransExpVisitor<R, E> v) {
			return v.visit(env, this);
		}

	}
/*
	public static class Apply extends TransExp {
		SigExp s, t;

		public Apply(SigExp s, SigExp t) {
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
			Apply other = (Apply) obj;
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

		@Override
		public String toString() {
			return "apply " + s + " " + t;
		}

		@Override
		public <R, E> R accept(E env, TransExpVisitor<R, E> v) {
			return v.visit(env, this);
		}

	}

	public static class Curry extends TransExp {
		TransExp f;

		public Curry(TransExp f) {
			this.f = f;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
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
			Curry other = (Curry) obj;
			if (f == null) {
				if (other.f != null)
					return false;
			} else if (!f.equals(other.f))
				return false;
			return true;
		}

		public String toString() {
			return "curry " + f;
		}

		@Override
		public <R, E> R accept(E env, TransExpVisitor<R, E> v) {
			return v.visit(env, this);
		}

	}
*/
	public static class Prod extends TransExp {
		public String obj;
		public TransExp l, r;

		public Prod(String obj, TransExp l, TransExp r) {
			this.l = l;
			this.r = r;
			this.obj = obj;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((l == null) ? 0 : l.hashCode());
			result = prime * result + ((obj == null) ? 0 : obj.hashCode());
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
			Prod other = (Prod) obj;
			if (l == null) {
				if (other.l != null)
					return false;
			} else if (!l.equals(other.l))
				return false;
			if (this.obj == null) {
				if (other.obj != null)
					return false;
			} else if (!this.obj.equals(other.obj))
				return false;
			if (r == null) {
				if (other.r != null)
					return false;
			} else if (!r.equals(other.r))
				return false;
			return true;
		}

		public String toString() {
			return obj + ".(" + l + " * " + r + ")";
		}

		@Override
		public <R, E> R accept(E env, TransExpVisitor<R, E> v) {
			return v.visit(env, this);
		}

	}

	public static class Comp extends TransExp {
		public TransExp l, r;

		public Comp(TransExp TransExp, TransExp r) {
			this.l = TransExp;
			this.r = r;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
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
			Comp other = (Comp) obj;
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
		public <R, E> R accept(E env, TransExpVisitor<R, E> v) {
			return v.visit(env, this);
		}

	}

	public static class Case extends TransExp {
		public String obj;
		public TransExp l, r;

		public Case(String obj, TransExp l, TransExp r) {
			this.l = l;
			this.r = r;
			this.obj = obj;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((l == null) ? 0 : l.hashCode());
			result = prime * result + ((obj == null) ? 0 : obj.hashCode());
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
			Case other = (Case) obj;
			if (l == null) {
				if (other.l != null)
					return false;
			} else if (!l.equals(other.l))
				return false;
			if (this.obj == null) {
				if (other.obj != null)
					return false;
			} else if (!this.obj.equals(other.obj))
				return false;
			if (r == null) {
				if (other.r != null)
					return false;
			} else if (!r.equals(other.r))
				return false;
			return true;
		}

		public String toString() {
			return obj + ".(" + l + " + " + r + ")";
		}

		@Override
		public <R, E> R accept(E env, TransExpVisitor<R, E> v) {
			return v.visit(env, this);
		}

	}

	public abstract int hashCode();

	public interface TransExpVisitor<R, E> {

		public R visit(E env, Id e);
		public R visit(E env, Comp e);
		public R visit(E env, Var e);
		public R visit(E env, Const e);
		public R visit(E env, TT e);
		public R visit(E env, FF e);
		public R visit(E env, Fst e);
		public R visit(E env, Snd e);
		public R visit(E env, Inl e);
		public R visit(E env, Inr e);
		public R visit(E env, Delta e);
		public R visit(E env, Sigma e);
		public R visit(E env, FullSigma e);
		public R visit(E env, Pi e);
		public R visit(E env, Relationalize e);
		public R visit(E env, Squash e);
		public R visit(E env, TransCurry e);
		public R visit(E env, TransEval e);
		public R visit(E env, Case e);
		public R visit(E env, Prod e);
		public R visit(E env, External e);
		public R visit(E env, Return e);
		public R visit(E env, Coreturn e);
		
	}


}
