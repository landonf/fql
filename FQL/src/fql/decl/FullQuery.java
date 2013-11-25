package fql.decl;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Paint;
import java.awt.Stroke;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.algorithms.layout.ISOMLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.VertexLabelRenderer;
import fql.Pair;

public abstract class FullQuery {

	public abstract Pair<Signature, Signature> type();
	
	public abstract Color color();

	public abstract String kind();

	private class ToGraph implements
			FullQueryVisitor<Integer, Graph<Pair<FullQuery, Integer>, Integer>> {

		int count = 0;
		int edge_count = 0;

		@Override
		public Integer visit(Graph<Pair<FullQuery, Integer>, Integer> env,
				Comp e) {
			Integer l = e.l.accept(env, this);
			Integer r = e.r.accept(env, this);
			env.addVertex(new Pair<>((FullQuery) e, count));
			env.addEdge(edge_count++, new Pair<>((FullQuery) e, count),
					new Pair<>(e.l, l));
			env.addEdge(edge_count++, new Pair<>((FullQuery) e, count),
					new Pair<>(e.r, r));
			return count++;
		}

		@Override
		public Integer visit(Graph<Pair<FullQuery, Integer>, Integer> env,
				Delta e) {
			env.addVertex(new Pair<>((FullQuery) e, count));

			return count++;
		}

		@Override
		public Integer visit(Graph<Pair<FullQuery, Integer>, Integer> env,
				Sigma e) {
			env.addVertex(new Pair<>((FullQuery) e, count));
			return count++;
		}

		@Override
		public Integer visit(Graph<Pair<FullQuery, Integer>, Integer> env, Pi e) {
			env.addVertex(new Pair<>((FullQuery) e, count));
			return count++;
		}

	}

	public Graph<Pair<FullQuery, Integer>, Integer> build() {
		Graph<Pair<FullQuery, Integer>, Integer> g2 = new DirectedSparseMultigraph<>();
		accept(g2, new ToGraph());
		return g2;
	}

	public JComponent pretty() {
		Graph<Pair<FullQuery, Integer>, Integer> g = build();
		if (g.getVertexCount() == 0) {
			return new JPanel();
		}
		return doView(g);
	}

	public JComponent doView(
			/* final Environment env, */Graph<Pair<FullQuery, Integer>, Integer> sgv) {

		// Layout<V, E>, BasicVisualizationServer<V,E>
		// Layout<String, String> layout = new FRLayout(sgv);
		Layout<Pair<FullQuery, Integer>, Integer> layout = new ISOMLayout<Pair<FullQuery, Integer>, Integer>(
				sgv);
		// Layout<String, String> layout = new CircleLayout(sgv);
		layout.setSize(new Dimension(600, 400));
		// BasicVisualizationServer<String, String> vv = new
		// BasicVisualizationServer<String, String>(
		// layout);
		VisualizationViewer<Pair<FullQuery, Integer>, Integer> vv = new VisualizationViewer<>(
				layout);
		// vv.setPreferredSize(new Dimension(600, 400));
		// Setup up a new vertex to paint transformer...
		Transformer<Pair<FullQuery, Integer>, Paint> vertexPaint = new Transformer<Pair<FullQuery, Integer>, Paint>() {
			public Paint transform(Pair<FullQuery, Integer> i) {
				return i.first.color();
			}
		};
		DefaultModalGraphMouse<String, String> gm = new DefaultModalGraphMouse<>();
		gm.setMode(ModalGraphMouse.Mode.TRANSFORMING);
		vv.setGraphMouse(gm);
		gm.setMode(Mode.PICKING);
		// Set up a new stroke Transformer for the edges
		float dash[] = { 1.0f };
		final Stroke edgeStroke = new BasicStroke(0.5f, BasicStroke.CAP_BUTT,
				BasicStroke.JOIN_MITER, 10.0f, dash, 10.0f);
		// Transformer<String, Stroke> edgeStrokeTransformer = new
		// Transformer<String, Stroke>() {
		// public Stroke transform(String s) {
		// return edgeStroke;
		// }
		// };
		vv.getRenderContext().setVertexFillPaintTransformer(vertexPaint);
		vv.getRenderContext().setVertexLabelRenderer(new MyVertexT());

		vv.getRenderContext().setEdgeLabelTransformer(
				new ToStringLabeller<Integer>() {

					@Override
					public String transform(Integer t) {
						return "";
					}

				});
		// new ToStringLabeller<String>());
		// vv.getRenderer().getVertexRenderer().
		// vv.getRenderContext().setLabelOffset(20);
		// vv.getRenderer().getVertexLabelRenderer().setPosition(Position.CNTR);

		vv.getRenderContext().setVertexLabelTransformer(
				new ToStringLabeller<Pair<FullQuery, Integer>>() {

					@Override
					public String transform(Pair<FullQuery, Integer> t) {
						return t.first.kind();
					}

				});

		GraphZoomScrollPane zzz = new GraphZoomScrollPane(vv);
		// JPanel ret = new JPanel(new GridLayout(1,1));
		// ret.add(zzz);

		JSplitPane newthing = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		newthing.setResizeWeight(.8d); // setDividerLocation(.9d);
		newthing.add(zzz);
		newthing.add(new JScrollPane(cards));
		cards.setWrapStyleWord(true);

		JPanel xxx = new JPanel(new GridLayout(1, 1));
		xxx.add(newthing);
		xxx.setBorder(BorderFactory.createEtchedBorder());

		// xxx.setMaximumSize(new Dimension(400,400));
		return xxx;
	}

	JTextArea cards = new JTextArea();

	public static class Comp extends FullQuery {
		public Comp(FullQuery l, FullQuery r) {
			super();
			this.l = l;
			this.r = r;
			if (!l.type().second.equals(r.type().first)) {
				throw new RuntimeException("Ill-typed " + this + "\n\n\nbadness: " + l.type().second + " and " + r.type().first);
			}
		}

		public FullQuery l, r;

		@Override
		public <R, E> R accept(E env, FullQueryVisitor<R, E> v) {
			return v.visit(env, this);
		}

		@Override
		public String toString() {
			return "(" + l + " then " + r + ")";
		}

		@Override
		public Color color() {
			return Color.black;
		}

		@Override
		public String kind() {
			return "compose";
		}

		@Override
		public Pair<Signature, Signature> type() {
			Pair<Signature, Signature> k = l.type();
			Pair<Signature, Signature> v = r.type();
			if (!k.second.equals(v.first)) {
				throw new RuntimeException();
			}
			return new Pair<>(k.first, v.second);
		}
	}

	public static class Delta extends FullQuery {
		public Mapping F;

		public Delta(Mapping f) {
			super();
			F = f;
		}

		@Override
		public String toString() {
			return "delta " + F.toStringFull();
		}

		@Override
		public <R, E> R accept(E env, FullQueryVisitor<R, E> v) {
			return v.visit(env, this);
		}

		@Override
		public Color color() {
			return Color.red;
		}

		@Override
		public String kind() {
			return "delta";
		}

		@Override
		public Pair<Signature, Signature> type() {
			return new Pair<>(F.target, F.source);
		}
	}

	public static class Sigma extends FullQuery {
		public Sigma(Mapping f) {
			super();
			F = f;
		}

		public Mapping F;

		@Override
		public String toString() {
			return "SIGMA " + F.toStringFull();
		}

		@Override
		public <R, E> R accept(E env, FullQueryVisitor<R, E> v) {
			return v.visit(env, this);
		}

		@Override
		public Color color() {
			return Color.green;
		}

		@Override
		public String kind() {
			return "sigma";
		}
		@Override
		public Pair<Signature, Signature> type() {
			return new Pair<>(F.source, F.target);
		}

	}

	public static class Pi extends FullQuery {
		public Pi(Mapping f) {
			F = f;
		}

		public Mapping F;

		@Override
		public String toString() {
			return "pi " + F.toStringFull();
		}

		@Override
		public <R, E> R accept(E env, FullQueryVisitor<R, E> v) {
			return v.visit(env, this);
		}

		@Override
		public Color color() {
			return Color.blue;
		}

		@Override
		public String kind() {
			return "pi";
		}
		@Override
		public Pair<Signature, Signature> type() {
			return new Pair<>(F.source, F.target);
		}

	}

	public abstract <R, E> R accept(E env, FullQueryVisitor<R, E> v);

	public interface FullQueryVisitor<R, E> {
		public R visit(E env, Comp e);

		public R visit(E env, Delta e);

		public R visit(E env, Sigma e);

		public R visit(E env, Pi e);
	}

	public Component text() {
		JTextArea ta = new JTextArea(toString());
		JPanel tap = new JPanel(new GridLayout(1, 1));
		// ta.setBorder(BorderFactory.createEmptyBorder());
		//
		// tap.setBorder(BorderFactory.createEtchedBorder());
		ta.setWrapStyleWord(true);
		ta.setLineWrap(true);
		JScrollPane xxx = new JScrollPane(ta);
		//
		tap.add(xxx);

		return tap;
	}

	public static FullQuery toQuery(FQLProgram prog, FullQueryExp v) {
		return v.accept(prog, new ToFullQueryExp()).accept(prog, new ToFullQueryVisitor());
	}

	private class MyVertexT implements VertexLabelRenderer {

		public MyVertexT() {
		}

		@Override
		public <T> Component getVertexLabelRendererComponent(JComponent arg0,
				Object arg1, Font arg2, boolean arg3, T arg4) {
			Pair<FullQuery, Integer> xxx = (Pair<FullQuery, Integer>) arg4;
			if (arg3) {
				cards.setText(xxx.first.toString());
				cards.setCaretPosition(0);
			}
			return new JLabel(xxx.first.kind());
		}
	}

	// TODO add id to FullQuery
}
