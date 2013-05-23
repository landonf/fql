package fql.examples;

public class CyclicGroupExample extends Example {

	@Override
	public String getName() {
		return "Cyclic";
	}

	@Override
	public String getText() {
		return "schema C = { f : A -> A ; " +

"A.f.f.f = A.f } \n";
	}

}
