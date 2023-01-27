package com.example.demo;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class Test {

    public static void main(String[] args) {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "DEBUG");
        System.out.println( Timestamp.from(Instant.now()));
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("dd-M-yyyy hh:mm:ss a", Locale.ENGLISH);
            formatter.setTimeZone(TimeZone.getTimeZone(ZoneId.systemDefault()));
            String dateInString = "22-01-2015 10:15:55 AM";
            Date date = formatter.parse(dateInString);
            System.out.println( "DATE : " + date.toInstant() );
        } catch (Exception e) {
            System.out.println( "ERROR : " + e );
        }
    }
}
