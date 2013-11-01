package fql.decl;

import java.util.LinkedHashMap;
import java.util.List;

import fql.Pair;
import fql.Unit;

public class NewProgram<A,B,C,D> {
	
	public List<String> order;

	public LinkedHashMap<String, Poly<Unit,A>> sigs;
	public LinkedHashMap<String, Caml<Unit,A, B>> maps;
	public LinkedHashMap<String, Poly<Poly<Unit,A>, C>> insts;
	public LinkedHashMap<String, Caml<Poly<Unit,A>, C, D>> trans;
	
	public LinkedHashMap<String, Pair<Poly<Unit,A>, Poly<Unit,A>>> maps_t;
	public LinkedHashMap<String, Poly<Unit, A>> insts_t;
	public LinkedHashMap<String, Pair<Poly<Poly<Unit,A>, C>, Poly<Poly<Unit,A>, C>>> trans_t;

	public LinkedHashMap<String, Integer> sigs_lines, maps_lines, insts_lines, trans_lines;

	
	@Override
	public String toString() {
		String ret = "";
		
		for (String k : sigs.keySet()) {
			Poly<Unit, A> v = sigs.get(k);
			ret += "schema " + k + " = " + v + "\n\n";
		}
		
		for (String k : maps.keySet()) {
			Caml<Unit, A, B> v = maps.get(k);
			Pair<Poly<Unit, A>, Poly<Unit, A>> t = maps_t.get(k);
			ret += "mapping " + k + " : " + t.first + " -> " + t.second + " = " + v + "\n\n";
		}
		
		for (String k : insts.keySet()) {
			Poly<Poly<Unit, A>, C> v = insts.get(k);
			Poly<Unit, A> t = insts_t.get(k);
			ret += "instance " + k + " : " + t + " = " + v + "\n\n";
		}
		
		for (String k : trans.keySet()) {
			Caml<Poly<Unit, A>, C, D> v = trans.get(k);
			Pair<Poly<Poly<Unit, A>, C>, Poly<Poly<Unit, A>, C>> t = trans_t.get(k);
			ret += "transform " + k + " : " + t.first + " -> " + t.second + " = " + v + "\n\n";
		}
		
		return ret;
	}

	public NewProgram(
			LinkedHashMap<String, Poly<Unit, A>> sigs,
			LinkedHashMap<String, Caml<Unit, A, B>> maps,
			LinkedHashMap<String, Poly<Poly<Unit, A>, C>> insts,
			LinkedHashMap<String, Caml<Poly<Unit, A>, C, D>> trans,
			LinkedHashMap<String, Pair<Poly<Unit, A>, Poly<Unit, A>>> maps_t,
			LinkedHashMap<String, Poly<Unit, A>> insts_t,
			LinkedHashMap<String, Pair<Poly<Poly<Unit, A>, C>, Poly<Poly<Unit, A>, C>>> trans_t,
			LinkedHashMap<String, Integer> sigs_lines, 
			LinkedHashMap<String, Integer> maps_lines,
			LinkedHashMap<String, Integer> insts_lines, 
			LinkedHashMap<String, Integer> trans_lines,
			List<String> order
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
		this.order = order;
	}

	
}
