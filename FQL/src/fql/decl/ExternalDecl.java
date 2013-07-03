package fql.decl;

import fql.decl.InstanceDecl;

/**
 * 
 * @author ryan
 *
 * "Blank" instances connecting generated SQL with existing tables
 */
public class ExternalDecl extends InstanceDecl {

	public ExternalDecl(String name, String type) {
		super(name, type);
		
	}

}
