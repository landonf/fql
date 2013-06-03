package fql.examples;

public class CompositionExample extends Example {

	@Override
	public String getName() {
		return "Composition";
	}

	@Override
	public String getText() {
		return compDefinitions;
	}

	
	public static String compDefinitions ="schema S = { s ; }" +
			"\nschema T = { t ; }" +
			"\nschema B = { b1,b2 ; }" +
			"\nschema A = { a1,a2,a3 ; }" +
			"\n" +
			"\nmapping s : B -> S = { b1 -> s, b2 -> s ; }" +
			"\nmapping f : B -> A = { b1 -> a1, b2 -> a2 ; }" +
			"\nmapping t : A -> T = { a1 -> t, a2 -> t, a3 -> t ; }" +
			"\n" +
			"\nquery q1 = delta s pi f sigma t" +
			"\n" +
			"\nschema D = { d1,d2 ; }" +
			"\nschema C = { c ; }" +
			"\nschema U = { u ; }" +
			"\n" +
			"\nmapping u : D -> T = { d1 -> t, d2 -> t ; }\n" +
			"\nmapping g : D -> C = { d1 -> c, d2 -> c ; }\n" +
			"\nmapping v : C -> U = { c -> u ; }\n" +
			"\n" + 
			"query q2 = delta u pi g sigma v" +
			"\nquery q = q2 o q1";


}
