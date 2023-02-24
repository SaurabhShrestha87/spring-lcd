package com.example.demo.model.request;

import lombok.Data;

import java.util.List;

@Data
public class ProfileLendRequest {
    private Long panelId;
    private List<Long> profileIds;
}
