package fql.decl;

public interface PolyVisitor<T, C, R, E> {
	
	public R visit (E env, Poly.Zero<T, C> e) ;
	
	public R visit (E env, Poly.One<T, C> e) ;
	
	public R visit (E env, Poly.Plus<T, C> e) ;
	
	public R visit (E env, Poly.Times<T, C> e) ;
	
	public R visit (E env, Poly.Exp<T, C> e) ;
	
	public R visit (E env, Poly.Two<T, C> e) ;
	
	public R visit (E env, Poly.Var<T, C> e) ;
	
	public R visit (E env, Poly.Const<T, C> e) ;
}
