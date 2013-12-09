package fql.sql;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;

import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.Parsers;
import org.codehaus.jparsec.Scanners;
import org.codehaus.jparsec.Terminals;
import org.codehaus.jparsec.functors.Tuple3;
import org.codehaus.jparsec.functors.Tuple4;
import org.codehaus.jparsec.functors.Tuple5;

import fql.Pair;
import fql.Triple;
import fql.decl.InstExp;
import fql.decl.MapExp;
import fql.decl.SigExp;
import fql.decl.SigExp.Const;
import fql.examples.Example;
import fql.gui.FQLTextPanel;

public class RaToFql {

	protected Example[] examples = { new PeopleExample() };

	String help = "Bags of tuples can be represented in FQL using an explicit active domain construction.  See the People example.  Unions of conjunctive queries are supported, using DISTINCT and ALL for set semantics.  Primary and foreign keys are not supported by this encoding.  WHERE clauses must have equalities between variables, not constants.";

	protected String kind() {
		return "RA";
	}

	static class PeopleExample extends Example {
		@Override
		public String getName() {
			return "People";
		}

		@Override
		public String getText() {
			return extext1;
		}
	}

	String translate(String in) {
		List<EExternal> list = program(in);
		return transSQLSchema(list);
	}

	public RaToFql() {
		final FQLTextPanel input = new FQLTextPanel(kind() + " Input", "");
		final FQLTextPanel output = new FQLTextPanel("FQL Output", "");

		// JButton jdbcButton = new JButton("Load using JDBC");
		// JButton runButton = new JButton("Run " + kind());
		JButton transButton = new JButton("Translate");
		JButton helpButton = new JButton("Help");
		// JButton runButton2 = new JButton("Run FQL");
		// JCheckBox jdbcBox = new JCheckBox("Run using JDBC");
		// JLabel lbl = new JLabel("Suffix:", JLabel.RIGHT);
		// lbl.setToolTipText("FQL will translate table T to T_suffix, and generate SQL to load T into T_suffix");
		// final JTextField field = new JTextField(8);
		// field.setText("fql");

		final JComboBox<Example> box = new JComboBox<>(examples);
		box.setSelectedIndex(-1);
		box.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				input.setText(((Example) box.getSelectedItem()).getText());
			}
		});

		// TODO shred and unshred queries

		transButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					output.setText(translate(input.getText()).toString());
				} catch (Exception ex) {
					ex.printStackTrace();
					output.setText(ex.getLocalizedMessage());
				}
			}
		});

		helpButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JTextArea jta = new JTextArea(help);
				jta.setWrapStyleWord(true);
				// jta.setEditable(false);
				jta.setLineWrap(true);
				JScrollPane p = new JScrollPane(jta,
						JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
						JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
				p.setPreferredSize(new Dimension(300, 200));

				JOptionPane pane = new JOptionPane(p);
				// Configure via set methods
				JDialog dialog = pane.createDialog(null, "Help on RA to FQL");
				dialog.setModal(false);
				dialog.setVisible(true);
				dialog.setResizable(true);

			}
		});

		JPanel p = new JPanel(new BorderLayout());

		JSplitPane jsp = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		jsp.setBorder(BorderFactory.createEmptyBorder());
		jsp.setDividerSize(4);
		jsp.setResizeWeight(0.5d);
		jsp.add(input);
		jsp.add(output);

		// JPanel bp = new JPanel(new GridLayout(1, 5));
		JPanel tp = new JPanel(new GridLayout(1, 5));

		// bp.add(field);

		tp.add(transButton);
		tp.add(helpButton);
		// tp.add(jdbcButton);
		// tp.add(helpButton);
		tp.add(new JLabel());
		// tp.add(lbl);
		// tp.add(field);
		tp.add(new JLabel("Load Example", JLabel.RIGHT));
		tp.add(box);

		// bp.add(runButton);
		// bp.add(runButton2);
		// bp.add(lbl);
		// bp.add(field);
		// bp.add(jdbcBox);

		// p.add(bp, BorderLayout.SOUTH);
		p.add(jsp, BorderLayout.CENTER);
		p.add(tp, BorderLayout.NORTH);
		JFrame f = new JFrame(kind() + " to FQL");
		f.setContentPane(p);
		f.pack();
		f.setSize(new Dimension(700, 600));
		f.setLocationRelativeTo(null);
		f.setVisible(true);
	}

	static String extext1 = "CREATE TABLE Place ("
			+ "\n description VARCHAR(255)"
			+ "\n);  "
			+ "\n"
			+ "\nCREATE TABLE Person ("
			+ "\n name VARCHAR(255), "
			+ "\n home VARCHAR(255)"
			+ "\n);"
			+ "\n"
			+ "\nINSERT INTO Place VALUES (\"New York\"),(\"Chicago\"),(\"New York\"); //bag semantics "
			+ "\nINSERT INTO Person VALUES (\"Alice\", \"Chicago\");"
			+ "\n"
			+ "\nSELECT DISTINCT x.description AS col0, z.name AS col1"
			+ "\nFROM Place AS x, Place AS y, Person AS z "
			+ "\nWHERE x.description = y.description AND x.description = z.home;"
			+ "\n"
			+ "\n(SELECT x.name AS col FROM Person AS x) "
			+ "\n UNION "
			+ "\n(SELECT x.name AS col FROM Person AS x);";

	public static String transSQLSchema(List<EExternal> in) {
		List<Pair<List<String>, List<String>>> eqs = new LinkedList<>();
		List<Triple<String, String, String>> arrows = new LinkedList<>();
		List<Triple<String, String, String>> attrs = new LinkedList<>();
		List<String> nodes = new LinkedList<>();

		List<Pair<String, List<Pair<Object, Object>>>> inodes = new LinkedList<>();
		List<Pair<String, List<Pair<Object, Object>>>> iattrs = new LinkedList<>();
		List<Pair<String, List<Pair<Object, Object>>>> iarrows = new LinkedList<>();
		String adom = "adom";
		nodes.add(adom);
		List<Pair<Object, Object>> adomT = new LinkedList<>();
		LinkedList<Pair<Object, Object>> attT = new LinkedList<>();
		inodes.add(new Pair<String, List<Pair<Object, Object>>>(adom, adomT));
		iattrs.add(new Pair<String, List<Pair<Object, Object>>>("att", attT));
		attrs.add(new Triple<>("att", adom, "string"));

		HashMap<String, Integer> dom1 = new HashMap<String, Integer>();

		List<EExternal> queries = new LinkedList<>();

		int count = 0;
		Set<String> seen = new HashSet<>();
		HashMap<String, List<String>> cols = new HashMap<String, List<String>>();
		for (EExternal k0 : in) {
			if (k0 instanceof ECreateTable) {
				ECreateTable k = (ECreateTable) k0;
				if (seen.contains(k.name)) {
					throw new RuntimeException("Duplicate name: " + k.name);
				}
				if (k.name.equals("adom") || k.name.equals("att")) {
					throw new RuntimeException(
							"The names adom and att cannot be used.");
				}
				seen.add(k.name);
				nodes.add(k.name);
				inodes.add(new Pair<String, List<Pair<Object, Object>>>(k.name,
						new LinkedList<Pair<Object, Object>>()));
				List<String> lcols = new LinkedList<>();
				for (Pair<String, String> col : k.types) {
					lcols.add(col.first);
					if (seen.contains(col.first)) {
						throw new RuntimeException("Duplicate name: " + k.name);
					}
					seen.add(col.first);
					arrows.add(new Triple<>(k.name + "_" + col.first, k.name,
							adom));
					iarrows.add(new Pair<String, List<Pair<Object, Object>>>(
							k.name + "_" + col.first,
							new LinkedList<Pair<Object, Object>>()));
				}
				cols.put(k.name, lcols);
			}
			if (k0 instanceof EInsertValues) {
				EInsertValues k = (EInsertValues) k0;
				List<String> lcols = cols.get(k.target);
				for (List<String> tuple : k.values) {
					if (lcols.size() != tuple.size()) {
						throw new RuntimeException("Column size mismatch "
								+ tuple + " in " + k.target);
					}
					List<Pair<Object, Object>> node = lookup2(k.target, inodes);
					if (node == null) {
						throw new RuntimeException("Missing table " + k.target);
					}

					String id = "" + count++;
					node.add(new Pair<Object, Object>(id, id));

					for (int colNum = 0; colNum < tuple.size(); colNum++) {
						Integer xxx = dom1.get(tuple.get(colNum));
						if (xxx == null) {
							dom1.put(tuple.get(colNum), count);
							adomT.add(new Pair<Object, Object>(count, count));
							attT.add(new Pair<Object, Object>(count, "\""
									+ tuple.get(colNum) + "\""));
							xxx = count;
							count++;
						}

						List<Pair<Object, Object>> yyy = lookup2(k.target + "_"
								+ lcols.get(colNum), iarrows);

						yyy.add(new Pair<Object, Object>(id, xxx));
					}
				}
			}
			if (k0 instanceof EFlower || k0 instanceof EUnion) {
				queries.add(k0);
			}
		}

		SigExp.Const exp = new SigExp.Const(nodes, attrs, arrows, eqs);
		InstExp.Const inst = new InstExp.Const(inodes, iattrs, iarrows,
				new SigExp.Var("S"));

		int ctx = 0;
		String xxx = "";
		for (EExternal gh : queries) {
			if (gh instanceof EFlower) {
				EFlower fl = (EFlower) gh;
				String yyy = trans(exp, fl, "out" + ctx++ + "_");
				xxx += "\n\n" + yyy;
			} else if (gh instanceof EUnion){
				EUnion g = (EUnion) gh;
				int lc = ctx++;
				int rc = ctx++;
				int uc = ctx++;
				xxx += trans(exp, g.l, "out" + lc + "_");
				xxx += trans(exp, g.r, "out" + rc + "_");
				
				String ls = g.l.distinct ? "out" + lc + "_" + "relationalizeInstance" : "out" + lc + "_" + "selectInstance";
				String rs = g.r.distinct ? "out" + rc + "_" + "relationalizeInstance" : "out" + rc + "_" + "selectInstance";
								
				xxx += "\n\n" + "instance " + "out" + uc + "_" + "plusInstance = (" + ls + " + " + rs + ")";
				
				if (g.distinct) {
					xxx += "\n\n" + "instance " + "out" + uc + "_" + "unionInstance = relationalize " + "out" + uc + "_plusInstance";
				} 
				
			} else {
				throw new RuntimeException();
			}
		}

		// FQLProgram ret = new FQLProgram();
		// ret.sigs.put("S", exp);
		return "schema S = " + exp + "\n\ninstance I = " + inst + " : S" + xxx;
	}

	private static String trans(Const src, EFlower fl, String pre) {
		// SigExp src0 = new SigExp.Var("S");

		LinkedList<Pair<List<String>, List<String>>> eqs = new LinkedList<>();

		List<String> nodes1 = new LinkedList<>();
		List<String> nodes2 = new LinkedList<>();
		List<String> nodes3 = new LinkedList<>();
		nodes1.add("adom");
		nodes2.add("adom");
		nodes2.add("guid");
		nodes3.add("adom");

		List<Triple<String, String, String>> attrs = new LinkedList<>();
		attrs.add(new Triple<>("att", "adom", "string"));

		List<Triple<String, String, String>> edges1 = new LinkedList<>();
		List<Triple<String, String, String>> edges2 = new LinkedList<>();
		List<Triple<String, String, String>> edges3 = new LinkedList<>();

		List<Pair<String, String>> inodes1 = new LinkedList<>();
		List<Pair<String, String>> inodes2 = new LinkedList<>();
		List<Pair<String, String>> inodes3 = new LinkedList<>();
		inodes1.add(new Pair<>("adom", "adom"));
		inodes2.add(new Pair<>("adom", "adom"));
		inodes3.add(new Pair<>("adom", "adom"));

		List<Pair<String, String>> iattrs = new LinkedList<>();
		iattrs.add(new Pair<>("att", "att"));

		List<Pair<String, List<String>>> iedges1 = new LinkedList<>();
		List<Pair<String, List<String>>> iedges2 = new LinkedList<>();
		List<Pair<String, List<String>>> iedges3 = new LinkedList<>();

		for (String k : fl.from.keySet()) {
			String v = fl.from.get(k);
			inodes1.add(new Pair<>(k, v));
			nodes1.add(k);
			inodes2.add(new Pair<>(k, "guid"));
			for (Triple<String, String, String> arr : src.arrows) {
				if (arr.second.equals(v)) {
					List<String> l = new LinkedList<>();
					l.add(v);
					l.add(arr.first);
					edges1.add(new Triple<>(k + "_" + arr.first, k, "adom"));
					iedges1.add(new Pair<>(k + "_" + arr.first, l));
					edges2.add(new Triple<>(k + "_" + arr.first, "guid", "adom"));

					List<String> l0 = new LinkedList<>();
					l0.add("guid");
					l0.add(k + "_" + arr.first);
					iedges2.add(new Pair<>(k + "_" + arr.first, l0));
				}
			}
		}

		List<List<Triple<String, String, String>>> eqcs = merge(edges2, fl);

		// System.out.println("eqcs " + eqcs);
		// System.out.println("edges2 " + edges2);
		Iterator<Triple<String, String, String>> it = edges2.iterator();
		while (it.hasNext()) {
			Triple<String, String, String> k = it.next();
			for (List<Triple<String, String, String>> v : eqcs) {
				if (v.contains(k) && !v.get(0).equals(k)) {
					// System.out.println("hit: " + k + " and " + v);
					it.remove();
					continue;
				}
			}
		}
		// System.out.println("x edges2 " + edges2);

		for (Pair<String, List<String>> kk : iedges2) {
			// System.out.println("trying edge map " + kk);
			Triple<String, String, String> k = new Triple<>(kk.second.get(1),
					"guid", "adom");
			// System.out.println("k is " + k);
			for (List<Triple<String, String, String>> v : eqcs) {
				if (v.contains(k) && !v.get(0).equals(k)) {
					// System.out.println("HIT");
					List<String> xxx = new LinkedList<>();
					xxx.add("guid");
					xxx.add(v.get(0).first);
					kk.second = xxx;
					break;
				}
			}
		}

		nodes3.add("guid");
		inodes3.add(new Pair<>("guid", "guid"));
		// List<String> ll = new LinkedList<>();
		// ll.add("guid");
		// iedges3.add(new Pair<>("adom", ll));

		// System.out.println("eqcs " + eqcs);
		for (String k : fl.select.keySet()) {
			Pair<String, String> v = fl.select.get(k);
			edges3.add(new Triple<>(k, "guid", "adom"));
			Triple<String, String, String> t = new Triple<>(v.first + "_"
					+ fl.from.get(v.first) + "_" + v.second, "guid", "adom");
			// System.out.println("t " + t);
			for (List<Triple<String, String, String>> eqc : eqcs) {
				if (eqc.contains(t)) {
					List<String> li = new LinkedList<>();
					li.add("guid");
					li.add(eqc.get(0).first);
					iedges3.add(new Pair<>(k, li));
				}
			}
		}

		Const sig1 = new Const(nodes1, attrs, edges1, eqs);
		Const sig2 = new Const(nodes2, attrs, edges2, eqs);
		Const sig3 = new Const(nodes3, attrs, edges3, eqs);

		MapExp.Const map1 = new MapExp.Const(inodes1, iattrs, iedges1, sig1,
				new SigExp.Var("S"));
		MapExp.Const map2 = new MapExp.Const(inodes2, iattrs, iedges2, src,
				sig2);
		MapExp.Const map3 = new MapExp.Const(inodes3, iattrs, iedges3, sig3,
				sig2);

		// return new Triple<>(new Pair<>(sig1, map1), new Pair<>(sig2, map2),
		// new Pair<>(sig3, map3));

		String xxx = "";
		xxx += "\n\nschema " + pre + "fromSchema = " + sig1.toString();
		xxx += "\n\nmapping " + pre + "fromMapping = " + map1.toString()
				+ " : " + pre + "fromSchema -> S";
		xxx += "\n\ninstance " + pre + "fromInstance = delta " + pre
				+ "fromMapping I";

		xxx += "\n\nschema " + pre + "whereSchema = " + sig2.toString();
		xxx += "\n\nmapping " + pre + "whereMapping = " + map2.toString()
				+ " : " + pre + "fromSchema -> " + pre + "whereSchema";
		xxx += "\n\ninstance " + pre + "whereInstance = pi " + pre
				+ "whereMapping " + pre + "fromInstance";

		xxx += "\n\nschema " + pre + "selectSchema = " + sig3.toString();
		xxx += "\n\nmapping " + pre + "selectMapping = " + map3.toString()
				+ " : " + pre + "selectSchema -> " + pre + "whereSchema";
		xxx += "\n\ninstance " + pre + "selectInstance = delta " + pre
				+ "selectMapping " + pre + "whereInstance";

		if (fl.distinct) {
			xxx += "\n\ninstance " + pre
					+ "relationalizeInstance = relationalize " + pre
					+ "selectInstance";
		}
		return xxx;
	}

	private static List<List<Triple<String, String, String>>> merge(
			List<Triple<String, String, String>> edges2,
			// List<Pair<String, List<String>>> iedges2,
			EFlower ef) {

		List<List<Triple<String, String, String>>> eqcs = new LinkedList<>();
		for (Triple<String, String, String> k : edges2) {
			List<Triple<String, String, String>> l = new LinkedList<>();
			l.add(k);
			eqcs.add(l);
		}
		for (Pair<Pair<String, String>, Pair<String, String>> k : ef.where) {
			mergeEqc(eqcs, k.first, k.second, ef.from);
		}

		// System.out.println("xxx eqcs are " + eqcs);
		return eqcs;
	}

	private static void mergeEqc(
			List<List<Triple<String, String, String>>> eqcs,
			Pair<String, String> l, Pair<String, String> r,
			Map<String, String> from) {
		// System.out.println("merge eqc on " + eqcs);
		// System.out.println("l is " + l);
		// System.out.println("r is " + r);
		Triple<String, String, String> l0 = new Triple<>(l.first + "_"
				+ from.get(l.first) + "_" + l.second, "guid", "adom");
		Triple<String, String, String> r0 = new Triple<>(r.first + "_"
				+ from.get(r.first) + "_" + r.second, "guid", "adom");
		// System.out.println("l0 is " + l0);
		// System.out.println("r0 is " + r0);

		List<Triple<String, String, String>> lx = null, rx = null;
		lbl: for (List<Triple<String, String, String>> k : eqcs) {
			if (!k.contains(l0)) {
				continue;
			}
			for (List<Triple<String, String, String>> v : eqcs) {
				if (k.equals(v)) {
					continue;
				}
				if (v.contains(r0)) {
					// System.out.println("hit");
					lx = k;
					rx = v;
					break lbl;
				}
			}
		}
		if (lx == rx) {
			return;
		}
		eqcs.remove(rx);
		lx.addAll(rx);
	}

	public static Object maybeQuote(Object o) {
		if (o instanceof String) {
			String x = (String) o;
			if (x.startsWith("\"") && x.endsWith("\"")) {
				return o;
			}
			try {
				return Integer.parseInt(x);
			} catch (Exception ex) {
			}
			return "\"" + o.toString() + "\"";
		}
		return o;
	}

	private static List<Pair<Object, Object>> lookup2(String target,
			List<Pair<String, List<Pair<Object, Object>>>> inodes) {
		for (Pair<String, List<Pair<Object, Object>>> k : inodes) {
			if (k.first.equals(target)) {
				return k.second;
			}
		}
		return null;
		// throw new RuntimeException("Not found: " + target + " in " + inodes);
	}

	/*
	 * private static String lookup(String s, List<Pair<String, String>> fks) {
	 * for (Pair<String, String> k : fks) { if (k.first.equals(s)) { return
	 * k.second; } } return null; }
	 */

	public static class EInsertValues extends EExternal {
		String target;
		List<List<String>> values;

		public EInsertValues(String target, List<List<String>> values) {
			super();
			this.target = target;
			this.values = values;
		}

	}

	public abstract static class EExternal {
	}

	public static class EFlower extends EExternal {
		Map<String, Pair<String, String>> select;
		Map<String, String> from;
		List<Pair<Pair<String, String>, Pair<String, String>>> where;
		boolean distinct;

		public EFlower(Map<String, Pair<String, String>> select,
				Map<String, String> from,
				List<Pair<Pair<String, String>, Pair<String, String>>> where,
				boolean distinct) {
			this.select = select;
			this.from = from;
			this.where = where;
			this.distinct = distinct;
		}
	}

	public static class EUnion extends EExternal {
		boolean distinct;
		EFlower l, r;

		public EUnion(boolean distinct, EFlower l, EFlower r) {
			super();
			this.distinct = distinct;
			this.l = l;
			this.r = r;
		}
	}

	public static class ECreateTable extends EExternal {
		String name;
		List<Pair<String, String>> types;

		public ECreateTable(String name, List<Pair<String, String>> types) {
			super();
			this.name = name;
			this.types = types;
		}
	}

	static final Parser<Integer> NUMBER = Terminals.IntegerLiteral.PARSER
			.map(new org.codehaus.jparsec.functors.Map<String, Integer>() {
				public Integer map(String s) {
					return Integer.valueOf(s);
				}
			});

	static String[] ops = new String[] { ",", ".", ";", ":", "{", "}", "(",
			")", "=", "->", "+", "*", "^", "|" };

	// TODO keyword 1 vs keyword 2 highlight color

	static String[] res = new String[] { "VARCHAR", "INT", "SELECT", "FROM",
			"WHERE", "DISTINCT", "UNION", "ALL", "CREATE", "TABLE", "AS",
			"AND", "OR", "NOT", "INSERT", "INTO", "VALUES" };

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

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static final List<EExternal> program(String s) {
		List<EExternal> ret = new LinkedList<>();
		List<Tuple3> decls = (List<Tuple3>) program.parse(s);

		for (Tuple3 decl : decls) {
			if (decl.a.toString().equals("CREATE")) {
				ret.add(toECreateTable(decl));
			} else if (decl.toString().startsWith("((INSERT")) {
				ret.add(toEInsertValues(decl));
			} else if (decl.toString().startsWith("((SELECT")) {
				ret.add(toFlower(decl));
			} else if (decl.toString().contains("UNION")) {
				ret.add(toUnion(decl));
			} else {
				throw new RuntimeException(decl.toString());
			}
		}
		return ret;
	}

	@SuppressWarnings("rawtypes")
	private static ECreateTable toECreateTable(Object decl) {
		Tuple4 t = (Tuple4) decl;

		String name = t.c.toString();
		Tuple3 t0 = (Tuple3) t.d;
		List t1 = (List) t0.b;

		List<Pair<String, String>> types = new LinkedList<>();

		for (Object o : t1) {
			org.codehaus.jparsec.functors.Pair p = (org.codehaus.jparsec.functors.Pair) o;
			types.add(new Pair<>(p.a.toString(), p.b.toString()));
		}
		return new ECreateTable(name, types);
	}

	public static final Parser<?> program = program().from(TOKENIZER, IGNORED);

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static EInsertValues toEInsertValues(Object decl) {
		Tuple5 t = (Tuple5) decl;
		String target = t.b.toString();
		List<Tuple3> x = (List<Tuple3>) t.d;
		List<List<String>> values = new LinkedList<>();
		for (Tuple3 y : x) {
			List<String> l = (List<String>) y.b;
			values.add(l);
		}
		return new EInsertValues(target, values);
	}

	public static final Parser<?> flower() {
		Parser<?> tuple = Parsers.tuple(ident(), term("."), ident());

		Parser<?> from0 = Parsers.tuple(ident(), term("AS"), ident()).sepBy1(
				term(","));
		Parser<?> from = Parsers.tuple(term("FROM"), from0);

		Parser<?> where0 = Parsers.tuple(tuple, term("="), tuple).sepBy(
				term("AND"));
		Parser<?> where = Parsers.tuple(term("WHERE"), where0).optional();

		Parser<?> select0 = Parsers.tuple(tuple, term("AS"), ident()).sepBy1(
				term(","));
		Parser<?> select = Parsers.tuple(term("SELECT"), term("DISTINCT")
				.optional(), select0);

		return Parsers.tuple(select, from, where);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static EFlower toFlower(Object decl) {
		Map<String, Pair<String, String>> select = new HashMap<String, Pair<String, String>>();
		Map<String, String> from = new HashMap<String, String>();
		List<Pair<Pair<String, String>, Pair<String, String>>> where = new LinkedList<>();

		Tuple3 o = (Tuple3) decl;

		Tuple3 select0 = (Tuple3) o.a;
		org.codehaus.jparsec.functors.Pair from0 = (org.codehaus.jparsec.functors.Pair) o.b;
		org.codehaus.jparsec.functors.Pair where0 = (org.codehaus.jparsec.functors.Pair) o.c;

		boolean distinct;
		if (select0.b == null) {
			distinct = false;
		} else {
			distinct = true;
		}

		List<Tuple3> select1 = (List<Tuple3>) select0.c;
		for (Tuple3 k : select1) {
			Tuple3 a = (Tuple3) k.a;
			String b = k.c.toString();
			select.put(b, new Pair<>(a.a.toString(), a.c.toString()));
		}

		List<Tuple3> from1 = (List<Tuple3>) from0.b;
		for (Tuple3 k : from1) {
			from.put(k.c.toString(), k.a.toString());
		}

		if (where0 == null) {

		} else {
			List<Tuple3> where1 = (List<Tuple3>) where0.b;
			for (Tuple3 k : where1) {
				Tuple3 l = (Tuple3) k.a;
				Tuple3 r = (Tuple3) k.c;
				where.add(new Pair<>(
						new Pair<>(l.a.toString(), l.c.toString()), new Pair<>(
								r.a.toString(), r.c.toString())));
			}
		}

		return new EFlower(select, from, where, distinct);
	}

	@SuppressWarnings("rawtypes")
	public static final EUnion toUnion(Object o) {
		Tuple4 t = (Tuple4) o;
		return new EUnion(t.c == null, toFlower(t.a), toFlower(t.d));
	}

	public static final Parser<?> union() {
		Parser<?> p = flower().between(term("("), term(")"));
		return Parsers.tuple(p, term("UNION"), term("ALL").optional(), p);
	}

	public static final Parser<?> insertValues() {
		Parser<?> p = string().sepBy(term(","));
		return Parsers.tuple(Parsers.tuple(term("INSERT"), term("INTO")),
				ident(), term("VALUES"), Parsers.tuple(term("("), p, term(")"))
						.sepBy(term(",")), term(";"));
	}

	public static final Parser<?> createTable() {
		Parser<?> q2 = Parsers.tuple(ident(), term("INT"));
		Parser<?> q3 = Parsers.tuple(ident(), term("VARCHAR"),
				Terminals.IntegerLiteral.PARSER.between(term("("), term(")")));
		Parser<?> p = Parsers.or(q2, q3).sepBy1(term(","));

		return Parsers.tuple(term("CREATE"), term("TABLE"), ident(),
				Parsers.tuple(term("("), p, term(")")), term(";"));
	}

	public static final Parser<?> program() {
		return Parsers.or(createTable(), insertValues(), flower().followedBy(term(";")), union().followedBy(term(";")))
				.many();
	}

	private static Parser<?> string() {
		return Parsers.or(Terminals.StringLiteral.PARSER,
				Terminals.IntegerLiteral.PARSER, Terminals.Identifier.PARSER);
	}
}
