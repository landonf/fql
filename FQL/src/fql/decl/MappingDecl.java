package fql.decl;

import java.util.List;

import fql.Pair;

/**
 * 
 * @author ryan
 * 
 *         Declarations for mappings
 */
public class MappingDecl extends Decl {

	public enum KIND {
		ID, COMPOSE, MORPHISM
	}

	public KIND kind;
	public String source, target, schema, m1, m2;
	public List<Pair<String, List<String>>> arrows;
	public List<Pair<String, String>> objs;
	public List<Pair<String, String>> atts;

	public MappingDecl(String name, String schema, String src, String dst) {
		super(name);
		this.schema = schema;
		kind = KIND.ID;
		source = src;
		target = dst;
	}

	public MappingDecl(String name, String source, String target, String m1, String m2) {
		super(name);
		this.source = source;
		this.target = target;
		this.m1 = m1;
		this.m2 = m2;
		kind = KIND.COMPOSE;
	}

	public MappingDecl(String name, String source, String target,
			List<Pair<String, String>> objs, List<Pair<String, String>> atts,
			List<Pair<String, List<String>>> arrows) {
		super(name);
		this.objs = objs;
		this.source = source;
		this.target = target;
		this.arrows = arrows;
		this.atts = atts;
		kind = KIND.MORPHISM;
	}

	@Override
	public String toString() {
		return "MappingDecl [kind=" + kind + ", source=" + source + ", target="
				+ target + ", schema=" + schema + ", arrows=" + arrows
				+ ", objs=" + objs + "]";
	}

}
