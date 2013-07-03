package fql.parse;

import java.util.List;

import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.Parsers;
import org.codehaus.jparsec.Scanners;
import org.codehaus.jparsec.Terminals;
import org.codehaus.jparsec.functors.Map;

public class FqlParsers {
	

	 
	  
	  static final Parser<Integer> NUMBER = Terminals.IntegerLiteral.PARSER.map(new Map<String, Integer>() {
	      public Integer map(String s) {
	        return Integer.valueOf(s);
	      }
	    });
	     
		 static String[] ops = new String[] {",", ".", ";", ":", "{", "}", "(", ")", "=", "->"};

		 static String[] res = new String[] {"nodes", "attributes", "schema", "arrows", "equations", "id", "delta", "sigma", "pi", "SIGMA", "relationalize", "external", "then", "query", "instance", "mapping", "eval", "string", "int"};
//	  private static final Terminals OPERATORS = Terminals.operators(ops); //",", ".", ";", ":", "{", "}", "(", ")", "=", "->");
	  
	  
	  private static final Terminals RESERVED = Terminals.caseInsensitive(ops, res);
			  //Terminals.operators();
	  
	  static final Parser<Void> IGNORED =
	      Parsers.or(Scanners.JAVA_LINE_COMMENT, Scanners.JAVA_BLOCK_COMMENT, Scanners.WHITESPACES).skipMany();
	      
	  static final Parser<?> TOKENIZER =
		      Parsers.or((Parser<?>)Terminals.StringLiteral.DOUBLE_QUOTE_TOKENIZER,
		    		  /* (Parser<?>)Terminals.StringLiteral.PARSER, */
		    		  RESERVED.tokenizer(), 
		    		  (Parser<?>)Terminals.Identifier.TOKENIZER, 
		    		  (Parser<?>) Terminals.IntegerLiteral.TOKENIZER);
	  
	  static Parser<?> term(String... names) {
	    return RESERVED.token(names);
	  }
	  
//	  static <T> Parser<T> op(String name, T value) {
//	    return term(name).retn(value);
//	  }
	  
	  public static Parser<?> ident() {
		  return Terminals.Identifier.PARSER;
	  }
	  
	  
//	  public static Parser<?> reserved(String s) {
//		  
//	  }
	  private static Parser<?> schema() {
		  Parser<?> p1 = ident();
		  Parser<?> p2 = Parsers.tuple(ident(), term(":"), ident(), term("->"), type());
		  Parser<?> pX = Parsers.tuple(ident(), term(":"), ident(), term("->"), ident());
		  Parser<?> p3 = Parsers.tuple(path(), term("="), path());
		  Parser<?> foo = Parsers.tuple(section("nodes", p1), section("attributes", p2), section("arrows", pX), section("equations", p3));
		  return Parsers.tuple(term("schema"), ident(), term("="), Parsers.between(term("{"), foo, term("}")));
		 // return p3;
	//	  return path();
	  }
	  
	  private static Parser<List<String>> path() {
	   return Terminals.Identifier.PARSER.sepBy1(term("."));
	  }
	  
	  public static Parser<?> section(String s, Parser<?> p) {
		  return Parsers.tuple(term(s), p.sepBy(term(",")), term(";"));
//		  return term(s).next(p.sepBy(term(","))).next(term(";"));
	  }
	  
	  private static Parser<?> type() {
		  return Parsers.or(term("int"), term("string"));
	  }
	  
	  private static Parser<?> program() {
		  return Parsers.or(schema(), instance(), mapping(), query()).many();
	  }
	
	  //
	  private static Parser<?> string() {		  
		  return Parsers.or(Terminals.StringLiteral.PARSER, Terminals.IntegerLiteral.PARSER, Terminals.Identifier.PARSER);
	  }
	  
	  private static Parser<?> instance() {
		  Parser<?> external = term("external");
		  Parser<?> delta = Parsers.tuple(term("delta"), ident(), ident());
		  Parser<?> sigma = Parsers.tuple(term("sigma"), ident(), ident());
		  Parser<?> pi = Parsers.tuple(term("pi"), ident(), ident());
		  Parser<?> SIGMA = Parsers.tuple(term("SIGMA"), ident(), ident());	
		  Parser<?> dssp = Parsers.or(delta, sigma, pi, SIGMA);
		  Parser<?> relationalize = Parsers.tuple(term("relationalize"), ident());
		  Parser<?> eval = Parsers.tuple(term("eval"), ident(), ident());
		  
		  Parser<?> node = Parsers.tuple(ident(), term("->"), Parsers.between(term("{"), string().sepBy(term(",")), term("}")));
		  Parser<?> arrow = Parsers.tuple(ident(), term("->"), Parsers.between(term("{"), Parsers.between(term("("), Parsers.tuple(string(), term(","), string()), 
				                                                                                          term(")")).sepBy(term(",")), term("}")));
		  
		  Parser<?> xxx = Parsers.tuple(section("nodes", node), section("attributes", arrow), section("arrows", arrow));
		  Parser<?> constant = Parsers.between(term("{"), xxx, term("}"));
		
		  
		Parser<?> p = Parsers.or(external, constant, dssp, relationalize, eval);
		  return Parsers.tuple(term("instance"), ident(), Parsers.tuple(term(":"), ident(), term("=")), p);

		  
	}
	  private static Parser<?> mapping() {
		  Parser<?> p2 = Parsers.tuple(term("id"), ident());

		  Parser<?> node = Parsers.tuple(ident(), term("->"), ident());
		  Parser<?> arrow = Parsers.tuple(ident(), term("->"), path());
		  
		  Parser<?> xxx = Parsers.tuple(section("nodes", node), section("attributes", node), section("arrows", arrow));
		  
		  Parser<?> p1 = Parsers.between(term("{"), xxx, term("}"));
		  
		  Parser<?> p3 = Parsers.tuple(ident(), term("then"), ident());
		  
		  Parser<?> p = Parsers.or(p1, p2, p3);
		return Parsers.tuple(term("mapping"), ident(), term(":"), 
				Parsers.tuple(ident(), term("->"), ident(), term("=")), p);
		}
	  private static Parser<?> query() {
		  Parser<?> p2 = Parsers.tuple(term("id"), ident());
		  Parser<?> delta = Parsers.tuple(term("delta"), ident());
		  Parser<?> pi = Parsers.tuple(term("pi"), ident());
		  Parser<?> sigma = Parsers.tuple(term("sigma"), ident());
		  Parser<?> p1 = Parsers.tuple(delta, pi, sigma);
		  
		  Parser<?> p3 = Parsers.tuple(ident(), term("then"), ident());
		  
		  Parser<?> p = Parsers.or(p1, p2, p3);
		return Parsers.tuple(term("query"), ident(), term(":"), 
				Parsers.tuple(ident(), term("->"), ident(), term("=")), p);
		
	  }


	public static final Parser<List<String>> pathP = path().from(TOKENIZER, IGNORED);
	  
	  public static final Parser<?> schema = schema().from(TOKENIZER, IGNORED);
	  
	  public static final Parser<?> program = program().from(TOKENIZER, IGNORED);
	  
	}