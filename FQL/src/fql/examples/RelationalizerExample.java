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
	
	String s =
			 "\nschema C={nodes A;attributes att:A->string;arrows f:A->A;equations A.f.f.f.f=A.f.f;}"
			+ "\n"
			+ "\ninstance I ={"
			+ "\n	nodes A->{1,2,3,4,5,6,7};"
			+ "\n	attributes att->{(1,1),(2,2),(3,3),(4,1),(5,5),(6,3),(7,5)};"
			+ "\n	arrows f->{(1,2),(2,3),(3,5),(4,2),(5,3),(6,7),(7,6)};"
			+ "\n	} : C"
			+ "\n"
			+ "\ninstance RelI=relationalize I"
			+ "\n"
			+ "\ntransform trans = RelI.relationalize"
			+ "\n"
			+ "\ninstance J ={"
			+ "\n	nodes A->{1,2,3,4,5};"
			+ "\n	attributes att->{(1,1),(2,2),(3,3),(4,1),(5,5)};"
			+ "\n	arrows f->{(1,2),(2,3),(3,5),(4,2),(5,3)};"
			+ "\n	} : C"
			+ "\n"
			+ "\ninstance RelJ=relationalize J"
			+ "\n"
			+ "\ntransform t = {"
			+ "\n	nodes A -> {(1,1),(2,2),(3,3),(4,4),(5,5)};"
			+ "\n} : J -> I "
			+ "\n"
			+ "\ntransform h = relationalize RelJ RelI t"


;



}
