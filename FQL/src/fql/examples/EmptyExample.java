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
	"schema C = {;}"
	+ "\n"
	+ "\ninstance I : C = { ; }"
	+ "\n"
	+ "\nmapping F : C -> C = id C"
	+ "\n"
	+ "\ninstance I1 : C = delta F I"
	+ "\n"
	+ "\ninstance I2 : C = pi F I"
	+ "\n"
	+ "\ninstance I3 : C = sigma F I"
	+ "\n"
	+ "\nquery q : C -> C = delta F pi F sigma F"
	+ "\nquery p : C -> C = q then q";
}
