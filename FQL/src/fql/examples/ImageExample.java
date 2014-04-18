package fql.examples;

public class ImageExample extends Example {

	@Override
	public String getName() {
		return "Image";
	}

	@Override
	public String getText() {
		return s;
	}
	
	String s = "/*"
			+ "\n * Let A and B be sets and F : A -> B a function.  "
			+ "\n * "
			+ "\n * The pre-image of F is a function P(B) -> P(A) defined by"
			+ "\n *   J -> { a | F(a) in J }"
			+ "\n * "
			+ "\n * The direct-image of F is a function P(A) -> P(B) defined by"
			+ "\n *   I -> { b | b = F(x), for some x in I }"
			+ "\n * "
			+ "\n * The dual-image of F is a function P(A) -> P(B) defined by"
			+ "\n *   I -> { b | b = F(x), for all x in I }"
			+ "\n */"
			+ "\n "
			+ "\n/*"
			+ "\n * We can encode a set {e1,e2,...,eN} as a schema with nodes e1,e2,...,eN."
			+ "\n * A function from set A to set B is encoded as a mapping from A to B."
			+ "\n * A subset of {e1,e2,...,eN} is encoded as an instance, where each node has either 0 or 1 elements."
			+ "\n *   "
			+ "\n * Under this encoding, "
			+ "\n *    delta is pre-image, "
			+ "\n *    sigma is direct-image,   "
			+ "\n *    pi is dual-image."
			+ "\n */"
			+ "\n"
			+ "\n// Example"
			+ "\n"
			+ "\n// {m1, m2, n}"
			+ "\nschema A = {"
			+ "\n	nodes m1, m2, n;"
			+ "\n	attributes;"
			+ "\n	arrows;"
			+ "\n	equations;"
			+ "\n}"
			+ "\n"
			+ "\n// {m, n, o}"
			+ "\nschema B = {"
			+ "\n	nodes m, n, o;"
			+ "\n	attributes;"
			+ "\n	arrows;"
			+ "\n	equations;"
			+ "\n}"
			+ "\n"
			+ "\n// m1 -> m, m2 -> m, n -> n"
			+ "\nmapping F = {"
			+ "\n	nodes m1 -> m, m2 -> m, n -> n;"
			+ "\n	attributes;"
			+ "\n	arrows;"
			+ "\n} : A -> B"
			+ "\n"
			+ "\n// {m1, n}"
			+ "\ninstance I = {"
			+ "\n	nodes m1 -> { present }, m2 -> { }, n -> { present } ;"
			+ "\n	attributes;"
			+ "\n	arrows;"
			+ "\n} : A"
			+ "\n"
			+ "\n// {m, n}"
			+ "\ninstance temp1 = sigma F I"
			+ "\ninstance direct_imFI = relationalize temp1"
			+ "\n"
			+ "\n// {n, o}"
			+ "\ninstance temp2 = pi F I"
			+ "\ninstance dual_imFI = relationalize temp2"
			+ "\n"
			+ "\n// {m, o}"
			+ "\ninstance J = {"
			+ "\n	nodes m -> { present }, n -> { }, o -> { present };"
			+ "\n	attributes;"
			+ "\n	arrows;"
			+ "\n} : B "
			+ "\n"
			+ "\n// {m1, m2}"
			+ "\ninstance temp3 = delta F J"
			+ "\ninstance pre_imFJ = relationalize temp3"
	+ "\n"
	+ "\n/*"
	+ "\nWritten in terms of adjunctions, we have"
	+ "\n"
	+ "\nimage(I) subseteq J"
	+ "\n----------------------"
	+ "\nI subseteq preimage(J)"
	+ "\n"
	+ "\npreimage(J) subseteq I"
	+ "\n----------------------"
	+ "\nJ subseteq dualimage(I)"
	+ "\n*/";
	




}
