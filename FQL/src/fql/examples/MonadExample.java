package fql.examples;

public class MonadExample extends Example {

	@Override
	public String getName() {
		return "Monads";
	}

	@Override
	public String getText() {
		return s;
	}


String s = ""; /*"schema One = {"
+ "\n	nodes a;"
+ "\n	attributes attx:a->string,atty:a->string,attz:a->string;"
+ "\n	arrows;"
+ "\n	equations;"
+ "\n}"
+ "\n"
+ "\nschema D = {"
+ "\n	nodes x,y,z;"
+ "\n	attributes attx:x->string,atty:y->string,attz:z->string;"
+ "\n	arrows f:x->z, g:y->z;"
+ "\n	equations;"
+ "\n}"
+ "\n"
+ "\nmapping ex = {"
+ "\n	nodes x->a, y->a, z->a;"
+ "\n	attributes attx->attx, atty->atty, attz->attz;"
+ "\n	arrows f->a, g->a;"
+ "\n} : D -> One "
+ "\n"
+ "\ninstance I = {"
+ "\n	nodes x->{1,2,3},y->{4,5,6,7},z->{ev,od};"
+ "\n	attributes attx->{(1,1),(2,2),(3,3)},atty->{(4,4),(5,5),(6,6),(7,7)},attz->{(ev,even),(od,odd)};"
+ "\n	arrows f->{(1,od),(2,ev),(3,od)},g->{(4,ev),(5,od),(6,ev),(7,od)};"
+ "\n} :  D"
+ "\n"
+ "\ninstance J = pi ex I"
+ "\n"
+ "\ninstance K = delta ex J";
*/
}

