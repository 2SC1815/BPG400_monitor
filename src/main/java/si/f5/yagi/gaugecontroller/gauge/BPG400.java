package si.f5.yagi.gaugecontroller.gauge;

import java.util.function.Consumer;

import javax.sound.midi.Receiver;

import com.fazecast.jSerialComm.SerialPort;

public class BPG400 {
	
	private static final byte[] START_DEGAS_COMMAND = {0x03, 0x10, 0x5D, (byte) 0x94, 0x01};
	private static final byte[] STOP_DEGAS_COMMAND  = {0x03, 0x10, 0x5D, 0x69, (byte) 0xD6};
	
	private Receiver receiver;
	private SerialPort port;
	private Consumer<BPG400> event = null;

	
	private int receivedData = 0;
	

	public BPG400(SerialPort port, Consumer<BPG400> onReceived) {
		
		this.event = onReceived;
		
		this.receiver = new Receiver(this::update, port);
		this.receiver.start();
		
		this.port = port;
		
	}
	
	public int getRawData() {
		return this.receivedData;
	}
	
	public int getStatus() {
		return (this.receivedData >> (8*3)) & 0xFF;
	}
	
	public double getPressure(Unit unit) {
		return unit.calculatePressure(this.getRawData());
	}
	
	public Emission getEmissionStatus() {
		
		switch (this.getStatus() & 0x3) {
		
		case 0:
			return Emission.OFF;
			
		case 1:
			return Emission.ON_25MICRO;
			
		case 2:
			return Emission.ON_5MILLI;
			
		case 3:
			return Emission.ON_DEGAS;
			
		default:
			return null;
		
		}
				
	}
	
	
	public void degasControl(boolean degas) {
		
		if (port.isOpen()) {
			port.writeBytes(degas ? START_DEGAS_COMMAND : STOP_DEGAS_COMMAND, 5);
		}
		
	}
	
	private synchronized void update(byte[] buffer) {
		
		int data = 0;
		
		for (int i=0; i<Integer.BYTES; i++) {
			data <<= 8;
			data |= (buffer[i] & 0xFF);
		}
		//System.out.println(Integer.toHexString(data));
		
		this.receivedData = data;
		
		if (this.event != null) {
			this.event.accept(this);
		}
	}
	
	
	private static class Receiver extends Thread {
		
		private Consumer<byte[]> func;
		private SerialPort port;
		private byte[] buffer = new byte[7];
		private int checksum = 0;
		
		public Receiver(Consumer<byte[]> updateFunction, SerialPort port) {
			
			this.port = port;
			
			this.port.setBaudRate(9600);
			this.port.setNumDataBits(8);
			this.port.setNumStopBits(1);
			
			this.func = updateFunction;
		}
		
		@Override
		public void run() {
			
			try {
				
				this.port.openPort();
				
				while (true) {
					
					
					if (this.port.bytesAvailable() < 9) {
						continue;
					}
					
					this.port.readBytes(buffer, 1);
					if (7 != buffer[0]) {
						continue;
					}
					this.port.readBytes(buffer, 1);
					if (5 != buffer[0]) {
						continue;
					}
					
					this.port.readBytes(buffer, buffer.length);
					this.port.flushIOBuffers();

					checksum = 5;
					for (int o=0; o<buffer.length-1; o++) {
						checksum += (int)(buffer[o]&0xFF);
					}
					checksum &= 0xFF;
					
					if (checksum != (int)(buffer[6]&0xFF)) {
						continue;
					}
					
					
					this.func.accept(buffer);
				}
				
			} finally {
				
				this.port.closePort();
				
			}
			
			
		}
		
		
	}

}
