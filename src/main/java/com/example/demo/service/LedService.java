package com.example.demo.service;

import com.example.demo.model.InfoType;
import com.example.demo.model.Information;
import com.example.demo.utils.RunShellCommandFromJava;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Runnable to send a timestamp to the Arduino board to demonstrate the echo function.
 */
@Service
@NoArgsConstructor
@Getter
@Setter
public class LedService implements Runnable {
    private static final int INTERVAL_SEND_SECONDS = 33;
    RunShellCommandFromJava runShellCommandFromJava = new RunShellCommandFromJava();
    Information information;
    volatile String shFilePath;
    volatile String deviceName;
    volatile boolean keepRunning = true;
    boolean isShFile = false;

    @Override
    public void run() {
        // Keep looping until an error occurs
        while (keepRunning) {
            try {
                if(isShFile){
                    runShellCommandFromJava.runShCmd(shFilePath);
                }else{
                    if(information.getType() == InfoType.GIF){
                        runShellCommandFromJava.runCmdForGif(information.getName(),information.getUrl(), deviceName);
                    }else{
                        runShellCommandFromJava.runCmdForImage(information.getUrl(), deviceName);
                    }
                }
                keepRunning = false;
            } catch (Exception ex) {
                System.err.println("Ran Shell Command Error... " + ex.getMessage());
                keepRunning = false;
            }
        }
        runShellCommandFromJava.destroyCmd();
    }

    public void clearScreen(String blankFilePath, List<String> devices){
        runShellCommandFromJava.clearScreen(blankFilePath, devices);
        keepRunning = false;
    }
}