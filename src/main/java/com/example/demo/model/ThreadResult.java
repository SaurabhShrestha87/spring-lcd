package com.example.demo.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ThreadResult {
    private final Boolean interrupted;
    private final String message;
    private final String value;

    public ThreadResult(Boolean interrupted, String message, String value) {
        this.interrupted = interrupted;
        this.message = message;
        this.value = value;
    }

    public String toStrings() {
        return "ThreadResult{" +
                "interrupted=" + interrupted +
                ", message='" + message + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}

