package si.f5.yagi.gaugecontroller.gui;

public enum TimeSpan {

	SEC_5 (5,  "5 sec"),
	SEC_10(10, "10 sec"),
	SEC_30(30, "30 sec"),
	
	MIN_1 (60,  "1 min"),
	MIN_2 (120, "2 min"),
	MIN_5 (300, "5 min"),
	
	MIN_10 (600,  "10 min"),
	MIN_20 (1200,  "20 min"),
	MIN_30 (1800, "30 min"),
	
	HOUR_1(3600, "1 hour"),
	HOUR_2(7200, "2 hour"),
	HOUR_5(18000, "5 hour"),
	
	HOUR_10(36000, "10 hour"),
	HOUR_20(72000, "20 hour"),
	HOUR_50(180000, "50 hour"),
	
	HOUR_100(360000, "100 hour"),
	
	;
	
	
	private final int sec;
	private final String display;
	
	private TimeSpan(int sec, String display) {
		this.sec = sec;
		this.display = display;
	}
	
	public int seconds() {
		return this.sec;
	}
	
	@Override
	public String toString() {
		return this.display;
	}
	
}

