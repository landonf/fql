package fql;


public class Select extends RA {
	
	int i, j;
	RA e;
	
	public Select(RA e, int i, int j) {
		this.i = i;
		this.j = j;
		this.e = e;
	}

	@Override
	public String toString() {
		return "(SELECT " + i + "=" + j + " " + e + ")";
	}
	
	

}
