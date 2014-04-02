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
	
	String s = 
			"/* you must disable: "
	        + "\n *  - observables, elements, rdf for instances"
			+ "\n *  - graph for transforms"
			+ "\n */"
			+ "\n"
			+ "\nenum dname = {AppliedMath, PureMath}"
			+ "\nenum fname = {Alan, Camille, Andrey}"
			+ "\n"
			+ "\nschema S = { "
			+ "\n nodes"
			+ "\n 	Employee, Department;"
			+ "\n attributes"
			+ "\n	name  : Department -> dname,"
			+ "\n 	first : Employee -> fname;"
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
			+ "\ninstance J = {"
			+ "\n nodes"
			+ "\n	Employee -> { 101, 102, 103 },"
			+ "\n	Department -> { q10, x02 };"
			+ "\n attributes"
			+ "\n	first -> { (101, Alan), (102, Camille), (103, Andrey) },"
			+ "\n	name  -> { (q10, AppliedMath), (x02, PureMath) };"
			+ "\n arrows"
			+ "\n	manager -> { (101, 101), (102, 102), (103, 103) },"
			+ "\n	worksIn -> { (101, q10), (102, x02), (103, q10) },"
			+ "\n	secretary -> { (q10, 101), (x02, 102) };"
			+ "\n} : S"
			+ "\n"
			+ "\ninstance I = {"
			+ "\n nodes"
			+ "\n	Employee -> { 101, 102 },"
			+ "\n	Department -> { q10, x02 };"
			+ "\n attributes"
			+ "\n	first -> { (101, Alan), (102, Camille) },"
			+ "\n	name  -> { (q10, AppliedMath), (x02, PureMath) };"
			+ "\n arrows"
			+ "\n	manager -> { (101, 101), (102, 102) },"
			+ "\n	worksIn -> { (101, q10), (102, x02) },"
			+ "\n	secretary -> { (q10, 101), (x02, 102) };"
			+ "\n} : S"
			+ "\n"
			+ "\ntransform t = {"
			+ "\n	nodes "
			+ "\n		Employee -> { (101,101), (102,102) },"
			+ "\n		Department -> { (q10,q10), (x02,x02) }"
			+ "\n	;"
			+ "\n} : I -> J"
			+ "\n"
			+ "\ninstance prp = prop S"
			+ "\ninstance one = unit S"
			+ "\n"
			+ "\ntransform tru = prp.true one // true"
			+ "\ntransform fals = prp.false one // false"
			+ "\n"
			+ "\ntransform char_t = prp.char t"
			+ "\n"
			+ "\n//these two transforms are equal"
			+ "\ntransform lhs = (t then char_t)"
			+ "\ntransform rhs = (one.unit I then tru)"
			+ "\n"
			+ "\ninstance ker = kernel char_t"
			+ "\ntransform char_t2 = ker.kernel"
			+ "\n//I and ker are isomorphic"
			+ "\ntransform iso = iso1 I ker"
			+ "\ntransform should_equal_t = (iso then char_t2) //= t"
			+ "\n";

}
