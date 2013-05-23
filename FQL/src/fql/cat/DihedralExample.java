package fql.cat;

import fql.examples.Example;

public class DihedralExample extends Example {

	@Override
	public String getName() {
		return "Dihedral 2";
	}

	@Override
	public String getText() {
		return "/* should have 4 elements */"
				+ "\nschema Dihedral2 = {"
				+ "\n	r: G -> G, R: G -> G, s: G -> G, S: G -> G ;"
				+ "\n	G.r.R = G, G.R.r = G, G.s.S = G, G.S.s=G,"
				+ "\n	G.r.r = G,"
				+ "\n	G.s.s = G,"
				+ "\n	G.S.r.s=G.R"
				+ "\n}" ;
	}

}
