package fql.parse;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Tokens {

	private List<String> words;

	private String[] symbols = new String[] { ",", ":", ";",
			"->", ".", "%", "{", "}", "(", ")", "=", "\"", "[", "]" };

	private Tokens(List<String> s) {
		words = s;
	}

	public Tokens(String s) {
		boolean skip = false;
		List<String> words0 = new LinkedList<String>();
		List<String> x = Arrays.asList(s.split("\\s+"));
		for (String w : x) {
			if (w.trim().equals("%")) {
				skip = !skip;
				continue;
			}
			if (skip) {
				continue;
			}
			if (!w.startsWith("*")) {
				words0.add(w);
			}

		}
		words = new LinkedList<String>();
		for (String word : words0) {
			expandWord(words, word);
		}
	}

	public void expandWord(List<String> l, String word) {
		if (word.equals("")) {
			return;
		}
		int x = -1;
		String s = null;
		for (int i = 0; i < word.length(); i++) {
			if ((s = check(word.charAt(i))) != null) {
				if (word.substring(0, i).trim().length() != 0) {
					l.add(word.substring(0, i));
				}
				if (s.trim().length() != 0) {
					l.add(s);
				}
				x = i;
				break;
			}
		}
		if (x == -1 && word.trim().length() != 0) {
			l.add(word);
			return;
		} else {
			expandWord(l, word.substring(x + 1));
		}
	}

	public String check(char c) {
		for (String x : symbols) {
			if (x.equals(Character.toString(c))) {
				return x;
			}
		}
		return null;
	}

	/**
	 * @return the current token
	 * @throws BadSyntax
	 *             if there is none
	 */
	public String head() throws BadSyntax {
		try {
			return words.get(0);
		} catch (IndexOutOfBoundsException e) {
			throw new BadSyntax("Premature end of input");
		}
	}
	
	public String peek(int n) {
		try {
			return words.get(n);
		} catch (IndexOutOfBoundsException e) {
			return "";
		}
	}

	/**
	 * @return a new Tokens without the head
	 * @throws BadSyntax
	 *             is there is no head
	 */
	public Tokens pop() throws BadSyntax {
		List<String> ret = new LinkedList<String>(words);
		try {
			ret.remove(0);
		} catch (IndexOutOfBoundsException e) {
			throw new BadSyntax("Premature end of input");
		}
		return new Tokens(ret);
	}

	public String toString() {
		String s = "";
		for (String w : words) {
			s = s + " " + w + " ";
		}
		return (s + "\n");
	}

}
