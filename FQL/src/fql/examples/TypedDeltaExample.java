package fql.examples;

public class TypedDeltaExample extends Example {

	@Override
	public String getName() {
		return "Typed Delta";
	}

	@Override
	public String getText() {
		return s;
	}

	public static final String s = 
			"schema C = {"
					+ "\n nodes "
					+ "\n	T1, "
					+ "\n	T2;"
					+ "\n attributes"
					+ "\n	t1_ssn    : T1 -> string,"
					+ "\n	t1_first  : T1 -> string,"
					+ "\n	t1_last   : T1 -> string,"
					+ "\n	t2_first  : T2 -> string,"
					+ "\n	t2_last   : T2 -> string,"
					+ "\n	t2_salary : T2 -> int;"
					+ "\n arrows;"
					+ "\n equations; "
					+ "\n}"
					+ "\n"
					+ "\nschema D = {"
					+ "\n nodes "
					+ "\n	T;"
					+ "\n attributes"
					+ "\n	ssn0    : T -> string,"
					+ "\n	first0  : T -> string,"
					+ "\n	last0   : T -> string,"
					+ "\n	salary0 : T -> int;"
					+ "\n arrows;"
					+ "\n equations;"
					+ "\n}"
					+ "\n"
					+ "\nmapping F : C -> D = {"
					+ "\n nodes "
					+ "\n	T1 -> T,"
					+ "\n	T2 -> T;"
					+ "\n attributes"
					+ "\n	t1_ssn    -> ssn0,"
					+ "\n	t1_first  -> first0,"
					+ "\n	t2_first  -> first0,"
					+ "\n	t1_last   -> last0,"
					+ "\n	t2_last   -> last0,"
					+ "\n	t2_salary -> salary0;"
					+ "\n arrows;"
					+ "\n} C D"
					+ "\n"
					+ "\ninstance J : D = {"
					+ "\n nodes "
					+ "\n	T -> { XF667,XF891,XF221 } ; "
					+ "\n attributes"
					+ "\n	ssn0    -> { (XF667, \"115-234\"),(XF891,\"112-988\"),(XF221,\"198-887\") },"
					+ "\n	first0  -> { (XF667,Bob),(XF891,Sue),(XF221,Alice) },"
					+ "\n	last0   -> { (XF667,Smith),(XF891,Smith),(XF221,Jones) },"
					+ "\n	salary0 -> { (XF667,250),(XF891,300),(XF221,100) };"
					+ "\n arrows;"
					+ "\n} D"
					+ "\n"
					+ "\ninstance I : C = delta F J"
					+ "\n";

}
