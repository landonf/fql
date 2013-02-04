package fql;

public class Keygen extends RA {
	
	RA e;
	
	public Keygen(RA e) {
		this.e = e;
	}

	@Override
	public String toString() {
		return "(KEYGEN " + e + ")";
	}
	

}
