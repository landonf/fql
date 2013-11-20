package fql.examples;

public class WrittenExample extends Example {

	@Override
	public String getName() {
		return "Written Macro";
	}

	@Override
	public String getText() {
		return r + s;
	}
	
	String r = "//Illustrates ASWRITTEN macro for automatically adding attributes to schemas and instances.\n\n";
	String s = "schema C = {"+ "\n	nodes A;"+ "\n	attributes ASWRITTEN;"+ "\n	arrows f:A->A;"+ "\nequations A.f.f.f.f.f.f=A.f;"+ "\n}"+ "\n"+ "\ninstance I = {"+ "\n	nodes A->{a,b,c,d,e};"+ "\n	attributes ASWRITTEN;"+ "\n	arrows f->{(a,b),(b,c),(c,d),(d,e),(e,a)};"+ "\n} : C"

;

}
