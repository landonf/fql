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
			"// should have 4 elements. "
					+ "\nschema Dihedral2 = {"
					+ "\n nodes G;"
					+ "\n attributes;"
					+ "\n arrows"
					+ "\n	r: G -> G, "
					+ "\n	s: G -> G;"
					+ "\n equations"
					+ "\n	G.r.r = G,"
					+ "\n	G.s.s = G,"
					+ "\n	G.s.r.s = G.r;"
					+ "\n}"
					+ "\n"
					+ "\n//requires standard left-kan "
					+ "\n/* schema Dihedral2 = {"
					+ "\n nodes G;"
					+ "\n attributes;"
					+ "\n arrows"
					+ "\n	r: G -> G, "
					+ "\n	R0: G -> G, "
					+ "\n	s: G -> G, "
					+ "\n	S0: G -> G;"
					+ "\n equations"
					+ "\n	G.r.R0 = G, "
					+ "\n	G.R0.r = G, "
					+ "\n	G.s.S0 = G, "
					+ "\n	G.S0.s = G,"
					+ "\n	G.r.r = G,"
					+ "\n	G.s.s = G,"
					+ "\n	G.S0.r.s = G.R0;"
					+ "\n} */"
;



}
