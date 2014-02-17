package fql.gui;

import java.awt.Dimension;
import java.awt.Event;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu.Separator;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;

import org.codehaus.jparsec.error.ParserException;
import org.fife.ui.rsyntaxtextarea.AbstractTokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.CodeTemplateManager;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.TokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.folding.CurlyFoldParser;
import org.fife.ui.rsyntaxtextarea.folding.FoldParserManager;
import org.fife.ui.rsyntaxtextarea.templates.CodeTemplate;
import org.fife.ui.rsyntaxtextarea.templates.StaticCodeTemplate;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.fife.ui.rtextarea.SearchContext;
import org.fife.ui.rtextarea.SearchEngine;

import fql.DEBUG;
import fql.FQLException;
import fql.LineException;
import fql.Pair;
import fql.Triple;
import fql.decl.Driver;
import fql.decl.Environment;
import fql.decl.FQLProgram;
import fql.decl.InstExp;
import fql.decl.InstanceEditor;
import fql.decl.MapExp;
import fql.decl.TransExp;
import fql.decl.Type;
import fql.examples.Example;
import fql.parse.FQLParser;
import fql.parse.PrettyPrinter;

/**
 * 
 * @author ryan
 * 
 *         The FQL code editor
 */
public class CodeEditor extends JPanel implements Runnable {

	final Integer id;

	Display display;

	private static final long serialVersionUID = 1L;

	public RSyntaxTextArea topArea;

	FQLTextPanel respArea = new FQLTextPanel("Compiler response", "");

	final JTextField searchField = new JTextField();
	final JButton nextButton = new JButton("Find Next");
	final JButton prevButton = new JButton("Find Previous");
	final JCheckBox matchCaseCB = new JCheckBox("Match Case");
	final JCheckBox replaceCB = new JCheckBox("Replace");
	final JCheckBox wholeCB = new JCheckBox("Whole Word");
	final JTextField replaceField = new JTextField();

	JFrame frame;

	public JPanel makeSearchDialog() {
		JPanel toolBar = new JPanel(new GridLayout(3, 4));
		toolBar.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

		// JPanel toolBar = new JPanel();
		toolBar.add(new JLabel("Search for:"));
		toolBar.add(searchField);
		// nextButton.setActionCommand("FindNext");
		nextButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				doFind(true);

			}
		});

		toolBar.add(nextButton);

		prevButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				doFind(false);

			}
		});
		toolBar.add(prevButton);
		searchField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				nextButton.doClick(0);
			}
		});

		// prevButton.setActionCommand("FindPrev");
		// prevButton.addActionListener(this);
		// toolBar.add(prevButton);

		// ret.add(toolBar);
		// toolBar = new JPanel();
		toolBar.add(new JLabel("Replace with:"));
		toolBar.add(replaceField);
		toolBar.add(replaceCB);

		toolBar.add(new JLabel());
		toolBar.add(new JLabel());
		toolBar.add(new JLabel());
		toolBar.add(wholeCB);
		toolBar.add(matchCaseCB);

		// ret.add(toolBar);

		return toolBar;
	}

	protected void doFind(boolean b) {
		// Create an object defining our search parameters.
		SearchContext context = new SearchContext();
		String text = searchField.getText();
		if (text.length() == 0) {
			return;
		}
		context.setSearchFor(text);
		context.setMatchCase(matchCaseCB.isSelected());
		// context.setRegularExpression(regexCB.isSelected());
		context.setSearchForward(b);
		context.setWholeWord(wholeCB.isSelected());

		if (replaceCB.isSelected()) {
			context.setReplaceWith(replaceField.getText());
			SearchEngine.replace(topArea, context);
		} else {
			SearchEngine.find(topArea, context);
		}
		// if (!found) {
		// JOptionPane.showMessageDialog(null, "Text not found");
		// }

	}

	// public void actionPerformed(ActionEvent e) {
	//
	// // "FindNext" => search forward, "FindPrev" => search backward
	// String command = e.getActionCommand();
	// boolean forward = "FindNext".equals(command);
	//
	// // Create an object defining our search parameters.
	// SearchContext context = new SearchContext();
	// String text = searchField.getText();
	// if (text.length() == 0) {
	// return;
	// }
	// context.setSearchFor(text);
	// context.setMatchCase(matchCaseCB.isSelected());
	// // context.setRegularExpression(regexCB.isSelected());
	// context.setSearchForward(forward);
	// context.setWholeWord(false);
	//
	// boolean found = SearchEngine.find(topArea, context);
	// if (!found) {
	// JOptionPane.showMessageDialog(this, "Text not found");
	// }
	//
	// }

	public void setText(String s) {
		topArea.setText(s);
		topArea.setCaretPosition(0);
		// topArea.set
	}

	public String getText() {
		return topArea.getText();
	}

	public void makeSearchVisible() {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {

				if (frame == null) {
					frame = new JFrame();
					frame.setContentPane(makeSearchDialog());
					frame.setTitle("Find and Replace");

					frame.pack();
					frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
					// frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
					frame.setLocationRelativeTo(null);

					ActionListener escListener = new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							frame.setVisible(false);
						}
					};

					frame.getRootPane().registerKeyboardAction(escListener,
							KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
							JComponent.WHEN_IN_FOCUSED_WINDOW);
					KeyStroke ctrlW = KeyStroke.getKeyStroke(KeyEvent.VK_W,
							InputEvent.CTRL_MASK);
					KeyStroke commandW = KeyStroke.getKeyStroke(KeyEvent.VK_W,
							InputEvent.META_MASK);
					frame.getRootPane().registerKeyboardAction(escListener,
							ctrlW, JComponent.WHEN_IN_FOCUSED_WINDOW);
					frame.getRootPane().registerKeyboardAction(escListener,
							commandW, JComponent.WHEN_IN_FOCUSED_WINDOW);
				}
				frame.setVisible(true);
			}

		});

	}
	
	public void vedit() {
		FQLProgram init = tryParse(topArea.getText());
		if (init == null) {
			respArea.setText(toDisplay);
			return;
		}
		if (init.lines.size() == 0) {
			return;
		}
		String which = null;
		int start = -1;
		int offs = topArea.getCaretPosition();
		int end = -1;
		int i = 0;
		int pos = 0;
		for (String k : init.lines.keySet()) {
			Integer v = init.lines.get(k);
			if (v < offs && v > start) {
				start = v;
				which = k;
				pos = i;
			}
			i++;
		}
		if (which == null) {
			throw new RuntimeException();
		}
		// System.out.println("which: " + which);

		int j = 0;
		for (String k : init.lines.keySet()) {
			if (j == pos + 1) {
				end = init.lines.get(k);
				break;
			}
			j++;
		}
		if (end == -1) {
			end = topArea.getText().length();
		}
		// System.out.println("end: " + end);

		InstExp ie = init.insts.get(which);
		if (ie == null || !(ie instanceof InstExp.Const)) {
			respArea.setText("Cannot visually edit "
					+ which
					+ ": only constant instances are visually editable.");
			return;
		}
		InstExp.Const iec = (InstExp.Const) ie;
		try {
			InstExp.Const n = new InstanceEditor(which, iec.sig
					.toSig(init), iec).show();
			if (n == null) {
				return;
			}
			String newText = "instance " + which + " = " + n.toString()
					+ " : " + n.sig + "\n\n";
			topArea.replaceRange(newText, start, end);
		} catch (FQLException fe) {
			fe.printStackTrace();
			respArea.setText(fe.getLocalizedMessage());
		}
	}

	public CodeEditor(Integer id, String content) {
		super(new GridLayout(1, 1));

		this.id = id;
		respArea.setWordWrap(true);

		AbstractTokenMakerFactory atmf = (AbstractTokenMakerFactory) TokenMakerFactory
				.getDefaultInstance();
		atmf.putMapping("text/fql", "fql.parse.FqlTokenMaker");
		FoldParserManager.get().addFoldParserMapping("text/fql",
				new CurlyFoldParser());

		// topArea.setAntiAliasingEnabled(true);
		RSyntaxTextArea.setTemplatesEnabled(true);

		CodeTemplateManager ctm = RSyntaxTextArea.getCodeTemplateManager();
		CodeTemplate ct = new StaticCodeTemplate("schema", "schema ",
				" = {\n\tnodes;\n\tattributes;\n\tarrows;\n\tequations;\n}");
		ctm.addTemplate(ct);

		ct = new StaticCodeTemplate("mapping", "mapping ",
				" = {\n\tnodes;\n\tattributes;\n\tarrows;\n} :  -> ");
		ctm.addTemplate(ct);

		ct = new StaticCodeTemplate("instance", "instance ",
				" = {\n\tnodes;\n\tattributes;\n\tarrows;\n} :  ");
		ctm.addTemplate(ct);

		ct = new StaticCodeTemplate("query", "query ", " = delta pi sigma");
		ctm.addTemplate(ct);

		ct = new StaticCodeTemplate("QUERY", "QUERY ",
				" = match {} src dst \"deta sigma forward\" ");
		ctm.addTemplate(ct);

		ct = new StaticCodeTemplate("transform", "transform ",
				" = {\n\tnodes;\n} :  -> ");
		ctm.addTemplate(ct);

		topArea = new RSyntaxTextArea();
		topArea.setSyntaxEditingStyle("text/fql");
		topArea.setText(content);
		topArea.setCaretPosition(0);
		topArea.setAutoscrolls(true);

		Separator s = new Separator();
		JMenuItem visualEdit = new JMenuItem("Visual Edit");
		visualEdit.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				vedit();
			}

		});

		InputMap inputMap = topArea.getInputMap();

		// KeyStroke key = KeyStroke.getKeyStroke(KeyEvent.VK_A,
		// Event.META_MASK);
		KeyStroke key2 = KeyStroke.getKeyStroke(KeyEvent.VK_A, Event.CTRL_MASK);

		// inputMap.put(key, DefaultEditorKit.beginLineAction);
		inputMap.put(key2, DefaultEditorKit.beginLineAction);

		KeyStroke key = KeyStroke.getKeyStroke(KeyEvent.VK_E, Event.META_MASK);
		key2 = KeyStroke.getKeyStroke(KeyEvent.VK_E, Event.CTRL_MASK);

		inputMap.put(key, DefaultEditorKit.endLineAction);
		inputMap.put(key2, DefaultEditorKit.endLineAction);

		Action alx = new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				topArea.redoLastAction();
			}
		};
		key = KeyStroke.getKeyStroke(KeyEvent.VK_Z, Event.SHIFT_MASK
				| Event.CTRL_MASK);
		inputMap.put(key, alx);
		key = KeyStroke.getKeyStroke(KeyEvent.VK_Z, Event.SHIFT_MASK
				| Event.META_MASK);
		inputMap.put(key, alx);

		key = KeyStroke.getKeyStroke(KeyEvent.VK_K, Event.META_MASK);
		key2 = KeyStroke.getKeyStroke(KeyEvent.VK_K, Event.CTRL_MASK);

		Action al = new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				int len = topArea.getLineEndOffsetOfCurrentLine();
				int offs = topArea.getCaretPosition();
				try {
					// System.out.println("xxx " + offs + " xxx " + len);
					// System.out.println(topArea.getDocument().getText(offs,
					// len - 1));
					if (len - offs - 1 > 0) {
						topArea.getDocument().remove(offs, len - offs - 1);
					}
				} catch (BadLocationException e1) {
					e1.printStackTrace();
				}
			}
		};
		topArea.getActionMap().put("RemoveToEndOfLine", al);
		inputMap.put(key, "RemoveToEndOfLine");
		inputMap.put(key2, "RemoveToEndOfLine");

		// topArea.setBracketMatchingEnabled(true);
		// topArea.setAutoIndentEnabled(true);
		// topArea.setAnimateBracketMatching(true);
		topArea.setCloseCurlyBraces(true);
		// topArea.setFadeCurrentLineHighlight(true);
		// topArea.setHighlightCurrentLine(true);

		// topArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
		topArea.setCodeFoldingEnabled(true);
		RTextScrollPane sp = new RTextScrollPane(topArea);
		sp.setFoldIndicatorEnabled(true);
		// add(sp);

		JSplitPane xx1 = new FQLSplit(.8, JSplitPane.VERTICAL_SPLIT);
		// topArea.setPreferredSize(new Dimension(600,400));
		xx1.add(sp);
		xx1.setDividerSize(6);
		xx1.setResizeWeight(.8);
		xx1.add(respArea);
		// respArea.setPreferredSize(new Dimension(200,400));

		add(xx1);

		topArea.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void insertUpdate(DocumentEvent e) {
				GUI.setDirty(CodeEditor.this.id, true);
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				GUI.setDirty(CodeEditor.this.id, true);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				GUI.setDirty(CodeEditor.this.id, true);
			}

		});

		topArea.getPopupMenu().add(visualEdit, 0);
		topArea.getPopupMenu().add(s, 1);
		// topArea.getPopupMenu().setLightWeightPopupEnabled(true);

		// topArea.getP
	}

	protected void findAction() {
		makeSearchVisible();
	}

	protected void doExample(Example e) {
		if (abortBecauseDirty()) {
			return;
		}
		String program = e.getText();
		topArea.setText(program);
		topArea.setCaretPosition(0);
		respArea.setText("");
		GUI.setDirty(CodeEditor.this.id, false);
		if (display != null) {
			display.close();
		}
		display = null;
	}

	@SuppressWarnings("deprecation")
	void abortAction() {
		if (thread == null) {
			return;
		}
		if (!thread.isAlive()) {
			thread = null;
			return;
		}
		try {
			Thread ttt = thread;
			thread = null;
			respArea.setText("Aborted");
			ttt.stop();
		} catch (Exception e) {
			respArea.setText(e.getLocalizedMessage());
		}
	}

	String toDisplay = null;
	Thread thread, temp;

	@SuppressWarnings("deprecation")
	public void runAction() {
		toDisplay = null;
		// DateFormat format = DateFormat.getTimeInstance();
		// String foo = format.format(new Date(System.currentTimeMillis()));

		// respArea.setText("Compilation and visualization started at " + foo);
		if (temp != null) {
			temp.stop();
		}
		temp = null;
		if (thread != null) {
			thread.stop();
		}
		thread = null;
		thread = new Thread(this);
		temp = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					// int counter = 0;
					respArea.setText("");
					for (;;) {
						Thread.sleep(250);
						if (toDisplay != null) {
							respArea.setText(toDisplay);
							toDisplay = null;
							return;
						} else if (thread != null) {
							respArea.setText(respArea.getText() + ".");
							// counter++;
							// if (counter == 80) {
							// counter = 0;
							// respArea.setText(respArea.getText() + "\n");
							// respArea.s
							// }
						}
					}
				} catch (InterruptedException ie) {
					ie.printStackTrace();
				} catch (Throwable tt) {
					tt.printStackTrace();
					respArea.setText(tt.getMessage());
				}
			}
		});
		temp.setPriority(Thread.MIN_PRIORITY);
		temp.start();
		thread.setPriority(Thread.MIN_PRIORITY);
		thread.start();
	}

	@SuppressWarnings("deprecation")
	public void run() {
		String program = topArea.getText();

		FQLProgram init;
		Environment env; // = Driver.intemp1;
		String env2;
		List<Throwable> exns;
		init = tryParse(program);
		if (init == null) {
			return;
		}

		try {
			// Driver.check(init);
			Triple<Environment, String, List<Throwable>> envX = Driver
					.makeEnv(init);
			env = envX.first;
			env2 = envX.second;
			exns = envX.third;
		} catch (LineException e) {
			toDisplay = "Error in " + e.kind + " " + e.decl + ": "
					+ e.getLocalizedMessage();
			e.printStackTrace();
			topArea.requestFocusInWindow();
			Integer theLine = init.lines.get(e.decl);
			topArea.setCaretPosition(theLine);
			if (thread != null) {
				thread.stop();
			}
			thread = null;
			if (temp != null) {
				temp.stop();
			}
			temp = null;
			return;
		} catch (Throwable re) {
			// System.out.println("xxxxxxx");
			toDisplay = re.getLocalizedMessage();
			respArea.setText(re.getLocalizedMessage());
			re.printStackTrace();
			if (thread != null) {
				thread.stop();
			}
			thread = null;
			if (temp != null) {
				temp.stop();
			}
			temp = null;
			return;
		}

		try {
			if (display != null && !DEBUG.debug.MultiView) {
				display.close();
			}
			DateFormat format = DateFormat.getTimeInstance();
			// try {
			display = new Display(init, env);
			String foo = GUI.getTitle(id);
			if (DEBUG.debug.MultiView) {
				foo += " - "
						+ format.format(new Date(System.currentTimeMillis()));
			}
			display.display(foo, init.order);

			// String psm = PSMGen.compile(env, init);
			// if (DEBUG.debug.continue_on_error) {
			if (exns.size() > 0) {
				// System.out.println("iiiiiiiiiii");
				toDisplay = "";
				for (Throwable ex : exns) {
					if (ex instanceof LineException) {
						toDisplay += "error on " + ((LineException) ex).kind
								+ " " + ((LineException) ex).decl + ": ";
					}
					toDisplay += ex.getLocalizedMessage();
					toDisplay += "\n\n---------------\n\n";
				}
				// }
			} else {
				toDisplay = env2;
			}
		} catch (Throwable ee) {
			toDisplay = ee.toString();
			ee.printStackTrace();
			if (thread != null) {
				thread.stop();
			}
			thread = null;
			if (temp != null) {
				temp.stop();
			}
			temp = null;
			return;
		}

		if (thread != null) {
			thread.stop();
		}
		thread = null;
		if (temp != null) {
			temp.stop();
		}
		temp = null;

	}
	
	
	public void format() {
		String input = topArea.getText();
		FQLProgram p = tryParse(input);
		if (p == null) {
			respArea.setText(toDisplay);
			return;
		}
		if (input.contains("//") || input.contains("/*")) {
			int x = JOptionPane.showConfirmDialog(null, "Formatting will erase all comments - continue?", "Continue?", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if (x != JOptionPane.YES_OPTION) {
				return;
			}
		}
		//order does not contain enums or drops
		StringBuffer sb = new StringBuffer();
		for (String k : p.enums.keySet()) {
			Type t = p.enums.get(k);
			if (!(t instanceof Type.Enum)) {
				continue;
			}
			Type.Enum e = (Type.Enum) t;
			sb.append("enum " + k + " = " + e.printFull());
			sb.append("\n\n");
		}
		for (String k : p.order) {
			Pair<String, Object> o = get(p, k);
			sb.append(o.first + " " + k + " = " + o.second.toString());
			if (o.second instanceof InstExp.Const) {
				InstExp.Const c = (InstExp.Const) o.second;
				sb.append(" : " + c.sig);
			} else if (o.second instanceof MapExp.Const) {
				MapExp.Const c = (MapExp.Const) o.second;
				sb.append(" : " + c.src + " -> " + c.dst);
			} else if (o.second instanceof TransExp.Const) {
				TransExp.Const c = (TransExp.Const) o.second;
				sb.append(" : " + c.src + " -> " + c.dst);
			}
			sb.append("\n\n");
		}
		if (p.drop.size() > 0) {
			sb.append("drop " + PrettyPrinter.sep0(" ", p.drop) + "\n\n");
		}
		topArea.setText(sb.toString().trim());
		topArea.setCaretPosition(0);
	}
	

	
	private Pair<String, Object> get(FQLProgram p, String k) {
		Object o = null;
		
		o = p.full_queries.get(k);
		if (o != null) {
			return new Pair<>("QUERY", o);
		}
		
		o = p.queries.get(k);
		if (o != null) {
			return new Pair<>("query", o);
		}
		
		o = p.insts.get(k);
		if (o != null) {
			return new Pair<>("instance", o);
		}
		
		o = p.maps.get(k);
		if (o != null) {
			return new Pair<>("mapping", o);
		}
		
		o = p.sigs.get(k);
		if (o != null) {
			return new Pair<>("schema", o);
		}
		
		o = p.transforms.get(k);
		if (o != null) {
			return new Pair<>("transform", o);
		}
		
		throw new RuntimeException("Cannot find " + k);
	}
	
	@SuppressWarnings("deprecation")
	private FQLProgram tryParse(String program) {
		try {
			return FQLParser.program(program);
		} catch (ParserException e) {
			int col = e.getLocation().column;
			int line = e.getLocation().line;
			topArea.requestFocusInWindow();
			// int startOffset = topArea.viewToModel(new Point(col, line));
			topArea.setCaretPosition(topArea.getDocument()
					.getDefaultRootElement().getElement(line - 1)
					.getStartOffset()
					+ (col - 1));
			// topArea.setCaretPosition(startOffset);
			// topArea.repaint();
			String s = e.getMessage();
			String t = s.substring(s.indexOf(" "));
			t.split("\\s+");

			toDisplay = "Syntax error: " + e.getLocalizedMessage();
			e.printStackTrace();
			thread = null;
			return null;
		} catch (Throwable e) {
			toDisplay = "Error: " + e.getLocalizedMessage();
			e.printStackTrace();
			if (thread != null) {
				thread.stop();
			}
			thread = null;
			if (temp != null) {
				temp.stop();
			}
			temp = null;
			return null;
		}
	}

	public boolean abortBecauseDirty() {
		try {
			if (!GUI.getDirty(id)) {
				return false;
			}
		} catch (NullPointerException npe) {
			npe.printStackTrace();
			return true;
		}

		int choice = JOptionPane.showConfirmDialog(null,
				"Unsaved Changes - Continue?", "Continue?",
				JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null);
		if (choice == JOptionPane.NO_OPTION) {
			return true;
		}
		return false;
	}

	public void check() {
		String program = topArea.getText();

		FQLProgram init;
		try {
			init = FQLParser.program(program);
		} catch (ParserException e) {
			int col = e.getLocation().column;
			int line = e.getLocation().line;
			topArea.requestFocusInWindow();
			topArea.setCaretPosition(topArea.getDocument()
					.getDefaultRootElement().getElement(line - 1)
					.getStartOffset()
					+ (col - 1));
			String s = e.getMessage();
			String t = s.substring(s.indexOf(" "));
			t.split("\\s+");

			respArea.setText("Syntax error: " + e.getLocalizedMessage());
			e.printStackTrace();
			return;
		} catch (RuntimeException e) {
			respArea.setText("Error: " + e.getLocalizedMessage());
			e.printStackTrace();
			return;
		}

		String xxx = Driver.checkReport(init);

		DateFormat format = DateFormat.getTimeInstance();
		String time = format.format(new Date(System.currentTimeMillis()));
		String foo = GUI.getTitle(id);

		JTextArea jta = new JTextArea(xxx);
		jta.setWrapStyleWord(true);
		jta.setLineWrap(true);
		JScrollPane p = new JScrollPane(jta,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		p.setPreferredSize(new Dimension(650, 300));

		JOptionPane pane = new JOptionPane(p);
		JDialog dialog = pane.createDialog(null, "Type Check " + foo + " - "
				+ time);
		dialog.setModal(false);
		dialog.setResizable(true);
		dialog.setVisible(true);

	}

}
