package fql.examples;



/**
 * 
 * @author ryan
 *
 * This class contains the text of the hard-coded demo examples and help dialog.
 */
public class Examples {
	
	public static Example sql2fql = new SqlToFqlExample();
	public static Example foil = new FoilExample();
	public static Example empty = new EmptyExample();
	public static Example typeddelta = new TypedDeltaExample();
	public static Example delta = new DeltaExample();
	public static Example pi = new PiExample();
	public static Example pi2 = new Pi2Example();
	public static Example sigma = new SigmaExample();
	public static Example composition = new CompositionExample();
	public static Example iso = new IsoExample();
	public static Example triangle = new TriangleExample();
	public static Example cyclicgroup = new CyclicExample();
	public static Example employess = new EmployeesExample();
	public static Example dihedral = new DihedralExample();
	public static Example one = new OneExample();
	public static Example typedemployees = new TypedEmployeesExample();
	public static Example typedsigma = new TypedSigmaExample();
	
	public static Example[] examples = { typeddelta, delta, pi, pi2, typedsigma, sigma, employess, typedemployees, composition, iso, triangle, cyclicgroup, dihedral, one, empty, foil, sql2fql };
	
	public static final Example INIT_EXAMPLE = typeddelta;
	
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
			+ "\n\nComments begin with /* and end with */ "
			+ "\n"
			+ "\nFor sigma/pi query generation, signatures must be finite."
			+ "\n\nMapping well-formedness is not checked."
			+ "\n\nKeyboard shotcuts should work."
			+ "\n"
			+ "\nFQL is not case-sensitive, but many SQL systems are."
			+ "\n"
			+ "\nInstance values must be single words, unless they are quoted."
			+ "\n"
			+ "\nMany features are disabled in applets, such as copy/paste,save,all menu items."
			+ "\n"
			+ "\nProject webpage: wisnesky.net/fql.html\n"
			;
	

	
	
}
