package fql.sql;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextField;

import fql.examples.Example;
import fql.gui.FQLTextPanel;

public abstract class SqlToFql {
	
	protected abstract String kind();
	protected abstract Vector<Example> examples();
	protected abstract String translate(String prefix, String in);
	protected abstract String help();
	
	public static class RAToFQL extends SqlToFql {

		@Override
		protected String kind() {
			return "RA";
		}

		@Override
		protected Vector<Example> examples() {
			// TODO Auto-generated method stub
			return new Vector<>();
		}

		@Override
		protected String translate(String prefix, String in) {
			// TODO Auto-generated method stub
			return "translation";
		}

		@Override
		protected String help() {
			// TODO Auto-generated method stub
			return "help";
		}
		
	}
	
	public static class SQLToFQL extends SqlToFql {

		@Override
		protected String kind() {
			// TODO Auto-generated method stub
			return "SQL";
		}

		@Override
		protected Vector<Example> examples() {
			// TODO Auto-generated method stub
			return new Vector<>();
		}

		@Override
		protected String translate(String prefix, String in) {
			// TODO Auto-generated method stub
			return "SQL translation";
		}

		@Override
		protected String help() {
			// TODO Auto-generated method stub
			return "help";
		}
		
	}
	
	
	public SqlToFql() {
		final FQLTextPanel input = new FQLTextPanel(kind() + " Input", "");
		final FQLTextPanel output = new FQLTextPanel("FQL Output", "");

		JButton jdbcButton = new JButton("Load using JDBC");
		JButton runButton = new JButton("Run " + kind());
		JButton transButton = new JButton("Translate");
		JButton helpButton = new JButton("Help");
		JButton runButton2 = new JButton("Run FQL");
		JCheckBox jdbcBox = new JCheckBox("Run using JDBC");
		JLabel lbl = new JLabel("Suffix (optional):", JLabel.RIGHT);
		lbl.setToolTipText("FQL will translate table T to T_suffix, and generate SQL to load T into T_suffix");
		JTextField field = new JTextField(8);
		field.setText("fql");

		final JComboBox<Example> box = new JComboBox<>(examples());
		box.setSelectedIndex(-1);
		box.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				input.setText(((Example) box.getSelectedItem()).getText());
			}

		});
				 		
		jdbcButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO load from jdbc sql to fql				
			}
		});
		
		runButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO run sql sql to fql				
			}
		});

		runButton2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO run sql sql to fql 2				
			}
		});

		
		transButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO load from jdbc sql to fql				
			}
		});
		
		helpButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO help Button				
			}
		});
		
		
		JPanel p = new JPanel(new BorderLayout());
		
		JSplitPane jsp = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		jsp.setBorder(BorderFactory.createEmptyBorder());
		jsp.setDividerSize(4);
		jsp.setResizeWeight(0.5d);
		jsp.add(input);
		jsp.add(output);
		
		JPanel bp = new JPanel(new GridLayout(1,5));
		JPanel tp = new JPanel(new GridLayout(1,5));
		
		bp.add(field);
		
		tp.add(transButton);
		tp.add(jdbcButton);
		tp.add(helpButton);
		tp.add(new JLabel("Load Example", JLabel.RIGHT));
		tp.add(box);
		
		bp.add(runButton);
		bp.add(runButton2);
		bp.add(lbl);
		bp.add(field);
		bp.add(jdbcBox);
		
		p.add(bp, BorderLayout.SOUTH);
		p.add(jsp, BorderLayout.CENTER);
		p.add(tp, BorderLayout.NORTH);
		JFrame f = new JFrame(kind() + " to FQL");
		f.setContentPane(p);
		f.pack();
		f.setSize(new Dimension(700, 600));
		f.setLocationRelativeTo(null);
		f.setVisible(true);
	}
	
	
}
