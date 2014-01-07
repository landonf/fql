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
			+ "\nsupport for full Sigma directly to the FQL IDE.  "
			+ "\n*/"
			+ "\n"
			+ "\nschema C ={"
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
			+ "\ninstance I = {"
			+ "\n	nodes "
			+ "\n		Amphibian->{1,2},"
			+ "\n		LandAnimal->{1,2,3,4,5},"
			+ "\n		WaterAnimal->{1,2,3,4};"
			+ "\n	attributes "
			+ "\n		attA -> {(1,gecko),(2, frog)}, "
			+ "\n		attL ->{(1,gecko),(2,frog),(3,human),(4,cow),(5,horse)},"
			+ "\n		attW -> {(1,fish),(2,gecko),(3,frog),(4,dolphin)};"
			+ "\n	arrows" + "\n		IsAL->{(1,1),(2,2)},"
			+ "\n		IsAW->{(1,2),(2,3)};" + "\n	} : C" + "\n"
			+ "\ninstance J = {" + "\n	nodes " + "\n		Amphibian->{1,2,3},"
			+ "\n		LandAnimal->{1,2,3,4,5}," + "\n		WaterAnimal->{1,2,3,4};"
			+ "\n	attributes "
			+ "\n		attA -> {(1,gecko),(2, gecko),(3, gecko)},"
			+ "\n		attL ->{(1,gecko),(2,gecko),(3,human),(4,cow),(5,horse)},"
			+ "\n		attW -> {(1,gecko),(2,gecko),(3,gecko),(4,dolphin)};"
			+ "\n	arrows" + "\n		IsAL->{(1,1),(2,2),(3,2)},"
			+ "\n		IsAW->{(1,1),(2,1),(3,2)};" + "\n	} : C" + "\n" + "\n"
			+ "\nschema D ={" + "\n	nodes " + "\n		Amphibian,"
			+ "\n		LandAnimal," + "\n		WaterAnimal," + "\n		Animal;"
			+ "\n	attributes" + "\n		attA : Amphibian -> string, "
			+ "\n		attL:LandAnimal-> string, "
			+ "\n		attW:WaterAnimal->string;" + "\n	arrows"
			+ "\n		IsAL:Amphibian->LandAnimal,"
			+ "\n		IsAW:Amphibian->WaterAnimal,"
			+ "\n		IsALL:LandAnimal->Animal,"
			+ "\n		IsAWW:WaterAnimal->Animal;" + "\n	equations"
			+ "\n		Amphibian.IsAL.IsALL=Amphibian.IsAW.IsAWW;" + "\n	}" + "\n"
			+ "\nmapping F = {" + "\n	nodes " + "\n		Amphibian->Amphibian,"
			+ "\n		LandAnimal->LandAnimal," + "\n		WaterAnimal->WaterAnimal;"
			+ "\n	attributes" + "\n		attA -> attA, " + "\n		attL -> attL, "
			+ "\n		attW -> attW;" + "\n	arrows" + "\n		IsAL->Amphibian.IsAL,"
			+ "\n		IsAW->Amphibian.IsAW;" + "\n	} : C -> D" + "\n"
			+ "\ninstance sigma_FI=SIGMA F I" + "\ninstance sigma_FJ=SIGMA F J"
			+ "\n" + "\ninstance I0 = {" + "\n	nodes " + "\n		Amphibian->{1},"
			+ "\n		LandAnimal->{1,2,3,4}," + "\n		WaterAnimal->{1,2,3};"
			+ "\n	attributes " + "\n		attA -> {(1,gecko)}, "
			+ "\n		attL ->{(1,gecko),(2,frog),(3,human),(4,cow)},"
			+ "\n		attW -> {(1,fish),(2,gecko),(3,frog)};" + "\n	arrows"
			+ "\n		IsAL->{(1,1)}," + "\n		IsAW->{(1,2)};" + "\n	} : C" + "\n"
			+ "\ntransform t = {" + "\n	nodes " + "\n		Amphibian->{(1,1)},"
			+ "\n		LandAnimal->{(1,1),(2,2),(3,3),(4,4)},"
			+ "\n		WaterAnimal->{(1,1),(2,2),(3,3)};	" + "\n} : I0 -> I" + "\n"
			+ "\ninstance sigma_FI0 = SIGMA F I0" + "\n	"
			+ "\ntransform t0 = SIGMA sigma_FI0 sigma_FI t"

	;

}
