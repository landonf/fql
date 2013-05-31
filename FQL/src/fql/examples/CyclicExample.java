package fql.examples;

public class CyclicExample extends Example {

	@Override
	public String getName() {
		return "Cyclic";
	}

	@Override
	public String getText() {
		return str;
	}

	String str = "schema CM53 = {"
			+ "\n  arr : M -> M"
			+ "\n ;"
			+ "\n  M.arr.arr.arr.arr.arr = M.arr.arr"
			+ "\n}"
			+ "\n"
			+ "\nschema CM33 = {"
			+ "\n  a : J -> J"
			+ "\n ;"
			+ "\n  J.a.a.a = J" 
			+ "\n}"
			+ "\n"
			+ "\nmapping F : CM53 -> CM33 = {"
			+ "\n  (M,J)" 
			+ "\n ;"
			+ "\n  (arr,J.a)"
			+ "\n}"
	+ "\n\ninstance A53 : CM53 = {"
	+ "\n M = { (mm1,mm1),(mm2,mm2),(mmm,mmm),(m,m)};"
	+ "\n arr = { (mm1,mm2), (mm2,m), (mmm,m),(m,m)}"
	+ "\n}"
	+ "\n"
	+ "\ninstance A33 : CM33 = {"
	+ "\n J = { (m1,m1), (m2,m2), (m3,m3)};"
	+ "\n a = { (m1,m2), (m2,m3), (m3,m1)}"
	+ "\n}"
	+ "\n\ninstance Inst1 = delta F A33"
	+ "\n"
	+ "\ninstance Inst2 = pi F A53";
}
