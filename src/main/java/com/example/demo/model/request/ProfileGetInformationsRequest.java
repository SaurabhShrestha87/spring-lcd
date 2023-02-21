package com.example.demo.model.request;

import com.example.demo.model.Information;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ProfileGetInformationsRequest {
    private List<Information> informationList;
    private Long profileId;

}
