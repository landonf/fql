package fql.examples;

public class ProductExample extends Example {

	@Override
	public String getName() {
		return "Products";
	}

	@Override
	public String getText() {
		return s;
	}
	
	String s = "schema S = {"
			+ "\n	nodes a, b, c;"
			+ "\n	attributes att:a->string;"
			+ "\n	arrows f:a->b, g:b->c, h:a->c;"
			+ "\n	equations a.h = a.f.g;"
			+ "\n}"
			+ "\n"
			+ "\nschema T = {"
			+ "\n	nodes x, y;"
			+ "\n	attributes att:x->string;"
			+ "\n	arrows u:x->y, z:x->y;"
			+ "\n	equations x.u = x.z;"
			+ "\n}"
			+ "\n"
			+ "\nmapping F = {"
			+ "\n	nodes x -> a, y -> c;"
			+ "\n	attributes att->att;"
			+ "\n	arrows u -> a.f.g, z->a.f.g;"
			+ "\n} : T -> S "
			+ "\n"
			+ "\nschema A = (S * T)"
			+ "\n"
			+ "\nmapping p1 = fst S T"
			+ "\nmapping p2 = snd S T"
			+ "\n"
			+ "\nmapping p = (p1*p2) //is identity"
			+ "\n";


}
