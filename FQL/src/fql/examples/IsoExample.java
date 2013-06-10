package fql.examples;

public class IsoExample extends Example {

	@Override
	public String getName() {
		return "Isomorphism";
	}

	@Override
	public String getText() {
		return s; 
	}

	String s = "schema C = { "
			+ "\n nodes A, B;"
			+ "\n attributes;"
			+ "\n arrows f : A -> B , g : B -> A;"
			+ "\n equations A.f.g = A, B.g.f = B; "
			+ "\n}";

}
