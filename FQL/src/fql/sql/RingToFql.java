package fql.sql;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;

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

import fql.Pair;
import fql.examples.Example;
import fql.gui.FQLTextPanel;
import fql.parse.FqlTokenizer;
import fql.parse.KeywordParser;
import fql.parse.ParserUtils;
import fql.parse.Partial;
import fql.parse.PrettyPrinter;
import fql.parse.RyanParser;
import fql.parse.StringParser;

/**
 * 
 * @author ryan
 * 
 *         Translates SQL (in categorical normal form) to FQL.
 */
public class RingToFql {

	protected Example[] examples = { new PeopleExample() };

	String help = "Polynomials written in fully explicit form (i.e., containing only additions of (\"1\" or multiplications of variables), can be treated as FQL queries.";
	

	// protected String kind() {
	// return "Polynomial";
	// }

	static class PeopleExample extends Example {
		@Override
		public String getName() {
			return "Example 1";
		}

		@Override
		public String getText() {
			return extext1;
		}
	}

	String translate(String in) {
		try {
			List<Pair<String, List<List<String>>>> list = program(in);
			return translate2(list) + "/* output will have\n" + in.trim() + "\n*/";
		} catch (Exception e) {
			e.printStackTrace();
			return e.getMessage();
		}
	}

	public RingToFql() {
		final FQLTextPanel input = new FQLTextPanel("Input Polynomials", "");
		final FQLTextPanel output = new FQLTextPanel("FQL Output", "");

		// JButton jdbcButton = new JButton("Load using JDBC");
		// JButton runButton = new JButton("Run " + kind());
		JButton transButton = new JButton("Translate");
		JButton helpButton = new JButton("Help");
		// JButton runButton2 = new JButton("Run FQL");
		// JCheckBox jdbcBox = new JCheckBox("Run using JDBC");
		// JLabel lbl = new JLabel("Suffix (optional):", JLabel.RIGHT);
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
				JDialog dialog = pane.createDialog(null,
						"Help on Polynomials to FQL");
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
		JFrame f = new JFrame("Polynomials to FQL");
		f.setContentPane(p);
		f.pack();
		f.setSize(new Dimension(700, 600));
		f.setLocationRelativeTo(null);
		f.setVisible(true);
	}

	static String extext1 = "p = x*x + y*y*y + y*y*y \nq = x + x*y + x*y + x*y\nr = 1 + 1 + 1 + y\n";

	
	public static String translate2(List<Pair<String, List<List<String>>>> in) {
		int fresh = 0;
		
		List<String> vars = new LinkedList<>();
		List<String> polynomials = new LinkedList<>();
		List<String> monomials = new LinkedList<>();
		List<String> occurences = new LinkedList<>();
		
		List<Pair<String, String>> delta = new LinkedList<>();
		List<Pair<String, String>> sigma = new LinkedList<>();
		List<Pair<String, String>> pi = new LinkedList<>();

		for (Pair<String, List<List<String>>> k : in) {
			String p = k.first;
			if (polynomials.contains(p)) {
				throw new RuntimeException("Duplicate polynomial: " + p);
			}
			polynomials.add(p);
			for (List<String> l : k.second) {
				String m = "m" + fresh++;
				monomials.add(m);
				sigma.add(new Pair<>(m, p));
				for (String r : l) {
					if (r.equals("1")) {
						continue;
					}
					assertVar(r);
					String o = "o" + fresh++;
					occurences.add(o);
					pi.add(new Pair<>(o, m));
					delta.add(new Pair<>(o, r));
					if (!vars.contains(r)) {
						vars.add(r);
					}
				}
			}
		}
		
		String occ = "schema occurances = {nodes "
				+ PrettyPrinter.sep0(",", occurences)
				+ "; attributes; arrows; equations;}\n";
		
		String mon = "schema monomials = {nodes "
				+ PrettyPrinter.sep0(",", monomials)
				+ "; attributes; arrows; equations;}\n";
		
		String src = "schema variables = {nodes "
				+ PrettyPrinter.sep0(",", vars)
				+ "; attributes; arrows; equations;}\n";

		String dst = "schema polynomials = {nodes " + PrettyPrinter.sep0(",", polynomials)
				+ "; attributes; arrows; equations;}\n";

		String d = "mapping load = {nodes "
				+ sepEdge(delta)
				+ "; attributes; arrows;} : occurances -> variables \n";
		
		String p = "mapping multiply = {nodes "
				+ sepEdge(pi)
				+ "; attributes; arrows;} : occurances -> monomials \n";
		
		String s = "mapping add = {nodes "
				+ sepEdge(sigma)
				+ "; attributes; arrows;} : monomials -> polynomials \n";

		String q = "query q = delta load pi multiply sigma add\n";
		
		String i0 = "//to set variable v := n, put n IDs into node v\n";
		String i = "instance input = {nodes " + sepSpecial(vars) + "; attributes; arrows; } : variables\n"; 
		
		String r = "instance output = eval q input\n"; 
		
		
		return src + "\n" + occ + "\n" + mon + "\n" + dst + "\n" + d + "\n" + p + "\n" + s + "\n" + q + "\n" + i0 + i + "\n" + r;

	}

	private static String sepSpecial(List<String> l ) {
		String ret = "";
		boolean first = true;
		for (String k : l) {
			if (!first) {
				ret += ", ";
			}
			first = false;

			ret += k + " -> { }";
		}
		
		return ret;
	}

	private static String sepEdge(List<Pair<String, String>> delta) {
		String ret = "";
		boolean first = true;
		for (Pair<String, String> k : delta) {
			if (!first) {
				ret += ", ";
			}
			first = false;

			ret += k.first + " -> " + k.second;
		}
		
		return ret;
	}

	static void assertVar(String o) {
		try {
			int z = Integer.parseInt(o);
			throw new RuntimeException("Encountered non-1 numeral: " + z);
		} catch (NumberFormatException e) {
		}
	}
	/*
	public static String translate(List<Pair<String, List<List<String>>>> in) {
		List<String> polys = polys(in);

		// Map<String, List<String>> varsByPoly = new HashMap<>();
		Set<String> vars = new HashSet<>();

		for (Pair<String, List<List<String>>> k : in) {
			vars.addAll(vars(k.second));
		}

		String src = "schema src = {nodes "
				+ PrettyPrinter.sep0(",", new LinkedList<>(vars))
				+ "; attributes; arrows; equations;}\n";

		String dst = "schema dst = {nodes " + PrettyPrinter.sep0(",", polys)
				+ "; attributes; arrows; equations;}\n";

		String x = "";
		String y = "";

		return src + "\n" + x + "\n" + y + "\n" + dst;
	}

	private static Set<String> vars(List<List<String>> l) {
		Set<String> ret = new HashSet<>();

		for (List<String> x : l) {
			for (String o : x) {
				if (o.equals("1")) {
					continue;
				}
				try {
					int z = Integer.parseInt(o);
					throw new RuntimeException("Encountered non-1 numeral: " + z);
				} catch (NumberFormatException e) {
					ret.add(o);
				}
			}
		}

		return ret;
	}

	private static List<String> polys(List<Pair<String, List<List<String>>>> in) {
		List<String> ret = new LinkedList<>();

		for (Pair<String, List<List<String>>> k : in) {
			if (ret.contains(k.first)) {
				throw new RuntimeException("Duplicate polynomial: " + k.first);
			}
			ret.add(k.first);
		}

		return ret;
	}
*/
	RyanParser<List<List<String>>> tparser = ParserUtils.manySep(
			ParserUtils.manySep(new StringParser(), new KeywordParser("*")),
			new KeywordParser("+"));
	RyanParser<List<Pair<String, List<List<String>>>>> parser = ParserUtils
			.many(ParserUtils.inside(new StringParser(),
					new KeywordParser("="), tparser));

	public final List<Pair<String, List<List<String>>>> program(String s)
			throws Exception {
		Partial<List<Pair<String, List<List<String>>>>> k = parser
				.parse(new FqlTokenizer(s));

		if (k.tokens.toString().trim().length() > 0) {
			throw new RuntimeException("Unconsumed input: " + k.tokens);
		}

		return k.value;
	}

}
