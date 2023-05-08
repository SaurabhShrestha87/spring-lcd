package com.example.demo.model.response;

import com.example.demo.model.Profile;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PaginatedProfileResponse {
    private List<Profile> profileList;
    private Long numberOfItems;
    private int numberOfPages;
}
