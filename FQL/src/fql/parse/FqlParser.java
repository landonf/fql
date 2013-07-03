package fql.parse;

import java.util.LinkedList;
import java.util.List;

import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.Parsers;
import org.codehaus.jparsec.Scanners;
import org.codehaus.jparsec.Terminals;
import org.codehaus.jparsec.Token;
import org.codehaus.jparsec.functors.Map;
import org.codehaus.jparsec.functors.Tuple3;
import org.codehaus.jparsec.functors.Tuple4;
import org.codehaus.jparsec.functors.Tuple5;

import fql.Pair;
import fql.Triple;
import fql.decl.ConstantInstanceDecl;
import fql.decl.Decl;
import fql.decl.EvalDSPInstanceDecl;
import fql.decl.EvalInstanceDecl;
import fql.decl.ExternalDecl;
import fql.decl.InstanceDecl;
import fql.decl.MappingDecl;
import fql.decl.Program;
import fql.decl.QueryDecl;
import fql.decl.RelationalizeDecl;
import fql.decl.SignatureDecl;

public class FqlParser {

	static final Parser<Integer> NUMBER = Terminals.IntegerLiteral.PARSER
			.map(new Map<String, Integer>() {
				public Integer map(String s) {
					return Integer.valueOf(s);
				}
			});

	static String[] ops = new String[] { ",", ".", ";", ":", "{", "}", "(",
			")", "=", "->" };

	static String[] res = new String[] { "nodes", "attributes", "schema",
			"arrows", "equations", "id", "delta", "sigma", "pi", "SIGMA",
			"relationalize", "external", "then", "query", "instance",
			"mapping", "eval", "string", "int" };
	// private static final Terminals OPERATORS = Terminals.operators(ops);
	// //",", ".", ";", ":", "{", "}", "(", ")", "=", "->");

	private static final Terminals RESERVED = Terminals.caseSensitive(ops,
			res);
	// Terminals.operators();

	static final Parser<Void> IGNORED = Parsers.or(Scanners.JAVA_LINE_COMMENT,
			Scanners.JAVA_BLOCK_COMMENT, Scanners.WHITESPACES).skipMany();

	static final Parser<?> TOKENIZER = Parsers.or(
			(Parser<?>) Terminals.StringLiteral.DOUBLE_QUOTE_TOKENIZER,
			/* (Parser<?>)Terminals.StringLiteral.PARSER, */
			RESERVED.tokenizer(), (Parser<?>) Terminals.Identifier.TOKENIZER,
			(Parser<?>) Terminals.IntegerLiteral.TOKENIZER);

	static Parser<?> term(String... names) {
		return RESERVED.token(names);
	}

	public static Parser<?> ident() {
		return Terminals.Identifier.PARSER;
	}

	private static Parser<?> schema() {
		Parser<?> p1 = ident();
		Parser<?> p2 = Parsers.tuple(ident(), term(":"), ident(), term("->"),
				type());
		Parser<?> pX = Parsers.tuple(ident(), term(":"), ident(), term("->"),
				ident());
		Parser<?> p3 = Parsers.tuple(path(), term("="), path());
		Parser<?> foo = Parsers.tuple(section("nodes", p1),
				section("attributes", p2), section("arrows", pX),
				section("equations", p3));
		return Parsers.tuple(term("schema"), ident(), term("="),
				Parsers.between(term("{"), foo, term("}")));

	}

	private static Parser<List<String>> path() {
		return Terminals.Identifier.PARSER.sepBy1(term("."));
	}

	public static Parser<?> section(String s, Parser<?> p) {
		return Parsers.tuple(term(s), p.sepBy(term(",")), term(";"));
	}

	private static Parser<?> type() {
		return Parsers.or(term("int"), term("string"));
	}

	private static Parser<?> program() {
		return Parsers.or(schema(), instance(), mapping(), query()).many();
	}

	private static Parser<?> string() {
		return Parsers.or(Terminals.StringLiteral.PARSER,
				Terminals.IntegerLiteral.PARSER, Terminals.Identifier.PARSER);
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

		Parser<?> node = Parsers.tuple(ident(), term("->"), Parsers.between(
				term("{"), string().sepBy(term(",")), term("}")));
		Parser<?> arrow = Parsers.tuple(
				ident(),
				term("->"),
				Parsers.between(
						term("{"),
						Parsers.between(term("("),
								Parsers.tuple(string(), term(","), string()),
								term(")")).sepBy(term(",")), term("}")));

		Parser<?> xxx = Parsers.tuple(section("nodes", node),
				section("attributes", arrow), section("arrows", arrow));
		Parser<?> constant = Parsers.between(term("{"), xxx, term("}"));

		Parser<?> p = Parsers.or(external, constant, dssp, relationalize, eval);
		return Parsers.tuple(term("instance"), ident(),
				Parsers.tuple(term(":"), ident(), term("=")), p);

	}

	private static Parser<?> mapping() {
		Parser<?> p2 = Parsers.tuple(term("id"), ident());

		Parser<?> node = Parsers.tuple(ident(), term("->"), ident());
		Parser<?> arrow = Parsers.tuple(ident(), term("->"), path());

		Parser<?> xxx = Parsers.tuple(section("nodes", node),
				section("attributes", node), section("arrows", arrow));

		Parser<?> p1 = Parsers.between(term("{"), xxx, term("}"));

		Parser<?> p3 = Parsers.tuple(ident(), term("then"), ident());

		Parser<?> p = Parsers.or(p1, p2, p3);
		return Parsers.tuple(term("mapping"), ident(), term(":"),
				Parsers.tuple(ident(), term("->"), ident(), term("=")), p);
	}

	private static Parser<?> query() {
	//	Parser<?> p2 = Parsers.tuple(term("id"), ident());
		Parser<?> delta = Parsers.tuple(term("delta"), ident());
		Parser<?> pi = Parsers.tuple(term("pi"), ident());
		Parser<?> sigma = Parsers.tuple(term("sigma"), ident());
		Parser<?> p1 = Parsers.tuple(delta, pi, sigma);

		Parser<?> p3 = Parsers.tuple(ident(), term("then"), ident());

		//cannot do id for query - what of the intermediate mappings?
		Parser<?> p = Parsers.or(p1, /* p2, */ p3);
		return Parsers.tuple(term("query"), ident(), term(":"),
				Parsers.tuple(ident(), term("->"), ident(), term("=")), p);

	}

	public static final Parser<List<String>> path = path().from(TOKENIZER,
			IGNORED);

	// public static final Parser<?> schema = schema().from(TOKENIZER, IGNORED);

	public static final Parser<?> program = program().from(TOKENIZER, IGNORED);

	@SuppressWarnings("rawtypes")
	public static final Program program(String s) {
		List<Decl> ret = new LinkedList<>();
		List decls = (List) FqlParser.program.parse(s);
		
		for (Object decl : decls) {
			Tuple3 t = (Tuple3) decl;
			String kind = ((Token) t.a).toString();
			switch (kind) {
			case "schema" :
				ret.add(schemaDecl(decl));
				break;
			case "instance" :
				ret.add(instanceDecl(decl));
				break;
			case "mapping" :
				ret.add(mappingDecl(decl));
				break;
			case "query" : 
				ret.add(queryDecl(decl));
				break;
			default: throw new RuntimeException("Unknown decl: " + kind);
			}
		}
		return new Program(ret);
	}

	//tuple 5
	@SuppressWarnings("rawtypes")
	private static QueryDecl queryDecl(Object decl) {
		Tuple5 t = (Tuple5) decl;
		
		String name = (String) t.b;
		
		String src = (String) ((Tuple4)t.d).a;
		String dst = (String) ((Tuple4)t.d).c;
		
		
		if (!(t.e instanceof Tuple3)) {
			return new QueryDecl(name, src, src, dst, 0); //id
		}
		Tuple3 x = (Tuple3) t.e;
		if (!(x.a instanceof String)) { //deltasigmapi
			org.codehaus.jparsec.functors.Pair delta = (org.codehaus.jparsec.functors.Pair) x.a;
			org.codehaus.jparsec.functors.Pair pi = (org.codehaus.jparsec.functors.Pair) x.b;
			org.codehaus.jparsec.functors.Pair sigma = (org.codehaus.jparsec.functors.Pair) x.c;
			return new QueryDecl(name, (String) delta.b, (String) pi.b, (String) sigma.b, src, dst);
		} else { //compose
			String m1 = (String) x.a;
			String m2 = (String) x.c;
			return new QueryDecl(name, m2, m1, src, dst);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static MappingDecl mappingDecl(Object decl) {
		Tuple5 t = (Tuple5) decl;
		
		String name = (String) t.b;
		
		String src = (String) ((Tuple4)t.d).a;
		String dst = (String) ((Tuple4)t.d).c;
		
		//t.e is pair (id), tuple (then), tuple (const)
		if (!(t.e instanceof Tuple3)) {
			return new MappingDecl(name, src, src, dst); //id
		}
		Tuple3 x = (Tuple3) t.e;
		if (!(x.a instanceof String)) {
			List<Pair<String, String>> objs = new LinkedList<>();
			List<Pair<String, String>> attrs = new LinkedList<>();
			List<Pair<String, List<String>>> arrows = new LinkedList<>();
			
			Tuple3 a = (Tuple3) x.a;
			Tuple3 b = (Tuple3) x.b;
			Tuple3 c = (Tuple3) x.c;
			
			List a0 = (List) a.b;
			for (Object o : a0) {
				Tuple3 z = (Tuple3) o;
				String p = (String) z.a;
				String q = (String) z.c;
				objs.add(new Pair<>(p, q));
			}
			
			List b0 = (List) b.b;
			for (Object o : b0) {
				Tuple3 z = (Tuple3) o;
				String p = (String) z.a;
				String q = (String) z.c;
				attrs.add(new Pair<>(p, q));
			}
			
			List c0 = (List) c.b;
			for (Object o : c0) {
				Tuple3 z = (Tuple3) o;
				String p = (String) z.a;
				List<String> q = (List<String>) z.c;
				arrows.add(new Pair<>(p, q));
			}

			return new MappingDecl(name, src, dst, objs, attrs, arrows);
		} else {
			String m1 = (String) x.a;
			String m2 = (String) x.c;
			return new MappingDecl(name, src, dst, m2, m1);
		}
		
		
	}
	
	@SuppressWarnings("rawtypes")
	private static InstanceDecl instanceDecl(Object decl) {
		Tuple4 t = (Tuple4) decl;
		String name = (String) t.b;
		
		String type = (String) ((Tuple3) t.c).b;
		
		if (t.d instanceof Token) {
			return new ExternalDecl(name, type);
		} else if (t.d instanceof Tuple3) {
			Tuple3 x = (Tuple3) t.d;
			
			if (x.a instanceof Tuple3) {
				//constant
				List<Pair<String, List<Pair<Object, Object>>>> data = new LinkedList<>();
				
				Tuple3 nodes = (Tuple3) x.a;
				Tuple3 attrs = (Tuple3) x.b;
				Tuple3 arrows = (Tuple3) x.c;
				
				List nodes0 = (List) nodes.b;
				List attrs0 = (List) attrs.b;
				List arrows0 = (List) arrows.b;
				
				for (Object o : nodes0) {
					Tuple3 u = (Tuple3) o;
					String n = (String) u.a;
					List m = (List) u.c;
					List<Pair<Object, Object>> l = new LinkedList<>();
					for (Object h : m) {
						l.add(new Pair<>(h,h));
					}
					data.add(new Pair<>(n, l));
				}
				
				for (Object o : attrs0) {
					Tuple3 u = (Tuple3) o;
					String n = (String) u.a;
					List m = (List) u.c;
					List<Pair<Object, Object>> l = new LinkedList<>();
					for (Object h : m) {
						Tuple3 k = (Tuple3) h;
						l.add(new Pair<>(k.a,k.c));
					}
					data.add(new Pair<>(n, l));
				}
				
				for (Object o : arrows0) {
					Tuple3 u = (Tuple3) o;
					String n = (String) u.a;
					List m = (List) u.c;
					List<Pair<Object, Object>> l = new LinkedList<>();
					for (Object h : m) {
						Tuple3 k = (Tuple3) h;
						l.add(new Pair<>(k.a,k.c));
					}
					data.add(new Pair<>(n, l));
				}
				
				return new ConstantInstanceDecl(name, type, data);
			} else {
				//delta, sigma, pi, SIGMA, eval		
				String kind = ((Token)x.a).toString();
				String mapping = (String) x.b;
				String inst = (String) x.c;
				if (kind.equals("eval")) {
					return new EvalInstanceDecl(name, mapping, inst, type);
				} else {
					return new EvalDSPInstanceDecl(name, kind, mapping, inst, type);
				}
			}
		} else {
			return new RelationalizeDecl(name, type, (String) ((org.codehaus.jparsec.functors.Pair)t.d).b);
		}
		
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static SignatureDecl schemaDecl(Object decl) {
		Tuple4 t = (Tuple4) decl;
		
		String name = (String) t.b;
		List<String> nodes = new LinkedList<>();
		List<Triple<String, String, String>> attrs = new LinkedList<>();
		List<Triple<String, String, String>> arrows = new LinkedList<>();
		List<Pair<List<String>, List<String>>> eqs = new LinkedList<>();
		
		Tuple4 s = (Tuple4) t.d;
		
		Tuple3 nodes0 = (Tuple3) s.a;
		Tuple3 attrs0 = (Tuple3) s.b;
		Tuple3 arrows0 = (Tuple3) s.c;
		Tuple3 eqs0 = (Tuple3) s.d;
		
		List nodes1 = (List) nodes0.b;
		List attrs1 = (List) attrs0.b;
		List arrows1 = (List) arrows0.b;
		List eqs1 = (List) eqs0.b;
		
		for (Object o : nodes1) {
			nodes.add((String) o);
		}
		for (Object o : attrs1) {
			Tuple5 x = (Tuple5) o;
			attrs.add(new Triple<>((String) x.a, (String) x.c, ((Token) x.e).toString()));
		}
		for (Object o : arrows1) {
			Tuple5 x = (Tuple5) o;
			arrows.add(new Triple<>((String) x.a, (String) x.c, (String) x.e));
		}
		for (Object o : eqs1) {
			Tuple3 x = (Tuple3) o;
			eqs.add(new Pair<>((List<String>) x.a, (List<String>) x.c));
		}
		
		return new SignatureDecl(name, nodes, attrs, arrows, eqs);
	}

}