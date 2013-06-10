package fql.examples;

public class CompositionExample extends Example {

	@Override
	public String getName() {
		return "Composition";
	}

	@Override
	public String getText() {
		return s;
	}

	
	public static String s =
			"schema S = { nodes s ; attributes; arrows; equations; }"
					+ "\nschema T = { nodes t ; attributes; arrows; equations; }"
					+ "\nschema B = { nodes b1,b2; attributes; arrows; equations; }"
					+ "\nschema A = { nodes a1,a2,a3; attributes; arrows; equations; }"
					+ "\n"
					+ "\nmapping s : B -> S = { nodes b1 -> s, b2 -> s; attributes; arrows; }"
					+ "\nmapping f : B -> A = { nodes b1 -> a1, b2 -> a2 ; attributes; arrows; }"
					+ "\nmapping t : A -> T = { nodes a1 -> t, a2 -> t, a3 -> t ; attributes; arrows; }"
					+ "\n"
					+ "\nquery q1 : S -> T = delta s pi f sigma t"
					+ "\n"
					+ "\nschema D = { nodes d1,d2 ;  attributes; arrows; equations; }"
					+ "\nschema C = { nodes c ;  attributes; arrows; equations; }"
					+ "\nschema U = { nodes u ;  attributes; arrows; equations;}"
					+ "\n"
					+ "\nmapping u : D -> T = { nodes d1 -> t, d2 -> t ;  attributes; arrows;}"
					+ "\n"
					+ "\nmapping g : D -> C = { nodes d1 -> c, d2 -> c ;  attributes; arrows;}"
					+ "\n"
					+ "\nmapping v : C -> U = { nodes c -> u ;  attributes; arrows; }"
					+ "\n"
					+ "\nquery q2 : T -> U = delta u pi g sigma v"
					+ "\nquery q : S -> U = q1 then q2";

			
}
