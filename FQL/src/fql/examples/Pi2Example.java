package fql.examples;

public class Pi2Example extends Example {

	@Override
	public String getName() {
		return "Pi";
	}

	@Override
	public String getText() {
		return s;
	}
	
	String s = "schema C = { "
			+ "\n nodes C1, C2; "
			+ "\n attributes;"
			+ "\n arrows c : C1 -> C2 , cc : C1 -> C2; "
			+ "\n equations; "
			+ "\n}"
			+ "\n"
			+ "\ninstance I : C = {"
			+ "\n nodes "
			+ "\n	C1 -> {c1A,c1B,c1C},"
			+ "\n	C2 -> {c2x,c2y};"
			+ "\n attributes;"
			+ "\n arrows"
			+ "\n	c -> {(c1A,c2x),(c1B,c2x),(c1C,c2x)},"
			+ "\n	cc -> {(c1A,c2x),(c1B,c2x),(c1C,c2y)};"
			+ "\n}"
			+ "\n"
			+ "\nschema D = {"
			+ "\n nodes"
			+ "\n 	D1, D2, D3;"
			+ "\n attributes;"
			+ "\n arrows"
			+ "\n 	d : D1 -> D2 , "
			+ "\n 	dd : D1 -> D2, "
			+ "\n 	ddd : D2 -> D3 ; "
			+ "\n equations;"
			+ "\n }"
			+ "\n"
			+ "\nmapping F : C -> D = {"
			+ "\n nodes"
			+ "\n  C1 -> D1,"
			+ "\n  C2 -> D3;"
			+ "\n attributes;"
			+ "\n arrows"
			+ "\n  c -> D1.d.ddd,"
			+ "\n  cc -> D1.dd.ddd;"
			+ "\n}"
			+ "\n"
			+ "\ninstance J : D = pi F I";


}
