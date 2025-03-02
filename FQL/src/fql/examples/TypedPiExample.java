package fql.examples;

import fql.examples.Example;


public class TypedPiExample extends Example {

	@Override
	public String getName() {
		return "Pi";
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
					+ "\ninstance I = {"
					+ "\n nodes"
					+ "\n	c1   -> { 1,2 },"
					+ "\n	c2   -> { 1,2,3 };"
					+ "\n attributes"
					+ "\n	att1 -> { (1,David), (2,Ryan) },"
					+ "\n	att2 -> { (1,Spivak), (2,Wisnesky) },"
					+ "\n	att3 -> { (1,MIT), (2,Harvard),(3,Leslie) };"
					+ "\n arrows;"
					+ "\n} : C"
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
					+ "\nmapping F = {"
					+ "\n nodes "
					+ "\n 	c1 -> d,"
					+ "\n 	c2 -> d;"
					+ "\n attributes"
					+ "\n	att1 -> a1, "
					+ "\n	att2 -> a2,"
					+ "\n	att3 -> a3;"
					+ "\n arrows;"
					+ "\n} : C -> D"
					+ "\n"
					+ "\ninstance J = pi F I"
					+ "\n"
					+ "\ninstance K = delta F J"
					+ "\n"
					+ "\ntransform monad_counit = K.coreturn"
					+ "\n"
					+ "\ninstance L = pi F K"
					+ "\n"
					+ "\ntransform monad_unit = L.return"
					+ "\n"
					+ "\ninstance I0 = {"
					+ "\n nodes"
					+ "\n	c1   -> { 1 },"
					+ "\n	c2   -> { 1,2 };"
					+ "\n attributes"
					+ "\n	att1 -> { (1,David) },"
					+ "\n	att2 -> { (1,Spivak) },"
					+ "\n	att3 -> { (1,MIT), (2,Harvard) };"
					+ "\n arrows;"
					+ "\n} : C"
					+ "\n"
					+ "\ntransform t = {"
					+ "\n	nodes c1 -> {(1,1)}, c2 -> {(1,1),(2,2)} ;"
					+ "\n} : I0 -> I "
					+ "\n"
					+ "\ninstance J0 = pi F I0"
					+ "\n"
					+ "\ntransform t0 = pi J0 J t";



}
