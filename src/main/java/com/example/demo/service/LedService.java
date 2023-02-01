package com.example.demo.service;

import com.example.demo.utils.RunShellCommandFromJava;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;

/**
 * Runnable to send a timestamp to the Arduino board to demonstrate the echo function.
 */
@Service
@NoArgsConstructor
@Getter
@Setter
public class LedService implements Runnable {

    private static final int INTERVAL_SEND_SECONDS = 5;

    volatile String filePath;
    volatile String deviceName;
    boolean keepRunning = true;

    /**
     * Constructor which gets the serial communication object to be used to send data.
     *
     * @param filePath
     * @param deviceName
     */
    public LedService(String filePath, String deviceName) {
        this.filePath = filePath;
        this.deviceName = deviceName;
    }

    @Override
    public void run() {
        // Keep looping until an error occurs
        while (keepRunning) {
            try {
                new RunShellCommandFromJava().runCmd(filePath, deviceName);
                // Sending data to the Arduino, as demo
                System.err.println("Ran Shell Command success... for file " + filePath + " at device : " + deviceName);
                Thread.sleep(INTERVAL_SEND_SECONDS);
            } catch (Exception ex) {
                System.err.println("Ran Shell Command Error... " + ex.getMessage());
                keepRunning = false;
            }
        }
    }
}