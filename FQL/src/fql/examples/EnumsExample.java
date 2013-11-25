package fql.examples;

public class EnumsExample extends Example {

	@Override
	public String getName() {
		return "Enums (User types)";
	}

	@Override
	public String getText() {
		return s;
	}
	
	String s = "enum color = { red, green, blue }"
			+ "\n"
			+ "\nenum quark = { \"top\", \"bottom\", \"up\", \"down\", \"charm\", \"strange\" }"
			+ "\n"
			+ "\nschema S = {"
			+ "\n	nodes S_node;"
			+ "\n	attributes S_att : S_node -> color;"
			+ "\n	arrows;"
			+ "\n	equations;"
			+ "\n}"
			+ "\n"
			+ "\nschema T = {"
			+ "\n	nodes T_node;"
			+ "\n	attributes T_att : T_node -> quark;"
			+ "\n	arrows;"
			+ "\n	equations;"
			+ "\n}"
			+ "\n"
			+ "\ninstance I = {"
			+ "\n	nodes S_node -> {0,1};"
			+ "\n	attributes S_att -> {(0,red),(1,blue)};"
			+ "\n	arrows;"
			+ "\n} : S"
			+ "\n"
			+ "\n/* bad "
			+ "\nmapping F = {"
			+ "\n	nodes S_node -> T_node;"
			+ "\n	attributes S_att -> T_att;"
			+ "\n	arrows;"
			+ "\n} : S -> T"
			+ "\n*/"
			+ "\n"
			+ "\n//good"
			+ "\nmapping F = {"
			+ "\n	nodes S_node -> S_node;"
			+ "\n	attributes S_att -> S_att;"
			+ "\n	arrows;"
			+ "\n} : S -> S"


;

}

