package fql.examples;

import fql.examples.Example;


public class ExternalExample extends Example {

	@Override
	public String getName() {
		return "External";
	}

	@Override
	public String getText() {
		return s;
	}

	String s = 
			"//The keyword 'external' can be used to suppress CREATE TABLE statements"
					+ "\n//in the generated SQL output.  Hence, the generated output can be run"
					+ "\n//on pre-existing database intances."
					+ "\n"
					+ "\nschema C = { nodes T; attributes; arrows; equations; }"
					+ "\n"
					+ "\n// generates CREATE TABLE I_T(c1 VARCHAR(128), c0 VARCHAR(128));"
					+ "\ninstance I : C = { nodes T -> {}; attributes; arrows; }"
					+ "\n"
					+ "\n// generates nothing"
					+ "\ninstance J : C = external"
					+ "\n"
					+ "\nmapping F : C -> C = id C"
					+ "\n"
					+ "\n/* generates"
					+ "\nCREATE TABLE K_T(c1 VARCHAR(128), c0 VARCHAR(128));"
					+ "\nINSERT INTO K_T SELECT * FROM J_T;"
					+ "\n*/"
					+ "\ninstance K : C = delta F J";

}
