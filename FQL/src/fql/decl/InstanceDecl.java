package fql.decl;

/**
 * 
 * @author ryan
 * 
 *         Superclass for instance declarations.
 */
public class InstanceDecl extends Decl {

	public String type;

	public InstanceDecl(String name, String type) {
		super(name);
		this.type = type;
	}
}
