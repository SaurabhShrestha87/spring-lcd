package com.example.demo.model.request;

import lombok.Data;

@Data
public class PanelCreationRequest {
    private String name;
    private String resolution;

    public PanelCreationRequest(String firstName, String lastName) {
        this.name = firstName;
        this.resolution = lastName;
    }
}
