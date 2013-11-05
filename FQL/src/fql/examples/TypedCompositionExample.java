package fql.examples;

public class TypedCompositionExample extends Example {

	@Override
	public String getName() {
		return "Typed Composition";
	}

	@Override
	public String getText() {
		return s;
	}
	
	String s = "schema S = { nodes s ; attributes satt : s -> string; arrows; equations; }"
			+ "\nschema T = { nodes t1,t2 ; attributes t1att : t1 -> string; arrows; equations; }"
			+ "\nschema B = { nodes b1,b2; attributes b1att : b1 -> string,b2att : b2 -> string; arrows; equations; }"
			+ "\nschema A = { nodes a1,a2,a3,a4; attributes a1att : a1 -> string,a2att : a2 -> string; arrows; equations; }"
			+ "\n"
			+ "\nmapping s = { nodes b1 -> s, b2 -> s; attributes b1att -> satt,b2att -> satt; arrows; } : B -> S"
			+ "\nmapping f = { nodes b1 -> a1, b2 -> a2 ; attributes b1att -> a1att,b2att -> a2att; arrows; } : B -> A "
			+ "\nmapping t = { nodes a1 -> t1, a2 -> t1, a3 -> t2,a4->t2 ; attributes a1att -> t1att,a2att->t1att; arrows; } : A -> T "
			+ "\n"
			+ "\nquery q1 = delta s pi f sigma t"
			+ "\n"
			+ "\ninstance I = {"
			+ "\n	nodes s->{1,2,3};"
			+ "\n	attributes satt->{(1,one),(2,two),(3,three)};"
			+ "\n	arrows;"
			+ "\n	} : S"
			+ "\n"
			+ "\ninstance q1I  =eval q1 I"
			+ "\n"
			+ "\nschema D = { nodes d1,d2,d3 ;  attributes d1att : d1 -> string; arrows; equations; }"
			+ "\nschema C = { nodes c1,c2 ;  attributes c1att : c1 -> string; arrows; equations; }"
			+ "\nschema U = { nodes u1,u2 ;  attributes u1att : u1 -> string; arrows; equations;}"
			+ "\n"
			+ "\nmapping u = { nodes d1 -> t1, d2 -> t1 , d3->t2; attributes d1att -> t1att; arrows;} : D -> T "
			+ "\n"
			+ "\nmapping g = { nodes d1 -> c1, d2 -> c2,d3->c2 ; attributes d1att -> c1att; arrows;} : D -> C"
			+ "\n"
			+ "\nmapping v = { nodes c1 -> u1,c2->u2 ;  attributes c1att -> u1att; arrows; } : C -> U"
			+ "\n"
			+ "\nquery q2 = delta u pi g sigma v"
			+ "\nquery q  = (q1 then q2)"
			+ "\n"
			+ "\n/*"
			+ "\n * The following two queries should be equal when applied to I. "
			+ "\n * The first q2q1I is manual composition of q1 and q2."
			+ "\n * The second qI is formulaic composition."
			+ "\n */"
			+ "\n "
			+ "\ninstance q2q1I=eval q2 q1I"
			+ "\ninstance qI= eval q I";

;


}
