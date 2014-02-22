package fql.examples;

public class CategoryExample extends Example {

	@Override
	public String getName() {
		return "Category";
	}

	@Override
	public String getText() {
		return s;
	}

	String s = "//requires parallel or hybrid left kan" 
			+ "\nschema Category = {"
			+ "\n	nodes "
			+ "\n		Ob, "
			+ "\n		Hom, "
			+ "\n		Comp"
			+ "\n		;"
			+ "\n	attributes"
			+ "\n		;"
			+ "\n	arrows"
			+ "\n		Dom:Hom->Ob,"
			+ "\n		Cod:Hom->Ob,"
			+ "\n		Id:Ob->Hom,"
			+ "\n		LeftId:Hom->Comp, //f:a-->b  mapped to id_a ; f"
			+ "\n		RightId:Hom->Comp, //f:a-->b  mapped to f ; id_b"
			+ "\n		First:Comp->Hom,"
			+ "\n		Second:Comp->Hom,"
			+ "\n		Compose:Comp->Hom"
			+ "\n		;"
			+ "\n	equations"
			+ "\n		Ob.Id.Dom=Ob,"
			+ "\n		Ob.Id.Cod=Ob,"
			+ "\n		Hom.LeftId.Second=Hom,"
			+ "\n		Hom.RightId.First=Hom,"
			+ "\n		Hom.LeftId.Compose=Hom,"
			+ "\n		Hom.RightId.Compose=Hom,"
			+ "\n		Hom.LeftId.First=Hom.Dom.Id,"
			+ "\n		Hom.RightId.Second=Hom.Cod.Id,"
			+ "\n		Comp.First.Cod=Comp.Second.Dom,"
			+ "\n		Comp.First.Dom=Comp.Compose.Dom,"
			+ "\n		Comp.Second.Cod=Comp.Compose.Cod"
			+ "\n		;"
			+ "\n}"
			+ "\n"
			+ "\nschema Graph = {"
			+ "\n	nodes Ob,Hom;"
			+ "\n	attributes;"
			+ "\n	arrows Dom:Hom->Ob,Cod:Hom->Ob;"
			+ "\n	equations;"
			+ "\n}"
			+ "\n"
			+ "\nmapping Free = {"
			+ "\n	nodes Ob->Ob, Hom->Hom;"
			+ "\n	attributes ;"
			+ "\n	arrows Dom->Hom.Dom, Cod->Hom.Cod;"
			+ "\n} : Graph -> Category"
			+ "\n"
			+ "\ninstance G = {"
			+ "\n	nodes Ob->{a,b,c},Hom->{f,g};"
			+ "\n	attributes ;"
			+ "\n	arrows Dom->{(f,a),(g,b)},Cod->{(f,b),(g,c)};"
			+ "\n} :  Graph"
			+ "\n"
			+ "\ninstance C=SIGMA Free G"
			+ "\n"
			+ "\ninstance D=pi Free G"
			+ "\n"
			+ "\n"
			+ "\nschema NamedCategory = {"
			+ "\n	nodes "
			+ "\n		Ob, "
			+ "\n		Hom, "
			+ "\n		Comp"
			+ "\n		;"
			+ "\n	attributes"
			+ "\n		ObName:Ob->string,"
			+ "\n		HomName:Hom->string;"
			+ "\n	arrows"
			+ "\n		Dom:Hom->Ob,"
			+ "\n		Cod:Hom->Ob,"
			+ "\n		Id:Ob->Hom,"
			+ "\n		LeftId:Hom->Comp, //f:a-->b  mapped to id_a ; f"
			+ "\n		RightId:Hom->Comp, //f:a-->b  mapped to f ; id_b"
			+ "\n		First:Comp->Hom,"
			+ "\n		Second:Comp->Hom,"
			+ "\n		Compose:Comp->Hom"
			+ "\n		;"
			+ "\n	equations"
			+ "\n		Ob.Id.Dom=Ob,"
			+ "\n		Ob.Id.Cod=Ob,"
			+ "\n		Hom.LeftId.Second=Hom,"
			+ "\n		Hom.RightId.First=Hom,"
			+ "\n		Hom.LeftId.Compose=Hom,"
			+ "\n		Hom.RightId.Compose=Hom,"
			+ "\n		Hom.LeftId.First=Hom.Dom.Id,"
			+ "\n		Hom.RightId.Second=Hom.Cod.Id,"
			+ "\n		Comp.First.Cod=Comp.Second.Dom,"
			+ "\n		Comp.First.Dom=Comp.Compose.Dom,"
			+ "\n		Comp.Second.Cod=Comp.Compose.Cod"
			+ "\n		;"
			+ "\n}"
			+ "\n"
			+ "\nschema NamedGraph = {"
			+ "\n	nodes Ob,Hom;"
			+ "\n	attributes ObName:Ob->string,HomName:Hom->string;"
			+ "\n	arrows Dom:Hom->Ob,Cod:Hom->Ob;"
			+ "\n	equations;"
			+ "\n}"
			+ "\n"
			+ "\nmapping NamedFree = {"
			+ "\n	nodes Ob->Ob, Hom->Hom;"
			+ "\n	attributes ObName->ObName,HomName->HomName;"
			+ "\n	arrows Dom->Hom.Dom, Cod->Hom.Cod;"
			+ "\n} : NamedGraph -> NamedCategory"
			+ "\n"
			+ "\ninstance NamedG = {"
			+ "\n	nodes Ob->{a,b,c},Hom->{f,g};"
			+ "\n	attributes ObName->{(a,a),(b,b),(c,c)},HomName->{(f,f),(g,g)};"
			+ "\n	arrows Dom->{(f,a),(g,b)},Cod->{(f,b),(g,c)};"
			+ "\n} :  NamedGraph" + "\n"
			+ "\ninstance NamedC=SIGMA NamedFree NamedG  //requires null option";

}
