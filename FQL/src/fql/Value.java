package fql;

public class Value<Y, X> {

	X x;
	
	Value<Y,X>[] tuple;
	
	Y tag;
	Value<Y,X> tagCargo;
	
	VALUETYPE which;
	
	@Override
	public String toString() {
		String s = "";
		if (which == VALUETYPE.ATOM) {
			s = x.toString();
		} else if (which == VALUETYPE.RECORD) {
			s = "(" + printNicely(tuple) + ")";
		} else if (which == VALUETYPE.CHOICE) {
			s = "<" + tag.toString() + "_" + tagCargo.toString() +">";
		}
		return s;
	}
	
	private String printNicely(Value<Y, X>[] G) {
		String s = " ";
		for (Value<Y,X> g : G) {
			s += (g + " ");
		}
		return s;
	}

	@Override
	public int hashCode() {
		return 0;
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Value)) {
			throw new RuntimeException();
//			return false;
		}
		Value that = (Value) o;
		if (this.which == VALUETYPE.ATOM && that.which == VALUETYPE.ATOM) {
			return (this.x.equals(that.x));
		}
		if (this.which == VALUETYPE.RECORD && that.which == VALUETYPE.RECORD) {
			if (this.tuple.length != that.tuple.length) {
				return false;
			}
			for (int i = 0; i < this.tuple.length; i++) {
				if (!this.tuple[i].equals(that.tuple[i])) {
					return false;
				}
			}
			return true;
		}
		if (this.which == VALUETYPE.CHOICE && that.which == VALUETYPE.CHOICE) {
			return (this.tag.equals(that.tag) && this.tagCargo.equals(that.tagCargo));
		}
		return false;
	}
	
	static enum VALUETYPE { ATOM, RECORD, CHOICE };
	
	Value(Y tag, Value<Y,X> tagCargo) {
		this.tag = tag;
		this.tagCargo = tagCargo;
		this.which = VALUETYPE.CHOICE;
	}
	
	Value(X x) {
		this.x = x;
		this.which = VALUETYPE.ATOM;
	}
	
	Value(Value<Y,X>[] tuple) {
		this.tuple = tuple;
		this.which = VALUETYPE.RECORD;
	}
	
	
}
