package fql.parse;


/**
 * 
 * @author ryan
 *
 * Interface for tokenizers.
 */
public interface Tokens {

	/**
	 * @return a new Tokens without the head
	 * @throws BadSyntax
	 *             is there is no head
	 */
	public Tokens pop() throws BadSyntax;
	
	/**
	 * Look ahead without popping
	 * @param n how far ahead to look
	 * @return the nth token, or null if there is none 
	 */
	public String peek(int n);
	
	/**
	 * @return the current token
	 * @throws BadSyntax
	 *             if there is none
	 */
	public String head() throws BadSyntax;
	
}
