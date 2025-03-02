package fql.sql;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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

/**
 * 
 * @author ryan
 *
 *         Translates SPCU (in the guise of SQL) to FQL using an active domain
 *         construction.
 */
public class RaToFql {

	// always adom and guid
	public static String doAdom(SigExp.Const A, String a) {
		String k = a;
		List<Pair<List<String>, List<String>>> eeqs = new LinkedList<>();
		List<Triple<String, String, String>> attrs = new LinkedList<>();
		attrs.add(new Triple<>("att", "adom", "str"));
		List<Triple<String, String, String>> dd_attrs = new LinkedList<>();
		dd_attrs.add(new Triple<>("att", "x", "str"));

		List<Triple<String, String, String>> e_attrs = new LinkedList<>();

		List<String> bn = new LinkedList<>();
		bn.add("r");
		bn.add("d");
		List<Triple<String, String, String>> barrs = new LinkedList<>();
		List<Pair<String, String>> abn = new LinkedList<>();
		abn.add(new Pair<>("r", "guid"));
		abn.add(new Pair<>("d", "adom"));
		List<Pair<String, String>> abatt = new LinkedList<>();
		abatt.add(new Pair<>("att", "att"));
		List<Pair<String, String>> e_abatt = new LinkedList<>();

		List<Pair<String, List<String>>> abarr = new LinkedList<>();

		List<String> cn = new LinkedList<>();
		cn.add("r");
		cn.add("d");
		cn.add("m");

		List<Triple<String, String, String>> carrs = new LinkedList<>();
		carrs.add(new Triple<>("f", "m", "d"));

		List<Pair<String, String>> bcn = new LinkedList<>();
		bcn.add(new Pair<>("r", "r"));
		bcn.add(new Pair<>("d", "d"));

		List<Pair<String, List<String>>> bcarr = new LinkedList<>();

		List<Triple<String, String, String>> bbarrs = new LinkedList<>();
		bbarrs.add(new Triple<>("f", "a", "b"));
		bbarrs.add(new Triple<>("g", "a", "c"));
		bbarrs.add(new Triple<>("h", "b", "d"));
		bbarrs.add(new Triple<>("i", "c", "d"));
		List<String> bbn = new LinkedList<>();
		bbn.add("r");
		bbn.add("a");
		bbn.add("b");
		bbn.add("c");
		bbn.add("d");

		List<Triple<String, String, String>> ccarrs = new LinkedList<>();
		List<String> ccn = new LinkedList<>();
		List<Pair<List<String>, List<String>>> cceqs = new LinkedList<>();
		ccn.add("r");
		ccn.add("a");
		ccn.add("b");
		ccn.add("c");
		ccn.add("d");
		ccn.add("e");
		ccarrs.add(new Triple<>("f", "a", "b"));
		ccarrs.add(new Triple<>("g", "a", "c"));
		ccarrs.add(new Triple<>("h", "b", "d"));
		ccarrs.add(new Triple<>("i", "c", "d"));
		ccarrs.add(new Triple<>("ff", "e", "b"));
		ccarrs.add(new Triple<>("gg", "e", "c"));
		List<String> l1 = new LinkedList<>();
		l1.add("e");
		l1.add("ff");
		l1.add("h");
		List<String> l2 = new LinkedList<>();
		l2.add("e");
		l2.add("gg");
		l2.add("i");
		cceqs.add(new Pair<>(l1, l2));

		List<Triple<String, String, String>> ddarrs = new LinkedList<>();
		List<Pair<List<String>, List<String>>> ddeqs = new LinkedList<>();
		List<String> ddn = new LinkedList<>();
		ddn.add("r");
		ddn.add("v");
		ddn.add("w");
		ddn.add("x");
		ddn.add("y");
		ddarrs.add(new Triple<>("f", "v", "w"));
		ddarrs.add(new Triple<>("g", "w", "x"));
		ddarrs.add(new Triple<>("h", "x", "y"));
		ddarrs.add(new Triple<>("ff", "v", "w"));
		l1 = new LinkedList<>();
		l1.add("v");
		l1.add("f");
		l1.add("g");
		l2 = new LinkedList<>();
		l2.add("v");
		l2.add("ff");
		l2.add("g");
		ddeqs.add(new Pair<>(l1, l2));

		List<Pair<String, String>> ffn = new LinkedList<>();
		List<Pair<String, List<String>>> ffarr = new LinkedList<>();
		ffn.add(new Pair<>("r", "r"));
		ffn.add(new Pair<>("a", "m"));
		ffn.add(new Pair<>("b", "m"));
		ffn.add(new Pair<>("c", "m"));
		ffn.add(new Pair<>("d", "d"));
		l1 = new LinkedList<>();
		l1.add("m");
		ffarr.add(new Pair<>("f", l1));
		l1 = new LinkedList<>();
		l1.add("m");
		ffarr.add(new Pair<>("g", l1));
		l1 = new LinkedList<>();
		l1.add("m");
		l1.add("f");
		ffarr.add(new Pair<>("h", l1));
		l1 = new LinkedList<>();
		l1.add("m");
		l1.add("f");
		ffarr.add(new Pair<>("i", l1));

		List<Pair<String, String>> ggn = new LinkedList<>();
		ggn.add(new Pair<>("r", "r"));
		ggn.add(new Pair<>("a", "a"));
		ggn.add(new Pair<>("b", "b"));
		ggn.add(new Pair<>("c", "c"));
		ggn.add(new Pair<>("d", "d"));
		List<Pair<String, List<String>>> ggarr = new LinkedList<>();
		l1 = new LinkedList<>();
		l1.add("a");
		l1.add("f");
		ggarr.add(new Pair<>("f", l1));
		l1 = new LinkedList<>();
		l1.add("a");
		l1.add("g");
		ggarr.add(new Pair<>("g", l1));
		l1 = new LinkedList<>();
		l1.add("b");
		l1.add("h");
		ggarr.add(new Pair<>("h", l1));
		l1 = new LinkedList<>();
		l1.add("c");
		l1.add("i");
		ggarr.add(new Pair<>("i", l1));

		List<Pair<String, String>> hhn = new LinkedList<>();
		hhn.add(new Pair<>("r", "r"));
		hhn.add(new Pair<>("e", "v"));
		hhn.add(new Pair<>("a", "w"));
		hhn.add(new Pair<>("b", "w"));
		hhn.add(new Pair<>("c", "w"));
		hhn.add(new Pair<>("d", "y"));
		List<Pair<String, List<String>>> hharr = new LinkedList<>();
		l1 = new LinkedList<>();
		l1.add("w");
		hharr.add(new Pair<>("f", l1));
		l1 = new LinkedList<>();
		l1.add("w");
		hharr.add(new Pair<>("g", l1));
		l1 = new LinkedList<>();
		l1.add("w");
		l1.add("g");
		l1.add("h");
		hharr.add(new Pair<>("h", l1));
		l1 = new LinkedList<>();
		l1.add("w");
		l1.add("g");
		l1.add("h");
		hharr.add(new Pair<>("i", l1));
		l1 = new LinkedList<>();
		l1.add("v");
		l1.add("f");
		hharr.add(new Pair<>("ff", l1));
		l1 = new LinkedList<>();
		l1.add("v");
		l1.add("ff");
		hharr.add(new Pair<>("gg", l1));

		List<Pair<String, String>> iin = new LinkedList<>();
		iin.add(new Pair<>("guid", "r"));
		iin.add(new Pair<>("adom", "x"));
		List<Pair<String, List<String>>> iiarr = new LinkedList<>();

		int i = 0;
		for (Triple<String, String, String> m0 : A.arrows) {
			String m = m0.first;
			bn.add("m" + i);
			barrs.add(new Triple<>("i" + i, "r", "m" + i));
			barrs.add(new Triple<>("f" + i, "m" + i, "d"));

			abn.add(new Pair<>("m" + i, "guid"));
			List<String> l = new LinkedList<>();
			l.add("guid");
			abarr.add(new Pair<>("i" + i, l));
			l = new LinkedList<>();
			l.add("guid");
			l.add(m);
			abarr.add(new Pair<>("f" + i, l));

			carrs.add(new Triple<>("i" + i, "r", "m"));

			bcn.add(new Pair<>("m" + i, "m"));

			l = new LinkedList<>();
			l.add("r");
			l.add("i" + i);
			bcarr.add(new Pair<>("i" + i, l));

			l = new LinkedList<>();
			l.add("m");
			l.add("f");
			bcarr.add(new Pair<>("f" + i, l));

			bbarrs.add(new Triple<>("i" + i, "r", "a"));
			ccarrs.add(new Triple<>("i" + i, "r", "a"));
			ddarrs.add(new Triple<>("i" + i, "r", "w"));

			l = new LinkedList<>();
			l.add("r");
			l.add("i" + i);
			ffarr.add(new Pair<>("i" + i, l));

			l = new LinkedList<>();
			l.add("r");
			l.add("i" + i);
			ggarr.add(new Pair<>("i" + i, l));

			l = new LinkedList<>();
			l.add("r");
			l.add("i" + i);
			hharr.add(new Pair<>("i" + i, l));

			l = new LinkedList<>();
			l.add("r");
			l.add("i" + i);
			l.add("g");
			iiarr.add(new Pair<>(m, l));

			i++;
		}

		SigExp.Const b = new SigExp.Const(bn, e_attrs, barrs, eeqs);
		MapExp.Const ab = new MapExp.Const(abn, e_abatt, abarr, b, A); // F

		SigExp.Const c = new SigExp.Const(cn, e_attrs, carrs, eeqs);
		MapExp.Const bc = new MapExp.Const(bcn, e_abatt, bcarr, b, c); // G

		SigExp.Const bb = new SigExp.Const(bbn, e_attrs, bbarrs, eeqs);
		SigExp.Const cc = new SigExp.Const(ccn, e_attrs, ccarrs, cceqs);
		SigExp.Const dd = new SigExp.Const(ddn, dd_attrs, ddarrs, ddeqs);

		MapExp.Const ff = new MapExp.Const(ffn, e_abatt, ffarr, bb, c);
		MapExp.Const gg = new MapExp.Const(ggn, e_abatt, ggarr, bb, cc);
		MapExp.Const hh = new MapExp.Const(hhn, e_abatt, hharr, c, dd);
		MapExp.Const ii = new MapExp.Const(iin, abatt, iiarr, A, dd);

		String ret = "///////////////\n";
		ret += "schema " + k + "_B = " + b + "\n\n";
		ret += "mapping " + k + "_F = " + ab + " : " + k + "_B -> " + a + "\n\n";
		ret += "schema " + k + "_C = " + c + "\n\n";
		ret += "mapping " + k + "_G = " + bc + " : " + k + "_B -> " + k + "_C\n\n";

		ret += "schema " + k + "_BB = " + bb + "\n\n";
		ret += "schema " + k + "_CC = " + cc + "\n\n";
		ret += "schema " + k + "_DD = " + dd + "\n\n";

		ret += "mapping " + k + "_FF = " + ff + " : " + k + "_BB -> " + k + "_C\n\n";
		ret += "mapping " + k + "_GG = " + gg + " : " + k + "_BB -> " + k + "_CC\n\n";
		ret += "mapping " + k + "_HH = " + hh + " : " + k + "_CC -> " + k + "_DD\n\n";
		ret += "mapping " + k + "_II = " + ii + " : " + a + " -> " + k + "_DD\n\n";

		ret += "//////////////\n";
		return ret;
		// emit as "as_rel"
	}

	private static String doAdom2(String k, String in, String out) {
		String ret = "";
		ret += "\n\ninstance " + out + "_y = delta " + k + "_F " + in;
		ret += "\n\ninstance " + out + "_z = sigma " + k + "_G " + out + "_y";
		ret += "\n\ninstance " + out + "_w1= delta " + k + "_FF " + out + "_z";
		ret += "\n\ninstance " + out + "_w2= pi " + k + "_GG " + out + "_w1";
		ret += "\n\ninstance " + out + "_w3= SIGMA " + k + "_HH " + out + "_w2";
		ret += "\n\ninstance " + out + "_rel= delta " + k + "_II " + out + "_w3";
		/*
		 * instance Y3=delta F3 X3 instance Z3=sigma G3 Y3
		 * 
		 * instance W1=delta F Z3 instance W2=pi G W1 instance W3=SIGMA H W2
		 * instance RelationImage=delta I W3
		 */
		return ret;
	}

	protected Example[] examples = { new PeopleExample()  , new NegExample() , new EDExample() };

	String help = "Bags of tuples can be represented in FQL using an explicit active domain construction.  See the People example.  Unions of conjunctive queries *of base relations* are supported, using DISTINCT and ALL for set semantics.  (The translated FQL will not compile if not translating unions of conjunctive queries of base relations).  Primary and foreign keys are not supported by this encoding.  WHERE clauses must have equalities between variables, not constants.  SQL keywords MUST be capitalized.  The observables viewer pane is useful for visualizing instances.";

	protected String kind() {
		return "SPCU";
	}

	static class EDExample extends Example {

		@Override
		public String getName() {
			return "ED";
		}

		@Override
		public String getText() {
			return "CREATE TABLE P (" + "\n f VARCHAR(255)" + "\n);  " + "\n"
					+ "\nCREATE TABLE Q (" + "\n g VARCHAR(255), " + "\n h VARCHAR(255)" + "\n);"
					+ "\n" + "\nINSERT INTO P VALUES (\"a\"),(\"b\"),(\"c\");  "
					+ "\nINSERT INTO Q VALUES (\"a\", \"b\"),(\"x\",\"x\");" + "\n"
					+ "\nc1 = FORALL Q AS Q1 " + "\n     WHERE Q1.g = Q1.h"
					+ "\n     EXISTS P AS P1, P AS P2" + "\n     WHERE P1.f = Q1.g " + "\n";
		}

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

	static class NegExample extends Example {
		@Override
		public String getName() {
			return "Negation";
		}

		@Override
		public String getText() {
			return negText;
		}
	}

	String translate(String in) {
		List<Pair<String, EExternal>> list = program(in);
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
				if (box.getSelectedItem() != null) {
					input.setText(((Example) box.getSelectedItem()).getText());
				}
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
				JScrollPane p = new JScrollPane(jta, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
						JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
				p.setPreferredSize(new Dimension(300, 200));

				JOptionPane pane = new JOptionPane(p);
				// Configure via set methods
				JDialog dialog = pane.createDialog(null, "Help on SPCU to FQL");
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
			+ "\nINSERT INTO Person VALUES (\"Alice\", \"Chicago\");" + "\n"
			+ "\nq1 = SELECT DISTINCT x.description AS col0, z.name AS col1"
			+ "\n     FROM Place AS x, Place AS y, Person AS z "
			+ "\n     WHERE x.description = y.description AND x.description = z.home" + "\n"
			+ "\nq2 = SELECT x.description AS col0, z.name AS col1"
			+ "\n     FROM Place AS x, Place AS y, Person AS z "
			+ "\n     WHERE x.description = y.description AND x.description = z.home" + "\n"
			+ "\nq3 = q1 UNION q2" + "\n" + "\nq4 = q1 UNION ALL q2" + "\n";

	static String negText = "//our encoding of negation doesn't work correctly yet\n"
			+ "\nCREATE TABLE A (" + "\n a VARCHAR(255)" + "\n);  " + "\n"
			+ "\nINSERT INTO A VALUES (\"a\"),(\"a\"),(\"a\"); " + "\n" + "\nCREATE TABLE B ("
			+ "\n b VARCHAR(255)" + "\n);" + "\n" + "\nINSERT INTO B VALUES (\"b\"),(\"b\");"
			+ "\n" + "\na1 = SELECT DISTINCT x.a AS c FROM A AS x " + "\n"
			+ "\na3 = SELECT x.a AS c FROM A AS x" + "\n" + "\nb2 = SELECT x.b AS c FROM B AS x"
			+ "\n" + "\na3b2 = a3 UNION ALL b2" + "\n" + "\na2b2 = a3b2 EXCEPT a1" + "\n"
			+ "\na1b1 = a3 UNION b2" + "\n" + "\nb1 = a1b1 EXCEPT a1\n"
			+ "\n\n///////// the active domain has an effect on difference: "
			+ "\n/*enum str = {a,b}" + "\n" + "\nschema X = {" + "\n nodes" + "\n  adom,"
			+ "\n  guid;" + "\n attributes" + "\n  att: adom -> str;" + "\n arrows"
			+ "\n  c: guid -> adom;" + "\n equations;" + "\n}" + "\n" + "\ninstance F1 = {"
			+ "\n nodes" + "\n  adom -> {123, 124}, " + "\n  guid -> {125, 126};" + "\n attributes"
			+ "\n  att -> {(124, b), (123, a)};" + "\n arrows"
			+ "\n  c -> {(126, 123), (125, 124)};" + "\n} : X" + "\n" + "\n//encodes {a}"
			+ "\ninstance F2 = {" + "\n nodes" + "\n  adom -> {27}, " + "\n  guid -> {28};"
			+ "\n attributes" + "\n  att -> {(27, a)};" + "\n arrows" + "\n  c -> {(28, 27)};"
			+ "\n} : X" + "\n" + "\n//encodes {a}, but includes b in active domain"
			+ "\n//having b in active domain will kill b in the output of the difference" + "\n"
			+ "\n//instance F2 = {" + "\n// nodes" + "\n//  adom -> {26, 27}, "
			+ "\n//  guid -> {28};" + "\n// attributes" + "\n//  att -> {(26, b), (27, a)};"
			+ "\n// arrows" + "\n//  c -> {(28, 27)};" + "\n//} : X" + "\n"
			+ "\ninstance tprp = prop X" + "\ninstance tone = unit X" + "\n"
			+ "\ninstance prpprp = (tprp * tprp)" + "\n" + "\ntransform F1t = tone.unit F1"
			+ "\ntransform F2t = tone.unit F2" + "\n" + "\ntransform chiF1t = tprp.char F1t"
			+ "\ntransform chiF2t = tprp.char F2t"
			+ "\ntransform negchiF2t = (tprp.char F2t then tprp.not)" + "\n"
			+ "\ntransform t0 = prpprp.(chiF1t * negchiF2t)"
			+ "\ntransform t1 = (t0 then prpprp.and)" + "\n"
			+ "\ninstance F1minusF2 = kernel t1\n*/";

	public static String transSQLSchema(List<Pair<String, EExternal>> in) {
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
		attrs.add(new Triple<>("att", adom, "str"));
		Set<Object> enums = new HashSet<>();

		HashMap<String, Integer> dom1 = new HashMap<String, Integer>();

		List<Pair<String, EExternal>> queries = new LinkedList<>();

		int count = 0;
		Set<String> seen = new HashSet<>();
		HashMap<String, List<String>> cols = new HashMap<String, List<String>>();
		for (Pair<String, EExternal> kk0 : in) {
			EExternal k0 = kk0.second;
			// String key = kk0.first;
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
						throw new RuntimeException("Duplicate name: " + col.first);
					}
					seen.add(col.first);
					arrows.add(new Triple<>(k.name + "_" + col.first, k.name, adom));
					iarrows.add(new Pair<String, List<Pair<Object, Object>>>(k.name + "_"
							+ col.first, new LinkedList<Pair<Object, Object>>()));
				}
				cols.put(k.name, lcols);
			}
			if (k0 instanceof EInsertValues) {
				EInsertValues k = (EInsertValues) k0;
				List<String> lcols = cols.get(k.target);
				for (List<String> tuple : k.values) {
					if (lcols.size() != tuple.size()) {
						throw new RuntimeException("Column size mismatch " + tuple + " in "
								+ k.target);
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
							enums.add(tuple.get(colNum));
							adomT.add(new Pair<Object, Object>(count, count));
							attT.add(new Pair<Object, Object>(count, "\"" + tuple.get(colNum)
									+ "\""));
							xxx = count;
							count++;
						}

						List<Pair<Object, Object>> yyy = lookup2(
								k.target + "_" + lcols.get(colNum), iarrows);

						yyy.add(new Pair<Object, Object>(id, xxx));
					}
				}
			}
			if (k0 instanceof EFlower || k0 instanceof EUnion || k0 instanceof EDiff
					|| k0 instanceof EED) {
				queries.add(kk0);
			}
		}

		SigExp.Const exp = new SigExp.Const(nodes, attrs, arrows, eqs);
		InstExp.Const inst = new InstExp.Const(inodes, iattrs, iarrows, new SigExp.Var("S"));

		// int ctx = 0;
		String xxx = "\n\n";
		Map<String, String> schemas = new HashMap<>();
		Map<String, SigExp.Const> schemas0 = new HashMap<>();
		Map<String, Boolean> done = new HashMap<>();
		for (Pair<String, EExternal> gh0 : queries) {
			String k = gh0.first;
			EExternal gh = gh0.second;
			if (gh instanceof EFlower) {
				EFlower fl = (EFlower) gh;
				Pair<String, Const> yyy = trans(exp, fl, k);
				xxx += yyy.first + "\n\n";
				schemas.put(k, k + "Schema");
				schemas0.put(k, yyy.second);

			} else if (gh instanceof EUnion) {
				EUnion g = (EUnion) gh;
				String s1 = schemas.get(g.l);
				schemas.put(k, s1);
				schemas0.put(k, schemas0.get(g.l));

				xxx += longSlash + "\n/* Translation of " + k + "  */\n" + longSlash;

				if (g.distinct) {
					xxx += "\n\n" + "instance " + k + "_temp = (" + g.l + " + " + g.r + ")";
					xxx += "\n\n" + "instance " + k + " = relationalize " + k + "_temp";
				} else {
					xxx += "\n\n" + "instance " + k + " = (" + g.l + " + " + g.r + ")";
				}
				xxx += "\n\n";

			} else if (gh instanceof EDiff) {
				String f1x = ((EDiff) gh).l;
				String f2x = ((EDiff) gh).r;

				String s1 = schemas.get(f1x);
				// System.out.println("s1 is " + s1);
				// System.out.println("schemas0 " + schemas0);
				// System.out.println("scheas " + schemas);
				Const s1x = schemas0.get(f1x);
				schemas.put(k, s1);

				String f1y = doAdom(s1x, s1);
				// String f2y = doAdom(f2x, s1x);

				if (!done.containsKey(s1)) {
					xxx += "\n\n" + f1y;
					done.put(s1, true);
				}

				xxx += doAdom2(s1, f1x, k + "_l");
				xxx += doAdom2(s1, f2x, k + "_r");

				String f1 = k + "_l" + "_rel";
				String f2 = k + "_r" + "_rel";

				xxx += longSlash + "\n/* Translation of " + k + "  */\n" + longSlash;

				// Boolean done0 = done.get(s1);

				// xxx += "\n\n" + f2y;

				xxx += "\n\ninstance " + k + "prp = prop " + s1;
				xxx += "\n\ninstance " + k + "one = unit " + s1;
				xxx += "\n\ninstance " + k + "prp2 = (" + k + "prp * " + k + "prp)";
				xxx += "\n\ntransform " + k + f1 + "t = " + k + "one.unit " + f1;
				xxx += "\n\ntransform " + k + f2 + "t = " + k + "one.unit " + f2;
				xxx += "\n\ntransform " + k + f1 + "tchi = " + k + "prp.char " + k + f1 + "t";
				xxx += "\n\ntransform " + k + f2 + "tchi = " + k + "prp.char " + k + f2 + "t";
				xxx += "\n\ntransform " + k + "n = (" + k + f2 + "tchi then " + k + "prp.not)";
				xxx += "\n\ntransform " + k + "j1 = " + k + "prp2.(" + k + f1 + "tchi * " + k
						+ "n)";
				xxx += "\n\ntransform " + k + "j2 = (" + k + "j1 then " + k + "prp2.and)";

				if (((EDiff) gh).distinct) {
					xxx += "\n\ninstance " + k + "temp = kernel " + k + "j2";
					xxx += "\n\ninstance " + k + " = relationalize " + k + "temp";
				} else {
					xxx += "\n\ninstance " + k + " = kernel " + k + "j2";
				}
				xxx += "\n\n";
				

			} else if (gh instanceof EED) {
				EED c = (EED) gh;
				fql.decl.MapExp.Const f = doED(cols, c.from1, c.where1, exp);
				SigExp.Const src = (SigExp.Const) f.src;
				System.out.println(f.src);
				System.out.println(f.dst);
				System.out.println(f);

				c.from2.putAll(c.from1);
				c.where2.addAll(c.where1);
				fql.decl.MapExp.Const g = doED(cols, c.from2, c.where2, exp);
				System.out.println("===");
				System.out.println(g.src);
				System.out.println(g.dst);
				System.out.println(g);
				
				List<Pair<String, String>> l = new LinkedList<>();
				for (String x : src.nodes) {
					l.add(new Pair<>(x, x));
				}
				List<Pair<String, List<String>>> em = new LinkedList<>();
				for (Triple<String, String, String> e : src.arrows) {
					List<String> y = new LinkedList<>();
					y.add(e.second);
					y.add(e.first);
					em.add(new Pair<>(e.first, y));
				}
				
				fql.decl.MapExp.Const i = new MapExp.Const(l, new LinkedList<Pair<String,String>>(), em , f.src, g.src);
				System.out.println("^^^");
				System.out.println(i); 
				

				xxx += longSlash + "\n/* Translation of " + k + " */\n" + longSlash;
				 xxx += "\n\nschema " + k + "A = " + f.src;
				xxx += "\n\nschema " + k + "E = " + g.src;
				 xxx += "\n\nmapping " + k + "I = " + i + " : " + k + "A -> "
				 + k + "E";
				xxx += "\n\nmapping " + k + " = " + g + " : " + k + "E -> S";
				xxx += "\n\n"; 

			} else {
				throw new RuntimeException();
			}
		}

		// FQLProgram ret = new FQLProgram();
		// ret.sigs.put("S", exp);
		String enum0 = "";
		boolean b = false;
		for (Object o : enums) {
			if (b) {
				enum0 += ", ";
			}
			b = true;
			enum0 += "\"" + o + "\"";
		}
		String comment = "//schema S and instance I represent the entire input database.\n\n";
		String preS = "enum str = { " + enum0 + " }";
		return comment + preS + "\n\nschema S = " + exp + "\n\ninstance I = " + inst + " : S" + xxx;
	}

	/* private static String lookup(List<Pair<String, String>> l, String ret) {
		for (Pair<String, String> x : l) {
			if (x.first.equals(ret)) {
				return x.second;
			}
		}
		return ret;
	} */
	
	private static fql.decl.MapExp.Const doED(HashMap<String, List<String>> cols, Map<String, String> from,
			List<Pair<Pair<String, String>, Pair<String, String>>> where, SigExp.Const target 
			/*, List<Pair<String, String>> pres, Map<String, Triple<String,String,String>> arr  */) {

		Set<Triple<String, String, Map<String, String>>> s = new HashSet<>();
		Set<Pair<String, String>> e = new HashSet<>(); // can process some
	
		Map<String, String> origin = new HashMap<>();
		Map<String, String> origin2 = new HashMap<>();
		Map<String, String> origin3 = new HashMap<>();

		for (Entry<String, String> x : from.entrySet()) { // a AS b
			Map<String, String> map = new HashMap<>();
			if (!cols.containsKey(x.getValue())) {
				throw new RuntimeException("No table " + x.getValue() + " found in " + cols);
			}
			origin.put(x.getKey(), x.getValue());
			for (String col : cols.get(x.getValue())) {
				map.put(col, x.getKey() + "_" + col);
				origin.put(x.getKey() + "_" + col, "adom");
				origin3.put(x.getKey() + "_" + col, x.getValue());
				origin2.put(x.getKey() + "_" + col, col);
			}
			s.add(new Triple<>(x.getValue(), x.getKey(), map));
		}
		for (Pair<Pair<String, String>, Pair<String, String>> x : where) {
			e.add(new Pair<>(x.first.first + "_" + x.first.second, x.second.first + "_"
					+ x.second.second));
		}
		
		///////////////////////////////
		
		Set<Pair<String, Pair<String, String>>> sx = new HashSet<>();
		for (Triple<String, String, Map<String, String>> x : s) { // table name, fresh, map
			// sx.add(new Pair<>(x.first, new Pair<>(x.second, x.second)));
			for (Entry<String, String> y : x.third.entrySet()) {
				sx.add(new Pair<>(x.first, new Pair<>(x.second, y.getValue())));
				// sx.add(new Pair<>("att", new Pair<>(y.getValue(), y.getValue())));
			}
		}
		
		//////////////////////////////

		List<String> wn = new LinkedList<>();
		List<Triple<String, String, String>> wa = new LinkedList<>();
		List<Pair<List<String>, List<String>>> we = new LinkedList<>();
		List<Triple<String, String, String>> wat = new LinkedList<>();
		
		for (Pair<String, Pair<String, String>> x : sx) {
			if (!wn.contains(x.second.first)) {
				wn.add(x.second.first);
			}
			if (!wn.contains(x.second.second)) {
				wn.add(x.second.second);
			}
			Triple<String,String,String> t = new Triple<>(x.first + "_" + x.second.first + "_" + x.second.second,
					x.second.first, x.second.second);
			if (!wa.contains(t)) {
			//	arr.put(x.first + "_" + x.second.first + "_" + x.second.second, new Triple<>(x.first, x.second.first, x.second.second));
				wa.add(t);
			}
		}
		Set<Triple<String,String,String>> eqE = new HashSet<>();
		for (Pair<String, String> eq : e) {
			Triple<String,String,String> t = new Triple<>(eq.first + "_" + eq.second,
					eq.first, eq.second);
			if (!wa.contains(t)) {
				wa.add(t);
				eqE.add(t);
			}
		}
	
		SigExp.Const w = new SigExp.Const(wn, wat, wa, we);

		List<Pair<String, String>> omx = new LinkedList<>();
		for (String x : wn) {
			omx.add(new Pair<>(x, origin.get(x)));
		}
		List<Pair<String, List<String>>> emx = new LinkedList<>();

		for (Triple<String, String, String> x : wa) {
			String n = origin3.get(x.third);
			String m = origin2.get(x.third);
			List<String> l = new LinkedList<>();
			if (eqE.contains(x)) {
				l.add("adom");	
			} else {
				l.add(n);
				l.add(n + "_" + m);
			}
			if (!emx.contains(new Pair<>(x.first, l))) {
				emx.add(new Pair<>(x.first, l));
			}
		}

		MapExp.Const f = new MapExp.Const(omx, new LinkedList<Pair<String, String>>(), emx, w, target);

		return f;
		
	}

	private static Pair<String, Const> trans(Const src, EFlower fl, String pre) {
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
		attrs.add(new Triple<>("att", "adom", "str"));

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
			Triple<String, String, String> k = new Triple<>(kk.second.get(1), "guid", "adom");
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
			Triple<String, String, String> t = new Triple<>(v.first + "_" + fl.from.get(v.first)
					+ "_" + v.second, "guid", "adom");
			// System.out.println("t " + t);
			if (fl.from.get(v.first) == null) {
				throw new RuntimeException(v.first + " is not selectable in " + fl);
			}
			for (List<Triple<String, String, String>> eqc : eqcs) {
				// System.out.println("eqc " + eqc);
				if (eqc.contains(t)) {
					List<String> li = new LinkedList<>();
					li.add("guid");
					li.add(eqc.get(0).first);
					iedges3.add(new Pair<>(k, li));
					// System.out.println("added " + new Pair<>(k, li));
				} else {
					// System.out.println("not added");
				}
			}
		}

		Const sig1 = new Const(nodes1, attrs, edges1, eqs);
		Const sig2 = new Const(nodes2, attrs, edges2, eqs);
		Const sig3 = new Const(nodes3, attrs, edges3, eqs);

		MapExp.Const map1 = new MapExp.Const(inodes1, iattrs, iedges1, sig1, new SigExp.Var("S"));
		MapExp.Const map2 = new MapExp.Const(inodes2, iattrs, iedges2, src, sig2);
		MapExp.Const map3 = new MapExp.Const(inodes3, iattrs, iedges3, sig3, sig2);

		// return new Triple<>(new Pair<>(sig1, map1), new Pair<>(sig2, map2),
		// new Pair<>(sig3, map3));

		String xxx = "";
		xxx += "\n\nschema " + pre + "fromSchema = " + sig1.toString();
		xxx += "\n\nmapping " + pre + "fromMapping = " + map1.toString() + " : " + pre
				+ "fromSchema -> S";
		xxx += "\n\ninstance " + pre + "fromInstance = delta " + pre + "fromMapping I";

		xxx += "\n\nschema " + pre + "whereSchema = " + sig2.toString();
		xxx += "\n\nmapping " + pre + "whereMapping = " + map2.toString() + " : " + pre
				+ "fromSchema -> " + pre + "whereSchema";
		xxx += "\n\ninstance " + pre + "whereInstance = pi " + pre + "whereMapping " + pre
				+ "fromInstance";

		xxx += "\n\nschema " + pre + "Schema = " + sig3.toString();
		xxx += "\n\nmapping " + pre + "selectMapping = " + map3.toString() + " : " + pre
				+ "Schema -> " + pre + "whereSchema";

		if (!fl.distinct) {
			xxx += "\n\ninstance " + pre + " = delta " + pre + "selectMapping " + pre
					+ "whereInstance";
		} else {
			xxx += "\n\ninstance " + pre + "selectInstance = delta " + pre + "selectMapping " + pre
					+ "whereInstance";
			xxx += "\n\ninstance " + pre + " = relationalize " + pre + "selectInstance";
		}
		String comment = longSlash + "\n/* " + "Translation of " + pre + "  */\n" + longSlash;
		return new Pair<>(comment + xxx, sig3);
	}

	static String longSlash = "////////////////////////////////////////////////////////////////////////////////";

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

	private static void mergeEqc(List<List<Triple<String, String, String>>> eqcs,
			Pair<String, String> l, Pair<String, String> r, Map<String, String> from) {
		// System.out.println("merge eqc on " + eqcs);
		// System.out.println("l is " + l);
		// System.out.println("r is " + r);
		Triple<String, String, String> l0 = new Triple<>(l.first + "_" + from.get(l.first) + "_"
				+ l.second, "guid", "adom");
		Triple<String, String, String> r0 = new Triple<>(r.first + "_" + from.get(r.first) + "_"
				+ r.second, "guid", "adom");
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

	public static class EED extends EExternal {

		public static <T> Set<T> diff(final Set<? extends T> s1, final Set<? extends T> s2) {
			Set<T> symmetricDiff = new HashSet<T>(s1);
			symmetricDiff.addAll(s2);
			Set<T> tmp = new HashSet<T>(s1);
			tmp.retainAll(s2);
			symmetricDiff.removeAll(tmp);
			return symmetricDiff;
		}

		public EED(Map<String, String> from1, Map<String, String> from2,
				List<Pair<Pair<String, String>, Pair<String, String>>> where1,
				List<Pair<Pair<String, String>, Pair<String, String>>> where2) {
			super();
			this.from1 = from1;
			this.from2 = from2;
			this.where1 = where1;
			this.where2 = where2;
			if (diff(from1.keySet(), from2.keySet()).isEmpty()) {
				throw new RuntimeException("Non-disjoint AS clauses in " + this);
			}
		}

		Map<String, String> from1, from2;
		List<Pair<Pair<String, String>, Pair<String, String>>> where1, where2;

		public String toString() {
			String x = "FORALL ";
			boolean b = false;
			for (String k : from1.keySet()) {
				if (b) {
					x += ", ";
				}
				b = true;
				String p = from1.get(k);
				x += p + " AS " + k;
			}
			// if (where1.size() > 0) {
			x += "\nWHERE ";
			// }

			b = false;
			for (Pair<Pair<String, String>, Pair<String, String>> k : where1) {
				if (b) {
					x += " AND ";
				}
				b = true;
				x += k.first.first + "." + k.first.second + " = " + k.second.first + "."
						+ k.second.second;
			}

			b = false;
			x += "\nEXISTS ";
			for (String k : from2.keySet()) {
				if (b) {
					x += ", ";
				}
				b = true;
				String p = from2.get(k);
				x += p + " AS " + k;
			}
			// if (where1.size() > 0) {
			x += "\nWHERE ";
			// }

			b = false;
			for (Pair<Pair<String, String>, Pair<String, String>> k : where2) {
				if (b) {
					x += " AND ";
				}
				b = true;
				x += k.first.first + "." + k.first.second + " = " + k.second.first + "."
						+ k.second.second;
			}

			return x;
		}
	}

	public static class EFlower extends EExternal {
		Map<String, Pair<String, String>> select;
		Map<String, String> from;
		List<Pair<Pair<String, String>, Pair<String, String>>> where;
		boolean distinct;

		public EFlower(Map<String, Pair<String, String>> select, Map<String, String> from,
				List<Pair<Pair<String, String>, Pair<String, String>>> where, boolean distinct) {
			this.select = select;
			this.from = from;
			this.where = where;
			this.distinct = distinct;
		}

		@Override
		public String toString() {
			String x = "SELECT ";
			if (distinct) {
				x += "DISTINCT ";
			}
			boolean b = false;
			for (String k : select.keySet()) {
				if (b) {
					x += ", ";
				}
				b = true;
				Pair<String, String> p = select.get(k);
				x += p.first + "." + p.second + " AS " + k;
			}
			x += "\nFROM ";

			b = false;
			for (String k : from.keySet()) {
				if (b) {
					x += ", ";
				}
				b = true;
				String p = from.get(k);
				x += p + " AS " + k;
			}
			if (where.size() > 0) {
				x += "\nWHERE ";
			}

			b = false;
			for (Pair<Pair<String, String>, Pair<String, String>> k : where) {
				if (b) {
					x += " AND ";
				}
				b = true;
				x += k.first.first + "." + k.first.second + " = " + k.second.first + "."
						+ k.second.second;
			}

			return x;
		}
	}

	public static class EUnion extends EExternal {
		boolean distinct;
		String l, r;

		public EUnion(boolean distinct, String l, String r) {
			super();
			this.distinct = distinct;
			this.l = l;
			this.r = r;
		}

		@Override
		public String toString() {
			String x = "";
			if (!distinct) {
				x += " ALL";
			}
			return l.toString() + "\n" + "UNION" + x + "\n" + r.toString();
		}
	}

	public static class EDiff extends EExternal {
		boolean distinct;
		String l, r;

		public EDiff(boolean distinct, String l, String r) {
			super();
			this.distinct = distinct;
			this.l = l;
			this.r = r;
		}

		@Override
		public String toString() {
			String x = "";
			if (!distinct) {
				x += " ALL";
			}
			return l.toString() + "\n" + "EXCEPT" + x + "\n" + r.toString();
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

	static String[] ops = new String[] { ",", ".", ";", ":", "{", "}", "(", ")", "=", "->", "+",
			"*", "^", "|" };

	static String[] res = new String[] { "VARCHAR", "INT", "SELECT", "FROM", "WHERE", "DISTINCT",
			"UNION", "EXCEPT", "ALL", "CREATE", "TABLE", "AS", "AND", "OR", "NOT", "INSERT",
			"INTO", "VALUES", "FORALL", "EXISTS" };

	private static final Terminals RESERVED = Terminals.caseSensitive(ops, res);

	static final Parser<Void> IGNORED = Parsers.or(Scanners.JAVA_LINE_COMMENT,
			Scanners.JAVA_BLOCK_COMMENT, Scanners.WHITESPACES).skipMany();

	static final Parser<?> TOKENIZER = Parsers.or(
			(Parser<?>) Terminals.StringLiteral.DOUBLE_QUOTE_TOKENIZER, RESERVED.tokenizer(),
			(Parser<?>) Terminals.Identifier.TOKENIZER,
			(Parser<?>) Terminals.IntegerLiteral.TOKENIZER);

	static Parser<?> term(String... names) {
		return RESERVED.token(names);
	}

	public static Parser<?> ident() {
		return Terminals.Identifier.PARSER;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static final List<Pair<String, EExternal>> program(String s) {
		List<Pair<String, EExternal>> ret = new LinkedList<>();
		List<Tuple3> decls = (List<Tuple3>) program.parse(s);

		for (Tuple3 decl : decls) {
			if (decl.a.toString().equals("CREATE")) {
				ret.add(new Pair<String, EExternal>(null, toECreateTable(decl)));
			} else if (decl.toString().contains("INSERT")) {
				ret.add(new Pair<String, EExternal>(null, toEInsertValues(decl)));
			} else if (decl.toString().contains("SELECT")) {
				ret.add(new Pair<String, EExternal>(decl.a.toString(), toFlower(decl.c)));
			} else if (decl.toString().contains("UNION")) {
				ret.add(new Pair<String, EExternal>(decl.a.toString(), toUnion(decl.c)));
			} else if (decl.toString().contains("EXCEPT")) {
				ret.add(new Pair<String, EExternal>(decl.a.toString(), toDiff(decl.c)));
			} else if (decl.toString().contains("FORALL")) {
				ret.add(new Pair<String, EExternal>(decl.a.toString(), toEd(decl.c)));
			} else {
				throw new RuntimeException();
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

	public static final Parser<?> ed() {
		Parser<?> tuple = Parsers.tuple(ident(), term("."), ident());

		Parser<?> from0 = Parsers.tuple(ident(), term("AS"), ident()).sepBy1(term(","));
		Parser<?> from1 = Parsers.tuple(term("FORALL"), from0);
		Parser<?> from2 = Parsers.tuple(term("EXISTS"), from0);

		Parser<?> where0 = Parsers.tuple(tuple, term("="), tuple).sepBy(term("AND"));
		Parser<?> where = Parsers.tuple(term("WHERE"), where0).optional();

		return Parsers.tuple(from1, where, from2, where);
	}

	public static final Parser<?> flower() {
		Parser<?> tuple = Parsers.tuple(ident(), term("."), ident());

		Parser<?> from0 = Parsers.tuple(ident(), term("AS"), ident()).sepBy(term(","));
		Parser<?> from = Parsers.tuple(term("FROM"), from0);

		Parser<?> where0 = Parsers.tuple(tuple, term("="), tuple).sepBy(term("AND"));
		Parser<?> where = Parsers.tuple(term("WHERE"), where0); // TODO
																// .optional();

		Parser<?> select0 = Parsers.tuple(tuple, term("AS"), ident()).sepBy1(term(","));
		Parser<?> select = Parsers.tuple(term("SELECT"), term("DISTINCT").optional(), select0);

		return Parsers.tuple(select, from, where);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static EED toEd(Object decl) {
		Map<String, String> from1 = new LinkedHashMap<String, String>();
		Map<String, String> from2 = new LinkedHashMap<String, String>();
		List<Pair<Pair<String, String>, Pair<String, String>>> where1 = new LinkedList<>();
		List<Pair<Pair<String, String>, Pair<String, String>>> where2 = new LinkedList<>();

		Tuple4 o = (Tuple4) decl;

		org.codehaus.jparsec.functors.Pair from10 = (org.codehaus.jparsec.functors.Pair) o.a;
		org.codehaus.jparsec.functors.Pair where10 = (org.codehaus.jparsec.functors.Pair) o.b;
		org.codehaus.jparsec.functors.Pair from11 = (org.codehaus.jparsec.functors.Pair) o.c;
		org.codehaus.jparsec.functors.Pair where11 = (org.codehaus.jparsec.functors.Pair) o.d;

		List<Tuple3> from10x = (List<Tuple3>) from10.b;
		for (Tuple3 k : from10x) {
			from1.put(k.c.toString(), k.a.toString());
		}

		// if (where0 == null) {
		//
		// } else {
		List<Tuple3> where10x = (List<Tuple3>) where10.b;
		for (Tuple3 k : where10x) {
			Tuple3 l = (Tuple3) k.a;
			Tuple3 r = (Tuple3) k.c;
			where1.add(new Pair<>(new Pair<>(l.a.toString(), l.c.toString()), new Pair<>(r.a
					.toString(), r.c.toString())));
		}
		// }

		List<Tuple3> from11x = (List<Tuple3>) from11.b;
		for (Tuple3 k : from11x) {
			from2.put(k.c.toString(), k.a.toString());
		}

		// if (where0 == null) {
		//
		// } else {
		List<Tuple3> where11x = (List<Tuple3>) where11.b;
		for (Tuple3 k : where11x) {
			Tuple3 l = (Tuple3) k.a;
			Tuple3 r = (Tuple3) k.c;
			where2.add(new Pair<>(new Pair<>(l.a.toString(), l.c.toString()), new Pair<>(r.a
					.toString(), r.c.toString())));
		}
		// }

		return new EED(from1, from2, where1, where2);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static EFlower toFlower(Object decl) {
		Map<String, Pair<String, String>> select = new LinkedHashMap<String, Pair<String, String>>();
		Map<String, String> from = new LinkedHashMap<String, String>();
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
				where.add(new Pair<>(new Pair<>(l.a.toString(), l.c.toString()), new Pair<>(r.a
						.toString(), r.c.toString())));
			}
		}

		return new EFlower(select, from, where, distinct);
	}

	@SuppressWarnings("rawtypes")
	public static final EUnion toUnion(Object o) {
		Tuple4 t = (Tuple4) o;
		return new EUnion(t.c == null, t.a.toString(), t.d.toString());
	}

	@SuppressWarnings("rawtypes")
	public static final EDiff toDiff(Object o) {
		Tuple4 t = (Tuple4) o;
		return new EDiff(t.c == null, t.a.toString(), t.d.toString());
	}

	public static final Parser<?> union() {
		// Parser<?> p = flower().between(term("("), term(")"));
		return Parsers.tuple(ident(), term("UNION"), term("ALL").optional(), ident());
	}

	public static final Parser<?> diff() {
		// Parser<?> p = flower().between(term("("), term(")"));
		return Parsers.tuple(ident(), term("EXCEPT"), term("ALL").optional(), ident());
	}

	public static final Parser<?> insertValues() {
		Parser<?> p = string().sepBy(term(","));
		return Parsers.tuple(Parsers.tuple(term("INSERT"), term("INTO")), ident(), term("VALUES"),
				Parsers.tuple(term("("), p, term(")")).sepBy(term(",")), term(";"));
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
		Parser<?> p1 = Parsers.tuple(ident(), term("="), flower());
		Parser<?> p2 = Parsers.tuple(ident(), term("="), union());
		Parser<?> p3 = Parsers.tuple(ident(), term("="), diff());
		Parser<?> p4 = Parsers.tuple(ident(), term("="), ed());

		return Parsers.or(createTable(), insertValues(), p1, p2, p3, p4).many();
	}

	private static Parser<?> string() {
		return Parsers.or(Terminals.StringLiteral.PARSER, Terminals.IntegerLiteral.PARSER,
				Terminals.Identifier.PARSER);
	}
}
