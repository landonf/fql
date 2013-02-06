package fql;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Paint;
import java.awt.event.MouseListener;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.picking.PickedInfo;
import edu.uci.ics.jung.visualization.renderers.DefaultEdgeLabelRenderer;
import edu.uci.ics.jung.visualization.renderers.VertexLabelRenderer;

public class Instance implements Viewable<Instance> {

	public void conformsTo(Signature s) throws FQLException {
		for (Node n : s.nodes) {
			Set<Pair<String, String>> i = data.get(n.string);
			for (Pair<String, String> p : i) {
				if (!p.first.equals(p.second)) {
					throw new FQLException("Not reflexive: " + s.name0 + " in "
							+ s + " and " + this);
				}
			}
			// identity
		}
		for (Edge e : s.edges) {
			Set<Pair<String, String>> i = data.get(e.name);
			for (Pair<String, String> p1 : i) {
				for (Pair<String, String> p2 : i) {
					if (p1.first.equals(p2.first)) {
						if (!p2.second.equals(p2.second)) {
							throw new FQLException("Not functional: " + s.name0
									+ " in " + s + " and " + this);
						}
					}
				}
				// functional

				if (!contained(p1.first, data.get(e.source.string))) {
					throw new FQLException("Domain has non foreign key: "
							+ s.name0 + " in " + s + " and " + this);
				}
				if (!contained(p1.second, data.get(e.target.string))) {
					throw new FQLException("Range has non foreign key: "
							+ p1.second + ", " + data.get(e.target.string)
							+ ", " + s.name0 + " in " + s + " and " + this);
				}
			}
		}
		for (Eq eq : s.eqs) {
			Set<Pair<String, String>> lhs = evaluate(eq.lhs);
			Set<Pair<String, String>> rhs = evaluate(eq.rhs);
			if (!lhs.equals(rhs)) {
				throw new FQLException("Violates constraints: " + s.name0
						+ " in " + s + " and " + this);
			}
		}
		
		//toFunctor();
	}

	private Set<Pair<String, String>> evaluate(Path p) {
		Set<Pair<String, String>> x = data.get(p.source.string);
		if (x == null) {
			System.out.println("Couldnt find " + p.source.string);
		}
		for (Edge e : p.path) {
			if (data.get(e.name) == null) {
				System.out.println("Couldnt find " + e.name);
			}

			x = compose(x, data.get(e.name));
		}
		return x;
	}

	private static Set<Pair<String, String>> compose(
			Set<Pair<String, String>> x, Set<Pair<String, String>> y) {
		Set<Pair<String, String>> ret = new HashSet<Pair<String, String>>();

		for (Pair<String, String> p1 : x) {
			for (Pair<String, String> p2 : y) {
				if (p1.second.equals(p2.first)) {
					Pair<String, String> p = new Pair<String, String>(p1.first,
							p2.second);
					ret.add(p);
				}
			}
		}
		return ret;
	}

	private boolean contained(String s, Set<Pair<String, String>> set) {
		for (Pair<String, String> p : set) {
			if (p.first.equals(s) && p.second.equals(s)) {
				return true;
			}
		}
		return false;
	}

	Map<String, Set<Pair<String, String>>> data;

	Signature thesig;

	public Instance(String n, Signature thesig,
			List<Pair<String, List<Pair<String, String>>>> data)
			throws FQLException {
		this.data = new HashMap<String, Set<Pair<String, String>>>();
		for (Pair<String, List<Pair<String, String>>> p : data) {
			this.data.put(p.first, new HashSet<Pair<String, String>>(p.second));
		}
		this.thesig = thesig;
		if (!typeCheck(thesig)) {
			throw new FQLException("Type-checking failure " + n);
		}
		conformsTo(thesig);
	}

	public Instance(String name, Query thequery, Instance theinstance)
			throws FQLException {
		if (!thequery.getSource().equals0(theinstance.thesig)) {
			throw new FQLException("Incompatible types. Expected "
					+ thequery.getSource() + " received " + theinstance.thesig);
		}
		thesig = thequery.getTarget();
		data = thequery.eval(theinstance);
		conformsTo(thesig);

	}

	public Instance(String name, Mapping m, Instance i, String type)
			throws FQLException {
		if (type.equals("delta")) {
			if (!m.target.equals0(i.thesig)) {
				throw new FQLException("Incompatible types. Expected "
						+ m.target + " received " + i.thesig);
			}
			thesig = m.source;
			data = m.evalDelta(i);
			conformsTo(thesig);

		} else if (type.equals("sigma")) {
			if (!m.source.equals0(i.thesig)) {
				throw new FQLException("Incompatible types. Expected "
						+ m.source + " received " + i.thesig);
			}
			thesig = m.target;
			data = m.evalSigma(i);

			conformsTo(thesig);

		} else if (type.equals("pi")) {
			if (!m.source.equals0(i.thesig)) {
				throw new FQLException("Incompatible types. Expected "
						+ m.source + " received " + i.thesig);
			}
			thesig = m.target;
			data = m.evalPi(i);
			conformsTo(thesig);

		} else {
			throw new FQLException("Unknown type " + type);
		}
	}

	private boolean typeCheck(Signature thesig2) {
		for (String s : data.keySet()) {
			if (!thesig2.contains(s)) {
				return false;
			}
		}
		for (String s : thesig2.all()) {
			if (null == data.get(s)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((data == null) ? 0 : data.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Instance other = (Instance) obj;
		if (data == null) {
			if (other.data != null)
				return false;
		} else if (!data.equals(other.data))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer("{ ");

		boolean first = true;
		for (String k : data.keySet()) {
			Set<Pair<String, String>> v = data.get(k);
			if (!first) {
				sb.append("; ");
			}
			first = false;
			sb.append(k);
			sb.append(" = { ");
			sb.append(printSet(v));
			sb.append(" }");
		}

		sb.append(" }");
		return sb.toString();

	}

	private String printSet(Set<Pair<String, String>> v) {
		StringBuffer sb = new StringBuffer();
		boolean first = true;
		for (Pair<String, String> p : v) {
			if (!first) {
				sb.append(", ");
			}
			first = false;
			sb.append("(");
			sb.append(p.first);
			sb.append(",");
			sb.append(p.second);
			sb.append(")");
		}
		return sb.toString();
	}

	@Override
	public JPanel view() throws FQLException {
		List<JPanel> panels = new LinkedList<JPanel>();
		// Map<String, Set<Pair<String,String>>> data;
		LinkedList<String> sorted = new LinkedList<String>(data.keySet());
		Collections.sort(sorted, new Comparator<String>()
                {
            public int compare(String f1, String f2)
            {
                return f1.toString().compareTo(f2.toString());
            }        
        });
		for (String k : sorted) {
			Set<Pair<String, String>> xxx = data.get(k);
			List<Pair<String, String>> table = new LinkedList<>(xxx);
			Collections.sort(table, new Comparator<Pair<String, String>>()
	                {
	            public int compare(Pair<String,String> f1, Pair<String,String> f2)
	            {
	                return f1.first.toString().compareTo(f2.first.toString());
	            }        
	        });

			String[][] arr = new String[table.size()][2];
			int i = 0;
			for (Pair<String, String> p : table) {
				arr[i][0] = p.first.trim();
				arr[i][1] = p.second.trim();
				i++;
			}
			Pair<String, String> cns = thesig.getColumnNames(k);
			JTable t = new JTable(arr, new Object[] { cns.first, cns.second });
			t.setRowSelectionAllowed(false);
			t.setColumnSelectionAllowed(false);
			MouseListener[] listeners = t.getMouseListeners();
			for (MouseListener l : listeners) {
				t.removeMouseListener(l);
			}
			t.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
			JPanel p = new JPanel(new GridLayout(1, 1));
			p.add(new JScrollPane(t));
			p.setBorder(BorderFactory.createTitledBorder(
					BorderFactory.createEmptyBorder(2, 2, 2, 2), k));
			panels.add(p);
			p.setSize(60, 60);
		}

		int x = (int) Math.ceil(Math.sqrt(panels.size()));
		JPanel panel = new JPanel(new GridLayout(x, x));
		for (JPanel p : panels) {
			panel.add(p);
		}
		panel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		return panel;
	}

	@Override
	public JPanel join() throws FQLException {
		// Map<String, Set<Pair<String,String>>> data;
		
		Map<String, Map<String, Set<Pair<String, String>>>> joined 
		= new HashMap<>();
		Map<String, Set<Pair<String, String>>> nd
		= new HashMap<>();
		
		List<String> names = new LinkedList<>();
		
		for (Node n : thesig.nodes) {
			nd.put(n.string, data.get(n.string));
			joined.put(n.string, new HashMap<String, Set<Pair<String, String>>>());
			names.add(n.string);
		}
		
		for (Edge e : thesig.edges) {
			joined.get(e.source.string).put(e.name, data.get(e.name));
	//		names.add(e.name);
		}
		
//		System.out.println(joined);
	//	System.out.println(nd);
		
		Comparator<String> strcmp = new Comparator<String>()  {
	        public int compare(String f1, String f2) {
	                return f1.compareTo(f2);
	            }        
	        };
		Collections.sort(names, strcmp);

		List<JPanel> pans = new LinkedList<>();
		for (String name : names) {
//			System.out.println("Name " + name);
			Map<String, Set<Pair<String, String>>> m = joined.get(name);
	//		System.out.println("m " + m);
			Set<Pair<String, String>> ids = nd.get(name);
		//	System.out.println("ids " + ids);
			String[][] arr = new String[ids.size()][m.size() + 1];
			Set<String> cols = m.keySet();
	//		System.out.println("cols " + cols);
			List<String> cols2 = new LinkedList<>(cols);
			Collections.sort(cols2, strcmp);
			cols2.add(0, "ID");
	//		System.out.println("cols2 " + cols2);
			Object[] cols3 = cols2.toArray();
	//		System.out.println("cols3 " + cols3);
			
			int i = 0;
			for (Pair<String, String> id : ids) {
//				System.out.println("id " + id);
				arr[i][0] = id.first;
//				System.out.println(" i " + i + " j " + 0 + " val " + arr[i][0]);

				int j = 1;
				for (String col : cols2) {
					if (col.equals("ID")) {
						continue;
					}
				//	System.out.println("col " + col);
					Set<Pair<String, String>> coldata = m.get(col);
					for (Pair<String, String> p : coldata) {
				//		System.out.println("p " + p);
						if (p.first.equals(id.first)) {
							arr[i][j] = p.second;
//							System.out.println(" i " + i + " j " + j + " val " + arr[i][j]);
							break;
						}
					}
					j++;
				}
				i++;
			}
			
			Arrays.sort(arr, new Comparator<String[]>() {

				@Override
				public int compare(String[] o1, String[] o2) {
					return o1[0].compareTo(o2[0]);
				}
				
			});
			
			JTable t = new JTable(arr, cols3);
			JPanel p = new JPanel(new GridLayout(1,1));
			//p.add(t);
			p.add(new JScrollPane(t));
			p.setMaximumSize(new Dimension(200,200));
			p.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), name));
			pans.add(p);
		}
	
		int x = (int) Math.ceil(Math.sqrt(pans.size()));
		JPanel panel = new JPanel(new GridLayout(x, x));
		for (JPanel p : pans) {
			panel.add(p);
		}
		panel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		return panel;		
	}

	
	@Override
	public JPanel text() {
		// String s = toString().replace('{', ' ').replace('}', ' ').trim();
		String[] t = toString().split(";");
		String ret = "";
		for (String a : t) {
			ret += (a.trim() + ";\n\n");
		}

		JTextArea ta = new JTextArea(ret);
		JPanel tap = new JPanel(new GridLayout(1, 1));
		ta.setBorder(BorderFactory.createEmptyBorder());
		//
		tap.setBorder(BorderFactory.createEmptyBorder());
		ta.setWrapStyleWord(true);
		ta.setLineWrap(true);
		JScrollPane xxx = new JScrollPane(ta);
		// xxx.setBorder(BorderFactory.createEmptyBorder());
		//
		tap.add(xxx);
		// tap.setSize(600, 600);

		return tap;
	}

	@Override
	public String plan() {
		// TODO plan for instance
		return "todo";
	}

	@Override
	public boolean equals0(Instance view2) {
		return equals(view2);
	}

	@Override
	public boolean iso(Instance i) {
		return false;
		// TODO iso instances
		// if (data.size() != i.data.size()) {
		// return false;
		// }
		// Map<String, Pair<String, String>> sub
		// for (String table : data.keySet()) {
		// if (!i.data.containsKey(table)) {
		// return false;
		// }
		//
		//
		// }
	}

	// TODO isHomo
	public static boolean isHomo(Map<String, Map<String, String>> subst) {
		return false;
	}

	@Override
	public String isos(Instance view) {
		// TODO isos instances
		return "todo - isos";
	}

	@Override
	public String homos(Instance view) {
		// TODO morphs instances
		return "todo - homos";
	}

	@Override
	public JPanel pretty() throws FQLException {
		return makeViewer();
	}

	@Override
	public String type() {
		return "instance";
	}

	public Graph<String, String> build() {
		// Graph<V, E> where V is the type of the vertices

		Graph<String, String> g2 = new DirectedSparseMultigraph<String, String>();
		for (Node n : thesig.nodes) {
			g2.addVertex(n.string);
		}

		for (Edge e : thesig.edges) {
			g2.addEdge(e.name, e.source.string, e.target.string);
		}

		return g2;
	}

	public JPanel makeViewer() {
		Graph<String, String> g = build();
		return doView(g);
	}

	public JPanel doView(Graph<String, String> sgv) {
		// Layout<V, E>, BasicVisualizationServer<V,E>
		// Layout<String, String> layout = new KKLayout(sgv);

		// Layout<String, String> layout = new FRLayout(sgv);
		// Layout<String, String> layout = new ISOMLayout<String,String>(sgv);
		Layout<String, String> layout = new CircleLayout<>(sgv);
		layout.setSize(new Dimension(600, 450));
		VisualizationViewer<String, String> vv = new VisualizationViewer<String, String>(layout);
		vv.setPreferredSize(new Dimension(600, 450));
		//vv.getRenderContext().setEdgeLabelRenderer(new MyEdgeT());
		// Setup up a new vertex to paint transformer...
		Transformer<String, Paint> vertexPaint = new Transformer<String, Paint>() {
			public Paint transform(String i) {
				return Environment.colors.get(thesig.name0);
			}
		};
		DefaultModalGraphMouse<String, String> gm = new DefaultModalGraphMouse<>();
      //  gm.setMode(ModalGraphMouse.Mode.TRANSFORMING);
        vv.setGraphMouse(gm);
        gm.setMode(Mode.PICKING);
//        gm.add(new AnnotatingGraphMousePlugin(vv.getRenderContext()) {
//
//		
//        	
//        }.);
               

		// Set up a new stroke Transformer for the edges
		// float dash[] = { 10.0f };
		// final Stroke edgeStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
		// BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f);
		// Transformer<String, Stroke> edgeStrokeTransformer = new
		// Transformer<String, Stroke>() {
		// public Stroke transform(String s) {
		// return edgeStroke;
		// }
		// };
		vv.getRenderContext().setVertexFillPaintTransformer(vertexPaint);
		// vv.getRenderContext().setEdgeStrokeTransformer(edgeStrokeTransformer);
		vv.getRenderContext().setVertexLabelRenderer(new MyVertexT()); // {
//
//			@Override
//			public <T> Component getVertexLabelRendererComponent(
//					JComponent arg0, Object arg1, Font arg2, boolean arg3,
//					T arg4) {
//				Set<Pair<String, String>> table = data.get(arg4);
//
//				String s = (String) arg4;
//				//s += " = {";
//				boolean b = false;
//				for (Pair<String, String> x : table) {
////					if (b) {
////						s += "\n ";
////					}
//					s += "\n";
//					s += x.first;
//					b = true;
//				}
//				s += "}";
//				JTextArea x = new JTextArea(s);
//				// x.setFont(new Font("Arial", 8, Font.PLAIN));
//				return x;
//			}
//
//		});
		// vv.getRenderContext().setVertexLabelTransformer(new
		// ToStringLabeller());
		
//				new MyEdgeT()); // {

//		vv.getRenderContext().setEdgeLabelTransformer(new MyEdgeT2(vv.getPickedEdgeState()));
		//vv.getRenderContext().setVertexLabelTransformer(new MyVertexT(vv.getPickedVertexState()));
		// vv.getRenderer().getVertexRenderer().
		vv.getRenderContext().setLabelOffset(20);
		vv.getRenderContext().setEdgeLabelTransformer(new ToStringLabeller());
//		vv.getRenderContext().getEdgeLabelRenderer().
		// vv.getRenderer().getVertexLabelRenderer().setPosition(Position.CNTR);

		return vv;
	}
	
	private class MyVertexT implements VertexLabelRenderer{
	  
	    public MyVertexT(  ){
	    }

	    @Override
	    public <T> Component getVertexLabelRendererComponent(
				JComponent arg0, Object arg1, Font arg2, boolean arg3,
				T arg4) {
	    	if (arg3) {
//			    	 if (pi.isPicked((String) arg4)) {
			    		 Vector ld = new  Vector();

			    		 Set<Pair<String, String>> table = data.get(arg4);
			    		 

			    		 String s = (String) arg4;
			    		 boolean b = false;
			    		 s += " = ";
			    		 for (Pair<String, String> x : table) {
			    			 if (b) {
			    				 s += ", ";
			    			 }
			    			 b = true;
			    			 s += x.first;
			    			 ld.add(x.first);
			    		 }
			    		 JList jl = new JList(ld);
			    		 JPanel p = new JPanel(new GridLayout(1,1));
			    		 p.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), (String) arg4));
			    		 p.add(new JScrollPane(jl));
			    		// p.add(jl);
			    		 
//					JLabel x = new JLabel(s);
					// x.setFont(new Font("Arial", 8, Font.PLAIN));
		//			return x;
//					return new JTextArea(s);
			    		 return p;
		        }
		        else {
		          return new JLabel((String)arg4);
		        }
		    }
	    }
	
	private class MyEdgeT extends DefaultEdgeLabelRenderer {
	   // private final PickedInfo<String> pi;

	    public MyEdgeT(){
	    	super(Color.GRAY, false);
	      //  this.pi = pi;
	    }

	    @Override
	    public <T> Component getEdgeLabelRendererComponent(
				JComponent arg0, Object arg1, Font arg2, boolean arg3,
				T arg4) {
	    	if (true) throw new RuntimeException();
	    	if (arg3) {
//			    	 if (pi.isPicked((String) arg4)) {
			    		 Vector ld = new  Vector();

			    		 Set<Pair<String, String>> table = data.get(arg4);
			    		 

			    		 String s = (String) arg4;
			    		 boolean b = false;
			    		 s += " = ";
			    		 for (Pair<String, String> x : table) {
			    			 if (b) {
			    				 s += ", ";
			    			 }
			    			 b = true;
			    			 s += x.first;
			    			 ld.add(x.first);
			    		 }
			    		 JList jl = new JList(ld);
			    		 JPanel p = new JPanel(new GridLayout(1,1));
			    		 p.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), (String) arg4));
			    		 p.add(new JScrollPane(jl));
			    		// p.add(jl);
			    		 
//					JLabel x = new JLabel(s);
					// x.setFont(new Font("Arial", 8, Font.PLAIN));
		//			return x;
//					return new JTextArea(s);
			    		// return p;
			    		 return new JLabel("ZZZZ");
		        }
		        else {
		        	return new JLabel("HHHH");
		         // return new JLabel("ZZZZZ" + (String)arg4);
		        }
		    }

	    boolean b = false;
		@Override
		public boolean isRotateEdgeLabels() {
			return b;
		}

		@Override
		public void setRotateEdgeLabels(boolean arg0) {
			this.b = arg0;
		}
	    }
	
	private  class MyEdgeT2 implements Transformer<String,String>{
	    private final PickedInfo<String> pi;

	    public MyEdgeT2( PickedInfo<String> pi ){
	        this.pi = pi;
	    }

	    @Override
	    public String transform(String t) {
	        if (pi.isPicked(t)) {
				Set<Pair<String, String>> table = data.get(t);

				String s = t;
				boolean b = false;
				s += " = ";
				for (Pair<String, String> x : table) {
					if (b) {
						s += ", ";
					}
					b = true;
					s += x.first;
					s += " -> ";
					s += x.second;
				}
//				JLabel x = new JLabel(s);
				// x.setFont(new Font("Arial", 8, Font.PLAIN));
	//			return x;
				return s;

	        }
	        else {
	          return t;
	        }
	    }
	}

	
	@SuppressWarnings("unchecked")
	static <K, V> List<Map<K, V>> substitutions(List<K> a, List<V> b) {
		// List<Map<String, String>> ret = new LinkedList<>();
		//
		// Map<String, Set<String>> x = new HashMap<>();
		// for (String s : S) {
		// Set<String> y = new HashSet<>();
		// for (String t : T) {
		// y.add(t);
		// }
		// x.put(s, y);
		// }

		List<V>[] x = new List[a.size()];

		for (int ax = 0; ax < a.size(); ax++) {
			x[ax] = new LinkedList<V>();
			for (int bx = 0; bx < b.size(); bx++) {
				x[ax].add(b.get(bx));
			}
			if (x[ax].size() == 0) {
				return new LinkedList<Map<K, V>>();
			}
		}

		int[] counters = new int[a.size() + 1];

		List<Map<K, V>> ret = new LinkedList<>();
		for (;;) {
			Map<K, V> m = new HashMap<>();
			for (int v = 0; v < a.size(); v++) {
				K p = a.get(v);
				// System.out.println("Trying to get " + counters[v] + " a is "
				// + p + " from " + x[v] + " wlen " + x[v].size());
				V q = x[v].get(counters[v]);
				m.put(p, q);
			}
			ret.add(m);
			counters[0]++;

			for (int u = 0; u < a.size(); u++) {
				if (counters[u] == x[u].size()) {
					counters[u] = 0;
					counters[u + 1]++;
				}
			}

			if (counters[a.size()] >= 1) {
				break;
			}
		}

		return ret;
	}


//	private static List<Pair<String,String>> dupl(Map<String, String> map) {
//		List<Pair<String,String>> ret = new LinkedList<Pair<String,String>>();
//		for (String k : map.keySet()) {
//			ret.add(new Pair<>(k,map.get(k)));
//		}
//		return ret;	
//	}
//
//	private static List<Pair<String,String>> dupl(Set<String> set) {
//		List<Pair<String,String>> ret = new LinkedList<Pair<String,String>>();
//		for (String s : set) {
//			ret.add(new Pair<>(s,s));
//		}
//		return ret;		
//	}

	public static Instance terminal(Signature s) throws FQLException {
		List<Pair<String, List<Pair<String, String>>>> ret = new LinkedList<>();

		int i = 0;
		Map<Node, String> map = new HashMap<>();
		for (Node node : s.nodes) {
			List<Pair<String, String>> tuples = new LinkedList<>();
			String g = Integer.toString(i);
			tuples.add(new Pair<>(g, g));
			ret.add(new Pair<>(node.string, tuples));
			map.put(node, g);
			i++;
		}

		for (Edge e : s.edges) {
			List<Pair<String, String>> tuples = new LinkedList<>();
			tuples.add(new Pair<>(map.get(e.source.string), map
					.get(e.target.string)));
			ret.add(new Pair<>(e.name, tuples));
		}

		return new Instance(s.name0 + "_terminal", s, ret);
	}

	public Inst<String, List<List<String>>, String, String> toFunctor() throws FQLException {
		FinCat<String, List<List<String>>> cat = thesig.toCategory().first;
		
		Map<String, Set<Value<String, String>>> objM = new HashMap<>();
		for (String obj : cat.objects) {
			objM.put(obj, conv(data.get(obj)));
		}
		
		Map<Arr<String, List<List<String>>>, Map<Value<String, String>, Value<String, String>>> arrM = new HashMap<>();
		for (Arr<String, List<List<String>>> arr : cat.arrows) {
			List<String> es = arr.arr.get(0);
			
			String h = es.get(0);
			Set<Pair<String, String>> h0 = data.get(h);
			for (int i = 1; i < es.size(); i++) {
				h0 = compose(h0, data.get(es.get(i)));
			}
			Map<Value<String, String>, Value<String, String>> xxx = FDM.degraph(h0);
			arrM.put(arr, xxx);
		}
		
		return new Inst<String, List<List<String>>, String, String>(objM, arrM, cat);
	}

	private Set<Value<String,String>> conv(Set<Pair<String, String>> set) {
		Set<Value<String,String>> ret = new HashSet<>();
		for (Pair<String, String> p : set) {
			ret.add(new Value<String,String>(p.first));
		}
		return ret;
	}

}
