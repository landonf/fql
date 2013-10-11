package fql.decl;

import java.util.Map;

import fql.Pair;
import fql.Unit;

public class NewProgram<A,B,C,D> {

	
	@Override
	public String toString() {
		return "NewProgram [sigs=" + sigs + ", maps=" + maps + ", insts="
				+ insts + ", trans=" + trans + ", maps_t=" + maps_t
				+ ", insts_t=" + insts_t + ", trans_t=" + trans_t
				+ ", sigs_lines=" + sigs_lines + ", maps_lines=" + maps_lines
				+ ", insts_lines=" + insts_lines + ", trans_line=" + trans_lines
				+ "]";
	}

	public NewProgram(
			Map<String, Poly<Unit, A>> sigs,
			Map<String, Caml<Unit, A, B>> maps,
			Map<String, Poly<Poly<Unit, A>, C>> insts,
			Map<String, Caml<Poly<Unit, A>, C, D>> trans,
			Map<String, Pair<Poly<Unit, A>, Poly<Unit, A>>> maps_t,
			Map<String, Poly<Unit, A>> insts_t,
			Map<String, Pair<Poly<Poly<Unit, A>, C>, Poly<Poly<Unit, A>, C>>> trans_t,
			Map<String, Integer> sigs_lines, Map<String, Integer> maps_lines,
			Map<String, Integer> insts_lines, Map<String, Integer> trans_lines
			) {
		super();
		this.sigs = sigs;
		this.maps = maps;
		this.insts = insts;
		this.trans = trans;
		this.maps_t = maps_t;
		this.insts_t = insts_t;
		this.trans_t = trans_t;
		this.sigs_lines = sigs_lines;
		this.maps_lines = maps_lines;
		this.insts_lines = insts_lines;
		this.trans_lines = trans_lines;
	}

	Map<String, Poly<Unit,A>> sigs;
	Map<String, Caml<Unit,A, B>> maps;
	Map<String, Poly<Poly<Unit,A>, C>> insts;
	Map<String, Caml<Poly<Unit,A>, C, D>> trans;
	
	Map<String, Pair<Poly<Unit,A>, Poly<Unit,A>>> maps_t;
	Map<String, Poly<Unit, A>> insts_t;
	Map<String, Pair<Poly<Poly<Unit,A>, C>, Poly<Poly<Unit,A>, C>>> trans_t;

	Map<String, Integer> sigs_lines, maps_lines, insts_lines, trans_lines;
	
	/*
	static class EChecker implements PolyVisitor<Pair<Poly<NewSigConst>, NewInstConst>, Unit, Poly<NewSigConst>> {

	
		
	} */

	
}
