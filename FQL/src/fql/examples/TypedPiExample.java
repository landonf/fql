package fql.examples;

import fql.examples.Example;


public class TypedPiExample extends Example {

	@Override
	public String getName() {
		return "Typed Pi";
	}

	@Override
	public String getText() {
		return s;
	}
	
	String s = "schema C={att1:c1->string, att2:c1->string, att3:c2->string;}"
			+ "\n"
			+ "\ninstance I:C ={"
			+ "\n	c1={(1,1),(2,2)},"
			+ "\n	c2={(1,1),(2,2),(3,3)},"
			+ "\n	att1={(1,david),(2,ryan)},"
			+ "\n	att2={(1,spivak),(2,wisnesky)},"
			+ "\n	att3={(1,MIT),(2,Harvard),(3,Leslie)}"
			+ "\n}"
			+ "\n"
			+ "\nschema D={a1:d->string, a2:d->string, a3:d->string;}"
			+ "\n"
			+ "\nmapping F:C->D = {c1->d, c2->d, att1->a1, att2->a2,att3->a3;}"
			+ "\n"
			+ "\ninstance J:D=pi F I"
			+ "\n";

}
