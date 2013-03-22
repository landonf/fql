package fql.parse;

import java.util.List;

import javax.swing.JOptionPane;

import fql.FQLException;
import fql.Instance;
import fql.Mapping;
import fql.Pair;
import fql.Signature;

public class JSONParsers {

	public static void main(String[] args) {
		try {
			String s = JOptionPane.showInputDialog(" foo ");
			Partial p = new JSONMappingParser().parse(new Tokens(s));
			System.out.println(p);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static class JSONMappingParser implements Parser<Mapping> {

		@Override
		public Partial<Mapping> parse(Tokens s) throws BadSyntax, IllTyped {
			
			Parser<Signature> src = ParserUtils.seq(new QuotedKeywordParser("source"), ParserUtils.seq(new KeywordParser(":"), new JSONSigParser(false)));
			
			Parser<Signature> dst = ParserUtils.seq(new QuotedKeywordParser("target"), ParserUtils.seq(new KeywordParser(":"), new JSONSigParser(false)));
			
			Parser<List<Pair<String, String>>> onObjects = new MappingObjectParser();
			
			Parser<List<Pair<Pair<Pair<String, String>, String>, List<Pair<Pair<String, String>, String>>>>>
			onGens = new MappingGeneratorParser();
						
			Parser<Pair<Signature, Signature>> 
			a = ParserUtils.inside(src, new KeywordParser(","), dst);
			
			Parser<Pair<List<Pair<String, String>>, List<Pair<Pair<Pair<String, String>, String>, List<Pair<Pair<String, String>, String>>>>>> 
			b = ParserUtils.inside(onObjects, new KeywordParser(","), onGens);
			
			Parser<Pair<Pair<Signature, Signature>, Pair<List<Pair<String, String>>, List<Pair<Pair<Pair<String, String>, String>, List<Pair<Pair<String, String>, String>>>>>>>
			u = ParserUtils.inside(a, new KeywordParser(","), b);
			
			Parser<Pair<Pair<Signature, Signature>, Pair<List<Pair<String, String>>, List<Pair<Pair<Pair<String, String>, String>, List<Pair<Pair<String, String>, String>>>>>>> 
			xxx = ParserUtils.outside(new KeywordParser("{"), u, new KeywordParser("}"));

			Partial<Pair<Pair<Signature, Signature>, Pair<List<Pair<String, String>>, List<Pair<Pair<Pair<String, String>, String>, List<Pair<Pair<String, String>, String>>>>>>> 
			zzz = xxx.parse(s);
			
			System.out.println(zzz);
			Mapping m = null;
			try {
				m = new Mapping(zzz.value.first.first, zzz.value.first.second, zzz.value.second.first, zzz.value.second.second);
			} catch (FQLException e) {
				e.printStackTrace();
				throw new BadSyntax(e.getMessage());
			}
			return new Partial<>(zzz.tokens, m);
		}
		
	}
	
	public static class MappingObjectParser implements Parser<List<Pair<String, String>>> {

		@Override
		public Partial<List<Pair<String, String>>> parse(Tokens s)
				throws BadSyntax, IllTyped {

			Parser<Pair<String, String>> q = ParserUtils.inside(new QuotedParser(), new KeywordParser(":"), new QuotedParser());
			Parser<List<Pair<String, String>>> u = ParserUtils.manySep(q, new KeywordParser(","));
			Parser<List<Pair<String, String>>> p = ParserUtils.outside(new KeywordParser("{"), u, new KeywordParser("}"));
			Parser<List<Pair<String, String>>> zzz = ParserUtils.seq(new QuotedKeywordParser("onObjects"), ParserUtils.seq(new KeywordParser(":"), p));
			return zzz.parse(s);
		}
		
	}

	public static class JSONInstParser implements Parser<Instance> {

		@Override
		public Partial<Instance> parse(Tokens s) throws BadSyntax, IllTyped {

			Parser<Pair<List<Pair<String, List<String>>>, List<Pair<Pair<Pair<String, String>, String>, Pair<String, String>>>>> 
			r = ParserUtils.inside(new instanceObjParser(), new KeywordParser(","),
							new onMorphismsParser());

			Parser<Pair<Signature, Pair<List<Pair<String, List<String>>>, List<Pair<Pair<Pair<String, String>, String>, Pair<String, String>>>>>> 
			u = ParserUtils.inside(ParserUtils.seq(new QuotedKeywordParser("ontology"), ParserUtils.seq(new KeywordParser(":"), new JSONSigParser(false))), new KeywordParser(","), r);

			Parser<Pair<Signature, Pair<List<Pair<String, List<String>>>, List<Pair<Pair<Pair<String, String>, String>, Pair<String, String>>>>>> ret = ParserUtils
					.outside(new KeywordParser("{"), u, new KeywordParser("}"));

			Partial<Pair<Signature, Pair<List<Pair<String, List<String>>>, List<Pair<Pair<Pair<String, String>, String>, Pair<String, String>>>>>>
			xxx = ret.parse(s);

			// xxx.value
			try {
				return new Partial<>(xxx.tokens, new Instance(xxx.value.first,
						xxx.value.second.first, xxx.value.second.second));
			} catch (Exception eee) {
				eee.printStackTrace();
				throw new RuntimeException(eee);
			}
		}
	}

	public static class instanceObjParser implements
			Parser<List<Pair<String, List<String>>>> {

		@Override
		public Partial<List<Pair<String, List<String>>>> parse(Tokens s)
				throws BadSyntax, IllTyped {
			Parser<List<String>> h = ParserUtils.manySep(new QuotedParser(),
					new KeywordParser(","));

			Parser<List<String>> r = ParserUtils.outside(
					new KeywordParser("["), h, new KeywordParser("]"));

			Parser<Pair<String, List<String>>> u = ParserUtils.inside(
					new QuotedParser(), new KeywordParser(":"), r);

			Parser<List<Pair<String, List<String>>>> x = ParserUtils.seq(
					new QuotedKeywordParser("onObjects"),
					ParserUtils.seq(new KeywordParser(":"), ParserUtils.outside(new KeywordParser("{"),
							ParserUtils.manySep(u, new KeywordParser(",")), new KeywordParser("}"))));

			return x.parse(s);
		}
	}

	public static class onMorphismsParser
			implements
			Parser<List<Pair<Pair<Pair<String, String>, String>, Pair<String, String>>>> {

		@Override
		public Partial<List<Pair<Pair<Pair<String, String>, String>, Pair<String, String>>>> parse(
				Tokens s) throws BadSyntax, IllTyped {

			Parser<Pair<String, String>> b = ParserUtils.seq(
					new QuotedKeywordParser("map"), ParserUtils.seq(new KeywordParser(":"), new MapParser()));

			Parser<Pair<Pair<String, String>, String>> a = ParserUtils.seq(
					new QuotedKeywordParser("arrow"), ParserUtils.seq(new KeywordParser(":"), new ArrowParser()));

			Parser<Pair<Pair<Pair<String, String>, String>, Pair<String, String>>> h = ParserUtils
					.inside(a, new KeywordParser(","), b);

			Parser<Pair<Pair<Pair<String, String>, String>, Pair<String, String>>> p = ParserUtils
					.outside(new KeywordParser("{"), h, new KeywordParser("}"));

			Parser<List<Pair<Pair<Pair<String, String>, String>, Pair<String, String>>>> u = 
					ParserUtils.manySep(p, new KeywordParser(","));

			Parser<List<Pair<Pair<Pair<String, String>, String>, Pair<String, String>>>> x = ParserUtils
					.seq(new QuotedKeywordParser("onMorphisms"), ParserUtils
							.seq(new KeywordParser(":"), ParserUtils.outside(
									new KeywordParser("["), u,
									new KeywordParser("]"))));

			return x.parse(s);

		}
	}

	public static class MapParser implements Parser<Pair<String, String>> {

		@Override
		public Partial<Pair<String, String>> parse(Tokens s) throws BadSyntax,
				IllTyped {

			Parser<String> a = ParserUtils
					.seq(new QuotedKeywordParser("input"), ParserUtils.seq(
							new KeywordParser(":"), new QuotedParser()));
			Parser<String> b = ParserUtils
					.seq(new QuotedKeywordParser("output"), ParserUtils.seq(
							new KeywordParser(":"), new QuotedParser()));
			Parser<Pair<String, String>> u = ParserUtils.inside(a,
					new KeywordParser(","), b);

			Parser<Pair<String, String>> x = ParserUtils.outside(
					new KeywordParser("{"), u, new KeywordParser("}"));

			return x.parse(s);
		}

	}

	public static class MappingGeneratorParser
			implements
			Parser<List<Pair<Pair<Pair<String, String>, String>, List<Pair<Pair<String, String>, String>>>>>
			{

		@Override
		public Partial<List<Pair<Pair<Pair<String, String>, String>, List<Pair<Pair<String, String>, String>>>>> 
		parse(Tokens s) throws BadSyntax, IllTyped {

			Parser<Pair<Pair<String, String>, String>> a = ParserUtils.seq(
					new QuotedKeywordParser("arrow"),
					ParserUtils.seq(new KeywordParser(":"), new ArrowParser()));

			Parser<List<Pair<Pair<String, String>, String>>> b = ParserUtils
					.seq(new QuotedKeywordParser("path"), ParserUtils.seq(
							new KeywordParser(":"), new PathParser()));

			Parser<Pair<Pair<Pair<String, String>, String>, List<Pair<Pair<String, String>, String>>>> e = ParserUtils
					.inside(a, new KeywordParser(","), b);

			Parser<Pair<Pair<Pair<String, String>, String>, List<Pair<Pair<String, String>, String>>>> g = ParserUtils
					.outside(new KeywordParser("{"), e, new KeywordParser("}"));

			Parser<List<Pair<Pair<Pair<String, String>, String>, List<Pair<Pair<String, String>, String>>>>> h = ParserUtils
					.manySep(g, new KeywordParser(","));

			Parser<List<Pair<Pair<Pair<String, String>, String>, List<Pair<Pair<String, String>, String>>>>> r = ParserUtils
					.outside(new KeywordParser("["), h, new KeywordParser("]"));

//			Parser<Pair<String, List<Pair<Pair<Pair<String, String>, String>, List<Pair<Pair<String, String>, String>>>>>> u = ParserUtils
//					.inside(new QuotedParser(), new KeywordParser(":"), r);

			Parser<List<Pair<Pair<Pair<String, String>, String>, List<Pair<Pair<String, String>, String>>>>> x = ParserUtils
					.seq(new QuotedKeywordParser("onGenerators"), ParserUtils
							.seq(new KeywordParser(":"), r));

			return x.parse(s);

		}
	}

	public static class JSONSigParser implements Parser<Signature> {
		
		boolean outerparens;
		
		public JSONSigParser(boolean outerparens) {
			this.outerparens = outerparens;
		}

		@Override
		public Partial<Signature> parse(Tokens s) throws BadSyntax, IllTyped {
			String name;
			List<Pair<Pair<String, String>, String>> arrows;
			List<Pair<List<Pair<Pair<String, String>, String>>, List<Pair<Pair<String, String>, String>>>> eqs;
			List<String> obs;

			Parser<List<String>> ob = ParserUtils.seq(new QuotedKeywordParser(
					"objects"), ParserUtils.seq(new KeywordParser(":"),
					ParserUtils.outside(new KeywordParser("["),
							ParserUtils.manySep(new QuotedParser(),
									new KeywordParser(",")), new KeywordParser(
									"]"))));

			Parser<List<Pair<Pair<String, String>, String>>> arrs = ParserUtils
					.seq(new QuotedKeywordParser("arrows"), ParserUtils.seq(
							new KeywordParser(":"), ParserUtils.outside(
									new KeywordParser("["), ParserUtils
											.manySep(new ArrowParser(),
													new KeywordParser(",")),
									new KeywordParser("]"))));

			Parser<List<Pair<List<Pair<Pair<String, String>, String>>, List<Pair<Pair<String, String>, String>>>>> rel = ParserUtils
					.seq(new QuotedKeywordParser("relations"), ParserUtils.seq(
							new KeywordParser(":"), ParserUtils.outside(
									new KeywordParser("["), ParserUtils
											.manySep(new RelationParser(),
													new KeywordParser(",")),
									new KeywordParser("]"))));

			Parser<Pair<List<String>, List<Pair<Pair<String, String>, String>>>> oa = ParserUtils
					.inside(ob, new KeywordParser(","), arrs);

			Parser<Pair<Pair<List<String>, List<Pair<Pair<String, String>, String>>>, List<Pair<List<Pair<Pair<String, String>, String>>, List<Pair<Pair<String, String>, String>>>>>> u = ParserUtils
					.inside(oa, new KeywordParser(","), rel);

			Parser<Pair<Pair<List<String>, List<Pair<Pair<String, String>, String>>>, List<Pair<List<Pair<Pair<String, String>, String>>, List<Pair<Pair<String, String>, String>>>>>> 
			ret = u;
			
//			if (outerparens) {
				ret = ParserUtils
					.outside(new KeywordParser("{"), u, new KeywordParser("}"));
	//		}

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
			Parser<Pair<List<Pair<Pair<String, String>, String>>, List<Pair<Pair<String, String>, String>>>> {

		@Override
		public Partial<Pair<List<Pair<Pair<String, String>, String>>, List<Pair<Pair<String, String>, String>>>> parse(
				Tokens s) throws BadSyntax, IllTyped {
			Parser<List<Pair<Pair<String, String>, String>>> u1 = ParserUtils
					.seq(new QuotedKeywordParser("left"), ParserUtils.seq(
							new KeywordParser(":"), new PathParser()));
			Parser<List<Pair<Pair<String, String>, String>>> u2 = ParserUtils
					.seq(new QuotedKeywordParser("right"), ParserUtils.seq(
							new KeywordParser(":"), new PathParser()));

			return ParserUtils.outside(new KeywordParser("{"),
					ParserUtils.inside(u1, new KeywordParser(","), u2),
					new KeywordParser("}")).parse(s);
		}

	}

	public static class PathParser implements
			Parser<List<Pair<Pair<String, String>, String>>> {

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
			Parser<Pair<Pair<String, String>, String>> {
		@Override
		public Partial<Pair<Pair<String, String>, String>> parse(Tokens s)
				throws BadSyntax, IllTyped {
			Parser<?> p1 = new QuotedKeywordParser("source");
			Parser<?> p2 = new QuotedKeywordParser("target");
			Parser<?> p3 = new QuotedKeywordParser("label");

			Parser<String> q1 = ParserUtils
					.seq(p1, ParserUtils.seq(new KeywordParser(":"),
							new QuotedParser()));
			Parser<String> q2 = ParserUtils
					.seq(p2, ParserUtils.seq(new KeywordParser(":"),
							new QuotedParser()));
			Parser<String> q3 = ParserUtils
					.seq(p3, ParserUtils.seq(new KeywordParser(":"),
							new QuotedParser()));

			Parser<Pair<String, String>> l = ParserUtils.inside(q1,
					new KeywordParser(","), q2);
			Parser<Pair<Pair<String, String>, String>> u = ParserUtils.inside(
					l, new KeywordParser(","), q3);
			return ParserUtils.outside(new KeywordParser("{"), u,
					new KeywordParser("}")).parse(s);
		}

	}

}
