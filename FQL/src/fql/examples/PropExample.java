package fql.examples;

public class PropExample extends Example {

	@Override
	public String getName() {
		return "Prop";
	}

	@Override
	public String getText() {
		return s;
	}
	
	String s = "schema S = { "
			+ "\n nodes"
			+ "\n 	Employee, Department;"
			+ "\n attributes;"
	//		+ "\n//	name  : Department -> string,"
	//		+ "\n// 	first : Employee -> string,"
	//		+ "\n//  	last  : Employee -> string;"
			+ "\n arrows"
			+ "\n	manager   : Employee -> Employee,"
			+ "\n	worksIn   : Employee -> Department,"
			+ "\n	secretary : Department -> Employee;"
			+ "\n equations  "
			+ "\n  	Employee.manager.worksIn = Employee.worksIn,"
			+ "\n  	Department.secretary.worksIn = Department,"
			+ "\n  	Employee.manager.manager.manager = Employee.manager.manager;"
			+ "\n}"
			+ "\n"
			+ "\ninstance I = {"
			+ "\n nodes"
			+ "\n	Employee -> { 101, 102, 103 },"
			+ "\n	Department -> { q10, x02 };"
			+ "\n attributes;"
	//		+ "\n//	first -> { (101, Alan), (102, Camille), (103, Andrey) },"
	//		+ "\n//	last  -> { (101, Turing), (102, Jordan), (103, Markov) },"
	//		+ "\n//	name  -> { (q10, AppliedMath), (x02, PureMath) };"
			+ "\n arrows"
			+ "\n	manager -> { (101, 103), (102, 102), (103, 103) },"
			+ "\n	worksIn -> { (101, q10), (102, x02), (103, q10) },"
			+ "\n	secretary -> { (q10, 101), (x02, 102) };"
			+ "\n} : S"
			+ "\n"
			+ "\ninstance J = {"
			+ "\n nodes"
			+ "\n  Department -> {p3, q10, x0}, "
			+ "\n  Employee -> {105, 103, 104, 101, 102, 106};"
			+ "\n attributes;"
//			+ "\n // first -> {(105, Emmy), (103, Andrey), (104, Bill), (101, Alan), (102, Camille), (106, Alexandre)}, "
//			+ "\n // last -> {(105, Noether), (103, Markov), (104, Lawvere), (101, Turing), (102, Jordan), (106, Grothendieck)}, "
//			+ "\n // name -> {(p3, "Algebraic Geometry"), (q10, AppliedMath), (x0, PureMath)};"
			+ "\n arrows"
			+ "\n  manager -> {(105, 104), (103, 103), (104, 102), (101, 103), (102, 102), (106, 106)}, "
			+ "\n  secretary -> {(p3, 106), (q10, 101), (x0, 102)}, "
			+ "\n  worksIn -> {(105, x0), (103, q10), (104, x0), (101, q10), (102, x0), (106, p3)};"
			+ "\n} : S"
			+ "\n"
			+ "\ntransform t = {"
			+ "\n	nodes "
			+ "\n		Employee -> {(101,101),(102,102),(103,103)},"
			+ "\n		Department ->{(q10,q10),(x02,x0)}"
			+ "\n	;"
			+ "\n} : I -> J"
			+ "\n"
			+ "\ninstance prp = prop S"
			+ "\ninstance one = unit S"
			+ "\ntransform tru = prp.true one // true"
			+ "\ntransform fals = prp.false one // false"
			+ "\n"
			+ "\ntransform char_t = prp.char t"
			+ "\n"
			+ "\n//these two transforms are equal"
			+ "\ntransform lhs = (t then char_t)"
			+ "\ntransform rhs = (one.unit I then tru)";







}
