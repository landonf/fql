package fql.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.border.Border;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.Document;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

@SuppressWarnings("serial")
/**
 * @author ryan
 * A text editor.
 */
public class FQLTextPanel extends JPanel {
	
	JTextArea area = new JTextArea();
	
	public void setText(String s) {
		area.setText(s);
		area.setCaretPosition(0);
	}
	
	public String getText() {
		return area.getText();
	}
	

	//TODO: (DEFER) topological sort of definitions
	
	public FQLTextPanel(String title, String text) {
		super(new GridLayout(1,1));
		Border b = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), title);

		setBorder(b);
		
		JScrollPane p = new JScrollPane(area);
		add(p);
		p.setBorder(BorderFactory.createEmptyBorder());
		setText(text);
		
		area.setFont(new Font("Courier", Font.PLAIN, 13));
		
		final UndoManager m = new UndoManager();
	//	area.setundoManager = new UndoManager();
		Document doc = area.getDocument();
		doc.addUndoableEditListener(new UndoableEditListener() {
		    @Override
		    public void undoableEditHappened(UndoableEditEvent e) {
		        m.addEdit(e.getEdit());

		    }
		});

		InputMap im = area.getInputMap(JComponent.WHEN_FOCUSED);
		ActionMap am = area.getActionMap();

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), "Undo");
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), "Redo");

		am.put("Undo", new AbstractAction() {
		    @Override
		    public void actionPerformed(ActionEvent e) {
		        try {
		            if (m.canUndo()) {
		                m.undo();
		            }
		        } catch (CannotUndoException exp) {
		            exp.printStackTrace();
		        }
		    }
		});
		am.put("Redo", new AbstractAction() {
		    @Override
		    public void actionPerformed(ActionEvent e) {
		        try {
		            if (m.canRedo()) {
		                m.redo();
		            }
		        } catch (CannotUndoException exp) {
		            exp.printStackTrace();
		        }
		    }
		});
		
		//setWordWrap(true);		
	}

	public void setWordWrap(boolean b) {
		area.setLineWrap(b);
		area.setWrapStyleWord(b);
	}
	

}
