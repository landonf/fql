package fql.sql;

/**
 * 
 * @author ryan
 * 
 *         RA syntax for key generation.
 */
public class Keygen extends RA {

	RA e;

	public Keygen(RA e) {
		this.e = e;
	}

	@Override
	public String toString() {
		return "(KEYGEN " + e + ")";
	}

}
