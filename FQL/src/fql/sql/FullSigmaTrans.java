package fql.sql;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fql.FQLException;
import fql.Pair;
import fql.Triple;
import fql.cat.Denotation;
import fql.decl.Attribute;
import fql.decl.Edge;
import fql.decl.Instance;
import fql.decl.Mapping;
import fql.decl.Node;
import fql.decl.Signature;
import fql.decl.Transform;

/**
 * 
 * @author ryan
 * 
 *         Full sigma, but on transformations. Implementing this requires
 *         hacking the PSM interpreter to reset its GUIDs to the ID where the
 *         source sigma computation started. Really, this should use skolem
 *         functions.
 */
public class FullSigmaTrans extends PSM {

	
	@Override
	public String toString() {
		return "FullSigmaTrans [f=" + f + ", srcH=" + srcH + ", src=" + src
				+ ", dstH=" + dstH + ", dst=" + dst + ", h=" + h + ", pre="
				+ pre + "]";
	}

	Mapping f;
	String srcH, src;
	String dstH, dst;
	String h;
	String pre;

	public FullSigmaTrans(Mapping f, String srcH, String src, String dstH,
			String dst, String h, String out) {
		super();
		this.f = f;
		this.srcH = srcH;
		this.src = src;
		this.dstH = dstH;
		this.dst = dst;
		this.h = h;
		this.pre = out;
	}

	@Override
	public String toPSM() {
		throw new RuntimeException("Cannot generate SQL for full sigma");
	}

	@Override
	public void exec(PSMInterp interp,
			Map<String, Set<Map<Object, Object>>> state) {
		Signature C = f.source;
		Signature D = f.target;
		List<Pair<String, List<Pair<Object, Object>>>> I0 = PSMGen.gather(srcH,
				C, state);
		List<Pair<String, List<Pair<Object, Object>>>> J0 = PSMGen.gather(dstH,
				C, state);
		List<Pair<String, List<Pair<Object, Object>>>> H0 = PSMGen.gather(h, C,
				state);

	//	System.out.println("trans is " + this);
	//	System.out.println("H0 is " + H0);
	//	System.out.println("state is " + state);
		
		// List<Pair<String, List<Pair<Object, Object>>>> I0X =
		// PSMGen.gather(src, D, state);
		List<Pair<String, List<Pair<Object, Object>>>> J0X = PSMGen.gather(dst,
				D, state);

		List<Pair<String, List<Pair<Object, Object>>>> tempI = new LinkedList<>();
		List<Pair<String, List<Pair<Object, Object>>>> tempH = new LinkedList<>();

		for (Node n : C.nodes) {
			Set<Map<Object, Object>> x1 = state.get(dst + "_" + n.string);
			Set<Map<Object, Object>> x2 = state
					.get(dst + "_" + n.string + "_e");

			tempI.add(new Pair<>(n.string, conv(x1)));
			tempH.add(new Pair<>(n.string, conv(x2)));
		}
		for (Edge e : C.edges) {
			Set<Map<Object, Object>> x1 = state.get(dst + "_" + e.name);
			tempI.add(new Pair<>(e.name, conv(x1)));
		}
		for (Attribute<Node> e : C.attrs) {
			Set<Map<Object, Object>> x1 = state.get(dst + "_" + e.name);
			tempI.add(new Pair<>(e.name, conv(x1)));
		}

		try {
			Instance I = new Instance(C, I0);
			Instance J = new Instance(C, J0);
			Transform H = new Transform(I, J, H0);

			// Instance IX = new Instance(D, I0X);
			Instance JX = new Instance(D, J0X);

			Instance temp = new Instance(C, tempI);
			Transform etaJ = new Transform(J, temp, tempH);

			Transform HX = Transform.composeX(H, etaJ);

			Integer current = interp.guid;
			interp.guid = interp.sigmas.get(src);
			Triple<Instance, Map<Node, Map<Integer, Integer>>, Map<Node, Map<Integer, Integer>>> xxx = Denotation
					.fullSigmaWithAttrs(interp, f, I, HX, JX,
							interp.sigmas2.get(src));
			interp.guid = current;

			// System.out.println("state " + state);

			for (Node n : D.nodes) {
				state.put(pre + "_" + n.string, conv0(xxx.third.get(n)));
			}
			for (Attribute<Node> a : D.attrs) {
				state.put(pre + "_" + a.name,
						new HashSet<Map<Object, Object>>());
			}
			for (Edge a : D.edges) {
				state.put(pre + "_" + a.name,
						new HashSet<Map<Object, Object>>());
			}

		} catch (FQLException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	private Set<Map<Object, Object>> conv0(Map<Integer, Integer> map) {
		Set<Map<Object, Object>> ret = new HashSet<>();
		for (Integer k : map.keySet()) {
			Map<Object, Object> m = new HashMap<>();
			m.put("c0", k);
			m.put("c1", map.get(k));
			ret.add(m);
		}
		return ret;
	}

	private List<Pair<Object, Object>> conv(Set<Map<Object, Object>> x2) {
		List<Pair<Object, Object>> ret = new LinkedList<>();
		for (Map<Object, Object> k : x2) {
			ret.add(new Pair<>(k.get("c0"), k.get("c1")));
		}

		return ret;
	}

}
