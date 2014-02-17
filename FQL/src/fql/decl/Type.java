package fql.decl;

import java.util.Set;

import fql.sql.PSM;

/**
 * 
 * @author ryan
 *
 * Represents types for attributes
 */
public abstract class Type {
	
	public abstract String psm();

	public abstract boolean in(Object o);
	
	@Override
	public abstract boolean equals(Object o);
	
	@Override
	public abstract int hashCode();
	
	public static class Enum extends Type {
		public String name;
		public Set<String> values;
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((name == null) ? 0 : name.hashCode());
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
			Enum other = (Enum) obj;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			return true;
		}
		
		public Object q(Object o) {
			String s = o.toString();
			if ((s.contains(" ") || s.contains("-") || s.length() == 0) && !s.contains("\"")) {
				return "\"" + s + "\"";
			}
			return s;
		}
		
		public String printFull() {
			String ret = "{";
			boolean b = false;
			for (String s : values) {
				if (b) {
					ret += ", ";
				}
				b = true;
				ret += q(s);
			}
			return ret + "}";
		}
		
		@Override
		public String toString() {
			return name;
		}
		public Enum(String name, Set<String> values) {
			super();
			this.name = name;
			this.values = values;
			if (values.size() == 0) {
				throw new RuntimeException("Empty enum: " + name);
			}
		}
		@Override
		public boolean in(Object o) {
			return values.contains(o);
		}
		@Override
		public String psm() {
			return PSM.VARCHAR();
		}
		
		
	}
	
	public static class Int extends Type {

		@Override
		public boolean equals(Object o) {
			if (o instanceof Int) {
				return true;
			}
			return false;
		}

		@Override
		public int hashCode() {

			return 0;
		}

		@Override
		public String toString() {
			return "int";
		}

		@Override
		public boolean in(Object o) {
			try {
				Integer.parseInt(o.toString());
				return true;
			} catch (Exception e) {
				return false;
			}
		}
		@Override
		public String psm() {
			return PSM.INTEGER;
		}

	}
	
	public static class Varchar extends Type {
		
		@Override
		public String psm() {
			return PSM.VARCHAR();
		}


		@Override
		public boolean equals(Object o) {
			if (o instanceof Varchar) {
				return true;
			}
			return false;
		}

		@Override
		public int hashCode() {
			return 0;
		}

		@Override
		public String toString() {
			return "string";
		}

		@Override
		public boolean in(Object o) {
			return (o instanceof String);
		}
	}
}
