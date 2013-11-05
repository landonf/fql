package fql.examples;

public class EmptyExample extends Example {

	@Override
	public String getName() {
		return "All Syntax";
	}

	@Override
	public String getText() {
		return s;
	}

	String s = 
			"//illustrates all possible FQL declarations"
					+ "\n"
					+ "\nschema C = {nodes; attributes; arrows; equations;}"
					+ "\nschema C1 = void"
					+ "\nschema C2 = (C + C)"
					+ "\n"
					+ "\nmapping F = id C"
					+ "\nmapping F1 = (F then F)"
					+ "\nmapping F2 = {nodes; attributes; arrows;} : C -> C"
					+ "\nmapping F3 = inl C C"
					+ "\nmapping F4 = inr C C"
					+ "\nmapping F5 = (F3 + F4)"
					+ "\n"
					+ "\nquery q = delta F pi F sigma F"
					+ "\nquery p = (q then q)"
					+ "\n"
					+ "\ninstance I  = { nodes; attributes; arrows; } : C"
					+ "\ninstance I1  = delta F I"
					+ "\ninstance I2  = pi F I"
					+ "\ninstance I3  = sigma F I"
					+ "\ninstance I4  = relationalize I"
					+ "\ninstance I5  = SIGMA F I"
					+ "\ninstance I6  = external C name\n"
					+ "\ninstance I7  = eval q I";


}
