package fql.decl;

import java.util.List;

import fql.Pair;
import fql.Unit;
import fql.decl.FullQueryExp.Comp;
import fql.decl.FullQueryExp.Delta;
import fql.decl.FullQueryExp.FullQueryExpVisitor;
import fql.decl.FullQueryExp.Match;
import fql.decl.FullQueryExp.Pi;
import fql.decl.FullQueryExp.Sigma;
import fql.decl.FullQueryExp.Var;
import fql.decl.InstExp.Eval;
import fql.decl.InstExp.Exp;
import fql.decl.InstExp.FullEval;
import fql.decl.InstExp.InstExpVisitor;
import fql.decl.InstExp.One;
import fql.decl.InstExp.Plus;
import fql.decl.InstExp.Times;
import fql.decl.InstExp.Two;
import fql.decl.InstExp.Zero;
import fql.decl.MapExp.Apply;
import fql.decl.MapExp.Curry;
import fql.decl.MapExp.Dist1;
import fql.decl.MapExp.Dist2;
import fql.decl.MapExp.MapExpVisitor;
import fql.decl.MapExp.Opposite;
import fql.decl.MapExp.Sub;
import fql.decl.QueryExp.Const;
import fql.decl.QueryExp.QueryExpVisitor;
import fql.decl.TransExp.Case;
import fql.decl.TransExp.External;
import fql.decl.TransExp.FF;
import fql.decl.TransExp.Fst;
import fql.decl.TransExp.FullSigma;
import fql.decl.TransExp.Id;
import fql.decl.TransExp.Inl;
import fql.decl.TransExp.Inr;
import fql.decl.TransExp.Prod;
import fql.decl.TransExp.Relationalize;
import fql.decl.TransExp.Snd;
import fql.decl.TransExp.Squash;
import fql.decl.TransExp.TT;
import fql.decl.TransExp.TransExpVisitor;

public class TypeInference implements
		MapExpVisitor<List<Pair<String, Pair<SigExp, SigExp>>>, Unit>,
		InstExpVisitor<List<Pair<String, Pair<SigExp, SigExp>>>, Unit>,
		TransExpVisitor<List<Pair<String, Pair<SigExp, SigExp>>>, Unit>,
		QueryExpVisitor<List<Pair<String, Pair<SigExp, SigExp>>>, Unit>,
		FullQueryExpVisitor<List<Pair<String, Pair<SigExp, SigExp>>>, Unit>
{

	FQLProgram prog;
	
	//TODO check for circularity and presence as separate visitor
	
	public TypeInference(FQLProgram prog) {
		super();
		this.prog = prog;
	}

	@Override
	public List<Pair<String, Pair<SigExp, SigExp>>> visit(Unit env, Comp e) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Pair<String, Pair<SigExp, SigExp>>> visit(Unit env, Var e) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Pair<String, Pair<SigExp, SigExp>>> visit(Unit env, Match e) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Pair<String, Pair<SigExp, SigExp>>> visit(Unit env, Delta e) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Pair<String, Pair<SigExp, SigExp>>> visit(Unit env, Sigma e) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Pair<String, Pair<SigExp, SigExp>>> visit(Unit env, Pi e) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Pair<String, Pair<SigExp, SigExp>>> visit(Unit env, Const e) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Pair<String, Pair<SigExp, SigExp>>> visit(Unit env,
			fql.decl.QueryExp.Comp e) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Pair<String, Pair<SigExp, SigExp>>> visit(Unit env,
			fql.decl.QueryExp.Var e) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Pair<String, Pair<SigExp, SigExp>>> visit(Unit env, Id e) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Pair<String, Pair<SigExp, SigExp>>> visit(Unit env,
			fql.decl.TransExp.Comp e) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Pair<String, Pair<SigExp, SigExp>>> visit(Unit env,
			fql.decl.TransExp.Var e) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Pair<String, Pair<SigExp, SigExp>>> visit(Unit env,
			fql.decl.TransExp.Const e) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Pair<String, Pair<SigExp, SigExp>>> visit(Unit env, TT e) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Pair<String, Pair<SigExp, SigExp>>> visit(Unit env, FF e) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Pair<String, Pair<SigExp, SigExp>>> visit(Unit env, Fst e) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Pair<String, Pair<SigExp, SigExp>>> visit(Unit env, Snd e) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Pair<String, Pair<SigExp, SigExp>>> visit(Unit env, Inl e) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Pair<String, Pair<SigExp, SigExp>>> visit(Unit env, Inr e) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Pair<String, Pair<SigExp, SigExp>>> visit(Unit env,
			fql.decl.TransExp.Delta e) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Pair<String, Pair<SigExp, SigExp>>> visit(Unit env,
			fql.decl.TransExp.Sigma e) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Pair<String, Pair<SigExp, SigExp>>> visit(Unit env, FullSigma e) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Pair<String, Pair<SigExp, SigExp>>> visit(Unit env,
			fql.decl.TransExp.Pi e) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Pair<String, Pair<SigExp, SigExp>>> visit(Unit env,
			Relationalize e) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Pair<String, Pair<SigExp, SigExp>>> visit(Unit env, Squash e) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Pair<String, Pair<SigExp, SigExp>>> visit(Unit env, Case e) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Pair<String, Pair<SigExp, SigExp>>> visit(Unit env, Prod e) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Pair<String, Pair<SigExp, SigExp>>> visit(Unit env, External e) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Pair<String, Pair<SigExp, SigExp>>> visit(Unit env, Zero e) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Pair<String, Pair<SigExp, SigExp>>> visit(Unit env, One e) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Pair<String, Pair<SigExp, SigExp>>> visit(Unit env, Two e) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Pair<String, Pair<SigExp, SigExp>>> visit(Unit env, Plus e) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Pair<String, Pair<SigExp, SigExp>>> visit(Unit env, Times e) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Pair<String, Pair<SigExp, SigExp>>> visit(Unit env, Exp e) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Pair<String, Pair<SigExp, SigExp>>> visit(Unit env,
			fql.decl.InstExp.Const e) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Pair<String, Pair<SigExp, SigExp>>> visit(Unit env,
			fql.decl.InstExp.Delta e) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Pair<String, Pair<SigExp, SigExp>>> visit(Unit env,
			fql.decl.InstExp.Sigma e) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Pair<String, Pair<SigExp, SigExp>>> visit(Unit env,
			fql.decl.InstExp.Pi e) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Pair<String, Pair<SigExp, SigExp>>> visit(Unit env,
			fql.decl.InstExp.FullSigma e) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Pair<String, Pair<SigExp, SigExp>>> visit(Unit env,
			fql.decl.InstExp.Relationalize e) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Pair<String, Pair<SigExp, SigExp>>> visit(Unit env,
			fql.decl.InstExp.External e) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Pair<String, Pair<SigExp, SigExp>>> visit(Unit env, Eval e) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Pair<String, Pair<SigExp, SigExp>>> visit(Unit env, FullEval e) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Pair<String, Pair<SigExp, SigExp>>> visit(Unit env,
			fql.decl.MapExp.Id e) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Pair<String, Pair<SigExp, SigExp>>> visit(Unit env,
			fql.decl.MapExp.Comp e) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Pair<String, Pair<SigExp, SigExp>>> visit(Unit env, Dist1 e) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Pair<String, Pair<SigExp, SigExp>>> visit(Unit env, Dist2 e) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Pair<String, Pair<SigExp, SigExp>>> visit(Unit env,
			fql.decl.MapExp.Var e) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Pair<String, Pair<SigExp, SigExp>>> visit(Unit env,
			fql.decl.MapExp.Const e) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Pair<String, Pair<SigExp, SigExp>>> visit(Unit env,
			fql.decl.MapExp.TT e) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Pair<String, Pair<SigExp, SigExp>>> visit(Unit env,
			fql.decl.MapExp.FF e) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Pair<String, Pair<SigExp, SigExp>>> visit(Unit env,
			fql.decl.MapExp.Fst e) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Pair<String, Pair<SigExp, SigExp>>> visit(Unit env,
			fql.decl.MapExp.Snd e) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Pair<String, Pair<SigExp, SigExp>>> visit(Unit env,
			fql.decl.MapExp.Inl e) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Pair<String, Pair<SigExp, SigExp>>> visit(Unit env,
			fql.decl.MapExp.Inr e) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Pair<String, Pair<SigExp, SigExp>>> visit(Unit env, Apply e) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Pair<String, Pair<SigExp, SigExp>>> visit(Unit env, Curry e) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Pair<String, Pair<SigExp, SigExp>>> visit(Unit env,
			fql.decl.MapExp.Case e) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Pair<String, Pair<SigExp, SigExp>>> visit(Unit env,
			fql.decl.MapExp.Prod e) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Pair<String, Pair<SigExp, SigExp>>> visit(Unit env, Sub e) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Pair<String, Pair<SigExp, SigExp>>> visit(Unit env, Opposite e) {
		// TODO Auto-generated method stub
		return null;
	}

}
