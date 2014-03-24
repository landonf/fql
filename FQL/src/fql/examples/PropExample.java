package fql.examples;

public class PropExample extends Example {

	@Override
	public String getName() {
		return "Prop";
	}

	@Override
	public String getText() {
		return s;
	}
	
	String s = "schema C = {"
			+ "\n	nodes a, b;"
			+ "\n	attributes;"
			+ "\n	arrows f : a -> b;"
			+ "\n	equations;"
			+ "\n}"
			+ "\n"
			+ "\ninstance one = unit C"
			+ "\ninstance prp = prop C"
			+ "\n"
			+ "\ntransform t1 = prp.true one"
			+ "\ntransform t2 = prp.false one"
			+ "\n"
			+ "\ninstance I = {"
			+ "\n	nodes a -> {1,2}, b -> {3};"
			+ "\n	attributes;"
			+ "\n	arrows f -> {(1,3),(2,3)};"
			+ "\n} : C"
			+ "\n"
			+ "\ninstance J = {"
			+ "\n	nodes a -> {x}, b -> {y};"
			+ "\n	attributes;"
			+ "\n	arrows f -> {(x,y)};"
			+ "\n} : C "
			+ "\n"
			+ "\ntransform trans = {"
			+ "\n	nodes a -> {(1,x),(2,x)}, b -> {(3,y)};"
			+ "\n} : I -> J "
			+ "\n"
			+ "\ntransform t3 = prp.chi trans "
			+ "\n"
			+ "\n//these two transforms are equal - f ; chi f = ! ; true "
			+ "\ntransform lhs = (trans then t3)"
			+ "\ntransform rhs = (one.unit I then prp.true one)";




}
