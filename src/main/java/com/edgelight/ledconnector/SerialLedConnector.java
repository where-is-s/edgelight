package com.edgelight.ledconnector;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edgelight.Configuration;
import com.edgelight.common.RGB;
import com.google.common.collect.Lists;

import de.ibapl.spsw.api.DataBits;
import de.ibapl.spsw.api.FlowControl;
import de.ibapl.spsw.api.Parity;
import de.ibapl.spsw.api.SerialPortSocket;
import de.ibapl.spsw.api.SerialPortSocketFactory;
import de.ibapl.spsw.api.Speed;
import de.ibapl.spsw.api.StopBits;

public class SerialLedConnector extends Thread implements LedConnector {

    private static Logger logger = LoggerFactory.getLogger(SerialLedConnector.class);

    private static final int START_MAGIC_BYTE = 27;
    private static final int END_MAGIC_BYTE = 91;
    private static final int UPDATES_INTERVAL = 11; // ms, !!! adjust this when you change BAUD_RATE !!!
    private static final Speed BAUD_RATE = Speed._460800_BPS;

    private SerialPortSocket serialPort;

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
                    long start = System.currentTimeMillis();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    baos.write(START_MAGIC_BYTE);
                    baos.write(0);
                    DataOutputStream dos = new DataOutputStream(baos);
                    for (int i = 0; i < Configuration.LEDS_COUNT; ++i) {
                        write(this.smoothRgbs.get(i), dos);
                    }
                    dos.close();
                    baos.write(END_MAGIC_BYTE);
                    serialPort.getOutputStream().write(baos.toByteArray());
                    long timePassed = System.currentTimeMillis() - start;
                    if (timePassed < UPDATES_INTERVAL) {
                        Thread.sleep(UPDATES_INTERVAL - timePassed);
                    }
                } catch (IOException e) {
                    logger.info("COM-port broken!");
                    e.printStackTrace();
                    try {
                        serialPort.close();
                    } catch (IOException ignored) {
                    }
                    serialPort = null;
                }
            }
        } catch (InterruptedException e) {
        }
    }

    private void connect() {
        try {
            ServiceLoader<SerialPortSocketFactory> loader = ServiceLoader.load(SerialPortSocketFactory.class);
            SerialPortSocketFactory serialPortSocketFactory = loader.iterator().next();

            List<String> portNames = serialPortSocketFactory.getPortNames(true);

            if (portNames.isEmpty()) {
                logger.info("No COM-ports available! Waiting...");
                return;
            }

            String port = Configuration.PREFERRED_PORT;
            if (port == null) {
                port = portNames.get(0);
            }

            if (!Lists.newArrayList(portNames).contains(port)) {
                logger.info("Port " + port + " is not available! Waiting...");
                return;
            }

            serialPort = serialPortSocketFactory.open(port, BAUD_RATE, DataBits.DB_8, StopBits.SB_1, Parity.NONE,
                    FlowControl.getFC_NONE());
            logger.info("Initialized port: " + port);
        } catch (IOException e) {
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
