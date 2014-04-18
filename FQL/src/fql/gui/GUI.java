package fql.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.MenuShortcut;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;

import fql.DEBUG;
import fql.Pair;
import fql.examples.Example;
import fql.examples.Examples;
import fql.sql.Chase;
import fql.sql.RaToFql;
import fql.sql.RingToFql;
import fql.sql.SqlToFql;

@SuppressWarnings("serial")
/**
 * 
 * @author ryan
 *
 * Top level gui
 */
public class GUI extends JPanel {

	public static JTabbedPane editors = new JTabbedPane();

	public static JComboBox<Example> box = null;

	public static JFrame topFrame;
	
	public static Pair<JPanel, MenuBar> makeGUI(JFrame frame) {
		topFrame = frame;
		
		JPanel pan = new JPanel();

		MenuBar menuBar = new MenuBar();

		// MenuItem m = new MenuItem()

		Menu fileMenu = new Menu("File");
		MenuItem newItem = new MenuItem("New");
		MenuItem openItem = new MenuItem("Open");
		MenuItem saveItem = new MenuItem("Save");
		MenuItem saveAsItem = new MenuItem("Save As");
		MenuItem closeItem = new MenuItem("Close");
		MenuItem exitItem = new MenuItem("Exit");
		fileMenu.add(newItem);
		fileMenu.add(openItem);
		fileMenu.add(saveItem);
		fileMenu.add(saveAsItem);
		fileMenu.add(closeItem);
		fileMenu.add(exitItem);

		closeItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				closeAction();
			}

		});

		saveAsItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				saveAsAction();
			}
		});

		// respArea.setWrapStyleWord();

		KeyStroke ctrlS = KeyStroke.getKeyStroke(KeyEvent.VK_S,
				InputEvent.CTRL_MASK);
		MenuShortcut s = new MenuShortcut(ctrlS.getKeyCode());
		saveItem.setShortcut(s);

		KeyStroke ctrlW = KeyStroke.getKeyStroke(KeyEvent.VK_W,
				InputEvent.CTRL_MASK);
		KeyStroke commandW = KeyStroke.getKeyStroke(KeyEvent.VK_W,
				InputEvent.META_MASK);
		MenuShortcut c = new MenuShortcut(ctrlW.getKeyCode());
		closeItem.setShortcut(c);

		// Get the appropriate input map using the JComponent constants.
		// This one works well when the component is a container.
		// InputMap inputMap =
		// editors.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		InputMap inputMap = editors
				.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);

		// Add the key binding for the keystroke to the action name
		inputMap.put(ctrlW, "closeTab");
		inputMap.put(commandW, "closeTab");

		// Now add a single binding for the action name to the anonymous action
		AbstractAction closeTabAction = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				closeAction();
			}
		};
		editors.getActionMap().put("closeTab", closeTabAction);

		KeyStroke ctrlN = KeyStroke.getKeyStroke(KeyEvent.VK_N,
				InputEvent.CTRL_MASK);
		MenuShortcut n = new MenuShortcut(ctrlN.getKeyCode());
		newItem.setShortcut(n);
		KeyStroke ctrlO = KeyStroke.getKeyStroke(KeyEvent.VK_O,
				InputEvent.CTRL_MASK);
		MenuShortcut o = new MenuShortcut(ctrlO.getKeyCode());
		openItem.setShortcut(o);

		Menu toolsMenu = new Menu("Tools");
		Menu transMenu = new Menu("Translate");

		final Menu editMenu = new Menu("Edit");
		MenuItem findItem = new MenuItem("Find");
		editMenu.add(findItem);

		KeyStroke ctrlF = KeyStroke.getKeyStroke(KeyEvent.VK_F,
				InputEvent.CTRL_MASK);
		MenuShortcut f = new MenuShortcut(ctrlF.getKeyCode());
		findItem.setShortcut(f);

		KeyStroke ctrlQ = KeyStroke.getKeyStroke(KeyEvent.VK_Q,
				InputEvent.CTRL_MASK);
		MenuShortcut q = new MenuShortcut(ctrlQ.getKeyCode());
		exitItem.setShortcut(q);

		// Menu optionsMenu = new Menu("Options");
/*		MenuItem optionsItem = new MenuItem("Show Options");
		editMenu.add(optionsItem);
		optionsItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DEBUG.debug.showOptions();
			}
		}); */

		MenuItem formatItem = new MenuItem("Format Code");
		editMenu.add(formatItem);
		formatItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				formatAction();
			}
		});
		
		MenuItem chaseItem = new MenuItem("Run Chase");
		toolsMenu.add(chaseItem);
		chaseItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Chase.dostuff();
			}
		});

		MenuItem checkItem = new MenuItem("Type Check");
		toolsMenu.add(checkItem);
		checkItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				checkAction();
			}
		});

		MenuItem raToFqlItem = new MenuItem("SPCU to FQL");
		transMenu.add(raToFqlItem);
		raToFqlItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				raToFqlAction();
			}
		});

		MenuItem sqlToFqlItem = new MenuItem("SQL Schema to FQL");
		transMenu.add(sqlToFqlItem);
		sqlToFqlItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sqlToFqlAction();
			}
		});
		
		MenuItem ringToFqlItem = new MenuItem("Polynomials to FQL");
		transMenu.add(ringToFqlItem);
		ringToFqlItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ringToFqlAction();
			}
		});

		MenuItem abortItem = new MenuItem("Abort");
		toolsMenu.add(abortItem);
		abortItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				abortAction();
			}
		});

		Menu helpMenu = new Menu("About");
		/*		MenuItem helpItem = new MenuItem("Help");
		helpMenu.add(helpItem);
		helpItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				helpAction();
			} 

		}); */
		MenuItem aboutItem = new MenuItem("About");
		helpMenu.add(aboutItem);
		aboutItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DEBUG.showAbout();
			}
		});

		newItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				newAction(null, "");
			}
		});
		openItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openAction();
			}
		});
		saveItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveAction();
			}
		});

		exitItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				exitAction();
			}
		});

		findItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				delay();
				((CodeEditor) editors.getComponentAt(editors.getSelectedIndex()))
						.findAction();

			}
		});

		menuBar.add(fileMenu);
		menuBar.add(editMenu);
		menuBar.add(toolsMenu);
		menuBar.add(transMenu);
		// menuBar.add(optionsMenu);

		// menuBar.add(webMenu);
		menuBar.add(helpMenu);

		// JSplitPane p = new FQLSplit(.8, JSplitPane.HORIZONTAL_SPLIT);
		// topArea.setPreferredSize(new Dimension(600,600));
		// p.add(topArea);
		// p.add(bottomArea);
		// p.setBorder(BorderFactory.createEmptyBorder());

		pan.setLayout(new BorderLayout());

		JPanel toolBar = new JPanel(new GridLayout(1, 7));
		// toolBar.setBorder(BorderFactory.createLineBorder(Color.BLACK));

		JButton compileB = new JButton("Compile");
		compileB.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				((CodeEditor) editors.getComponentAt(editors.getSelectedIndex()))
						.runAction();
			}

		});

		JButton helpB = new JButton("Help");
		helpB.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				helpAction();
			}
		});

		JButton new_button = new JButton("New");
		new_button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				newAction(null, "");
			}
		});

		JButton save_button = new JButton("Save");
		save_button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveAction();
			}
		});
		// if (FQLApplet.isapplet) {
		// save_button.setEnabled(false);
		// }

		JButton open_button = new JButton("Open");
		open_button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openAction();
			}
		});
		// if (FQLApplet.isapplet) {
		// open_button.setEnabled(false);
		// }

		// toolBar temp1 = new JPanel();
		JLabel l = new JLabel("Load Example:", JLabel.RIGHT);
		
		if (DEBUG.debug.limit_examples) {
			box = new JComboBox<>(Examples.key_examples);			
		} else {
			box = new JComboBox<>(Examples.examples);
		}
		box.setSelectedIndex(-1);
		box.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				doExample((Example) box.getSelectedItem());
			}

		});

		toolBar.add(compileB);

		// button.setFont(new Font("Arial", 12, Font.PLAIN));
		// toolBar.setFont(new Font("Arial", 12, Font.PLAIN));
		// toolBar.addSeparator();
		toolBar.add(new_button);
		// toolBar.addSeparator();
		toolBar.add(open_button);
		// toolBar.addSeparator();
		toolBar.add(save_button);
		// toolBar.add(all_button);
		// toolBar.addSeparator();

		toolBar.add(helpB);

		JButton optionsb = new JButton("Options");
		toolBar.add(optionsb);
		optionsb.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DEBUG.debug.showOptions();
			}
		});

		MenuItem vItem = new MenuItem("Visually Edit");
		editMenu.add(vItem);
		vItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				vedit();
			}
			
		});
		

		// JPanel temp2 = new JPanel();
		// temp2.add(l);
		// temp2.add(box);

		// toolBar.add(temp2);
		toolBar.add(l);
		toolBar.add(box);

		pan.add(toolBar, BorderLayout.PAGE_START);
		pan.add(editors, BorderLayout.CENTER);

		newAction(null, "");

		// editors.setFocusCycleRoot(true);
		// editors.requestFocusInWindow();
		return new Pair<>(pan, menuBar);
	}
	


	protected static void doExample(Example e) {
		// int i = untitled_count;
		if (e == null) {
			return;
		}
		newAction(e.getName(), e.getText());
	}
	
	private static void formatAction() {
		int i = editors.getSelectedIndex();
		CodeEditor c = (CodeEditor) editors.getComponentAt(i);
		if (c == null) {
			return;
		}
		c.format();
	}
	
	private static void vedit() {
		int i = editors.getSelectedIndex();
		CodeEditor c = (CodeEditor) editors.getComponentAt(i);
		if (c == null) {
			return;
		}
		c.vedit();
	}

	private static void checkAction() {
		int i = editors.getSelectedIndex();
		CodeEditor c = (CodeEditor) editors.getComponentAt(i);
		if (c == null) {
			return;
		}
		c.check();
	}

	private static void sqlToFqlAction() {
		new SqlToFql();
	}
	
	private static void ringToFqlAction() {
		new RingToFql();
	}

	private static void raToFqlAction() {
		new RaToFql();
	}

	private static void abortAction() {
		int i = editors.getSelectedIndex();
		CodeEditor c = (CodeEditor) editors.getComponentAt(i);
		if (c == null) {
			return;
		}
		c.abortAction();
	}

	private static void closeAction() {
		delay();
		int i = editors.getSelectedIndex();
		CodeEditor c = (CodeEditor) editors.getComponentAt(i);
		if (c == null) {
			return;
		}
		if (c.abortBecauseDirty()) {
			return;
		}
		editors.remove(i);
		dirty.remove(c.id);
		keys.remove(c.id);
		files.remove(c.id);
		titles.remove(c.id);
	}

	static void helpAction() {
		JTextArea jta = new JTextArea(Examples.helpString);
		jta.setWrapStyleWord(true);
		// jta.setEditable(false);
		jta.setLineWrap(true);
		JScrollPane p = new JScrollPane(jta,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		p.setPreferredSize(new Dimension(650, 300));

		JOptionPane pane = new JOptionPane(p);
		// Configure via set methods
		JDialog dialog = pane.createDialog(null, "Help");
		dialog.setModal(false);
		dialog.setVisible(true);
		dialog.setResizable(true);

		// JOptionPane.showMessageDialog(null, p, "Help",
		// JOptionPane.PLAIN_MESSAGE, null);
	}

	public static void exitAction() {
		delay();
		int i = 0;
		for (Integer x : keys.keySet()) {
			if (dirty.get(x).equals(true)) {
				i++;
			}
		}
		if (i == 0) {
			System.exit(0);
		}
		int choice = JOptionPane.showConfirmDialog(null, i
				+ " documents have unsaved changes - continue?", "Exit?",
				JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null);
		if (choice == JOptionPane.NO_OPTION) {
			return;
		}
		System.exit(0);

		// if (abortBecauseDirty()) {
		// return;
		// }
		// System.exit(0);
	}

	protected static void saveAction() {
		CodeEditor e = (CodeEditor) editors.getComponentAt(editors
				.getSelectedIndex());
		File f = files.get(e.id);
		if (f == null) {
			saveAsAction();
		} else {
			delay();
			doSave(f, e.getText());
			setDirty(e.id, false);
		}
	}

	static void doSave(File f, String s) {
		try {
			FileWriter fw = new FileWriter(f);
			fw.write(s);
			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Could not save to " + f);
		}
	}

	public static class Filter extends FileFilter {
		@Override
		public boolean accept(File f) {
			return f.getName().toLowerCase().endsWith(".fql")
					|| f.isDirectory();
		}

		@Override
		public String getDescription() {
			return "FQL files (*.fql)";
		}
	}

	protected static void saveAsAction() {
		delay();
		JFileChooser jfc = new JFileChooser(DEBUG.debug.FILE_PATH);
		jfc.setFileFilter(new Filter());
		jfc.showSaveDialog(null);
		File f = jfc.getSelectedFile();
		if (f == null) {
			return;
		}
		if (!jfc.getSelectedFile().getAbsolutePath().endsWith(".fql")) {
			f = new File(jfc.getSelectedFile() + ".fql");
		}
		CodeEditor e = (CodeEditor) editors.getComponentAt(editors
				.getSelectedIndex());
		doSave(f, e.getText());
		// change for david
		dirty.put(e.id, false);
		closeAction();
		doOpen(f);
	}

	static void doOpen(File f) {
		String s = readFile(f.getAbsolutePath());
		if (s == null) {
			return;
		}
		Integer i = newAction(f.getName(), s);
		files.put(i, f);
	}

	static protected void openAction() {
		delay();
		JFileChooser jfc = new JFileChooser(DEBUG.debug.FILE_PATH);
		jfc.setFileFilter(new Filter());
		jfc.showOpenDialog(null);
		File f = jfc.getSelectedFile();
		if (f == null) {
			return;
		}
		doOpen(f);
	}

	static void setDirty(Integer i, boolean b) {
		dirty.put(i, b);
	}

	static Boolean getDirty(Integer i) {
		Boolean ret = dirty.get(i);
		if (ret == null) {
			throw new RuntimeException();
		}
		return dirty.get(i);
	}

	public static void setFontSize(int size) {
		for (CodeEditor c : keys.values()) {
			c.setFontSize(size);
		}
	}
	
	static Map<Integer, Boolean> dirty = new HashMap<>();
	static Map<Integer, CodeEditor> keys = new HashMap<>();
	static Map<Integer, File> files = new HashMap<>();
	static Map<Integer, String> titles = new HashMap<>();
	// static Map<Integer, Integer> position = new HashMap<>();
	static int untitled_count = 0;

	public static String getTitle(Integer i) {
		return titles.get(i);
	}

	static Integer newAction(String title, String content) {
		untitled_count++;
		CodeEditor c = new CodeEditor(untitled_count, content);
		int i = editors.getTabCount();
		keys.put(untitled_count, c);
		dirty.put(untitled_count, false);
		// position.put(untitled_count, i);

		if (title == null) {
			title = "Untitled " + untitled_count;
		}
		titles.put(c.id, title);
		editors.addTab(title, c);
		editors.setTabComponentAt(i, new ButtonTabComponent(editors));
		editors.setSelectedIndex(i);
		// editors.requestFocus();
		// editors.requestFocus(false); //requestDefaultFocus();
		// c.dis
		return c.id;

	}

	static private String readFile(String file) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line = null;
			StringBuilder stringBuilder = new StringBuilder();
			String ls = System.getProperty("line.separator");

			while ((line = reader.readLine()) != null) {
				stringBuilder.append(line);
				stringBuilder.append(ls);
			}

			reader.close();
			return stringBuilder.toString();
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Could not read from " + file);
		}
		return null;
	}

	private static void delay() {
		try {
			// Thread.currentThread();
			Thread.sleep(100); // hack for enough time to unhighlight menu
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}

}
