package fql;

public class IsoCommand extends Command {
	
	String lhs, rhs;

	public IsoCommand(String text, String lhs, String rhs) {
		super(text);
		this.lhs = lhs;
		this.rhs = rhs;
	}

}
