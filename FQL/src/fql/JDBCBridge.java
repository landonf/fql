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

import fql.decl.Attribute;
import fql.decl.Driver;
import fql.decl.Edge;
import fql.decl.FQLProgram;
import fql.decl.InstExp;
import fql.decl.InstOps;
import fql.decl.Node;
import fql.decl.SigExp;
import fql.decl.Signature;
import fql.decl.TransExp;
import fql.sql.FullSigma;
import fql.sql.FullSigmaTrans;
import fql.sql.InsertValues;
import fql.sql.PSM;
import fql.sql.PSMGen;
import fql.sql.PSMInterp;

//TODO always execute postlude

/**
 * 
 * @author ryan
 * 
 *         Class for communicating with external sql engines over jdbc.
 */
public class JDBCBridge {

	// TODO test on jdbc again
	
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
							//throw new RuntimeException("Cannot use full sigma with jdbc/h2");
							List<PSM> xxx = v.accept(k, ops).first;
							if (xxx.size() != 1) {
								throw new RuntimeException();
							}
							FullSigma yyy = (FullSigma) xxx.get(0);
							int theguid = getGuid(Stmt);
							interp.guid = theguid;
							yyy.exec(interp, ret);
							Stmt.execute("SET @guid = " + interp.guid);
							psm.addAll(makeInserts(k, ret, v.type(prog).toSig(prog)));
						} else if (v instanceof InstExp.External && DEBUG.debug.sqlKind == DEBUG.SQLKIND.H2) {
							
						} else {
							psm.addAll(v.accept(k, ops).first);
						}
						for (PSM sql : psm) {
//							System.out.println("exec " + sql.toPSM());
							Stmt.execute(sql.toPSM());
						}
						if (!(v instanceof InstExp.FullSigma)) {
							gatherInstance(prog, ret, Stmt, k, v);
						}
						break;
					}
					sqls.addAll(psm);
				} catch (Throwable re) {
					re.printStackTrace();
					LineException exn = new LineException(
							re.getLocalizedMessage(), k, "instance");
					if (DEBUG.debug.continue_on_error) {
						exns.add(exn);
					} else {
						if (DEBUG.debug.sqlKind == DEBUG.SQLKIND.JDBC) {
							String[] prel0 = DEBUG.debug.afterlude.split(";");
							for (String s : prel0) {
								if (s.trim().length() > 0) {
									Stmt.execute(s);
								}
							}
						}
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
					sqls.addAll(psm);
					switch (DEBUG.debug.sqlKind) {
					case NATIVE:
						psm.addAll(v.accept(k, ops));
						interp.interpX(psm, ret);
						break;
					default:
						if (v instanceof TransExp.FullSigma) {
							List<PSM> xxx = v.accept(k, ops);
							if (xxx.size() != 1) {
								throw new RuntimeException();
							}
							FullSigmaTrans yyy = (FullSigmaTrans) xxx.get(0);
							yyy.exec(interp, ret);
							psm.addAll(makeInserts(k, ret, s));
						} else if (v instanceof TransExp.External && DEBUG.debug.sqlKind == DEBUG.SQLKIND.H2) {
							
						} else {
							psm.addAll(v.accept(k, ops));
						}
						for (PSM sql : psm) {
							Stmt.execute(sql.toPSM());
						}
						if (!(v instanceof TransExp.FullSigma)) {
							gatherTransform(prog, ret, Stmt, k, v);
						}
						break;
					}

				} catch (Throwable re) {
					re.printStackTrace();
					LineException exn = new LineException(
							re.getLocalizedMessage(), k, "transform");
					if (DEBUG.debug.continue_on_error) {
						exns.add(exn);
					} else {
						if (DEBUG.debug.sqlKind == DEBUG.SQLKIND.JDBC) {
							String[] prel0 = DEBUG.debug.afterlude.split(";");
							for (String s : prel0) {
								if (s.trim().length() > 0) {
									Stmt.execute(s);
								}
							}
						}
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
			if (exception instanceof LineException) {
				throw ((LineException)exception);
			}
			exception.printStackTrace();
			// System.out.println(ret);
			throw new RuntimeException(exception.getLocalizedMessage());
		}
	}

	private static int getGuid(Statement stmt) throws SQLException {
		stmt.execute("CREATE TABLE GUID_TEMP_TABLE_XXX_YYY(C0 INTEGER)");
		stmt.execute("INSERT INTO GUID_TEMP_TABLE_XXX_YYY VALUES (@GUID)");
		ResultSet RS = stmt
				.executeQuery("SELECT * FROM GUID_TEMP_TABLE_XXX_YYY");
		Integer i = null;
		while (RS.next() != false) {
			i = (Integer) RS.getObject("C0");
			break;
		}
		RS.close();
		stmt.execute("DROP TABLE GUID_TEMP_TABLE_XXX_YYY");
		if (i == null) {
			throw new RuntimeException("Cannot get GUID from database");
		}
		return i.intValue();
	}

	private static List<PSM> makeInserts(String k,
			Map<String, Set<Map<Object, Object>>> state, Signature sig) {
		List<PSM> ret = new LinkedList<>();
		
		List<String> attrs = new LinkedList<>();
		attrs.add("c0");
		attrs.add("c1");
		for (Node n : sig.nodes) {
			Set<Map<Object, Object>> v = state.get(k + "_" + n.string);
			if (v.size() == 0) {
				continue;
			}
			ret.add(new InsertValues(k + "_" + n.string, attrs, v));
		}
		for (Edge e : sig.edges) {
			Set<Map<Object, Object>> v = state.get(k + "_" + e.name);
			if (v.size() == 0) {
				continue;
			}
			ret.add(new InsertValues(k + "_" + e.name, attrs, v));
		}
		for (Attribute<Node> a : sig.attrs) {
			Set<Map<Object, Object>> v = state.get(k + "_" + a.name);
			if (v.size() == 0) {
				continue;
			}
			ret.add(new InsertValues(k + "_" + a.name, attrs, v));			
		}
		
		return ret;
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
				m.put("c0", Integer.parseInt(RS.getObject("c0").toString()));
				m.put("c1", Integer.parseInt(RS.getObject("c1").toString()));
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
				m.put("c0", Integer.parseInt(RS.getObject("c0").toString()));
				m.put("c1", Integer.parseInt(RS.getObject("c1").toString()));
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
				m.put("c0", Integer.parseInt(RS.getObject("c0").toString()));
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
				m.put("c0", Integer.parseInt(RS.getObject("c0").toString()));
				m.put("c1", Integer.parseInt(RS.getObject("c1").toString()));
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
