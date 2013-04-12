package fql.decl;

/**
 * 
 * @author ryan
 *
 * Instances that were created by delta, sigma, pi.
 */
public class DirectInstanceDecl extends InstanceDecl {
	
	public String mapping;
	public String inst;
	public String type;

	public DirectInstanceDecl(String name, String type, String mapping, String inst) {
		super(name);
		this.mapping = mapping;
		this.inst = inst;
		this.type = type;
	}

}
