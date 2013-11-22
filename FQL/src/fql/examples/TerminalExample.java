package fql.examples;

public class TerminalExample extends Example {

	@Override
	public String getName() {
		return "Terminal Instance";
	}

	@Override
	public String getText() {
		return s;
	}
	
	String s = "enum color = {r,g,b}"
			+ "\nenum num = {1,2}"
			+ "\n"
			+ "\nschema C = {"
			+ "\n	nodes a,b,c;"
			+ "\n	attributes attb:b->color, attc:c->num;"
			+ "\n	arrows f:a->b, g:a->c;"
			+ "\n	equations;"
			+ "\n}"
			+ "\n"
			+ "\ninstance I = unit C"
			+ "\n"
			+ "\ninstance J = {"
			+ "\n	nodes a -> {1,2,3}, b -> {1,2}, c -> {1,3};"
			+ "\n	attributes attb -> {(1,r),(2,g)}, attc -> {(1,1),(3,2)};"
			+ "\n	arrows f -> {(1,1),(2,1),(3,2)}, g -> {(1,1),(2,1),(3,3)};"
			+ "\n} : C"
			+ "\n"
			+ "\ntransform K = I.unit J";








;

}
