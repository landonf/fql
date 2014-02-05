package fql.sql;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fql.Pair;
import fql.Triple;

/**
 * 
 * @author ryan
 *
 * An embedded dependency.
 */
public class ED {

	public List<String> forall, exists;
	public List<Pair<Integer, Integer>> where, ret;

	public ED(List<String> forall, List<String> exists,
			List<Pair<Integer, Integer>> where, List<Pair<Integer, Integer>> ret) {
		this.forall = forall;
		this.exists = exists;
		this.where = dedup(where);
		this.ret = dedup(ret);

	}

	private List<Pair<Integer, Integer>> dedup(List<Pair<Integer, Integer>> l) {
		 List<Pair<Integer, Integer>> ret = new LinkedList<>();
		 
		 for (int i = 0; i < l.size(); i++) {
			 Pair<Integer, Integer> p = l.get(i);
			 List<Pair<Integer, Integer>> l0 = l.subList(i+1, l.size());
			 if (l0.contains(p)) {
				 continue;
			 }
			 if (l0.contains(p.reverse())) {
				 continue;
			 }
			 ret.add(p);
		 }
		 
		 return ret;
	}

	public static ED from(EmbeddedDependency ed) {
		List<String> f = new LinkedList<>();
		List<Pair<Integer, Integer>> w = new LinkedList<>();

		int i = 0;
		String[] v = new String[(2 * ed.tgd.size()) + (2 * ed.where.size())];
		
		for (Triple<String, String, String> k : ed.where) {
			f.add(k.first);
			v[i++] = k.second;
			v[i++] = k.third;
		}
		
		for (int x = 0; x < (2 * ed.where.size()); x++) {
			for (int y = 0; y < (2 * ed.where.size()); y++) {
				if (x == y) {
					continue;
				}
				if (v[x].equals(v[y])) {
					w.add(new Pair<>(x, y));
				}
			}
		}
		
		List<String> e = new LinkedList<>();
		List<Pair<Integer, Integer>> r = new LinkedList<>();

		for (Triple<String, String, String> k : ed.tgd) {
			e.add(k.first);
			v[i] = k.second;
			if (ed.forall.contains(k.second)) {
				r.add(new Pair<>(i, invLookup(v, k.second)));
			}
			i++;

			v[i] = k.third;
			if (ed.forall.contains(k.third)) {
				r.add(new Pair<>(i, invLookup(v, k.third)));
			}
			i++;

		}
		

		for (int x = (2 * ed.where.size()); x < v.length; x++) {
			for (int y = (2 * ed.where.size()); y < v.length; y++) {
				if (x == y) {
					continue;
				}
				if (v[x].equals(v[y])) {
					r.add(new Pair<>(x, y));
				}
			}
		}

		for (Pair<String, String> k : ed.egd) {
			int x = invLookup(v, k.first);
			int y = invLookup(v, k.second);
			if (x != y) {
				r.add(new Pair<>(x, y));
			}
		}
		
	
		ED ret = new ED(f, e, w, r);

//		System.out.println("Input: " + ed);
	//	System.out.println("Output: " + ret);
		
		return ret; 
		
	}

	private static int invLookup(String[] v, String first) {
		int i = 0;
		for (String k : v) {
			if (k.equals(first)) {
				return i;
			}
			i++;
		}
		throw new RuntimeException("Cannot find " + first + " in " + Arrays.toString(v));
	}

	public Flower front() {
		return make(2 * forall.size(), forall, where);
	}
	
	public Flower back() {
		List<String> forall0 = new LinkedList<>(forall);
		List<Pair<Integer, Integer>> where0 = new LinkedList<>(where);
		forall0.addAll(exists);
		where0.addAll(ret);
		Flower back = make(2 * forall.size(), forall0, where0);
		return back;
	}
	
	public boolean holds(Map<String, Set<Pair<Object, Object>>> I) {
		Flower front = make(2 * forall.size(), forall, where);

		List<String> forall0 = new LinkedList<>(forall);
		List<Pair<Integer, Integer>> where0 = new LinkedList<>(where);
		forall0.addAll(exists);
		where0.addAll(ret);
		Flower back = make(2 * forall.size(), forall0, where0);

		Map<String, Set<Map<Object, Object>>> state = conv(I);

	//		System.out.println("front is " + front);
		//	System.out.println("back is " + back);
		
		Set<Map<Object, Object>> lhs = front.eval(state);
		Set<Map<Object, Object>> rhs = back.eval(state);

		if (lhs.equals(rhs)) {
			return true;
		}
	//	System.out.println("Does not hold\n" + lhs + "\n\n" + rhs + "\n\n" + front + "\n\n" + back);
		return false;
	}

	private Flower make(int max, List<String> frm,
			List<Pair<Integer, Integer>> whr) {

		//System.out.println("Make called on " + max + " " + frm + " " + whr);
		
		LinkedHashMap<String, String> f = new LinkedHashMap<>();
		int i = 0;
		for (String r : frm) {
			f.put("t" + (i++), r);
		}

		List<Pair<Pair<String, String>, Pair<String, String>>>
		w = new LinkedList<>();
		for (Pair<Integer, Integer> p : whr) {
			w.add(new Pair<>(
				  new Pair<>("t" + (p.first / 2), "c" + (p.first % 2)), 
				  new Pair<>("t" + (p.second/ 2), "c" + (p.second% 2))));
		}

		LinkedHashMap<String, Pair<String, String>> s = new LinkedHashMap<>();
		for (i = 0; i < max; i++) {
			s.put("c" + i, new Pair<>("t" + (i / 2), "c" + (i % 2)));
		}

		return new Flower(s, f, w);
	}

	public static Map<String, Set<Map<Object, Object>>> conv(
			Map<String, Set<Pair<Object, Object>>> i) {
		Map<String, Set<Map<Object, Object>>> ret = new HashMap<>();

		for (String k : i.keySet()) {
			Set<Pair<Object, Object>> v = i.get(k);
			Set<Map<Object, Object>> v0 = new HashSet<>();

			for (Pair<Object, Object> o : v) {
				Map<Object, Object> x = new HashMap<>();
				x.put("c0", o.first);
				x.put("c1", o.second);
				v0.add(x);
			}

			ret.put(k, v0);
		}

		return ret;
	}
	
	@Override
	public String toString() {
		String r = "forall ";
		int i = 0;
		for (String s : forall) {
			r += s + "(v" + (i++) + ",v" + (i++) + ") "; 
		}
		r += ", ";
		for (Pair<Integer, Integer> p : where) {
			r += "v" + p.first + "=v" + p.second + "  "; 
		}		
		r += " -> exists ";
		for (String s : exists) {
			r += s + "(v" + (i++) + ",v" + (i++) + ") "; 
		}
		r += ", ";
		for (Pair<Integer, Integer> p : ret) {
			r += "v" + p.first + "=v" + p.second + "  "; 
		}		
		
		return r;
	}

}
