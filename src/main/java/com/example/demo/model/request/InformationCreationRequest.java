package com.example.demo.model.request;

import com.example.demo.model.InfoType;
import lombok.Data;

@Data
public class InformationCreationRequest {
    private String name;
    private InfoType type;
    private String fileURL;
    private Long profileId;
}
