package fql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fql.decl.FQLProgram;
import fql.decl.InstExp;
import fql.decl.SigExp;
import fql.decl.TransExp;
import fql.sql.FullSigma;
import fql.sql.PSM;

public class JDBCBridge {

	/*
	public static String populate() {
		try {
			String ret = "";
			Class.forName(DEBUG.debug.jdbcClass);	
			Connection Conn = DriverManager.getConnection(DEBUG.debug.jdbcUrl);
			DatabaseMetaData m = Conn.getMetaData();
			ResultSet RS = m.getSchemas();
			while (RS.next() != false) { 
				String r = RS.getString("TABLE_SCHEM");
				ret += r;
			}
			m.
			
			return ret;
		} (Exception e) {
			e.printStackTrace();
			return e.getLocalizedMessage();
		}
	}
	*/

	
	public static Map<String, Set<Map<Object, Object>>>  go(List<PSM> sqls, List<PSM> drops, FQLProgram prog) {
		Map<String, Set<Map<Object, Object>>> ret = new HashMap<>();

		for (PSM sql : sqls) {
			if (sql instanceof FullSigma) {
				throw new RuntimeException("Cannot use JDBC with full sigma");
			}
		}

		//TODO jdbc for full sigma
		try {
			
			Class.forName(DEBUG.debug.jdbcClass);
			
			Connection Conn = DriverManager.getConnection(DEBUG.debug.jdbcUrl);
						
			Statement Stmt = Conn.createStatement();
			
			String[] prel = DEBUG.debug.prelude.split(";");
			for (String s : prel) {
				Stmt.execute(s);
			}
			
//			Stmt.execute("set @guid := 0");
			for (PSM sql : sqls) {
		//		System.out.println(sql);
				Stmt.execute(sql.toPSM());
			}

			for (String k : prog.insts.keySet()) {
				InstExp v = prog.insts.get(k);
				SigExp.Const t = v.type(prog).toConst(prog);
				for (String n : t.nodes) {
					ResultSet RS = Stmt.executeQuery("SELECT c0,c1 FROM " + k + "_" + n);
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
					ResultSet RS = Stmt.executeQuery("SELECT c0,c1 FROM " + k + "_" + n.first);
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
					ResultSet RS = Stmt.executeQuery("SELECT c0,c1 FROM " + k + "_" + n.first);
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
			
			for (String k : prog.transforms.keySet()) {
				TransExp v = prog.transforms.get(k);
				SigExp.Const t = prog.insts.get(v.type(prog).first).type(prog).toConst(prog);
				for (String n : t.nodes) {
					ResultSet RS = Stmt.executeQuery("SELECT c0,c1 FROM " + k + "_" + n);
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

			for (PSM k : drops) {
				Stmt.execute(k.toPSM());
			}
			
			String[] prel0 = DEBUG.debug.afterlude.split(";");
			for (String s : prel0) {
				if (s.trim().length() > 0) {
					Stmt.execute(s);
				}
			}

//			Conn.commit();
			
			return ret;
			
		} catch (Exception exception) {
			exception.printStackTrace();
			throw new RuntimeException("JDBC error: "
					+ exception.getLocalizedMessage());
		}
	}
}
