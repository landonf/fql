package fql.examples;

public class FullSigmaExample2 extends Example {

	@Override
	public String getName() {
		return "Full Sigma 2";
	}

	@Override
	public String getText() {
		return s;
	}

	String s = "schema T = {" + "\n	nodes s;" + "\n	attributes;" + "\n	arrows;"
			+ "\n	equations;" + "\n}" + "\n" + "\ninstance I = {"
			+ "\n	nodes s->{1,2};" + "\n	attributes;" + "\n	arrows;"
			+ "\n} :  T" + "\n" + "\nschema Loop = {" + "\n	nodes s;"
			+ "\n	attributes;" + "\n	arrows f:s->s;"
			+ "\n	equations s.f.f.f.f.f=s.f.f.f;" + "\n}" + "\n"
			+ "\ninstance J = {" + "\n	nodes s->{1,2,3,4,5,6,7};"
			+ "\n	attributes;"
			+ "\n	arrows f->{(1,2),(2,3),(3,4),(4,3),(5,2), (6,7),(7,6)};"
			+ "\n} :  Loop" + "\n" + "\nmapping F = {" + "\n	nodes s->s;"
			+ "\n	attributes;" + "\n	arrows;" + "\n} : T -> Loop" + "\n"
			+ "\nmapping G = {" + "\n	nodes s->s;" + "\n	attributes;"
			+ "\n	arrows f->s;" + "\n} : Loop -> T" + "\n"
			+ "\ninstance Sigma_FI = SIGMA F I" + "\n"
			+ "\ninstance Sigma_GJ = SIGMA G J"
	;

}
