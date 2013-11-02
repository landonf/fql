package fql.examples;

public class DeltaExample extends Example {

	@Override
	public String getName() {
		return "Delta";
	}

	@Override
	public String getText() {
		return s;
	}

	public static final String s = 
			"schema C = {"
					+ "\n nodes"
					+ "\n 	T1, T2, SSN, First, Last, Salary;"
					+ "\n attributes;"
					+ "\n arrows"
					+ "\n	t1_ssn    : T1 -> SSN,"
					+ "\n	t1_first  : T1 -> First,"
					+ "\n	t1_last   : T1 -> Last,"
					+ "\n	t2_first  : T2 -> First,"
					+ "\n	t2_last   : T2 -> Last,"
					+ "\n	t2_salary : T2 -> Salary;"
					+ "\n equations;"
					+ "\n}"
					+ "\n"
					+ "\nschema D = {"
					+ "\n nodes"
					+ "\n 	T, SSN, First, Last, Salary;"
					+ "\n attributes;"
					+ "\n arrows"
					+ "\n	ssn0    : T -> SSN,"
					+ "\n	first0  : T -> First,"
					+ "\n	last0   : T -> Last,"
					+ "\n	salary0 : T -> Salary;"
					+ "\n equations;"
					+ "\n}"
					+ "\n"
					+ "\nmapping F = {"
					+ "\n nodes"
					+ "\n	SSN    -> SSN,"
					+ "\n	First  -> First,"
					+ "\n	Last   -> Last,"
					+ "\n	Salary -> Salary,"
					+ "\n	T1     -> T,"
					+ "\n	T2     -> T;"
					+ "\n attributes;"
					+ "\n arrows"
					+ "\n	t1_ssn    -> T.ssn0,"
					+ "\n	t1_first  -> T.first0,"
					+ "\n	t2_first  -> T.first0,"
					+ "\n	t1_last   -> T.last0,"
					+ "\n	t2_last   -> T.last0,"
					+ "\n	t2_salary -> T.salary0;"
					+ "\n} : C -> D"
					+ "\n"
					+ "\ninstance J = {"
					+ "\n nodes"
					+ "\n	T -> { XF667,XF891,XF221 },"
					+ "\n	SSN -> { 115234,112988,198887 },"
					+ "\n	First  -> { Bob,Sue,Alice },"
					+ "\n	Last   -> { Smith,Jones},"
					+ "\n	Salary -> { 250,300,100 };"
					+ "\n attributes;"
					+ "\n arrows"
					+ "\n	ssn0 -> { (XF667,115234),(XF891,112988),(XF221,198887) },"
					+ "\n	first0 -> { (XF667,Bob),(XF891,Sue),(XF221,Alice) },"
					+ "\n	last0 -> { (XF667,Smith),(XF891,Smith),(XF221,Jones) },"
					+ "\n	salary0 -> { (XF667,250),(XF891,300),(XF221,100) };"
					+ "\n} : D"
					+ "\n"
					+ "\ninstance I  = delta F J"
					+ "\n"
					+ "\ninstance J0  = pi F I\n";




}
