package fql.examples;

public class EmptyExample extends Example {

	@Override
	public String getName() {
		return "Empty";
	}

	@Override
	public String getText() {
		return s;
	}

	String s = 
			"//illustrates all possible FQL declarations"
					+ "\n"
					+ "\nschema C = { nodes; attributes; arrows; equations; }"
					+ "\n"
					+ "\nmapping F : C -> C = id C"
					+ "\nmapping G : C -> C = F then F"
					+ "\nmapping H : C -> C = { nodes; attributes; arrows;}"
					+ "\n"
					+ "\nquery q : C -> C = delta F pi F sigma F"
					+ "\nquery p : C -> C = q then q"
					+ "\n"
					+ "\ninstance I : C = { nodes; attributes; arrows; }"
					+ "\ninstance I1 : C = delta F I"
					+ "\ninstance I2 : C = pi F I"
					+ "\ninstance I3 : C = sigma F I"
					+ "\ninstance I4 : C = relationalize I"
					+ "\ninstance I5 : C = SIGMA F I"
					+ "\ninstance I6 : C = external"
					+ "\ninstance I7 : C = eval q I";


}
