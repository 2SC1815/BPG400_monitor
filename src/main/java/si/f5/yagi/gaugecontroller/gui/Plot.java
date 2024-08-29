package si.f5.yagi.gaugecontroller.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ListIterator;

import javax.swing.JPanel;

import si.f5.yagi.gaugecontroller.gauge.Log;
import si.f5.yagi.gaugecontroller.gauge.Unit;


public class Plot extends JPanel {
    
    private int x_border = 10;
    private int y_border = 10;
    
    public int time_range = TimeSpan.MIN_5.seconds();
	public int y_max = 6;
    public int y_min = -5;
    
    private Log history;
    
    private Translator t;
    
    
    public Unit unit = Unit.PASCAL;
    
    
   // private static final Font labelFont = new Font( Font.DIALOG , Font.PLAIN, 10);
    
    
    
    public Plot(Log history) {
        super();
        this.setBackground(Color.WHITE);
        
        this.history = history;
        this.t = new Translator(this);
        
    }
    
    @Override
    protected void paintComponent(Graphics g) {
    	
        super.paintComponent(g);
        
        if (history.data.size() > 0) {
        	
        	long ct = System.currentTimeMillis();
    	
        	this.t.setGraphics(g);
            drawBorder(g); // グラフ外枠線を描く
            drawAxis(g, ct); // 軸線を描く
            
            long cTime = ct - history.getInitialTime();
        	
	        double max = -Double.MAX_VALUE;
	        double min = +5;
    	
	        synchronized (history.data) {
	        	
	        	Log.Entry e = history.data.get(history.data.size()-1);
	            double logPressure = unit.calculateLogPressure(e.getData());
	            
	            g.setColor(Color.red);
	            g.drawRect(t.w(0), t.h(logPressure)-2, 4, 4);
	            
	            g.setColor(Color.blue);
	        
		        for (ListIterator<Log.Entry> i = history.data.listIterator(history.data.size()); i.hasPrevious();) {
		            
		            e = i.previous();
	
		            double x =  (cTime - e.getTimeStamp() * 10) / 1000.0d;
		            
		            if (x > this.time_range) {
		            	break;
		            }
		            
		            logPressure = unit.calculateLogPressure(e.getData());

		            min = Math.min(min, logPressure);
		            max = Math.max(max, logPressure);
		            
		            drawPoint(g, -x, logPressure);
		            
	
		        }
	        
	        }
	        
	        this.y_min = (int)Math.floor(min);
	        this.y_max = (int)Math.ceil(max);
	        
	        if (this.y_min == this.y_max) {
	        	y_min -= 1;
	        }
	        
        }

        

        
    }
    
    
    private class Translator {
    	
    	private Plot p;
    	private Graphics g;
    	
    	public Translator(Plot p) {
    		this.p = p;
    	}
    	
    	public void setGraphics(Graphics g) {
    		this.g = g;
    	}

        private int labelWidth() {
            return g.getFontMetrics().stringWidth("100k");
        }
        
        private int labelHeight() {
            return g.getFontMetrics().getHeight();
        }
        
    	public int w(double x) {
    		int lwidth = labelWidth();
            int w = p.getWidth() - (x_border * 3) - lwidth;
            return ((int) ((x + time_range) / time_range * w) + x_border * 2 + lwidth);
        }
    	
        public int h(double y) {
            int h = p.getHeight() - (y_border * 2);
            return (h - (int) ((y - p.y_min) / (p.y_max - p.y_min) * h) + y_border);
        }
        
    }
    
    private void drawBorder(Graphics g) {
    	
    	int labelWidth = t.labelWidth();
        int w = this.getWidth() - (x_border * 3) - labelWidth;
        int h = this.getHeight() - (y_border * 2);

        g.setColor(Color.black);
        
        g.drawRect(x_border*2+labelWidth, y_border, w, h);
    }
    

    private void drawAxis(Graphics g, long time) {

    	int labelHeight = t.labelHeight();
    	
        for (int i = y_max; i >= this.y_min; i--) {

        	if (i > this.y_min) {
	            g.setColor(Color.lightGray);
	        	for (int o = 0; o<10; o++) {
	        		double s = Math.log10(Math.pow(10, i)*o/10);
	            	g.drawLine(t.w(-this.time_range), t.h(s), t.w(0), t.h(s));
	        	}
        	}
        	
            g.setColor(Color.black);
        	g.drawLine(t.w(-this.time_range), t.h(i), t.w(0), t.h(i));
        	
        	String s = "";
        	
        	if (i >= 3) {
        		s += (int)Math.pow(10, i-3) + "k";
        	} else if (i >= 0) {
        		s += (int)Math.pow(10, i);
        	} else {
        		s += "1E"+i;
        	}

            g.setColor(Color.black);
        	g.drawString(s, this.x_border, t.h(i) + labelHeight/2);
        	
        }
        
        g.setColor(Color.black);
        
        for (
        		double i = (int)(time % (100 * this.time_range)) / 1000.0; 
        		i < this.time_range; 
        		i += this.time_range / 10.0) {

        	g.drawLine(t.w(-i), t.h(this.y_max), t.w(-i), t.h(this.y_min));
        	
        }
        
    }
    

    private void drawPoint(Graphics g, double x, double y) {
        // 描画領域外のチェック
    	//System.out.println(x + " " + y);
        if (x < -time_range || 0 < x || y < y_min || y_max < y)
            return;

        
        g.drawRect(t.w(x), t.h(y), 1, 1);
    }
    
}