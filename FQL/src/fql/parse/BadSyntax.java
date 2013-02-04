package fql.parse;

public class BadSyntax extends Exception {

	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unused")
	private BadSyntax() {
	}

	public BadSyntax(String s) {
		super(s);
	}
}
