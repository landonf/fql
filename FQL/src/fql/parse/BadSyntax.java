package fql.parse;

/**
 * 
 * @author ryan
 * 
 *         Exception for bad syntax
 */
public class BadSyntax extends Exception {

	private static final long serialVersionUID = 1L;

	static Tokens furthest;

	// String f = "unknown";

	@SuppressWarnings("unused")
	private BadSyntax() {
	}

	// keep static singleton of furthest match
	// add line numbers to tokens
	public BadSyntax(Tokens t, String s) {
		super(s);
		if (furthest == null) {
			furthest = t;
			// f = getMessage();
		}
		if (furthest.words().size() > t.words().size()) {
			furthest = t;
			// f = getMessage();
		}
	}

	@Override
	public String getMessage() {

		return super.getMessage(); // + "\n\nlongest parse: " +
									// furthest.toString2();
	}
}
