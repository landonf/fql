package fql.examples;

public class TriangleExample extends Example {

	@Override
	public String getName() {
		return "Triangle";
	}

	@Override
	public String getText() {
		return s;
	}

	String s = 
			"schema C = {"
					+ "\n nodes "
					+ "\n 	A, B, C;"
					+ "\n attributes;"
					+ "\n arrows "
					+ "\n 	a : A -> B, b : B -> C, c : C -> A; "
					+ "\n equations"
					+ "\n	A.a.b.c = A,"
					+ "\n	B.b.c.a = B,"
					+ "\n	C.c.a.b = C;"
					+ "\n}\n";
}
