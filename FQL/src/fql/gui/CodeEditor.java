package fql.gui;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.sql.Date;
import java.text.DateFormat;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

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
import fql.DEBUG.Intermediate;
import fql.FQLException;
import fql.decl.Environment;
import fql.decl.Program;
import fql.examples.Example;
import fql.parse.FqlParser;
import fql.sql.PSMGen;

/**
 * 
 * @author ryan
 * 
 *         The FQL code editor
 */
public class CodeEditor extends JPanel {

	final Integer id;

	Display display;

	private static final long serialVersionUID = 1L;

	RSyntaxTextArea topArea = new RSyntaxTextArea();

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

	public CodeEditor(Integer id, String content) {
		super(new GridLayout(1, 1));

		this.id = id;
		respArea.setWordWrap(true);

		// Border b =
		// BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK),
		// "FQL Program");

		// setBorder(b);

		// JPanel cp = new JPanel(new BorderLayout());

		topArea = new RSyntaxTextArea();
		// topArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_NONE);

		AbstractTokenMakerFactory atmf = (AbstractTokenMakerFactory) TokenMakerFactory
				.getDefaultInstance();
		atmf.putMapping("text/fql", "fql.parse.FqlTokenMaker");
		FoldParserManager.get().addFoldParserMapping("text/fql",
				new CurlyFoldParser());
		topArea.setSyntaxEditingStyle("text/fql");
		topArea.setText(content);
		topArea.setCaretPosition(0);
		topArea.setAutoscrolls(true);
		RSyntaxTextArea.setTemplatesEnabled(true);
		
		   CodeTemplateManager ctm = RSyntaxTextArea.getCodeTemplateManager();
		      CodeTemplate ct = new StaticCodeTemplate("schema", "schema ",
		    		  " = {\n\tnodes;\n\tattributes;\n\tarrows;\n\tequations;\n}");
		      ctm.addTemplate(ct);
		      
			       ct = new StaticCodeTemplate("mapping", "mapping ",
			    		  " :  ->  = {\n\tnodes;\n\tattributes;\n\tarrows;\n}");
			      ctm.addTemplate(ct);
			      
			      ct = new StaticCodeTemplate("instance", "instance ",
			    		  " :  = {\n\tnodes;\n\tattributes;\n\tarrows;\n}");
			      ctm.addTemplate(ct);
			      
			      ct = new StaticCodeTemplate("query", "query ",
			    		  " :  ->  = delta pi sigma");
			      ctm.addTemplate(ct);
		      
			
		      
		//topArea.setBracketMatchingEnabled(true);
	//	topArea.setAutoIndentEnabled(true);
	//	topArea.setAnimateBracketMatching(true);
		topArea.setCloseCurlyBraces(true);
		//topArea.setFadeCurrentLineHighlight(true);
	//	topArea.setHighlightCurrentLine(true);


		// topArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
		// topArea.setSyn

		// RSyntaxDocument.
		// SyntaxScheme scheme = topArea.s();

		// topArea.setS
		topArea.setCodeFoldingEnabled(true);
//		topArea.setAntiAliasingEnabled(true);
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

	}

	protected void findAction() {

		makeSearchVisible();

	}

	// public static void main(String[] args) {
	// // Start all Swing applications on the EDT.
	// SwingUtilities.invokeLater(new Runnable() {
	// public void run() {
	//
	// setContentPane(cp);
	// setTitle("Text Editor Demo");
	// setDefaultCloseOperation(EXIT_ON_CLOSE);
	// pack();
	// setLocationRelativeTo(null);
	// new Test("foo", "bar").setVisible(true);
	// }
	// });
	// }

	// static String program = Examples.INIT_EXAMPLE.getText();

	protected void doExample(Example e) {
		if (abortBecauseDirty()) {
			return;
		}
		String program = e.getText();
		topArea.setText(program);
		topArea.setCaretPosition(0);
		// bottomArea.setText(Examples.employeesCommands);
		respArea.setText("");
		GUI.setDirty(CodeEditor.this.id, false);
		if (display != null) {
			display.close();
		}
		display = null;
	}

	void runAction() {
		String program = topArea.getText();
		// String view = bottomArea.getText();

		try {
			Program parsed_program;
			try {
				parsed_program = FqlParser.program(program);
			} catch (ParserException e) {
				int col = e.getLocation().column;
				int line = e.getLocation().line;
				topArea.requestFocusInWindow();
				//int startOffset = topArea.viewToModel(new Point(col, line));
				topArea.setCaretPosition(topArea.getDocument().getDefaultRootElement().getElement(line-1).getStartOffset() + (col-1) );  
				//				topArea.setCaretPosition(startOffset);
				//topArea.repaint();
				String s = e.getMessage();
				String t = s.substring(s.indexOf(" "));
				t.split("\\s+");
				throw new FQLException("Syntax Error: " + s);
			}
			
			Environment cp = new Environment(parsed_program);

			List<String> commands = new LinkedList<>();
			for (String s : parsed_program.inorder()) {
				if (parsed_program.isMapping(s)
						|| parsed_program.isSignature(s)) {
					if (!parsed_program.hasKey(s)
							&& DEBUG.INTERMEDIATE == Intermediate.NONE) {
						continue;
					}
				}
				if (parsed_program.isMapping(s)) {
					if (DEBUG.INTERMEDIATE == Intermediate.NONE
							|| DEBUG.INTERMEDIATE == Intermediate.SOME) {
						if (parsed_program.isId(s)) {
							// System.out.println("skipping " + s);
							continue;
						}
					}
				}

				commands.add(s);
			}
			
			for (String m : cp.mappings.keySet()) {
				if (parsed_program.isMapping(m)) {
					continue;
				}
				if (DEBUG.INTERMEDIATE == Intermediate.ALL) {
					commands.add(m);
				}
			}
			for (String m : cp.signatures.keySet()) {
				if (parsed_program.isSignature(m)) {
					continue;
				}
				if (DEBUG.INTERMEDIATE == Intermediate.ALL) {
					commands.add(m);
				}
			}
			

			// Commands parsed_view = Commands.parse(view);
			if (display != null && !DEBUG.MultiView) {
				display.close();
			}
			DateFormat format = DateFormat.getTimeInstance();
			display = new Display(cp, commands);
			String foo = GUI.getTitle(id);
			if (DEBUG.MultiView) {
				foo += " - " + format.format(new Date(System.currentTimeMillis()));
			}
			display.display(foo, commands);

			String psm = PSMGen.compile(cp, parsed_program);
			respArea.setText(psm);

			//List<PSM> psm0 = PSMGen.compile0(cp, parsed_program);
			//String output0 = PSMInterp.interp0(psm0);
			// System.out.println(output0);
			// respArea.setText(output0 + "\n\n---------------\n\n" + psm);

		} catch (Exception e) {
			respArea.setText(e.toString());
			e.printStackTrace();
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

}
