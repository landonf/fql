package fql;

public interface BackEnd2 {

	public String delta(String instance, String mapping) throws Exception;
	
	public String sigma(String instance, String mapping) throws Exception;
	
	public String pi(String instance, String mapping) throws Exception;
	
	public String iso(String instance1, String instance2) throws Exception;
	
	public String version(); // e.g., "FQL" or "Metaphor"

	public String readme(); // e.g., "Don't use spaces in values"
	
}
