package fql.examples;

public class EmployeesExample extends Example {

	@Override
	public String getName() {
		return "Employees";
	}

	@Override
	public String getText() {
		return s;
	}
	
	public static final String s = 
					"/* this is an example of an infinite schema.  \nto compile it, check 'do not validate mappings' in the options menu */\n"
			        + "\nschema S = { "
					+ "\n nodes"
					+ "\n 	Employee, Department, FirstName, LastName, DepartmentName;"
					+ "\n attributes;"
					+ "\n arrows"
					+ "\n	manager   : Employee -> Employee,"
					+ "\n  	worksIn   : Employee -> Department,"
					+ "\n  	secretary : Department -> Employee,"
					+ "\n  	name      : Department -> DepartmentName,"
					+ "\n  	first     : Employee -> FirstName,"
					+ "\n  	last      : Employee -> LastName;"
					+ "\n equations"
					+ "\n  	Employee.manager.worksIn = Employee.worksIn,"
					+ "\n  	Department.secretary.worksIn = Department;"
					+ "\n}"
					+ "\n"
					+ "\ninstance I = {"
					+ "\n nodes"
					+ "\n 	FirstName      -> { Alan, Alice,  Andrey, Camille, David },"
					+ "\n 	LastName       -> { Arden, Hoover, Jordan, Markov, Turing },"
					+ "\n  	DepartmentName -> { AppliedMath, Biology, PureMath },"
					+ "\n	Employee       -> { 101, 102, 103 },"
					+ "\n	Department     -> { q10, x02 };"
					+ "\n attributes;"
					+ "\n arrows"
					+ "\n	first     -> { (101, Alan), (102, Camille), (103, Andrey) },"
					+ "\n	last      -> { (101, Turing),(102, Jordan), (103, Markov) },"
					+ "\n	manager   -> { (101, 103), (102, 102), (103, 103) },"
					+ "\n	worksIn   -> { (101, q10), (102, x02), (103, q10) },"
					+ "\n	name      -> { (q10, AppliedMath), (x02, PureMath) },"
					+ "\n	secretary -> { (q10, 101), (x02, 102) };"
					+ "\n} : S\n";


}
