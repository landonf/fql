package fql.decl;

import java.util.LinkedList;
import java.util.List;

import fql.FQLException;
import fql.Pair;
import fql.Unit;
import fql.parse.FqlTokenizer;
import fql.parse.Jsonable;
import fql.parse.Partial;
import fql.parse.PathParser;
import fql.parse.PrettyPrinter;
import fql.parse.Tokens;

/**
 * 
 * @author ryan
 * 
 *         Paths
 */
public class Path implements Jsonable {

	public Node source;
	public Node target;
	public List<Edge> path;

	public Path(Node source, Node target, List<Edge> path) {
		assert (source != null);
		assert (target != null);
		assert (path != null);
		this.source = source;
		this.target = target;
		this.path = path;
	}

	public List<String> asList() {
		List<String> ret = new LinkedList<String>();
		ret.add(source.string);
		for (Edge e : path) {
			ret.add(e.name);
		}
		return ret;
	}

	public Path(Signature schema, List<String> strings) throws FQLException {
		if (strings.isEmpty()) {
			throw new FQLException("Empty path");
		}

		path = new LinkedList<Edge>();

		String head = strings.get(0);
		source = schema.getNode(head);
		if (source == null) {
			throw new FQLException("bad path: " + strings + " in " + schema.name0);
		}

		target = source;
		for (int i = 1; i < strings.size(); i++) {
			String string = strings.get(i);
			Edge e = schema.getEdge(string);
			if (e == null) {
				throw new FQLException("bad path: " + strings + " in " + schema.name0);
			}
			if (schema.getNode(source.string) == null) {
				throw new FQLException("bad path: " + strings + " in " + schema.name0);
			}
			if (schema.getNode(target.string) == null) {
				throw new FQLException("bad path: " + strings + " in " + schema.name0);
			}
			if (!e.source.equals(target)) {
				throw new FQLException("bad path: " + strings+ " in " + schema.name0);
			}
			path.add(e);
			target = e.target;
			if (target == null) {
				throw new FQLException("bad path: " + strings+ " in " + schema.name0);
			}
		}
	}

	public Path(Signature s, Edge e) throws FQLException {
		this(s, doStuff(s, e));
	}

	public Path(Unit n, Signature schema,
			List<Pair<Pair<String, String>, String>> strings)
			throws FQLException {
		if (strings.isEmpty()) {
			throw new FQLException("Empty path");
		}

		path = new LinkedList<Edge>();

		source = schema.getNode(strings.get(0).first.first);

		for (int i = 0; i < strings.size(); i++) {
			String string = strings.get(i).second;
			Edge e = schema.getEdge(string);
			path.add(e);
			target = e.target;
		}

	}

	public Path(Signature schema,
			List<Pair<Pair<String, String>, String>> strings, Node node)
			throws FQLException {
		path = new LinkedList<Edge>();

		if (node == null) {
			throw new RuntimeException();
		}
		source = node;

		target = source;
		for (int i = 0; i < strings.size(); i++) {
			String string = strings.get(i).second;
			Edge e = schema.getEdge(string);
			path.add(e);
			target = e.target;
		}
	}

	// private static List<String> convert(
	// List<Pair<Pair<String, String>, String>> l) {
	// List<String> ret = new LinkedList<>();
	// for (Pair<Pair<String, String>, String> x : l) {
	// ret.add(x.second);
	// }
	// return ret;
	// }

	public static Path parsePath(Signature a, String s) throws FQLException {
		try {
			Tokens t = new FqlTokenizer(s);
			PathParser pp = new PathParser();
			Partial<List<String>> r = pp.parse(t);
			if (r.tokens.toString().trim().length() != 0) {
				throw new FQLException("Invalid path: " + s);
			}
			return new Path(a, r.value);

		} catch (Exception e) {
			throw new FQLException("Invalid path: " + s);
		}

	}

	public Path(Signature a, Node a2) throws FQLException {
		this(a, foo(a2));
	}

	public static Path append(Signature s, Path arr, Path arr2)
			throws FQLException {
		if (!arr.target.equals(arr2.source)) {
			throw new RuntimeException("bad path append " + arr.toLong() + " and " + arr2.toLong());
		}
		List<String> x = new LinkedList<>(arr.asList());
		List<String> y = new LinkedList<>(arr2.asList());
		y.remove(0);
		x.addAll(y);
		return new Path(s, x);
	}
	
	public static Path append2(Signature s, Path arr2, Path arr)
			throws FQLException {
		if (!arr.target.equals(arr2.source)) {
			throw new RuntimeException("bad path append");
		}
		List<String> x = new LinkedList<>(arr.asList());
		List<String> y = new LinkedList<>(arr2.asList());
		y.remove(0);
		x.addAll(y);
		return new Path(s, x);
	}

	private static List<String> foo(Node a) {
		List<String> ret = new LinkedList<>();
		ret.add(a.string);
		return ret;
	}

	private static List<String> doStuff(Signature s, Edge e) {
		List<String> ret = new LinkedList<String>();
		ret.add(e.source.string);
		ret.add(e.name);
		return ret;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer(source.string);
		for (Edge e : path) {
			sb.append(".");
			sb.append(e.name);
		}
		return sb.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		result = prime * result + ((source == null) ? 0 : source.hashCode());
		result = prime * result + ((target == null) ? 0 : target.hashCode());
		return result;
	}

	@Override
	/**
	 * Syntactic equality of paths
	 */
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Path other = (Path) obj;
		if (path == null) {
			if (other.path != null)
				return false;
		} else if (!path.equals(other.path))
			return false;
		if (source == null) {
			if (other.source != null)
				return false;
		} else if (!source.equals(other.source))
			return false;
		if (target == null) {
			if (other.target != null)
				return false;
		} else if (!target.equals(other.target))
			return false;
		return true;
	}

	public String toLong() {
		return toString() + " : " + source.string + " -> " + target.string;
	}

	@Override
	public String tojson() {
		return PrettyPrinter.sep(",", "[", "]", path);
	}
}
