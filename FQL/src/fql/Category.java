package fql;

import java.util.List;

public interface Category<Obj, Arrow> {

	List<Obj> objects();
	
	List<Triple<Arrow, Obj, Obj>> arrows();
	
	Obj src(Arrow a);
	
	Obj dst(Arrow a);
	
	Arrow id(Obj o);
	
	Arrow compose(Arrow a, Arrow b);
	
	
	
}

