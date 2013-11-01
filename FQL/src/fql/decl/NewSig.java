package fql.decl;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import fql.DEBUG;
import fql.FQLException;
import fql.Pair;
import fql.Triple;

public class NewSig {
/*
	public List<Node> nodes;
	public List<Edge> edges;
	public List<Attribute<Node>> attrs;
	public List<Eq> eqs;
	
	public NewSig(List<String> nodes_str,
			List<Triple<String, String, String>> attrs_str,
			List<Triple<String, String, String>> arrows_str,
			List<Pair<List<String>, List<String>>> equivs) throws FQLException {
		Set<Node> nodesA = new HashSet<>();
		Set<Edge> edgesA = new HashSet<>();
		Set<Attribute<Node>> attrsA = new HashSet<>();

		Set<String> seen = new HashSet<String>();
		for (String s : nodes_str) {
			if (seen.contains(s)) {
				throw new FQLException("Duplicate name: " + s);
			}
			seen.add(s);
			nodesA.add(new Node(s));
		}

		for (Triple<String, String, String> arrow : arrows_str) {
			String name = arrow.first;
			String source = arrow.second;
			String target = arrow.third;

			if (seen.contains(name)) {
				throw new FQLException("Duplicate name: " + name);
			}
			seen.add(name);

			Node source_node = lookup(source, nodesA);
			if (source_node == null) {
				throw new FQLException("Missing node " + source + " in " + n);
			}
			Node target_node = lookup(target, nodesA);
			if (target_node == null) {
				throw new FQLException("Missing node " + target + " in " + n);
			}
			// nodesA.add(target_node);
			Edge e = new Edge(name, source_node, target_node);
			edgesA.add(e);
		}

		for (Triple<String, String, String> attr : attrs_str) {
			String name = attr.first;
			String source = attr.second;
			String target = attr.third;

			if (seen.contains(name)) {
				throw new FQLException("Duplicate name: " + name);
			}
			seen.add(name);

			Node source_node = lookup(source, nodesA);
			if (source_node == null) {
				throw new FQLException("Missing node " + source + " in " + n);
			}

			Attribute<Node> a = new Attribute<>(name, source_node, tryParseType(target));

			attrsA.add(a);

		}

		nodes = new LinkedList<>(nodesA);
		edges = new LinkedList<>(edgesA);
		attrs = new LinkedList<>(attrsA);

		eqs = new HashSet<Eq>();
		for (Pair<List<String>, List<String>> equiv : equivs) {
			Path lhs = new Path(this, equiv.first);
			Path rhs = new Path(this, equiv.second);
			if (!lhs.source.equals(rhs.source)) {
				throw new FQLException("source object mismatch " + lhs
						+ " and " + rhs + " in " + name0);
			}
			if (!lhs.target.equals(rhs.target)) {
				throw new FQLException("target object mismatch " + lhs
						+ " and " + rhs + " in " + name0);
			}
			Eq eq = new Eq(lhs, rhs);
			eqs.add(eq);
		}
//		if (!DEBUG.ALLOW_INFINITES) {
			toCategory2();
	//	}
	}

*/
}
