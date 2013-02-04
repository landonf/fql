package fql;

import java.util.HashMap;
import java.util.Map;

public class SetFunTrans <Obj, Arrow, Y, X> {

		Map<Obj, Map<Value<Y,X>, Value<Y,X>>> eta;
		Inst<Obj, Arrow, Y, X> F, G;
		
		public SetFunTrans(Map<Obj, Map<Value<Y,X>, Value<Y,X>>> eta,
				Inst<Obj, Arrow, Y, X> F,
				Inst<Obj, Arrow, Y, X> G) {
			this.eta = eta;
			this.F = F;
			this.G = G;
			validate();
		}
		
		public Map<Value<Y,X>, Value<Y,X>> eta(Obj X) {
			return eta.get(X);
		}
		
		public void validate() {
			if (!F.cat.equals(G.cat)) {
				throw new RuntimeException("SetFuntrans category mismath " + F.cat + " and " + G.cat);
			}

			for (Arr<Obj, Arrow> f : F.cat.arrows) {
				Map<Value<Y,X>, Value<Y,X>> lhs = compose(F.applyA(f), eta(f.dst));
				Map<Value<Y,X>, Value<Y,X>> rhs = compose(eta(f.src), G.applyA(f));
				if (!lhs.equals(rhs)) {
					throw new RuntimeException("Bad nat trans " + f + "\n in " + this + "\n\nlhs is " + lhs + "\n\nrhs is " + rhs
							+"\n\nF(f) is " + F.applyA(f) + "\n\neta(F(f)) is " + eta(f.dst) + "\n\neta(G(f)) is " + eta(f.src) + "\n\nG(f) is " + G.applyA(f));
				}
			}
			
//		    \eta_Y \circ F(f) = G(f) \circ \eta_X 
		}
		
		private Map<Value<Y,X>, Value<Y,X>> compose(Map<Value<Y,X>, Value<Y,X>> f,
				Map<Value<Y,X>, Value<Y,X>> g) {
			Map<Value<Y,X>, Value<Y,X>> ret = new HashMap<>();
			for (Value<Y,X> s : f.keySet()) {
				ret.put(s, g.get(f.get(s)));
			}
			return ret;
		}

		public boolean equals() {
			throw new RuntimeException("Equality of FinTrans");
		}

		@Override
		public String toString() {
			return "SetFunTrans [eta=" + eta + "]";
		}
		
		

	}



