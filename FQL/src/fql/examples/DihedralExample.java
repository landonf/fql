package fql.examples;

import fql.examples.Example;

public class DihedralExample extends Example {

	@Override
	public String getName() {
		return "Dihedral 2";
	}

	@Override
	public String getText() {
		return s;
	}

	String s =
			"// should have 4 elements "
					+ "\nschema Dihedral2 = {"
					+ "\n nodes G;"
					+ "\n attributes;"
					+ "\n arrows"
					+ "\n	r: G -> G, "
					+ "\n	R: G -> G, "
					+ "\n	s: G -> G, "
					+ "\n	S: G -> G;"
					+ "\n equations"
					+ "\n	G.r.R = G, "
					+ "\n	G.R.r = G, "
					+ "\n	G.s.S = G, "
					+ "\n	G.S.s = G,"
					+ "\n	G.r.r = G,"
					+ "\n	G.s.s = G,"
					+ "\n	G.S.r.s = G.R;"
					+ "\n}\n";

}
