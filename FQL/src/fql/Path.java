package fql;

import java.util.LinkedList;
import java.util.List;

public class Path {

	Node source;
	Node target;
	List<Edge> path;
	
	public Path(Node source, Node target, List<Edge> path) {
		assert(source != null);
		assert(target != null);
		assert(path != null);
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
		assert(source != null);
		
		target = source;
		for (int i = 1; i < strings.size(); i++) {
			String string = strings.get(i);
			Edge e = schema.getEdge(string);
			path.add(e);
			target = e.target;
			assert(target != null);
		}
	}

	public Path(Signature s, Edge e) throws FQLException {
		this(s, doStuff(s,e));
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

	public boolean equals0(Path p, Signature s) {
//		if (Equality.which.equals(Equality.syntactic)) {
//			return equals(p);
//		}
		return false;
		//TODO (DEFER) eq path
	}

	public String toLong() {
		return toString() + " : " + source.string + " -> " + target.string;
	}
}
