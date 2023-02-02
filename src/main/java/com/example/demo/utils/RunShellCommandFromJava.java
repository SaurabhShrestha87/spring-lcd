package com.example.demo.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class RunShellCommandFromJava {
    Process process;
    ProcessBuilder processBuilder = new ProcessBuilder();
    public void destroyCmd() {
        if(process!=null && process.isAlive()){
            process.destroy();
        }
    }
    public void runShCmd(String pathToShFile) {
        if (OSValidator.isWindows()) {
        } else {
            processBuilder.command(pathToShFile);
        }
        runProcess();
    }
    public void clearScreen(String filePath, List<String> devices) {
        for (String device : devices) {
            processBuilder.command("bash", "-c", "cat " + filePath + " > /dev/" + device);
            runProcess();
            destroyCmd();
        }
    }
    public void runCmd(String filePath, String deviceName) {
        if (OSValidator.isWindows()) {
        } else {
            processBuilder.command("bash", "-c", "cat " + filePath + " > /dev/" + deviceName);
            runProcess();
        }
    }
    private void runProcess(){
        try {
            process = processBuilder.start();
            StringBuilder output = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line + "\n");
            }
            int exitVal = process.waitFor();
            if (exitVal == 0) {
                System.out.println("Success!");
                System.out.println(output);
            } else {
                //abnormal...
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
