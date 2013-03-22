package fql;


public class FQLBackEnd implements BackEnd {

	@Override
	public String delta(String instance, String mapping) throws Exception {
		Mapping m = Mapping.fromjson(mapping);
		Instance i = Instance.fromjson(instance);		
		Instance j = new Instance("", m.source, m.evalDelta(i));
		return j.tojson();
	}

	@Override
	public String sigma(String instance, String mapping) {
		return instance;
	}

	@Override
	public String pi(String instance, String mapping) {
		return "todo";
	}

	@Override
	public String iso(String instance1, String instance2) {
		 throw new RuntimeException("can't iso yet");
	}

	@Override
	public String version() {
		return "FQL, using SQL-ish queries";
	}
	
	@Override
	public String readme() {
		return "Note: data values cannot contain spaces.";
	}

}
