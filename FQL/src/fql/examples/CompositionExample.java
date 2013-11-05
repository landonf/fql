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
					+ "\nmapping s = { nodes b1 -> s, b2 -> s; attributes; arrows; } : B -> S "
					+ "\nmapping f = { nodes b1 -> a1, b2 -> a2 ; attributes; arrows; } : B -> A"
					+ "\nmapping t = { nodes a1 -> t, a2 -> t, a3 -> t ; attributes; arrows; } : A -> T"
					+ "\n"
					+ "\nquery q1 = delta s pi f sigma t"
					+ "\n"
					+ "\nschema D = { nodes d1,d2 ;  attributes; arrows; equations; }"
					+ "\nschema C = { nodes c ;  attributes; arrows; equations; }"
					+ "\nschema U = { nodes u ;  attributes; arrows; equations;}"
					+ "\n"
					+ "\nmapping u = { nodes d1 -> t, d2 -> t ;  attributes; arrows;} : D -> T"
					+ "\n"
					+ "\nmapping g = { nodes d1 -> c, d2 -> c ;  attributes; arrows;} : D -> C"
					+ "\n"
					+ "\nmapping v = { nodes c -> u ;  attributes; arrows; } : C -> U "
					+ "\n"
					+ "\nquery q2 = delta u pi g sigma v"
					+ "\nquery q3 = (q1 then q2)";

			
}
