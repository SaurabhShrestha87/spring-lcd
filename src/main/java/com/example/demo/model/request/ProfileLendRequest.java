package com.example.demo.model.request;

import com.example.demo.model.DisplayType;
import lombok.Data;

import java.util.List;

@Data
public class ProfileLendRequest {
    private DisplayType displayType;
    private Long panelId;
    private List<Long> profileIds;
}
