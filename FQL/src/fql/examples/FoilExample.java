package fql.examples;

public class FoilExample extends Example {

	@Override
	public String getName() {
		return "FOIL";
	}

	@Override
	public String getText() {
		return s;
	}

	String s = "schema Begin = {a,b,c,d;}"
			+ "\n"
			+ "\nschema Added = {aPLUSb,cPLUSd;}"
			+ "\n"
			+ "\nschema Multiplied = {aPLUSbTIMEScPLUSd;}"
			+ "\n"
			+ "\nmapping F:Begin->Added = {a->aPLUSb,b->aPLUSb,c->cPLUSd,d->cPLUSd;}"
			+ "\n"
			+ "\nmapping G:Added->Multiplied = {aPLUSb->aPLUSbTIMEScPLUSd,cPLUSd->aPLUSbTIMEScPLUSd;}"
			+ "\n"
			+ "\n/* Below, put any number of elements into a,b,c,d. "
			+ "\n * The output should have (a+b)*(c+d) many elements "
			+ "\n */"
			+ "\n"
			+ "\nmapping idB : Begin -> Begin = id Begin"
			+ "\nmapping idA : Added -> Added = id Added"
			+ "\nmapping idM : Multiplied -> Multiplied = id Multiplied"		
			+ "\ninstance I: Begin = {"
			+ "\n	a={1},"
			+ "\n	b={1,2},"
			+ "\n	c={1,2},"
			+ "\n	d={1,2,3} ;	"
			+ "\n}"
			+ "\n"
			+ "\n/*(1+2)*(2+3)=15*/"
			+ "\n"
			+ "\ninstance J:Added = sigma F I"
			+ "\ninstance K:Multiplied =pi G J "
			+ "\nquery p : Begin -> Added = delta idB pi idB sigma F"
			+ "\nquery q : Added -> Multiplied = delta idA pi G sigma idM "
			+ "\nquery res : Begin -> Multiplied = p then q"
			+ "\ninstance resinst : Multiplied = eval res I"
			;
}
