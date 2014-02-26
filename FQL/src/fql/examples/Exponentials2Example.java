package fql.examples;

public class Exponentials2Example extends Example {

	@Override
	public String getName() {
		return "Exponentials 2";
	}

	@Override
	public String getText() {
		return s;
	}
	
	String s = "schema D0 = {"
			+ "\n	nodes a0;"
			+ "\n	attributes;"
			+ "\n	arrows;"
			+ "\n	equations;"
			+ "\n}"
			+ "\n"
			+ "\nschema D1 = {"
			+ "\n	nodes a0, a1;"
			+ "\n	attributes;"
			+ "\n	arrows af : a0 -> a1;"
			+ "\n	equations;"
			+ "\n}"
			+ "\n"
			+ "\nschema D2 = {"
			+ "\n	nodes b0, b1, b2;"
			+ "\n	attributes;"
			+ "\n	arrows bf1 : b0 -> b1, bf2 : b1 -> b2;"
			+ "\n	equations;"
			+ "\n}"
			+ "\n"
			+ "\nschema D3 = {"
			+ "\n	nodes b0, b1, b2, b3;"
			+ "\n	attributes;"
			+ "\n	arrows bf1 : b0 -> b1, bf2 : b1 -> b2, bf3: b2->b3;"
			+ "\n	equations;"
			+ "\n}"
			+ "\n"
			+ "\n"
			+ "\n"
			+ "\nschema H0_0=(D0^D0)"
			+ "\nschema H0_1=(D0^D1)"
			+ "\nschema H0_2=(D0^D2)"
			+ "\nschema H0_3=(D0^D3)"
			+ "\n"
			+ "\nschema H1_0=(D1^D0)"
			+ "\nschema H1_1=(D1^D1)"
			+ "\nschema H1_2=(D1^D2)"
			+ "\nschema H1_3=(D1^D3)"
			+ "\n"
			+ "\nschema H2_0=(D2^D0)"
			+ "\nschema H2_1=(D2^D1)"
			+ "\nschema H2_2=(D2^D2)"
			+ "\nschema H2_3=(D2^D3)"
			+ "\n"
			+ "\n"
			+ "\nschema H3_0=(D3^D0)"
			+ "\nschema H3_1=(D3^D1)"
			+ "\nschema H3_2=(D3^D2)"
			+ "\n//schema H3_3=(D3^D3)"
			+ "\n"
			+ "\nmapping F = iso1 H1_2 H3_0"
			+ "\n"
			+ "\nschema Loop43 = {"
			+ "\n	nodes a;"
			+ "\n	attributes;"
			+ "\n	arrows f:a->a;"
			+ "\n	equations a.f.f.f.f=a.f.f.f;"
			+ "\n}"
			+ "\n"
			+ "\nschema Loop32 = {"
			+ "\n	nodes a;"
			+ "\n	attributes;"
			+ "\n	arrows f:a->a;"
			+ "\n	equations a.f.f.f=a.f.f;"
			+ "\n}"
			+ "\n"
			+ "\nschema term = {"
			+ "\n	nodes a;"
			+ "\n	attributes;"
			+ "\n	arrows;"
			+ "\n	equations;"
			+ "\n}"
			+ "\n"
			+ "\nschema Y=(Loop32^term)"
			+ "\nschema X=(Loop43^Loop32)";







}
