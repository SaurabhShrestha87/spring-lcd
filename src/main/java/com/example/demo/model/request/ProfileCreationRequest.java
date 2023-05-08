package com.example.demo.model.request;

import lombok.Data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

@Data
public class ProfileCreationRequest {
    private Long id = 0L;
    private String name;
    private String date;

    //  TEST DATE :  22-01-2015 10:15:55 AM
    public Instant getInstantDate() throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("dd-M-yyyy hh:mm:ss a", Locale.ENGLISH);
        formatter.setTimeZone(TimeZone.getTimeZone(ZoneId.systemDefault()));
        Date dateToBeInstant = formatter.parse(date);
        return dateToBeInstant.toInstant();
    }
}
