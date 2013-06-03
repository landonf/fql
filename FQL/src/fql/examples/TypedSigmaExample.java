package fql.examples;


public class TypedSigmaExample extends Example {

	@Override
	public String getName() {
		return "Typed Sigma";
	}

	@Override
	public String getText() {
		return text;
	}
	
	String text  =
			"\nschema C = {"
					+ "\n g1 : a1 -> b1, g2 : a2 -> b2, g3 : a3 -> b2,"
					+ "\n h1 : a1 -> c1, h2 : a2 -> c2, h3 : a3 -> c4,"
					+ "\n c3,"
					+ "\n"
					+ "\n a1_str : a1 -> string,"
					+ "\n a2_str : a2 -> string,"
					+ "\n a3_str : a3 -> string,"
					+ "\n"
					+ "\n b1_str : b1 -> string,"
					+ "\n b2_str : b2 -> string,"
					+ "\n"
					+ "\n c1_str : c1 -> string,"
					+ "\n c2_str : c2 -> string,"
					+ "\n c3_str : c3 -> string,"
					+ "\n c4_str : c4 -> string"
					+ "\n"
					+ "\n;"
					+ "\n}"
					+ "\n"
					+ "\nschema D = {"
					+ "\n G : A -> B, H : A -> C,"
					+ "\n A_str : A -> string,"
					+ "\n B_str : B -> string,"
					+ "\n C_str : C -> string"
					+ "\n;"
					+ "\n}"
					+ "\n"
					+ "\nmapping F : C -> D = {"
					+ "\n  a1 -> A, a2 -> A, a3 -> A,"
					+ "\n  b1 -> B, b2 -> B,"
					+ "\n  c1 -> C, c2 -> C, c3 -> C, c4 -> C,"
					+ "\n  a1_str -> A_str, a2_str -> A_str, a3_str -> A_str,"
					+ "\n  b1_str -> B_str, b2_str -> B_str,"
					+ "\n  c1_str -> C_str, c2_str -> C_str, c3_str -> C_str, c4_str -> C_str"
					+ "\n ; "
					+ "\n  g1 -> A.G, g2 -> A.G, g3 -> A.G,"
					+ "\n  h1 -> A.H, h2 -> A.H, h3 -> A.H"
					+ "\n}"
					+ "\n"
					+ "\n"
					+ "\ninstance I : C = {"
					+ "\n b2 = {(a,a),(b,b),(c,c)};"
					+ "\n b1 = {(d,d),(e,e)};"
					+ "\n b2_str = {(a,a),(b,b),(c,c)};"
					+ "\n b1_str = {(d,d),(e,e)}; "
					+ "\n"
					+ "\n a3 = {(1,1),(2,2)};"
					+ "\n a2 = {(3,3),(4,4),(5,5)};"
					+ "\n a1 = {(6,6)};"
					+ "\n a1_str = {(6, 6)};"
					+ "\n a2_str = {(3,3),(4,4),(5,5)};"
					+ "\n a3_str = {(1,1),(2,2)};"
					+ "\n "
					+ "\n c4 = {(f,f),(g,g)};"
					+ "\n c3 = {(h,h)};"
					+ "\n c2 = {(i,i),(j,j)};"
					+ "\n c1 = {(k,k),(l,l)};"
					+ "\n c4_str = {(f,f),(g,g)};"
					+ "\n c3_str = {(h,h)};"
					+ "\n c2_str = {(i,i),(j,j)};"
					+ "\n c1_str = {(k,k),(l,l)};"
					+ "\n "
					+ "\n g3 = {(1,a),(2,b)};"
					+ "\n g2 = {(3,a),(4,b),(5,c)};"
					+ "\n g1 = {(6,d)};"
					+ "\n"
					+ "\n h3 = {(1,f),(2,g)};"
					+ "\n h2 = {(3,i),(4,j),(5,j)};"
					+ "\n h1 = {(6,k)}"
					+ "\n}"
					+ "\n"
					+ "\ninstance J : D = sigma F I"
					;

}
