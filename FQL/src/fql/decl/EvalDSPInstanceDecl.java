package fql.decl;

/**
 * 
 * @author ryan
 * 
 *         Instances that were created by delta, sigma, pi.
 */
public class EvalDSPInstanceDecl extends InstanceDecl {

	@Override
	public String toString() {
		return "EvalDSPInstanceDecl [mapping=" + mapping + ", inst=" + inst
				+ ", kind=" + kind + "]";
	}

	public String mapping;
	public String inst;
	public String kind;

	public EvalDSPInstanceDecl(String name, String kind, String mapping,
			String inst, String type) {
		super(name, type);
		this.mapping = mapping;
		this.inst = inst;
		this.kind = kind;
	}

}
