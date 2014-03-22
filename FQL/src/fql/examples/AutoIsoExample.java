package fql.examples;

public class AutoIsoExample extends Example {

	@Override
	public String getName() {
		return "Auto Iso";
	}

	@Override
	public String getText() {
		return s;
	}

	String s = "schema A = {"
			+ "\n	nodes a1, a2;"
			+ "\n	attributes;"
			+ "\n	arrows af : a1 -> a2;"
			+ "\n	equations;"
			+ "\n}"
			+ "\n"
			+ "\nschema B = {"
			+ "\n	nodes b1, b2, b3;"
			+ "\n	attributes;"
			+ "\n	arrows bf1 : b1 -> b2, bf2 : b2 -> b3;"
			+ "\n	equations;"
			+ "\n}"
			+ "\n"
			+ "\nschema T = (A*B)"
			+ "\n"
			+ "\n// T1 is isomorphic to T."
			+ "\nschema T1 = {"
			+ "\n	nodes x1,y1,z1,x2,y2,z2;"
			+ "\n	attributes;"
			+ "\n	arrows f1:x1->y1, g1:y1->z1, f2:x2->y2, g2:y2->z2, hx:x1->x2,hy:y1->y2, hz:z1->z2;"
			+ "\n	equations x1.f1.hy=x1.hx.f2, y1.g1.hz=y1.hy.g2;"
			+ "\n}"
			+ "\n"
			+ "\nschema U = {"
			+ "\n	nodes a,b;"
			+ "\n	attributes;"
			+ "\n	arrows f:a->b;"
			+ "\n	equations;"
			+ "\n}"
			+ "\n"
			+ "\nmapping test = {"
			+ "\n	nodes x1->a,x2->a,y1->a,y2->b,z1->a, z2->b;"
			+ "\n	attributes;"
			+ "\n	arrows f1->a,hx->a,f2->a.f,hy->a.f,g1->a,hz->a.f,g2->b;"
			+ "\n} : T1 -> U"
			+ "\n"
			+ "\n//Get test as a functor T->U using i1"
			+ "\nmapping i1 = iso1 T T1"
			+ "\nmapping test1 = (i1 then test)"
			+ "\n"
			+ "\nmapping i2 = iso2 T T1"
			+ "\nmapping f1 = (i1 then i2)"
			+ "\nmapping f2 = (i2 then i1)"
			+ "\n\n////// isomorphisms of instances"
			+ "\n"
			+ "\ninstance I = {"
			+ "\n	nodes a1 -> {a,b}, a2 -> {c,d,e};"
			+ "\n	attributes;"
			+ "\n	arrows af -> {(a,c),(b,d)};"
			+ "\n} : A"
			+ "\n"
			+ "\ninstance J = {"
			+ "\n	nodes a1 -> {u,v}, a2 -> {x,y,z};"
			+ "\n	attributes;"
			+ "\n	arrows af -> {(u,x),(v,y)};"
			+ "\n} : A"
			+ "\n"
			+ "\ntransform tx = iso1 I J"
			+ "\ntransform ty = iso2 I J"
			+ "\n"
			+ "\ntransform id1 = (tx then ty)"
			+ "\ntransform id2 = (ty then tx)"
;


}
