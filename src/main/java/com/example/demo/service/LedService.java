package com.example.demo.service;

import com.example.demo.utils.RunShellCommandFromJava;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Runnable to send a timestamp to the Arduino board to demonstrate the echo function.
 */
@Service
@NoArgsConstructor
public class LedService implements Runnable {

    private static final int INTERVAL_SEND_SECONDS = 5;

    volatile String filePath;
    volatile String panelId;

    /**
     * Constructor which gets the serial communication object to be used to send data.
     *
     * @param filePath
     * @param panelId
     */
    public LedService(String filePath, String panelId) {
        this.filePath = filePath;
        this.panelId = panelId;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getPanelId() {
        return panelId;
    }

    public void setPanelId(String panelId) {
        this.panelId = panelId;
    }

    @Override
    public void run() {
        // Keep looping until an error occurs
        boolean keepRunning = true;
        while (keepRunning) {
            try {
                new RunShellCommandFromJava().runCmd(filePath, panelId);
                // Sending data to the Arduino, as demo
                System.err.println("Ran Shell Command success... for file " + filePath + " at panel id : " + panelId);
            } catch (Exception ex) {
                System.err.println("Ran Shell Command Error... " + ex.getMessage());
                keepRunning = false;
            }
        }
    }
}