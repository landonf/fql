package fql.decl;

import fql.parse.Jsonable;

/**
 * 
 * @author ryan
 * 
 *         Class for edges in a signature.
 */
public class Edge implements Jsonable {

	public String toString() {
		return name + " : " + source + " -> " + target;
	}

	public String name;
	public Node source;
	public Node target;

	public Object morphism;

	public Edge(String name, Node source, Node target) {
		this.name = name;
		this.source = source;
		this.target = target;
		if (source == null || target == null || name == null) {
			throw new RuntimeException();
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		Edge other = (Edge) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
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

	public String tojson() {
		String s = "{";
		s += "\"source\" : " + source.tojson();
		s += ", \"target\" : " + target.tojson();
		s += ", \"label\" : \"" + name + "\"}";
		return s;
	}

}
