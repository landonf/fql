package fql.decl;

/**
 * 
 * @author ryan
 *
 * Represents types for attributes
 */
public abstract class Type {

	@Override
	public abstract boolean equals(Object o);
	
	@Override
	public abstract int hashCode();
}
