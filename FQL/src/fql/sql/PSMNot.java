package fql.sql;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fql.FQLException;
import fql.Pair;
import fql.Triple;
import fql.cat.Arr;
import fql.decl.Attribute;
import fql.decl.Edge;
import fql.decl.Instance;
import fql.decl.Node;
import fql.decl.Path;
import fql.decl.Signature;
import fql.decl.Transform;

public class PSMNot extends PSM {

	String pre, prop;
	Signature sig;

	public PSMNot(Signature sig, String pre, String prop) {
		this.sig = sig;
		this.pre = pre;
		this.prop = prop;
	}

	@Override
	public String isSql() {
		return pre;
	}

	@Override
	public void exec(PSMInterp interp,
			Map<String, Set<Map<Object, Object>>> state) {
		try {

			Signature sig0 = new Signature(sig.nodes, sig.edges, new LinkedList<Attribute<Node>>(), sig.eqs);
			
			Pair<Map<Node, Triple<Instance, Map<Object, Path>, Map<Path, Object>>>, Map<Edge, Transform>> H1 = interp.prop1
					.get(prop);
			Pair<Instance, Map<Node, Pair<Map<Object, Instance>, Map<Instance, Object>>>> H2 = interp.prop2
					.get(prop);
			//Instance old = H2.first;

			Instance prp = new Instance(sig, PSMGen.gather(prop, sig, state));

			Map<Node, Map<Object, Pair<Object, LinkedHashMap<Pair<Arr<Node, Path>, Attribute<Node>>, Object>>>> I1 = interp.prop3
					.get(prop);
			Map<Node, Map<Pair<Object, LinkedHashMap<Pair<Arr<Node, Path>, Attribute<Node>>, Object>>, Object>> I2 = interp.prop4
					.get(prop);

			List<Pair<String, List<Pair<Object, Object>>>> data = new LinkedList<>();
			
			for (Node c : sig.nodes) {
				List<Pair<Object, Object>> data0 = new LinkedList<>();
				Triple<Instance, Map<Object, Path>, Map<Path, Object>> Hc = H1.first.get(c);
				
				for (Object id : prp.getNode(c)) {
					Pair<Object, LinkedHashMap<Pair<Arr<Node, Path>, Attribute<Node>>, Object>> id0 = I1.get(c).get(id);
					Instance A = H2.second.get(c).first.get(id0.first);
		//			System.out.println("A " + A);
					Instance notA = calcSub(sig0, H1, Hc, A);
		//			System.out.println("not A" + notA);
					Object notId = H2.second.get(c).second.get(notA);
		//			System.out.println("notID " + notId);
			
					Object x = I2.get(c).get(new Pair<>(notId, id0.second));
					data0.add(new Pair<>(id, x));
					
				}
				//System.out.println(data0);
				data.add(new Pair<>(c.string, data0));
			}
			
			Transform ret = new Transform(prp, prp, data);
			PSMGen.shred(pre, ret, state);
		} catch (FQLException fe) {
			fe.printStackTrace();
			throw new RuntimeException(fe.getMessage());
		}
	}

	private Instance calcSub(
			Signature sig0,
		    Pair<Map<Node, Triple<Instance, Map<Object, Path>, Map<Path, Object>>>, Map<Edge, Transform>> H1 ,
			Triple<Instance, Map<Object, Path>, Map<Path, Object>> Hc, Instance A)
			throws FQLException {
		Map<String, Set<Pair<Object, Object>>> notA_data = new HashMap<>();
		for (Node d : sig.nodes) {
			Set<Pair<Object,Object>> dd = new HashSet<>();
			for (Object f : Hc.first.getNode(d)) {
				Path ff = Hc.second.get(f);
				for (Node d0 : sig.nodes) {
					for (Arr<Node, Path> g : sig.toCategory2().first.hom(d, d0)) {
						Arr<Node, Path> fg = sig.toCategory2().first.compose(sig.toCategory2().second.of(ff), g);
						Object xxx = H1.first.get(d0).third.get(fg.arr);
						if (!A.getNode(d0).contains(xxx)) {
							dd.add(new Pair<>(f, f));
						}
					}
				}
			}
			
			notA_data.put(d.string, dd);
		}
		for (Edge h : sig.edges) {
			Set<Pair<Object,Object>> dd = new HashSet<>();
			for (Object f : Hc.first.getNode(h.source)) { //TODO
				Path ff = Hc.second.get(f);
				Arr<Node, Path> fg = sig.toCategory2().first.compose(sig.toCategory2().second.of(ff), sig.toCategory2().second.of(new Path(sig, h)));
				Object xxx = Hc.third.get(fg.arr);
				dd.add(new Pair<>(f, xxx));
			}
			notA_data.put(h.name, dd);
		}				
		Instance notA = new Instance(sig0, notA_data);
		return notA;
	}

	@Override
	public String toPSM() {
		return pre + " := " + prop + ".not";
	}

}
