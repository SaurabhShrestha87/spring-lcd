package com.example.demo.service.settings;

import com.example.demo.model.Panel;
import com.example.demo.model.PanelStatus;
import com.example.demo.model.setting.PanelConfig;
import com.example.demo.model.setting.Setting;
import com.example.demo.service.RepositoryService;
import com.example.demo.service.SerialCommunication;
import com.example.demo.service.brightness.BrightnessService;
import com.example.demo.utils.FileUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/**
 * Service class for managing settings related to panel configurations and communication.
 */
@Service
@RequiredArgsConstructor
public class SettingService {
    private static final Logger logger = LoggerFactory.getLogger(SettingService.class);
    @Autowired
    private final BrightnessService brightnessService;
    @Autowired
    SerialCommunication serialCommunication;
    @Autowired
    RepositoryService repositoryService;
    private Long activeSettingId;

    /**
     * Initializes the setting service by setting up the active setting from the active panel
     * and copying the active panel to the custom setting.
     */
    @PostConstruct
    public void init() {
        setupActiveSettingFromActivePanel();
        copyActivePanelToCustom();
    }

    /**
     * Sets up the active setting based on the connected active panels.
     */
    public void setupActiveSettingFromActivePanel() {
        boolean needUpdate = false;
        String[] activeList = FileUtils.getPanelsList();
        List<String> activeList2 = new ArrayList<>(List.of(activeList));
        Setting activeSetting = repositoryService.getActiveSetting();
        activeSettingId = activeSetting.getId();
        List<PanelConfig> panelConfig = activeSetting.getPanel_configs();
        for (PanelConfig config : panelConfig) {
            boolean isPanelConnected = false;
            for (String name : activeList) {
                if (name.equalsIgnoreCase(config.getName())) {
                    if (config.getStatus().equals(PanelStatus.UNAVAILABLE)) {
                        needUpdate = true;
                        config.setStatus(PanelStatus.INACTIVE);
                    }
                    if (config.getPanel_order() != repositoryService.findPanelByName(config.getName()).getPanel_order()) {
                        needUpdate = true;
                        config.setPanel_order(repositoryService.findPanelByName(config.getName()).getPanel_order());
                    }
                    activeList2.removeIf(list -> list.equalsIgnoreCase(name));
                    isPanelConnected = true;
                    break;
                }
            }
            if (!isPanelConnected) {
                needUpdate = true;
                config.setStatus(PanelStatus.UNAVAILABLE);
            }
        }
        if (!activeList2.isEmpty()) {
            needUpdate = true;
            for (String s : activeList2) {
                PanelConfig config = new PanelConfig();
                config.setStatus(PanelStatus.INACTIVE);
                config.setName(s);
                activeSetting.getPanel_configs().add(config);
            }
        }
        if (needUpdate) {
            repositoryService.updateSetting(activeSetting);
        }
    }

    /**
     * Copies the active panel configurations to the custom setting.
     *
     * @return the updated custom setting
     */
    public Setting copyActivePanelToCustom() {
        if (repositoryService.getActiveSetting().getName().equalsIgnoreCase("CUSTOM")) {
            return repositoryService.getActiveSetting();
        } else {
            Setting customSetting = copySetting(repositoryService.getActiveSetting(), repositoryService.getCustomSetting());
            repositoryService.updateSetting(customSetting);
            return customSetting;
        }
    }

    /**
     * Updates the custom setting with the provided panel configurations.
     *
     * @param customSetting the updated custom setting
     */
    public void updateCustom(Setting customSetting) {
        try {
            for (PanelConfig panelConfig : customSetting.getPanel_configs()) {
                // Updating panel brightness in Serial
                brightnessService.setSingleBrightness(panelConfig.getId(), panelConfig.getBrightness());
                // Updating panel cool in Serial
                brightnessService.setSingleCool(panelConfig.getId(), panelConfig.getBc());
                // Updating panel warm in Serial
                brightnessService.setSingleWarm(panelConfig.getId(), panelConfig.getBw());
                Panel panel = repositoryService.findPanelByName(panelConfig.getName());
                if (panel != null) {
                    panel.setBw(panelConfig.getBw());
                    panel.setBc(panelConfig.getBc());
                    panel.setBrightness(panelConfig.getBrightness());
                    panel.setStatus(panelConfig.getStatus());
                    panel.setSn(panelConfig.getSn());
                    panel.setPanel_order(panelConfig.getPanel_order());
                    repositoryService.updatePanel(panel);
                } else {
                    throw new RuntimeException("Panel with name " + panelConfig.getName());
                }
            }
        } catch (Exception e) {
            logger.error("Error updating brightness: " + e);
        } finally {
            // Updating display output in db setting
            repositoryService.updateSettingOutput(customSetting.getP_output());
            // Updating activePanel to CUSTOM in db
            repositoryService.setSettingStatus(repositoryService.getCustomSetting().getId(), true);
            //FINALLY saving the new custom to db
            repositoryService.updateSetting(customSetting);
            repositoryService.setSettingStatus(customSetting.getId(), true);
        }
    }

    /**
     * Saves the custom setting with the provided setting ID.
     *
     * @param customSetting the custom setting to be saved
     * @param settingId     the ID of the target setting
     */
    public void saveCustomToSettingWithId(Setting customSetting, int settingId) {
        try {
            Setting newSetting = repositoryService.getSetting((long) settingId);
            newSetting = copySetting(customSetting, newSetting);
            Setting newUpdatedSetting = repositoryService.updateSetting(newSetting);
            repositoryService.setSettingStatus(newUpdatedSetting.getId(), true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Copies the source setting to the target setting.
     *
     * @param source the source setting to be copied
     * @param target the target setting to be updated
     * @return the updated target setting
     */
    public Setting copySetting(Setting source, Setting target) {
            target.setP_output(source.getP_output());
            for (int i = 0; i < source.getPanel_configs().size(); i++) {
                if (i > target.getPanel_configs().size() - 1) {
                    PanelConfig newConfig = new PanelConfig();
                    newConfig.setSetting(target);
                    newConfig.setId(0L);
                    target.getPanel_configs().add(newConfig);
                }
                PanelConfig panelConfigCustom = target.getPanel_configs().get(i);
                PanelConfig panelConfigActive = source.getPanel_configs().get(i);
                panelConfigCustom.setName(panelConfigActive.getName());
                panelConfigCustom.setBw(panelConfigActive.getBw());
                panelConfigCustom.setBc(panelConfigActive.getBc());
                panelConfigCustom.setStatus(panelConfigActive.getStatus());
                panelConfigCustom.setBrightness(panelConfigActive.getBrightness());
                panelConfigCustom.setPanel_order(panelConfigActive.getPanel_order());
                panelConfigCustom.setSn(panelConfigActive.getSn());
            }
            if (target.getPanel_configs().size() > source.getPanel_configs().size()) {
                for (int i = source.getPanel_configs().size(); i < target.getPanel_configs().size(); i++) {
                    target.getPanel_configs().get(i).setStatus(PanelStatus.UNAVAILABLE);
                }
            }
            return target;
    }

    /**
     * Sets the selected setting ID.
     *
     * @param settingId the ID of the selected setting
     */
    public void setSelected(Long settingId) {
        this.activeSettingId = settingId;
        repositoryService.setSettingStatus(activeSettingId, true);
    }

    /**
     * Retrieves the active setting ID.
     *
     * @return the active setting ID
     */
    public Long getActiveSettingId() {
        return activeSettingId;
    }
}
