package fql.examples;

public class TypedFoilExample extends Example {

	@Override
	public String getName() {
		return "Typed FOIL";
	}

	@Override
	public String getText() {
		return s;
	}
	
	String s = "schema Begin = {"
			+ "\n	atta:a->string,"
			+ "\n	attb:b->string,"
			+ "\n	attc:c->string,"
			+ "\n	attd:d->string;"
			+ "\n}"
			+ "\n"
			+ "\nschema Added = {"
			+ "\n	attab:aPLUSb->string,"
			+ "\n	attcd:cPLUSd->string;"
			+ "\n}"
			+ "\n"
			+ "\nschema Multiplied = {"
			+ "\n	attab:aPLUSbTIMEScPLUSd->string,"
			+ "\n	attcd:aPLUSbTIMEScPLUSd->string;"
			+ "\n}"
			+ "\n"
			+ "\nmapping F:Begin->Added = {"
			+ "\n	a->aPLUSb,"
			+ "\n	b->aPLUSb,"
			+ "\n	c->cPLUSd,"
			+ "\n	d->cPLUSd,"
			+ "\n	atta->attab,"
			+ "\n	attb->attab,"
			+ "\n	attc->attcd,"
			+ "\n	attd->attcd;"
			+ "\n}"
			+ "\n"
			+ "\nmapping G:Added->Multiplied = {"
			+ "\n	aPLUSb->aPLUSbTIMEScPLUSd,"
			+ "\n	cPLUSd->aPLUSbTIMEScPLUSd,"
			+ "\n	attab->attab,"
			+ "\n	attcd->attcd;"
			+ "\n}"
			+ "\n"
			+ "\n/* Below, put any number of elements into a,b,c,d. "
			+ "\n * The output should have (a+b)*(c+d) many elements "
			+ "\n */"
			+ "\n"
			+ "\ninstance I: Begin = {"
			+ "\n	a={1},"
			+ "\n	b={1,2},"
			+ "\n	c={1,2},"
			+ "\n	d={1,2,3};"
			+ "\n	atta={(1,a1)},"
			+ "\n	attb={(1,b1),(2,b2)},"
			+ "\n	attc={(1,c1),(2,c2)},"
			+ "\n	attd={(1,d1),(2,d2),(3,d3)}		"
			+ "\n}"
			+ "\n"
			+ "\n/*(1+2)*(2+3)=15*/"
			+ "\n"
			+ "\ninstance J:Added = sigma F I"
			+ "\ninstance K:Multiplied =pi G J "
			+ "\n";

}
