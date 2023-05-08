package com.example.demo.model.request;

import com.example.demo.model.DisplayType;
import com.example.demo.model.Panel;
import com.example.demo.model.Profile;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class PanelSelectionDto {
    private List<Panel> panelList;
    private DisplayType displayType;
    private List<Profile> profileList;

    public PanelSelectionDto(ArrayList<Panel> es) {
        panelList = es;
    }

    public void addPanel(Panel panel) {
        this.panelList.add(panel);
    }

    public List<Long> getProfileIds() {
        List<Long> profileIds = new ArrayList<>();
        for (Profile profile : profileList) {
            profileIds.add(profile.getId());
        }
        return profileIds;
    }
}
