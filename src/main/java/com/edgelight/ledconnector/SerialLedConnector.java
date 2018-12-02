package com.edgelight.ledconnector;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edgelight.Configuration;
import com.edgelight.common.RGB;
import com.google.common.collect.Lists;

import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;

public class SerialLedConnector extends Thread implements LedConnector {

	private static Logger logger = LoggerFactory.getLogger(SerialLedConnector.class);

	private static final int START_MAGIC_BYTE = 27;
	private static final int END_MAGIC_BYTE = 91;
	private static final int UPDATES_INTERVAL = 7; // 9ms for 115200, 5ms for 921600 baud rate
	private static final int BAUD_RATE = 921600;
	private static final int BLOCK_SIZE = 112;
	
	private SerialPort serialPort;

	private final List<RGB> rgbs = new ArrayList<>();
	private final List<RGB> smoothRgbs = new ArrayList<>();

	public SerialLedConnector() {
		for (int i = 0; i < Configuration.LEDS_COUNT; ++i) {
			this.rgbs.add(new RGB(0, 0, 0));
			this.smoothRgbs.add(new RGB(0, 0, 0));
		}
		
		start();
	}
	
	private void write(RGB rgb, DataOutputStream os) throws IOException {
		os.writeByte((int) rgb.r);
		os.writeByte((int) rgb.g);
		os.writeByte((int) rgb.b);
	}
	
	@Override
	public void run() {
		try {
			while (true) {
				while (serialPort == null) {
					Thread.sleep(1000);
					connect();
				}
				
				try {
					synchronized (this.rgbs) {
						for (int i = 0; i < Configuration.LEDS_COUNT; ++i) {
							smoothRgbs.get(i).smoothenTo(this.rgbs.get(i), 0.18);
						}
					}
					for (int i = 0; i < Configuration.LEDS_COUNT;) {
						long start = System.currentTimeMillis();
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						baos.write(START_MAGIC_BYTE);
						baos.write(i);
						DataOutputStream dos = new DataOutputStream(baos);
						for (int j = 0; j < BLOCK_SIZE; ++j, ++i) {
							write(this.smoothRgbs.get(i), dos);
						}
						dos.close();
						baos.write(END_MAGIC_BYTE);
						if (!serialPort.writeBytes(baos.toByteArray())) {
							serialPort = null;
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e1) {
							}
							continue;
						}
						long timePassed = System.currentTimeMillis() - start;
						if (timePassed < UPDATES_INTERVAL) {
							Thread.sleep(UPDATES_INTERVAL - timePassed);
						}
					}
				} catch (SerialPortException e) {
					// ignored
				} catch (IOException e) {
					// ignored
				}
			}
		} catch (InterruptedException e) {
		}
	}
	
	private void connect() {
		String[] portNames = SerialPortList.getPortNames();
		
		if (portNames.length == 0) {
			logger.info("No COM-ports available! Waiting...");
			return;
		}
		
		String port = Configuration.PREFERRED_PORT;
		if (port == null) {
			port = portNames[0];
		}
		
		if (!Lists.newArrayList(portNames).contains(port)) {
			logger.info("Port " + port + " is not available! Waiting...");
			return;
		}
		
		try {
			serialPort = new SerialPort(port);
			serialPort.openPort();
			serialPort.setParams(BAUD_RATE,
	                SerialPort.DATABITS_8,
	                SerialPort.STOPBITS_1,
	                SerialPort.PARITY_NONE);
			logger.info("Initialized port: " + port);
		} catch (SerialPortException e) {
			e.printStackTrace();
			serialPort = null;
		}
	}
	
	@Override
	public void submit(List<RGB> rgbs) {
		synchronized (this.rgbs) {
			this.rgbs.clear();
			this.rgbs.addAll(rgbs);
		}
	}
	
}
