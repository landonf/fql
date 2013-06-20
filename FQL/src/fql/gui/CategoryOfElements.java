package fql.gui;

import java.awt.Dimension;
import java.util.Set;

import javax.swing.JPanel;

import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import fql.FQLException;
import fql.Pair;
import fql.cat.Arr;
import fql.cat.FinCat;
import fql.decl.Instance;
import fql.decl.Node;
import fql.decl.Path;

/**
 * 
 * @author ryan
 *
 * Displays the category of elements of an instance,
 * the Grothendieck construction.
 */
public class CategoryOfElements {
	
	private static Graph<Pair<Node, Object>, Pair<Path, Integer>> build(Instance i) throws FQLException {
		FinCat<Node, Path> c = i.thesig.toCategory2().first;

		Graph<Pair<Node, Object>, Pair<Path, Integer>> g2 = new DirectedSparseMultigraph<>();
		for (Node n : c.objects) {
			for (Pair<Object, Object> o : i.data.get(n.string)) {
				g2.addVertex(new Pair<>(n, o.first));
			}			
		}
		
		int j = 0;
		for (Pair<Node, Object> x : g2.getVertices()) {
			for (Pair<Node, Object> y : g2.getVertices()) {
				Set<Arr<Node, Path>> h = c.hom(x.first, y.first);
				for (Arr<Node, Path> arr : h) {
					if (c.isId(arr)) {
						continue;
					}
					if (doLookup(i, arr.arr, x.second, y.second)) {
						g2.addEdge(new Pair<>(arr.arr, j++), x, y);
					}
				}
			}
		}

		return g2;
	}

	
	private static boolean doLookup(Instance i, Path arr, Object x1,
			Object x2) {
		for (Pair<Object, Object> y : i.evaluate(arr)) {
			if (y.first.equals(x1) && y.second.equals(x2)) {
				return true;
			}
		}
		return false;
	}

	public static JPanel doView(Graph<Pair<Node, Object>, Pair<Path, Integer>> sgv) {
		// Layout<V, E>, BasicVisualizationServer<V,E>
		 Layout<Pair<Node, Object>, Pair<Path, Integer>> layout = new FRLayout<>(sgv);
	//	Layout<Pair<Node, Object>, Pair<Path, Integer>> layout = new ISOMLayout<>(sgv);
		// Layout<String, String> layout = new CircleLayout(sgv);
		layout.setSize(new Dimension(600, 400));
		// BasicVisualizationServer<String, String> vv = new
		// BasicVisualizationServer<String, String>(
		// layout);
		VisualizationViewer<Pair<Node, Object>, Pair<Path, Integer>> vv = new VisualizationViewer<>(
				layout);
		vv.setPreferredSize(new Dimension(600, 400));
		// Setup up a new vertex to paint transformer...
//		Transformer<String, Paint> vertexPaint = new Transformer<String, Paint>() {
//			public Paint transform(String i) {
//				if (!isAttribute(i)) {
//					return env.colors.get(name0);
//				} else {
//					return UIManager.getColor("Panel.background");
//				}
//			}
//		};
		DefaultModalGraphMouse<String, String> gm = new DefaultModalGraphMouse<>();
		gm.setMode(ModalGraphMouse.Mode.TRANSFORMING);
		vv.setGraphMouse(gm);
		gm.setMode(Mode.PICKING);
		// Set up a new stroke Transformer for the edges
		//float dash[] = { 1.0f };
		//final Stroke edgeStroke = new BasicStroke(0.5f, BasicStroke.CAP_BUTT,
		//		BasicStroke.JOIN_MITER, 10.0f, dash, 10.0f);
		// Transformer<String, Stroke> edgeStrokeTransformer = new
		// Transformer<String, Stroke>() {
		// public Stroke transform(String s) {
		// return edgeStroke;
		// }
		// };
	//	final Stroke bs = new BasicStroke();
//		Transformer<String, Stroke> edgeStrokeTransformer = new Transformer<String, Stroke>() {
//			public Stroke transform(String s) {
//				if (isAttribute(s)) {
//					return edgeStroke;
//				}
//				return bs;
//			}
//		};
//		vv.getRenderContext().setVertexFillPaintTransformer(vertexPaint);
//		vv.getRenderContext().setEdgeStrokeTransformer(edgeStrokeTransformer);
//		vv.getRenderContext().setVertexLabelTransformer(
	//			new ToStringLabeller<String>());
		vv.getRenderContext().setEdgeLabelTransformer(
				new ToStringLabeller<Pair<Path, Integer>>() {

					@Override
					public String transform(Pair<Path, Integer> t) {
						return t.first.toString();
					}

				});
		// new ToStringLabeller<String>());
		// vv.getRenderer().getVertexRenderer().
		// vv.getRenderContext().setLabelOffset(20);
		// vv.getRenderer().getVertexLabelRenderer().setPosition(Position.CNTR);

		vv.getRenderContext().setVertexLabelTransformer(
				new ToStringLabeller<Pair<Node, Object>>() {

					@Override
					public String transform(Pair<Node, Object> t) {
						return t.second.toString();
					}

				});

		return vv;
	}

	public static JPanel makePanel(Instance i) throws FQLException {
		Graph<Pair<Node, Object>, Pair<Path, Integer>> g = build(i);
		if (g.getVertexCount() == 0) {
			return new JPanel();
		}
		return doView(g);
	}

}
