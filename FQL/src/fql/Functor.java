package fql;


public interface Functor<ObjA, ArrowA, ObjB, ArrowB> {
	
	
	Category<ObjB, ArrowB> apply (Category<ObjA, ArrowA> c);

}
