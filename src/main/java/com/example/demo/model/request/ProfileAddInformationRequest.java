package com.example.demo.model.request;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class ProfileAddInformationRequest {
    private Long profileId;
    private Long informationId;
    private String duration;
    private String count;

}
