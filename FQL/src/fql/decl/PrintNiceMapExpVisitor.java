package fql.decl;

import fql.decl.MapExp.Apply;
import fql.decl.MapExp.Case;
import fql.decl.MapExp.Comp;
import fql.decl.MapExp.Const;
import fql.decl.MapExp.Curry;
import fql.decl.MapExp.Dist1;
import fql.decl.MapExp.Dist2;
import fql.decl.MapExp.FF;
import fql.decl.MapExp.Fst;
import fql.decl.MapExp.Id;
import fql.decl.MapExp.Inl;
import fql.decl.MapExp.Inr;
import fql.decl.MapExp.Iso;
import fql.decl.MapExp.MapExpVisitor;
import fql.decl.MapExp.Opposite;
import fql.decl.MapExp.Prod;
import fql.decl.MapExp.Snd;
import fql.decl.MapExp.Sub;
import fql.decl.MapExp.TT;
import fql.decl.MapExp.Var;

public class PrintNiceMapExpVisitor implements MapExpVisitor<String, FQLProgram> {

	@Override
	public String visit(FQLProgram env, Id e) {
		return "id " + e.t.unresolve(env.sigs);
	}

	@Override
	public String visit(FQLProgram env, Comp e) {
		return "(" + e.l.accept(env, this) + " then " + e.r.accept(env, this) + ")";
	}

	@Override
	public String visit(FQLProgram env, Dist1 e) {
		throw new RuntimeException();
	}

	@Override
	public String visit(FQLProgram env, Dist2 e) {
		throw new RuntimeException();
	}

	@Override
	public String visit(FQLProgram env, Var e) {
		return e.v;
	}

	@Override
	public String visit(FQLProgram env, Const e) {
		return e.toString() + " : " + e.src.unresolve(env.sigs) + " -> " + e.dst.unresolve(env.sigs);
	}

	@Override
	public String visit(FQLProgram env, TT e) {
		return "unit " + e.t.unresolve(env.sigs);
	}

	@Override
	public String visit(FQLProgram env, FF e) {
		return "void " + e.t.unresolve(env.sigs);
	}

	@Override
	public String visit(FQLProgram env, Fst e) {
		return "fst " + e.s.unresolve(env.sigs) + " " + e.t.unresolve(env.sigs);
	}

	@Override
	public String visit(FQLProgram env, Snd e) {
		return "snd " + e.s.unresolve(env.sigs) + " " + e.t.unresolve(env.sigs);
	}

	@Override
	public String visit(FQLProgram env, Inl e) {
		return "inl " + e.s.unresolve(env.sigs) + " " + e.t.unresolve(env.sigs);
	}

	@Override
	public String visit(FQLProgram env, Inr e) {
		return "inr " + e.s.unresolve(env.sigs) + " " + e.t.unresolve(env.sigs);
	}

	@Override
	public String visit(FQLProgram env, Apply e) {
		return "eval " + e.s.unresolve(env.sigs) + " " + e.t.unresolve(env.sigs);
	}

	@Override
	public String visit(FQLProgram env, Curry e) {
		return "curry " + e.f.accept(env, this);
	}

	@Override
	public String visit(FQLProgram env, Case e) {
		return "(" + e.l.accept(env, this) + " + " + e.r.accept(env, this) + ")";
	}

	@Override
	public String visit(FQLProgram env, Prod e) {
		return "(" + e.l.accept(env, this) + " * " + e.r.accept(env, this) + ")";
	}

	@Override
	public String visit(FQLProgram env, Sub e) {
		return "subschema " + e.s.unresolve(env.sigs) + " " + e.t.unresolve(env.sigs);
	}

	@Override
	public String visit(FQLProgram env, Opposite e) {
		return "opposite " + e.e.accept(env, this);
	}

	@Override
	public String visit(FQLProgram env, Iso e) {
		String x = e.lToR ? "1" : "2";
		return "iso" + x + " " + e.l.unresolve(env.sigs) + " " + e.r.unresolve(env.sigs);
	}

}
