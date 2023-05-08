package com.example.demo.model.response;

import com.example.demo.model.Lend;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PaginatedLendResponse {
    private List<Lend> lendList;
    private Long numberOfItems;
    private int numberOfPages;
}
