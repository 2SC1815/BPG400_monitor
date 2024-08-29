package si.f5.yagi.gaugecontroller.gauge;

public enum Unit {

	MILLIBAR(12.5, "mbar"),
	TORR(12.625, "Torr"),
	PASCAL(10.5, "Pa")
	
	;
	
	private final double coeff;
	private final String symbol;
	
	private Unit(double coeff, String symbol) {
		this.coeff = coeff;
		this.symbol = symbol;
	}
	
	public double calculateLogPressure(int raw) {
		return (raw & 0xFFFF) /4000d-this.coeff;
	}
	
	public double calculatePressure(int raw) {
		return Math.pow(10, this.calculateLogPressure(raw));
	}
	
	
	@Override
	public String toString() {
		return this.symbol;
	}
	
}
