package fql.gui;

import javax.swing.JPanel;

import fql.FQLException;
import fql.decl.Environment;

/**
 * 
 * @author ryan
 *
 * Collects together the GUI methods used.
 *
 * @param <X>
 */
public interface Viewable<X> {

	public JPanel text();
	
	public JPanel pretty(final Environment env) throws FQLException;
	
	public JPanel view() throws FQLException;
	
	public JPanel denotation() throws FQLException;
	
	public JPanel initial() throws FQLException;

//	public boolean equals0(X view);

	public String type();

	public JPanel join() throws FQLException;

	public JPanel json();
	

}
