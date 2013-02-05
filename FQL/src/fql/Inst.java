package fql;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 
 * @author ryan
 *
 * Finite instances - functors to set.
 * 
 * @param <Obj> type of objects
 * @param <Arrow> type of arrows
 * @param <Y> carrier for set objects
 * @param <X> carrier for set arrows
 */
public class Inst<Obj, Arrow, Y, X>  {

	Map<Obj, Set<Value<Y, X>>> objM;
	Map<Arr<Obj,Arrow>, Map<Value<Y, X>, Value<Y, X>>> arrM;
	FinCat<Obj, Arrow> cat;

	public Inst(Map<Obj, Set<Value<Y, X>>> objM,
			Map<Arr<Obj,Arrow>, Map<Value<Y, X>, Value<Y, X>>> arrM, FinCat<Obj, Arrow> cat) {
		this.objM = objM;
		this.arrM = arrM;
		this.cat = cat;
		if (DEBUG.VALIDATE) {
			validate();
		}
	}
	
	

	@Override
	public String toString() {
		return "SetFunctor [objM=\n" + objM + ",\narrM=" + arrM + "]";
	}

	public Set<Value<Y, X>> applyO(Object o) {
		return objM.get(o);
	}

	public Map<Value<Y, X>, Value<Y,X>> applyA(Arr<Obj,Arrow> a) {
		return arrM.get(a);
	}
	
	public void validate() {
		for (Obj o : cat.objects) {
			if (!objM.containsKey(o)) {
				throw new RuntimeException("Functor does not map " + o + " \n in \n " + this);
			}
		}
		for (Arr<Obj, Arrow> a : cat.arrows) {
			if (!arrM.containsKey(a)) {
				throw new RuntimeException("Functor does not map " + a + this);
			}
			Set<Value<Y, X>> src = objM.get(a.src);
			Set<Value<Y, X>> dst = objM.get(a.dst);
			Map<Value<Y, X>, Value<Y, X>> f = arrM.get(a);
			for (Value<Y,X> src0 : src) {
				if (f.get(src0) == null) {
					throw new RuntimeException();
				}
				if (!dst.contains(f.get(src0)) ) {
					throw new RuntimeException();
				}
			}
			
			for (Arr<Obj, Arrow> b : cat.arrows) {
				Arr<Obj, Arrow> c = cat.compose(a, b);
				if (c == null) {
					continue;
				}
				Map<Value<Y,X>, Value<Y,X>> a0 = arrM.get(a);
				Map<Value<Y,X>, Value<Y,X>> b0 = arrM.get(b);
				Map<Value<Y,X>, Value<Y,X>> c0 = arrM.get(c);
				if (!c0.equals(compose(a0, b0))) {
					throw new RuntimeException("Func does not preserve \n " + a + "\n" + b + "\n" + c + "\n" + a0 + "\n" + b0 + "\n" + c0 + "\n" + compose(a0, b0) + "\n" + cat);
				}
			}	
		}
	}
	
	private Map<Value<Y,X>, Value<Y,X>> compose(Map<Value<Y,X>, Value<Y,X>> f,
			Map<Value<Y,X>, Value<Y,X>> g) {
		Map<Value<Y,X>, Value<Y,X>> ret = new HashMap<>();
		for (Value<Y,X> s : f.keySet()) {
			ret.put(s, g.get(f.get(s)));
		}
		return ret;
	}

	/**
	 * Constructs a terminal (one element) instance
	 */
	public static <Obj,Arrow,Y> Inst<Obj,Arrow,Y,Obj> terminal(FinCat<Obj,Arrow> s) throws FQLException {
		
		Map<Obj, Set<Value<Y,Obj>>> ret1 = new HashMap<>();
		Map<Arr<Obj,Arrow>, Map<Value<Y,Obj>,Value<Y,Obj>>> ret2 = new HashMap<>();
		
		for (Obj o : s.objects) {
			Set<Value<Y,Obj>> x = new HashSet<>();
			x.add(new Value<Y,Obj>(o));
			ret1.put(o, x);
		}
		
		for (Arr<Obj, Arrow> a : s.arrows) {
			Map<Value<Y,Obj>, Value<Y,Obj>> x = new HashMap<>();
			x.put(new Value<Y,Obj>(a.src), new Value<Y,Obj>(a.dst));
			ret2.put(a, x);
		}

		return new Inst<>(ret1, ret2, s);
	}

	
}
