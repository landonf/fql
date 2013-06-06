package fql.examples;

public class TypedEmployeesExample extends Example {

	@Override
	public String getName() {
		return "Typed employees";
	}

	@Override
	public String getText() {
		return employeesDefinitions;
	}
	
	public static final String employeesDefinitions = 
			"schema S = { "
			+ "\n  manager : Employee -> Employee,"
			+ "\n  worksIn : Employee -> Department,"
			+ "\n  secretary : Department -> Employee,"
			+ "\n  name : Department -> string,"
			+ "\n  first : Employee -> string,"
			+ "\n  last : Employee -> string"
			+ "\n            ;"
			+ "\n  Employee.manager.worksIn = Employee.worksIn,"
			+ "\n  Department.secretary.worksIn = Department,"
			+ "\n  Employee.manager.manager = Employee.manager"
			+ "\n}\n\n"
			+ "instance I : S = {"
			+ "\nEmployee = {"
			+ "\n    101,"
			+ "\n    102,"
			+ "\n    103 },"
			+ "\nDepartment = {"
			+ "\n    q10,"
			+ "\n    x02};"
			+ "\nfirst = {"
			+ "\n    (101, Alan),"
			+ "\n    (102, Camille),"
			+ "\n    (103, Andrey) },"
			+ "\nlast = {"
			+ "\n    (101, Turing),"
			+ "\n    (102, Jordan),"
			+ "\n    (103, Markov) },"
			+ "\nmanager = {"
			+ "\n    (101, 103),"
			+ "\n    (102, 102),"
			+ "\n    (103, 103) },"
			+ "\nworksIn = {"
			+ "\n    (101, q10),"
			+ "\n    (102, x02),"
			+ "\n    (103, q10) },"
			+ "\nname = {"
			+ "\n    (q10, AppliedMath),"
			+ "\n    (x02, PureMath) },"
			+ "\nsecretary = {"
			+ "\n    (q10, 101),"
			+ "\n    (x02, 102) }"
			+ "\n}" + "\n";


}
