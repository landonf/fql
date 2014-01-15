package fql.examples;

public class TypedEmployeesExample extends Example {

	@Override
	public String getName() {
		return "Employees";
	}

	@Override
	public String getText() {
		return s;
	}
	
	public static final String s = 
			"schema S = { "
					+ "\n nodes"
					+ "\n 	Employee, Department;"
					+ "\n attributes"
					+ "\n	name  : Department -> string,"
					+ "\n  	first : Employee -> string,"
					+ "\n  	last  : Employee -> string;"
					+ "\n arrows"
					+ "\n	manager   : Employee -> Employee,"
					+ "\n	worksIn   : Employee -> Department,"
					+ "\n	secretary : Department -> Employee;"
					+ "\n equations  "
					+ "\n  	Employee.manager.worksIn = Employee.worksIn,"
					+ "\n  	Department.secretary.worksIn = Department,"
					+ "\n  	Employee.manager.manager = Employee.manager;"
					+ "\n}"
					+ "\n"
					+ "\ninstance I = {"
					+ "\n nodes"
					+ "\n	Employee -> { 101, 102, 103 },"
					+ "\n	Department -> { q10, x02 };"
					+ "\n attributes"
					+ "\n	first -> { (101, Alan), (102, Camille), (103, Andrey) },"
					+ "\n	last  -> { (101, Turing), (102, Jordan), (103, Markov) },"
					+ "\n	name  -> { (q10, AppliedMath), (x02, PureMath) };"
					+ "\n arrows"
					+ "\n	manager -> { (101, 103), (102, 102), (103, 103) },"
					+ "\n	worksIn -> { (101, q10), (102, x02), (103, q10) },"
					+ "\n	secretary -> { (q10, 101), (x02, 102) };"
					+ "\n} : S\n";



}
