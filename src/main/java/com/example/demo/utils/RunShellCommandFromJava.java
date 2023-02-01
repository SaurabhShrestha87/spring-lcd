package com.example.demo.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class RunShellCommandFromJava {

    public void runCmd(String filePath, String deviceName) {
        System.out.println("Running RunShellCommandFromJava! \n");
        ProcessBuilder processBuilder = new ProcessBuilder();
        if (OSValidator.isWindows()) {
            // -- Windows --
            // Run a command
//            processBuilder.command("cmd.exe", "/c", "dir C:\\Users\\mkyong");
            // Run a bat file
            //processBuilder.command("C:\\Users\\mkyong\\hello.bat");
        } else {
            // -- Linux --
            processBuilder.command("bash", "-c", "cat " + filePath + " > /dev/" + deviceName);
            System.out.println("cat " + filePath + " > /dev/" + (deviceName));
            // Run a shell script
            //processBuilder.command("path/to/hello.sh");
        }

        try {
            Process process = processBuilder.start();
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
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
