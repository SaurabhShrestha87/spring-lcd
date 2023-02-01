package com.example.demo.model.request;

import com.example.demo.model.DisplayType;
import com.example.demo.model.Panel;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class PanelSelectionDto {
    private List<Panel> panelList;
    private DisplayType displayType;
    public PanelSelectionDto(ArrayList<Panel> es) {
        panelList = es;
    }
    public void addPanel(Panel panel) {
        this.panelList.add(panel);
    }
}
