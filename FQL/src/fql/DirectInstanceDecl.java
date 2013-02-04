package fql;

public class DirectInstanceDecl extends InstanceDecl {
	
	String mapping, inst, type;

	public DirectInstanceDecl(String name, String type, String mapping, String inst) {
		super(name);
		this.mapping = mapping;
		this.inst = inst;
		this.type = type;
	}

}
