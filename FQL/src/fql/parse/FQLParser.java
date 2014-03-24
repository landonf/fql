package fql.parse;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.Parser.Reference;
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
import fql.decl.FQLProgram;
import fql.decl.FQLProgram.NewDecl;
import fql.decl.FullQueryExp;
import fql.decl.InstExp;
import fql.decl.MapExp;
import fql.decl.QueryExp;
import fql.decl.SigExp;
import fql.decl.SigExp.Const;
import fql.decl.TransExp;

/**
 * 
 * @author ryan
 * 
 *         Parser for FQL programs.
 */
public class FQLParser {

	
	static final Parser<Integer> NUMBER = Terminals.IntegerLiteral.PARSER
			.map(new Map<String, Integer>() {
				public Integer map(String s) {
					return Integer.valueOf(s);
				}
			});

	static String[] ops = new String[] { ",", ".", ";", ":", "{", "}", "(",
			")", "=", "->", "+", "*", "^", "|", "?" };

	static String[] res = new String[] { "return", "coreturn", "opposite", "EVAL", "QUERY", "union",
			"subschema", "match", "drop", "nodes", "attributes", "enum",
			"ASWRITTEN", "schema", "transform", "dist1", "dist2", "arrows",
			"equations", "id", "delta", "sigma", "pi", "SIGMA", "eval", /* "eq" , */
			"relationalize", "external", "then", "query", "instance", "fst",
			"snd", "inl", "inr", "curry", "mapping", "eval", "void", "unit",
			"prop", "iso1", "iso2", "true", "false", "chi" /*, "unchi" */};

	private static final Terminals RESERVED = Terminals.caseSensitive(ops, res);

	static final Parser<Void> IGNORED = Parsers.or(Scanners.JAVA_LINE_COMMENT,
			Scanners.JAVA_BLOCK_COMMENT, Scanners.WHITESPACES).skipMany();

	static final Parser<?> TOKENIZER = Parsers.or(
			(Parser<?>) Terminals.StringLiteral.DOUBLE_QUOTE_TOKENIZER,
			RESERVED.tokenizer(), (Parser<?>) Terminals.Identifier.TOKENIZER,
			(Parser<?>) Terminals.IntegerLiteral.TOKENIZER);

	static Parser<?> term(String... names) {
		return RESERVED.token(names);
	}

	public static Parser<?> ident() {
		return Terminals.Identifier.PARSER;
	}

	public static final Parser<?> program = program().from(TOKENIZER, IGNORED);

	public static final Parser<?> program() {
		return Parsers
				.or(Parsers.tuple(schemaDecl().source().peek(), schemaDecl()),
						Parsers.tuple(instanceDecl().source().peek(),
								instanceDecl()),
						Parsers.tuple(mappingDecl().source().peek(),
								mappingDecl()),
						Parsers.tuple(enumDecl().source().peek(), enumDecl()),
						Parsers.tuple(fullQueryDecl().source().peek(),
								fullQueryDecl()),
						Parsers.tuple(queryDecl().source().peek(), queryDecl()),
						Parsers.tuple(transDecl().source().peek(), transDecl()),
						Parsers.tuple(dropDecl().source().peek(), dropDecl()))
				.many();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static final Parser<?> schema() {
		Reference ref = Parser.newReference();

		Parser<?> plusTy = Parsers.between(term("("),
				Parsers.tuple(ref.lazy(), term("+"), ref.lazy()), term(")"));
		Parser<?> prodTy = Parsers.between(term("("),
				Parsers.tuple(ref.lazy(), term("*"), ref.lazy()), term(")"));
		Parser<?> expTy = Parsers.between(term("("),
				Parsers.tuple(ref.lazy(), term("^"), ref.lazy()), term(")"));
		Parser<?> unionTy = Parsers
				.between(term("("),
						Parsers.tuple(ref.lazy(), term("union"), ref.lazy()),
						term(")"));

		Parser<?> xxx = ident().sepBy(term(",")).between(term("{"), term("}"));

		Parser<?> op = Parsers.tuple(term("opposite"), ref.lazy());

		Parser<?> a = Parsers.or(new Parser<?>[] { term("void"),
				Parsers.tuple(term("unit"), xxx), plusTy, prodTy, expTy,
				unionTy, ident(), schemaConst(), op, term("?") });

		ref.set(a);

		return a;
	}

	public static final Parser<?> enumDecl() {
		return Parsers.tuple(term("enum"), ident(), term("="), Parsers.between(
				term("{"), string().sepBy(term(",")), term("}")));
	}

	public static final Parser<?> queryDecl() {
		return Parsers.tuple(term("query"), ident(), term("="), query());
	}

	public static final Parser<?> fullQueryDecl() {
		return Parsers.tuple(term("QUERY"), ident(), term("="), fullQuery());
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static final Parser<?> query() {
		Reference ref = Parser.newReference();

		Parser p1 = Parsers.tuple(term("delta"), mapping());
		Parser p2 = Parsers.tuple(term("pi"), mapping());
		Parser p3 = Parsers.tuple(term("sigma"), mapping());
		Parser comp = Parsers.tuple(term("("), ref.lazy(), term("then"),
				ref.lazy(), term(")"));
		Parser zzz = Parsers.tuple(ident(), term(","), ident());
		Parser yyy = Parsers.between(term("("), zzz, term(")"));
		Parser xxx = Parsers
				.between(term("{"), yyy.sepBy(term(",")), term("}"));
		Parser mtch = Parsers.tuple(term("match"), xxx, schema(), schema(),
				Terminals.StringLiteral.PARSER);
		Parser ret = Parsers.or(Parsers.tuple(p1, p2, p3), comp, ident(), mtch);

		ref.set(ret);

		return ret;
	}

	// TODO add identity query

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static final Parser<?> fullQuery() {
		Reference ref = Parser.newReference();

		Parser p1 = Parsers.tuple(term("delta"), mapping());
		Parser p2 = Parsers.tuple(term("pi"), mapping());
		Parser p3 = Parsers.tuple(term("SIGMA"), mapping());
		Parser comp = Parsers.tuple(term("("), ref.lazy(), term("then"),
				ref.lazy(), term(")"));
		Parser zzz = Parsers.tuple(ident(), term(","), ident());
		Parser yyy = Parsers.between(term("("), zzz, term(")"));
		Parser xxx = Parsers
				.between(term("{"), yyy.sepBy(term(",")), term("}"));
		Parser mtch = Parsers.tuple(term("match"), xxx, schema(), schema(),
				Terminals.StringLiteral.PARSER);
		Parser ret = Parsers.or(p1, p2, p3, comp, ident(), mtch);

		ref.set(ret);

		return ret;
	}

	public static final Parser<?> schemaDecl() {
		return Parsers.tuple(term("schema"), ident(), term("="), schema());
	}

	public static final Parser<?> schemaConst() {
		Parser<?> p1 = ident();
		Parser<?> p2 = Parsers.tuple(ident(), term(":"), ident(), term("->"),
				ident());
		Parser<?> pX = Parsers.tuple(ident(), term(":"), ident(), term("->"),
				ident());
		Parser<?> p3 = Parsers.tuple(path(), term("="), path());
		Parser<?> foo = Parsers.tuple(section("nodes", p1), Parsers.or(
				(Parser<?>) section("attributes", p2),
				(Parser<?>) Parsers.tuple(term("attributes"),
						term("ASWRITTEN"), term(";"))), section("arrows", pX),
				section("equations", p3));
		return Parsers.between(term("{"), foo, term("}"));
	}

	private static int unknown_idx = 0;
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static final SigExp toSchema(Object o) {
		try {
			Tuple3<?, ?, ?> t = (Tuple3<?, ?, ?>) o;
			Token z = (Token) t.b;
			String y = z.toString();
			if (y.equals("+")) {
				return new SigExp.Plus(toSchema(t.a), toSchema(t.c));
			} else if (y.equals("*")) {
				return new SigExp.Times(toSchema(t.a), toSchema(t.c));
			} else if (y.equals("^")) {
				return new SigExp.Exp(toSchema(t.a), toSchema(t.c));
			} else if (y.equals("union")) {
				return new SigExp.Union(toSchema(t.a), toSchema(t.c));
			}
		} catch (RuntimeException cce) {
		}

		try {
			org.codehaus.jparsec.functors.Pair p = (org.codehaus.jparsec.functors.Pair) o;
			if (p.a.toString().equals("unit")) {
				return new SigExp.One(new HashSet<>((List<String>) p.b));
			} else if (p.a.toString().equals("opposite")) {
				return new SigExp.Opposite(toSchema(p.b));
			}
		} catch (RuntimeException cce) {
		}

		try {
			if (o.toString().equals("void")) {
				return new SigExp.Zero();
			} else if (o.toString().equals("?")) {
				return new SigExp.Unknown("?" + unknown_idx++);
			}
			
			throw new RuntimeException();
		} catch (RuntimeException cce) {
		}

		try {
			return toSchemaConst(o);
		} catch (RuntimeException cce) {
		}

		return new SigExp.Var(o.toString());
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static final SigExp toSchemaConst(Object y) {
		List<String> nodes = new LinkedList<>();
		List<Triple<String, String, String>> attrs = new LinkedList<>();
		List<Triple<String, String, String>> arrows = new LinkedList<>();
		List<Pair<List<String>, List<String>>> eqs = new LinkedList<>();

		Tuple4 s = (Tuple4) y;

		Tuple3 nodes0 = (Tuple3) s.a;
		Tuple3 attrs0 = (Tuple3) s.b;
		Tuple3 arrows0 = (Tuple3) s.c;
		Tuple3 eqs0 = (Tuple3) s.d;

		List nodes1 = (List) nodes0.b;
		List arrows1 = (List) arrows0.b;
		List eqs1 = (List) eqs0.b;

		for (Object o : nodes1) {
			nodes.add((String) o);
		}

		if (attrs0.b.toString().equals("ASWRITTEN")) {
			for (String k : nodes) {
				attrs.add(new Triple<>(k + "_att", k, "string"));
			}
		} else {
			List attrs1 = (List) attrs0.b;
			for (Object o : attrs1) {
				Tuple5 x = (Tuple5) o;
				attrs.add(new Triple<>((String) x.a, (String) x.c, (String) x.e));
			}
		}
		for (Object o : arrows1) {
			Tuple5 x = (Tuple5) o;
			arrows.add(new Triple<>((String) x.a, (String) x.c, (String) x.e));
		}
		for (Object o : eqs1) {
			Tuple3 x = (Tuple3) o;
			eqs.add(new Pair<>((List<String>) x.a, (List<String>) x.c));
		}
		Const c = new Const(nodes, attrs, arrows, eqs);
		return c;
	}

	public static final Parser<?> transDecl() {
		return Parsers
				.tuple(term("transform"), ident(), term("="), transform());
	}

	public static final Parser<?> dropDecl() {
		return Parsers.tuple(term("drop"), ident().many());
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static final Parser<?> transform() {
		Reference ref = Parser.newReference();

		Parser plusTy = Parsers.tuple(ident(), term("."), Parsers.between(
				term("("), Parsers.tuple(ref.lazy(), term("+"), ref.lazy()),
				term(")")));
		Parser prodTy = Parsers.tuple(ident(), term("."), Parsers.between(
				term("("), Parsers.tuple(ref.lazy(), term("*"), ref.lazy()),
				term(")")));
		Parser compTy = Parsers.between(term("("),
				Parsers.tuple(ref.lazy(), term("then"), ref.lazy()), term(")"));

		Parser a = Parsers.or(new Parser[] {
				Parsers.tuple(term("external"), ident(), ident(), ident()),
				Parsers.tuple(ident(), term("."), term("chi"), ident()),
			//	Parsers.tuple(term("unchi"), ident(), ident()),
				Parsers.tuple(ident(), term("."), term("unit"), ident()),
				Parsers.tuple(ident(), term("."), term("void"), ident()),
				Parsers.tuple(ident(), term("."), term("curry"), ident()),
				Parsers.tuple(ident(), term("."), term("fst")),
				Parsers.tuple(ident(), term("."), term("return")),
				Parsers.tuple(ident(), term("."), term("coreturn")),
				Parsers.tuple(ident(), term("."), term("snd")),
				Parsers.tuple(ident(), term("."), term("eval")),
				Parsers.tuple(ident(), term("."), term("true"), ident()),
				Parsers.tuple(ident(), term("."), term("false"), ident()),
				Parsers.tuple(ident(), term("."), term("inl")),
				Parsers.tuple(ident(), term("."), term("inr")),
				Parsers.tuple(term("iso1"), ident(), ident()),
				Parsers.tuple(term("iso2"), ident(), ident()),
				Parsers.tuple(ident(), term("."), term("relationalize")),
				Parsers.tuple(term("delta"), ident(), ident(), ref.lazy()),
				Parsers.tuple(term("sigma"), ident(), ident(), ref.lazy()),
				Parsers.tuple(term("SIGMA"), ident(), ident(), ident()),
				Parsers.tuple(term("pi"), ident(), ident(), ref.lazy()),
				Parsers.tuple(term("relationalize"), ident(), ident(),
						ref.lazy()),
				// Parsers.tuple(term("apply"), sig(), sig()),
				// Parsers.tuple(term("curry"), ref.lazy()),
				// Parsers.tuple(term("eq"), sig()),
				Parsers.tuple(term("id"), ident()),
				// Parsers.tuple(term("dist1"), sig(), sig(), sig()),
				// Parsers.tuple(term("dist2"), sig(), sig(), sig()), compTy,
				compTy, plusTy, prodTy, ident(), transConst() });

		ref.set(a);
		return a;
	}

	public static final Parser<?> transConst() {
		Parser<?> p = Parsers.tuple(term("("), string(), term(","), string(),
				term(")")).sepBy(term(","));

		Parser<?> node = Parsers.tuple(ident(), term("->"),
				p.between(term("{"), term("}")));

		Parser<?> xxx = section("nodes", node);

		Parser<?> p1 = Parsers.tuple(
				Parsers.between(term("{"), xxx, term("}")), term(":"), ident(),
				term("->"), ident());

		return p1;
	}

	@SuppressWarnings({ "rawtypes" })
	public static final TransExp toTransConst(Object decl, String t1, String t2) {

		List<Pair<String, List<Pair<Object, Object>>>> objs = new LinkedList<>();

		Tuple3 a0 = (Tuple3) decl;
		List b0 = (List) a0.b;
		for (Object o : b0) {
			Tuple3 z = (Tuple3) o;
			String p = (String) z.a;

			List<?> q = (List<?>) z.c;
			List<Pair<Object, Object>> l = new LinkedList<>();
			for (Object q0 : q) {
				Tuple5 q1 = (Tuple5) q0;
				l.add(new Pair<>(q1.b, q1.d));
			}

			objs.add(new Pair<>(p, l));
		}

		return new TransExp.Const(objs, t1, t2);

	}

	@SuppressWarnings("rawtypes")
	public static final TransExp toTrans(Object o) {

		try {
			Tuple4 p = (Tuple4) o;
			String src = p.b.toString();
			String dst = p.c.toString();
			TransExp h = toTrans(p.d);
			String kind = p.a.toString();
			if (kind.equals("delta")) {
				return new TransExp.Delta(h, src, dst);
			} else if (kind.equals("pi")) {
				return new TransExp.Pi(h, src, dst);
			} else if (kind.equals("sigma")) {
				return new TransExp.Sigma(h, src, dst);
			} else if (kind.equals("relationalize")) {
				return new TransExp.Relationalize(h, src, dst);
			} else {
				throw new RuntimeException(o.toString());
			}
		} catch (RuntimeException ex) {

		}
		
		try {
			Tuple4 p = (Tuple4) o;
			String src = p.b.toString();
			String dst = p.c.toString();
			String name = p.d.toString();
			String kind = p.a.toString();
			if (kind.equals("external")) {
				return new TransExp.External(src, dst, name);
			} else if (kind.equals("SIGMA")) {
				return new TransExp.FullSigma(name, src, dst);
			} else {
				throw new RuntimeException(o.toString());
			}
		} catch (RuntimeException ex) {

		}

		try {
			Tuple4 p = (Tuple4) o;

			String obj = p.a.toString();
			String dst = p.d.toString();
			if (p.c.toString().equals("void")) {
				return new TransExp.FF(obj, dst);
			} else if (p.c.toString().equals("unit")) {
				return new TransExp.TT(obj, dst);
			} else if (p.c.toString().equals("curry")) {
				return new TransExp.TransCurry(obj, dst);
			} else if (p.c.toString().equals("true")) {
				return new TransExp.Bool(true, dst, obj);
			} else if (p.c.toString().equals("false")) {
				return new TransExp.Bool(false, dst, obj);
			} else if (p.c.toString().equals("chi")) {
				return new TransExp.Chi(obj, dst);
			} 

		} catch (RuntimeException re) {

		}

		try {
			Tuple3 p = (Tuple3) o;

			Object p2 = p.b;
			Object p3 = p.c;
			Object o1 = p.a;
			String p1 = p.a.toString();

			if (p1.toString().equals("iso1")) {
				return new TransExp.TransIso(true, p2.toString(), p3.toString());
			} else if (p1.toString().equals("iso2")) {
				return new TransExp.TransIso(false, p2.toString(), p3.toString());
			} else if (p3.toString().equals("fst")) {
				return new TransExp.Fst(p1);
			} else if (p3.toString().equals("eval")) { 
				return new TransExp.TransEval(p1);
			} else if (p3.toString().equals("relationalize")) {
				return new TransExp.Squash(p1);
			} else if (p3.toString().equals("snd")) {
				return new TransExp.Snd(p1);
			} else if (p3.toString().equals("return")) {
				return new TransExp.Return(p1);
			} else if (p3.toString().equals("coreturn")) {
				return new TransExp.Coreturn(p1);
			} else if (p3.toString().equals("inl")) {
				return new TransExp.Inl(p1);
//			} else if (p1.toString().equals("unchi")) {
	//			return new TransExp.UnChi(p2.toString(), p3.toString());
			} else if (p3.toString().equals("inr")) {
				return new TransExp.Inr(p1);
			} else if (p2.toString().equals("then")) {
				return new TransExp.Comp(toTrans(o1), toTrans(p3));
			} else if (p3 instanceof Tuple3) {
				Tuple3 y = (Tuple3) p3;
				String x = y.b.toString();
				if (x.equals("+")) {
					return new TransExp.Case(p1, toTrans(y.a), toTrans(y.c));
				} else if (x.equals("*")) {
					return new TransExp.Prod(p1, toTrans(y.a), toTrans(y.c));
					// } else if (x.equals("^")) {
					// return new TransExp.(p1, toTrans(y.a), toTrans(y.c));
				} else {
					throw new RuntimeException("foo");
				}
			}

		} catch (RuntimeException re) {
		}

		try {
			Tuple5 p = (Tuple5) o;

			Object p2 = p.c;
			Object p3 = p.e;
			Object o1 = p.a;
			return toTransConst(o1, p2.toString(), p3.toString());
		} catch (RuntimeException re) {
		}

		try {
			org.codehaus.jparsec.functors.Pair p = (org.codehaus.jparsec.functors.Pair) o;
			String p1 = p.a.toString();
			Object p2 = p.b;
			if (p1.equals("id")) {
				return new TransExp.Id(p2.toString());
			}
		} catch (RuntimeException re) {

		}

		if (o instanceof String) {
			return new TransExp.Var(o.toString());
		}

		// System.out.println(o.getClass());
		// System.out.println(o);
		throw new RuntimeException();
	}

	public static final Parser<?> mappingDecl() {
		return Parsers.tuple(term("mapping"), ident(), term("="), mapping());
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static final Parser<?> mapping() {
		Reference ref = Parser.newReference();

		Parser plusTy = Parsers.between(term("("),
				Parsers.tuple(ref.lazy(), term("+"), ref.lazy()), term(")"));
		Parser prodTy = Parsers.between(term("("),
				Parsers.tuple(ref.lazy(), term("*"), ref.lazy()), term(")"));
		Parser compTy = Parsers.between(term("("),
				Parsers.tuple(ref.lazy(), term("then"), ref.lazy()), term(")"));

		Parser<?> xxx = ident().sepBy(term(",")).between(term("{"), term("}"));

		Parser a = Parsers.or(new Parser[] {
				Parsers.tuple(term("unit"), xxx, schema()),
				Parsers.tuple(term("void"), schema()),
				Parsers.tuple(term("iso1"), schema(), schema()),
				Parsers.tuple(term("iso2"), schema(), schema()),
				Parsers.tuple(term("fst"), schema(), schema()),
				Parsers.tuple(term("snd"), schema(), schema()),
				Parsers.tuple(term("inl"), schema(), schema()),
				Parsers.tuple(term("inr"), schema(), schema()),
				Parsers.tuple(term("eval"), schema(), schema()),
				Parsers.tuple(term("opposite"), ref.lazy()),
				Parsers.tuple(term("curry"), ref.lazy()),
			//	Parsers.tuple(term("eq"), schema()),
				Parsers.tuple(term("id"), schema()),
				Parsers.tuple(term("subschema"), schema(), schema()),
			//	Parsers.tuple(term("dist1"), schema(), schema(), schema()),
			//	Parsers.tuple(term("dist2"), schema(), schema(), schema()),
				compTy, plusTy, prodTy, ident(), mappingConst() });

		ref.set(a);
		return a;
	}

	public static final Parser<?> mappingConst() {
		Parser<?> node = Parsers.tuple(ident(), term("->"), ident());
		Parser<?> arrow = Parsers.tuple(ident(), term("->"), path());

		Parser<?> xxx = Parsers.tuple(section("nodes", node),
				section("attributes", node), section("arrows", arrow));

		Parser<?> p1 = Parsers.between(term("{"), xxx, term("}"));

		return Parsers.tuple(p1, term(":"), schema(), term("->"), schema());
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static final MapExp toMapConst(Object decl, SigExp t1, SigExp t2) {
		Tuple3 x = (Tuple3) decl;

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

		return new MapExp.Const(objs, attrs, arrows, t1, t2);

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static final MapExp toMapping(Object o) {
	/*	try {
			Tuple4 p = (Tuple4) o;
			String p1 = p.a.toString();
			Object p2 = p.b;
			Object p3 = p.c;
			Object p4 = p.d;
			if (p1.equals("dst1")) {
				return new MapExp.Dist1(toSchema(p2), toSchema(p3),
						toSchema(p4));
			} else if (p1.equals("dist2")) {
				return new MapExp.Dist2(toSchema(p2), toSchema(p3),
						toSchema(p4)); 
			} else  if (p1.equals("inl")) {
				return new MapExp.Inl(toSchema(p2), toSchema(p3));
			} else if (p1.equals("inr")) {
				return new MapExp.Inr(toSchema(p2), toSchema(p3));
			} else if (p1.equals("eval")) {
				return new MapExp.Apply(toSchema(p2), toSchema(p3));
			}
		} catch (RuntimeException re) {

		}
*/
		try {
			Tuple3 p = (Tuple3) o;

			Object p2 = p.b;
			Object p3 = p.c;
			Object o1 = p.a;
			String p1 = p.a.toString();

			if (p1.equals("fst")) {
				return new MapExp.Fst(toSchema(p2), toSchema(p3));
			} else if (p1.equals("snd")) {
				return new MapExp.Snd(toSchema(p2), toSchema(p3));
			} else if (p1.equals("unit")) {
				return new MapExp.TT(toSchema(p3), new HashSet<>(
						(List<String>) p2));
			} else if (p1.equals("subschema")) {
				return new MapExp.Sub(toSchema(p2), toSchema(p3));
			} else if (p1.equals("inl")) {
				return new MapExp.Inl(toSchema(p2), toSchema(p3));
			} else if (p1.equals("inr")) {
				return new MapExp.Inr(toSchema(p2), toSchema(p3));
			} else if (p1.equals("iso1")) {
				return new MapExp.Iso(true, toSchema(p2), toSchema(p3));
			} else if (p1.equals("iso2")) {
				return new MapExp.Iso(false, toSchema(p2), toSchema(p3));
			} else if (p1.equals("eval")) {
				return new MapExp.Apply(toSchema(p2), toSchema(p3));
			} else if (p2.toString().equals("then")) {
				return new MapExp.Comp(toMapping(o1), toMapping(p3));
			} else if (p2.toString().equals("*")) {
				return new MapExp.Prod(toMapping(o1), toMapping(p3));
			} else if (p2.toString().equals("+")) {
				return new MapExp.Case(toMapping(o1), toMapping(p3));
			}

		} catch (RuntimeException re) {
		}

		try {
			Tuple5 p = (Tuple5) o;

			Object p2 = p.c;
			Object p3 = p.e;
			Object o1 = p.a;
			return toMapConst(o1, toSchema(p2), toSchema(p3));
		} catch (RuntimeException re) {
		}

		try {
			org.codehaus.jparsec.functors.Pair p = (org.codehaus.jparsec.functors.Pair) o;
			String p1 = p.a.toString();
			Object p2 = p.b;
			if (p1.equals("id")) {
				return new MapExp.Id(toSchema(p2));
			} else if (p1.equals("curry")) {
				return new MapExp.Curry(toMapping(p2));
			} else if (p1.equals("void")) {
				return new MapExp.FF(toSchema(p2));
			} else if (p1.equals("opposite")) {
				return new MapExp.Opposite(toMapping(p2));
			}
		} catch (RuntimeException re) {

		}

		if (o instanceof String) {
			return new MapExp.Var(o.toString());
		}

		throw new RuntimeException();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static final Parser<?> instance() {
		Reference ref = Parser.newReference();

		Parser plusTy = Parsers.between(term("("),
				Parsers.tuple(ref.lazy(), term("+"), ref.lazy()), term(")"));
		Parser prodTy = Parsers.between(term("("),
				Parsers.tuple(ref.lazy(), term("*"), ref.lazy()), term(")"));
		Parser expTy = Parsers.between(term("("),
				Parsers.tuple(ref.lazy(), term("^"), ref.lazy()), term(")"));

		Parser<?> external = Parsers.tuple(term("external"), ident(), schema());
		Parser<?> delta = Parsers.tuple(term("delta"), mapping(), ref.lazy());
		Parser<?> sigma = Parsers.tuple(term("sigma"), mapping(), ref.lazy());
		Parser<?> pi = Parsers.tuple(term("pi"), mapping(), ref.lazy());
		Parser<?> SIGMA = Parsers.tuple(term("SIGMA"), mapping(), ref.lazy());
		Parser<?> relationalize = Parsers.tuple(term("relationalize"), ident());
		Parser<?> eval = Parsers.tuple(term("eval"), query(), ref.lazy());
		Parser<?> fullEval = Parsers.tuple(term("EVAL"), fullQuery(),
				ref.lazy());

		Parser a = Parsers.or(new Parser[] {
				Parsers.tuple(term("prop"), schema()),
				Parsers.tuple(term("void"), schema()),
				Parsers.tuple(term("unit"), schema()), plusTy, prodTy, expTy,
				ident(), instanceConst(), delta, sigma, pi, SIGMA, external,
				relationalize, eval, fullEval });

		ref.set(a);

		return a;
	}

	public static final Parser<?> instanceDecl() {
		return Parsers.tuple(term("instance"), ident(), term("="), instance());
	}

	public static final Parser<?> instanceConst() {
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

		Parser<?> xxx = Parsers.tuple(section("nodes", node), Parsers.or(
				(Parser<?>) section("attributes", arrow),
				(Parser<?>) Parsers.tuple(term("attributes"),
						term("ASWRITTEN"), term(";"))),
				section("arrows", arrow));
		Parser<?> constant = Parsers
				.tuple(Parsers.between(term("{"), xxx, term("}")), term(":"),
						schema());
		return constant;
	}

	@SuppressWarnings("rawtypes")
	public static InstExp toInstConst(Object decl) {
		Tuple3 y = (Tuple3) decl;
		Tuple3 x = (Tuple3) y.a;

		// List<Pair<String, List<Pair<Object, Object>>>> data = new
		// LinkedList<>();

		Tuple3 nodes = (Tuple3) x.a;
		Tuple3 arrows = (Tuple3) x.c;
		Tuple3 attrs = (Tuple3) x.b;

		List nodes0 = (List) nodes.b;
		List arrows0 = (List) arrows.b;

		// List<Object> seen = new LinkedList<>();

		List<Pair<String, List<Pair<Object, Object>>>> nodesX = new LinkedList<>();
		for (Object o : nodes0) {
			Tuple3 u = (Tuple3) o;
			String n = (String) u.a;
			List m = (List) u.c;
			List<Pair<Object, Object>> l = new LinkedList<>();
			for (Object h : m) {
				l.add(new Pair<>(h, h));
			}
			// if (seen.contains(n)) {
			// throw new RuntimeException("duplicate field: " + o);
			// }
			// seen.add(n);
			nodesX.add(new Pair<>(n, l));
		}

		// RuntimeException toThrow = null;

		List<Pair<String, List<Pair<Object, Object>>>> attrsX = new LinkedList<>();
		if (attrs.b.toString().equals("ASWRITTEN")) {
			for (Pair<String, List<Pair<Object, Object>>> k : nodesX) {
				attrsX.add(new Pair<>(k.first + "_att", k.second));
			}
		} else {
			List attrs0 = (List) attrs.b;

			for (Object o : attrs0) {

				Tuple3 u = (Tuple3) o;
				String n = (String) u.a;
				List m = (List) u.c;
				List<Pair<Object, Object>> l = new LinkedList<>();
				for (Object h : m) {
					Tuple3 k = (Tuple3) h;
					l.add(new Pair<>(k.a, k.c));
				}
				// if (seen.contains(n)) {
				// toThrow = new RuntimeException("duplicate field: " + n );
				// throw toThrow;
				// }
				// seen.add(n);
				attrsX.add(new Pair<>(n, l));
			}
		}
		List<Pair<String, List<Pair<Object, Object>>>> arrowsX = new LinkedList<>();
		for (Object o : arrows0) {
			Tuple3 u = (Tuple3) o;
			String n = (String) u.a;
			List m = (List) u.c;
			List<Pair<Object, Object>> l = new LinkedList<>();
			for (Object h : m) {
				Tuple3 k = (Tuple3) h;
				l.add(new Pair<>(k.a, k.c));
			}
			// if (seen.contains(n)) {
			// throw new RuntimeException("duplicate field: " + o);
			// }
			// seen.add(n);
			arrowsX.add(new Pair<>(n, l));
		}
		fql.decl.InstExp.Const ret = new InstExp.Const(nodesX, attrsX, arrowsX,
				toSchema(y.c));
		return ret;
	}

	@SuppressWarnings("rawtypes")
	public static final InstExp toInst(Object o) {
		try {
			Tuple3 t = (Tuple3) o;
			Token z = (Token) t.a;
			String y = z.toString();
			if (y.equals("delta")) {
				return new InstExp.Delta(toMapping(t.b), t.c.toString());
			} else if (y.equals("sigma")) {
				return new InstExp.Sigma(toMapping(t.b), t.c.toString());
			} else if (y.equals("SIGMA")) {
				return new InstExp.FullSigma(toMapping(t.b), t.c.toString());
			} else if (y.equals("pi")) {
				return new InstExp.Pi(toMapping(t.b), t.c.toString());
			} else if (y.equals("external")) {
				return new InstExp.External(toSchema(t.b), t.c.toString());
			} else if (y.equals("eval")) {
				return new InstExp.Eval(toQuery(t.b), t.c.toString());
			} else if (y.equals("EVAL")) {
				return new InstExp.FullEval(toFullQuery(t.b), t.c.toString());
			}
		} catch (RuntimeException cce) {
		}

		try {
			Tuple3 t = (Tuple3) o;
			Token z = (Token) t.b;
			String y = z.toString();
			if (y.equals("+")) {
				return new InstExp.Plus(t.a.toString(), t.c.toString());
			} else if (y.equals("*")) {
				return new InstExp.Times(t.a.toString(), t.c.toString());
			}
			if (y.equals("^")) {
				return new InstExp.Exp(t.a.toString(), (t.c).toString());
			}
		} catch (RuntimeException cce) {
		}

		try {
			org.codehaus.jparsec.functors.Pair pr = (org.codehaus.jparsec.functors.Pair) o;

			if (pr.a.toString().equals("unit")) {
				return new InstExp.One(toSchema(pr.b));
			} else if (pr.a.toString().equals("void")) {
				return new InstExp.Zero(toSchema(pr.b));
			} else if (pr.a.toString().equals("prop")) {
				return new InstExp.Two(toSchema(pr.b));
			} else if (pr.a.toString().equals("relationalize")) {
				return new InstExp.Relationalize(pr.b.toString());
			}
			throw new RuntimeException();
		} catch (RuntimeException cce) {
		}

		return toInstConst(o);
	}

	@SuppressWarnings("rawtypes")
	public static QueryExp toQuery(Object o) {
		if (o instanceof Tuple5) {
			Tuple5 t = (Tuple5) o;
			return new QueryExp.Comp(toQuery(t.b), toQuery(t.d));

		} else if (o instanceof Tuple3) {
			Tuple3 x = (Tuple3) o;
			org.codehaus.jparsec.functors.Pair p1 = (org.codehaus.jparsec.functors.Pair) x.a;
			org.codehaus.jparsec.functors.Pair p2 = (org.codehaus.jparsec.functors.Pair) x.b;
			org.codehaus.jparsec.functors.Pair p3 = (org.codehaus.jparsec.functors.Pair) x.c;
			return new QueryExp.Const(toMapping(p1.b), toMapping(p2.b),
					toMapping(p3.b));
		} else {
			return new QueryExp.Var(o.toString());
		}
	}

	@SuppressWarnings("rawtypes")
	public static FullQueryExp toFullQuery(Object o) {
		if (o instanceof Tuple5) {
			Tuple5 t = (Tuple5) o;
			if (t.a.toString().equals("match")) {
				return new FullQueryExp.Match(toMatch(t.b), toSchema(t.c),
						toSchema(t.d), t.e.toString());
			}
			return new FullQueryExp.Comp(toFullQuery(t.b), toFullQuery(t.d));
		} else if (o instanceof org.codehaus.jparsec.functors.Pair) {
			org.codehaus.jparsec.functors.Pair p = (org.codehaus.jparsec.functors.Pair) o;
			if (p.a.toString().equals("delta")) {
				return new FullQueryExp.Delta(toMapping(p.b));
			} else if (p.a.toString().equals("SIGMA")) {
				return new FullQueryExp.Sigma(toMapping(p.b));
			} else if (p.a.toString().equals("pi")) {
				return new FullQueryExp.Pi(toMapping(p.b));
			}
		} else {
			return new FullQueryExp.Var(o.toString());
		}
		throw new RuntimeException();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static Set<Pair<String, String>> toMatch(Object b) {
		List<Tuple3> l = (List<Tuple3>) b;
		Set<Pair<String, String>> ret = new HashSet<>();
		for (Tuple3 k : l) {
			ret.add(new Pair<>(k.a.toString(), k.c.toString()));
		}
		return ret;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static final FQLProgram program(String s) {
		List<NewDecl> ret = new LinkedList<>();
		List decls = (List) FQLParser.program.parse(s);

		for (Object d : decls) {
			org.codehaus.jparsec.functors.Pair pr = (org.codehaus.jparsec.functors.Pair) d;
			Object decl = pr.b;
			String txt = pr.a.toString();
			int idx = s.indexOf(txt);
			if (idx < 0) {
				throw new RuntimeException();
			}

			if (decl instanceof org.codehaus.jparsec.functors.Pair) {
				// System.out.println("a");
				org.codehaus.jparsec.functors.Pair p = (org.codehaus.jparsec.functors.Pair) decl;
				if (p.a.toString().equals("drop")) {
					// System.out.println("b");
					ret.add(NewDecl.dropDecl((List<String>) p.b));
					continue;
					// System.out.println("c");
				} else {
				}
			}
			// System.out.println("whasabi  " + s.indexOf(txt));
			// s.indexOf(txt);
			Tuple3 t = (Tuple3) decl;
			String kind = ((Token) t.a).toString();
			switch (kind) {
			case "enum":
				Tuple4 tte = (Tuple4) decl;
				String name = (String) tte.b;

				List<String> values = (List<String>) tte.d;

				ret.add(NewDecl.typeDecl(name, values, idx));

				break;
			case "query":
				Tuple4 tta = (Tuple4) decl;
				name = (String) tta.b;

				ret.add(NewDecl.queryDecl(name, idx, toQuery(tta.d)));
				break;
			case "QUERY":
				tta = (Tuple4) decl;
				name = (String) tta.b;

				ret.add(NewDecl.fullQuery(name, toFullQuery(tta.d), idx));
				break;
			case "schema":
				Tuple4 tt = (Tuple4) decl;
				name = (String) tt.b;

				ret.add(FQLProgram.NewDecl.sigDecl(name, idx, toSchema(tt.d)));

				break;
			case "instance":
				Tuple4 tt0 = (Tuple4) decl;
				name = (String) t.b;

				NewDecl toAdd = FQLProgram.NewDecl.instDecl(name, idx,
						toInst(tt0.d));
				ret.add(toAdd);

				break;
			case "mapping":
				Tuple4 t0 = (Tuple4) decl;
				name = (String) t.b;

				ret.add(FQLProgram.NewDecl.mapDecl(name, idx, toMapping(t0.d)));
				break;
			case "transform":
				Tuple4 tx = (Tuple4) decl;
				name = (String) tx.b;

				ret.add(FQLProgram.NewDecl.transDecl(name, idx, toTrans(tx.d)));
				break;

			default:
				throw new RuntimeException("Unknown decl: " + kind);
			}
		}

		return new FQLProgram(ret);
	}

	private static Parser<List<String>> path() {
		return Terminals.Identifier.PARSER.sepBy1(term("."));
	}

	public static Parser<?> section(String s, Parser<?> p) {
		return Parsers.tuple(term(s), p.sepBy(term(",")), term(";"));
	}

	private static Parser<?> string() {
		return Parsers.or(Terminals.StringLiteral.PARSER,
				Terminals.IntegerLiteral.PARSER, Terminals.Identifier.PARSER);
	}

}