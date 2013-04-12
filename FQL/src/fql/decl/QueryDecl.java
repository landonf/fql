package fql.decl;

/**
 * 
 * @author ryan
 *
 * Query declarations.
 */
public class QueryDecl extends Decl {
	
	public enum KIND { ID, COMPOSE, QUERY }
	public KIND kind;
	public String source;
	public String target;
	public String schema;
	public String project, join, union;	

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


}
