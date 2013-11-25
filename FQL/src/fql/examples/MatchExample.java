package fql.examples;

public class MatchExample extends Example {

	@Override
	public String getName() {
		return "Schema Matching";
	}

	@Override
	public String getText() {
		return s;
	}
 
	String s = "schema C ={"
			+ "\n	nodes "
			+ "\n		Amphibian,"
			+ "\n		LandAnimal,"
			+ "\n		WaterAnimal;"
			+ "\n	attributes "
			+ "\n		attA : Amphibian -> string, "
			+ "\n		attL:LandAnimal-> string, "
			+ "\n		attW:WaterAnimal->string;"
			+ "\n	arrows"
			+ "\n		IsAL:Amphibian->LandAnimal,"
			+ "\n		IsAW:Amphibian->WaterAnimal;"
			+ "\n	equations;"
			+ "\n	}"
			+ "\n"
			+ "\nschema D ={"
			+ "\n	nodes "
			+ "\n		Amphibian,"
			+ "\n		LandAnimal,"
			+ "\n		WaterAnimal,"
			+ "\n		Animal;"
			+ "\n	attributes"
			+ "\n		attA : Amphibian -> string, "
			+ "\n		attL:Animal-> string, "
			+ "\n		attW:Animal->string;"
			+ "\n	arrows"
			+ "\n		IsAL:Amphibian->LandAnimal,"
			+ "\n		IsAW:Amphibian->WaterAnimal,"
			+ "\n		IsALL:LandAnimal->Animal,"
			+ "\n		IsAWW:WaterAnimal->Animal;"
			+ "\n	equations"
			+ "\n		Amphibian.IsAL.IsALL=Amphibian.IsAW.IsAWW;"
			+ "\n}"
			+ "\n"
			+ "\n{nodes match_node0, match_node1, right_LandAnimal, right_WaterAnimal; "
			+ "\nattributes match_att2: match_node1 -> string, match_att1: match_node0 -> string, match_att0: match_node1 -> string; "
			+ "\narrows right_IsAWW: right_WaterAnimal -> match_node1, "
			+ "\nright_IsAW: match_node0 -> right_WaterAnimal, "
			+ "\nright_IsALL: right_LandAnimal -> match_node1, "
			+ "\nright_IsAL: match_node0 -> right_LandAnimal, "
			+ "\nleft_IsAW: match_node0 -> match_node1, "
			+ "\nleft_IsAL: match_node0 -> match_node1; "
			+ "\nequations match_node0.right_IsAL.right_IsALL = match_node0.right_IsAW.right_IsAWW;}"
			+ "\n"
			+ "\n"
			+ "\ninstance I = {"
			+ "\n	nodes "
			+ "\n		Amphibian->{1,2},"
			+ "\n		LandAnimal->{1,2,3,4,5},"
			+ "\n		WaterAnimal->{1,2,3,4};"
			+ "\n	attributes "
			+ "\n		attA -> {(1,gecko),(2, salamander)}, "
			+ "\n		attL ->{(1,gecko),(2,salamander),(3,human),(4,cow),(5,horse)},"
			+ "\n		attW -> {(1,fish),(2,gecko),(3,salamander),(4,dolphin)};"
			+ "\n	arrows"
			+ "\n		IsAL->{(1,1),(2,2)},"
			+ "\n		IsAW->{(1,2),(2,3)};"
			+ "\n	} : C"
			+ "\n"
			+ "\n"
			+ "\nQUERY q1 = match {(attA, attA),(attL, attL), (attW, attW)} C D \"delta sigma forward\""
			+ "\nQUERY q2 = match {(attA, attA),(attL, attL), (attW, attW)} C D \"delta pi forward\""
			+ "\nQUERY q3 = match {(attA, attA),(attL, attL), (attW, attW)} C D \"delta sigma backward\""
			+ "\nQUERY q4 = match {(attA, attA),(attL, attL), (attW, attW)} C D \"delta pi backward\""
			+ "\n"
			+ "\n//instance out1 = EVAL q1 I // - I has wrong type"
			+ "\n//instance out2 = EVAL q2 I // - I has wrong type"
			+ "\ninstance out3 = EVAL q3 I // SIGMA fails"
			+ "\n//instance out4 = EVAL q4 I // has 800 rows!";







	

}
