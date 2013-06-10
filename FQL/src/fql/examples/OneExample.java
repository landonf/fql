package fql.examples;

import fql.examples.Example;

public class OneExample extends Example {

	@Override
	public String getName() {
		return "One";
	}

	@Override
	public String getText() {
		return s;
	}

	String s = 
			"schema C = { nodes A; attributes; arrows arr : A -> A ; equations; }"
					+ "\n"
					+ "\nmapping F : C -> C = {"
					+ "\n nodes A -> A;"
					+ "\n attributes;"
					+ "\n arrows arr -> A.arr;"
					+ "\n}"
					+ "\n"
					+ "\ninstance I : C = {"
					+ "\n nodes A -> {x,y};"
					+ "\n attributes;"
					+ "\n arrows arr -> {(x,y),(y,x)};"
					+ "\n}";

}
