package fql.examples;

public class IsoExample extends Example {

	@Override
	public String getName() {
		return "Isomorphism";
	}

	@Override
	public String getText() {
		return s + t; 
	}

	String s = "schema C = { "
			+ "\n nodes A, B;"
			+ "\n attributes;"
			+ "\n arrows f : A -> B , g : B -> A;"
			+ "\n equations A.f.g = A, B.g.f = B; "
			+ "\n}";
	
	String t = "\n\ninstance I   = {"
			+"\n nodes A -> {1,2}, B -> {a,b};"
			+"\n attributes;"
			+"\n arrows f -> {(1,a),(2,b)}, g -> {(a,1),(b,2)};\n} : C\n";
		

}
