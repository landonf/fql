package fql.examples;

public class Transforms2Example extends Example {

	@Override
	public String getName() {
		return "Transforms2";
	}

	@Override
	public String getText() {
		return s;
	}
	
String s = "schema C = {"
		+ "\n	nodes A;"
		+ "\n	attributes att:A->string;"
		+ "\n	arrows f:A->A;"
		+ "\n	equations A.f.f.f=A.f.f;"
		+ "\n}"
		+ "\n"
		+ "\ninstance I = {"
		+ "\n	nodes A->{1,2,3,4,5};"
		+ "\n	attributes att->{(1,common),(2,common),(3,common),(4,common),(5,common)};"
		+ "\n	arrows f->{(1,2),(2,3),(3,3),(4,2),(5,3)};"
		+ "\n} : C "
		+ "\n"
		+ "\ninstance J = {"
		+ "\n	nodes A->{1,2,3};"
		+ "\n	attributes att->{(1,common),(2,common),(3,common)};"
		+ "\n	arrows f->{(1,2),(2,3),(3,3)};"
		+ "\n} : C "
		+ "\n"
		+ "\n//transform BadTransform = {"
		+ "\n//	nodes A->{(1,1),(2,2),(3,4)};"
		+ "\n//} :  J -> I"
		+ "\n"
		+ "\ntransform GoodTransform1 = {"
		+ "\n	nodes A->{(1,1),(2,2),(3,3)};"
		+ "\n} :  J -> I"
		+ "\n"
		+ "\ntransform GoodTransform2 = {"
		+ "\n	nodes A->{(1,1),(2,2),(3,3),(4,1),(5,2)};"
		+ "\n} :  I -> J"


;

}
