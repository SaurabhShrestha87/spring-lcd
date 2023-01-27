package com.example.demo.model.request;

import lombok.Data;

import java.util.List;

@Data
public class ProfileLendRequest {
    private List<Long> profileIds;
    private Long panelId;
}
