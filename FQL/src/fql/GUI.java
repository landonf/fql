package fql;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

@SuppressWarnings("serial")
public class GUI extends JPanel {
	
	
	static Display display;
	
	static boolean dirty = false;

	public static JPanel makeGUI() {
		JPanel pan = new JPanel();
		//super("FQL IDE");
		
		MenuBar menuBar = new MenuBar();
		
		Menu fileMenu = new Menu("File");
		MenuItem newItem = new MenuItem("New");
		MenuItem openItem = new MenuItem("Open");
		MenuItem saveItem = new MenuItem("Save");
		MenuItem exitItem = new MenuItem("Exit");
		fileMenu.add(newItem);
		fileMenu.add(openItem);
		fileMenu.add(saveItem);
		fileMenu.add(exitItem);
		//respArea.setWrapStyleWord();
		
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
		
				
		menuBar.add(fileMenu);
				
		
		//JSplitPane p = new FQLSplit(.8, JSplitPane.HORIZONTAL_SPLIT);
		//topArea.setPreferredSize(new Dimension(600,600));
		//p.add(topArea);
		//p.add(bottomArea);
		//p.setBorder(BorderFactory.createEmptyBorder());
		
		JPanel p = topArea;
		
		JSplitPane xx1 = new FQLSplit(.8, JSplitPane.VERTICAL_SPLIT);
		p.setPreferredSize(new Dimension(600,400));
		xx1.add(p);
		xx1.add(respArea);
		
		 pan.setLayout(new BorderLayout());
		 
		 
	     JPanel toolBar = new JPanel(new GridLayout(2,5));
	    // toolBar.setBorder(BorderFactory.createLineBorder(Color.BLACK));
	     
	     JButton button = new JButton("Compile");
	     button.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				runAction();
			}
	    	 
	     });
	     
	     JButton reset_button = new JButton("Delta");
	     reset_button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				restAction();
			}
	    	 
	     });
	     
	     JButton pi_button = new JButton("Pi");
	     pi_button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				piAction();
			}

	     });
	     
	     JButton mig_button = new JButton("Sigma");
	     mig_button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				migAction();
			}
	    	 
	     });
	     
	     JButton emps_button = new JButton("Employees");
	     emps_button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				empsAction();
			}
	    	 
	     });
	     
	     JButton comp_button = new JButton("Composition");
	     comp_button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				compAction();
			}
	    	 
	     });
	     
	     	     
	     JButton button2 = new JButton("Help");
	     button2.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				helpAction();
			}

	    	 
	     });
	     
	     final JTextField jcb = new JTextField(Long.toString(Equality.def));
	     jcb.addActionListener(new ActionListener() {
	    	 public void actionPerformed(ActionEvent e) {
	    		 //TODO: KB timeout
	 //   		 comboAction(jcb.getSelectedItem());
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

	     toolBar.add(button);
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
	     toolBar.add(button2);

	     toolBar.add(reset_button);
	    // toolBar.addSeparator();
	     toolBar.add(pi_button);
	    // toolBar.addSeparator();
	     toolBar.add(mig_button);
	    // toolBar.addSeparator();
	     toolBar.add(emps_button);
	     //toolBar.addSeparator();
	     toolBar.add(comp_button);
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
	//	setMenuBar(menuBar);
	
		//pack();
		//setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		
	     return pan;
	}
	
	protected static void piAction() {
		if (abortBecauseDirty()) {
			return;
		}		
		program = Examples.piDefinitions;
		topArea.setText(program);
		bottomArea.setText(Examples.piCommands);
		respArea.setText("");
		dirty = false;
		if (display != null) {
			display.close();
		}
		display = null;
	}

	protected static  void restAction() {
		if (abortBecauseDirty()) {
			return;
		}		
		program = Examples.initialDefinitions;
		topArea.setText(program);
		bottomArea.setText(Examples.initialCommands);
		respArea.setText("");
		dirty = false;
		if (display != null) {
			display.close();
		}
		display = null;
	}
	
	protected static void compAction() {
		if (abortBecauseDirty()) {
			return;
		}		
		program = Examples.compDefinitions;
		topArea.setText(program);
//		bottomArea.setText(Examples.employeesCommands);
		respArea.setText("");
		dirty = false;
		if (display != null) {
			display.close();
		}
		display = null;
	}
	
	protected static void empsAction() {
		if (abortBecauseDirty()) {
			return;
		}		
		program = Examples.employeesDefinitions;
		topArea.setText(program);
		bottomArea.setText(Examples.employeesCommands);
		respArea.setText("");
		dirty = false;
		if (display != null) {
			display.close();
		}
		display = null;
	}
	
	 static void migAction() {
		if (abortBecauseDirty()) {
			return;
		}		
		program = Examples.migrationDefinitions;
		topArea.setText(program);
		bottomArea.setText(Examples.migrationCommands);
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

	 //TODO: (DEFER) crosscutting equality

	 static void runAction() {
		String program = topArea.getText();
		//String view = bottomArea.getText();
		
		try {
			Program parsed_program = Program.parse(program);
			Environment cp = new Environment(parsed_program);
			
			List<Command> commands = new LinkedList<Command>();
			for (String s : cp.instances.keySet()) {
				commands.add(new ShowCommand(s));
			}
			for (String s : cp.mappings.keySet()) {
				commands.add(new ShowCommand(s));	
			}
			for (String s : cp.queries.keySet()) {
				commands.add(new ShowCommand(s));
			}
			for (String s : cp.signatures.keySet()) {
				commands.add(new ShowCommand(s));
			}
//			Commands parsed_view = Commands.parse(view);
			if (display != null) {
				display.close();
			}
			display = new Display(cp, commands);
			display.display();
			respArea.setText("");
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
		bottomArea.setText("");
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

	static FQLTextPanel topArea = new FQLTextPanel("FQL Program", Examples.initialDefinitions);
	static FQLTextPanel bottomArea = new FQLTextPanel("Commands", Examples.initialCommands);
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
	
	static String program = Examples.initialDefinitions;

}
