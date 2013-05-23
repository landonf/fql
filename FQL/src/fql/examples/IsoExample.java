package fql.examples;

public class IsoExample extends Example {

	@Override
	public String getName() {
		return "Isomorphism";
	}

	@Override
	public String getText() {
		// TODO Auto-generated method stub
		return "schema C = { f : A -> B , g : B -> A ;  A.f.g = A, B.g.f = B }\n";
	}

}
