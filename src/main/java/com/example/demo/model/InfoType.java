package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum InfoType {
    IMAGE("Image"),
    VIDEO("Video"),
    GIF("Gif");

    private final String displayValue;

    private InfoType(String displayValue) {
        this.displayValue = displayValue;
    }

    public String getDisplayValue() {
        return displayValue;
    }
    }
