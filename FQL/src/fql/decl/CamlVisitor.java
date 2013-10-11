package fql.decl;

public interface CamlVisitor<T, C, D, R, E> {
	
	public R visit (E env, Caml.Id<T, C, D> e) ;
	
	public R visit (E env, Caml.Comp<T, C, D> e) ;
	
	public R visit (E env, Caml.Dist1<T, C, D> e) ;
	
	public R visit (E env, Caml.Dist2<T, C, D> e) ;
	
	public R visit (E env, Caml.Var<T, C, D> e) ;
	
	public R visit (E env, Caml.Const<T, C, D> e) ;
	
	public R visit (E env, Caml.TT<T, C, D> e) ;
	
	public R visit (E env, Caml.FF<T, C, D> e);
	
	public R visit (E env, Caml.Fst<T, C, D> e) ;
	
	public R visit (E env, Caml.Snd<T, C, D> e);
	
	public R visit (E env, Caml.Inl<T, C, D> e) ;
	
	public R visit (E env, Caml.Inr<T, C, D> e) ;
	
	public R visit (E env, Caml.Apply<T, C, D> e) ;
	
	public R visit (E env, Caml.Curry<T, C, D> e);
	
	public R visit (E env, Caml.Eq<T, C, D> e) ;
	
	public R visit (E env, Caml.Case<T, C, D> e) ;
	
	public R visit (E env, Caml.Prod<T, C, D> e) ;

}
