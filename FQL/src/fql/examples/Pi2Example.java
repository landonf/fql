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
	
	String s = "schema C = { c : C1 -> C2 , cc : C1 -> C2; }"
			+ "\n"
			+ "\ninstance I : C = {"
			+ "\nC1 = {(c1A,c1A),(c1B,c1B),(c1C,c1C)},"
			+ "\nC2 = {(c2x,c2x),(c2y,c2y)},"
			+ "\nc = {(c1A,c2x),(c1B,c2x),(c1C,c2x)},"
			+ "\ncc = {(c1A,c2x),(c1B,c2x),(c1C,c2y)}"
			+ "\n}"
			+ "\n"
			+ "\nschema D = { d : D1 -> D2 , dd : D1 -> D2, ddd : D2 -> D3 ; }"
			+ "\n"
			+ "\nmapping F : C -> D = {"
			+ "\n  C1 -> D1,"
			+ "\n  C2 -> D3;"
			+ "\n  c -> D1.d.ddd,"
			+ "\n  cc -> D1.dd.ddd"
			+ "\n}"
			+ "\n"
			+ "\ninstance J : D = pi F I";

}
