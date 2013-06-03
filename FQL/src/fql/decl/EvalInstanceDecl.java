package fql.decl;

/**
 * 
 * @author ryan
 *
 * Instances that are given by evaluation of a query.
 */
public class EvalInstanceDecl extends InstanceDecl {
	
	public String query;
	public String inst;

	public EvalInstanceDecl(String name, String query, String inst, String type) {
		super(name, type);
		this.query = query;
		this.inst = inst;
	}

}
