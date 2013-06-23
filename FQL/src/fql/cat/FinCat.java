package fql.cat;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fql.DEBUG;
import fql.FQLException;
import fql.Pair;
import fql.Quad;
import fql.Triple;
import fql.decl.Attribute;
import fql.decl.Signature;

/**
 * 
 * Implementation of finite categories.
 * 
 * @author ryan
 * 
 * @param <Obj>
 *            type of objects
 * @param <Arrow>
 *            type of arrows
 */
public class FinCat<Obj, Arrow> {

	public List<Attribute<Obj>> attrs;
	
	public List<Obj> objects = new LinkedList<>();
	public List<Arr<Obj, Arrow>> arrows = new LinkedList<>();
	public Map<Pair<Arr<Obj, Arrow>, Arr<Obj, Arrow>>, Arr<Obj, Arrow>> composition = new HashMap<>();
	public Map<Obj, Arr<Obj, Arrow>> identities = new HashMap<>();

	/**
	 * Empty Category
	 */
	public FinCat() {
	}

	/**
	 * Singleton category
	 * 
	 * @param o
	 *            the object
	 * @param a
	 *            the identity arrow
	 */
	public FinCat(Obj o, Arr<Obj, Arrow> a) {
		objects.add(o);
		arrows.add(a);
		composition.put(new Pair<>(a, a), a);
		identities.put(o, a);
	}

	/**
	 * Creates a new category, does not copy inputs.
	 */
	public FinCat(
			List<Obj> objects,
			List<Arr<Obj, Arrow>> arrows,
			Map<Pair<Arr<Obj, Arrow>, Arr<Obj, Arrow>>, Arr<Obj, Arrow>> composition,
			Map<Obj, Arr<Obj, Arrow>> identities) {
		noDupes(objects);
		noDupes(arrows);
		this.objects = objects;
		this.arrows = arrows;
		this.composition = composition;
		this.identities = identities;
		if (DEBUG.VALIDATE) {
			validate();
		}
	}

	private <X> void noDupes(List<X> X) {
		Set<X> x = new HashSet<X>(X);
		if (x.size() != X.size()) {
			throw new RuntimeException("duplicates " + X);
		}
	}

	public void validate() {
		if (arrows.size() < objects.size()) {
			throw new RuntimeException("Missing arrows: " + this);
		}
		for (Arr<Obj, Arrow> a : arrows) {
			if (a.src == null) {
				throw new RuntimeException(a + " has no source " + this);
			}
			if (a.dst == null) {
				throw new RuntimeException(a + " has no dst " + this);
			}
		}
		for (Obj o : objects) {
			Arr<Obj, Arrow> i = identities.get(o);
			if (i == null) {
				throw new RuntimeException(o + " has no arrow " + this);
			}
			for (Arr<Obj, Arrow> a : arrows) {
				if (a.src.equals(o)) {
					if (!a.equals(compose(i, a))) {
						throw new RuntimeException("Identity compose error1 "
								+ i + o + a);
					}
				}
				if (a.dst.equals(o)) {
					if (!a.equals(compose(a, i))) {
						throw new RuntimeException("Identity compose error2 "
								+ i + o + a);
					}
				}
			}
		}

		for (Arr<Obj, Arrow> a : arrows) {
			for (Arr<Obj, Arrow> b : arrows) {
				if (a.dst.equals(b.src)) {
					Arr<Obj, Arrow> c = compose(a, b);
					if (!arrows.contains(c)) {
						throw new RuntimeException(
								"Not closed under composition " + a + b + c
										+ this);
					}
					if (!a.src.equals(c.src)) {
						throw new RuntimeException("Composition type error1 "
								+ a + b + c + this);
					}
					if (!b.dst.equals(c.dst)) {
						throw new RuntimeException("Composition type error2 "
								+ a + b + c + this);
					}
					for (Arr<Obj, Arrow> cc : arrows) {
						if (cc.src.equals(b.dst)) {
							if (!compose(a, compose(b, cc)).equals(
									compose(compose(a, b), cc))) {
								throw new RuntimeException("Not associative "
										+ a + b + cc);
							}
						}
					}
				}
			}
		}

	}

	public Arr<Obj, Arrow> id(Obj o) {
		return identities.get(o);
	}

	public Set<Arr<Obj, Arrow>> hom(Obj A, Obj B) {
		Set<Arr<Obj, Arrow>> ret = new HashSet<>();
		for (Arr<Obj, Arrow> a : arrows) {
			if (a.src.equals(A) && a.dst.equals(B)) {
				ret.add(a);
			}
		}
		return ret;
	}

	public Arr<Obj, Arrow> compose(Arr<Obj, Arrow> a, Arr<Obj, Arrow> b) {
		return composition.get(new Pair<>(a, b));
	}

	public boolean isId(Arr<Obj, Arrow> a) {
		return identities.containsValue(a);
	}

	/**
	 * Converts a category to a signature.
	 * 
	 * @param n
	 *            the "name" of the signature
	 * @return a signature and isomorphism
	 * @throws FQLException
	 */
	public Quad<Signature, Pair<Map<Obj, String>, Map<String, Obj>>, Pair<Map<Arr<Obj, Arrow>, String>, Map<String, Arr<Obj, Arrow>>>, Pair<Map<Attribute<Obj>, String>, Map<String, Attribute<Obj>>>> 
	toSig(String n) throws FQLException {

		
		 Map<Attribute<Obj>, String> attM = new HashMap<>(); 
		 Map<String, Attribute<Obj>> attM2 = new HashMap<>();
		int ax = 0;
		
		List<String> objs = new LinkedList<>();
		List<Triple<String, String, String>> attrs0 = new LinkedList<>();
		
		int i = 0;
		Map<String, Obj> objM = new HashMap<>();
		Map<Obj, String> objM2 = new HashMap<>();
		for (Obj o : objects) {
			objM2.put(o, "obj" + i);
			objM.put("obj" + i, o);
			objs.add("obj" + i);
			i++;
		}

		for (Attribute<Obj> att : attrs) {
			attM.put(att, "attrib" + ax);
			attM2.put("attrib" + ax, att);
			attrs0.add(new Triple<>("attrib" + ax++, objM2.get(att.source), att.target.toString()));
		}
		
		List<Triple<String, String, String>> arrs = new LinkedList<>();
		int j = 0;
		Map<String, Arr<Obj, Arrow>> arrM = new HashMap<>();
		Map<Arr<Obj, Arrow>, String> arrM2 = new HashMap<>();
		for (Arr<Obj, Arrow> a : arrows) {
			if (isId(a)) {
				continue;
			} 
			arrM.put("arrow" + j, a);
			arrM2.put(a, "arrow" + j);
			arrs.add(new Triple<>(arrM2.get(a), objM2.get(a.src), objM2
					.get(a.dst)));
			j++;
		}

		// System.out.println(objM);
		// System.out.println(arrM);

//		for (Arr<Obj, Arrow> a : this.arrows) {
//			// System.out.println("arrow a is " + a);
//			
//		}

		

		// System.out.println("arrows are " + arrows);
		LinkedList<Pair<List<String>, List<String>>> eqs = new LinkedList<>();

		 //System.out.println("$$$$$$$$$$$$$$$$$$$$$");
		 //System.out.println(this);
		 //System.out.println(ret2);
//		 System.out.println(objM);
//		 System.out.println(objM2);
//		 System.out.println(arrM);
//		 System.out.println(arrM2);
//		 System.out.println(attM2);
//		 System.out.println(attM);
//		 System.out.println("$$$$$$$$$$$$$$$$$$$$$");
		
		Signature ret2 = new Signature(n, objs, attrs0, arrs, 
				eqs);

		
		 
		Quad<Signature, Pair<Map<Obj, String>, Map<String, Obj>>, Pair<Map<Arr<Obj, Arrow>, String>, Map<String, Arr<Obj, Arrow>>>, Pair<Map<Attribute<Obj>, String>, Map<String, Attribute<Obj>>>> retret 
		= new Quad<>(ret2, new Pair<>(objM2, objM), new Pair<>(arrM2, arrM), new Pair<>(attM, attM2));
		return retret;
	}

//	private Set<Obj> isolated() {
//		Set<Obj> ret = new HashSet<>(objects);
//
//		for (Arr<Obj, Arrow> a : arrows) {
//			if (isId(a)) {
//				continue;
//			}
//			ret.remove(a.src);
//			ret.remove(a.dst);
//		}
//
//		return ret;
//	}

	@Override
	public String toString() {

		String o = "";
		for (Obj oo : objects) {
			o += "\t" + oo + "\n";
		}

		String a = "";
		for (Arr<Obj, Arrow> aa : arrows) {
			a += "\t" + aa.toString2() + "\n";
		}

		String c = "";
		for (Arr<Obj, Arrow> a1 : arrows) {
			for (Arr<Obj, Arrow> a2 : arrows) {
				Arr<Obj, Arrow> a3 = compose(a1, a2);
				if (a3 != null) {
					c += "\t" + a1 + " ; " + a2 + " = " + a3 + "\n";
				}
			}
		}

		String j = "";
		for (Obj oo : objects) {
			j += "\t" + "id_" + oo + " = " + identities.get(oo) + "\n";
		}

		return "objects:\n" + o + "\n\narrows:\n" + a + "\n\ncomposition:\n"
				+ c + "\n\nidentities:\n" + j;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((arrows == null) ? 0 : arrows.hashCode());
		result = prime * result
				+ ((composition == null) ? 0 : composition.hashCode());
		result = prime * result
				+ ((identities == null) ? 0 : identities.hashCode());
		result = prime * result + ((objects == null) ? 0 : objects.hashCode());
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
		@SuppressWarnings("rawtypes")
		FinCat other = (FinCat) obj;
		if (arrows == null) {
			if (other.arrows != null)
				return false;
		} else if (!arrows.equals(other.arrows))
			return false;
		if (composition == null) {
			if (other.composition != null)
				return false;
		} else if (!composition.equals(other.composition))
			return false;
		if (identities == null) {
			if (other.identities != null)
				return false;
		} else if (!identities.equals(other.identities))
			return false;
		if (objects == null) {
			if (other.objects != null)
				return false;
		} else if (!objects.equals(other.objects))
			return false;
		return true;
	}

}
