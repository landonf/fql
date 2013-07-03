package fql.parse;

import java.util.LinkedList;
import java.util.List;

import javax.swing.JOptionPane;

import fql.FQLException;
import fql.Pair;
import fql.decl.Instance;
import fql.decl.Mapping;
import fql.decl.Signature;

/**
 * 
 * @author ryan
 * 
 *         parsers for json
 */
public class JSONParsers {

	public static void main(String[] args) {
		try {
			String s = JOptionPane.showInputDialog(" foo ");
			Partial<Mapping> p = new JSONMappingParser()
					.parse(new FqlTokenizer(s));
			System.out.println(p);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static class JSONMappingParser implements RyanParser<Mapping> {

		@Override
		public Partial<Mapping> parse(Tokens s) throws BadSyntax, IllTyped {

			RyanParser<Signature> src = ParserUtils.seq(new QuotedKeywordParser(
					"source"), ParserUtils.seq(new KeywordParser(":"),
					new JSONSigParser(false)));

			RyanParser<Signature> dst = ParserUtils.seq(new QuotedKeywordParser(
					"target"), ParserUtils.seq(new KeywordParser(":"),
					new JSONSigParser(false)));

			RyanParser<List<Pair<String, String>>> onObjects = new MappingObjectParser();

			RyanParser<List<Pair<Pair<Pair<String, String>, String>, List<Pair<Pair<String, String>, String>>>>> onGens = new MappingGeneratorParser();

			RyanParser<Pair<Signature, Signature>> a = ParserUtils.inside(src,
					new KeywordParser(","), dst);

			RyanParser<Pair<List<Pair<String, String>>, List<Pair<Pair<Pair<String, String>, String>, List<Pair<Pair<String, String>, String>>>>>> b = ParserUtils
					.inside(onObjects, new KeywordParser(","), onGens);

			RyanParser<Pair<Pair<Signature, Signature>, Pair<List<Pair<String, String>>, List<Pair<Pair<Pair<String, String>, String>, List<Pair<Pair<String, String>, String>>>>>>> u = ParserUtils
					.inside(a, new KeywordParser(","), b);

			RyanParser<Pair<Pair<Signature, Signature>, Pair<List<Pair<String, String>>, List<Pair<Pair<Pair<String, String>, String>, List<Pair<Pair<String, String>, String>>>>>>> xxx = ParserUtils
					.outside(new KeywordParser("{"), u, new KeywordParser("}"));

			Partial<Pair<Pair<Signature, Signature>, Pair<List<Pair<String, String>>, List<Pair<Pair<Pair<String, String>, String>, List<Pair<Pair<String, String>, String>>>>>>> zzz = xxx
					.parse(s);

			// System.out.println(zzz);
			Mapping m = null;
			try {
				m = new Mapping(zzz.value.first.first, zzz.value.first.second,
						zzz.value.second.first, zzz.value.second.second);
			} catch (FQLException e) {
				e.printStackTrace();
				throw new BadSyntax(s, e.getMessage());
			}
			return new Partial<>(zzz.tokens, m);
		}

	}

	public static class MappingObjectParser implements
			RyanParser<List<Pair<String, String>>> {

		@Override
		public Partial<List<Pair<String, String>>> parse(Tokens s)
				throws BadSyntax, IllTyped {

			RyanParser<Pair<String, String>> q = ParserUtils.inside(
					new QuotedParser(), new KeywordParser(":"),
					new QuotedParser());
			RyanParser<List<Pair<String, String>>> u = ParserUtils.manySep(q,
					new KeywordParser(","));
			RyanParser<List<Pair<String, String>>> p = ParserUtils.outside(
					new KeywordParser("{"), u, new KeywordParser("}"));
			RyanParser<List<Pair<String, String>>> zzz = ParserUtils.seq(
					new QuotedKeywordParser("onObjects"),
					ParserUtils.seq(new KeywordParser(":"), p));
			return zzz.parse(s);
		}

	}

	public static class JSONInstParser implements RyanParser<Instance> {

		@Override
		public Partial<Instance> parse(Tokens s) throws BadSyntax, IllTyped {

			RyanParser<Pair<List<Pair<String, List<String>>>, List<Pair<Pair<Pair<String, String>, String>, List<Pair<String, String>>>>>> r = ParserUtils
					.inside(new instanceObjParser(), new KeywordParser(","),
							new onMorphismsParser2());

			RyanParser<Pair<Signature, Pair<List<Pair<String, List<String>>>, List<Pair<Pair<Pair<String, String>, String>, List<Pair<String, String>>>>>>> u = ParserUtils
					.inside(ParserUtils.seq(
							new QuotedKeywordParser("ontology"), ParserUtils
									.seq(new KeywordParser(":"),
											new JSONSigParser(false))),
							new KeywordParser(","), r);

			RyanParser<Pair<Signature, Pair<List<Pair<String, List<String>>>, List<Pair<Pair<Pair<String, String>, String>, List<Pair<String, String>>>>>>> ret = ParserUtils
					.outside(new KeywordParser("{"), u, new KeywordParser("}"));

			Partial<Pair<Signature, Pair<List<Pair<String, List<String>>>, List<Pair<Pair<Pair<String, String>, String>, List<Pair<String, String>>>>>>> xxx = ret
					.parse(s);

			// xxx.value

			Signature u1 = xxx.value.first;
			List<Pair<String, List<String>>> u2 = xxx.value.second.first;
			List<Pair<Pair<Pair<String, String>, String>, List<Pair<String, String>>>> u3 = xxx.value.second.second;

			List<Pair<String, List<Object>>> v2 = new LinkedList<>();
			for (Pair<String, List<String>> v2X : u2) {
				List<Object> ddd = new LinkedList<>();
				ddd.addAll(v2X.second);
				v2.add(new Pair<>(v2X.first, ddd));
			}
			List<Pair<Pair<Pair<Object, Object>, String>, List<Pair<Object, Object>>>> v3 = new LinkedList<>();
			for (Pair<Pair<Pair<String, String>, String>, List<Pair<String, String>>> g : u3) {

				List<Pair<Object, Object>> l = new LinkedList<>();
				for (Pair<String, String> p : g.second) {
					l.add(new Pair<Object, Object>(p.first, p.second));
				}

				v3.add(new Pair<Pair<Pair<Object, Object>, String>, List<Pair<Object, Object>>>(
						new Pair<>(new Pair<Object, Object>(
								g.first.first.first, g.first.first.second),
								g.first.second), l));
			}

			try {
				return new Partial<>(xxx.tokens, new Instance(u1, v2, v3));
			} catch (Exception eee) {
				eee.printStackTrace();
				throw new RuntimeException(eee);
			}
		}
	}

	public static class instanceObjParser implements
			RyanParser<List<Pair<String, List<String>>>> {

		@Override
		public Partial<List<Pair<String, List<String>>>> parse(Tokens s)
				throws BadSyntax, IllTyped {
			RyanParser<List<String>> h = ParserUtils.manySep(new QuotedParser(),
					new KeywordParser(","));

			RyanParser<List<String>> r = ParserUtils.outside(
					new KeywordParser("["), h, new KeywordParser("]"));

			RyanParser<Pair<String, List<String>>> u = ParserUtils.inside(
					new QuotedParser(), new KeywordParser(":"), r);

			RyanParser<List<Pair<String, List<String>>>> x = ParserUtils.seq(
					new QuotedKeywordParser("onObjects"), ParserUtils.seq(
							new KeywordParser(":"), ParserUtils.outside(
									new KeywordParser("{"),
									ParserUtils.manySep(u, new KeywordParser(
											",")), new KeywordParser("}"))));

			return x.parse(s);
		}
	}

	// { arrow : -, map : { } }
	public static class onMorphismsParser2
			implements
			RyanParser<List<Pair<Pair<Pair<String, String>, String>, List<Pair<String, String>>>>> {

		@Override
		public Partial<List<Pair<Pair<Pair<String, String>, String>, List<Pair<String, String>>>>> parse(
				Tokens s) throws BadSyntax, IllTyped {

			RyanParser<Pair<Pair<String, String>, String>> arrP = ParserUtils.seq(
					new QuotedKeywordParser("arrow"),
					ParserUtils.seq(new KeywordParser(":"), new ArrowParser()));

			RyanParser<Pair<String, String>> foo = ParserUtils.inside(
					new QuotedParser(), new KeywordParser(":"),
					new QuotedParser());

			RyanParser<List<Pair<String, String>>> mP = ParserUtils.seq(
					new QuotedKeywordParser("map"), ParserUtils.seq(
							new KeywordParser(":"), ParserUtils.outside(
									new KeywordParser("{"), ParserUtils
											.manySep(foo,
													new KeywordParser(",")),
									new KeywordParser("}"))));

			RyanParser<Pair<Pair<Pair<String, String>, String>, List<Pair<String, String>>>> xxx = ParserUtils
					.outside(new KeywordParser("{"), ParserUtils.inside(arrP,
							new KeywordParser(","), mP), new KeywordParser("}"));

			RyanParser<List<Pair<Pair<Pair<String, String>, String>, List<Pair<String, String>>>>> u = ParserUtils
					.manySep(xxx, new KeywordParser(","));

			RyanParser<List<Pair<Pair<Pair<String, String>, String>, List<Pair<String, String>>>>> x = ParserUtils
					.seq(new QuotedKeywordParser("onMorphisms"), ParserUtils
							.seq(new KeywordParser(":"), ParserUtils.outside(
									new KeywordParser("["), u,
									new KeywordParser("]"))));

			return x.parse(s);

		}

	}

	// public static class onMorphismsParser
	// implements
	// Parser<List<Pair<Pair<Pair<String, String>, String>, Pair<String,
	// String>>>> {
	//
	// @Override
	// public Partial<List<Pair<Pair<Pair<String, String>, String>, Pair<String,
	// String>>>> parse(
	// Tokens s) throws BadSyntax, IllTyped {
	//
	// Parser<Pair<String, String>> b = ParserUtils.seq(
	// new QuotedKeywordParser("map"), ParserUtils.seq(new KeywordParser(":"),
	// new MapParser()));
	//
	// Parser<Pair<Pair<String, String>, String>> a = ParserUtils.seq(
	// new QuotedKeywordParser("arrow"), ParserUtils.seq(new KeywordParser(":"),
	// new ArrowParser()));
	//
	// Parser<Pair<Pair<Pair<String, String>, String>, Pair<String, String>>> h
	// = ParserUtils
	// .inside(a, new KeywordParser(","), b);
	//
	// Parser<Pair<Pair<Pair<String, String>, String>, Pair<String, String>>> p
	// = ParserUtils
	// .outside(new KeywordParser("{"), h, new KeywordParser("}"));
	//
	// Parser<List<Pair<Pair<Pair<String, String>, String>, Pair<String,
	// String>>>> u =
	// ParserUtils.manySep(p, new KeywordParser(","));
	//
	// Parser<List<Pair<Pair<Pair<String, String>, String>, Pair<String,
	// String>>>> x = ParserUtils
	// .seq(new QuotedKeywordParser("onMorphisms"), ParserUtils
	// .seq(new KeywordParser(":"), ParserUtils.outside(
	// new KeywordParser("["), u,
	// new KeywordParser("]"))));
	//
	// return x.parse(s);
	//
	// }
	// }

	public static class MapParser implements RyanParser<Pair<String, String>> {

		@Override
		public Partial<Pair<String, String>> parse(Tokens s) throws BadSyntax,
				IllTyped {

			RyanParser<String> a = ParserUtils
					.seq(new QuotedKeywordParser("input"), ParserUtils.seq(
							new KeywordParser(":"), new QuotedParser()));
			RyanParser<String> b = ParserUtils
					.seq(new QuotedKeywordParser("output"), ParserUtils.seq(
							new KeywordParser(":"), new QuotedParser()));
			RyanParser<Pair<String, String>> u = ParserUtils.inside(a,
					new KeywordParser(","), b);

			RyanParser<Pair<String, String>> x = ParserUtils.outside(
					new KeywordParser("{"), u, new KeywordParser("}"));

			return x.parse(s);
		}

	}

	public static class MappingGeneratorParser
			implements
			RyanParser<List<Pair<Pair<Pair<String, String>, String>, List<Pair<Pair<String, String>, String>>>>> {

		@Override
		public Partial<List<Pair<Pair<Pair<String, String>, String>, List<Pair<Pair<String, String>, String>>>>> parse(
				Tokens s) throws BadSyntax, IllTyped {

			RyanParser<Pair<Pair<String, String>, String>> a = ParserUtils.seq(
					new QuotedKeywordParser("arrow"),
					ParserUtils.seq(new KeywordParser(":"), new ArrowParser()));

			RyanParser<List<Pair<Pair<String, String>, String>>> b = ParserUtils
					.seq(new QuotedKeywordParser("path"), ParserUtils.seq(
							new KeywordParser(":"), new PathParser()));

			RyanParser<Pair<Pair<Pair<String, String>, String>, List<Pair<Pair<String, String>, String>>>> e = ParserUtils
					.inside(a, new KeywordParser(","), b);

			RyanParser<Pair<Pair<Pair<String, String>, String>, List<Pair<Pair<String, String>, String>>>> g = ParserUtils
					.outside(new KeywordParser("{"), e, new KeywordParser("}"));

			RyanParser<List<Pair<Pair<Pair<String, String>, String>, List<Pair<Pair<String, String>, String>>>>> h = ParserUtils
					.manySep(g, new KeywordParser(","));

			RyanParser<List<Pair<Pair<Pair<String, String>, String>, List<Pair<Pair<String, String>, String>>>>> r = ParserUtils
					.outside(new KeywordParser("["), h, new KeywordParser("]"));

			// Parser<Pair<String, List<Pair<Pair<Pair<String, String>, String>,
			// List<Pair<Pair<String, String>, String>>>>>> u = ParserUtils
			// .inside(new QuotedParser(), new KeywordParser(":"), r);

			RyanParser<List<Pair<Pair<Pair<String, String>, String>, List<Pair<Pair<String, String>, String>>>>> x = ParserUtils
					.seq(new QuotedKeywordParser("onGenerators"),
							ParserUtils.seq(new KeywordParser(":"), r));

			return x.parse(s);

		}
	}

	public static class JSONSigParser implements RyanParser<Signature> {

		boolean outerparens;

		public JSONSigParser(boolean outerparens) {
			this.outerparens = outerparens;
		}

		@Override
		public Partial<Signature> parse(Tokens s) throws BadSyntax, IllTyped {
			List<Pair<Pair<String, String>, String>> arrows;
			List<Pair<List<Pair<Pair<String, String>, String>>, List<Pair<Pair<String, String>, String>>>> eqs;
			List<String> obs;

			RyanParser<List<String>> ob = ParserUtils.seq(new QuotedKeywordParser(
					"objects"), ParserUtils.seq(new KeywordParser(":"),
					ParserUtils.outside(new KeywordParser("["),
							ParserUtils.manySep(new QuotedParser(),
									new KeywordParser(",")), new KeywordParser(
									"]"))));

			RyanParser<List<Pair<Pair<String, String>, String>>> arrs = ParserUtils
					.seq(new QuotedKeywordParser("arrows"), ParserUtils.seq(
							new KeywordParser(":"), ParserUtils.outside(
									new KeywordParser("["), ParserUtils
											.manySep(new ArrowParser(),
													new KeywordParser(",")),
									new KeywordParser("]"))));

			RyanParser<List<Pair<List<Pair<Pair<String, String>, String>>, List<Pair<Pair<String, String>, String>>>>> rel = ParserUtils
					.seq(new QuotedKeywordParser("relations"), ParserUtils.seq(
							new KeywordParser(":"), ParserUtils.outside(
									new KeywordParser("["), ParserUtils
											.manySep(new RelationParser(),
													new KeywordParser(",")),
									new KeywordParser("]"))));

			RyanParser<Pair<List<String>, List<Pair<Pair<String, String>, String>>>> oa = ParserUtils
					.inside(ob, new KeywordParser(","), arrs);

			RyanParser<Pair<Pair<List<String>, List<Pair<Pair<String, String>, String>>>, List<Pair<List<Pair<Pair<String, String>, String>>, List<Pair<Pair<String, String>, String>>>>>> u = ParserUtils
					.inside(oa, new KeywordParser(","), rel);

			RyanParser<Pair<Pair<List<String>, List<Pair<Pair<String, String>, String>>>, List<Pair<List<Pair<Pair<String, String>, String>>, List<Pair<Pair<String, String>, String>>>>>> ret = u;

			// if (outerparens) {
			ret = ParserUtils.outside(new KeywordParser("{"), u,
					new KeywordParser("}"));
			// }

			Partial<Pair<Pair<List<String>, List<Pair<Pair<String, String>, String>>>, List<Pair<List<Pair<Pair<String, String>, String>>, List<Pair<Pair<String, String>, String>>>>>> xxx = ret
					.parse(s);

			obs = xxx.value.first.first;
			arrows = xxx.value.first.second;
			eqs = xxx.value.second;

			try {
				return new Partial<>(xxx.tokens,
						new Signature(obs, arrows, eqs));
			} catch (Exception eee) {
				eee.printStackTrace();
				throw new RuntimeException(eee);
			}

		}
	}

	public static class RelationParser
			implements
			RyanParser<Pair<List<Pair<Pair<String, String>, String>>, List<Pair<Pair<String, String>, String>>>> {

		@Override
		public Partial<Pair<List<Pair<Pair<String, String>, String>>, List<Pair<Pair<String, String>, String>>>> parse(
				Tokens s) throws BadSyntax, IllTyped {
			RyanParser<List<Pair<Pair<String, String>, String>>> u1 = ParserUtils
					.seq(new QuotedKeywordParser("left"), ParserUtils.seq(
							new KeywordParser(":"), new PathParser()));
			RyanParser<List<Pair<Pair<String, String>, String>>> u2 = ParserUtils
					.seq(new QuotedKeywordParser("right"), ParserUtils.seq(
							new KeywordParser(":"), new PathParser()));

			return ParserUtils.outside(new KeywordParser("{"),
					ParserUtils.inside(u1, new KeywordParser(","), u2),
					new KeywordParser("}")).parse(s);
		}

	}

	public static class PathParser implements
			RyanParser<List<Pair<Pair<String, String>, String>>> {

		@Override
		public Partial<List<Pair<Pair<String, String>, String>>> parse(Tokens s)
				throws BadSyntax, IllTyped {
			return ParserUtils.outside(
					new KeywordParser("["),
					ParserUtils.manySep(new ArrowParser(), new KeywordParser(
							",")), new KeywordParser("]")).parse(s);
		}

	}

	public static class ArrowParser implements
			RyanParser<Pair<Pair<String, String>, String>> {
		@Override
		public Partial<Pair<Pair<String, String>, String>> parse(Tokens s)
				throws BadSyntax, IllTyped {
			RyanParser<?> p1 = new QuotedKeywordParser("source");
			RyanParser<?> p2 = new QuotedKeywordParser("target");
			RyanParser<?> p3 = new QuotedKeywordParser("label");

			RyanParser<String> q1 = ParserUtils
					.seq(p1, ParserUtils.seq(new KeywordParser(":"),
							new QuotedParser()));
			RyanParser<String> q2 = ParserUtils
					.seq(p2, ParserUtils.seq(new KeywordParser(":"),
							new QuotedParser()));
			RyanParser<String> q3 = ParserUtils
					.seq(p3, ParserUtils.seq(new KeywordParser(":"),
							new QuotedParser()));

			RyanParser<Pair<String, String>> l = ParserUtils.inside(q1,
					new KeywordParser(","), q2);
			RyanParser<Pair<Pair<String, String>, String>> u = ParserUtils.inside(
					l, new KeywordParser(","), q3);
			return ParserUtils.outside(new KeywordParser("{"), u,
					new KeywordParser("}")).parse(s);
		}

	}

}
