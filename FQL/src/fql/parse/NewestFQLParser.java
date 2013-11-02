package fql.parse;

import java.util.LinkedList;
import java.util.List;

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
import fql.decl.InstExp;
import fql.decl.MapExp;
import fql.decl.NewestFQLProgram;
import fql.decl.NewestFQLProgram.NewDecl;
import fql.decl.SigExp;
import fql.decl.SigExp.Const;

public class NewestFQLParser {

	static final Parser<Integer> NUMBER = Terminals.IntegerLiteral.PARSER
			.map(new Map<String, Integer>() {
				public Integer map(String s) {
					return Integer.valueOf(s);
				}
			});

	static String[] ops = new String[] { ",", ".", ";", ":", "{", "}", "(",
			")", "=", "->", "+", "*", "^", "|" };

	static String[] res = new String[] { "nodes", "attributes", "schema",
			"transform", "dist1", "dist2", "arrows", "equations", "id",
			"delta", "sigma", "pi", "SIGMA", "apply", "eq", "relationalize",
			"external", "then", "query", "instance", "fst", "snd", "inl",
			"inr", "curry", "mapping", "eval", "string", "int", "void", "unit",
			"prop", "tt", "ff" };

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
		return Parsers.or(
				Parsers.tuple(schemaDecl().source().peek(), schemaDecl()),
				Parsers.tuple(instanceDecl().source().peek(), instanceDecl()),
				Parsers.tuple(mappingDecl().source().peek(), mappingDecl()))
				.many();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static final Parser<?> sig() {
		Reference ref = Parser.newReference();

		Parser<?> plusTy = Parsers.between(term("("),
				Parsers.tuple(ref.lazy(), term("+"), ref.lazy()), term(")"));
		Parser<?> prodTy = Parsers.between(term("("),
				Parsers.tuple(ref.lazy(), term("*"), ref.lazy()), term(")"));
		Parser<?> expTy = Parsers.between(term("("),
				Parsers.tuple(ref.lazy(), term("^"), ref.lazy()), term(")"));

		Parser<?> a = Parsers.or(new Parser<?>[] { term("void"), term("unit"),
				plusTy, prodTy, expTy, ident(), schemaConst() });

		ref.set(a);

		return a;
	}

	public static final Parser<?> schemaDecl() {
		return Parsers.tuple(term("schema"), ident(), term("="), sig());
	}

	public static final Parser<?> schemaConst() {
		Parser<?> p1 = ident();
		Parser<?> p2 = Parsers.tuple(ident(), term(":"), ident(), term("->"),
				type());
		Parser<?> pX = Parsers.tuple(ident(), term(":"), ident(), term("->"),
				ident());
		Parser<?> p3 = Parsers.tuple(path(), term("="), path());
		Parser<?> foo = Parsers.tuple(section("nodes", p1),
				section("attributes", p2), section("arrows", pX),
				section("equations", p3));
		return Parsers.between(term("{"), foo, term("}"));
	}

	public static final SigExp toSchema(Object o) {
		try {
			Tuple3<?,?,?> t = (Tuple3<?,?,?>) o;
			Token z = (Token) t.b;
			String y = z.toString();
			if (y.equals("+")) {
				return new SigExp.Plus(toSchema(t.a), toSchema(t.c));
			} else if (y.equals("*")) {
				return new SigExp.Times(toSchema(t.a), toSchema(t.c));
			}
			if (y.equals("^")) {
				return new SigExp.Exp(toSchema(t.a), toSchema(t.c));

			}
		} catch (RuntimeException cce) {
		}

		try {
			if (o.toString().equals("unit")) {
				return new SigExp.One();
			} else if (o.toString().equals("void")) {
				return new SigExp.Zero();
			}
			throw new RuntimeException();
		} catch (RuntimeException cce) {
		}

		try {
			// System.out.println("ta is " + t.a);
			// System.out.println("tb is " + t.b);
			return toSchemaConst(o);
		} catch (RuntimeException cce) {
			// System.out.println(o);
			// System.out.println(f1.of(o));
			// System.out.println(f2.of(o));
			// cce.printStackTrace();
		}

		// System.out.println(o.getClass());
		// System.out.println(o);
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
		List attrs1 = (List) attrs0.b;
		List arrows1 = (List) arrows0.b;
		List eqs1 = (List) eqs0.b;

		for (Object o : nodes1) {
			nodes.add((String) o);
		}
		for (Object o : attrs1) {
			Tuple5 x = (Tuple5) o;
			attrs.add(new Triple<>((String) x.a, (String) x.c, ((Token) x.e)
					.toString()));
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

	public static final Parser<?> mappingDecl() {
		return Parsers.tuple(term("mapping"), ident(), term("="), mapping());
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static final Parser<?> mapping() {
		Reference ref = Parser.newReference();

		Parser plusTy = Parsers.between(term("("),
				Parsers.tuple(ref.lazy(), term("|"), ref.lazy()), term(")"));
		Parser prodTy = Parsers.between(term("("),
				Parsers.tuple(ref.lazy(), term(","), ref.lazy()), term(")"));
		Parser compTy = Parsers.between(term("("),
				Parsers.tuple(ref.lazy(), term("then"), ref.lazy()), term(")"));

		Parser a = Parsers.or(new Parser[] { Parsers.tuple(term("tt"), sig()),
				Parsers.tuple(term("ff"), sig()),
				Parsers.tuple(term("fst"), sig(), sig()),
				Parsers.tuple(term("snd"), sig(), sig()),
				Parsers.tuple(term("inl"), sig(), sig()),
				Parsers.tuple(term("inr"), sig(), sig()),
				Parsers.tuple(term("apply"), sig(), sig()),
				Parsers.tuple(term("curry"), ref.lazy()),
				Parsers.tuple(term("eq"), sig()),
				Parsers.tuple(term("id"), sig()),
				Parsers.tuple(term("dist1"), sig(), sig(), sig()),
				Parsers.tuple(term("dist2"), sig(), sig(), sig()), compTy,
				plusTy, prodTy, ident(), mappingConst()});
//				Parsers.tuple(, sig(), sig()) });

		ref.set(a);

		return a;

	}

	public static final Parser<?> mappingConst() {
		Parser<?> node = Parsers.tuple(ident(), term("->"), ident());
		Parser<?> arrow = Parsers.tuple(ident(), term("->"), path());

		Parser<?> xxx = Parsers.tuple(section("nodes", node),
				section("attributes", node), section("arrows", arrow));

		Parser<?> p1 = Parsers.between(term("{"), xxx, term("}"));

		return Parsers.tuple(p1, term(":"), sig(), term("->"), sig());
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

	@SuppressWarnings("rawtypes")
	public static final MapExp toMapping(Object o) {
		try {
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
			} else if (p1.equals("inl")) {
				return new MapExp.Inl(toSchema(p2), toSchema(p3));
			} else if (p1.equals("inr")) {
				return new MapExp.Inr(toSchema(p2), toSchema(p3));
			} else if (p1.equals("apply")) {
				return new MapExp.Apply(toSchema(p2), toSchema(p3));
			}
		} catch (RuntimeException re) {

		}

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
			} else if (p1.equals("inl")) {
				return new MapExp.Inl(toSchema(p2), toSchema(p3));
			} else if (p1.equals("inr")) {
				return new MapExp.Inr(toSchema(p2), toSchema(p3));
			} else if (p1.equals("apply")) {
				return new MapExp.Apply(toSchema(p2), toSchema(p3));
			} else if (p2.toString().equals("then")) {
				return new MapExp.Comp(toMapping(o1), toMapping(p3));
			} else if (p2.toString().equals(",")) {
				return new MapExp.Prod(toMapping(o1), toMapping(p3));
			} else if (p2.toString().equals("|")) {
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
			} else if (p1.equals("tt")) {
				return new MapExp.TT(toSchema(p2));
			} else if (p1.equals("ff")) {
				return new MapExp.FF(toSchema(p2));
			}
		} catch (RuntimeException re) {

		}

		// TODO delta, sigma, pi, SIGMA, external, relationalize
		if (o instanceof String) {
			return new MapExp.Var(o.toString());
		}

		System.out.println("tomapping");
		System.out.println(o);
		System.out.println(o.getClass());
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

		Parser<?> external = Parsers.tuple(term("external"), ident(), sig());
		Parser<?> delta = Parsers.tuple(term("delta"), mapping(), ref.lazy());
		Parser<?> sigma = Parsers.tuple(term("sigma"), mapping(), ref.lazy());
		Parser<?> pi = Parsers.tuple(term("pi"), mapping(), ref.lazy());
		Parser<?> SIGMA = Parsers.tuple(term("SIGMA"), mapping(), ref.lazy());
		Parser<?> relationalize = Parsers.tuple(term("relationalize"), ident());

		Parser a = Parsers.or(new Parser[] {
				Parsers.tuple(term("prop"), sig()),
				Parsers.tuple(term("void"), sig()),
				Parsers.tuple(term("unit"), sig()), plusTy, prodTy, expTy,
				ident(), instanceConst(), delta, sigma, pi, SIGMA, external,
				relationalize });

		ref.set(a);

		return a;
	}

	public static final Parser<?> instanceDecl() {
		return Parsers.tuple(term("instance"), ident(), term("="), instance());
//				Parsers.tuple(term(":"), sig(), term("=")), instance());
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

		Parser<?> xxx = Parsers.tuple(section("nodes", node),
				section("attributes", arrow), section("arrows", arrow));
		Parser<?> constant = Parsers.tuple(
				Parsers.between(term("{"), xxx, term("}")), term(":"), sig());
		return constant;
	}

	@SuppressWarnings("rawtypes")
	public static InstExp toInstConst(Object decl) {
		Tuple3 y = (Tuple3) decl;
		Tuple3 x = (Tuple3) y.a;

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
				l.add(new Pair<>(h, h));
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
				l.add(new Pair<>(k.a, k.c));
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
				l.add(new Pair<>(k.a, k.c));
			}
			data.add(new Pair<>(n, l));
		}
		return new InstExp.Const(data, toSchema(y.c));
	}

	@SuppressWarnings("rawtypes")
	public static final InstExp toInst(Object o) {
		try {
			Tuple3 t = (Tuple3) o;
			Token z = (Token) t.a;
			String y = z.toString();
			if (y.equals("delta")) {
				return new InstExp.Delta(toMapping(t.b), toInst(t.c));
			} else if (y.equals("sigma")) {
				return new InstExp.Sigma(toMapping(t.b), toInst(t.c));
			} else if (y.equals("SIGMA")) {
				return new InstExp.FullSigma(toMapping(t.b), toInst(t.c));
			} else if (y.equals("pi")) {
				return new InstExp.Pi(toMapping(t.b), toInst(t.c));
			} else if (y.equals("external")) {
				return new InstExp.External(toSchema(t.b), t.c.toString());
			}
		} catch (RuntimeException cce) {
		}

		try {
			Tuple3 t = (Tuple3) o;
			Token z = (Token) t.b;
			String y = z.toString();
			if (y.equals("+")) {
				return new InstExp.Plus(toInst(t.a), toInst(t.c));
			} else if (y.equals("*")) {
				return new InstExp.Times(toInst(t.a), toInst(t.c));
			}
			if (y.equals("^")) {
				return new InstExp.Exp(toInst(t.a), toInst(t.c));
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
				return new InstExp.Relationalize(toInst(pr.b));
			}
			throw new RuntimeException();
		} catch (RuntimeException cce) {
		}

		try {
			// System.out.println("ta is " + t.a);
			// System.out.println("tb is " + t.b);
			return toInstConst(o);
		} catch (RuntimeException cce) {
			// System.out.println(o);
			// System.out.println(f1.of(o));
			// System.out.println(f2.of(o));
			// cce.printStackTrace();
		}

		// System.out.println(o.getClass());
		// System.out.println(o);
		return new InstExp.Var(o.toString());
	}

	@SuppressWarnings("rawtypes")
	public static final NewestFQLProgram program(String s) {
		List<NewDecl> ret = new LinkedList<>();
		List decls = (List) NewestFQLParser.program.parse(s);

		for (Object d : decls) {
			org.codehaus.jparsec.functors.Pair pr = (org.codehaus.jparsec.functors.Pair) d;
			Object decl = pr.b;
			String txt = pr.a.toString();
			int idx = s.indexOf(txt);
			if (idx < 0) {
				throw new RuntimeException();
			}
			// System.out.println("whasabi  " + s.indexOf(txt));
			// s.indexOf(txt);
			Tuple3 t = (Tuple3) decl;
			String kind = ((Token) t.a).toString();
			switch (kind) {
			case "schema":
				Tuple4 tt = (Tuple4) decl;
				String name = (String) tt.b;

				ret.add(NewestFQLProgram.NewDecl.sigDecl(name, idx,
						toSchema(tt.d)));

				break;
			case "instance":
				Tuple4 tt0 = (Tuple4) decl;
				name = (String) t.b;

//				Tuple3 xxx = (Tuple3) tt0.c;
				//
				// System.out.println("trying on " + tt0.d);
				// Poly<Poly<Unit, NewSigConst>, NewInstConst> aaa = toTy(f2x,
				// f1x, tt0.d);
				// System.out.println(">>>" + aaa);
				//
				// Poly<Unit, NewSigConst> bbb = toTy(f2, f1, xxx.b);
				// System.out.println("<<<" + bbb);
				//
				NewDecl toAdd = NewestFQLProgram.NewDecl.instDecl(name, idx,
						toInst(tt0.d));
				ret.add(toAdd);

				break;
			case "mapping":
				Tuple4 t0 = (Tuple4) decl;
				name = (String) t.b;
/*
				xxx = (Tuple3) t0.c; // :, (x, ->, x, =), stuff
				Tuple4 yyy = (Tuple4) xxx.b;
				Object t1 = yyy.a;
				Object t2 = yyy.c;
				Object o = xxx.c;
				*/
				ret.add(NewestFQLProgram.NewDecl.mapDecl(name, idx,
						toMapping(t0.d))); //, new Pair<>(toSchema(t1), toSchema(t2))));
				// // ret.add(mappingDecl(decl));
				break;
			default:
				throw new RuntimeException("Unknown decl: " + kind);
			}
		}

		return new NewestFQLProgram(ret);
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

	private static Parser<?> string() {
		return Parsers.or(Terminals.StringLiteral.PARSER,
				Terminals.IntegerLiteral.PARSER, Terminals.Identifier.PARSER);
	}

}