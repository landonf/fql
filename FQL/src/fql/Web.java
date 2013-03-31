package fql;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.categoricaldata.api.BackEnd;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;


public class Web extends HttpServlet {

	BackEnd backend;

	@Override
	public void init() throws ServletException {
		super.init();
		this.backend = new FQLBackEnd();
	}

	public static void main(String[] args) throws Exception {
		serve(8085).join();
	}

	public static Server serve(int port) throws Exception {
		Server server = new Server(port);
		ServletHandler handler = new ServletHandler();
		server.setHandler(handler);
		handler.addServletWithMapping(Web.class, "/*");
		// ServletHolder sh = new ServletHolder(Web.class);
		// sh.getRegistration().setMultipartConfig(new
		// MultipartConfigElement("data/tmp", 1048576, 1048576, 262144));

		server.start();
		return server;
	}

	static String readFrom(InputStream in) throws IOException {
		InputStreamReader is = new InputStreamReader(in);
		StringBuilder sb = new StringBuilder();
		BufferedReader br = new BufferedReader(is);
		String read = br.readLine();

		while (read != null) {
			// System.out.println(read);
			sb.append(read);
			read = br.readLine();
		}
		// System.out.println("returning " + sb.toString());
		return sb.toString().trim();
	}

	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		String inst = readFrom(request.getPart("area1").getInputStream());
		String map = readFrom(request.getPart("area2").getInputStream());
		String sel = readFrom(request.getPart("selected").getInputStream());
		String ver = backend.version();
		
		String res;
		try {
			switch (sel) {
			case "delta" :
				res = backend.delta(inst, map);
				break;
			case "sigma" :
				res = backend.sigma(inst, map);
				break;
			case "pi" :
				res = backend.pi(inst, map);
				break;
			case "iso" :
				res = backend.iso(inst, map);
				break;
			default:
				throw new RuntimeException();
			}
		} catch (Exception e) {
			res = e.getMessage();
		}
		
		response.setContentType("text/html");
		response.setStatus(HttpServletResponse.SC_OK);
		response.getWriter().println(html(inst, map, res, sel, ver, backend.readme()));

			
	}

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");
		response.setStatus(HttpServletResponse.SC_OK);
		response.getWriter().println(
				html("", "", "", "delta", backend.version(), backend.readme()));
	}

	String html(String area1, String area2, String result, String selected,
			String version, String readme) {
		return "\n<html>"
				+ "\n<head>"
				+ "\n<title>FDM - Generic Web/JSON Interface</title>"
				+ "\n</head>"
				+ "\n<body>"
				+ "\n<p>Use this page to compute pullbacks and pushforwards.<br>To check isomorphism, put instances in both input boxes."
				+ "\n</p>"
				+ "\n<form action=\".\" enctype=\"multipart/form-data\" method=\"post\">"
				+ "\nInput dataset:" + "\n<br>"
				+ "\n<TEXTAREA NAME=\"area1\" ROWS=\"8\" COLS=\"40\">"
				+ area1
				+ "\n</TEXTAREA>"
				+ "\n<br><br>"
				+ "\nMapping:"
				+ "\n<br>"
				+ "\n<TEXTAREA NAME=\"area2\" ROWS=\"8\" COLS=\"40\">"
				+ area2
				+ "\n</TEXTAREA>"
				+ "\n<br>"
				+ "\nQuery type:"
				+ "\n<select name=\"selected\">"
				+ "\n  <option value =\"delta\""
				+ (selected.equals("delta") ? "selected = \"selected\"" : "")
				+ ">Delta</option>"
				+ "\n  <option value =\"sigma\""
				+ (selected.equals("sigma") ? "selected = \"selected\"" : "")
				+ ">Sigma</option>"
				+ "\n  <option value =\"pi\""
				+ (selected.equals("pi") ? "selected = \"selected\"" : "")
				+ ">Pi</option>"
				+ "\n  <option value =\"iso\""
				+ (selected.equals("iso") ? "selected = \"selected\"" : "")
				+ ">Isomorphism</option>"
				+ "\n</select>"
				+ "\n"
				+ "\n<input type=\"submit\" />"
				+ "\n</form>"
				+ "\nResult:"
				+ "\n<br>"
				+ "\n<TEXTAREA NAME=\"result\" ROWS=\"8\" COLS=\"40\">"
				+ result
				+ "\n</TEXTAREA>"
				+ "\n<br><br>"
				+ "\nBack-end: "
				+ version + "\n</body>" + "\n</html>"
				+ "<br><br>" + readme
				+ "\n";
	}

}