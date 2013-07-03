package fql.sql;

import java.util.List;

import fql.Pair;
import fql.Triple;

public class EmbeddedDependency {

	public List<String> forall, exists;
	public List<Triple<String, String, String>> where, tgd, not;
	public List<Pair<String, String>> egd;
	
	public EmbeddedDependency(List<String> forall, List<String> exists,
			List<Triple<String, String, String>> where,
			List<Triple<String, String, String>> tgd,
			List<Triple<String, String, String>> not,
			List<Pair<String, String>> egd) {
		super();
		this.forall = forall;
		this.exists = exists;
		this.where = where;
		this.tgd = tgd;
		this.egd = egd;
		this.not = not;
	}
	
	public String toString() {
		String ret = "";
		
		ret += "forall ";
		int i = 0;
		for (String s : forall) {
			if (i++ > 0) {
				ret += " ";
			}
			ret += s;
		}
		
		ret += ", ";
		i = 0;
		for (Triple<String, String, String> s : where) {
			if (i++ > 0) {
				ret += " /\\ ";
			}
			ret += s.first + "(" + s.second + ", " + s.third + ")";
		}
		
		ret += " -> ";
		if (exists.size() > 0) {
			ret += "exists ";
			i = 0;
			for (String s : exists) {
				if (i++ > 0) {
					ret += " ";
				}
				ret += s;
			}
			ret += ", ";
		}
		
		i = 0;
		for (Pair<String, String> s : egd) {
			if (i++ > 0) {
				ret += " /\\ ";
			}
			ret += s.first + " = " + s.second;
		}

		for (Triple<String, String, String> s : tgd) {
			if (i++ > 0) {
				ret += " /\\ ";
			}
			ret += s.first + "(" + s.second + ", " + s.third + ")";
		}
		for (Triple<String, String, String> s : not) {
			if (i++ > 0) {
				ret += " /\\ ";
			}
			ret += "not " + s.first + "(" + s.second + ", " + s.third + ")";
		}
		
		
		return ret;
	}
	
	
	
}
