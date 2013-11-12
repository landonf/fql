package fql.examples;

public class SigPlusExample extends Example {

	@Override
	public String getName() {
		return "Co-products";
	}

	@Override
	public String getText() {
		return "schema C = { nodes a, b; attributes att : a -> string; arrows f : a -> b, g : a -> a; equations a.g = a; }"
				+ "\n"
				+ "\nschema D = { nodes a; attributes att : a -> string; arrows ; equations ; }"
				+ "\n" + "\nschema E = (C + D)"
				+ "\n"
				+ "\nmapping f = inl C D"
				+ "\n"
				+ "\nmapping g = inr C D"
				+ "\n"
				+ "\nmapping h = (f + g) // this is actually the identity!"
				+ "\n"
				+ "\nschema X = void"
				+ "\n"
				+ "\nschema Y = ((C + (C + C)) + void)\n"
				+ "\nschema s = {"
				+ "\n	nodes a, b ;"
				+ "\n	attributes;"
				+ "\n	arrows f : a -> b;"
				+ "\n	equations;"
				+ "\n	}"
				+ "\n"
				+ "\ninstance i = unit s"
				+ "\ninstance j = unit s"
				+ "\ninstance k = (i + j)"
				+ "\n";
	}

}
