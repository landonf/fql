package fql.examples;

public class GraphExample extends Example {

	@Override
	public String getName() {
		return "Connected Components";
	}

	@Override
	public String getText() {
		return s;
	}
	
	String s = "schema Graph = {"
+ "\n	nodes arrow,vertex;"
+ "\n	attributes;"
+ "\n	arrows src:arrow->vertex,tgt:arrow->vertex;"
+ "\n	equations;"
+ "\n}"
+ "\n"
+ "\n//has 4 connected components"
+ "\ninstance G = {"
+ "\n	nodes arrow->{a,b,c,d,e}, vertex->{t,u,v,w,x,y,z};"
+ "\n	attributes;"
+ "\n	arrows"
+ "\n		src->{(a,t),(b,t),(c,x),(d,y),(e,z)},"
+ "\n		tgt->{(a,u),(b,v),(c,x),(d,z),(e,y)};"
+ "\n} : Graph"
+ "\n"
+ "\nschema Terminal = {"
+ "\n	nodes X;"
+ "\n	attributes;"
+ "\n	arrows;"
+ "\n	equations;"
+ "\n} "
+ "\n"
+ "\nmapping F = {"
+ "\n	nodes arrow->X, vertex->X;"
+ "\n	attributes;"
+ "\n	arrows src->X, tgt->X;"
+ "\n} : Graph -> Terminal"
+ "\n"
+ "\n//has 4 rows"
+ "\ninstance Components=SIGMA F G";


}
