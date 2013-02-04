package fql;


public class QueryDecl extends Decl {

	public QueryDecl(String name, String schema) {
		super(name);
		this.schema = schema;
		kind = KIND.ID;		
	}

	public QueryDecl(String name, String source, String target) {
		super(name);
		this.source = source;
		this.target = target;
		kind = KIND.COMPOSE;
	}
	
	public QueryDecl(String name, String project, String join, String union) {
		super(name);
		this.project = project;
		this.join = join;
		this.union = union;
		kind = KIND.QUERY;
	}

	@Override
	public String toString() {
		return "project " + project + " join " + join + " union " + union;
	}

	enum KIND { ID, COMPOSE, QUERY }

	KIND kind;
	
	String source, target;
	
	String schema;
	
	String	project, join, union;	


}
