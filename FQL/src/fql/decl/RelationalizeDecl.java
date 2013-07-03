package fql.decl;

public class RelationalizeDecl extends InstanceDecl {

	public String inst;
	
	public RelationalizeDecl(String name, String type, String inst) {
		super(name, type);
		this.inst = inst;
	}

	@Override
	public String toString() {
		return "RelationalizeDecl [inst=" + inst + ", type=" + type + ", name="
				+ name + "]";
	}

}
