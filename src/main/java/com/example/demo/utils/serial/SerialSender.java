package com.example.demo.utils.serial;

import com.pi4j.io.serial.Serial;

/**
 * Runnable to send a timestamp to the Arduino board to demonstrate the echo function.
 */
public class SerialSender implements Runnable {

    private static int INTERVAL_SEND_SECONDS = 5;

    final Serial serial;
    final byte[] data;

    /**
     * Constructor which gets the serial communication object to be used to send data.
     *
     * @param serial
     * @param data
     */
    public SerialSender(Serial serial, byte[] data) {
        this.serial = serial;
        this.data = data;
    }

    @Override
    public void run() {
        // Keep looping until an error occurs
        boolean keepRunning = true;
        while (keepRunning) {
            try {
                // Sending data to the Arduino, as demo
                this.serial.write(data);
                System.err.println("Sent Serial...");
            } catch (Exception ex) {
                System.err.println("Error: " + ex.getMessage());
                keepRunning = false;
            }
        }
    }
}