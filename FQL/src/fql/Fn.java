package fql;

/**
 * 
 * @author ryan
 *
 * Interface for functions
 *
 * @param <X> domain
 * @param <Y> range
 */
public interface Fn<X,Y> {

	public abstract Y of(X x) ;
	
}
