package fql.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;

import net.categoricaldata.api.BackEnd;

/**
 * 
 * @author ryan
 * 
 *         Input for doing delta, sigma, pi on json instances
 * 
 */
public class JsonPanel {

	public static void showPanel(BackEnd b) {
		JPanel p = JsonPanel.makePanel(b);
		JFrame f = new JFrame("JSON Input - " + b.version());
		f.pack();
		f.setSize(new Dimension(800, 400));
		f.setContentPane(p);
		f.setLocationRelativeTo(null);
		f.setVisible(true);
	}

	public static JPanel makePanel(final BackEnd back) {
		JPanel p = new JPanel(new BorderLayout());

		final JTextArea a1 = new JTextArea(48, 48);
		JScrollPane p1 = new JScrollPane(a1);
		JPanel q1 = new JPanel(new GridLayout(1, 1));
		q1.add(p1);
		q1.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createLineBorder(Color.black), "Instance"));
		final JTextArea a2 = new JTextArea(48, 48);
		JScrollPane p2 = new JScrollPane(a2);
		JPanel q2 = new JPanel(new GridLayout(1, 1));
		q2.add(p2);
		q2.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createLineBorder(Color.black), "Mapping/Instance"));
		final JTextArea resp = new JTextArea(48, 48);
		JScrollPane p3 = new JScrollPane(resp);
		JPanel q3 = new JPanel(new GridLayout(1, 1));
		q3.add(p3);
		q3.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createLineBorder(Color.black), "Result"));

		JButton delta = new JButton("Delta");
		delta.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				resp.setText(delta(back, a1.getText(), a2.getText()));
			}

		});

		JButton sigma = new JButton("Sigma");
		sigma.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				resp.setText(sigma(back, a1.getText(), a2.getText()));
			}

		});

		JButton pi = new JButton("Pi");
		pi.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				resp.setText(pi(back, a1.getText(), a2.getText()));
			}

		});

		JButton iso = new JButton("Iso");
		iso.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				resp.setText(iso(back, a1.getText(), a2.getText()));
			}

		});

		JSplitPane pp1 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		JSplitPane pp2 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		pp1.add(q1);
		pp1.add(pp2);
		pp2.add(q2);
		pp2.add(q3);

		pp2.setBorder(BorderFactory.createEmptyBorder());
		pp1.setBorder(BorderFactory.createEmptyBorder());

		pp1.setDividerLocation(260);
		pp2.setDividerLocation(260);

		pp1.setResizeWeight(0.4);
		pp2.setResizeWeight(0.5);

		a1.setFont(new Font("Courier", Font.PLAIN, 13));
		a2.setFont(new Font("Courier", Font.PLAIN, 13));
		resp.setFont(new Font("Courier", Font.PLAIN, 13));

		// pp1.setDividerSize(4);
		// pp2.setDividerSize(3);

		// pp1.setPreferredSize(new Dimension(200, 200));
		// a2.setSize(200, 200);
		// resp.set(200, 200);
		//

		JPanel center = new JPanel(new GridLayout(1, 1));
		center.add(pp1);

		JPanel buttons = new JPanel(new GridLayout(1, 4, 2, 2));
		buttons.add(delta);
		buttons.add(sigma);
		buttons.add(pi);
		buttons.add(iso);

		// Make the center component big, since that's the
		// typical usage of BorderLayout.

		// center.setPreferredSize(new Dimension(200, 100));
		p.add(new JLabel(back.readme()), BorderLayout.PAGE_START);
		p.add(center, BorderLayout.CENTER);
		p.add(buttons, BorderLayout.PAGE_END);

		p.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

		return p;
	}

	protected static String iso(BackEnd back, String i1, String i2) {
		try {
			return back.iso(i1, i2);
		} catch (Exception e) {
			e.printStackTrace();
			return e.getMessage();
		}
	}

	protected static String pi(BackEnd back, String i1, String i2) {
		try {
			return back.pi(i1, i2);
		} catch (Exception e) {
			e.printStackTrace();
			return e.getMessage();
		}
	}

	protected static String sigma(BackEnd back, String i1, String i2) {
		try {
			return back.sigma(i1, i2);
		} catch (Exception e) {
			e.printStackTrace();
			return e.getMessage();
		}
	}

	protected static String delta(BackEnd back, String i1, String i2) {
		try {
			return back.delta(i1, i2);
		} catch (Exception e) {
			e.printStackTrace();
			return e.getMessage();
		}
	}

}
