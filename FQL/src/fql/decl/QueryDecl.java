package fql.decl;

/**
 * 
 * @author ryan
 * 
 *         Query declarations.
 */
public class QueryDecl extends Decl {

	public enum KIND {
		ID, COMPOSE, QUERY
	}

	public KIND kind;
	public String source;
	public String target;
	public String schema;
	public String project, join, union;
	public String q1, q2;

	public QueryDecl(String name, String schema, String source, String target,
			int x) {
		super(name);
		this.schema = schema;
		kind = KIND.ID;
		this.source = source;
		this.target = target;
	}

	public QueryDecl(String name, String m1, String m2, String source,
			String target) {
		super(name);
		this.source = source;
		this.target = target;
		this.q1 = m1;
		this.q2 = m2;
		kind = KIND.COMPOSE;
	}

	public QueryDecl(String name, String project, String join, String union,
			String src, String dst) {
		super(name);
		this.source = src;
		this.target = dst;
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
