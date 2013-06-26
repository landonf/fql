package fql.sql;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import fql.FQLException;
import fql.Pair;
import fql.cat.Arr;
import fql.cat.FinCat;
import fql.decl.Attribute;
import fql.decl.Edge;
import fql.decl.Node;
import fql.decl.Path;
import fql.decl.Signature;

public class Relationalizer {

	// sig, outname, inname

	public static Map<Node, List<String>> attrs;

	public static List<PSM> compile(Signature sig, String out, String in,
			boolean suppress) throws FQLException {
		attrs = new HashMap<Node, List<String>>();
		List<PSM> ret = new LinkedList<>();
		Map<String, String> edge_types = new HashMap<>();
		edge_types.put("c0", PSM.VARCHAR());
		edge_types.put("c1", PSM.VARCHAR());

		// copy in to out, to start with
		ret.addAll(copy(sig, out, in));

		FinCat<Node, Path> cat = sig.toCategory2().first;
		toplabel: for (Node n : sig.nodes) {
			attrs.put(n, new LinkedList<String>());
			int count = 0;
			List<Map<String, String>> alltypes = new LinkedList<>();
			for (Arr<Node, Path> p : cat.arrows) {
				// if (cat.isId(p)) {
				// continue;
				// }
				// need identity path to get attributes from n
				if (!p.src.equals(n)) {
					continue;
				}
				Flower f = PSMGen.compose(in, p.arr);

				ret.add(new CreateTable(n.string + "tempNoAttrs" + count,
						edge_types, false));
				InsertSQL f0 = new InsertSQL(n.string + "tempNoAttrs" + count,
						f);
				ret.add(f0);

				Map<String, Pair<String, String>> select = new HashMap<>();
				Map<String, String> from = new HashMap<>();
				List<Pair<Pair<String, String>, Pair<String, String>>> where = new LinkedList<>();
				List<Attribute<Node>> l = sig.attrsFor(p.arr.target);
				from.put(n.string, n.string + "tempNoAttrs" + count);
				select.put("c0", new Pair<>(n.string, "c0"));
			//	select.put("c1", new Pair<>(n.string + "tempNoAttrs", "c1"));
				int i = 1;
				Map<String, String> types = new HashMap<>();
				types.put("c0", PSM.VARCHAR());
			//	types.put("c1", PSM.VARCHAR());
				for (Attribute<Node> a : l) {
					from.put(a.name, in + "_" + a.name);
					Pair<String, String> lhs = new Pair<>(n.string, "c1");
					Pair<String, String> rhs = new Pair<>(a.name, "c0");
					where.add(new Pair<>(lhs, rhs));
					select.put("c" + i, new Pair<>(a.name, "c1"));
					types.put("c" + i, a.target.toString());
					attrs.get(n).add(p.toString() + "." + a.name);
					i++;
				}
				alltypes.add(types);
				Flower g = new Flower(select, from, where);
		//		System.out.println("&&&Flower is " + g);

				ret.add(new CreateTable(n.string + "temp" + count, types, false));
				ret.add(new InsertSQL(n.string + "temp" + count, g));
				count++;
			}

			Map<String, Pair<String, String>> select = new HashMap<>();
			List<Pair<Pair<String, String>, Pair<String, String>>> where = new LinkedList<>();
			Map<String, String> from = new HashMap<>();
			from.put(in + "_" + n.string, in + "_" + n.string);
			Map<String, String> ty = new HashMap<>();
			int u = 0;
			select.put("id", new Pair<>(in + "_" + n.string, "c0"));
			for (int i = 0; i < count; i++) {
				Map<String, String> types = alltypes.get(i);
				for (int v = 0; v < types.size() - 1; v++) {
					ty.put("c" + u, types.get("c" + (v+1))); //was c1
					select.put("c" + u, new Pair<>(n.string + "temp" + i, "c" + (v+1)));
					u++;
				}
				from.put(n.string + "temp" + i, n.string + "temp" + i);
				where.add(new Pair<>(new Pair<>(n.string + "temp" + i, "c0"),
						new Pair<>(in + "_" + n.string, "c0")));

			}
			if (select.size() == 0 && !suppress) {
				throw new RuntimeException("No observable for " + n.string
						+ " in " + sig.name0);
			} else if (select.size() == 0 && suppress) {
				continue;
			}
			Flower j = new Flower(select, from, where);
			ret.add(new CreateTable(n.string + "_observables", ty, false));
			ret.add(new InsertSQL(n.string + "_observables", j));

			Map<String, Pair<String, String>> select0 = new HashMap<>(select);
			Map<String, String> ty0 = new HashMap<>(ty);
			ty0.remove("id");
			select0.remove("id");
			Flower j0 = new Flower(select0, from, where);
			ret.add(new CreateTable(n.string + "_observables_proj", ty0, false));
			ret.add(new InsertSQL(n.string + "_observables_proj", j0));

			ret.add(new CreateTable(n.string + "_observables_guid", ty, false));
			ret.add(new InsertKeygen(n.string + "_observables_guid", "id",
					n.string + "_observables_proj", new LinkedList<>(ty
							.keySet())));

			select = new HashMap<>();
			where = new LinkedList<>();
			from = new HashMap<>();
			from.put(n.string + "_observables", n.string + "_observables");
			from.put(n.string + "_observables_guid", n.string
					+ "_observables_guid");
			for (int u0 = 0; u0 < u; u0++) {
				where.add(new Pair<>(new Pair<>(n.string + "_observables", "c"
						+ u0), new Pair<>(n.string + "_observables_guid", "c"
						+ u0)));
			}
			select.put("c0", new Pair<>(n.string + "_observables", "id"));
			select.put("c1", new Pair<>(n.string + "_observables_guid", "id"));

			Flower k = new Flower(select, from, where);
			ret.add(new CreateTable(n.string + "_subst", edge_types, false));
			ret.add(new InsertSQL(n.string + "_subst", k));
			if (suppress) {
				continue toplabel;
			}
			ret.add(new DropTable(n.string + "_observables"));
			ret.add(new DropTable(n.string + "_observables_guid"));
			ret.add(new DropTable(n.string + "_observables_proj"));
			for (int count0 = 0; count0 < count; count0++) {
				ret.add(new DropTable(n.string + "temp" + count0));
				ret.add(new DropTable(n.string + "tempNoAttrs" + count0));
			}

			ret.addAll(applySubst(sig, n, out));

			ret.add(new DropTable(n.string + "_subst"));
		}

		return ret;
	}

	private static Collection<PSM> applySubst(Signature sig, Node N, String out) {
		List<PSM> ret = new LinkedList<>();

		Map<String, String> attrs = new HashMap<>();
		attrs.put("c0", PSM.VARCHAR());
		attrs.put("c1", PSM.VARCHAR());

		List<Pair<Pair<String, String>, Pair<String, String>>> where = new LinkedList<>();
		Map<String, String> from = new HashMap<>();
		Map<String, Pair<String, String>> select = new HashMap<>();

		from.put(N + "_subst", N + "_subst");
		select.put("c1", new Pair<>(N + "_subst", "c1"));
		select.put("c0", new Pair<>(N + "_subst", "c1"));

		Flower f = new Flower(select, from, where);

		ret.add(new CreateTable("relationalize_temp", attrs, false));
		ret.add(new InsertSQL("relationalize_temp", f));

		ret.add(new DropTable(out + "_" + N.string));
		ret.add(new CreateTable(out + "_" + N.string, attrs, false));
		ret.add(new InsertSQL(out + "_" + N.string, new CopyFlower(
				"relationalize_temp")));

		ret.add(new DropTable("relationalize_temp"));

		for (Edge n : sig.edges) {
			if (!n.source.equals(N)) {
				continue;
			}

			where = new LinkedList<>();
			from = new HashMap<>();
			select = new HashMap<>();

			from.put(N + "_subst", N + "_subst");
			from.put(out + "_" + n.name, out + "_" + n.name);
			where.add(new Pair<>(new Pair<>(N + "_subst", "c0"), new Pair<>(out
					+ "_" + n.name, "c0")));
			select.put("c0", new Pair<>(N + "_subst", "c1"));
			select.put("c1", new Pair<>(out + "_" + n.name, "c1"));

			f = new Flower(select, from, where);

			ret.add(new CreateTable("relationalize_temp", attrs, false));
			ret.add(new InsertSQL("relationalize_temp", f));

			ret.add(new DropTable(out + "_" + n.name));
			ret.add(new CreateTable(out + "_" + n.name, attrs, false));
			ret.add(new InsertSQL(out + "_" + n.name, new CopyFlower(
					"relationalize_temp")));

			ret.add(new DropTable("relationalize_temp"));
		}
		for (Attribute<Node> n : sig.attrs) {
			if (!n.source.equals(N)) {
				continue;
			}

			where = new LinkedList<>();
			from = new HashMap<>();
			select = new HashMap<>();

			from.put(N + "_subst", N + "_subst");
			from.put(out + "_" + n.name, out + "_" + n.name);
			where.add(new Pair<>(new Pair<>(N + "_subst", "c0"), new Pair<>(out
					+ "_" + n.name, "c0")));
			select.put("c0", new Pair<>(N + "_subst", "c1"));
			select.put("c1", new Pair<>(out + "_" + n.name, "c1"));

			f = new Flower(select, from, where);

			ret.add(new CreateTable("relationalize_temp", attrs, false));
			ret.add(new InsertSQL("relationalize_temp", f));

			ret.add(new DropTable(out + "_" + n.name));
			ret.add(new CreateTable(out + "_" + n.name, attrs, false));
			ret.add(new InsertSQL(out + "_" + n.name, new CopyFlower(
					"relationalize_temp")));

			ret.add(new DropTable("relationalize_temp"));
		}
		for (Edge n : sig.edges) {
			if (!n.target.equals(N)) {
				continue;
			}

			where = new LinkedList<>();
			from = new HashMap<>();
			select = new HashMap<>();

			from.put(N + "_subst", N + "_subst");
			from.put(out + "_" + n.name, out + "_" + n.name);
			where.add(new Pair<>(new Pair<>(N + "_subst", "c0"), new Pair<>(out
					+ "_" + n.name, "c1")));
			select.put("c1", new Pair<>(N + "_subst", "c1"));
			select.put("c0", new Pair<>(out + "_" + n.name, "c0"));

			f = new Flower(select, from, where);

			ret.add(new CreateTable("relationalize_temp", attrs, false));
			ret.add(new InsertSQL("relationalize_temp", f));

			ret.add(new DropTable(out + "_" + n.name));
			ret.add(new CreateTable(out + "_" + n.name, attrs, false));
			ret.add(new InsertSQL(out + "_" + n.name, new CopyFlower(
					"relationalize_temp")));

			ret.add(new DropTable("relationalize_temp"));
		}

		// then do n.target
		// then do attributes

		return ret;
	}

	private static List<PSM> copy(Signature sig, String out, String in) {
		List<PSM> ret = new LinkedList<>();

		for (Node n : sig.nodes) {
			Map<String, String> attrs = new HashMap<>();
			attrs.put("c0", PSM.VARCHAR());
			attrs.put("c1", PSM.VARCHAR());
			ret.add(new CreateTable(out + "_" + n.string, attrs, false));
			ret.add(new InsertSQL(out + "_" + n.string, new CopyFlower(in + "_"
					+ n.string)));
		}
		for (Attribute<Node> n : sig.attrs) {
			Map<String, String> attrs = new HashMap<>();
			attrs.put("c0", PSM.VARCHAR());
			attrs.put("c1", n.target.toString());
			ret.add(new CreateTable(out + "_" + n.name, attrs, false));
			ret.add(new InsertSQL(out + "_" + n.name, new CopyFlower(in + "_"
					+ n.name)));
		}
		for (Edge n : sig.edges) {
			Map<String, String> attrs = new HashMap<>();
			attrs.put("c0", PSM.VARCHAR());
			attrs.put("c1", PSM.VARCHAR());
			ret.add(new CreateTable(out + "_" + n.name, attrs, false));
			ret.add(new InsertSQL(out + "_" + n.name, new CopyFlower(in + "_"
					+ n.name)));
		}

		return ret;
	}

}
