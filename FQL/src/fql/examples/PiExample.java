package fql.examples;

public class PiExample extends Example {

	@Override
	public String getName() {
		return "Pi id";
	}

	@Override
	public String getText() {
		return piDefinitions;
	}
	
	public static final String piDefinitions = 
			"schema C = { c : C1 -> C2 ; }\n" +
					"\n" +
					"instance I : C = {\n" +
					"C1 = {c1A,c1B},\n" +
					"C2 = {c2};\n" +
					"c = {(c1A,c2),(c1B,c2)}\n" +
					"}\n" +
					"\n"+
					"mapping idC : C -> C = id C\n" +
					"\n" + 
					"instance J : C = pi idC I\n\n"
					+ "\n\n\n/*"
					+ "\nExpected output:"
					+ "\n"
					+ "\nJ = {"
					+ "\n  c = { (c2^c2^c1A,c2), (c2^c2^c1B,c2) };"
					+ "\n  C1 = { (c2^c2^c1B,c2^c2^c1B), (c2^c2^c1A,c2^c2^c1A) };"
					+ "\n  C2 = { (c2,c2) }"
					+ "\n}"
					+ "\n*/\n";
	

}
