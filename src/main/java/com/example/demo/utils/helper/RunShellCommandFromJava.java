package com.example.demo.utils.helper;

import com.example.demo.utils.StreamGobbler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.apache.tomcat.util.compat.JrePlatform.IS_WINDOWS;


public class RunShellCommandFromJava {

    public static void main(String[] args) throws ExecutionException, IOException, InterruptedException {

        String homeDirectory = System.getProperty("user.home");
        Process process;
        if (IS_WINDOWS) {
            process = Runtime.getRuntime()
                    .exec(String.format("cmd.exe /c dir %s | findstr \"Desktop\"", homeDirectory));
        } else {
            process = Runtime.getRuntime()
                    .exec(String.format("/bin/sh -c ls %s | grep \"Desktop\"", homeDirectory));
        }
        StreamGobbler streamGobbler =
                new StreamGobbler(process.getInputStream(), System.out::println);
        Future<?> future = Executors.newSingleThreadExecutor().submit(streamGobbler);

        int exitCode = process.waitFor();
        assert exitCode == 0;
        future.get(); // waits for streamGobbler to finish
    }
}