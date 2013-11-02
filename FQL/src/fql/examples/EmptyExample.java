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
//					+ "\nmapping F : C -> C = id C"
	//				+ "\nmapping G : C -> C = F then F"
					+ "\nmapping F = { nodes; attributes; arrows;} : C -> C"
					+ "\n"
//					+ "\nquery q : C -> C = delta F pi F sigma F"
	//				+ "\nquery p : C -> C = q then q"
		//			+ "\n"
					+ "\ninstance I  = { nodes; attributes; arrows; } : C"
					+ "\ninstance I1  = delta F I"
					+ "\ninstance I2  = pi F I"
					+ "\ninstance I3  = sigma F I"
					+ "\ninstance I4  = relationalize I"
					+ "\ninstance I5  = SIGMA F I"
					+ "\ninstance I6  = external C name\n";
//					+ "\ninstance I7 : C = eval q I";


}
