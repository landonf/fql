package fql;

public class EqCommand extends Command {

	public String lhs;
	public String rhs;

	public EqCommand(String text, String lhs, String rhs) {
		super(text);
		this.lhs = lhs;
		this.rhs = rhs;
	}

}
