package fql.examples;

public class ExponentialExample extends Example {

	@Override
	public String getName() {
		return "Exponentials";
	}

	@Override
	public String getText() {
		return s;
	}

	String s = "schema A = {"
			+ "\n	nodes a1, a2;"
			+ "\n	attributes;"
			+ "\n	arrows af : a1 -> a2;"
			+ "\n	equations;"
			+ "\n}"
			+ "\n"
			+ "\nschema B = {"
			+ "\n	nodes b1, b2, b3;"
			+ "\n	attributes;"
			+ "\n	arrows bf1 : b1 -> b2, bf2 : b2 -> b3;"
			+ "\n	equations;"
			+ "\n}"
			+ "\n"
			+ "\nschema S = (A^B)"
			+ "\n"
			+ "\nmapping eta = curry eval A B // (= id)"
			+ "\n"
			+ "\nmapping F = unit {} (A*B) //can use any F for beta, we choose this one"
			+ "\n"
			+ "\nmapping beta = (  ((fst A B then curry F) * (snd A B then id B)) then eval unit {} B  ) // (= F)"
;



}
