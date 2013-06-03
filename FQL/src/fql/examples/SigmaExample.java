package fql.examples;

public class SigmaExample extends Example {

	@Override
	public String getName() {
		return "Sigma";
	}

	@Override
	public String getText() {
		return migrationDefinitions;
	}
	
	public static final String migrationDefinitions = "schema C = {" +
			"\n g1 : a1 -> b1, g2 : a2 -> b2, g3 : a3 -> b2," +
			"\n h1 : a1 -> c1, h2 : a2 -> c2, h3 : a3 -> c4," +
			"\n c3;" + 
			"\n}" +
			"\n" +
			"\nschema D = {" +
			"\n G : A -> B, H : A -> C;" +
			"\n}" +
			"\n" +
			"\nmapping F : C -> D = {" +
			"\n  a1 -> A, a2 -> A, a3 -> A," +
			"\n  b1 -> B, b2 -> B, " +
			"\n  c1 -> C, c2 -> C, c3 -> C, c4 -> C" +
			"\n ; " +
			"\n  g1 -> A.G, g2 -> A.G, g3 -> A.G," +
			"\n  h1 -> A.H, h2 -> A.H, h3 -> A.H" +
			"\n}\n\n" + 
			"\ninstance I : C = {"+
			"\n b2 = {(a,a),(b,b),(c,c)};"+
			"\n b1 = {(d,d),(e,e)};"+
			"\n "+
			"\n a3 = {(1,1),(2,2)};"+
			"\n a2 = {(3,3),(4,4),(5,5)};"+
			"\n a1 = {(6,6)};"+
			"\n "+
			"\n c4 = {(f,f),(g,g)};"+
			"\n c3 = {(h,h)};"+
			"\n c2 = {(i,i),(j,j)};"+
			"\n c1 = {(k,k),(l,l)};"+
			"\n "+
			"\n g3 = {(1,a),(2,b)};"+
			"\n g2 = {(3,a),(4,b),(5,c)};"+
			"\n g1 = {(6,d)};"+
			"\n "+
			"\n h3 = {(1,f),(2,g)};"+
			"\n h2 = {(3,i),(4,j),(5,j)};"+
			"\n h1 = {(6,k)}"+
			"\n}\n\n" 
		//	+ "mapping idC = id C\nquery q = project idC join idC union F\n"
			+ "instance J : D = sigma F I\n"
	+ "\n\n\n/*\nExpected output:"
	+ "\nJ = {"
	+ "\n  G = { (a3_2,b2_b), (a2_4,b2_b), (a3_1,b2_a), (a2_5,b2_c), (a1_6,b1_d), (a2_3,b2_a) };"
	+ "\n  A = { (a3_1,a3_1), (a2_5,a2_5), (a3_2,a3_2), (a2_4,a2_4), (a1_6,a1_6), (a2_3,a2_3) };"
	+ "\n  B = { (b1_d,b1_d), (b1_e,b1_e), (b2_c,b2_c), (b2_a,b2_a), (b2_b,b2_b) };"
	+ "\n  C = { (c2_i,c2_i), (c1_l,c1_l), (c1_k,c1_k), (c3_h,c3_h), (c4_g,c4_g), (c2_j,c2_j), (c4_f,c4_f) };"
	+ "\n  H = { (a2_5,c2_j), (a3_1,c4_f), (a3_2,c4_g), (a1_6,c1_k), (a2_3,c2_i), (a2_4,c2_j) } "
	+ "\n  }\n*/\n";


}
