package fql.cat;

import fql.cat.FinFunctor;

/**
 * 
 * @author ryan
 * 
 *         A placeholder for semantic queries.
 */
public class SemQuery<ObjA, ArrowA, ObjB, ArrowB, ObjC, ArrowC, ObjD, ArrowD> {

	public FinFunctor<ObjA, ArrowA, ObjB, ArrowB> project;
	public FinFunctor<ObjA, ArrowA, ObjC, ArrowC> join;
	public FinFunctor<ObjC, ArrowC, ObjD, ArrowD> union;

	public SemQuery(FinFunctor<ObjA, ArrowA, ObjB, ArrowB> project,
			FinFunctor<ObjA, ArrowA, ObjC, ArrowC> join,
			FinFunctor<ObjC, ArrowC, ObjD, ArrowD> union) {
		super();
		this.project = project;
		this.join = join;
		this.union = union;
	}

	@Override
	public String toString() {
		return "SemQuery [project=" + project + ", join=" + join + ", union="
				+ union + "]";
	}

}
