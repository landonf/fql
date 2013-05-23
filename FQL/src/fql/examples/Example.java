package fql.examples;


public abstract class Example {
	
	public abstract String getName();
	
	public abstract String getText();

	@Override 
	public String toString() {
		return getName();
	}
	
	
}
