package com.example.demo.model.request;

import lombok.Data;

import java.util.List;

@Data
public class InformationLendRequest {
    private List<Long> informationIds;
    private Long panelId;
}
