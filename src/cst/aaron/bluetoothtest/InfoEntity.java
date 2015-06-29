package cst.aaron.bluetoothtest;

public class InfoEntity {
	private int signal_color_code=1; // 1: red, 2:yellow, 3: green;
	private int signal_time=1; // default time is one second;
	private int direction_code=1; // 1: right and straight, 2: left turn; 3: straight;
	
	public static final int SIGNAL_NONE=0,SIGNAL_RED=1,SIGNAL_YELLOW=2,SINGAL_GREEN=3;
	public static final int SIGNAL_DIRECTION_RL=1,SIGNAL_DIRECTION_LEFT=2,SIGNAL_DIRECTION_STRAIGHT=3;
	
	public int getSignal_color_code() {
		return signal_color_code;
	}
	public void setSignal_color_code(int signal_color_code) {
		this.signal_color_code = signal_color_code;
	}
	public int getSignal_time() {
		return signal_time;
	}
	public void setSignal_time(int signal_time) {
		this.signal_time = signal_time;
	}
	public int getDirection_code() {
		return direction_code;
	}
	public void setDirection_code(int direction_code) {
		this.direction_code = direction_code;
	}
	
	

}
