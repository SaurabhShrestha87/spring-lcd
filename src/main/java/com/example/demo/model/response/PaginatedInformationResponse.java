package com.example.demo.model.response;

import com.example.demo.model.Information;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PaginatedInformationResponse {
    private List<Information> informationList;
    private Long numberOfItems;
    private int numberOfPages;
}
