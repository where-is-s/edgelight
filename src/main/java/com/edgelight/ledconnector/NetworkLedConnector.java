package com.edgelight.ledconnector;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.List;

import com.edgelight.Configuration;
import com.edgelight.common.RGB;

public class NetworkLedConnector implements LedConnector {

    private DatagramSocket socket;
    private StringBuilder data;
    private InetAddress inetAddress;

    public NetworkLedConnector() throws SocketException, UnknownHostException {
        this.socket = new DatagramSocket();
        this.inetAddress = InetAddress.getByName(Configuration.IP_ADDRESS);
    }

    public void submit(List<RGB> rgbs) {
        data = new StringBuilder();
        data.append("[");

        for (RGB rgb: rgbs) {
            if (data.length() > 2) {
                data.append(",");
            }
            data.append("[");
            data.append((int) rgb.g);
            data.append(",");
            data.append((int) rgb.r);
            data.append(",");
            data.append((int) rgb.b);
            data.append("]");
        }
        data.append("]");
        byte[] rawData = data.toString().getBytes();

        DatagramPacket packet = new DatagramPacket(rawData, rawData.length, inetAddress, 5005);
        try {
            socket.send(packet);
        } catch (IOException e) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e1) {
            }
        }
    }

}
