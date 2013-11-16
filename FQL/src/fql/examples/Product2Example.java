package fql.examples;

public class Product2Example extends Example {

	@Override
	public String getName() {
		return "Products Inst";
	}

	@Override
	public String getText() {
		return s;
	}
	
	String s = "schema S = {"
			+ "\n	nodes a, b;"
			+ "\n	attributes att : a -> string;"
			+ "\n	arrows f : a -> b;"
			+ "\n	equations;"
			+ "\n}"
			+ "\n"
			+ "\ninstance I = {"
			+ "\nnodes a -> {1,2}, b -> {3};"
			+ "\nattributes att -> {(1,common),(2,common)};"
			+ "\narrows f -> {(1,3),(2,3)};"
			+ "\n} : S"
			+ "\n"
			+ "\ninstance J = {"
			+ "\nnodes a -> {a,b,c}, b -> {d,e};"
			+ "\nattributes att -> {(a,common),(b,common),(c,baz)};"
			+ "\narrows f -> {(a,d),(b,e),(c,e)};"
			+ "\n} : S"
			+ "\n"
			+ "\ninstance A = (I * J)"
			+ "\n"
			+ "\ntransform K = A.fst"
			+ "\n"
			+ "\ntransform L = A.snd"
			+ "\n"
			+ "\ntransform M = A.(K * L) //is id"
			+ "\n"
			+ "\nschema X = {"
			+ "\n	nodes a;"
			+ "\n	attributes;"
			+ "\n	arrows;"
			+ "\n	equations;"
			+ "\n}"
			+ "\n"
			+ "\ninstance N = unit X"
			+ "\n"
			+ "\ntransform O = N.unit N"


;

}
