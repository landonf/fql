package fql.examples;

public class CyclicExample extends Example {

	@Override
	public String getName() {
		return "Cyclic";
	}

	@Override
	public String getText() {
		return s;
	}

	String s = 
			"schema CM53 = {"
					+ "\n nodes M;"
					+ "\n attributes;"
					+ "\n arrows arr : M -> M;"
					+ "\n equations M.arr.arr.arr.arr.arr = M.arr.arr;"
					+ "\n}"
					+ "\n"
					+ "\nschema CM33 = {"
					+ "\n nodes J;"
					+ "\n attributes;"
					+ "\n arrows a : J -> J;"
					+ "\n equations J.a.a.a = J;"
					+ "\n}"
					+ "\n"
					+ "\nmapping F : CM53 -> CM33 = {"
					+ "\n nodes M -> J;"
					+ "\n attributes;"
					+ "\n arrows arr -> J.a;"
					+ "\n}"
					+ "\n"
					+ "\ninstance A53 : CM53 = {"
					+ "\n nodes M -> { mm1,mm2,mmm,m };"
					+ "\n attributes;"
					+ "\n arrows arr -> { (mm1,mm2), (mm2,m), (mmm,m),(m,m) };"
					+ "\n}"
					+ "\n"
					+ "\ninstance A33 : CM33 = {"
					+ "\n nodes J -> { m1, m2 , m3 };"
					+ "\n attributes;"
					+ "\n arrows a -> { (m1,m2), (m2,m3), (m3,m1) };"
					+ "\n}"
					+ "\n"
					+ "\ninstance Inst2 : CM33 = pi F A53";
}