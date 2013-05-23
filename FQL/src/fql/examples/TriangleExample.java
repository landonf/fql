package fql.examples;

public class TriangleExample extends Example {

	@Override
	public String getName() {
		return "Triangle";
	}

	@Override
	public String getText() {
		return def;
	}

	String def = "schema C = { a : A -> B, b : B -> C, c : C -> A ; " + "\n"
			+ "\nA.a.b.c = A," + "\nB.b.c.a = B," + "\nC.c.a.b = C\n } \n";

}
