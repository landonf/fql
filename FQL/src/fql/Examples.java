package fql;


/**
 * 
 * @author ryan
 *
 * This class contains the text of the hard-coded demo examples and help dialog.
 */
public class Examples {
	
	
	
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
			+ "\n    (David, David) };"
			+ "\n LastName = {"
			+ "\n    (Arden, Arden),"
			+ "\n    (Hoover, Hoover),"
			+ "\n    (Jordan, Jordan),"
			+ "\n    (Markov, Markov),"
			+ "\n    (Turing, Turing) };"
			+ "\nDepartmentName = {"
			+ "\n    (AppliedMath, AppliedMath),"
			+ "\n    (Biology, Biology),"
			+ "\n    (PureMath, PureMath) };"
			+ "\nEmployee = {"
			+ "\n    (101, 101),"
			+ "\n    (102, 102),"
			+ "\n    (103, 103) };"
			+ "\nDepartment = {"
			+ "\n    (q10, q10),"
			+ "\n    (x02, x02) };"
			+ "\nfirst = {"
			+ "\n    (101, Alan),"
			+ "\n    (102, Camille),"
			+ "\n    (103, Andrey) };"
			+ "\nlast = {"
			+ "\n    (101, Turing),"
			+ "\n    (102, Jordan),"
			+ "\n    (103, Markov) };"
			+ "\nmanager = {"
			+ "\n    (101, 103),"
			+ "\n    (102, 102),"
			+ "\n    (103, 103) };"
			+ "\nworksIn = {"
			+ "\n    (101, q10),"
			+ "\n    (102, x02),"
			+ "\n    (103, q10) };"
			+ "\nname = {"
			+ "\n    (q10, AppliedMath),"
			+ "\n    (x02, PureMath) };"
			+ "\nsecretary = {"
			+ "\n    (q10, 101),"
			+ "\n    (x02, 102) }"
			+ "\n}" + "\n";

	public static final String employeesCommands = "show S  show I";

	public static final String helpString = "Available definitions (quasi-BNF): \n\n"
			+ "schema S = { [E : N -> N | N], ; [N[.E] = N[.E]], }"
			+ "\n\nmapping F : S -> S = { [(N,N)], ; [(E, [E].)], }"
			+ "\nmapping F = id S"
			+ "\nmapping F = F o F"
			+ "\n\nquery Q = id S"
			+ "\nquery Q = Q o Q"
			+ "\nquery Q = delta Q sigma Q pi Q"
			+ "\n\ninstance I : S = { [V = { [(C,C)], }]; }"
			+ "\ninstance I = eval Q I"
			+ "\ninstance I = delta F I"
			+ "\ninstance I = sigma F I"
			+ "\ninstance I = pi F I"
			+ "\n\ncomments begin with /* and end with */ "
			+ "\n\nnew, open, save, and exit are available in the file menu"
			+ "\ncontrol-Z and control-Y work for redo and undo"
			+ "\ncontrol-C and control-V and control-X and contol-Q work for Copy, Paste, Cut, and Quit"
			+ "\n"
			+ "\nFor sigma/pi query generation, signatures must be acyclic."
			+ "\n"
			+ "\nThe \"employees, pi, etc\" buttons are example FQL programs."
			+ "\n"
			+ "\nInstance values must be single words, unless they are quoted"
			+ "\n"
			+ "\nIn applets, copy and paste do not work outside the browser, and no open/save"
			+ "\n"
			+ "\nThe web menu offers json input options";
	
	public static final String piDefinitions = 
			"schema C = { c : C1 -> C2 ; }\n" +
					"\n" +
					"instance I : C = {\n" +
					"C1 = {(c1A,c1A),(c1B,c1B)};\n" +
					"C2 = {(c2,c2)};\n" +
					"c = {(c1A,c2),(c1B,c2)}\n" +
					"}\n" +
					"\n"+
					"mapping idC = id C\n" +
					"\n" + 
					"instance J = pi idC I\n\n";
	
	public static final String piCommands = "show C show idC\nshow I show J";


	public static final String initialCommands = 
		  "show C"
//		+ "\n" + "show D"
//		+ "\n" + "show F"
		+ "\n" + "show I"
		+ "\n" + "show J"
		+ "\n" + "show J'"
		+ "\n" + "show F"
		+ "\n";

	
	public static final String initialDefinitions = 
			"/* note: this is not a discrete op-fibration */\n\n"
			 + "schema C = {" 
					+ "\n" + "    t1_ssn : T1 -> SSN"
					+ "\n" + "  , t1_first : T1 -> First"
					+ "\n" + "  , t1_last : T1 -> Last"
					+ "\n" + "  , t2_first : T2 -> First"
					+ "\n" + "  , t2_last : T2 -> Last"
					+ "\n" + "  , t2_salary : T2 -> Salary"
					+ "\n" + " ; "
					+ "\n" + "}"
					+ "\n" 
					+ "\n" + "schema D = {"
					+ "\n" + "    ssn : T -> SSN"
					+ "\n" + "  , first : T -> First"
					+ "\n" + "  , last : T -> Last"
					+ "\n" + "  , salary : T -> Salary"
					+ "\n" + " ;"
					+ "\n" + "}"
					+ "\n"
					+ "\n" + "mapping F : C -> D = {"
					+ "\n" + "    (SSN,SSN)"
					+ "\n" + "  , (First,First)"
					+ "\n" + "  , (Last,Last)"
					+ "\n" + "  , (Salary,Salary)"
					+ "\n" + "  , (T1,T)"
					+ "\n" + "  , (T2,T)"
					+ "\n" + " ;"
					+ "\n" + "    (t1_ssn, T.ssn)"
					+ "\n" + "  , (t1_first, T.first)"
					+ "\n" + "  , (t2_first, T.first)"
					+ "\n" + "  , (t1_last, T.last)"
					+ "\n" + "  , (t2_last, T.last)"
					+ "\n" + "  , (t2_salary, T.salary)"
					+ "\n" + "}"
					+ "\n\ninstance J : D = {\n"
					+ " T = { (XF667,XF667),(XF891,XF891),(XF221,XF221) };\n"
					+ " SSN = { (115-234,115-234),(112-988,112-988),(198-887,198-887) };\n"
					+ " First = { (Bob,Bob),(Sue,Sue),(Alice,Alice) };\n"
					+ " Last = { (Smith,Smith),(Jones,Jones)};\n"
					+ " Salary = { (250,250),(300,300),(100,100) };\n"
					+ " ssn = { (XF667,115-234),(XF891,112-988),(XF221,198-887) };\n"
					+ " first = { (XF667,Bob),(XF891,Sue),(XF221,Alice) };\n"
					+ " last = { (XF667,Smith),(XF891,Smith),(XF221,Jones) };\n"
					+ " salary = { (XF667,250),(XF891,300),(XF221,100)}\n"
					+ "}\n"
				//	+ "\n" + "mapping idC = id C"
			//		+ "\n" + "mapping idD = id D"
			//		+ "\n" 
			//		+ "\n" + "query delta = project F join idC union idC"
			//		+ "\n" 
			//		+ "\n" + "query pi = project idC join F union idD"
			//		+ "\n" 
					+ "\n"
					+ "\ninstance I = delta F J\n"
					+ "\ninstance J' = pi F I\n";


	public static final String migrationDefinitions = "schema C = {" +
			"\n g1 : a1 -> b1, g2 : a2 -> b2, g3 : a3 -> b2," +
			"\n h1 : a1 -> c1, h2 : a2 -> c2, h3 : a3 -> c4," +
			"\n c3;" + 
			"\n}" +
			"\n" +
			"\nschema D = {" +
			"\n G : A -> B, H : A -> C;" +
			"\n}" +
			"\n" +
			"\nmapping F : C -> D = {" +
			"\n  (a1,A), (a2,A), (a3,A)," +
			"\n  (b1,B), (b2,B), " +
			"\n  (c1,C), (c2,C), (c3,C), (c4,C)" +
			"\n ; " +
			"\n  (g1,A.G), (g2,A.G), (g3,A.G)," +
			"\n  (h1,A.H), (h2,A.H), (h3,A.H)" +
			"\n}\n\n" + 
			"\ninstance I : C = {"+
			"\n b2 = {(a,a),(b,b),(c,c)};"+
			"\n b1 = {(d,d),(e,e)};"+
			"\n "+
			"\n a3 = {(1,1),(2,2)};"+
			"\n a2 = {(3,3),(4,4),(5,5)};"+
			"\n a1 = {(6,6)};"+
			"\n "+
			"\n c4 = {(f,f),(g,g)};"+
			"\n c3 = {(h,h)};"+
			"\n c2 = {(i,i),(j,j)};"+
			"\n c1 = {(k,k),(l,l)};"+
			"\n "+
			"\n g3 = {(1,a),(2,b)};"+
			"\n g2 = {(3,a),(4,b),(5,c)};"+
			"\n g1 = {(6,d)};"+
			"\n "+
			"\n h3 = {(1,f),(2,g)};"+
			"\n h2 = {(3,i),(4,j),(5,j)};"+
			"\n h1 = {(6,k)}"+
			"\n}\n\n" 
		//	+ "mapping idC = id C\nquery q = project idC join idC union F\n"
			+ "instance J = sigma F I\n";

	public static final String migrationCommands = "show C show D\nshow F \nshow I show J";

	public static String compDefinitions ="schema S = { s ; }" +
			"\nschema T = { t ; }" +
			"\nschema B = { b1,b2 ; }" +
			"\nschema A = { a1,a2,a3 ; }" +
			"\n" +
			"\nmapping s : B -> S = { (b1,s),(b2,s) ; }" +
			"\nmapping f : B -> A = { (b1,a1),(b2,a2) ; }" +
			"\nmapping t : A -> T = { (a1,t),(a2,t),(a3,t) ; }" +
			"\n" +
			"\nquery q1 = delta s pi f sigma t" +
			"\n" +
			"\nschema D = { d1,d2 ; }" +
			"\nschema C = { c ; }" +
			"\nschema U = { u ; }" +
			"\n" +
			"\nmapping u : D -> T = { (d1,t),(d2,t) ; }\n" +
			"\nmapping g : D -> C = { (d1,c),(d2,c) ; }\n" +
			"\nmapping v : C -> U = { (c,u) ; }\n" +
			"\n" + 
			"query q2 = delta u pi g sigma v" +
			"\nquery q = q2 o q1";





}
