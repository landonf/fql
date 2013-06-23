package fql.examples;

public class TypedCompositionExample extends Example {

	@Override
	public String getName() {
		return "Typed composition";
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
			+ "\nmapping s : B -> S = { nodes b1 -> s, b2 -> s; attributes b1att -> satt,b2att -> satt; arrows; }"
			+ "\nmapping f : B -> A = { nodes b1 -> a1, b2 -> a2 ; attributes b1att -> a1att,b2att -> a2att; arrows; }"
			+ "\nmapping t : A -> T = { nodes a1 -> t1, a2 -> t1, a3 -> t2,a4->t2 ; attributes a1att -> t1att,a2att->t1att; arrows; }"
			+ "\n"
			+ "\nquery q1 : S -> T = delta s pi f sigma t"
			+ "\n"
			+ "\ninstance I:S = {"
			+ "\n	nodes s->{1,2,3};"
			+ "\n	attributes satt->{(1,one),(2,two),(3,three)};"
			+ "\n	arrows;"
			+ "\n	}"
			+ "\n"
			+ "\ninstance q1I : T =eval q1 I"
			+ "\n"
			+ "\nschema D = { nodes d1,d2,d3 ;  attributes d1att : d1 -> string; arrows; equations; }"
			+ "\nschema C = { nodes c1,c2 ;  attributes c1att : c1 -> string; arrows; equations; }"
			+ "\nschema U = { nodes u1,u2 ;  attributes u1att : u1 -> string; arrows; equations;}"
			+ "\n"
			+ "\nmapping u : D -> T = { nodes d1 -> t1, d2 -> t1 , d3->t2; attributes d1att -> t1att; arrows;}"
			+ "\n"
			+ "\nmapping g : D -> C = { nodes d1 -> c1, d2 -> c2,d3->c2 ; attributes d1att -> c1att; arrows;}"
			+ "\n"
			+ "\nmapping v : C -> U = { nodes c1 -> u1,c2->u2 ;  attributes c1att -> u1att; arrows; }"
			+ "\n"
			+ "\nquery q2 : T -> U = delta u pi g sigma v"
			+ "\nquery q : S -> U = q1 then q2"
			+ "\n"
			+ "\n/*"
			+ "\n * The following two queries should be equal when applied to I. "
			+ "\n * The first q2q1I is manual composition of q1 and q2."
			+ "\n * The second qI is formulaic composition."
			+ "\n */"
			+ "\n "
			+ "\ninstance q2q1I:U=eval q2 q1I"
			+ "\ninstance qI:U= eval q I";

;


}
