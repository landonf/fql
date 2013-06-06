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
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import fql.DEBUG;
import fql.DEBUG.Intermediate;
import fql.FQLApplet;
import fql.FQLBackEnd;
import fql.FQLServlet;
import fql.Pair;
import fql.decl.Environment;
import fql.decl.Program;
import fql.examples.Example;
import fql.examples.Examples;
import fql.sql.PSM;
import fql.sql.PSMGen;
import fql.sql.PSMInterp;

@SuppressWarnings("serial")
public class GUI extends JPanel {
	
	
	static Display display;
	
	static boolean dirty = false;

	
	public static Pair<JPanel, MenuBar> makeGUI() {
		JPanel pan = new JPanel();
		//super("FQL IDE");
		
		MenuBar menuBar = new MenuBar();
		
//		MenuItem m = new MenuItem()
		
		Menu fileMenu = new Menu("File");
		MenuItem newItem = new MenuItem("New");
		MenuItem openItem = new MenuItem("Open");
		MenuItem saveItem = new MenuItem("Save");
		MenuItem exitItem = new MenuItem("Exit");
		fileMenu.add(newItem);
		fileMenu.add(openItem);
		fileMenu.add(saveItem);
		fileMenu.add(exitItem);
		respArea.setWordWrap(true);
//		respArea.setWrapStyleWord();
		KeyStroke ctrlS = KeyStroke.getKeyStroke(KeyEvent.VK_S,
		        InputEvent.CTRL_MASK);
		MenuShortcut s = new MenuShortcut(ctrlS.getKeyCode());
		saveItem.setShortcut(s);
		KeyStroke ctrlN = KeyStroke.getKeyStroke(KeyEvent.VK_N,
		        InputEvent.CTRL_MASK);
		MenuShortcut n = new MenuShortcut(ctrlN.getKeyCode());
		newItem.setShortcut(n);
		KeyStroke ctrlO = KeyStroke.getKeyStroke(KeyEvent.VK_O,
		        InputEvent.CTRL_MASK);
		MenuShortcut o = new MenuShortcut(ctrlO.getKeyCode());
		openItem.setShortcut(o);
		
		final Menu editMenu = new Menu("Edit");
		MenuItem findItem = new MenuItem("Find");
		editMenu.add(findItem);
		
		
		KeyStroke ctrlF = KeyStroke.getKeyStroke(KeyEvent.VK_F,
		        InputEvent.CTRL_MASK);
		MenuShortcut f = new MenuShortcut(ctrlF.getKeyCode());
		findItem.setShortcut(f);
		
		
//		KeyStroke ctrlQ = KeyStroke.getKeyStroke(KeyEvent.VK_Q,
//		        InputEvent.CTRL_MASK);
//		MenuShortcut q = new MenuShortcut(ctrlQ.getKeyCode());
//		exitItem.setShortcut(q);
		
		
		Menu webMenu = new Menu("Web");
		MenuItem serverItem = new MenuItem("Start Local Server");
		webMenu.add(serverItem);
		serverItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				serverAction();
			}
		});
		MenuItem jsonItem = new MenuItem("JSON Input");
		webMenu.add(jsonItem);
		jsonItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JsonPanel.showPanel(new FQLBackEnd());
			}
		});
		
		
		//Menu optionsMenu = new Menu("Options");
		MenuItem optionsItem = new MenuItem("Show Options");
		editMenu.add(optionsItem);
		optionsItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DEBUG.showOptions();
			}
		});
		
		Menu helpMenu = new Menu("About");
		MenuItem helpItem = new MenuItem("Help");
		helpMenu.add(helpItem);
		helpItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				helpAction();
			}
			
		});
		MenuItem aboutItem = new MenuItem("Legal stuff");
		helpMenu.add(aboutItem);
		aboutItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DEBUG.showAbout();
			}
		});
		
		newItem.addActionListener(
				new	ActionListener() {
					public void actionPerformed(ActionEvent e) {
						newAction();
					}
				}
		);
		openItem.addActionListener(
				new	ActionListener() {
					public void actionPerformed(ActionEvent e) {
						openAction();
					}
				}
		);
		saveItem.addActionListener(
				new	ActionListener() {
					public void actionPerformed(ActionEvent e) {
						saveAction();
					}
				}
		);
		
		exitItem.addActionListener(
				new	ActionListener() {
					public void actionPerformed(ActionEvent e) {
						exitAction();
					}
				}
		);
		
		findItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				findAction();
//				findItem.
//				editMenu.setEnabled(false);
//				editMenu.setEnabled(true);
			}
		});
				
		menuBar.add(fileMenu);
		menuBar.add(editMenu);
		//menuBar.add(optionsMenu);
		
		menuBar.add(webMenu);
		menuBar.add(helpMenu);
				
		
		//JSplitPane p = new FQLSplit(.8, JSplitPane.HORIZONTAL_SPLIT);
		//topArea.setPreferredSize(new Dimension(600,600));
		//p.add(topArea);
		//p.add(bottomArea);
		//p.setBorder(BorderFactory.createEmptyBorder());
		
		JPanel p = topArea;
		
		JSplitPane xx1 = new FQLSplit(.8, JSplitPane.VERTICAL_SPLIT);
		p.setPreferredSize(new Dimension(600,400));
		xx1.add(p);
		xx1.setDividerSize(6);
		xx1.add(respArea);
		
		 pan.setLayout(new BorderLayout());
		 
		 
	     JPanel toolBar = new JPanel(new GridLayout(1,7));
	    // toolBar.setBorder(BorderFactory.createLineBorder(Color.BLACK));
	     
	     JButton compileB = new JButton("Compile");
	     compileB.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				runAction();
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
	    		newAction();
	    	}
	     });
	     
	     JButton save_button = new JButton("Save");
	     save_button.addActionListener(new ActionListener() {
		    	public void actionPerformed(ActionEvent e) {
		    		saveAction();
		    	}
		     });
	     if (FQLApplet.isapplet) {
	    	 save_button.setEnabled(false);
	     }
	     
	     JButton open_button = new JButton("Open");
	     open_button.addActionListener(new ActionListener() {
		    	public void actionPerformed(ActionEvent e) {
		    		openAction();
		    	}
		     });
	     if (FQLApplet.isapplet) {
	    	 open_button.setEnabled(false);
	     }

	  //   toolBar temp1 = new JPanel();
	     JLabel l = new JLabel("Load Example:", JLabel.RIGHT);
	     final JComboBox<Example> box = new JComboBox<>(Examples.examples);
	     box.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				doExample((Example)box.getSelectedItem());
			}
	    	 
	     });
	     
	     toolBar.add(compileB);


//	     button.setFont(new Font("Arial", 12, Font.PLAIN));
	//toolBar.setFont(new Font("Arial", 12, Font.PLAIN));
	  //   toolBar.addSeparator();
	     toolBar.add(new_button);
	   //  toolBar.addSeparator();
	     toolBar.add(open_button);
	    // toolBar.addSeparator();
	     toolBar.add(save_button);
	     //toolBar.add(all_button);
	    // toolBar.addSeparator();
	     
	     toolBar.add(helpB);
	     
	     JButton optionsb = new JButton("Options");
	     toolBar.add(optionsb);
			optionsb.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					DEBUG.showOptions();
				}
			});

	     
	     //JPanel temp2 = new JPanel();
	     //temp2.add(l);
	     //temp2.add(box);
	     
	     //toolBar.add(temp2);
	     toolBar.add(l);
	     toolBar.add(box);



	     //toolBar.add(temp1);
	     
//	     toolBar.add(reset_button);
	    // toolBar.addSeparator();
	//     toolBar.add(pi_button);
	    // toolBar.addSeparator();
	  //   toolBar.add(mig_button);
	    // toolBar.addSeparator();
	   //  toolBar.add(emps_button);
	     //toolBar.addSeparator();
	    // toolBar.add(comp_button);
	     //toolBar.addSeparator();

//	     toolBar.addSeparator();
	//     toolBar.add(new JLabel("Knuth-Bendix timout:"));
	  //   toolBar.add(jcb);
	   //  toolBar.setFloatable(false);
	    // toolBar.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
	     
	     pan.add(toolBar, BorderLayout.PAGE_START);
	     pan.add(xx1, BorderLayout.CENTER);
		
	  //   this.
	     
	//	setContentPane(pan);
	//	pan.setMenuBar(menuBar);
	
		//pack();
		//setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		
	     return new Pair<>(pan, menuBar);
	}


	protected static void findAction() {
		
						topArea.makeSearchVisible();
				
	}


	protected static void serverAction() {
		String r = JOptionPane.showInputDialog("Local port:", 8085);
		if (r == null) {
			return;
		}
		try {
			int rr = Integer.parseInt(r);
			FQLServlet.serve(rr);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, e);
		}
	}

	

	
	
	protected static void doExample(Example e) {
		if (abortBecauseDirty()) {
			return;
		}		
		program = e.getText();
		topArea.setText(program);
//		bottomArea.setText(Examples.employeesCommands);
		respArea.setText("");
		dirty = false;
		if (display != null) {
			display.close();
		}
		display = null;		
	}
	
	

	 static void helpAction() {
		 JTextArea jta = new JTextArea(Examples.helpString);
		 JScrollPane p = new JScrollPane(jta);
		 
		 JOptionPane.showMessageDialog(null, p, "Help", JOptionPane.QUESTION_MESSAGE);
	}


	 static void runAction() {
		String program = topArea.getText();
		//String view = bottomArea.getText();
		
		try {
			Program parsed_program = Program.parse(program);
			Environment cp = new Environment(parsed_program);
			
			List<String> commands = new LinkedList<>();
			for (String s : cp.instances.keySet()) {
				commands.add(s);
			}
			for (String s : cp.mappings.keySet()) {
				if (!parsed_program.hasKey(s) && DEBUG.INTERMEDIATE == Intermediate.NONE) {
					continue;
				}
				commands.add(s);	
			}
			for (String s : cp.queries.keySet()) {
				commands.add(s);
			}
			for (String s : cp.signatures.keySet()) {
				if (!parsed_program.hasKey(s) && DEBUG.INTERMEDIATE == Intermediate.NONE) {
					continue;
				}
				commands.add(s);
			}
//			Commands parsed_view = Commands.parse(view);
			if (display != null) {
				display.close();
			}
			display = new Display(cp, commands);
			display.display();
			
			String psm = PSMGen.compile(cp, parsed_program);
			respArea.setText(psm);
						
			List<PSM> psm0 = PSMGen.compile0(cp, parsed_program);
			String output0 = PSMInterp.interp0(psm0);
		//	System.out.println(output0);
		//	respArea.setText(output0 + "\n\n---------------\n\n" + psm);
			
		} catch (Exception e) {
			respArea.setText(e.toString());
			e.printStackTrace();
		}
		
	}

	static protected void exitAction() {
		if (abortBecauseDirty()) {
			return;
		}
		System.exit(0);
	}

	static protected void saveAction() {
		delay();
		JFileChooser jfc = new JFileChooser();
		jfc.showSaveDialog(null);
		File f = jfc.getSelectedFile();
		if (f == null) {
			return;
		}
		try {
			FileWriter fw = new FileWriter(f);
			fw.write(topArea.getText());
			fw.close();
			dirty = false;
			program = topArea.getText();
		} catch (Exception e){
			e.printStackTrace();
		}		
	}

	static protected void openAction() {
		delay();
		if (abortBecauseDirty()) {
			return;
		}
		JFileChooser jfc = new JFileChooser();
		jfc.showOpenDialog(null);
		File f = jfc.getSelectedFile();
		if (f == null) {
			return;
		}
		try {
			String s = readFile(f.getAbsolutePath());
			topArea.setText(s);
			dirty = false;
			program = s;
			if (display != null) {
				display.close();
			}
			display = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	static protected void newAction() {
		if (abortBecauseDirty()) {
			return;
		}
		topArea.setText("");
//		bottomArea.setText("");
		dirty = false;
		program = "";
		if (display != null) {
			display.close();
		}
		display = null;
	}

	static private boolean abortBecauseDirty() {
		if (program.trim().equals(topArea.getText().trim())) {
			
			return false;
		}
		
		int choice = JOptionPane.showConfirmDialog(null, "Unsaved Changes - Continue?", "Continue?", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null);
		if (choice == JOptionPane.NO_OPTION) {
			return true;
		}
		return false;
	}
	

	static String program = Examples.INIT_EXAMPLE.getText();
	static CodeEditor topArea = new CodeEditor("FQL Program",program);

	//static FQLTextPanel topArea = new FQLTextPanel("FQL Program",program);
//	static FQLTextPanel bottomArea = new FQLTextPanel("Commands", Examples.initialCommands);
	static FQLTextPanel respArea = new FQLTextPanel("Compiler response", "");
	
	static private String readFile( String file ) throws IOException {
	    BufferedReader reader = new BufferedReader( new FileReader (file));
	    String         line = null;
	    StringBuilder  stringBuilder = new StringBuilder();
	    String         ls = System.getProperty("line.separator");

	    while( ( line = reader.readLine() ) != null ) {
	        stringBuilder.append( line );
	        stringBuilder.append( ls );
	    }

	    reader.close();
	    return stringBuilder.toString();
	}
	
	private static void delay() {
	try {
		//Thread.currentThread();
		Thread.sleep(100); //hack for enough time to unhighlight menu
	} catch (Exception e) {
		e.printStackTrace();
		return;
	}}

}
