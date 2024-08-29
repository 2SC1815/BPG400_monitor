package si.f5.yagi.gaugecontroller.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ItemEvent;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortInvalidPortException;

import si.f5.yagi.gaugecontroller.Profile;
import si.f5.yagi.gaugecontroller.gauge.BPG400;
import si.f5.yagi.gaugecontroller.gauge.Emission;
import si.f5.yagi.gaugecontroller.gauge.Log;
import si.f5.yagi.gaugecontroller.gauge.Unit;

public class MainWindow extends JFrame {
	
	public static void showMainWindow() {
		new MainWindow();
	}
	
	private BPG400 gauge = null;
	private JLabel portNameLabel;
	private JLabel pressureLabel;
	private JLabel emissionLabel;
	private JComboBox<Unit> unitSelect;
	private JComboBox<TimeSpan> timeSpan;
	private Plot plot;
	
	private Log history = new Log();
	
	private Timer plotUpdate;
	
	double pss = 100000.0;

	private MainWindow() {
		
		setTitle("BPG400 Monitor");
		setBounds(100, 100, 800, 500);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		plot = new Plot(this.history);
		
		pressureLabel = new JLabel("-");
		pressureLabel.setFont(new Font(Font.DIALOG_INPUT, Font.BOLD, 50));
		pressureLabel.setHorizontalAlignment(JLabel.RIGHT);

		portNameLabel = new JLabel("No connection");
		
		emissionLabel = new JLabel("-");
		emissionLabel.setFont(new Font(Font.DIALOG_INPUT, Font.BOLD, 50));
		emissionLabel.setHorizontalAlignment(JLabel.LEFT);
		
		unitSelect = new JComboBox<Unit>(Unit.values());
		unitSelect.setSelectedItem(Profile.getUnit());

		unitSelect.addItemListener(e -> {
			  if ( e.getStateChange() == ItemEvent.SELECTED ) {
				  Unit u = (Unit) e.getItem();
				  Profile.setUnit(u);
			  }
		});
		
		timeSpan = new JComboBox<TimeSpan>(TimeSpan.values()); 

		timeSpan.addItemListener(e -> {
			  if ( e.getStateChange() == ItemEvent.SELECTED ) {
				  TimeSpan t = (TimeSpan) e.getItem();
				  plot.time_range = t.seconds();
			  }
		});
		
		timeSpan.setSelectedItem(TimeSpan.MIN_5);
		
		
		JButton portUpdate = new JButton("Port select");
		MainWindow window = this;
		
		portUpdate.addActionListener(e -> {
			PortSelectDialog dialog = new PortSelectDialog(window);
			String result = dialog.getResult();
			window.connectGauge(result);
		});
		
		JButton save = new JButton("Save");
		
		save.addActionListener(e -> {
			window.history.save();
			window.history.saveCsv(currentUnit());
		});

		JButton reset = new JButton("RESET");
		
		reset.addActionListener(e -> {
			window.history.reset();
		});
		
		JButton degas = new JButton();
		degas.setEnabled(false);
		
		degas.addActionListener(e -> {
			if (this.gauge != null) {
				this.gauge.degasControl(this.gauge.getEmissionStatus() != Emission.ON_DEGAS);
			}
		});
		

		JTabbedPane tabbedPane = new JTabbedPane();
		

        JPanel graphTool = new JPanel(); // 保存、グラフ操作、デガス
        graphTool.add(save);
        graphTool.add(timeSpan);
        graphTool.add(degas);
        
        JPanel statusPanel = new JPanel();
		statusPanel.setLayout(new BorderLayout());
		statusPanel.add(emissionLabel, BorderLayout.WEST);
		statusPanel.add(pressureLabel, BorderLayout.EAST);
        
		JPanel measurePanel = new JPanel(); // グラフと値の表示
		measurePanel.setLayout(new BorderLayout());
        measurePanel.add(plot, BorderLayout.CENTER);
        measurePanel.add(statusPanel, BorderLayout.SOUTH);
        
        JPanel mainPanel = new JPanel(); // 測定と操作
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(measurePanel, BorderLayout.CENTER);
        mainPanel.add(graphTool, BorderLayout.SOUTH);
        
        
        JPanel portPanel = new JPanel();
        portPanel.setLayout(new FlowLayout());
        portPanel.add(portNameLabel);
        portPanel.add(portUpdate);
        
        JPanel unitPanel = new JPanel();
        unitPanel.setLayout(new FlowLayout());
        unitPanel.add(new JLabel("Pressure Unit"));
        unitPanel.add(unitSelect);
        
        JPanel resetPanel = new JPanel();
        resetPanel.setLayout(new FlowLayout());
        resetPanel.add(new JLabel("Reset graph"));
        resetPanel.add(reset);
        

        JPanel settingPanel = new JPanel();
        settingPanel.setLayout(new BoxLayout(settingPanel, BoxLayout.Y_AXIS));
        settingPanel.add(portPanel);
        settingPanel.add(unitPanel);
        settingPanel.add(resetPanel);
        

        tabbedPane.add("Measure", mainPanel);
        tabbedPane.add("Settings", settingPanel);
        
        Container container = this.getContentPane();
        container.add(tabbedPane);
        
        this.connectGauge(Profile.getPort());
        
        plotUpdate = new Timer();
        
        plotUpdate.scheduleAtFixedRate(new TimerTask() {
        	@Override
        	public void run() {
				plot.unit = currentUnit();
        		if (gauge != null) {
	        		double pressure = gauge.getPressure(currentUnit());
	        		pressureLabel.setText(format(pressure) + " " + currentUnit().toString());
	        		emissionLabel.setText(gauge.getEmissionStatus().toString());
	        		Emission e = gauge.getEmissionStatus();
	        		degas.setText(e != Emission.ON_DEGAS ? "Start degas" : "Stop degas");
	        		degas.setEnabled(e != Emission.OFF);
        		}
        		plot.repaint();
        	}
        }, 10, 200);

        this.setVisible(true);

	}
	
	public Unit currentUnit() {
		return (Unit) this.unitSelect.getSelectedItem();
	}

	
	public void connectGauge(String portName) {
		
		if (portName == null)
			return;
		
		try {
			
			SerialPort port = SerialPort.getCommPort(portName);
			this.gauge = new BPG400(port, this::updatePressure);
			portNameLabel.setText(port.getSystemPortName());
			Profile.setPort(portName);
			
		} catch (SerialPortInvalidPortException e) {
			
			System.out.println("port " + portName + " is invalid.");
			portNameLabel.setText("invalid port");
			
		}
		
	}
	
	public static String format(double value) {
		
		double val = value;
		String rtn = "";
		
		if (value > 1000.0) {
			val /= 1000;
			rtn = String.format("%.1f", val) + "k";
		} else if (value > 1.0) {
			rtn = String.format("%.2f", val);
		} else {
			
			int e = 0;
			
			while (true) {
				val *= 10;
				e --;
				if (val >= 1.0) {
					break;
				}
			}

			rtn = String.format("%.2f", val) + " E " + e;
			
		}
		
		return rtn;
	}
	
	public void updatePressure(BPG400 gauge) {
		
		this.history.addEntry(gauge.getRawData());
		
	}
	
}
