package com.example.demo.model.response;

import com.example.demo.model.Information;
import com.example.demo.model.Panel;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PaginatedPanelResponse {
    private List<Panel> panelList;
    private Long numberOfItems;
    private int numberOfPages;
}
