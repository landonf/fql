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
	
	String s = 
			"schema C = {"
					+ "\n nodes "
					+ "\n 	c1, "
					+ "\n 	c2;"
					+ "\n attributes"
					+ "\n	att1 : c1 -> string,"
					+ "\n	att2 : c1 -> string, "
					+ "\n	att3 : c2 -> string;"
					+ "\n arrows;"
					+ "\n equations;"
					+ "\n}"
					+ "\n"
					+ "\ninstance I : C = {"
					+ "\n nodes"
					+ "\n	c1   -> { 1,2 },"
					+ "\n	c2   -> { 1,2,3 };"
					+ "\n attributes"
					+ "\n	att1 -> { (1,David), (2,Ryan) },"
					+ "\n	att2 -> { (1,Spivak), (2,Wisnesky) },"
					+ "\n	att3 -> { (1,MIT), (2,Harvard),(3,Leslie) };"
					+ "\n arrows;"
					+ "\n}"
					+ "\n"
					+ "\nschema D = {"
					+ "\n nodes "
					+ "\n 	d;"
					+ "\n attributes"
					+ "\n 	a1 : d -> string, "
					+ "\n 	a2 : d -> string, "
					+ "\n 	a3 : d -> string;"
					+ "\n arrows;"
					+ "\n equations;"
					+ "\n}"
					+ "\n"
					+ "\nmapping F : C -> D = {"
					+ "\n nodes "
					+ "\n 	c1 -> d,"
					+ "\n 	c2 -> d;"
					+ "\n attributes"
					+ "\n	att1 -> a1, "
					+ "\n	att2 -> a2,"
					+ "\n	att3 -> a3;"
					+ "\n arrows;"
					+ "\n}"
					+ "\n"
					+ "\ninstance J : D = pi F I";
}
