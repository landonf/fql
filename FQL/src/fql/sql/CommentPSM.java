package fql.sql;

import java.util.Map;
import java.util.Set;

public class CommentPSM extends PSM {

	String text;
	
	
	
	@Override
	public String toString() {
		return " /* " + text + " */";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((text == null) ? 0 : text.hashCode());
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
		CommentPSM other = (CommentPSM) obj;
		if (text == null) {
			if (other.text != null)
				return false;
		} else if (!text.equals(other.text))
			return false;
		return true;
	}

	public CommentPSM(String text) {
		this.text = text;
	}

	@Override
	public void exec(PSMInterp interp,
			Map<String, Set<Map<Object, Object>>> state) {		
	}

	@Override
	public String toPSM() {
		return "/* " + text + " */";
	}

}
