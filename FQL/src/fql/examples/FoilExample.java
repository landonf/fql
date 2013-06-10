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

	String s = 
			"schema Begin = { "
					+ "\n nodes "
					+ "\n	a,b,c,d; "
					+ "\n attributes; "
					+ "\n arrows; "
					+ "\n equations; "
					+ "\n}"
					+ "\n"
					+ "\nschema Added = { "
					+ "\n nodes "
					+ "\n 	aPLUSb,cPLUSd; "
					+ "\n attributes; "
					+ "\n arrows; "
					+ "\n equations;"
					+ "\n}"
					+ "\n"
					+ "\nschema Multiplied = { "
					+ "\n nodes "
					+ "\n 	aPLUSbTIMEScPLUSd; "
					+ "\n attributes; "
					+ "\n arrows; "
					+ "\n equations;"
					+ "\n}"
					+ "\n"
					+ "\nmapping F:Begin -> Added = { "
					+ "\n nodes "
					+ "\n 	a -> aPLUSb,"
					+ "\n 	b -> aPLUSb,"
					+ "\n 	c -> cPLUSd,"
					+ "\n 	d -> cPLUSd;"
					+ "\n attributes; "
					+ "\n arrows;"
					+ "\n}"
					+ "\n"
					+ "\nmapping G : Added -> Multiplied = {"
					+ "\n nodes "
					+ "\n 	aPLUSb -> aPLUSbTIMEScPLUSd,"
					+ "\n 	cPLUSd -> aPLUSbTIMEScPLUSd; "
					+ "\n attributes; "
					+ "\n arrows;"
					+ "\n}"
					+ "\n"
					+ "\n/* Below, put any number of elements into a,b,c,d. "
					+ "\n * The output should have (a+b)*(c+d) many elements "
					+ "\n */"
					+ "\n"
					+ "\nmapping idB : Begin -> Begin = id Begin"
					+ "\nmapping idA : Added -> Added = id Added"
					+ "\nmapping idM : Multiplied -> Multiplied = id Multiplied"
					+ "\ninstance I: Begin = {"
					+ "\n nodes"
					+ "\n	a -> {1},"
					+ "\n	b -> {1,2},"
					+ "\n	c -> {1,2},"
					+ "\n	d -> {1,2,3};"
					+ "\n attributes;"
					+ "\n arrows;	"
					+ "\n}"
					+ "\n"
					+ "\n// (1+2)*(2+3)=15"
					+ "\n"
					+ "\ninstance J : Added = sigma F I"
					+ "\ninstance K : Multiplied =pi G J "
					+ "\nquery p : Begin -> Added = delta idB pi idB sigma F"
					+ "\nquery q : Added -> Multiplied = delta idA pi G sigma idM "
					+ "\nquery res : Begin -> Multiplied = p then q"
					+ "\ninstance resinst : Multiplied = eval res I";
}
