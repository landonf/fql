package fql.examples;

public class FnCompExample extends Example {

	@Override
	public String getName() {
		return "Function Composition";
	}

	@Override
	public String getText() {
		return s;
	}
	
	String s = "/* Here we show that all three data migration functors (delta, pi, SIGMA) "
			+ "\n * can be used to compute composition of functions."
			+ "\n * It's good to TURN OBSERVABLES ON."
			+ "\n  */"
			+ "\n"
			+ "\nschema TwoMaps = {"
			+ "\n	nodes A,B,C;"
			+ "\n	attributes attA:A->int,attC:C->int;"
			+ "\n	arrows sq:A->B,inc:B->C;"
			+ "\n	equations;"
			+ "\n}"
			+ "\n"
			+ "\ninstance I = {"
			+ "\n	nodes "
			+ "\n		A->{a,b,c,d},"
			+ "\n		B->{m,n,o,p,q},"
			+ "\n		C->{v,w,x,y,z};"
			+ "\n	attributes "
			+ "\n		attA->{(a,\"-1\"),(b,0),(c,1),(d,2)},"
			+ "\n		attC->{(v,1),(w,2),(x,3),(y,4),(z,5)};"
			+ "\n	arrows"
			+ "\n		sq->{(a,n),(b,m),(c,n),(d,q)},//square map \\x.x^2"
			+ "\n		inc->{(m,v),(n,w),(o,x),(p,y),(q,z)}//inc map \\x.x+1"
			+ "\n	;"
			+ "\n} : TwoMaps"
			+ "\n"
			+ "\nschema OneMap = {"
			+ "\n	nodes A,C;"
			+ "\n	attributes attA:A->int,attC:C->int;"
			+ "\n	arrows sqinc:A->C;"
			+ "\n	equations;"
			+ "\n}"
			+ "\n"
			+ "\nmapping ForDelta = {"
			+ "\n	nodes A->A,C->C;"
			+ "\n	attributes attA->attA,attC->attC;"
			+ "\n	arrows sqinc->A.sq.inc;"
			+ "\n} : OneMap -> TwoMaps"
			+ "\n"
			+ "\ninstance ComposeUsingDelta = delta ForDelta I"
			+ "\n"
			+ "\nmapping ForSIGMA = {"
			+ "\n	nodes A->A,B->C, C->C;"
			+ "\n	attributes attA->attA,attC->attC;"
			+ "\n	arrows sq->A.sqinc,inc->C;"
			+ "\n} : TwoMaps -> OneMap"
			+ "\n"
			+ "\ninstance ComposeUsingSigma = SIGMA ForSIGMA I"
			+ "\n"
			+ "\nmapping ForPi = {"
			+ "\n	nodes A->A,B->A, C->C;"
			+ "\n	attributes attA->attA,attC->attC;"
			+ "\n	arrows sq->A,inc->A.sqinc;"
			+ "\n} : TwoMaps -> OneMap"
			+ "\n"
			+ "\ninstance ComposeUsingPi = pi ForPi I";




}
