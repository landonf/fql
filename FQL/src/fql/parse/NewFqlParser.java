package fql.parse;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;

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

import fql.Fn;
import fql.Pair;
import fql.Triple;
import fql.Unit;
import fql.decl.Caml;
import fql.decl.NewFQLProgram;
import fql.decl.NewFQLProgram.DSP;
import fql.decl.NewFQLProgram.NewDecl;
import fql.decl.NewFQLProgram.NewInstConst;
import fql.decl.NewFQLProgram.NewMapConst;
import fql.decl.NewFQLProgram.NewSigConst;
import fql.decl.NewFQLProgram.NewTransConst;
import fql.decl.Poly;

public class NewFqlParser {

	static final Parser<Integer> NUMBER = Terminals.IntegerLiteral.PARSER
			.map(new Map<String, Integer>() {
				public Integer map(String s) {
					return Integer.valueOf(s);
				}
			});

	static String[] ops = new String[] { ",", ".", ";", ":", "{", "}", "(",
			")", "=", "->", "+", "*", "^", "|" };

	static String[] res = new String[] { "nodes", "attributes", "schema", "transform",
			"dist1", "dist2", "arrows", "equations", "id", "delta", "sigma",
			"pi", "SIGMA", "apply", "eq", "relationalize", "external", "then",
			"query", "instance", "fst", "snd", "inl", "inr", "curry",
			"mapping", "eval", "string", "int", "void", "unit", "prop", "tt",
			"ff" };
	// private static final Terminals OPERATORS = Terminals.operators(ops);
	// //",", ".", ";", ":", "{", "}", "(", ")", "=", "->");

	private static final Terminals RESERVED = Terminals.caseSensitive(ops, res);
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

	private static Parser<?> sig() {
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

	private static Parser<?> schema() {
		return Parsers.tuple(term("schema"), ident(), term("="),
				ty(sig(), Parsers.always()));
	}

	private static Parser<?> mapping() {
		return Parsers.tuple(
				term("mapping"),
				ident(),
				Parsers.tuple(term(":"), Parsers.tuple(
						ty(sig(), Parsers.always()), term("->"),
						ty(sig(), Parsers.always())), term("=")),
				caml(mappingConst(), ty(sig(), Parsers.always())));
	}
	
	private static Parser<?> transform() {
		return Parsers.tuple(
				term("transform"),
				ident(),
				Parsers.tuple(term(":"), Parsers.tuple(
						ty(instanceConst(), ty(sig(), Parsers.always())), term("->"),
						ty(instanceConst(), ty(sig(), Parsers.always()))), term("=")),
				caml(transformConst(), ty(instanceConst(), ty(sig(), Parsers.always()))));
	}
	
	private static Parser<?> instance() {
		return Parsers.tuple(term("instance"), ident(), Parsers.tuple(
				term(":"), ty(sig(), Parsers.always()), term("=")),
				ty(instanceConst(), ty(sig(), Parsers.always())));

		// return ty(instanceConst(), ty());
	}


	@SuppressWarnings("unchecked")
	public static Parser<?> caml(Parser<?> c, Parser<?> t) {
		Reference ref = Parser.newReference();

		Parser plusTy = Parsers.between(term("("),
				Parsers.tuple(ref.lazy(), term("|"), ref.lazy()), term(")"));
		Parser prodTy = Parsers.between(term("("),
				Parsers.tuple(ref.lazy(), term(","), ref.lazy()), term(")"));
		Parser compTy = Parsers.between(term("("),
				Parsers.tuple(ref.lazy(), term("then"), ref.lazy()), term(")"));

		Parser a = Parsers.or(new Parser[] { Parsers.tuple(term("tt"), t),
				Parsers.tuple(term("ff"), t), Parsers.tuple(term("fst"), t, t),
				Parsers.tuple(term("snd"), t, t),
				Parsers.tuple(term("inl"), t, t),
				Parsers.tuple(term("inr"), t, t),
				Parsers.tuple(term("apply"), t, t),
				Parsers.tuple(term("curry"), ref.lazy()),
				Parsers.tuple(term("eq"), t), Parsers.tuple(term("id"), t),
				Parsers.tuple(term("dist1"), t, t, t),
				Parsers.tuple(term("dist2"), t, t, t), compTy, plusTy, prodTy,
				ident(), Parsers.tuple(c, t, t) });

		ref.set(a);

		return a;
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
		return Parsers.or(Parsers.tuple(schema().source().peek(), schema()), 
				          Parsers.tuple(instance().source().peek(), instance()), 
				          Parsers.tuple(mapping().source().peek(), mapping()),
				          Parsers.tuple(transform().source().peek(), transform()),
				          Parsers.tuple(query().source().peek(), query())).many();
//				          Parsers.tuple(query(), query()).many();

//		return Parsers.or(schema(), instance(), mapping(), query()).many();
	}

	private static Parser<?> string() {
		return Parsers.or(Terminals.StringLiteral.PARSER,
				Terminals.IntegerLiteral.PARSER, Terminals.Identifier.PARSER);
	}


	private static Parser<?> transformConst() {
		Parser<?> arrow = Parsers.tuple(
				ident(),
				term("->"),
				Parsers.between(
						term("{"),
						Parsers.between(term("("),
								Parsers.tuple(string(), term(","), string()),
								term(")")).sepBy(term(",")), term("}")));
		return arrow.sepBy(term(",")).between(term("{"), term("}"));
	}
	private static Parser<?> instanceConst() {
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

		return p;
	}

	private static Parser<?> mappingConst() {
		// Parser<?> p2 = Parsers.tuple(term("id"), ident());

		Parser<?> node = Parsers.tuple(ident(), term("->"), ident());
		Parser<?> arrow = Parsers.tuple(ident(), term("->"), path());

		Parser<?> xxx = Parsers.tuple(section("nodes", node),
				section("attributes", node), section("arrows", arrow));

		Parser<?> p1 = Parsers.between(term("{"), xxx, term("}"));

		// Parser<?> p3 = Parsers.tuple(ident(), term("then"), ident());

		// Parser<?> p = Parsers.or(p1, p2, p3);
		// return Parsers.tuple(term("mapping"), ident(), term(":"),
		// Parsers.tuple(ident(), term("->"), ident(), term("=")), p);
		return p1;
	}

	private static Parser<?> query() {
		
		Parser<?> d_p = Parsers.tuple(term("delta"), caml(mappingConst(), ty(sig(), Parsers.always())));
		Parser<?> s_p = Parsers.tuple(term("sigma"), caml(mappingConst(), ty(sig(), Parsers.always())));
		Parser<?> p_p = Parsers.tuple(term("pi"), caml(mappingConst(), ty(sig(), Parsers.always())));
		Parser<?> dsp = Parsers.tuple(d_p, p_p, s_p);
		Parser<?> p = Parsers.tuple(dsp, term("then"), dsp);
		
		return Parsers.tuple(term("query"), ident(), term(":"),
				Parsers.tuple(ident(), term("->"), ident(), term("=")), p);

	}

	public static final Parser<List<String>> path = path().from(TOKENIZER,
			IGNORED);

	// public static final Parser<?> schema = schema().from(TOKENIZER, IGNORED);

	public static final Parser<?> program = program().from(TOKENIZER, IGNORED);

	// public static final Parser<?> ty = ty().from(TOKENIZER, IGNORED);

	public static void main(String[] args) {
		JFrame f = new JFrame();
		JPanel p = new JPanel(new BorderLayout());
		final JTextArea topT = new JTextArea(
				"\n\n\n\n\n\n\n\n\n\n\n\n                                         ");
		final JTextArea botT = new JTextArea();
		JScrollPane top = new JScrollPane(topT);
		JScrollPane bot = new JScrollPane(botT);
		JSplitPane jsp = new JSplitPane(JSplitPane.VERTICAL_SPLIT, top, bot);
		p.add(jsp, BorderLayout.CENTER);
		JPanel btns = new JPanel(new GridLayout(1, 1));
		JButton btn = new JButton("GO");
		btns.add(btn);
		btn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					NewFQLProgram p = program(topT.getText());
					botT.setText(p.toString());
				} catch (Exception ee) {
					botT.setText(ee.toString());
					ee.printStackTrace();
				}
			}

		});
		p.add(btns, BorderLayout.SOUTH);
		f.setContentPane(p);
		f.pack();
		f.setVisible(true);

	}



	public static <T, C, D> Caml<T, C, D> toCaml(Fn<Object, T> f1,
			Fn<Object, C> f2, Fn<Object, D> f3, Object o) {

		// try {
		// Tuple5 p = (Tuple5) o;
		// String p1 = p.a.toString();
		// Object p2 = p.b;
		// Object p3 = p.c;
		// Object p4 = p.d;
		// Object p5 = p.e;
		// if (p3.equals("then")) {
		// return new Caml.Comp<>(toCaml(f1, f2, f3, p2), toCaml(f1, f2, f3,
		// p4));
		// } else if (p3.equals(",")) {
		// return new Caml.Prod<>(toCaml(f1, f2, f3, p2), toCaml(f1, f2, f3,
		// p4));
		// } else if (p3.equals("|")) {
		// return new Caml.Case<>(toCaml(f1, f2, f3, p2), toCaml(f1, f2, f3,
		// p4));
		// }
		// } catch (RuntimeException re) {
		//
		// }

		try {
			Tuple4 p = (Tuple4) o;
			String p1 = p.a.toString();
			Object p2 = p.b;
			Object p3 = p.c;
			Object p4 = p.d;
			if (p1.equals("dst1")) {
				return new Caml.Dist1<>(toTy(f1, f2, p2), toTy(f1, f2, p3),
						toTy(f1, f2, p4));
			} else if (p1.equals("dist2")) {
				return new Caml.Dist2<>(toTy(f1, f2, p2), toTy(f1, f2, p3),
						toTy(f1, f2, p4));
			} else if (p1.equals("inl")) {
				return new Caml.Inl<>(toTy(f1, f2, p2), toTy(f1, f2, p3));
			} else if (p1.equals("inr")) {
				return new Caml.Inr<>(toTy(f1, f2, p2), toTy(f1, f2, p3));
			} else if (p1.equals("apply")) {
				return new Caml.Apply<>(toTy(f1, f2, p2), toTy(f1, f2, p3));
			}
		} catch (RuntimeException re) {

		}

		try {
			Tuple3 p = (Tuple3) o;

			Object p2 = p.b;
			Object p3 = p.c;
			Object o1 = p.a;
			if (!(o instanceof String)) {
				return new Caml.Const<T, C, D>(toTy(f1, f2, p2), toTy(f1, f2, p3), f3.of(o1));
			}
			String p1 = p.a.toString();

			if (p1.equals("fst")) {
				return new Caml.Fst<>(toTy(f1, f2, p2), toTy(f1, f2, p3));
			} else if (p1.equals("snd")) {
				return new Caml.Snd<>(toTy(f1, f2, p2), toTy(f1, f2, p3));
			} else if (p1.equals("inl")) {
				return new Caml.Inl<>(toTy(f1, f2, p2), toTy(f1, f2, p3));
			} else if (p1.equals("inr")) {
				return new Caml.Inr<>(toTy(f1, f2, p2), toTy(f1, f2, p3));
			} else if (p1.equals("apply")) {
				return new Caml.Apply<>(toTy(f1, f2, p2), toTy(f1, f2, p3));
			} else if (p2.toString().equals("then")) {
				return new Caml.Comp<>(toCaml(f1, f2, f3, p1), toCaml(f1, f2,
						f3, p3));
			} else if (p2.toString().equals(",")) {
				return new Caml.Prod<>(toCaml(f1, f2, f3, p1), toCaml(f1, f2,
						f3, p3));
			} else if (p2.toString().equals("|")) {
				return new Caml.Case<>(toCaml(f1, f2, f3, p1), toCaml(f1, f2,
						f3, p3));
			}
			
		} catch (RuntimeException re) {
		}

		try {
			org.codehaus.jparsec.functors.Pair p = (org.codehaus.jparsec.functors.Pair) o;
			String p1 = p.a.toString();
			Object p2 = p.b;
			if (p1.equals("id")) {
				return new Caml.Id<>(toTy(f1, f2, p2));
			} else if (p1.equals("curry")) {
				return new Caml.Curry<>(toCaml(f1, f2, f3, p2));
			} else if (p1.equals("eq")) {
				return new Caml.Eq<>(toTy(f1, f2, p2));
			} else if (p1.equals("tt")) {
				return new Caml.TT<>(toTy(f1, f2, p2));
			} else if (p1.equals("ff")) {
				return new Caml.FF<>(toTy(f1, f2, p2));
			}
		} catch (RuntimeException re) {

		}

			if (o instanceof String) {
				return new Caml.Var<>(o.toString());				
			}
			

		System.out.println(o);
		System.out.println(o.getClass());
		throw new RuntimeException();

	}
//(((delta, f), (pi, f), (sigma, f)), then, ((delta, f), (pi, f), (sigma, f))))
	public static DSP toDSP(Object o) {
		Tuple3 a = (Tuple3) o;
		Tuple3 x = (Tuple3) a.a;
		Tuple3 y = (Tuple3) a.c;
		
		Caml<Unit, NewSigConst, NewMapConst> s1 = toCaml(f2, f1, f3, ((org.codehaus.jparsec.functors.Pair<?, ?>)x.a).b);
		Caml<Unit, NewSigConst, NewMapConst>  s2 = toCaml(f2, f1, f3, ((org.codehaus.jparsec.functors.Pair<?, ?>)x.b).b);
		Caml<Unit, NewSigConst, NewMapConst>  s3 = toCaml(f2, f1, f3, ((org.codehaus.jparsec.functors.Pair<?, ?>)x.c).b);
		Caml<Unit, NewSigConst, NewMapConst>  s1x = toCaml(f2, f1, f3, ((org.codehaus.jparsec.functors.Pair<?, ?>)y.a).b);
		Caml<Unit, NewSigConst, NewMapConst>  s2x = toCaml(f2, f1, f3, ((org.codehaus.jparsec.functors.Pair<?, ?>)y.b).b);
		Caml<Unit, NewSigConst, NewMapConst>  s3x = toCaml(f2, f1, f3, ((org.codehaus.jparsec.functors.Pair<?, ?>)y.c).b);

		return new DSP(s1, s2, s3, s1x, s2x, s3x);
	}
	public static <X, Y> Poly<X, Y> toTy(Fn<Object, X> f1, Fn<Object, Y> f2,
			Object o) {

		try {
			Tuple3 t = (Tuple3) o;
			Token z = (Token) t.b;
			String y = z.toString();
			if (y.equals("+")) {
				return new Poly.Plus<X, Y>((Poly<X, Y>) toTy(f1, f2, t.a),
						(Poly<X, Y>) toTy(f1, f2, t.c));
			} else if (y.equals("*")) {
				return new Poly.Times<X, Y>((Poly<X, Y>) toTy(f1, f2, t.a),
						(Poly<X, Y>) toTy(f1, f2, t.c));
			}
			if (y.equals("^")) {
				return new Poly.Exp<X, Y>((Poly<X, Y>) toTy(f1, f2, t.a),
						(Poly<X, Y>) toTy(f1, f2, t.c));
			}
		} catch (RuntimeException cce) {
		}

		try {
			org.codehaus.jparsec.functors.Pair t = (org.codehaus.jparsec.functors.Pair) o;
			if (t.a.toString().equals("unit")) {
				return new Poly.One<X, Y>((X) t.b);
			} else if (t.a.toString().equals("void")) {
				return new Poly.Zero<X, Y>((X) t.b);
			} else if (t.a.toString().equals("prop")) {
				return new Poly.Two<X, Y>((X) t.b);
			}
			throw new RuntimeException();
		} catch (RuntimeException cce) {
		}

		try {
			return new Poly.Const<X, Y>(f2.of(o), f1.of(o));
		} catch (RuntimeException cce) {

		}

		return new Poly.Var<X, Y>(o.toString());

		// throw new RuntimeException(o.toString());
	}

	@SuppressWarnings("unchecked")
	public static Parser<?> ty(Parser<?> c, Parser<?> t) {
		Reference ref = Parser.newReference();

		Parser plusTy = Parsers.between(term("("),
				Parsers.tuple(ref.lazy(), term("+"), ref.lazy()), term(")"));
		Parser prodTy = Parsers.between(term("("),
				Parsers.tuple(ref.lazy(), term("*"), ref.lazy()), term(")"));
		Parser expTy = Parsers.between(term("("),
				Parsers.tuple(ref.lazy(), term("^"), ref.lazy()), term(")"));

		Parser a = Parsers.or(new Parser[] { Parsers.tuple(term("prop"), t),
				Parsers.tuple(term("void"), t), Parsers.tuple(term("unit"), t),
				plusTy, prodTy, expTy, ident(), Parsers.tuple(c, t) });

		ref.set(a);

		return a;
	}

	static Fn<Object, NewSigConst> f1 = new Fn<Object, NewSigConst>() {
		@Override
		public NewSigConst of(Object x) {
			if (x instanceof Tuple4) {
				return toSig((Tuple4) x);
			}
			throw new RuntimeException();
		}
	};
	static Fn<Object, Unit> f2 = new Fn<Object, Unit>() {
		@Override
		public Unit of(Object x) {
			return new Unit();
		}
	};
	static Fn<Object, NewInstConst> f1x = new Fn<Object, NewInstConst>() {
		@Override
		public NewInstConst of(Object x) {
			return toInst(x);
		}
	};
	static Fn<Object, Poly<Unit, NewSigConst>> f2x = new Fn<Object, Poly<Unit, NewSigConst>>() {
		@Override
		public Poly<Unit, NewSigConst> of(Object x) {
			return null; // new Unit();
		}
	};//
	static Fn<Object, NewTransConst> f3x = new Fn<Object, NewTransConst>() {
		@Override
		public NewTransConst of(Object x) {
			return toTrans(x);
		}
	};
	
	static Fn<Object, NewMapConst> f3 = new Fn<Object, NewMapConst>() {
		@Override
		public NewMapConst of(Object x) {
			return toMap(x);
		}
	};

	@SuppressWarnings("rawtypes")
	public static final NewFQLProgram program(String s) {
		List<NewDecl> ret = new LinkedList<>();
		List decls = (List) NewFqlParser.program.parse(s);

		for (Object d : decls) {
			org.codehaus.jparsec.functors.Pair pr = (org.codehaus.jparsec.functors.Pair) d;
			Object decl = pr.b;
			String txt = pr.a.toString();
			int idx = s.indexOf(txt);
			if (idx < 0) {
				throw new RuntimeException();
			}
			//System.out.println("whasabi  " + s.indexOf(txt));
//			s.indexOf(txt);
			Tuple3 t = (Tuple3) decl;
			String kind = ((Token) t.a).toString();
			switch (kind) {
			case "schema":
				Tuple4 tt = (Tuple4) decl;
				String name = (String) tt.b;

				ret.add(NewFQLProgram.NewDecl.sigDecl(name, idx,
						toTy(f2, f1, tt.d)));

				break;
			case "instance":
				// System.out.println(t.getClass());
				// System.out.println(t);
				Tuple4 tt0 = (Tuple4) decl;
				name = (String) t.b;

				Tuple3 xxx = (Tuple3) tt0.c;
				ret.add(NewFQLProgram.NewDecl.instDecl(name, idx,
						toTy(f2x, f1x, xxx.b), toTy(f2, f1, tt0.d)));
				break;
			case "mapping":
				// System.out.println(t.getClass());
				// System.out.println(t);
				tt0 = (Tuple4) decl;
				name = (String) t.b;

				xxx = (Tuple3) tt0.c;
				Tuple3 yyy = (Tuple3) xxx.b;
				Object t1 = yyy.a;
				Object t2 = yyy.c;
				Object o = tt0.d;
				ret.add(NewFQLProgram.NewDecl.mapDecl(name, idx,
						toCaml(f2, f1, f3, o), new Pair<>(toTy(f2, f1, t1),
								toTy(f2, f1, t2))));
				// ret.add(mappingDecl(decl));
				break;
			case "transform":
				tt0 = (Tuple4) decl;
				name = (String) t.b;

				xxx = (Tuple3) tt0.c;
				 yyy = (Tuple3) xxx.b;
				 t1 = yyy.a;
				 t2 = yyy.c;
				 o = tt0.d;
				ret.add(NewFQLProgram.NewDecl.transDecl(name, idx, toCaml(f2x, f1x, f3x, o), 
						new Pair<>(toTy(f2x, f1x, t1), toTy(f2x, f1x, t2))));
				// ret.add(mappingDecl(decl));
				
			case "query":
				 System.out.println(t.getClass());
				 System.out.println(t);
				Tuple5 tt1 = (Tuple5) decl;
				name = (String) t.b;

				Tuple4 xxx0 = (Tuple4) tt1.d;
				 t1 = xxx0.a;
				 t2 = xxx0.c;
				 o = tt1.e;
				ret.add(NewFQLProgram.NewDecl.queryDecl(name, idx, toDSP(o), 
						new Pair<>(toTy(f2, f1, t1), toTy(f2, f1, t2))));
				break;
			// case "query":
			// ret.add(queryDecl(decl));
			// break;
			default:
				throw new RuntimeException("Unknown decl: " + kind);
			}
		}

		return new NewFQLProgram(ret);
	}

	// // tuple 5
	// @SuppressWarnings("rawtypes")
	// private static QueryDecl queryDecl(Object decl) {
	// Tuple5 t = (Tuple5) decl;
	//
	// String name = (String) t.b;
	//
	// String src = (String) ((Tuple4) t.d).a;
	// String dst = (String) ((Tuple4) t.d).c;
	//
	// if (!(t.e instanceof Tuple3)) {
	// return new QueryDecl(name, src, src, dst, 0); // id
	// }
	// Tuple3 x = (Tuple3) t.e;
	// if (!(x.a instanceof String)) { // deltasigmapi
	// org.codehaus.jparsec.functors.Pair delta =
	// (org.codehaus.jparsec.functors.Pair) x.a;
	// org.codehaus.jparsec.functors.Pair pi =
	// (org.codehaus.jparsec.functors.Pair) x.b;
	// org.codehaus.jparsec.functors.Pair sigma =
	// (org.codehaus.jparsec.functors.Pair) x.c;
	// return new QueryDecl(name, (String) delta.b, (String) pi.b,
	// (String) sigma.b, src, dst);
	// } else { // compose
	// String m1 = (String) x.a;
	// String m2 = (String) x.c;
	// return new QueryDecl(name, m2, m1, src, dst);
	// }
	// }

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static NewTransConst toTrans(Object decl) {
	
		List<Pair<String, List<Pair<String, String>>>> data = new LinkedList<>();
		
		System.out.println(decl);
		System.out.println(decl.getClass());
		
		 return new NewFQLProgram.NewTransConst(data);

	}
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static NewMapConst toMap(Object decl) {
		// return null;
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

			 return new NewFQLProgram.NewMapConst(objs, attrs, arrows);
		
	}

	// @SuppressWarnings("rawtypes")
	private static NewInstConst toInst(Object decl) {
		Tuple4 t = (Tuple4) decl;
		String name = (String) t.b;

		String type = (String) ((Tuple3) t.c).b;

		if (t.d instanceof Token) {
			return new NewFQLProgram.External(toTy(f2, f1, type));
		} else if (t.d instanceof Tuple3) {
			Tuple3 x = (Tuple3) t.d;

			if (x.a instanceof Tuple3) {
				// constant
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
				return new NewFQLProgram.Fin(toTy(f2, f1, type), data);

			}

		}

		throw new RuntimeException();

		/*
		 * else { // delta, sigma, pi, SIGMA, eval String kind = ((Token)
		 * x.a).toString(); String mapping = (String) x.b; String inst =
		 * (String) x.c; if (kind.equals("eval")) { return new
		 * EvalInstanceDecl(name, mapping, inst, type); } else { return new
		 * EvalDSPInstanceDecl(name, kind, mapping, inst, type); } } } else {
		 * return new RelationalizeDecl(name, type, (String)
		 * ((org.codehaus.jparsec.functors.Pair) t.d).b); }
		 */

	}

	@SuppressWarnings("rawtypes")
	private static NewFQLProgram.NewSigConst toSig(Tuple4 s) {
		List<String> nodes = new LinkedList<>();
		List<Triple<String, String, String>> attrs = new LinkedList<>();
		List<Triple<String, String, String>> arrows = new LinkedList<>();
		List<Pair<List<String>, List<String>>> eqs = new LinkedList<>();

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
		NewFQLProgram.NewSigConst c = new NewFQLProgram.NewSigConst(nodes,
				attrs, arrows, eqs);
		return c;
		// return NewFQLProgram.NewDecl.sigDecl(name, line, new Poly.Const<Unit,
		// NewFQLProgram.NewSigConst>(c, new Unit()));
		// NewSigConst(name, nodes, attrs, arrows, eqs);
	}

}