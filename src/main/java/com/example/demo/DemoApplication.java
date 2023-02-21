package com.example.demo;

import com.pi4j.util.Console;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing // Enabling JPA Auditing
public class DemoApplication {
    public static String blankFilePath = "/home/pi/Application/Uploads/blank";

    public static void main(String[] args) {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "DEBUG");
        SpringApplication.run(DemoApplication.class, args);
    }
}
