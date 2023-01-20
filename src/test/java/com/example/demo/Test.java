package com.example.demo;

import java.time.Instant;
import java.sql.Timestamp;

public class Test {

    public static void main(String[] args) {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "DEBUG");
        System.out.println( Timestamp.from(Instant.now()));
    }
}
