package fql;

/**
 * 
 * @author ryan
 *
 * Command to take equality (of schemas, mappings, etc)
 */
public class EqCommand extends Command {

	public String lhs;
	public String rhs;

	public EqCommand(String text, String lhs, String rhs) {
		super(text);
		this.lhs = lhs;
		this.rhs = rhs;
	}

}
