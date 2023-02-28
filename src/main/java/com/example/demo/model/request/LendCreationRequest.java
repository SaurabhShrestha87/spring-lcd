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
public class LendCreationRequest {
    private String type;
    private String startOn;
    private String dueOn;
    private String profileId;
    private String panelId;

    //  TEST DATE :  22-01-2015 10:15:55 AM
    public Instant getInstantStartOn() throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("dd-M-yyyy hh:mm:ss a", Locale.ENGLISH);
        formatter.setTimeZone(TimeZone.getTimeZone(ZoneId.systemDefault()));
        Date dateToBeInstant = formatter.parse(startOn);
        return dateToBeInstant.toInstant();
    }

    //  TEST DATE :  22-01-2015 10:15:55 AM
    public Instant getInstantDueOn() throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("dd-M-yyyy hh:mm:ss a", Locale.ENGLISH);
        formatter.setTimeZone(TimeZone.getTimeZone(ZoneId.systemDefault()));
        Date dateToBeInstant = formatter.parse(dueOn);
        return dateToBeInstant.toInstant();
    }
}
