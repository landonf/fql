package fql.decl;

import java.util.LinkedList;
import java.util.List;

public class TypeCheckError extends RuntimeException {
	
	List<String> l = new LinkedList<>();
	
	public TypeCheckError(String s) {
		add(s);
	}
	
	public void add(String s) {
		l.add(s);
	}
	

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
