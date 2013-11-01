
package fql;

@SuppressWarnings("serial")
public class LineException extends RuntimeException {

		public String decl;
		public String kind;
		
		public LineException(String string, String decl, String kind) {
			super(string);
			this.decl = decl;
			this.kind = kind;
		}

	
}
