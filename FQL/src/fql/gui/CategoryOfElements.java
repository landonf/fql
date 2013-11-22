package fql.gui;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Paint;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.algorithms.layout.FRLayout;
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
import fql.DEBUG;
import fql.FQLException;
import fql.Pair;
import fql.cat.Arr;
import fql.cat.FinCat;
import fql.decl.Attribute;
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
	
	private static Pair<Graph<Pair<Node, Object>, Pair<Path, Integer>>, HashMap<Pair<Node, Object>, Map<Attribute<Node>, Object>>>  build(Instance i) throws FQLException {
		FinCat<Node, Path> c = i.thesig.toCategory2().first;
		HashMap<Pair<Node, Object>, Map<Attribute<Node>, Object>> map = new HashMap<>();

		Graph<Pair<Node, Object>, Pair<Path, Integer>> g2 = new DirectedSparseMultigraph<>();
		for (Node n : c.objects) {
			for (Pair<Object, Object> o : i.data.get(n.string)) {
				Pair<Node, Object> xx = new Pair<>(n, o.first);
				g2.addVertex(xx);
				
				List<Attribute<Node>> attrs = i.thesig.attrsFor(n);
				Map<Attribute<Node>, Object> m = new HashMap<>();
				for (Attribute<Node> attr : attrs) {
					Object a = lookup(i.data.get(attr.name), o.first);
					m.put(attr,  a);
				}
				map.put(xx, m);
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
					if (!DEBUG.ALL_GR_PATHS && arr.arr.path.size() != 1) {
						continue;
					}
					if (doLookup(i, arr.arr, x.second, y.second)) {
						g2.addEdge(new Pair<>(arr.arr, j++), x, y);
					}
				}
			}
		}

		return new Pair<>(g2, map);
	}

	
	private static Object lookup(Set<Pair<Object, Object>> set, Object first) {
		for (Pair<Object, Object> p : set) {
			if (p.first.equals(first)) {
				return p.second;
			}
		}
		throw new RuntimeException();
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

	public static JPanel doView(final Instance inst, Graph<Pair<Node, Object>, Pair<Path, Integer>> sgv, HashMap<Pair<Node, Object>, Map<Attribute<Node>, Object>> map0) {
	//	HashMap<Pair<Node, Object>,String> map = new HashMap<>();
		JPanel cards = new JPanel(new CardLayout());

		
		// Layout<V, E>, BasicVisualizationServer<V,E>
		 Layout<Pair<Node, Object>, Pair<Path, Integer>> layout = new FRLayout<>(sgv);
	//	Layout<Pair<Node, Object>, Pair<Path, Integer>> layout = new ISOMLayout<>(sgv);
		// Layout<String, String> layout = new CircleLayout(sgv);
		//layout.setSize(new Dimension(600, 400));
		// BasicVisualizationServer<String, String> vv = new
		// BasicVisualizationServer<String, String>(
		// layout);
		VisualizationViewer<Pair<Node, Object>, Pair<Path, Integer>> vv = new VisualizationViewer<>(
				layout);
		//vv.setPreferredSize(new Dimension(600, 400));
		// Setup up a new vertex to paint transformer...
		Transformer<Pair<Node, Object>, Paint> vertexPaint = new Transformer<Pair<Node, Object>, Paint>() {
			public Paint transform(Pair<Node, Object> i) {
				return inst.thesig.colors.get(i.first.string);
			}
		};
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
		vv.getRenderContext().setVertexLabelRenderer(new MyVertexT(cards));
	//	final Stroke bs = new BasicStroke();
//		Transformer<String, Stroke> edgeStrokeTransformer = new Transformer<String, Stroke>() {
//			public Stroke transform(String s) {
//				if (isAttribute(s)) {
//					return edgeStroke;
//				}
//				return bs;
//			}
//		};
		vv.getRenderContext().setVertexFillPaintTransformer(vertexPaint);
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

		JPanel ret = new JPanel(new GridLayout(1,1));
		JSplitPane pane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		
		for (Pair<Node, Object> n : sgv.getVertices()) {
			Map<Attribute<Node>, Object> s = map0.get(n);
			Object[] columnNames = new Object[s.keySet().size()];
			Object[][] rowData = new Object[1][s.keySet().size()];

			int i = 0;
//			for (Pair<Node, Object> k : map0.keySet()) {
	//			Map<Attribute<Node>, Object> v = ma;
				for (Attribute<Node> a : s.keySet()) {
					columnNames[i] = a.name;
					rowData[0][i] = s.get(a);
					i++;
				}

			//}
			JPanel p = new JPanel(new GridLayout(1,1));
			JTable table = new JTable(rowData, columnNames);
			JScrollPane jsp = new JScrollPane(table);
			p.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), "Attributes for " + n.second));

			p.add(jsp);
			cards.add(p, n.second.toString());
		}
		cards.add(new JPanel(), "blank");
		CardLayout cl = (CardLayout)(cards.getLayout());
		cl.show(cards, "blank");

		pane.add(new GraphZoomScrollPane(vv));
		pane.add(cards);
		pane.setResizeWeight(1.0d);
		ret.add(pane);
//		cards.setPreferredSize(new Dimension(400,100));
		
		return ret;
	}

	public static JPanel makePanel(Instance i) throws FQLException {
				
		try {
		
			Pair<Graph<Pair<Node, Object>, Pair<Path, Integer>>, HashMap<Pair<Node, Object>, Map<Attribute<Node>, Object>>> g = build(i);
			if (g.first.getVertexCount() == 0) {
				return new JPanel();
			}
			return doView(i, g.first, g.second);

		} catch (FQLException e) {
			JPanel p = new JPanel(new GridLayout(1,1));
			JTextArea a = new JTextArea(e.getMessage());
			p.add(new JScrollPane(a));
			return p;
		}

		
		
	}
	
	private static class MyVertexT implements VertexLabelRenderer {

		JPanel cards;
		public MyVertexT(JPanel cards) {
			this.cards = cards;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> Component getVertexLabelRendererComponent(JComponent arg0,
				Object arg1, Font arg2, boolean arg3, T arg4) {
			Pair<Node, Object> p = (Pair<Node, Object>) arg4;
			if (arg3) {
				CardLayout c = (CardLayout) cards.getLayout();
						c.show(cards, p.second.toString());
			}

			return new JLabel(p.second.toString());

		}
	}

}
