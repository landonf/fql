package fql;

public class PlanCommand extends Command {

	String name;

	@Override
	public String toString() {
		return "PlanCommand []";
	}

	public PlanCommand(String name) {
		super("Plan " + name);
		this.name = name;
	}

}
