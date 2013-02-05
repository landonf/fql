package fql;

/**
 * 
 * @author ryan
 *
 * Relational variables as syntax.
 */
public class Relvar extends RA {

	String r;
	
	public Relvar(String r) {
		this.r = r;
	}
	
	@Override
	public String toString() {
		return r;
	}
	
}
