package fql.examples;

import fql.cat.DihedralExample;


/**
 * 
 * @author ryan
 *
 * This class contains the text of the hard-coded demo examples and help dialog.
 */
public class Examples {
	
	public static Example delta = new DeltaExample();
	public static Example pi = new PiExample();
	public static Example sigma = new SigmaExample();
	public static Example composition = new CompositionExample();
	public static Example iso = new IsoExample();
	public static Example triangle = new TriangleExample();
	public static Example cyclicgroup = new CyclicGroupExample();
	public static Example employess = new EmployeesExample();
	public static Example dihedral = new DihedralExample();
	
	public static Example[] examples = { delta, pi, sigma, employess, composition, iso, triangle, cyclicgroup, dihedral };
	
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
			+ "\nFor sigma/pi query generation, signatures must be finite."
			+ "\n"
			+ "\nInstance values must be single words, unless they are quoted"
			+ "\n"
			+ "\nIn applets, copy and paste do not work outside the browser, and no open/save"
			+ "\n"
			+ "\nThe web menu offers a local web server input option"
			+ "\nProject webpage: wisnesky.net/fql.html\n"
			;
	
	
}
