package fql.decl;

import java.util.List;

import fql.Pair;

/**
 * 
 * @author ryan
 *
 * Declarations for mappings
 */
public class MappingDecl extends Decl {
	
	public enum KIND { ID, COMPOSE, MORPHISM }
	public KIND kind;
	public String source, target;
	public String schema;
	public List<Pair<String,List<String>>> arrows;
	public List<Pair<String, String>> objs;

	
	public MappingDecl(String name, String schema) {
		super(name);
		this.schema = schema;
		kind = KIND.ID;
	}

	public MappingDecl(String name, String source, String target) {
		super(name);
		this.source = source;
		this.target = target;
		kind = KIND.COMPOSE;
	}

	public MappingDecl(String name, String source, String target,
			List<Pair<String, String>> objs,
			List<Pair<String, List<String>>> arrows) {
		this(name, source, target);
		this.objs = objs;
		this.arrows = arrows;
		kind = KIND.MORPHISM;
	}

	@Override
	public String toString() {
		return "MappingDecl [kind=" + kind + ", source=" + source + ", target="
				+ target + ", schema=" + schema + ", arrows=" + arrows
				+ ", objs=" + objs + "]";
	}

	
}
