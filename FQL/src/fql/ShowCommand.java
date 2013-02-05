package fql;

/**
 * 
 * @author ryan
 *
 * Show command syntax.
 */
public class ShowCommand extends Command {

	String name;
	
	public ShowCommand(String name) {
		super("show " + name);
		this.name = name;
	}

	@Override
	public String toString() {
		return "ShowCommand [name=" + name + "]";
	}
}
