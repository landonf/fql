package fql.examples;

public class SurjectiveExample extends Example {

	@Override
	public String getName() {
		return "Surjective Pi";
	}

	@Override
	public String getText() {
		return s;
	}
	
	String s = "schema C = {"
			+ "\n	nodes a, b;"
			+ "\n	attributes att1 : a -> string, att2 : b -> string;"
			+ "\n	arrows;"
			+ "\n	equations;"
			+ "\n}"
			+ "\n"
			+ "\nschema D = {"
			+ "\n	nodes c;"
			+ "\n	attributes att : c -> string;"
			+ "\n	arrows;"
			+ "\n	equations;"
			+ "\n}"
			+ "\n"
			+ "\ninstance I = {"
			+ "\n	nodes a -> {1,2,3}, b -> {1,2};"
			+ "\n	attributes att1 -> {(1,alpha), (2,beta), (3,gamma)}, att2 -> {(1,alpha),(2,dlta)};"
			+ "\n	arrows;"
			+ "\n} : C"
			+ "\n"
			+ "\nmapping F = {"
			+ "\n	nodes a -> c, b -> c;"
			+ "\n	attributes att1 -> att, att2 -> att;"
			+ "\n	arrows;"
			+ "\n} : C -> D "
			+ "\n"
			+ "\ninstance J = pi F I";




}
