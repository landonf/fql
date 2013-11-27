package fql.sql;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
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
import org.codehaus.jparsec.functors.Map;
import org.codehaus.jparsec.functors.Tuple3;
import org.codehaus.jparsec.functors.Tuple4;
import org.codehaus.jparsec.functors.Tuple5;

import fql.Pair;
import fql.Triple;
import fql.decl.InstExp;
import fql.decl.SigExp;
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
	//	JLabel lbl = new JLabel("Suffix:", JLabel.RIGHT);
		// lbl.setToolTipText("FQL will translate table T to T_suffix, and generate SQL to load T into T_suffix");
		//final JTextField field = new JTextField(8);
	//	field.setText("fql");

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
		//tp.add(lbl);
	//	tp.add(field);
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
			+ "\n description VARCHAR(255)" + "\n);  " + "\n"
			+ "\nCREATE TABLE Person (" + "\n name VARCHAR(255), "
			+ "\n home VARCHAR(255)" + "\n);" + "\n"
			+ "\nINSERT INTO Place VALUES (\"New York\"),(\"Chicago\"),(\"New York\"); //bag semantics "
			+ "\nINSERT INTO Person VALUES (\"Alice\", \"Los Angeles\");";

	public static String transSQLSchema(List<EExternal> in) {
		// TODO wrong: every attribute becomes an edge to "dom"
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
					throw new RuntimeException("The names adom and att cannot be used.");
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
							attT.add(new Pair<Object, Object>(count, "\"" + tuple.get(colNum) + "\""));
							xxx = count;
							count++;
						}
						
						List<Pair<Object, Object>> yyy = lookup2(k.target + "_"
								+ lcols.get(colNum), iarrows);
						
						yyy.add(new Pair<Object, Object>(id, xxx));						
					}
				}
			}
		}

		SigExp.Const exp = new SigExp.Const(nodes, attrs, arrows, eqs);
		InstExp.Const inst = new InstExp.Const(inodes, iattrs, iarrows,
				new SigExp.Var("S"));

		// FQLProgram ret = new FQLProgram();
		// ret.sigs.put("S", exp);
		return "schema S = " + exp + "\n\ninstance I = " + inst + " : S";
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

/*	private static String lookup(String s, List<Pair<String, String>> fks) {
		for (Pair<String, String> k : fks) {
			if (k.first.equals(s)) {
				return k.second;
			}
		}
		return null;
	} */

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
			.map(new Map<String, Integer>() {
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
		Parser<?> p = Parsers.or(q2, q3).sepBy(term(","));

		return Parsers.tuple(term("CREATE"), term("TABLE"), ident(),
				Parsers.tuple(term("("), p, term(")")), term(";"));
	}

	public static final Parser<?> program() {
		return Parsers.or(createTable(), insertValues()).many();
	}

	private static Parser<?> string() {
		return Parsers.or(Terminals.StringLiteral.PARSER,
				Terminals.IntegerLiteral.PARSER, Terminals.Identifier.PARSER);
	}
}
