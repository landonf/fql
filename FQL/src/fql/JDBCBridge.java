package fql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fql.decl.Driver;
import fql.decl.FQLProgram;
import fql.decl.InstExp;
import fql.decl.InstOps;
import fql.decl.SigExp;
import fql.decl.Signature;
import fql.decl.TransExp;
import fql.sql.PSM;
import fql.sql.PSMGen;
import fql.sql.PSMInterp;

/**
 * 
 * @author ryan
 * 
 *         Class for communicating with external sql engines over jdbc.
 */
public class JDBCBridge {

	//TODO add externals for transforms
	
	/*
	 * public JDBCBridge(FQLProgram prog) throws SQLException { h2conn =
	 * DriverManager.getConnection("jdbc:h2:mem:"); stmt =
	 * h2conn.createStatement(); this.prog = prog; }
	 */

	// TODO if full sigmas are required, should *push* results to DB

	public static Triple<Map<String, Set<Map<Object, Object>>>, String, List<Throwable>> run(
			FQLProgram prog) {
		Map<String, Set<Map<Object, Object>>> ret = new HashMap<>();
		List<Throwable> exns = new LinkedList<>();
		InstOps ops = new InstOps(prog);
		PSMInterp interp = new PSMInterp();
		Connection Conn = null;
		Statement Stmt = null;
		List<PSM> sqls = new LinkedList<PSM>();
		try {
			switch (DEBUG.debug.sqlKind) {
			case H2:
				Class.forName("org.h2.Driver");
				Conn = DriverManager.getConnection("jdbc:h2:mem:");
				Stmt = Conn.createStatement();
				Stmt.execute("SET @GUID = 0");
				break;
			case JDBC:
				Class.forName(DEBUG.debug.jdbcClass);
				Conn = DriverManager.getConnection(DEBUG.debug.jdbcUrl);
				Stmt = Conn.createStatement();
				String[] prel = DEBUG.debug.prelude.split(";");
				for (String s : prel) {
					Stmt.execute(s);
				}
				break;
			case NATIVE:
				break;
			default:
				throw new RuntimeException();
			}

			for (String k : prog.insts.keySet()) {
				try {
					InstExp v = prog.insts.get(k);
					List<PSM> psm = new LinkedList<PSM>();
					psm.addAll(PSMGen.makeTables(k, v.type(prog).toSig(prog),
							false));
					switch (DEBUG.debug.sqlKind) {
					case NATIVE:
						psm.addAll(v.accept(k, ops).first);
						interp.interpX(psm, ret);
						break;
					default:
						if (v instanceof InstExp.FullSigma) {
							throw new RuntimeException("Cannot use full sigma with jdbc/h2");
						}
						if (v instanceof InstExp.External && DEBUG.debug.sqlKind == DEBUG.SQLKIND.H2) {
							
						} else {
							psm.addAll(v.accept(k, ops).first);
						}
						for (PSM sql : psm) {
							Stmt.execute(sql.toPSM());
						}
						gatherInstance(prog, ret, Stmt, k, v);
						break;
					}
					sqls.addAll(psm);
				} catch (RuntimeException re) {
					re.printStackTrace();
					LineException exn = new LineException(
							re.getLocalizedMessage(), k, "instance");
					if (DEBUG.debug.continue_on_error) {
						exns.add(exn);
					} else {
						throw exn;
					}
				}
			}

			for (String k : prog.transforms.keySet()) {
				try {
					List<PSM> psm = new LinkedList<PSM>();
					TransExp v = prog.transforms.get(k);
					Pair<String, String> val = prog.transforms.get(k)
							.type(prog);
					InstExp i = prog.insts.get(val.first);
					Signature s = i.type(prog).toSig(prog);
					psm.addAll(PSMGen.makeTables(k, s, false));
					psm.addAll(v.accept(k, ops));
					sqls.addAll(psm);
					switch (DEBUG.debug.sqlKind) {
					case NATIVE:
						interp.interpX(psm, ret);
						break;
					default:
						if (v instanceof TransExp.FullSigma) {
							throw new RuntimeException("Cannot use full sigma with jdbc/h2");
						}

						for (PSM sql : psm) {
							Stmt.execute(sql.toPSM());
						}
						gatherTransform(prog, ret, Stmt, k, v);
					}

				} catch (RuntimeException re) {
					re.printStackTrace();
					LineException exn = new LineException(
							re.getLocalizedMessage(), k, "transform");
					if (DEBUG.debug.continue_on_error) {
						exns.add(exn);
					} else {
						throw exn;
					}
				}

			}
			List<PSM> drops = Driver.computeDrops(prog);

			if (DEBUG.debug.sqlKind == DEBUG.SQLKIND.JDBC) {
				for (PSM dr : drops) {
					Stmt.execute(dr.toPSM());
				}

				String[] prel0 = DEBUG.debug.afterlude.split(";");
				for (String s : prel0) {
					if (s.trim().length() > 0) {
						Stmt.execute(s);
					}
				}
			}

			String str = "";
			try {
				str = DEBUG.debug.prelude
						+ "\n\n"
						+ PSMGen.prettyPrint(sqls)
						+ "\n\n"
						+ (drops.size() == 0 ? "" : PSMGen.prettyPrint(drops)
								+ "\n\n") + DEBUG.debug.afterlude + "\n\n";
			} catch (RuntimeException re) {
				str = re.getLocalizedMessage();
			}

			return new Triple<>(ret, str, exns);
		} catch (Exception exception) {
			exception.printStackTrace();
			// System.out.println(ret);
			throw new RuntimeException("JDBC error: "
					+ exception.getLocalizedMessage());
		}
	}

	private static void gatherTransform(FQLProgram prog,
			Map<String, Set<Map<Object, Object>>> ret, Statement Stmt,
			String k, TransExp v) throws SQLException {
		SigExp.Const t = prog.insts.get(v.type(prog).first).type(prog)
				.toConst(prog);
		for (String n : t.nodes) {
			ResultSet RS = Stmt
					.executeQuery("SELECT c0,c1 FROM " + k + "_" + n);
			Set<Map<Object, Object>> ms = new HashSet<>();
			while (RS.next() != false) {
				Map<Object, Object> m = new HashMap<>();
				m.put("c0", RS.getObject("c0"));
				m.put("c1", RS.getObject("c1"));
				ms.add(m);
			}
			RS.close();
			ret.put(k + "_" + n, ms);
		}
		for (Triple<String, String, String> n : t.arrows) {
			ret.put(k + "_" + n.first, new HashSet<Map<Object, Object>>());
		}
		for (Triple<String, String, String> n : t.attrs) {
			ret.put(k + "_" + n.first, new HashSet<Map<Object, Object>>());
		}
	}

	private static void gatherInstance(FQLProgram prog,
			Map<String, Set<Map<Object, Object>>> ret, Statement Stmt,
			String k, InstExp v) throws SQLException {
		SigExp.Const t = v.type(prog).toConst(prog);

		for (String n : t.nodes) {
			ResultSet RS = Stmt
					.executeQuery("SELECT c0,c1 FROM " + k + "_" + n);
			Set<Map<Object, Object>> ms = new HashSet<>();
			while (RS.next() != false) {
				Map<Object, Object> m = new HashMap<>();
				m.put("c0", RS.getObject("c0"));
				m.put("c1", RS.getObject("c1"));
				ms.add(m);
			}
			RS.close();
			ret.put(k + "_" + n, ms);
		}
		for (Triple<String, String, String> n : t.attrs) {
			ResultSet RS = Stmt.executeQuery("SELECT c0,c1 FROM " + k + "_"
					+ n.first);
			Set<Map<Object, Object>> ms = new HashSet<>();
			while (RS.next() != false) {
				Map<Object, Object> m = new HashMap<>();
				m.put("c0", RS.getObject("c0"));
				m.put("c1", RS.getObject("c1"));
				ms.add(m);
			}
			RS.close();
			ret.put(k + "_" + n.first, ms);
		}
		for (Triple<String, String, String> n : t.arrows) {
			ResultSet RS = Stmt.executeQuery("SELECT c0,c1 FROM " + k + "_"
					+ n.first);
			Set<Map<Object, Object>> ms = new HashSet<>();
			while (RS.next() != false) {
				Map<Object, Object> m = new HashMap<>();
				m.put("c0", RS.getObject("c0"));
				m.put("c1", RS.getObject("c1"));
				ms.add(m);
			}
			RS.close();
			ret.put(k + "_" + n.first, ms);
		}
	}

	/*
	 * public static Map<String, Set<Map<Object, Object>>> h2(List<PSM> sqls,
	 * FQLProgram prog) {
	 * 
	 * for (PSM sql : sqls) { if (sql instanceof FullSigma) { throw new
	 * RuntimeException("Cannot use h2 with full sigma"); } }
	 * 
	 * try { Class.forName("org.h2.Driver"); Connection Conn =
	 * DriverManager.getConnection("jdbc:h2:mem:");
	 * 
	 * Statement Stmt = Conn.createStatement();
	 * 
	 * Stmt.execute("SET @GUID = 0");
	 * 
	 * for (PSM sql : sqls) { Stmt.execute(sql.toPSM()); }
	 * 
	 * Map<String, Set<Map<Object, Object>>> ret = gather(prog, Stmt);
	 * 
	 * return ret;
	 * 
	 * } catch (Exception exception) { exception.printStackTrace(); throw new
	 * RuntimeException("JDBC error: " + exception.getLocalizedMessage()); } }
	 */
	/*
	 * private static Map<String, Set<Map<Object, Object>>> gather( FQLProgram
	 * prog, Statement Stmt) throws SQLException { Map<String, Set<Map<Object,
	 * Object>>> ret = new HashMap<>();
	 * 
	 * for (String k : prog.insts.keySet()) { InstExp v = prog.insts.get(k);
	 * gatherInstance(prog, ret, Stmt, k, v); }
	 * 
	 * for (String k : prog.transforms.keySet()) { TransExp v =
	 * prog.transforms.get(k); gatherTransform(prog, ret, Stmt, k, v); } return
	 * ret; }
	 * 
	 * public static Map<String, Set<Map<Object, Object>>> go(List<PSM> sqls,
	 * List<PSM> drops, FQLProgram prog) { // Map<String, Set<Map<Object,
	 * Object>>> ret = new HashMap<>();
	 * 
	 * for (PSM sql : sqls) { if (sql instanceof FullSigma) { throw new
	 * RuntimeException("Cannot use JDBC with full sigma"); } }
	 * 
	 * try {
	 * 
	 * Class.forName(DEBUG.debug.jdbcClass);
	 * 
	 * Connection Conn = DriverManager.getConnection(DEBUG.debug.jdbcUrl);
	 * 
	 * Statement Stmt = Conn.createStatement();
	 * 
	 * String[] prel = DEBUG.debug.prelude.split(";"); for (String s : prel) {
	 * Stmt.execute(s); }
	 * 
	 * for (PSM sql : sqls) { Stmt.execute(sql.toPSM()); }
	 * 
	 * Map<String, Set<Map<Object, Object>>> ret = gather(prog, Stmt); /* for
	 * (String k : prog.insts.keySet()) { InstExp v = prog.insts.get(k);
	 * SigExp.Const t = v.type(prog).toConst(prog); for (String n : t.nodes) {
	 * ResultSet RS = Stmt.executeQuery("SELECT c0,c1 FROM " + k + "_" + n);
	 * Set<Map<Object, Object>> ms = new HashSet<>(); while (RS.next() != false)
	 * { Map<Object, Object> m = new HashMap<>(); m.put("c0",
	 * RS.getObject("c0")); m.put("c1", RS.getObject("c1")); ms.add(m); }
	 * RS.close(); ret.put(k + "_" + n, ms); } for (Triple<String, String,
	 * String> n : t.attrs) { ResultSet RS =
	 * Stmt.executeQuery("SELECT c0,c1 FROM " + k + "_" + n.first);
	 * Set<Map<Object, Object>> ms = new HashSet<>(); while (RS.next() != false)
	 * { Map<Object, Object> m = new HashMap<>(); m.put("c0",
	 * RS.getObject("c0")); m.put("c1", RS.getObject("c1")); ms.add(m); }
	 * RS.close(); ret.put(k + "_" + n.first, ms); } for (Triple<String, String,
	 * String> n : t.arrows) { ResultSet RS =
	 * Stmt.executeQuery("SELECT c0,c1 FROM " + k + "_" + n.first);
	 * Set<Map<Object, Object>> ms = new HashSet<>(); while (RS.next() != false)
	 * { Map<Object, Object> m = new HashMap<>(); m.put("c0",
	 * RS.getObject("c0")); m.put("c1", RS.getObject("c1")); ms.add(m); }
	 * RS.close(); ret.put(k + "_" + n.first, ms); } }
	 * 
	 * for (String k : prog.transforms.keySet()) { TransExp v =
	 * prog.transforms.get(k); SigExp.Const t =
	 * prog.insts.get(v.type(prog).first).type(prog) .toConst(prog); for (String
	 * n : t.nodes) { ResultSet RS = Stmt.executeQuery("SELECT c0,c1 FROM " + k
	 * + "_" + n); Set<Map<Object, Object>> ms = new HashSet<>(); while
	 * (RS.next() != false) { Map<Object, Object> m = new HashMap<>();
	 * m.put("c0", RS.getObject("c0")); m.put("c1", RS.getObject("c1"));
	 * ms.add(m); } RS.close(); ret.put(k + "_" + n, ms); } for (Triple<String,
	 * String, String> n : t.arrows) { ret.put(k + "_" + n.first, new
	 * HashSet<Map<Object, Object>>()); } for (Triple<String, String, String> n
	 * : t.attrs) { ret.put(k + "_" + n.first, new HashSet<Map<Object,
	 * Object>>()); } }
	 * 
	 * for (PSM k : drops) { Stmt.execute(k.toPSM()); }
	 * 
	 * String[] prel0 = DEBUG.debug.afterlude.split(";"); for (String s : prel0)
	 * { if (s.trim().length() > 0) { Stmt.execute(s); } }
	 * 
	 * return ret;
	 * 
	 * } catch (Exception exception) { exception.printStackTrace(); throw new
	 * RuntimeException("JDBC error: " + exception.getLocalizedMessage()); } }
	 */
}
