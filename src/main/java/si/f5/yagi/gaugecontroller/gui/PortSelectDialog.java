package si.f5.yagi.gaugecontroller.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;

import com.fazecast.jSerialComm.SerialPort;

public class PortSelectDialog extends JDialog {
	
	private String result = null;
	
	public PortSelectDialog(MainWindow parent) {
		super(parent, "Select a serial port", true);
		this.setSize(400, 100);
		this.setResizable(false);
		
		JComboBox<String> ports = new JComboBox<String>();

		for (SerialPort port : SerialPort.getCommPorts()) {
			String name = port.getSystemPortName();
			if (!name.startsWith("tty.")) {
				ports.addItem(name);
			}
		}
		
		JButton portSelect = new JButton("Select");

		portSelect.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				result = (String)ports.getSelectedItem();
				setVisible(false);
				dispose();
			}
			
		});
		
		JPanel p = new JPanel();
		p.add(ports);
		p.add(portSelect);
		
		Container contentPane = getContentPane();
		contentPane.add(p, BorderLayout.CENTER);
		
		this.setVisible(true);
		
	}
	
	public String getResult() {
		return this.result;
	}
	

}
