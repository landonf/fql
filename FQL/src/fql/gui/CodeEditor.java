package fql.gui;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.Border;

import org.fife.ui.rsyntaxtextarea.AbstractTokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.TokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.folding.CurlyFoldParser;
import org.fife.ui.rsyntaxtextarea.folding.FoldParserManager;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.fife.ui.rtextarea.SearchContext;
import org.fife.ui.rtextarea.SearchEngine;


public class CodeEditor extends JPanel {

   private static final long serialVersionUID = 1L;
   
   RSyntaxTextArea textArea = new RSyntaxTextArea();
	
   final JTextField  searchField = new JTextField();
   final JButton nextButton = new JButton("Find Next");
   final JButton prevButton = new JButton("Find Previous");
   final JCheckBox matchCaseCB = new JCheckBox("Match Case");
   final JCheckBox replaceCB = new JCheckBox("Replace");
   final JTextField replaceField = new JTextField();
   
   JFrame frame;
   
   
   public JPanel foo() {
   JPanel toolBar = new JPanel(new GridLayout(2,4));
   
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
   //prevButton.addActionListener(this);
  // toolBar.add(prevButton);
 
   
  // ret.add(toolBar);
  // toolBar = new JPanel();
   toolBar.add(new JLabel("Replace with:"));
   toolBar.add(replaceField);
   toolBar.add(replaceCB);
   toolBar.add(matchCaseCB);
   
   //ret.add(toolBar);
  
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
    //context.setRegularExpression(regexCB.isSelected());
    context.setSearchForward(b);
    context.setWholeWord(false);
    
    if (replaceCB.isSelected()) {
    	context.setReplaceWith(replaceField.getText());
    	SearchEngine.replace(textArea, context);
    } else {
    	SearchEngine.find(textArea, context);
    }
//    if (!found) {
//       JOptionPane.showMessageDialog(null, "Text not found");
//    }
	
}

//   public void actionPerformed(ActionEvent e) {
//
//	      // "FindNext" => search forward, "FindPrev" => search backward
//	      String command = e.getActionCommand();
//	      boolean forward = "FindNext".equals(command);
//
//	      // Create an object defining our search parameters.
//	      SearchContext context = new SearchContext();
//	      String text = searchField.getText();
//	      if (text.length() == 0) {
//	         return;
//	      }
//	      context.setSearchFor(text);
//	      context.setMatchCase(matchCaseCB.isSelected());
//	     // context.setRegularExpression(regexCB.isSelected());
//	      context.setSearchForward(forward);
//	      context.setWholeWord(false);
//
//	      boolean found = SearchEngine.find(textArea, context);
//	      if (!found) {
//	         JOptionPane.showMessageDialog(this, "Text not found");
//	      }
//
//	   }

   
	public void setText(String s) {
		textArea.setText(s);
		textArea.setCaretPosition(0);
		//textArea.set
	}
	
	public String getText() {
		return textArea.getText();
	}

	public void makeSearchVisible() {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				
				if (frame == null) {
					frame = new JFrame();
					frame.setContentPane(foo()); 
					frame.setTitle("Find and Replace");
					 
					frame.pack();
					frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
					//frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
					frame.setLocationRelativeTo(null);
				}
				frame.setVisible(true);
			}
			
		});
		
	}
	
   public  CodeEditor(String title, String content) {
	super(new GridLayout(1,1));
	
	
	
	
	Border b = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), title);

	setBorder(b);

     // JPanel cp = new JPanel(new BorderLayout());

      textArea = new RSyntaxTextArea();
    //  textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_NONE);
      
      AbstractTokenMakerFactory atmf = (AbstractTokenMakerFactory)TokenMakerFactory.getDefaultInstance();
      atmf.putMapping("text/fql", "fql.parse.FqlTokenMaker");
     FoldParserManager.get().addFoldParserMapping("text/fql", new CurlyFoldParser());
     textArea.setSyntaxEditingStyle("text/fql");
     
     
    //  textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
    //  textArea.setSyn
      
  //    RSyntaxDocument.
    //  SyntaxScheme scheme = textArea.s();
     
      //textArea.setS
      textArea.setCodeFoldingEnabled(true);
      textArea.setAntiAliasingEnabled(true);
      RTextScrollPane sp = new RTextScrollPane(textArea);
      sp.setFoldIndicatorEnabled(true);
      add(sp);

      textArea.setText(content);
      textArea.setCaretPosition(0);

   }

//   public static void main(String[] args) {
//      // Start all Swing applications on the EDT.
//      SwingUtilities.invokeLater(new Runnable() {
//         public void run() {
//        	 
//           setContentPane(cp);
//           setTitle("Text Editor Demo");
//           setDefaultCloseOperation(EXIT_ON_CLOSE);
//           pack();
//           setLocationRelativeTo(null);
//            new Test("foo", "bar").setVisible(true);
//         }
//      });
//   }

}
