package com.cycos.service;

import com.cycos.controllers.LotRepository;
import com.fazecast.jSerialComm.SerialPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class SerialLotReceiver extends Thread {

    @Autowired
    private LotRepository repository;

    private final String serialDevice;
    private final int baudrate;
    private final int databits;
    private final int flowcontrol;

    @Autowired
    public SerialLotReceiver(
            @Value("${serialDevice}") String serialDevice,
            @Value("${baudrate}") int baudrate,
            @Value("${databits}") int databits,
            @Value("${flowcontrol}") int flowcontrol) {
        this.serialDevice = serialDevice;
        this.baudrate = baudrate;
        this.databits = databits;
        this.flowcontrol = flowcontrol;

        log.info("Created SerialLotReceiver...");
        log.info(": New Thread is Starting...");
        this.setDaemon(false);
        this.start();
    }

    @Override
    public void run() {
        SerialPort comPort = null;
        InputStream is = null;
        BufferedReader br;

        while (true) {
            if (serialDevice.equals("dummy")) {
                sleepUninterrupted(1000);
                continue;
            }

            try {
                // Öffnen des seriellen Ports, falls dieser noch nicht geöffnet ist
                if (comPort == null || !comPort.isOpen()) {
                    comPort = SerialPort.getCommPort(serialDevice);
                    comPort.setBaudRate(baudrate);
                    comPort.setFlowControl(flowcontrol);
                    comPort.setNumDataBits(databits);
                    openSerialPort(comPort);
                }

                comPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 1000, 0);
                is = new BufferedInputStream(comPort.getInputStream());
                br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                log.info("Waiting for data...");

                while (true) {
                    if (comPort.bytesAvailable() > 0) {
                        String record = br.readLine();

                        if (!validateRecord(record)) {
                            log.warn("Invalid data received: {}", record);
                            continue;
                        }

                        log.info("Received record: {}", record);
                        String[] data = record.split(":");

                        int lot = repository.macAddressExists(data[0]);

                        if (lot == -1) {
                            log.warn("MAC address {} does not exist in the database.", data[0]);
                            continue;
                        }

                        try {
                            // Batteriestatus und RSSI aus dem Datensatz extrahieren
                            int status = Integer.parseInt(data[1]);
                            float batteryStatus = Float.parseFloat(data[2]);
                            int rssi = Integer.parseInt(data[3]);

                            // Status, Batteriestatus und RSSI loggen
                            log.info("MAC: {}, Status: {}, RSSI: {}, Batterystate: {}", data[0], status,rssi , batteryStatus);

                            // Übergebe die Daten an die repository.upsertLots-Methode
                            repository.upsertLots(data[0],lot,Integer.parseInt(data[1]),batteryStatus,rssi);

                        } catch (NumberFormatException e) {
                            log.warn("Invalid number format in data: {}", record, e);
                        }
                    } else {
                        sleepUninterrupted(1000);  // Verringerung der CPU-Belastung
                    }
                }

            } catch (Exception e) {
                log.warn("Exception during serial command processing ::= ", e);
                closeSerialPort(comPort);
                sleepUninterrupted(5000); // Wartezeit vor dem erneuten Versuch, den Port zu öffnen
            }
        }
    }

    private void openSerialPort(SerialPort comPort) {
        int retries = 5;
        while (retries-- > 0) {
            if (comPort.openPort()) {
                log.info("Serial port is open");
                return;
            } else {
                log.warn("Failed to open serial port " + comPort.getSystemPortName() + ". Retrying...");
                sleepUninterrupted(1000);
            }
        }
        log.error("Could not open serial port after multiple attempts");
    }

    private void closeSerialPort(SerialPort comPort) {
        try {
            if (comPort != null && comPort.isOpen()) {
                comPort.closePort();
                log.info("Closed serial port");
            }
        } catch (Exception e) {
            log.warn("Cannot close serial port ::= ", e);
        }
    }

    private void sleepUninterrupted(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            log.error("Interrupted while sleeping", e);
            Thread.currentThread().interrupt();
        }
    }

    public boolean validateRecord(String input) {
        String[] split = input.split(":");
        // Überprüfen, ob der Datensatz 4 Teile enthält: MAC, Status, Battery, und RSSI
        if (split.length != 4) {
            return false;
        }
        // Überprüfen, ob die MAC-Adresse 16 Zeichen lang ist
        if (split[0].length() != 16) {
            return false;
        }
        return true;
    }
}


//    public static void main(String[] args) {
//        Thread t1 = new Thread(new Runnable() {
//            long counter = 0;
//
//            @Override
//            public void run() {
//                while (true) {
//                    try {
//                        Thread.sleep(1000);
//                        counter++;
//                        System.out.println("thread 1 : " + new Date());
//                        if (counter == 10) {
//                            throw new RuntimeException("Time to DIEEEEEEE !");
//                        }
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        });
//
//        Thread t2 = new Thread(() -> {
//            while (true) {
//                try {
//                    Thread.sleep(5000);
//                } catch (InterruptedException e) {
//                    throw new RuntimeException(e);
//                }
//                System.out.println("thread 2 : " + new Date());
//            }
//        });
//
//        t1.start();
//        t2.start();
//    }

