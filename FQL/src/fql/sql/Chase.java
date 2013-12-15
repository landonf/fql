package fql.sql;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;

import fql.DEBUG;
import fql.FQLException;
import fql.Pair;
import fql.Triple;
import fql.decl.Edge;
import fql.decl.Instance;
import fql.decl.Mapping;
import fql.decl.Node;
import fql.decl.Signature;
import fql.gui.FQLTextPanel;
import fql.parse.BadSyntax;
import fql.parse.FqlTokenizer;
import fql.parse.KeywordParser;
import fql.parse.ParserUtils;
import fql.parse.Partial;
import fql.parse.RyanParser;
import fql.parse.StringParser;
import fql.parse.Tokens;

/**
 * 
 * @author ryan
 *
 * Class for running the chase.
 */
public class Chase {

	static JButton run = new JButton("Run");

	public static void dostuff() {
		JFrame f = new JFrame("Chaser");

		run.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					Pair<String, String> x = run(eds.getText(), inst.getText(),
							(KIND) box.getSelectedItem());
					res.setText(x.second);
					eds0.setText(x.first);
				} catch (Throwable ee) {
					ee.printStackTrace();
					res.setText(ee.toString());
				}
			}

		});

		JPanel ret = new JPanel(new BorderLayout());

		JPanel inner = new JPanel(new GridLayout(3, 1));

		// JSplitPane pane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		// pane.add(p);
		// pane.add(q);
		// p.setPreferredSize(new Dimension(300,600));
		// q.setPreferredSize(new Dimension(300,600));
		// pane.setDividerLocation(0.5);

		inner.add(eds);
		inner.add(inst);
		// ret.add(eds0);
		// ret.add(eds1);
		inner.add(res);

		JPanel bar = new JPanel();
		bar.add(run);
		bar.add(box);

		ret.add(inner, BorderLayout.CENTER);
		ret.add(bar, BorderLayout.NORTH);

		f.setContentPane(ret);
		f.setSize(800, 600);
		f.setVisible(true);

	}

	static JComboBox<KIND> box = new JComboBox<>(new KIND[] { KIND.PARALLEL,
			KIND.STANDARD, KIND.CORE });
	static FQLTextPanel eds = new FQLTextPanel("EDs",
			"forall x, S(x,x) -> exists y, T(y,y)");
	static FQLTextPanel eds0 = new FQLTextPanel("Simplified EDs", "");
	static FQLTextPanel eds1 = new FQLTextPanel("Tuple EDs", "");
	static FQLTextPanel inst = new FQLTextPanel("Instance",
			"S -> {(a,a),(b,b)}, T -> {}");
	static FQLTextPanel res = new FQLTextPanel("Result", "");

	private static RyanParser<EmbeddedDependency> ed_p = make_ed_p();
	private static RyanParser<Pair<String, List<Pair<String, String>>>> inst_p0 = make_inst_p0();

	static RyanParser<List<EmbeddedDependency>> eds_p = ParserUtils.many(ed_p);
	static RyanParser<List<Pair<String, List<Pair<String, String>>>>> inst_p = ParserUtils
			.manySep(inst_p0, new KeywordParser(","));

	protected static Pair<String, String> run(String eds, String inst, KIND kind)
			throws Exception {
		Partial<List<EmbeddedDependency>> xxx = eds_p.parse(new FqlTokenizer(
				eds));
		List<EmbeddedDependency> eds0 = xxx.value;

		if (xxx.tokens.toString().trim().length() > 0) {
			throw new FQLException("Unconsumed input: " + xxx.tokens);
		}

		Partial<List<Pair<String, List<Pair<String, String>>>>> yyy = inst_p
				.parse(new FqlTokenizer(inst));
		Map<String, Set<Pair<Object, Object>>> inst0 = conv(yyy.value);

		if (yyy.tokens.toString().trim().length() > 0) {
			throw new FQLException("Unconsumed input: " + yyy.tokens);
		}

		Pair<List<Triple<List<String>, List<Triple<String, String, String>>, List<Triple<String, String, String>>>>, List<Triple<List<String>, List<Triple<String, String, String>>, List<Pair<String, String>>>>> zzz = split(eds0);

		if (inst0.size() == 0) {
			return new Pair<>(printNicely3(zzz), "");
		}

		Map<String, Set<Pair<Object, Object>>> res = chase(
				new HashSet<String>(), zzz, inst0, kind);

		return new Pair<>(printNicely3(zzz), printNicely(res));
	}

	private static String printNicely3(
			Pair<List<Triple<List<String>, List<Triple<String, String, String>>, List<Triple<String, String, String>>>>, List<Triple<List<String>, List<Triple<String, String, String>>, List<Pair<String, String>>>>> zzz) {
		String ret = "";
		for (Triple<List<String>, List<Triple<String, String, String>>, List<Triple<String, String, String>>> ed : zzz.first) {
			ret += ed + "\n\n";
		}
		for (Triple<List<String>, List<Triple<String, String, String>>, List<Pair<String, String>>> ed : zzz.second) {
			ret += ed + "\n\n";
		}
		return ret.trim();
	}

	private static RyanParser<Pair<String, List<Pair<String, String>>>> make_inst_p0() {
		return ParserUtils.inside(new StringParser(), new KeywordParser("->"),
				ParserUtils.outside(new KeywordParser("{"), ParserUtils
						.manySep(ParserUtils.outside(new KeywordParser("("),
								ParserUtils.inside(new StringParser(),
										new KeywordParser(","),
										new StringParser()), new KeywordParser(
										")")), new KeywordParser(",")),
						new KeywordParser("}")));
	}

	private static RyanParser<EmbeddedDependency> make_ed_p() {
		return new RyanParser<EmbeddedDependency>() {

			@Override
			public Partial<EmbeddedDependency> parse(Tokens s) throws BadSyntax {
				RyanParser<List<String>> strings = ParserUtils
						.many(new StringParser());
				RyanParser<List<Triple<String, String, String>>> where1 = ParserUtils
						.manySep(facts_p(), new KeywordParser("/\\"));

				RyanParser<Pair<List<Triple<String, String, String>>, List<Pair<String, String>>>> where2 = facts_p2();

				RyanParser<Pair<List<String>, List<Triple<String, String, String>>>> p = ParserUtils
						.seq(new KeywordParser("forall"), ParserUtils.inside(
								strings, new KeywordParser(","), where1));

				RyanParser<Pair<List<String>, Pair<List<Triple<String, String, String>>, List<Pair<String, String>>>>> q = ParserUtils
						.seq(new KeywordParser("exists"), ParserUtils.inside(
								strings, new KeywordParser(","), where2));

				RyanParser<Pair<Pair<List<String>, List<Triple<String, String, String>>>, Pair<List<String>, Pair<List<Triple<String, String, String>>, List<Pair<String, String>>>>>> xxx = ParserUtils
						.inside(p, new KeywordParser("->"), q);

				Partial<Pair<Pair<List<String>, List<Triple<String, String, String>>>, Pair<List<String>, Pair<List<Triple<String, String, String>>, List<Pair<String, String>>>>>> yyy = xxx
						.parse(s);
				s = yyy.tokens;
				Pair<Pair<List<String>, List<Triple<String, String, String>>>, Pair<List<String>, Pair<List<Triple<String, String, String>>, List<Pair<String, String>>>>> t = yyy.value;

				// RyanParser<List<String>> q = ParserUtils.outside(new
				// KeywordParser("where"), strings, new KeywordParser("where"));

				// List<Triple<String, String, String>> not = new
				// LinkedList<>();

				return new Partial<EmbeddedDependency>(s,
						new EmbeddedDependency(t.first.first, t.second.first,
								t.first.second, t.second.second.first,
								t.second.second.second));

			}

		};
	}

	protected static RyanParser<Pair<List<Triple<String, String, String>>, List<Pair<String, String>>>> facts_p2() {
		return new RyanParser<Pair<List<Triple<String, String, String>>, List<Pair<String, String>>>>() {

			@SuppressWarnings("unchecked")
			@Override
			public Partial<Pair<List<Triple<String, String, String>>, List<Pair<String, String>>>> parse(
					Tokens s) throws BadSyntax {

				RyanParser<List<Object>> objs0 = ParserUtils.manySep(
						ParserUtils.or(facts_p(), eq_p()), new KeywordParser(
								"/\\"));
				Partial<List<Object>> par = objs0.parse(s);
				List<Object> objs = par.value;
				s = par.tokens;

				List<Triple<String, String, String>> l1 = new LinkedList<>();
				List<Pair<String, String>> l2 = new LinkedList<>();

				for (Object o : objs) {
					if (o instanceof Triple) {
						l1.add((Triple<String, String, String>) o);
					} else {
						l2.add((Pair<String, String>) o);
					}
				}

				return new Partial<>(s, new Pair<>(l1, l2));
			}

		};
	}

	protected static RyanParser<Pair<String, String>> eq_p() {
		return ParserUtils.inside(new StringParser(), new KeywordParser("="),
				new StringParser());
	}

	private static RyanParser<Triple<String, String, String>> facts_p() {
		return new RyanParser<Triple<String, String, String>>() {

			@Override
			public Partial<Triple<String, String, String>> parse(Tokens s)
					throws BadSyntax {
				StringParser p = new StringParser();

				String b;
				String a;
				String r;

				Partial<String> x = p.parse(s);
				s = x.tokens;
				r = x.value;

				RyanParser<Pair<String, String>> h = ParserUtils.outside(
						new KeywordParser("("), ParserUtils.inside(
								new StringParser(), new KeywordParser(","),
								new StringParser()), new KeywordParser(")"));
				Partial<Pair<String, String>> y = h.parse(s);
				s = y.tokens;
				a = y.value.first;
				b = y.value.second;

				return new Partial<Triple<String, String, String>>(s,
						new Triple<String, String, String>(r, a, b));
			}

		};
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static Map<String, Set<Pair<Object, Object>>> conv(
			List<Pair<String, List<Pair<String, String>>>> value) {
		Map<String, Set<Pair<Object, Object>>> ret = new HashMap<>();

		for (Pair<String, List<Pair<String, String>>> k : value) {
			ret.put(k.first, new HashSet(k.second));
		}
		return ret;
	}

	private static String printNicely(Map<String, Set<Pair<Object, Object>>> m) {
		String ret = "";
		for (String k : m.keySet()) {
			ret += k + " = {" + printNicely2(m.get(k)) + "}";
			ret += "\n";
		}
		return ret;
	}

	private static String printNicely2(Set<Pair<Object, Object>> set) {
		String ret = "";
		for (Pair<Object, Object> p : set) {
			ret += p.toString();
		}
		return ret;
	}

	public static Pair<List<Triple<List<String>, List<Triple<String, String, String>>, List<Triple<String, String, String>>>>, List<Triple<List<String>, List<Triple<String, String, String>>, List<Pair<String, String>>>>> split(
			List<EmbeddedDependency> l) {
		List<Triple<List<String>, List<Triple<String, String, String>>, List<Triple<String, String, String>>>> ret1 = new LinkedList<>();
		List<Triple<List<String>, List<Triple<String, String, String>>, List<Pair<String, String>>>> ret2 = new LinkedList<>();
		for (EmbeddedDependency e : l) {
			Triple<List<String>, List<Triple<String, String, String>>, List<Triple<String, String, String>>> s = new Triple<>(
					e.forall, e.where, e.tgd);
			Triple<List<String>, List<Triple<String, String, String>>, List<Pair<String, String>>> t = new Triple<>(
					e.forall, e.where, e.egd);
			if (s.third.size() > 0) {
				ret1.add(s);
			}
			if (t.third.size() > 0) {
				ret2.add(t);
			}
		}
		// System.out.println(l);
		// System.out.println(ret1);
		// System.out.println(ret2);
		return new Pair<>(ret1, ret2);
	}

	static int ruleNo = -1;

	public static enum KIND {
		PARALLEL, STANDARD, CORE
	};

	private static Map<String, Set<Pair<Object, Object>>> chase(
			Set<String> keys,
			Pair<List<Triple<List<String>, List<Triple<String, String, String>>, List<Triple<String, String, String>>>>, List<Triple<List<String>, List<Triple<String, String, String>>, List<Pair<String, String>>>>> zzz,
			Map<String, Set<Pair<Object, Object>>> inst0, KIND kind) {

		Map<String, Set<Pair<Object, Object>>> ret = new HashMap<>(inst0);

		List<Triple<List<String>, List<Triple<String, String, String>>, List<Triple<String, String, String>>>> tgds = zzz.first;
		List<Triple<List<String>, List<Triple<String, String, String>>, List<Pair<String, String>>>> egds = zzz.second;

		int i = 0;
		ruleNo = 0;
		for (;;) {
			// System.out.println("iteration " + i);
			Map<String, Set<Pair<Object, Object>>> ret_old = new HashMap<>(ret);
			switch (kind) {
			case CORE:
				boolean fired = false;
				for (Triple<List<String>, List<Triple<String, String, String>>, List<Triple<String, String, String>>> tgd : tgds) {
					ret = union(ret,
							chaseTgd(ret_old, tgd.first, tgd.second, tgd.third));
					fired = !ret.equals(ret_old);
				}
				for (Triple<List<String>, List<Triple<String, String, String>>, List<Pair<String, String>>> egd : egds) {
					ret = apply(ret,
							chaseEgd(ret_old, egd.first, egd.second, egd.third));
					fired = !ret.equals(ret_old);
				}
				ret = minimize(keys, ret, tgds, egds);
				if (!fired) {
					return ret;
				}
				break;
			case PARALLEL:
				for (Triple<List<String>, List<Triple<String, String, String>>, List<Triple<String, String, String>>> tgd : tgds) {
					ret = union(ret,
							chaseTgd(ret_old, tgd.first, tgd.second, tgd.third));
				}
				for (Triple<List<String>, List<Triple<String, String, String>>, List<Pair<String, String>>> egd : egds) {
					ret = apply(ret,
							chaseEgd(ret_old, egd.first, egd.second, egd.third));
				}
				// System.out.println("comparing " + ret + " and " + ret_old);
				if (ret.equals(ret_old)) {
					return ret;
				}
				break;
			case STANDARD:
				fired = false;
				// System.out.println("ruleNo " + ruleNo);
				// System.out.println("tgd size " + tgds.size());
				// System.out.println("egd size " + egds.size());
				for (int x = 0; x < tgds.size() + egds.size(); x++) {
					int pos = (ruleNo + x) % (tgds.size() + egds.size());
					System.out.println("x " + x + "\npos" + pos);
					if (pos < egds.size()) {
						Triple<List<String>, List<Triple<String, String, String>>, List<Pair<String, String>>> egd = egds
								.get(pos);
						ret = apply(
								ret,
								chaseEgd(ret_old, egd.first, egd.second,
										egd.third));
					} else if (pos < tgds.size() + egds.size()) {
						Triple<List<String>, List<Triple<String, String, String>>, List<Triple<String, String, String>>> tgd = tgds
								.get(pos - egds.size());
						ret = union(
								ret,
								chaseTgd(ret_old, tgd.first, tgd.second,
										tgd.third));
					}
					if (!ret.equals(ret_old)) {
						ruleNo++;
						if (ruleNo == tgds.size() + egds.size()) {
							ruleNo = 0;
						}
						fired = true;
						break;
					}
				}
				if (!fired) {
					return ret;
				}
				// System.out.println("\n\n\n" + ret);
			}

			if (i++ > DEBUG.chase_limit) {
				throw new RuntimeException("Chase exceeds " + DEBUG.chase_limit
						+ " iterations");
			}
		}

		// return ret;
	}

	private static Map<String, Set<Pair<Object, Object>>> minimize(
			Set<String> keys,
			Map<String, Set<Pair<Object, Object>>> I,
			List<Triple<List<String>, List<Triple<String, String, String>>, List<Triple<String, String, String>>>> tgds,
			List<Triple<List<String>, List<Triple<String, String, String>>, List<Pair<String, String>>>> egds) {

		// System.out.println("minimizing " + I);

		Map<String, Set<Pair<Object, Object>>> sol = I;
		for (Map<String, Set<Pair<Object, Object>>> I0 : smaller(keys, I, tgds,
				egds)) {
			// System.out.println("checking " + I0);
			if (hasHomo(keys, I0, I) && hasHomo(keys, I, I0)) {
				// System.out.println("has homo ");
				if (size(I0) < size(sol)) {
					// System.out.println("smaller sized");
					sol = I0;
				}
			}
		}

		return sol;
	}

	private static List<Map<String, Set<Pair<Object, Object>>>> smaller(
			Set<String> keys,
			Map<String, Set<Pair<Object, Object>>> i,
			List<Triple<List<String>, List<Triple<String, String, String>>, List<Triple<String, String, String>>>> tgds,
			List<Triple<List<String>, List<Triple<String, String, String>>, List<Pair<String, String>>>> egds) {
		Set<Object> ids = ids(keys, i);
		Collection<Set<Object>> subsets = pow(ids);
		List<Map<String, Set<Pair<Object, Object>>>> ret = new LinkedList<>();

		for (Set<Object> subset : subsets) {
			Map<String, Set<Pair<Object, Object>>> i0 = remove(i, subset);
			if (obeys(i0, tgds, egds)) {
				ret.add(i0);
			}
		}

		return ret;
	}

	private static boolean obeys(
			Map<String, Set<Pair<Object, Object>>> i0,
			List<Triple<List<String>, List<Triple<String, String, String>>, List<Triple<String, String, String>>>> tgds,
			List<Triple<List<String>, List<Triple<String, String, String>>, List<Pair<String, String>>>> egds) {

		for (Triple<List<String>, List<Triple<String, String, String>>, List<Pair<String, String>>> egd : egds) {
			EmbeddedDependency xxx0 = conv(egd.first, egd.second, egd.third);
			ED xxx = ED.from(xxx0);

			Flower front = xxx.front();
			Flower back = xxx.back();
			Set<Map<Object, Object>> frontX = front.eval(ED.conv(i0));
			Set<Map<Object, Object>> backX = back.eval(ED.conv(i0));
			if (!frontX.equals(backX)) {
				return false;
			}
		}
		for (Triple<List<String>, List<Triple<String, String, String>>, List<Triple<String, String, String>>> tgd : tgds) {
			EmbeddedDependency xxx0 = conv2(tgd.first, tgd.second, tgd.third);
			ED xxx = ED.from(xxx0);

			Flower front = xxx.front();
			Flower back = xxx.back();
			Set<Map<Object, Object>> frontX = front.eval(ED.conv(i0));
			Set<Map<Object, Object>> backX = back.eval(ED.conv(i0));
			if (!frontX.equals(backX)) {
				return false;
			}
		}

		return true;
	}

	private static Map<String, Set<Pair<Object, Object>>> remove(
			Map<String, Set<Pair<Object, Object>>> i, Set<Object> subset) {

		Map<String, Set<Pair<Object, Object>>> ret = new HashMap<>();

		for (String k : i.keySet()) {
			ret.put(k, remove(i.get(k), subset));
		}

		return ret;
	}

	private static Set<Pair<Object, Object>> remove(
			Set<Pair<Object, Object>> set, Set<Object> subset) {
		Set<Pair<Object, Object>> ret = new HashSet<>();

		for (Pair<Object, Object> p : set) {
			if (subset.contains(p.first) || subset.contains(p.second)) {
				continue;
			}
			ret.add(p);
		}

		return ret;
	}

	private static boolean hasHomo(Set<String> keys,
			Map<String, Set<Pair<Object, Object>>> i,
			Map<String, Set<Pair<Object, Object>>> i0) {

		List<List<Pair<Object, Object>>> homs = homomorphs(ids(keys, i),
				ids(keys, i0));
		for (List<Pair<Object, Object>> hom : homs) {
			Map<String, Set<Pair<Object, Object>>> applied = apply(i, hom);
			if (subset(applied, i0)) {
				return true;
			}
		}
		return false;
	}

	private static boolean subset(Map<String, Set<Pair<Object, Object>>> l,
			Map<String, Set<Pair<Object, Object>>> r) {
		for (String k : l.keySet()) {
			for (Pair<Object, Object> p : l.get(k)) {
				if (!r.get(k).contains(p)) {
					return false;
				}
			}
		}
		return true;
	}

	private static Set<Object> ids(Set<String> keys,
			Map<String, Set<Pair<Object, Object>>> i) {
		Set<Object> ret = new HashSet<>();
		// ignore keys
		for (String k : i.keySet()) {
			for (Pair<Object, Object> p : i.get(k)) {
				ret.add(p.first);
				ret.add(p.second);
			}
		}

		return ret;
	}

	public static <X> List<List<Pair<X, X>>> homomorphs(Collection<X> A,
			Collection<X> B) {
		List<List<Pair<X, X>>> ret = new LinkedList<>();

		if (A.size() == 0) {
			return ret;
		}

		if (B.size() == 0) {
			throw new RuntimeException();
		}

		int[] counters = new int[A.size() + 1];

		// int i = 0;
		for (;;) {

			if (counters[A.size()] == 1) {
				break;
			}
			ret.add(make2(counters, new LinkedList<>(A), new LinkedList<>(B)));
			inc(counters, B.size());
		}

		return ret;
	}

	private static <X> List<Pair<X, X>> make2(int[] counters, List<X> A,
			List<X> B) {
		List<Pair<X, X>> ret = new LinkedList<>();
		int i = 0;
		for (X x : A) {
			ret.add(new Pair<>(x, B.get(counters[i++])));
		}
		return ret;
	}

	private static void inc(int[] counters, int size) {
		counters[0]++;
		for (int i = 0; i < counters.length - 1; i++) {
			if (counters[i] == size) {
				counters[i] = 0;
				counters[i + 1]++;
			}
		}
	}

	private static int size(Map<String, Set<Pair<Object, Object>>> sol) {
		int i = 0;

		for (String k : sol.keySet()) {
			i += sol.get(k).size();
		}

		return i;
	}

	private static Map<String, Set<Pair<Object, Object>>> union(
			Map<String, Set<Pair<Object, Object>>> a,
			Map<String, Set<Pair<Object, Object>>> b) {
		Map<String, Set<Pair<Object, Object>>> ret = new HashMap<>();

		for (String k : a.keySet()) {
			Set<Pair<Object, Object>> x = new HashSet<>();
			x.addAll(a.get(k));
			x.addAll(b.get(k));
			ret.put(k, x);
		}

		return ret;
	}

	private static Map<String, Set<Pair<Object, Object>>> apply(
			Map<String, Set<Pair<Object, Object>>> I,
			List<Pair<Object, Object>> subst) {

		// System.out.println("Subst " + subst);
		// System.out.println(I);

		Map<String, Set<Pair<Object, Object>>> ret = new HashMap<>(I);

		List<Pair<Object, Object>> subst0 = new LinkedList<>(subst);

		for (;;) {
			if (subst0.size() == 0) {
				break;
			}
			Pair<Object, Object> phi = subst0.remove(0);
			ret = apply0(ret, phi);
			subst0 = apply2(subst0, phi);
		}

		// System.out.println(ret);

		return ret;
	}

	private static List<Pair<Object, Object>> apply2(
			List<Pair<Object, Object>> l, Pair<Object, Object> phi) {

		List<Pair<Object, Object>> ret = new LinkedList<>();

		for (Pair<Object, Object> p : l) {
			ret.add(new Pair<>(
					p.first.equals(phi.first) ? phi.second : p.first, p.second
							.equals(phi.first) ? phi.second : p.second));
		}

		return ret;

	}

	private static Map<String, Set<Pair<Object, Object>>> apply0(
			Map<String, Set<Pair<Object, Object>>> I, Pair<Object, Object> s) {

		Map<String, Set<Pair<Object, Object>>> ret = new HashMap<>();

		for (String k : I.keySet()) {
			ret.put(k, apply1(I.get(k), s));
		}

		return ret;
	}

	private static Set<Pair<Object, Object>> apply1(
			Set<Pair<Object, Object>> set, Pair<Object, Object> s) {

		Set<Pair<Object, Object>> ret = new HashSet<>();

		for (Pair<Object, Object> p : set) {
			ret.add(new Pair<>(p.first.equals(s.first) ? s.second : p.first,
					p.second.equals(s.first) ? s.second : p.second));
		}

		return ret;
	}

	static int fresh = 0;

	private static Map<String, Set<Pair<Object, Object>>> chaseTgd(
			Map<String, Set<Pair<Object, Object>>> i, List<String> forall,
			List<Triple<String, String, String>> where,
			List<Triple<String, String, String>> t) {

		Map<String, Set<Pair<Object, Object>>> ret = new HashMap<>();
		for (String k : i.keySet()) {
			ret.put(k, new HashSet<Pair<Object, Object>>());
		}

		EmbeddedDependency xxx0 = conv2(forall, where, t);
		ED xxx = ED.from(xxx0);

		// System.out.println("TGD " + xxx);

		Flower front = xxx.front();
		// System.out.println("Front " + front);

		Flower back = xxx.back();
		// System.out.println("Back " + back);

		Set<Map<Object, Object>> frontX = front.eval(ED.conv(i));
		// System.out.println("Front " + frontX);

		Set<Map<Object, Object>> backX = back.eval(ED.conv(i));
		// System.out.println("Back " + backX);

		if (frontX.equals(backX)) {
			return ret;
		}

		// System.out.println("Firing on " + i);

		for (Map<Object, Object> eq : frontX) {
			// System.out.println("eq is " + eq);

			Map<String, String> map = new HashMap<>();
			for (String v : xxx0.exists) {
				String v0 = "_" + (fresh++);
				map.put(v, v0);
			}

			for (Triple<String, String, String> fact : xxx0.tgd) {
				Object a;
				try {
					a = eq.get("c"
							+ getColNo(xxx0.forall, xxx0.where, fact.second));
				} catch (Exception ee) {
					a = map.get(fact.second);
				}
				Object b;
				try {
					b = eq.get("c"
							+ getColNo(xxx0.forall, xxx0.where, fact.third));
				} catch (Exception ee) {
					b = map.get(fact.third);
				}
				ret.get(fact.first).add(new Pair<Object, Object>(a, b));
			}

			// Map<String, Object> n = new HashMap<>();
			// System.out.println(a);
			// System.out.println(b);
			// for (Map<String, Object> row : frontX) {
			// ret.add(new Pair<>(row.get("c" + a), row.get("c" + b)));
			// }
		}

		// System.out.println("Add " + ret);

		return ret;

	}

	private static List<Pair<Object, Object>> chaseEgd(
			Map<String, Set<Pair<Object, Object>>> i, List<String> forall,
			List<Triple<String, String, String>> where,
			List<Pair<String, String>> t) {

		List<Pair<Object, Object>> ret = new LinkedList<>();

		EmbeddedDependency xxx0 = conv(forall, where, t);
		ED xxx = ED.from(xxx0);

		// System.out.println("ED " + xxx);

		Flower front = xxx.front();
		// System.out.println("Front " + front);

		Flower back = xxx.back();
		// System.out.println("Back " + back);

		Set<Map<Object, Object>> frontX = front.eval(ED.conv(i));
		// System.out.println("Front " + frontX);

		Set<Map<Object, Object>> backX = back.eval(ED.conv(i));
		// System.out.println("Back " + backX);

		if (frontX.equals(backX)) {
			return ret;
		}

		// System.out.println("Firing on " + i);

		for (Pair<String, String> eq : t) {
			// System.out.println("eq is " + eq);
			int a = getColNo(xxx0.forall, xxx0.where, eq.first);
			int b = getColNo(xxx0.forall, xxx0.where, eq.second);
			// System.out.println(a);
			// System.out.println(b);
			for (Map<Object, Object> row : frontX) {
				ret.add(new Pair<>(row.get("c" + a), row.get("c" + b)));
			}
		}

		// System.out.println("Subst " + ret);

		return ret;
	}

	private static int getColNo(List<String> f,
			List<Triple<String, String, String>> s, String str) {
		int i = 0;
		for (Triple<String, String, String> k : s) {
			if (str.equals(k.second)) {
				return i;
			}
			if (str.equals(k.third)) {
				return i + 1;
			}
			i += 2;
		}
		throw new RuntimeException("Canont find " + str + " in " + s);
	}

	private static EmbeddedDependency conv(List<String> f,
			List<Triple<String, String, String>> s, List<Pair<String, String>> t) {

		// System.out.println("Conv on " + f + " and " + s + " and " + t);

		Set<String> all = new HashSet<>(f);
		for (Pair<String, String> p : t) {
			all.add(p.first);
			all.add(p.second);
		}

		List<String> exists = new LinkedList<>(all);
		exists.removeAll(f);

		List<Triple<String, String, String>> tgd = new LinkedList<>();

		return new EmbeddedDependency(f, exists, s, tgd, t);

	}

	private static EmbeddedDependency conv2(List<String> f,
			List<Triple<String, String, String>> s,
			List<Triple<String, String, String>> t) {

		// System.out.println("Conv on " + f + " and " + s + " and " + t);

		Set<String> all = new HashSet<>(f);
		for (Triple<String, String, String> p : t) {
			all.add(p.second);
			all.add(p.third);
		}
		for (Triple<String, String, String> p : t) {
			all.add(p.second);
			all.add(p.third);
		}

		List<String> exists = new LinkedList<>(all);
		exists.removeAll(f);

		List<Pair<String, String>> egd = new LinkedList<>();

		return new EmbeddedDependency(f, exists, s, t, egd);

	}

	/*
	 * public static Instance pi(Mapping m, Instance i) throws FQLException {
	 * 
	 * Signature cd = m.toEDs().third;
	 * 
	 * Map<String, Set<Pair<Object, Object>>> I = new HashMap<>(); for (Node n :
	 * cd.nodes) { I.put(n.string, new HashSet<Pair<Object, Object>>()); } for
	 * (Edge n : cd.edges) { I.put(n.name, new HashSet<Pair<Object, Object>>());
	 * } for (String k : i.data.keySet()) { I.put("src_" + k, i.data.get(k)); }
	 * 
	 * List<EmbeddedDependency> eds0 = cd.toED("");
	 * 
	 * Pair<List<Triple<List<String>, List<Triple<String, String, String>>,
	 * List<Triple<String, String, String>>>>, List<Triple<List<String>,
	 * List<Triple<String, String, String>>, List<Pair<String, String>>>>> zzz =
	 * split(eds0);
	 * 
	 * Set<String> keys = new HashSet<>(); for (Node n : m.target.nodes) {
	 * keys.add("dst_" + n.string); } for (Edge n : m.target.edges) {
	 * keys.add("dst_" + n.name); } Map<String, Set<Pair<Object, Object>>> res =
	 * chase(keys, zzz, I, KIND.CORE); Map<String, Set<Pair<Object, Object>>>
	 * res0 = new HashMap<>(); for (Node n : m.target.nodes) {
	 * res0.put(n.string, res.get("dst_" + n.string)); } for (Edge n :
	 * m.target.edges) { res0.put(n.name, res.get("dst_" + n.name)); }
	 * 
	 * Instance ret = new Instance("chased_" + i.name, m.target, res0);
	 * 
	 * return ret;
	 * 
	 * }
	 */
	public static Instance sigma(Mapping m, Instance i) throws FQLException {

		Signature cd = m.toEDs().second;

		Map<String, Set<Pair<Object, Object>>> I = new HashMap<>();
		for (Node n : cd.nodes) {
			I.put(n.string, new HashSet<Pair<Object, Object>>());
		}
		for (Edge n : cd.edges) {
			I.put(n.name, new HashSet<Pair<Object, Object>>());
		}
		for (String k : i.data.keySet()) {
			I.put("src_" + k, i.data.get(k));
		}

		List<EmbeddedDependency> eds0 = cd.toED("");

		Pair<List<Triple<List<String>, List<Triple<String, String, String>>, List<Triple<String, String, String>>>>, List<Triple<List<String>, List<Triple<String, String, String>>, List<Pair<String, String>>>>> zzz = split(eds0);

		Set<String> keys = new HashSet<>();
		for (Node n : m.target.nodes) {
			keys.add("dst_" + n.string);
		}
		for (Edge n : m.target.edges) {
			keys.add("dst_" + n.name);
		}
		Map<String, Set<Pair<Object, Object>>> res = chase(keys, zzz, I,
				KIND.PARALLEL);
		Map<String, Set<Pair<Object, Object>>> res0 = new HashMap<>();
		for (Node n : m.target.nodes) {
			res0.put(n.string, res.get("dst_" + n.string));
		}
		for (Edge n : m.target.edges) {
			res0.put(n.name, res.get("dst_" + n.name));
		}

		Instance ret = new Instance(m.target, res0);

		return ret;

	}

	public static Instance delta(Mapping m, Instance i) throws FQLException {

		Signature cd = m.toEDs().first;

		Map<String, Set<Pair<Object, Object>>> I = new HashMap<>();
		for (Node n : cd.nodes) {
			I.put(n.string, new HashSet<Pair<Object, Object>>());
		}
		for (Edge n : cd.edges) {
			I.put(n.name, new HashSet<Pair<Object, Object>>());
		}
		for (String k : i.data.keySet()) {
			I.put("dst_" + k, i.data.get(k));
		}

		List<EmbeddedDependency> eds0 = cd.toED("");

		Pair<List<Triple<List<String>, List<Triple<String, String, String>>, List<Triple<String, String, String>>>>, List<Triple<List<String>, List<Triple<String, String, String>>, List<Pair<String, String>>>>> zzz = split(eds0);

		Set<String> keys = new HashSet<>();
		for (Node n : m.target.nodes) {
			keys.add("src_" + n.string);
		}
		for (Edge n : m.target.edges) {
			keys.add("src_" + n.name);
		}
		Map<String, Set<Pair<Object, Object>>> res = chase(keys, zzz, I,
				KIND.PARALLEL);
		Map<String, Set<Pair<Object, Object>>> res0 = new HashMap<>();
		for (Node n : m.source.nodes) {
			res0.put(n.string, res.get("src_" + n.string));
		}
		for (Edge n : m.source.edges) {
			res0.put(n.name, res.get("src_" + n.name));
		}

		Instance ret = new Instance(m.source, res0);

		return ret;

	}

	public static <T> Set<Set<T>> pow(Set<T> originalSet) {
		Set<Set<T>> sets = new HashSet<Set<T>>();
		if (originalSet.isEmpty()) {
			sets.add(new HashSet<T>());
			return sets;
		}
		List<T> list = new ArrayList<T>(originalSet);
		T head = list.get(0);
		Set<T> rest = new HashSet<T>(list.subList(1, list.size()));
		for (Set<T> set : pow(rest)) {
			Set<T> newSet = new HashSet<T>();
			newSet.add(head);
			newSet.addAll(set);
			sets.add(newSet);
			sets.add(set);
		}
		return sets;
	}
}
