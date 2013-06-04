package fql.examples;

public class EmployeesExample extends Example {

	@Override
	public String getName() {
		return "Employees";
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
			+ "\n  name : Department -> DepartmentName,"
			+ "\n  first : Employee -> FirstName,"
			+ "\n  last : Employee -> LastName"
			+ "\n            ;"
			+ "\n  Employee.manager.worksIn = Employee.worksIn,"
			+ "\n  Department.secretary.worksIn = Department"
			+ "\n}\n\n"
			+ "instance I : S = {"
			+ "\n  FirstName = {"
			+ "\n    (Alan, Alan),"
			+ "\n    (Alice, Alice),"
			+ "\n    (Andrey, Andrey),"
			+ "\n    (Camille, Camille),"
			+ "\n    (David, David) },"
			+ "\n LastName = {"
			+ "\n    (Arden, Arden),"
			+ "\n    (Hoover, Hoover),"
			+ "\n    (Jordan, Jordan),"
			+ "\n    (Markov, Markov),"
			+ "\n    (Turing, Turing) },"
			+ "\nDepartmentName = {"
			+ "\n    (AppliedMath, AppliedMath),"
			+ "\n    (Biology, Biology),"
			+ "\n    (PureMath, PureMath) },"
			+ "\nEmployee = {"
			+ "\n    (101, 101),"
			+ "\n    (102, 102),"
			+ "\n    (103, 103) },"
			+ "\nDepartment = {"
			+ "\n    (q10, q10),"
			+ "\n    (x02, x02) },"
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
