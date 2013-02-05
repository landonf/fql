package fql;

/**
 * 
 * @author ryan
 *
 * Command to enumerate homomorphisms.
 */
public class HomosCommand extends Command {

	String lhs, rhs;
	
	public HomosCommand(String text, String lhs, String rhs) {
		super(text);
		this.lhs = rhs;
		this.lhs = rhs;
	}

}
