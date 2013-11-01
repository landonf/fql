package fql.examples;

public class RelationalizerExample extends Example {

	@Override
	public String getName() {
		return "Relationalize";
	}

	@Override
	public String getText() {
		return s;
	}
	
	String s ="//Illustrates the paper's relationalize operation\n"
			+ "\nschema C={nodes A;attributes a:A->string;arrows f:A->A;equations A.f.f.f.f=A.f.f;}"
			+ "\n"
			+ "\ninstance I:C={"
			+ "\n	nodes A->{1,2,3,4,5,6,7};"
			+ "\n	attributes a->{(1,1),(2,2),(3,3),(4,1),(5,5),(6,3),(7,5)};"
			+ "\n	arrows f->{(1,2),(2,3),(3,5),(4,2),(5,3),(6,7),(7,6)};"
			+ "\n	} C"
			+ "\n"
			+ "\ninstance RelI:C=relationalize I";



}
