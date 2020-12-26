package com.edgelight.test;

/**
 * Just a test class for working with Arduino through COM port.
 * @author Alex Kravchenko
 */
public class ComTest {
    static final int test[] = new int[] { 23, 79, 12, 64, 43, 26, 89, 34, 121, 200, 43, 2, 11, 80, 53, 23, 79, 12, 64,
            43, 26, 89, 34, 121, 200, 43, 2, 11, 80, 53, 23, 79, 12, 64, 43, 26, 89, 34, 121, 200, 43, 2, 11, 80, 53,
            23, 79, 12, 64, 43, 26, 89, 34, 121, 200, 43, 2, 11, 80, 53, 23, 79, 12, 64, 43, 26, 89, 34, 121, 200, 43,
            2, 11, 80, 53, 23, 79, 12, 64, 43, 26, 89, 34, 121, 200, 43, 2, 11, 80, 53, 23, 79, 12, 64, 43, 26, 89, 34,
            121, 200, 43, 2, 11, 80, 53, 14, 15, 16, 62, 64, 56, 5, 23, 79, 12, 64, 43, 26, 89, 34, 121, 200, 43, 2, 11,
            80, 53, 23, 79, 12, 64, 43, 26, 89, 34, 121, 200, 43, 2, 11, 80, 53, 23, 79, 12, 64, 43, 26, 89, 34, 121,
            200, 43, 2, 11, 80, 53, 23, 79, 12, 64, 43, 26, 89, 34, 121, 200, 43, 2, 11, 80, 53, 23, 79, 12, 64, 43, 26,
            89, 34, 121, 200, 43, 2, 11, 80, 53, 23, 79, 12, 64, 43, 26, 89, 34, 121, 200, 43, 2, 11, 80, 53, 23, 79,
            12, 64, 43, 26, 89, 34, 121, 200, 43, 2, 11, 80, 53, 14, 15, 16, 62, 64, 56, 5, 23, 79, 12, 64, 43, 26, 89,
            34, 121, 200, 43, 2, 11, 80, 53, 23, 79, 12, 64, 43, 26, 89, 34, 121, 200, 43, 2, 11, 80, 53, 23, 79, 12,
            64, 43, 26, 89, 34, 121, 200, 43, 2, 11, 80, 53, 23, 79, 12, 64, 43, 26, 89, 34, 121, 200, 43, 2, 11, 80,
            53, 23, 79, 12, 64, 43, 26, 89, 34, 121, 200, 43, 2, 11, 80, 53, 23, 79, 12, 64, 43, 26, 89, 34, 121, 200,
            43, 2, 11, 80, 53, 23, 79, 12, 64, 43, 26, 89, 34, 121, 200, 43, 2, 11, 80, 53, 14, 15, 16, 62, 64, 56,
            5, };

    public static void main(String args[]) throws Exception {
//        String port = "COM3";
//        int baudRate = 115200;
//        NRSerialPort serial = new NRSerialPort(port, baudRate);
//        serial.connect();
//
//        DataInputStream ins = new DataInputStream(serial.getInputStream());
//        DataOutputStream outs = new DataOutputStream(serial.getOutputStream());
//
//        new Thread() {
//            public void run() {
//                while (true) {
//                    try {
//                        StringBuilder sb = new StringBuilder();
//                        int c;
//                        while ((c = ins.read()) != 'Z') {
//                            if (c != -1 && c != 0) {
//                                sb.append((char) c);
//                            }
//                        }
//                        System.out.println("> " + sb);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }.start();
//
//        int offset = 0;
//        while (true) {
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            baos.write(27);
//            baos.write(offset);
//            for (int i = 0; i < 16; ++i) {
//                baos.write(test[i * 3 + 0]);
//                baos.write(test[i * 3 + 1]);
//                baos.write(test[i * 3 + 2]);
//            }
//            outs.write(baos.toByteArray());
//            System.out.println("-write- offset " + offset);
//            Thread.sleep(100);
//            offset = (offset + 16) % 112;
//        }
    }
}
