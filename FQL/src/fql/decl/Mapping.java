package fql.decl;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Paint;
import java.awt.Stroke;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.UIManager;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import fql.DEBUG;
import fql.FQLException;
import fql.Fn;
import fql.Pair;
import fql.Triple;
import fql.cat.Arr;
import fql.cat.FinCat;
import fql.cat.FinFunctor;
import fql.parse.PrettyPrinter;
import fql.sql.EmbeddedDependency;
import fql.sql.PSM;
import fql.sql.PSMGen;

/**
 * 
 * @author ryan
 * 
 *         Implementation of signature morphisms
 */
public class Mapping {

	boolean ALLOW_WF_CHECK = true;
	public boolean flag = false;
/*
	public Mapping(Signature src, Signature dst, LinkedHashMap<Node, Node> nm,
			LinkedHashMap<Edge, Path> em,
			LinkedHashMap<Attribute<Node>, Attribute<Node>> am) {
		ALLOW_WF_CHECK = false;
		// this.name = name;
		this.source = src;
		this.target = dst;
		this.nm = nm;
		this.em = em;
		this.am = am;
	}
*/
	public Mapping(boolean b, Signature src, Signature dst, LinkedHashMap<Node, Node> nm,
			LinkedHashMap<Edge, Path> em,
			LinkedHashMap<Attribute<Node>, Attribute<Node>> am) throws FQLException {
		ALLOW_WF_CHECK = b;
		// this.name = name;
		this.source = src;
		this.target = dst;
		this.nm = nm;
		this.em = em;
		this.am = am;
		validate();
	}
	
	public void validate() throws FQLException {
		for (Attribute<Node> a : source.attrs) {
			Attribute<Node> b = am.get(a);
			if (b == null) {
				throw new FQLException("Mapping " /* + name */
						+ " does not map attribute " + a);
			}
			if (!a.target.equals(b.target)) {
				throw new FQLException("Mapping " /* + name */
						+ " does not preserve typing on " + a + " and " + b);
			}
			if (!b.source.equals(nm.get(a.source))) {
				throw new FQLException("Mapping does not preserve source on "
						+ nm.get(a.source) + " and " + b.source);
			}
		}

		// should be checked by knuth-bendix

		if (!DEBUG.debug.ALLOW_INFINITES && !flag) {

			Triple<FinFunctor<Node, Path, Node, Path>, Pair<FinCat<Node, Path>, Fn<Path, Arr<Node, Path>>>, Pair<FinCat<Node, Path>, Fn<Path, Arr<Node, Path>>>> zzz = toFunctor2();

			for (Eq x : source.eqs) {
				appy(x.lhs);

				if (!zzz.third.second.of(appy(x.lhs)).equals(
						zzz.third.second.of(appy(x.rhs)))) {
					throw new FQLException("On " + this + "\n\n equivalence "
							+ x + " not respected on \n\n" + appy(x.lhs)
							+ "\nand\n" + appy(x.rhs)
							+ "\n\ntransformed lhs is "
							+ zzz.third.second.of(appy(x.lhs))
							+ "\n\n transformed rhs is "
							+ zzz.third.second.of(appy(x.rhs)) + "\n\nschemas:"
							+ source + " and target " + target);

				}
			}
		}

	}

	public LinkedHashMap<Node, Node> nm = new LinkedHashMap<>();
	public LinkedHashMap<Edge, Path> em = new LinkedHashMap<>();
	public LinkedHashMap<Attribute<Node>, Attribute<Node>> am = new LinkedHashMap<>();
	public Signature source;
	public Signature target;

	// public String name;
	// public boolean isId = false;
	/*
	 * public Mapping(String name, Environment env, MappingDecl md) throws
	 * FQLException { this.name = name; switch (md.kind) { case COMPOSE: Mapping
	 * m1 = env.mappings.get(md.m1); Mapping m2 = env.mappings.get(md.m2);
	 * 
	 * if (m1 == null) { throw new FQLException("For " + name +
	 * ", cannot find mapping " + md.m1); } if (m2 == null) { throw new
	 * FQLException("For " + name + ", cannot find mapping " + md.m2); } if
	 * (!m2.target.equals(m1.source)) { throw new FQLException("Ill-typed: " +
	 * md); } this.source = m2.source; this.target = m1.target; for (Node k :
	 * m1.source.nodes) { Node v = m1.nm.get(k); nm.put(k, m2.nm.get(v)); } for
	 * (Edge k : m1.source.edges) { Path v = m1.em.get(k); Path p0 = expand(v,
	 * m2.nm, m2.em); em.put(k, p0); }
	 * 
	 * break; case ID: Signature s = env.getSchema(md.schema); if
	 * (!(md.schema.equals(md.source) && md.schema.equals(md.target))) { throw
	 * new FQLException("Bad identity mapping : " + md.name); } identity(env,
	 * s); break; case MORPHISM: // Pair<List<Pair<String, String>>,
	 * List<Pair<String, String>>> xxx // = filter(md.objs);
	 * initialize(env.getSchema(md.source), env.getSchema(md.target), md.objs,
	 * md.atts, md.arrows); break; default: throw new RuntimeException(); }
	 * validate(); }
	 */

	private Pair<List<Pair<String, String>>, List<Pair<String, String>>> filter(
			List<Pair<String, String>> objs) throws FQLException {
		List<Pair<String, String>> ret = new LinkedList<>();
		List<Pair<String, String>> ret2 = new LinkedList<>();

		for (Pair<String, String> p : objs) {
			if (source.isAttribute(p.first) && target.isAttribute(p.second)) {
				ret.add(p);
			} else if (source.isNode(p.first) && target.isNode(p.second)) {
				ret2.add(p);
			} else {
				throw new FQLException("Bad mapping: " + p);
			}
		}

		return new Pair<>(ret2, ret);
	}

	/*
	 * private Path expand(Path v, Map<Node, Node> nm2, Map<Edge, Path> em2) {
	 * Node newhead = nm2.get(v.source); Node newtarget = nm2.get(v.target);
	 * List<Edge> newedges = new LinkedList<Edge>(); for (Edge e : v.path) {
	 * Path p = em2.get(e); newedges.addAll(p.path); newtarget = p.target; }
	 * return new Path(newhead, newtarget, newedges); }
	 */

	public Mapping(Environment env, Signature s) throws FQLException {
		identity(env, s);
		validate();
	}

	/**
	 * Constructs a new mapping.
	 */
	public Mapping(Signature source, Signature target,
			List<Pair<String, String>> objs, List<Pair<String, String>> atts,
			List<Pair<String, List<String>>> arrows) throws FQLException {
		initialize(source, target, objs, atts, arrows);
		validate();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (ALLOW_WF_CHECK ? 1231 : 1237);
		result = prime * result + ((am == null) ? 0 : am.hashCode());
		result = prime * result + ((em == null) ? 0 : em.hashCode());
		result = prime * result + ((nm == null) ? 0 : nm.hashCode());
		result = prime * result + ((source == null) ? 0 : source.hashCode());
		result = prime * result + ((target == null) ? 0 : target.hashCode());
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
		Mapping other = (Mapping) obj;
		if (ALLOW_WF_CHECK != other.ALLOW_WF_CHECK)
			return false;
		if (am == null) {
			if (other.am != null)
				return false;
		} else if (!am.equals(other.am))
			return false;
		if (em == null) {
			if (other.em != null)
				return false;
		} else if (!em.equals(other.em))
			return false;
		if (nm == null) {
			if (other.nm != null)
				return false;
		} else if (!nm.equals(other.nm))
			return false;
		if (source == null) {
			if (other.source != null)
				return false;
		} else if (!source.equals(other.source))
			return false;
		if (target == null) {
			if (other.target != null)
				return false;
		} else if (!target.equals(other.target))
			return false;
		return true;
	}

	public Mapping(boolean b, Signature source, Signature target,
			List<Pair<String, String>> objs, List<Pair<String, String>> atts,
			List<Pair<String, List<String>>> arrows) throws FQLException {
		this.flag = b;
		initialize(source, target, objs, atts, arrows);
		validate();
	}

	public Mapping(
			Signature source,
			Signature target,
			List<Pair<String, String>> objs,
			List<Pair<Pair<Pair<String, String>, String>, List<Pair<Pair<String, String>, String>>>> arrows)
			throws FQLException {
		this.source = source;
		this.target = target;
		for (Pair<String, String> p : objs) {
			Node sn = this.source.getNode(p.first);
			Node tn = this.target.getNode(p.second);
			nm.put(sn, tn);
		}
		for (Pair<Pair<Pair<String, String>, String>, List<Pair<Pair<String, String>, String>>> arrow : arrows) {
			Edge e = this.source.getEdge(arrow.first.second);
			Node n = nm.get(e.source);
			Path p = new Path(this.target, arrow.second, n);
			em.put(e, p);
		}
		for (Node n : this.source.nodes) {
			if (nm.get(n) == null) {
				throw new FQLException("Missing node mapping from " + n
				/* + " in " + name */);
			}
		}
		for (Edge e : this.source.edges) {
			if (em.get(e) == null) {
				throw new FQLException("Missing arrow mapping from " + e
				/* + " in " + name */);
			}
		}

		validate();
	}

	/**
	 * Does most of the work of the constructor.
	 */
	private void initialize(Signature source, Signature target,
			List<Pair<String, String>> objs, List<Pair<String, String>> attrs,
			List<Pair<String, List<String>>> arrows) throws FQLException {
		this.source = source;
		this.target = target;

		Pair<List<Pair<String, String>>, List<Pair<String, String>>> s = filter(objs);
		objs = s.first;
		// List<Pair<String, String>> attrs = s.second;
		for (Pair<String, String> p : objs) {
			Node sn = this.source.getNode(p.first);
			Node tn = this.target.getNode(p.second);
			nm.put(sn, tn);
		}
		try {
			for (Pair<String, List<String>> arrow : arrows) {
				Edge e = this.source.getEdge(arrow.first);
				Path p = new Path(this.target, arrow.second);
				em.put(e, p);
			}
		} catch (FQLException e) {
			throw new FQLException("In mapping " + this
					+ ", bad path mapping: " + e.toString());
		}
		// System.out.println("INIT \n" + this);
		for (Pair<String, String> a : attrs) {
			Attribute<Node> a1 = source.getAttr(a.first);
			Attribute<Node> a2 = target.getAttr(a.second);
			if (a1 == null) {
				throw new FQLException(/* "In mapping " + this
						+ ", */ "Cannot find source attribute " + a.first);
			}
			if (a2 == null) {
				throw new FQLException(/* "In mapping " + this
						+ ", */ "Cannot find target attribute " + a.second);
			}
			if (a1.target.equals(a2.target)) {
				am.put(a1, a2);
			} else {
				throw new FQLException("Incompatible attribute mapping types "
						+ a);
			}
		}
		for (Node n : this.source.nodes) {
			if (nm.get(n) == null) {
				throw new FQLException("Missing node mapping from " + n
						+ " in " + this + "\n" + this);
			}
		}
		for (Edge e : this.source.edges) {
			if (em.get(e) == null) {
				throw new FQLException("Missing arrow mapping from " + e
						+ " in " + this);
			}
		}
		for (Attribute<Node> a : this.source.attrs) {
			if (am.get(a) == null) {
				throw new FQLException("Missing attribute mapping from " + a
						+ " in " + this);
			}
		}
	}

	/**
	 * Constructs an identity mapping
	 */
	private void identity(Environment env, Signature s) throws FQLException {
		for (Node n : s.nodes) {
			nm.put(n, n);
		}
		for (Edge e : s.edges) {
			em.put(e, new Path(s, e));
		}
		for (Attribute<Node> a : s.attrs) {
			am.put(a, a);
		}
		this.source = s;
		this.target = s;
		// isId = true;
	}

	/**
	 * The viewer for mappings.
	 */
	public JPanel view() {
		Object[][] arr = new Object[nm.size()][2];
		int i = 0;
		for (Entry<Node, Node> eq : nm.entrySet()) {
			arr[i][0] = eq.getKey();
			arr[i][1] = eq.getValue();
			i++;
		}
		Arrays.sort(arr, new Comparator<Object[]>() {
			public int compare(Object[] f1, Object[] f2) {
				return f1[0].toString().compareTo(f2[0].toString());
			}
		});

		JTable nmC = new JTable(arr, new Object[] { "Source node"/*
																 * +
																 * source.name0
																 */,
				"Target node" /* + target.name0 */});

		Object[][] arr2 = new Object[em.size()][2];
		int i2 = 0;
		for (Entry<Edge, Path> eq : em.entrySet()) {
			arr2[i2][0] = eq.getKey();
			arr2[i2][1] = eq.getValue().toLong();
			i2++;
		}
		Arrays.sort(arr2, new Comparator<Object[]>() {
			public int compare(Object[] f1, Object[] f2) {
				return f1[0].toString().compareTo(f2[0].toString());
			}
		});

		JTable emC = new JTable(arr2, new Object[] { "Source edge" /*
																	 * +
																	 * source.name0
																	 */,
				"Target path" /* + target.name0 */});

		Object[][] arr3 = new Object[am.size()][2];
		int i3 = 0;
		for (Entry<Attribute<Node>, Attribute<Node>> eq : am.entrySet()) {
			arr3[i3][0] = eq.getKey();
			arr3[i3][1] = eq.getValue();
			i3++;
		}
		Arrays.sort(arr3, new Comparator<Object[]>() {
			public int compare(Object[] f1, Object[] f2) {
				return f1[0].toString().compareTo(f2[0].toString());
			}
		});
		JTable amC = new JTable(arr3, new Object[] { "Source attribute" /*
																		 * +
																		 * source
																		 * .
																		 * name0
																		 */,
				"Target attribute" /* + target.name0 */});

		JPanel p = new JPanel(new GridLayout(2, 2));

		JScrollPane q1 = new JScrollPane(nmC);
		JScrollPane q2 = new JScrollPane(emC);
		JScrollPane q3 = new JScrollPane(amC);

		JPanel j1 = new JPanel(new GridLayout(1, 1));
		j1.add(q1);
		j1.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEmptyBorder(), "Node mapping"));
		p.add(j1);

		JPanel j2 = new JPanel(new GridLayout(1, 1));
		j2.add(q2);
		j2.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEmptyBorder(), "Arrow mapping"));
		p.add(j2);

		JPanel j3 = new JPanel(new GridLayout(1, 1));
		j3.add(q3);
		j3.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEmptyBorder(), "Attribute mapping"));
		p.add(j3);
		p.setBorder(BorderFactory.createEtchedBorder());
		return p;
	}

	/*
	 * static String printNicely(Map<String, RA> r) { String s = ""; for (String
	 * k : r.keySet()) { RA e = r.get(k); s += (k + " = " + e + "\n\n"); }
	 * return s; }
	 */

	/**
	 * Text view for mappings.
	 */
	public JPanel text() {

		// String[] t = toString().split(";");
		// String ret = "";
		// for (String a : t) {
		// ret += (a.trim());
		// }

		JPanel tap = new JPanel(new GridLayout(2, 2));

		JTextArea ta = new JTextArea(toString());
		ta.setWrapStyleWord(true);
		ta.setLineWrap(true);
		JScrollPane xxx = new JScrollPane(ta);
		JPanel p = new JPanel(new GridLayout(1, 1));
		p.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEmptyBorder(), "Mapping" /*
															 * + " : " + source
															 * + " -> " + target
															 */));
		p.add(xxx);
		tap.add(p);

		String delta = "";
		try {
			delta = printNicely(PSMGen.delta(this, "input", "output"));
		} catch (Exception e) {
			delta = e.toString();
		}

		String sigma = "";
		try {
			sigma = printNicely(PSMGen.sigma(this, "output", "input")); // backwards
		} catch (Exception e) {
			sigma = e.toString();
		}

		String pi = "";
		try {
			pi = printNicely(PSMGen.pi(this, "input", "output").first);
		} catch (Exception e) {
			pi = e.toString();
		}

		JTextArea ta2 = new JTextArea(delta);
		ta2.setWrapStyleWord(true);
		ta2.setLineWrap(true);
		JScrollPane xxx2 = new JScrollPane(ta2);
		JPanel p2 = new JPanel(new GridLayout(1, 1));
		p2.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEmptyBorder(), "Delta" /*
															 * + " : " +
															 * target.name0 +
															 * " -> " +
															 * source.name0
															 */));
		p2.add(xxx2);
		tap.add(p2);

		JTextArea ta3 = new JTextArea(pi);
		ta3.setWrapStyleWord(true);
		ta3.setLineWrap(true);
		JScrollPane xxx3 = new JScrollPane(ta3);
		JPanel p3 = new JPanel(new GridLayout(1, 1));
		p3.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEmptyBorder(), "Pi" /*
														 * + " : " +
														 * source.name0 + " -> "
														 * + target.name0
														 */));
		p3.add(xxx3);
		tap.add(p3);

		JTextArea ta4 = new JTextArea(sigma);
		ta4.setWrapStyleWord(true);
		ta4.setLineWrap(true);
		JScrollPane xxx4 = new JScrollPane(ta4);
		JPanel p4 = new JPanel(new GridLayout(1, 1));
		p4.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEmptyBorder(), "Sigma" /*
															 * + " : " +
															 * source.name0 +
															 * " -> " +
															 * target.name0
															 */));
		p4.add(xxx4);
		tap.add(p4);
		tap.setBorder(BorderFactory.createEtchedBorder());
		return tap;
	}

	static String printNicely(List<PSM> delta) {
		String ret = "";
		for (PSM p : delta) {
			ret += p + "\n\n";
		}
		return ret;
	}

	public String toStringFull() {
		return toString2(true);
	}

	@Override
	public String toString() {
		return toString2(false);
	}

	public String toString2(boolean xxx) {
/*		StringBuffer sb = new StringBuffer(); // "mapping " + name + " : "
		// + source + " -> " + target + " = ");
		// if (isId) {
		// sb.append("id " + source.name0);
		// return sb.toString();
		// } else {
		sb.append("{\n");
		// }

		boolean first = true;
		sb.append("nodes\n");
		for (Node k : nm.keySet()) {
			Node v = nm.get(k);
			if (!first) {
				sb.append(",\n");
			}
			first = false;
			// sb.append("(");
			sb.append(k.string);
			sb.append(" -> ");
			sb.append(v.string);
			// sb.append(")");
		}
		first = true;
		sb.append("\n ;\nattributes\n");

		for (Attribute<Node> k : am.keySet()) {
			Attribute<Node> v = am.get(k);
			if (!first) {
				sb.append(",\n");
			}
			first = false;
			// sb.append("(");
			sb.append(k.name);
			sb.append(" -> ");
			sb.append(v.name);
			// sb.append(")");
		}
		sb.append("\n ;\narrows\n");
		first = true;
		for (Edge k : em.keySet()) {
			Path v = em.get(k);
			if (!first) {
				sb.append(",\n");
			}
			first = false;
			// sb.append("(");
			sb.append(k.name);
			sb.append(" -> ");
			sb.append(v);
			// sb.append(")");
		}
		sb.append("\n ;\n}");
		*/
		String nm = "\n nodes\n";
		boolean b = false;
		for (Entry<Node, Node> k : this.nm.entrySet()) {
			if (b) {
				nm += ",\n";
			}
			b = true;
			nm += "  " + k.getKey() + " -> " + k.getValue();
		}
		nm = nm.trim();
		nm += ";\n";

		nm += " attributes\n";
		b = false;
		for (Entry<Attribute<Node>, Attribute<Node>> k : this.am.entrySet()) {
			if (b) {
				nm += ",\n";
			}
			b = true;
			nm += "  " + k.getKey().name + " -> " + k.getValue().name;
		}
		nm = nm.trim();
		nm += ";\n";
		
		nm += " arrows\n";
		b = false;
		for (Entry<Edge, Path> k : this.em.entrySet()) {
			if (b) {
				nm += ",\n";
			}
			b = true;
			nm += "  " + k.getKey().name + " -> " + PrettyPrinter.sep0(".", k.getValue().asList());
		}
		nm = nm.trim();
		nm += ";\n";

		String ret = "{\n " + nm + "}";

		if (xxx) {
			ret += (" : " + source.toString());
			ret += (" \n\n -> \n\n ");
			ret += (target.toString());
		}
		return ret;
	}

	/**
	 * Applies to a path
	 */
	public Path appy(Path path) {
		List<Edge> r = new LinkedList<Edge>();
		for (Edge e : path.path) {
			Path p = em.get(e);
			if (p == null) {
				throw new RuntimeException("No mapping for " + e + " in " + this);
			}
			r.addAll(p.path);
		}

		return new Path(nm.get(path.source), nm.get(path.target), r);
	}

	/**
	 * Converts a mapping to a functor.
	 * 
	 * @return the functor, and some isomorphisms
	 */
	// public Triple<FinFunctor<String, List<List<String>>, String,
	// List<List<String>>>, Pair<FinCat<String, List<List<String>>>,
	// Pair<Pair<Map<String, String>, Map<String, String>>, Pair<Map<String,
	// Arr<String, List<List<String>>>>, Map<Arr<String, List<List<String>>>,
	// String>>>>, Pair<FinCat<String, List<List<String>>>,
	// Pair<Pair<Map<String, String>, Map<String, String>>, Pair<Map<String,
	// Arr<String, List<List<String>>>>, Map<Arr<String, List<List<String>>>,
	// String>>>>> toFunctor() throws FQLException {
	// HashMap<String, String> objMapping = new HashMap<String, String>();
	// HashMap<Arr<String,List<List<String>>>, Arr<String,List<List<String>>>>
	// arrowMapping = new HashMap<>();
	//
	// for (Entry<Node, Node> e : nm.entrySet()) {
	// objMapping.put(e.getKey().string, e.getValue().string);
	// }
	//
	// Pair<FinCat<String, List<List<String>>>, Pair<Pair<Map<String, String>,
	// Map<String, String>>, Pair<Map<String, Arr<String, List<List<String>>>>,
	// Map<Arr<String, List<List<String>>>, String>>>> srcCat0 =
	// source.toCategory();
	// Pair<FinCat<String, List<List<String>>>, Pair<Pair<Map<String, String>,
	// Map<String, String>>, Pair<Map<String, Arr<String, List<List<String>>>>,
	// Map<Arr<String, List<List<String>>>, String>>>> dstCat0 =
	// target.toCategory();
	//
	// FinCat<String, List<List<String>>> srcCat = srcCat0.first;
	// FinCat<String, List<List<String>>> dstCat = dstCat0.first;
	//
	// for (Arr<String, List<List<String>>> arroweqc : srcCat.arrows) {
	// List<String> arrow = arroweqc.arr.get(0);
	//
	// List<String> mapped = apply(arrow);
	// Arr<String, List<List<String>>> mappedeqc = findeqc(dstCat, mapped);
	// arrowMapping.put(arroweqc, mappedeqc);
	// }
	//
	// return new Triple<>(new FinFunctor<>(objMapping, arrowMapping, srcCat,
	// dstCat), srcCat0, dstCat0);
	// }

	public Triple<FinFunctor<Node, Path, Node, Path>, Pair<FinCat<Node, Path>, Fn<Path, Arr<Node, Path>>>, Pair<FinCat<Node, Path>, Fn<Path, Arr<Node, Path>>>> toFunctor2()
			throws FQLException {
		HashMap<Node, Node> objMapping = new HashMap<>();
		HashMap<Arr<Node, Path>, Arr<Node, Path>> arrowMapping = new HashMap<>();

		for (Entry<Node, Node> e : nm.entrySet()) {
			objMapping.put(e.getKey(), e.getValue());
		}

		Pair<FinCat<Node, Path>, Fn<Path, Arr<Node, Path>>> srcCat0 = source
				.toCategory2();
		Pair<FinCat<Node, Path>, Fn<Path, Arr<Node, Path>>> dstCat0 = target
				.toCategory2();

		FinCat<Node, Path> srcCat = srcCat0.first;
		FinCat<Node, Path> dstCat = dstCat0.first;

		for (Arr<Node, Path> arroweqc : srcCat.arrows) {
			Path arrow = arroweqc.arr;

			Path mapped = appy(arrow);
			arrowMapping.put(new Arr<>(arrow, arrow.source, arrow.target),
					dstCat0.second.of(mapped));
		}

		FinFunctor<Node, Path, Node, Path> F = new FinFunctor<>(objMapping,
				arrowMapping, srcCat, dstCat);

		F.am = am;

		return new Triple<>(F, srcCat0, dstCat0);
	}

	// private Arr<String, List<List<String>>> findeqc(
	// FinCat<String, List<List<String>>> cat, List<String> path) {
	// for (Arr<String, List<List<String>>> eqc : cat.arrows) {
	// if (eqc.arr.contains(path)) {
	// return eqc;
	// }
	// }
	// throw new RuntimeException("No equivalence class for " + path + " in " +
	// cat);
	// }

	// private List<String> apply(List<String> arrow) {
	// List<String> ret = new LinkedList<String>();
	// for (Entry<Node, Node> e : nm.entrySet()) {
	// if (e.getKey().string.equals(arrow.get(0))) {
	// ret.add(e.getValue().string);
	// break;
	// }
	// }
	//
	// for (int i = 1; i < arrow.size(); i++) {
	// String s = arrow.get(i);
	// for (Entry<Edge, Path> e : em.entrySet()) {
	// if (e.getKey().name.equals(s)) {
	// List<String> x = e.getValue().asList();
	// x.remove(0);
	// ret.addAll(x);
	// }
	// }
	// }
	// return ret;
	// }
	/*
	 * public Map<String, Set<Pair<Object, Object>>> evalDelta(Instance
	 * theinstance) throws FQLException {
	 * 
	 * // System.out.println(FDM.delta(this.toFunctor(), //
	 * theinstance.toFunctor()));
	 * 
	 * Map<String, RA> x = RA.delta(this); Map<String, Set<Object[]>> i0 =
	 * Query.convert0(theinstance); Map<String, Set<Object[]>> i1 = RA.eval0(x,
	 * i0); return Query.convert(i1); }
	 * 
	 * public Map<String, Set<Pair<Object, Object>>> evalSigma(Instance
	 * theinstance) throws FQLException { Map<String, RA> x = RA.sigma(this);
	 * Map<String, Set<Object[]>> i0 = Query.convert0(theinstance); Map<String,
	 * Set<Object[]>> i1 = RA.eval0(x, i0);
	 * 
	 * // System.out.println(FDM.sigma(this.toFunctor(), //
	 * theinstance.toFunctor()));
	 * 
	 * return Query.convert(i1); }
	 * 
	 * public Map<String, Set<Pair<Object, Object>>> evalPi(Instance
	 * theinstance) throws FQLException {
	 * 
	 * Map<String, RA> x = RA.pi(this); Map<String, Set<Object[]>> i0 =
	 * Query.convert0(theinstance); Map<String, Set<Object[]>> i1 = RA.eval0(x,
	 * i0);
	 * 
	 * // System.out.println(FDM.grothendieck(FDM.pi(this.toFunctor().first, //
	 * theinstance.toFunctor())));
	 * 
	 * return Query.convert(i1); }
	 */

	public JPanel pretty(final Environment env) throws FQLException {
		Graph<String, String> g = build();
		if (g.getVertexCount() == 0) {
			return new JPanel();
		}
		return doView(env, g);
	}

	public Graph<String, String> build() {
		// Graph<V, E> where V is the type of the vertices

		Graph<String, String> g2 = new DirectedSparseMultigraph<String, String>();
		for (Node n : source.nodes) {
			g2.addVertex("@source" + "." + n.string);
		}
		for (Edge e : source.edges) {
			g2.addEdge("@source" + "." + e.name, "@source" + "."
					+ e.source.string, "@source" + "." + e.target.string);
		}
		for (Attribute<Node> a : source.attrs) {
			g2.addVertex("@source" + "." + a.name);
			g2.addEdge("@source" + "." + a.name, "@source" + "."
					+ a.source.string, "@source" + "." + a.name);
		}

		for (Node n : target.nodes) {
			g2.addVertex("@target" + "." + n.string);
		}
		for (Edge e : target.edges) {
			g2.addEdge("@target" + "." + e.name, "@target" + "."
					+ e.source.string, "@target" + "." + e.target.string);
		}
		for (Attribute<Node> a : target.attrs) {
			g2.addVertex("@target" + "." + a.name);
			g2.addEdge("@target" + "." + a.name, "@target" + "."
					+ a.source.string, "@target" + "." + a.name);
		}

		for (Node n : nm.keySet()) {
			Node m = nm.get(n);
			g2.addEdge(n.string + " " + m.string, "@source" + "." + n.string,
					"@target" + "." + m.string);
		}

		for (Attribute<Node> n : am.keySet()) {
			Attribute<Node> m = am.get(n);
			g2.addEdge(n.name + " " + m.name, "@source" + "." + n.name,
					"@target" + "." + m.name);
		}

		return g2;
	}

	// Transformer<String, Paint> vertexPaint = new Transformer<String, Paint>()
	// {
	// public Paint transform(String i) {
	// return which(i);
	// }
	//
	// private Color which(String t) {
	// int i = t.indexOf(".");
	// String j = t.substring(i+1);
	// String p = t.substring(0, i);
	// System.out.println("%%%%%%%%%%%%%%%%%% " + j);
	// if (source.isAttribute(j) || target.isAttribute(j)) {
	// return null;
	// }
	// if (p.equals("@source")) {
	// return Environment.colors.get(source.name0);
	// }
	// return Environment.colors.get(target.name0);
	// }
	// };

	@SuppressWarnings("unchecked")
	public JPanel doView(final Environment env, Graph<String, String> sgv) {
		// Layout<V, E>, BasicVisualizationServer<V,E>
		try {
			Class<?> c = Class.forName(DEBUG.layout_prefix + DEBUG.debug.mapping_graph);
			Constructor<?> x = c.getConstructor(Graph.class);
			Layout<String, String> layout = (Layout<String, String>) x.newInstance(sgv);

//		Layout<String, String> layout = new FRLayout<>(sgv);
		// Layout<String, String> layout = new KKLayout(sgv);

		// Layout<String, String> layout = new ISOMLayout<String,String>(sgv);
		// Layout<String, String> layout = new CircleLayout(sgv);
		layout.setSize(new Dimension(600, 400));
		VisualizationViewer<String, String> vv = new VisualizationViewer<String, String>(
				layout);
		// vv.setPreferredSize(new Dimension(600, 400));
		// Setup up a new vertex to paint transformer...
		Transformer<String, Paint> vertexPaint = new Transformer<String, Paint>() {
			public Paint transform(String i) {
				return which(i);
			}

			private Color which(String t) {
				int i = t.indexOf(".");
				String j = t.substring(i + 1);
				String p = t.substring(0, i);
				if (source.isAttribute(j) || target.isAttribute(j)) {
					return UIManager.getColor("Panel.background");
				}
				if (p.equals("@source")) {
					return Color.RED;
					// return source.colors.get(j);
				}
				// return target.colors.get(j);
				return Color.BLUE;
				// return env.colors.get(target.name0);
			}
		};
		DefaultModalGraphMouse<String, String> gm = new DefaultModalGraphMouse<>();
		gm.setMode(ModalGraphMouse.Mode.TRANSFORMING);
		vv.setGraphMouse(gm);
		gm.setMode(Mode.PICKING);

		// Set up a new stroke Transformer for the edges
		float dash[] = { 10.0f };
		final Stroke edgeStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
				BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f);
		final Stroke bs = new BasicStroke();

		Transformer<String, Stroke> edgeStrokeTransformer = new Transformer<String, Stroke>() {
			public Stroke transform(String s) {
				if (s.contains(" ")) {
					return edgeStroke;
				}
				return bs;
			}
		};
		vv.getRenderContext().setVertexFillPaintTransformer(vertexPaint);
		vv.getRenderContext().setEdgeStrokeTransformer(edgeStrokeTransformer);
		vv.getRenderContext().setVertexLabelTransformer(
				new ToStringLabeller<String>() {
					@Override
					public String transform(String t) {
						int i = t.indexOf(".");
						String j = t.substring(i + 1);
						// String p = t.substring(0, i);
						return j;
						/*
						 * if (p.equals("@source")) { j = source.name0 + "." +
						 * j; } else { j = target.name0 + "." + j; } return j;
						 */
					}
				});
		// vv.getRenderContext().setEdgeLabelTransformer(
		// new ToStringLabeller<String>() {
		//
		// @Override
		// public String transform(String t) {
		// if (t.contains(" ")) {
		// return "";
		// }
		// return t;
		// // return "";
		// }
		// });
		// vv.getRenderer().getVertexRenderer();
		// vv.getRenderContext().setLabelOffset(20);
		// vv.getRenderer().getVertexLabelRenderer().setPosition(Position.CNTR);

		JPanel p = new JPanel(new GridLayout(1, 1));
		p.setBorder(BorderFactory.createEtchedBorder());
		p.add(new GraphZoomScrollPane(vv));
		return p;
		} catch (Throwable e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
	}

	public static Mapping compose(/* String string, */Mapping l, Mapping r)
			throws FQLException {
		if (!l.target.equals(r.source)) {
			throw new RuntimeException(l.target + "\n\n" + r.source);
		}

		List<Pair<String, String>> xxx = new LinkedList<>();
		List<Pair<String, List<String>>> yyy = new LinkedList<>();
		List<Pair<String, String>> zzz = new LinkedList<>();

		for (Node n : l.source.nodes) {
			xxx.add(new Pair<>(n.string, r.nm.get(l.nm.get(n)).string));
		}

		for (Edge e : l.source.edges) {
			Path p = l.em.get(e);
			yyy.add(new Pair<>(e.name, r.appy(p).asList()));
		}

		for (Attribute<Node> a : l.source.attrs) {
			Attribute<Node> b = l.am.get(a);
			zzz.add(new Pair<>(a.name, r.am.get(b).name));
		}

		return new Mapping(l.source, r.target, xxx, zzz, yyy);
	}

	public void okForPi() throws FQLException {
		for (Attribute<Node> n : target.attrs) {
//			Set<Attribute<Node>> set = new HashSet<>
			boolean found = false;
			for (Attribute<Node> a : source.attrs) {
				Attribute<Node> c = am.get(a);
				if (c.equals(n)) {
					if (!DEBUG.debug.allow_surjective) {
					if (found) {
						throw new FQLException("Not attribute bijection "
								+ this);
					}
					}
					found = true;
				}
			}
			if (!found) {
				throw new FQLException("Not surjective: " + this);
			}
		}

	}

	public boolean okForSigmaBool() {
		try {
			okForSigma();
			return true;
		} catch (Throwable fe) {
			return false;
		}
	}
	public void okForSigma() throws FQLException {
		for (Node n : source.nodes) {
			List<Attribute<Node>> nattrs = source.attrsFor(n);

			List<Attribute<Node>> mattrs = target.attrsFor(nm.get(n));

			if (!isBijection(nattrs, mattrs, am)) {
				throw new FQLException("Not union compatible " + this);
			}
		}

	}

	private <X> boolean isBijection(List<X> l, List<X> r, Map<X, X> f) {

		for (X x : l) {
			if (f.get(x) == null) {
				return false;
			}
			if (!r.contains(f.get(x))) {
				return false;
			}
		}

		for (X x : r) {
			boolean found = false;
			for (X y : l) {
				if (f.get(y).equals(x)) {
					if (found) {
						return false;
					}
					found = true;
				}
			}
			if (!found) {
				return false;
			}
		}

		return true;
	}

	public JPanel constraint() {
		JPanel ret = new JPanel(new GridLayout(1, 1));
		try {
			Triple<Pair<Signature, List<Pair<Attribute<Node>, Pair<Edge, Attribute<Node>>>>>, Pair<Signature, List<Pair<Attribute<Node>, Pair<Edge, Attribute<Node>>>>>, Pair<Signature, List<Pair<Attribute<Node>, Pair<Edge, Attribute<Node>>>>>> x = toEDs();

			JTabbedPane t = new JTabbedPane();
			t.addTab("Sigma", quickView(x.second));
			t.addTab("Pi", quickView(x.third));
			t.addTab("Delta", quickView(x.first));

			ret.add(t);

		} catch (FQLException e) {
			// e.printStackTrace();
			ret.add(new JScrollPane(new JTextArea(e.getMessage())));
		}
		return ret;
	}

	//Pair<Signature, List<Pair<Attribute<Node>, Pair<Edge, Attribute<Node>>>>>
	
	//does not add equations for attrs
	public Triple<Pair<Signature, List<Pair<Attribute<Node>, Pair<Edge, Attribute<Node>>>>>, 
				  Pair<Signature, List<Pair<Attribute<Node>, Pair<Edge, Attribute<Node>>>>>, 
				  Pair<Signature, List<Pair<Attribute<Node>, Pair<Edge, Attribute<Node>>>>>> toEDs() throws FQLException {
		// List<EmbeddedDependency> l = new //toED();

		Signature sigma = Signature.sum("src", "dst", source, target);
		Signature pi = Signature.sum("src", "dst", source, target);
		Signature delta = Signature.sum("src", "dst", source, target);
		
		Map<Node, Edge> m_map = new HashMap<>();
		Map<Node, Edge> l_map = new HashMap<>();
		for (Node c : source.nodes) {
			Edge m = new Edge("m_src_" + c.string,
					new Node("dst_" + nm.get(c)), new Node("src_" + c));
			Edge l = new Edge("l_src_" + c.string, new Node("src_" + c),
					new Node("dst_" + nm.get(c)));
			pi.edges.add(m);
			sigma.edges.add(l);
			delta.edges.add(m);
			delta.edges.add(l);
			m_map.put(c, m);
			l_map.put(c, l);
			delta.eqs.add(new Eq(Path.append2(delta, new Path(delta, l),
					new Path(delta, m)), new Path(delta, new Node("dst_"
					+ nm.get(c)))));
			delta.eqs
					.add(new Eq(Path.append2(delta, new Path(delta, m),
							new Path(delta, l)), new Path(delta, new Node(
							"src_" + c))));
		}
		for (Edge f : source.edges) {
			Edge fX = new Edge("src_" + f.name, new Node("src_"
					+ f.source.string), new Node("src_" + f.target.string));

			Edge e1 = m_map.get(f.source);
			Edge e2 = m_map.get(f.target);
			Path pX = em.get(f);
			List<String> x = new LinkedList<>();
			x.add("dst_" + pX.source.string);
			for (Edge y : pX.path) {
				x.add("dst_" + y.name);
			}
			Path p = new Path(pi, x);

			Path lhs = Path.append(pi, p, new Path(pi, e2));
			Path rhs = Path.append(pi, new Path(pi, e1), new Path(pi, fX));
			pi.eqs.add(new Eq(lhs, rhs));

			Edge e10 = l_map.get(f.target);
			Edge e20 = l_map.get(f.source);
			Path pX0 = em.get(f);
			x = new LinkedList<>();
			x.add("dst_" + pX0.source.string);
			for (Edge y : pX0.path) {
				x.add("dst_" + y.name);
			}
			Path p0 = new Path(sigma, x);

			Path lhs0 = Path.append(sigma, new Path(sigma, e20), p0);
			Path rhs0 = Path.append(sigma, new Path(sigma, fX), new Path(sigma,
					e10));
			sigma.eqs.add(new Eq(lhs0, rhs0));

			lhs = Path.append(delta, p, new Path(delta, e2));
			rhs = Path.append(delta, new Path(delta, e1), new Path(delta, fX));
			delta.eqs.add(new Eq(lhs, rhs));

			lhs0 = Path.append(delta, new Path(delta, e20), p0);
			rhs0 = Path
					.append(delta, new Path(delta, fX), new Path(delta, e10));
			delta.eqs.add(new Eq(lhs0, rhs0));

		}
		
		List<Pair<Attribute<Node>, Pair<Edge, Attribute<Node>>>> deltaXX = new LinkedList<>();
		List<Pair<Attribute<Node>, Pair<Edge, Attribute<Node>>>> sigmaYY = new LinkedList<>();
		List<Pair<Attribute<Node>, Pair<Edge, Attribute<Node>>>> piZZ = new LinkedList<>();

		for (Attribute<Node> a : source.attrs) {
			Attribute<Node> a0 = new Attribute<Node>("src_" + a.name, new Node("src_" + a.source.string), a.target);
			Attribute<Node> b0 = new Attribute<Node>("dst_" + am.get(a).name, new Node("dst_" + am.get(a).source.string), a.target);
			
			sigmaYY.add(new Pair<>(a0, new Pair<>(l_map.get(a.source), b0)));
			deltaXX.add(new Pair<>(a0, new Pair<>(l_map.get(a.source), b0)));

			piZZ.add(new Pair<>(b0, new Pair<>(m_map.get(a.source), a0)));
			deltaXX.add(new Pair<>(b0, new Pair<>(m_map.get(a.source), a0)));
		}
		 
		Pair<Signature, List<Pair<Attribute<Node>, Pair<Edge, Attribute<Node>>>>> deltaX = new Pair<>(delta, deltaXX);
		Pair<Signature, List<Pair<Attribute<Node>, Pair<Edge, Attribute<Node>>>>> sigmaY = new Pair<>(delta, sigmaYY);
		Pair<Signature, List<Pair<Attribute<Node>, Pair<Edge, Attribute<Node>>>>> piZ = new Pair<>(delta, piZZ);
		return new Triple<>(deltaX, sigmaY, piZ);
	}


	private JComponent quickView(Pair<Signature, List<Pair<Attribute<Node>, Pair<Edge, Attribute<Node>>>>> second) {
		JTabbedPane ret = new JTabbedPane();

		ret.addTab("Signature", new JScrollPane(new JTextArea(Signature.toString(second))));
		ret.addTab("Embedded Dependencies", new JScrollPane(new JTextArea(
				quickConv(Signature.toED("", second)))));

		return ret;
	}

	private String quickConv(List<EmbeddedDependency> ed) {
		String ret = "";
		for (EmbeddedDependency d : ed) {
			ret += d.toString();
			ret += "\n\n";
		}

		return ret.trim();
	}

	@Override
	public Mapping clone() {
		Signature s = source.clone();
		Signature t = target.clone();
		try {
		return new Mapping(false, s, t, new LinkedHashMap<>(nm), new LinkedHashMap<>(
				em), new LinkedHashMap<>(am));
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
	}

}
