package com.example.demo.model.request;

import com.example.demo.model.PanelStatus;
import lombok.Data;

@Data
public class PanelCreationRequest {
    private Long id = 0L;
    private String name;
    private String resolution;
    private String status;

    public PanelStatus getStatusAsEnum() {
        return PanelStatus.valueOf(status);
    }
}
