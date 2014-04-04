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
			+ "\n"
			+ "\n// exponentials of instances below /////////////////////////////////////"
			+ "\n"
			+ "\nenum dom = {foo, bar, baz}"
			+ "\n"
			+ "\nschema C = {"
			+ "\n         nodes a, b;"
			+ "\n         attributes att : a -> dom;"
			+ "\n         arrows f : a -> b;"
			+ "\n         equations;"
			+ "\n}"
			+ "\n"
			+ "\ninstance I = {"
			+ "\n         nodes a -> {1,2,3}, b -> {4,5};"
			+ "\n         attributes att -> {(1,foo),(2,bar),(3,baz)};"
			+ "\n         arrows f -> {(1,4),(2,5),(3,5)};"
			+ "\n} : C"
			+ "\n"
			+ "\ninstance J = {"
			+ "\n         nodes a -> {1,2}, b -> {4};"
			+ "\n         attributes att -> {(1,foo),(2,baz)};"
			+ "\n         arrows f -> {(1,4),(2,4)};"
			+ "\n} : C"
			+ "\n"
			+ "\ninstance K = (J^I)"
			+ "\n"
			+ "\ninstance M = (K*I) "
			+ "\n"
			+ "\ntransform trans = M.eval"
			+ "\n"
			+ "\ntransform idx = K.curry trans //eta"
			+ "\n "
			+ "\n// beta below here"
			+ "\ninstance one = unit C"
			+ "\ninstance ab = (I * J)"
			+ "\ninstance oneB = (one ^ J)"
			+ "\ninstance oneBB = (oneB * J)"
			+ "\n"
			+ "\ntransform G = one.unit ab "
			+ "\n"
			+ "\ntransform curryF = oneB.curry G"
			+ "\n"
			+ "\ntransform tr = (oneBB.( (ab.fst then curryF) * ab.snd) then oneBB.eval) //beta (= G)";

}
