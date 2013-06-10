package fql.parse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;

/**
 * 
 * @author ryan
 *
 * Tokenizer for FQL.
 */
public class FqlTokenizer implements Tokens {

		private List<String> words;
	
		private String[] symbols = new String[] { ",", ":", ";",
				"->", ".", "{", "}", "(", ")", "=", "[", "]" };
		
		private String comment_start = "/*";
		private String comment_end = "*/";
		
		private String quote = "\"";
		
		private String space = " ";
		private String tab = "\t";
		private String linefeed = "\n";
		private String carriagereturn = "\r";
		
		private FqlTokenizer(List<String> s) {
			words = s;
		}
		
		public FqlTokenizer(String s) throws BadSyntax {
			BufferedReader br = new BufferedReader(new StringReader(s));
			StringBuffer sb = new StringBuffer();
			String l;
			try {
				while ((l = br.readLine()) != null) {
					int i = l.indexOf("//");
					if (i == -1) {
						sb.append(l);
						sb.append("\n");
					} else {
						String k = l.substring(0, i);
						sb.append(k);
						sb.append("\n");
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException();
			}
			
			
			words = tokenize(sb.toString());
		}
		
		private static enum State { IN_QUOTE, IN_COMMENT, NORMAL };
		
		private List<String> tokenize(String input) throws BadSyntax {
			List<String> ret = new LinkedList<>();
			
			State state = State.NORMAL;
			String quote_state = "";
			String comment_state = "";
			String token_state = "";
			for (;;) {
//				System.out.println("-----");
//				System.out.println("input: [" + input + "]");
//				System.out.println("state: " + state);
//				System.out.println("token: [" + token_state + "]");
//				System.out.println("comment: [" + comment_state + "]");
//				System.out.println("quote: [" + quote_state + "]");
//				System.out.println("-----");
				
				if (input.length() == 0) {
					switch (state) {
					case NORMAL: 
						if (token_state.length() != 0) {
							ret.add(token_state);
						}
						return ret;
					case IN_QUOTE : throw new BadSyntax(this, "Unfinished quote: " + quote_state);
					case IN_COMMENT : throw new BadSyntax(this, "Unfinished comment: " + comment_state);
					}
				}
				if (input.startsWith(comment_start)) {
					switch (state) {
					case NORMAL : 
						if (token_state.length() != 0) {
							ret.add(token_state); 
							token_state = "";
						}
						state = State.IN_COMMENT;
						break;
					case IN_QUOTE :
						quote_state += comment_start;
						break;
					case IN_COMMENT :
						comment_state += comment_start;
						break;
					}
					input = input.substring(comment_start.length());
					continue;
				} 
				if (input.startsWith(comment_end)) {
					switch (state) {
					case NORMAL :
						throw new BadSyntax(this, "No comment to end: " + token_state);
					case IN_QUOTE :
						quote_state += comment_end;
						break;
					case IN_COMMENT :
						comment_state = "";
						state = State.NORMAL;
						break;
					}
					input = input.substring(comment_end.length());
					continue;
				}
				if (input.startsWith(quote)) {
					switch (state) {
					case NORMAL:
						if (token_state.length() != 0) {
							ret.add(token_state); 
							token_state = "";
						}
						state = State.IN_QUOTE;
						break;						
					case IN_QUOTE:
						ret.add(quote_state);
						state = State.NORMAL;
						quote_state = "";
						break;
					case IN_COMMENT:
						comment_state += quote;
						break;
					}
					ret.add(quote);
					input = input.substring(1);
					continue;
				}
				if (input.startsWith(space) || input.startsWith(tab) || input.startsWith(linefeed) || input.startsWith(carriagereturn)) {
					switch (state) {
					case NORMAL: 
						if (token_state.length() != 0) {
							ret.add(token_state); 
							token_state = "";
						}
						break;
					case IN_COMMENT: 
						comment_state += input.substring(0, 1);
						break;
					case IN_QUOTE: 
						quote_state += input.substring(0, 1);
						break;
					}
					input = input.substring(1); 
					continue;
				}
				
				String matched = matchSymbol(input);
				if (matched == null) {
					switch (state) {
					case NORMAL: 
						token_state += input.substring(0,1);
						break;
					case IN_COMMENT:
						comment_state += input.substring(0,1);
						break;
					case IN_QUOTE:
						quote_state += input.substring(0,1);
						break;
					}
					input = input.substring(1);
					continue;
				}
				
				switch (state) {
				case NORMAL :
					if (token_state.length() != 0) {
						ret.add(token_state); 
						token_state = "";
					}
					ret.add(matched);
					break;
				case IN_COMMENT:
					comment_state += matched;
					break;
				case IN_QUOTE:
					quote_state += matched;
					break;
				}
				input = input.substring(matched.length());

			}
		}
		
	private String matchSymbol(String input) {
			for (String symbol : symbols) {
				if (input.startsWith(symbol)) {
					return symbol;
				}
			}
			return null;
		}

		//
//		public Tokens(String s) {
//			boolean skip = false;
//			List<String> words0 = new LinkedList<String>();
//			List<String> x = Arrays.asList(s.split("\\s+"));
//			for (String w : x) {
//				if (w.trim().equals("%")) {
//					skip = !skip;
//					continue;
//				}
//				if (skip) {
//					continue;
//				}
//				if (!w.startsWith("*")) {
//					words0.add(w);
//				}
	//
//			}
//			words = new LinkedList<String>();
//			for (String word : words0) {
//				expandWord(words, word);
//			}
//			
//			words = squashQuoted(words);
//		}
	//	
//		private List<String> squashQuoted(List<String> w) {
//			Iterator<String> it = w.iterator();
//			boolean looking = false;
//			List<String> found = null;
//			List<String> ret = new LinkedList<>();
//			while (it.hasNext()) {
//				String s = it.next();
//				if (s.equalsIgnoreCase("\"")) {
//					if (!looking) {
//						found = new LinkedList<>();
//					} else {
//						ret.add("\"");
//						ret.add(concat(found));
//						ret.add("\"");
//						found = null;
//					}
//					looking = !looking;
//				} else {
//					if (looking) {
//						found.add(s);
//					} else {
//						ret.add(s);
//					}
//				}
//						
//			}
//					
//			return ret;
//		}
	//
//		private String concat(List<String> s) {
//			String ret = "";
//			for (String a : s) {
//		//		ret += " ";
//				ret += a;
//			//	ret += " ";
//			}
//			return ret;
//		}
	//	
//		public void expandWord(List<String> l, String word) {
//			if (word.equals("")) {
//				return;
//			}
//			int x = -1;
//			String s = null;
//			for (int i = 0; i < word.length(); i++) {
//				if ((s = check(word.charAt(i))) != null) {
//					if (word.substring(0, i).trim().length() != 0) {
//						l.add(word.substring(0, i));
//					}
//					if (s.trim().length() != 0) {
//						l.add(s);
//					}
//					x = i;
//					break;
//				}
//			}
//			if (x == -1 && word.trim().length() != 0) {
//				l.add(word);
//				return;
//			} else {
//				expandWord(l, word.substring(x + 1));
//			}
//		}
	//
//		public String check(char c) {
//			for (String x : symbols) {
//				if (x.equals(Character.toString(c))) {
//					return x;
//				}
//			}
//			return null;
//		}
	//
		public String head() throws BadSyntax {
			try {
				return words.get(0);
			} catch (IndexOutOfBoundsException e) {
				throw new BadSyntax(this, "Premature end of input");
			}
		}
		
		public String peek(int n) {
			try {
				return words.get(n);
			} catch (IndexOutOfBoundsException e) {
				return "";
			}
		}

		public Tokens pop() throws BadSyntax {
			List<String> ret = new LinkedList<String>(words);
			try {
				ret.remove(0);
			} catch (IndexOutOfBoundsException e) {
				throw new BadSyntax(this, "Premature end of input");
			}
			return new FqlTokenizer(ret);
		}

		public String toString() {
		//	int i = 0;
			String s = "";
			for (String w : words) {
				s = s + " " + w + " ";
			//	i++;
//				if (i == 10) { 
//					s += " ... ";
//					break;
//				}
			}
			return (s + "\n");
		}
		
		public String toString2() {
			int i = 0;
			String s = "";
			for (String w : words) {
				s = s + " " + w + " ";
				i++;
				if (i == 10) { 
					s += " ... ";
					break;
				}
			}
			return (s + "\n");
		}

		@Override
		public List<String> words() {
			return words;
		}
	
	//}

}
