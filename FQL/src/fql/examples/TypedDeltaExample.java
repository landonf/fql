package fql.examples;

public class TypedDeltaExample extends Example {

	@Override
	public String getName() {
		return "Typed Delta";
	}

	@Override
	public String getText() {
		return initialDefinitions;
	}

	public static final String initialDefinitions = 
			"/* note: this is not a discrete op-fibration */\n\n"
			 + "schema C = {" 
					+ "\n" + "    t1_ssn : T1 -> string"
					+ "\n" + "  , t1_first : T1 -> string"
					+ "\n" + "  , t1_last : T1 -> string"
					+ "\n" + "  , t2_first : T2 -> string"
					+ "\n" + "  , t2_last : T2 -> string"
					+ "\n" + "  , t2_salary : T2 -> int"
					+ "\n" + " ; "
					+ "\n" + "}"
					+ "\n" 
					+ "\n" + "schema D = {"
					+ "\n" + "    ssn0 : T -> string"
					+ "\n" + "  , first0 : T -> string"
					+ "\n" + "  , last0 : T -> string"
					+ "\n" + "  , salary0 : T -> int"
					+ "\n" + " ;"
					+ "\n" + "}"
					+ "\n" 
					+ "\n" + "mapping F : C -> D = {"
					+ "\n" + "    T1 -> T"
					+ "\n" + "  , T2 -> T"
					+ "\n" + "  , t1_ssn -> ssn0"
					+ "\n" + "  , t1_first -> first0"
					+ "\n" + "  , t2_first -> first0"
					+ "\n" + "  , t1_last -> last0"
					+ "\n" + "  , t2_last -> last0"
					+ "\n" + "  , t2_salary -> salary0;"
					+ "\n" + "}" 
					+ "\n\ninstance J : D = {\n"
					+ " T = { XF667,XF891,XF221 } ; \n"
					+ " ssn0 = { (XF667,115-234),(XF891,112-988),(XF221,198-887) },\n"
					+ " first0 = { (XF667,Bob),(XF891,Sue),(XF221,Alice) },\n"
					+ " last0 = { (XF667,Smith),(XF891,Smith),(XF221,Jones) },\n"
					+ " salary0 = { (XF667,250),(XF891,300),(XF221,100)}\n"
					+ "}\n"
					+ "\ninstance I : C = delta F J\n";
				//	+ "\n" + "mapping idC = id C"
			//		+ "\n" + "mapping idD = id D"
			//		+ "\n" 
			//		+ "\n" + "query delta = project F join idC union idC"
			//		+ "\n" 
			//		+ "\n" + "query pi = project idC join F union idD"
			//		+ "\n" 
			/*
					+ "\n"
					+ "\ninstance I = delta F J\n"
					+ "\ninstance J' = pi F I\n"
					+ "\n\n\n/*"
					+ "\nExpected output:"
					+ "\n"
					+ "\nI = {"
					+ "\n  t2_last = { (XF221,Jones), (XF667,Smith), (XF891,Smith) };"
					+ "\n  Last = { (Jones,Jones), (Smith,Smith) };"
					+ "\n  t1_first = { (XF667,Bob), (XF221,Alice), (XF891,Sue) };"
					+ "\n  t2_first = { (XF667,Bob), (XF221,Alice), (XF891,Sue) };"
					+ "\n  SSN = { (112-988,112-988), (115-234,115-234), (198-887,198-887) }"
					+ "\n  t1_ssn = { (XF667,115-234), (XF891,112-988), (XF221,198-887) };"
					+ "\n  Salary = { (250,250), (100,100), (300,300) };"
					+ "\n  t1_last = { (XF221,Jones), (XF667,Smith), (XF891,Smith) };"
					+ "\n  t2_salary = { (XF221,100), (XF667,250), (XF891,300) };"
					+ "\n  T1 = { (XF667,XF667), (XF221,XF221), (XF891,XF891) };"
					+ "\n  First = { (Sue,Sue), (Bob,Bob), (Alice,Alice) };"
					+ "\n  T2 = { (XF667,XF667), (XF221,XF221), (XF891,XF891) } };"
					+ "\n}"
					+ "\n"
					+ "\nJ' = {"
					+ "\n   T = { (Sue^Sue^300^Smith^112-988^XF891^XF891,Sue^Sue^300^Smith^112-988^XF891^XF891), (Bob^Bob^250^Smith^115-234^XF667^XF667,Bob^Bob^250^Smith^115-234^XF667^XF667), (Alice^Alice^100^Jones^198-887^XF221^XF221,Alice^Alice^100^Jones^198-887^XF221^XF221) };"
					+ "\n   Last = { (Jones,Jones), (Smith,Smith) };"
					+ "\n   last = { (Sue^Sue^300^Smith^112-988^XF891^XF891,Smith), (Alice^Alice^100^Jones^198-887^XF221^XF221,Jones), (Bob^Bob^250^Smith^115-234^XF667^XF667,Smith) };"
					+ "\n   SSN = { (112-988,112-988), (115-234,115-234), (198-887,198-887) };"
					+ "\n   ssn = { (Alice^Alice^100^Jones^198-887^XF221^XF221,198-887), (Bob^Bob^250^Smith^115-234^XF667^XF667,115-234), (Sue^Sue^300^Smith^112-988^XF891^XF891,112-988) };"
					+ "\n   Salary = { (250,250), (100,100), (300,300) };"
					+ "\n   salary = { (Alice^Alice^100^Jones^198-887^XF221^XF221,100), (Bob^Bob^250^Smith^115-234^XF667^XF667,250), (Sue^Sue^300^Smith^112-988^XF891^XF891,300) };"
					+ "\n   First = { (Sue,Sue), (Bob,Bob), (Alice,Alice) };"
					+ "\n   first = { (Sue^Sue^300^Smith^112-988^XF891^XF891,Sue), (Alice^Alice^100^Jones^198-887^XF221^XF221,Alice), (Bob^Bob^250^Smith^115-234^XF667^XF667,Bob) } };"
					+ "\n}"
					+ "\n" */



}
