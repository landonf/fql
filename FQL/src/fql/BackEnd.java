package fql;

public interface BackEnd {

	public String delta(String instance, String mapping) throws Exception;
	
	public String sigma(String instance, String mapping) throws Exception;
	
	public String pi(String instance, String mapping) throws Exception;
	
	public String iso(String instance1, String instance2) throws Exception;
	
	public String version();

	public String readme();
	
}
