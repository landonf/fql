package fql;

import javax.swing.JPanel;

public interface Viewable<X> {

	public JPanel text();
	
	public JPanel pretty() throws FQLException;
	
	public JPanel view() throws FQLException;

	public String plan() throws FQLException;

	public boolean equals0(X view);

	public boolean iso(X view);

	public String isos(X view);

	public String homos(X view);

	public String type();
	

}
