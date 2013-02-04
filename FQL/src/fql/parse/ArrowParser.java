package fql.parse;

import fql.Triple;

public class ArrowParser implements Parser<Triple<String, String, String>> {

	@Override
	public Partial<Triple<String, String, String>> parse(Tokens s)
			throws BadSyntax, IllTyped {
		try {
			Parser<String> p = new StringParser();
			
			Partial<String> x = p.parse(s);		
			String a = x.value;
			
			Parser<?> colon = new KeywordParser(":");
			Parser<?> arrow = new KeywordParser("->");
			
			Partial<?> temp1 = colon.parse(x.tokens);
	
			x = p.parse(temp1.tokens);
			String b = x.value;
			
			Partial<?> temp2 = arrow.parse(x.tokens);
			
			x = p.parse(temp2.tokens);
			String c = x.value;
			
			return new Partial<Triple<String,String,String>>(x.tokens, new Triple<String,String,String>(a,b,c));
		} catch (Exception e) {
			
		}
		try {
			Parser<String> p = new StringParser();
			
			Partial<String> x = p.parse(s);	
			
			return new Partial<Triple<String,String,String>>(x.tokens, new Triple<String,String,String>(x.value, null, null));
		} catch (Exception e) {
			throw new BadSyntax("Could not parse arrow from " + s);
		}
	}

}
