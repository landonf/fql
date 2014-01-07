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
			+ "\n		attL:LandAnimal-> string, "
			+ "\n		attW:WaterAnimal->string;"
			+ "\n	arrows"
			+ "\n		IsAL:Amphibian->LandAnimal,"
			+ "\n		IsAW:Amphibian->WaterAnimal,"
			+ "\n		IsALL:LandAnimal->Animal,"
			+ "\n		IsAWW:WaterAnimal->Animal;"
			+ "\n	equations"
			+ "\n		Amphibian.IsAL.IsALL=Amphibian.IsAW.IsAWW;"
			+ "\n	}"
			+ "\n"
			+ "\ninstance I = {"
			+ "\n	nodes "
			+ "\n		Amphibian->{1,2},"
			+ "\n		LandAnimal->{1,2,3,4,5},"
			+ "\n		WaterAnimal->{1,2,3,4};"
			+ "\n	attributes "
			+ "\n		attA -> {(1,gecko),(2, frog)}, "
			+ "\n		attL ->{(1,gecko),(2,frog),(3,human),(4,cow),(5,horse)},"
			+ "\n		attW -> {(1,fish),(2,gecko),(3,frog),(4,dolphin)};"
			+ "\n	arrows"
			+ "\n		IsAL->{(1,1),(2,2)},"
			+ "\n		IsAW->{(1,2),(2,3)};"
			+ "\n	} : C"
			+ "\n"
			+ "\n"
			+ "\n"
			+ "\nQUERY q1 = match {(attA, attA),(attL, attL), (attW, attW)} C D \"delta sigma forward\""
			+ "\nQUERY q2 = match {(attA, attA),(attL, attL), (attW, attW)} C D \"delta pi forward\""
			+ "\nQUERY q3 = match {(attA, attA),(attL, attL), (attW, attW)} C D \"delta sigma backward\""
			+ "\nQUERY q4 = match {(attA, attA),(attL, attL), (attW, attW)} C D \"delta pi backward\""
			+ "\n"
			+ "\n//instance out1 = EVAL q1 I // - I has wrong type"
			+ "\n//instance out2 = EVAL q2 I // - I has wrong type"
			+ "\ninstance out3 = EVAL q3 I // SIGMA fails if it can't create nulls"
			+ "\ninstance out4 = EVAL q4 I // has 40 rows";










	

}
