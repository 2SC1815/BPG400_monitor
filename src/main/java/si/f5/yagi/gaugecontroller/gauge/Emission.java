package si.f5.yagi.gaugecontroller.gauge;

public enum Emission {

	OFF("OFF"),
	ON_25MICRO("25μA"),
	ON_5MILLI("5mA"),
	ON_DEGAS("Degas"),
	
	;
	
	private final String name;
	
	private Emission(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return this.name;
	}
	
}
