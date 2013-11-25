package fql.examples;

public class SubSchemaExample extends Example {

	@Override
	public String getName() {
		return "Sub Schema";
	}

	@Override
	public String getText() {
		return s;
	}
	
	String s = "schema ab = {"
			+ "\n	nodes a, b;"
			+ "\n	attributes atta : a -> string, attb : b -> string;"
			+ "\n	arrows f : a -> b;"
			+ "\n	equations;"
			+ "\n}"
			+ "\n"
			+ "\nschema c = {"
			+ "\n	nodes c;"
			+ "\n	attributes attc : c -> string;"
			+ "\n	arrows ;"
			+ "\n	equations;"
			+ "\n}"
			+ "\n"
			+ "\nschema abc = (ab union c)"
			+ "\n"
			+ "\nmapping F = subschema ab abc"


;

}
