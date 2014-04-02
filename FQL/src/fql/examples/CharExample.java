package fql.examples;

public class CharExample extends Example {

	@Override
	public String getName() {
		return "Characteristic Fn";
	}

	@Override
	public String getText() {
		return s;
	}

	String s = "//our schema S has one node, so an instance on S will just be a set."
			+ "\nschema S = {"
			+ "\n	nodes n;"
			+ "\n	attributes;"
			+ "\n	arrows;"
			+ "\n	equations;"
			+ "\n}"
			+ "\n"
			+ "\n//the set {true, false}.  "
			+ "\n//For sets, any 2-element set is a sub-object classifier."
			+ "\ninstance P = prop S // has n -> {true, false} . "
			+ "\n"
			+ "\n//the set { tt }."
			+ "\n//For sets, any 1-element set is a terminal instance"
			+ "\ninstance terminal = unit S // has n -> { tt } .  "
			+ "\n"
			+ "\n//truth values are transforms terminal -> P"
			+ "\ntransform tru = P.true terminal // maps tt -> true   "
			+ "\ntransform fals = P.false terminal // maps tt -> false"
			+ "\n"
			+ "\n//the set {1,2,3,4}"
			+ "\ninstance J = {"
			+ "\n	nodes n -> {1,2,3,4};"
			+ "\n	attributes;"
			+ "\n	arrows;"
			+ "\n} : S"
			+ "\n"
			+ "\n//the set {a,b,c}"
			+ "\ninstance I = {"
			+ "\n	nodes n -> {a,b,c};"
			+ "\n	attributes;"
			+ "\n	arrows;"
			+ "\n} : S"
			+ "\n"
			+ "\n//the injective function a -> 1, b -> 2, c -> 3"
			+ "\ntransform f = {"
			+ "\n	nodes n -> {(a,1),(b,2),(c,3)};"
			+ "\n} : I -> J"
			+ "\n"
			+ "\n//the characteristic function J -> prop"
			+ "\n//maps 1 -> true, 2 -> true, 3 -> true, 4 -> false"
			+ "\ntransform char_f = P.char f "
			+ "\n"
			+ "\n//these two transforms I -> prop are equal"
			+ "\n//they map a -> true, b -> true, c -> true"
			+ "\ntransform rhs = (terminal.unit I then tru) "
			+ "\ntransform lhs = (f then char_f)";




}
