package fql.sql;

/**
 * 
 * @author ryan
 *
 * RA syntax for binary products
 */
public class Product extends RA {

	public RA e1, e2;

	public Product(RA e1, RA e2) {
		this.e1 = e1;
		this.e2 = e2;
	}
	
	@Override
	public String toString() {
		return "(" + e1 + " * " + e2 + ")";
	}

}
