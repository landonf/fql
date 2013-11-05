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
					+ "\nmapping F = { "
					+ "\n nodes "
					+ "\n 	a -> aPLUSb,"
					+ "\n 	b -> aPLUSb,"
					+ "\n 	c -> cPLUSd,"
					+ "\n 	d -> cPLUSd;"
					+ "\n attributes; "
					+ "\n arrows;"
					+ "\n} : Begin -> Added"
					+ "\n"
					+ "\nmapping G = {"
					+ "\n nodes "
					+ "\n 	aPLUSb -> aPLUSbTIMEScPLUSd,"
					+ "\n 	cPLUSd -> aPLUSbTIMEScPLUSd; "
					+ "\n attributes; "
					+ "\n arrows;"
					+ "\n} : Added -> Multiplied"
					+ "\n"
					+ "\n/* Below, put any number of elements into a,b,c,d. "
					+ "\n * The output should have (a+b)*(c+d) many elements "
					+ "\n */"
					+ "\n"
					+ "\nmapping idB  = id Begin"
					+ "\nmapping idA  = id Added"
					+ "\nmapping idM  = id Multiplied"
					+ "\ninstance I = {"
					+ "\n nodes"
					+ "\n	a -> {1},"
					+ "\n	b -> {1,2},"
					+ "\n	c -> {1,2},"
					+ "\n	d -> {1,2,3};"
					+ "\n attributes;"
					+ "\n arrows;	"
					+ "\n}: Begin"
					+ "\n"
					+ "\n// (1+2)*(2+3)=15"
					+ "\n"
					+ "\ninstance J  = sigma F I"
					+ "\ninstance K  =pi G J "
					+ "\nquery p = delta idB pi idB sigma F"
					+ "\nquery q  = delta idA pi G sigma idM "
					+ "\nquery res  = (p then q)"
					+ "\ninstance resinst  = eval res I";
}
