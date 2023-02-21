package com.example.demo.model.request;

import com.example.demo.model.Information;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ProfileAddInformationRequest {
    private Long profileId;
    private Long informationId;
    private String duration;
    private String count;

}
