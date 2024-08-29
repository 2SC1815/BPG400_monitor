package si.f5.yagi.gaugecontroller.gauge;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class Log {
	
	private static final String SAVE_DIR = "gauge_log";
	
	private long initialTime = -1;
	public List<Entry> data = Collections.synchronizedList(new ArrayList<Entry>());
	private boolean overflow = false;
	
	
	public Log() {
		
		this.reset();
		
	}
	
	public void reset() {

		this.initialTime = -1;
		this.data.clear();
		this.overflow = false;
		
	}
	
	public long getInitialTime() {
		return this.initialTime;
	}
	
	
	public boolean isOverflow() {
		return this.overflow;
	}
	
	public void addEntry(int data) {
		
		if (this.overflow)
			return;
		
		if (initialTime <= 0) {
			initialTime = System.currentTimeMillis();
		}
		
		long time = (System.currentTimeMillis() - initialTime) / 10;
		
		if (time < Integer.MAX_VALUE) {
			
			this.data.add(new Entry((int)time, data));
			
		} else {
			
			this.overflow = true;
			
		}
		
	}
	
	public String date() {

        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");

        Date t = new Date(this.initialTime);
		
		return fmt.format(t);
	}
	
	public void save() {

        // 書き込みファイルの名前
        Path path = Paths.get(SAVE_DIR, "log_" + this.date() + ".dat");

        Path parent = path.getParent();
        if (!Files.exists(parent)) {
	        try {
				Files.createDirectory(parent);
			} catch (IOException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}
        }

        try (OutputStream out = Files.newOutputStream(path)) {
        	
            ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
    		out.write(buffer.putLong(this.initialTime).array());

        	synchronized (this.data) {
        	
	        	Iterator<Entry> iterator = this.data.iterator();
	
	            buffer = ByteBuffer.allocate(Integer.BYTES);
	            
	        	while (iterator.hasNext()) {
	        		
	        		Entry e = iterator.next();

	        		buffer.clear();
	        		out.write(buffer.putInt(e.getTimeStamp()).array());
	        		buffer.clear();
	        		out.write(buffer.putInt(e.getData()).array());
	        		
	        	}
        	
        	}

        } catch (IOException e) {
            e.printStackTrace();
        }
        
	}
	
	
	public void saveCsv( Unit unit) {

        Path path = Paths.get(SAVE_DIR, "log_" + this.date() + ".csv");
        
        Path parent = path.getParent();
        if (!Files.exists(parent)) {
	        try {
				Files.createDirectory(parent);
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
        
        try (
            BufferedWriter bw = Files.newBufferedWriter(path, StandardCharsets.UTF_8);
            PrintWriter pw = new PrintWriter(bw);
        ) {
        	
    		pw.println("timestamp,pressure(" + unit.toString() + ")");

        	synchronized (this.data) {
        	
	        	Iterator<Entry> iterator = this.data.iterator();
        	
	        	while (iterator.hasNext()) {
	        		
	        		Entry e = iterator.next();
	        		
	        		pw.println(((long)e.getTimeStamp()+this.initialTime) + "," + unit.calculatePressure(e.getData()));
	        		
	        	}
        	}

        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	
	public static class Entry {
		
		private int timeStamp;
		private int rawData;
		
		public Entry(int timeStamp, int data) {
			this.timeStamp = timeStamp;
			this.rawData = data;
		}
		
		public int getTimeStamp() {
			return this.timeStamp;
		}
		
		public int getData() {
			return this.rawData;
		}
		
		
	}
	
}
