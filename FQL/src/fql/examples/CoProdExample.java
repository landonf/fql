package fql.examples;

public class CoProdExample extends Example {

	@Override
	public String getName() {
		return "Co-products";
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
			+ "\nattributes att -> {(1,one),(2,two)};"
			+ "\narrows f -> {(1,3),(2,3)};"
			+ "\n} : S"
			+ "\n"
			+ "\ninstance J = {"
			+ "\nnodes a -> {a,b,c}, b -> {d,e};"
			+ "\nattributes att -> {(a,foo),(b,bar),(c,baz)};"
			+ "\narrows f -> {(a,d),(b,e),(c,e)};"
			+ "\n} : S"
			+ "\n"
			+ "\ninstance A = (I + J)"
			+ "\n"
			+ "\ntransform K = A.inl"
			+ "\n"
			+ "\ntransform L = A.inr"
			+ "\n"
			+ "\ntransform M = A.(K + L) //is id"
			+ "\n"
			+ "\ninstance N = void S"
			+ "\n"
			+ "\ntransform O = N.void J"
			+ "\n"
			+ "\nschema C = { nodes a, b; attributes att : a -> string; arrows f : a -> b, g : a -> a; equations a.g = a; }"
			+ "\n"
			+ "\nschema D = { nodes a; attributes att : a -> string; arrows ; equations ; }"
			+ "\n"
			+ "\nschema E = (C + D)"
			+ "\n"
			+ "\nmapping f = inl C D"
			+ "\n"
			+ "\nmapping g = inr C D"
			+ "\n"
			+ "\nmapping h = (f + g) // this is actually the identity!"
			+ "\n"
			+ "\nschema X = void"
			+ "\n"
			+ "\nschema Y = ((C + (C + C)) + void)"
			+ "\n"
			+ "\nmapping q = void C";




}
