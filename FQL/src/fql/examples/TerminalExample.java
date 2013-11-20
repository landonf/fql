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
			+ "\n	nodes a -> {1,2}, b -> {1}, c -> {1};"
			+ "\n	attributes attb -> {(1,r)}, attc -> {(1,1)};"
			+ "\n	arrows f -> {(1,1),(2,1)}, g -> {(1,1),(2,1)};"
			+ "\n} : C"
			+ "\n"
			+ "\ntransform K = I.unit J"





;

}
