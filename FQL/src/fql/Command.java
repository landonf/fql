package fql;


/**
 * Superclass for commands like "show" or "eq"
 * 
 * @author ryan
 *
 */
public class Command {

	String text;
	
	public Command(String text) {
		this.text = text;
	}

}
