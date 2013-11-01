package fql.examples;

public class FullSigmaExample extends Example {

	@Override
	public String getName() {
		return "Full Sigma";
	}

	@Override
	public String getText() {
		return s;
	}

	String s = "/*"
			+ "\nThe FQL SQL compiler only supports sigma for mappings "
			+ "\nthat are discrete op-fibrations.  However, we have added "
			+ "\nsupport for full Sigma directly to the FQL IDE.  Note"
			+ "\nthat at a theoretical level, attributes cannot be used with"
			+ "\nfull sigma."
			+ "\n*/"
			+ "\n"
			+ "\nschema C ={"
			+ "\n	nodes "
			+ "\n		Amphibian,"
			+ "\n		LandAnimal,"
			+ "\n		WaterAnimal;"
			+ "\n	attributes;"
			+ "\n	arrows"
			+ "\n		IsAL:Amphibian->LandAnimal,"
			+ "\n		IsAW:Amphibian->WaterAnimal;"
			+ "\n	equations;"
			+ "\n	}"
			+ "\n"
			+ "\ninstance I:C = {"
			+ "\n	nodes "
			+ "\n		Amphibian->{1,2},"
			+ "\n		LandAnimal->{1,2,3,4,5},"
			+ "\n		WaterAnimal->{1,2,3,4};"
			+ "\n	attributes;"
			+ "\n	arrows"
			+ "\n		IsAL->{(1,1),(2,2)},"
			+ "\n		IsAW->{(1,2),(2,3)};"
			+ "\n	} C"
			+ "\n"
			+ "\ninstance J:C = {"
			+ "\n	nodes "
			+ "\n		Amphibian->{1,2,3},"
			+ "\n		LandAnimal->{1,2,3,4,5},"
			+ "\n		WaterAnimal->{1,2,3,4};"
			+ "\n	attributes;"
			+ "\n	arrows"
			+ "\n		IsAL->{(1,1),(2,2),(3,2)},"
			+ "\n		IsAW->{(1,1),(2,1),(3,2)};"
			+ "\n	} C"
			+ "\n	"
			+ "\nschema D ={"
			+ "\n	nodes "
			+ "\n		Amphibian,"
			+ "\n		LandAnimal,"
			+ "\n		WaterAnimal,"
			+ "\n		Animal;"
			+ "\n	attributes;"
			+ "\n	arrows"
			+ "\n		IsAL:Amphibian->LandAnimal,"
			+ "\n		IsAW:Amphibian->WaterAnimal,"
			+ "\n		IsALL:LandAnimal->Animal,"
			+ "\n		IsAWW:WaterAnimal->Animal;"
			+ "\n	equations"
			+ "\n		Amphibian.IsAL.IsALL=Amphibian.IsAW.IsAWW;"
			+ "\n	}"
			+ "\n"
			+ "\nmapping F:C->D = {"
			+ "\n	nodes "
			+ "\n		Amphibian->Amphibian,"
			+ "\n		LandAnimal->LandAnimal,"
			+ "\n		WaterAnimal->WaterAnimal;"
			+ "\n	attributes;"
			+ "\n	arrows"
			+ "\n		IsAL->Amphibian.IsAL,"
			+ "\n		IsAW->Amphibian.IsAW;"
			+ "\n	} C D"
			+ "\n"
			+ "\ninstance sigma_FI:D=SIGMA F I"
			+ "\ninstance sigma_FJ:D=SIGMA F J";

}
