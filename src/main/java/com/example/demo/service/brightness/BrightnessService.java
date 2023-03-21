package com.example.demo.service.brightness;

import com.example.demo.model.Panel;
import com.example.demo.repository.PanelRepository;
import com.example.demo.service.SerialCommunication;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BrightnessService {
    private static final Logger logger = LoggerFactory.getLogger(BrightnessService.class);
    @Autowired
    SerialCommunication serialCommunication;
    @Autowired
    PanelRepository panelRepository;

    @PostConstruct
    public String init() {
        String log = "";
        for (int i = 0; i < serialCommunication.getSize(); i++) {
            logger.info("panel no : " + i);
            setPanelBrightness(i, "0x1f");
        }
        return log;
    }

    public void setBrightness(int value) {
        String hexString = "0x%s".formatted(Integer.toHexString(value)); // "1f"
        for (int i = 0; i < serialCommunication.getSize(); i++) {
            setPanelBrightness(i, hexString);
        }
    }

    void setPanelBrightness(int panelByIndex, String hexaValue) {
        System.out.println("panelByIndex : " + panelByIndex);
        try {
            serialCommunication.runSerial("B %s".formatted(hexaValue), panelByIndex);
        } catch (Exception e) {
            throw new RuntimeException("runSerial ERROR : " + e);
        }
    }
    void setPanelCool(int panelByIndex, String hexaValue) {
        serialCommunication.runSerial("B %s".formatted(hexaValue), panelByIndex);
    }

    void setPanelWarm(int panelByIndex, String hexaValue) {
        serialCommunication.runSerial("B %s".formatted(hexaValue), panelByIndex);
    }

    public void setSingleBrightness(Long panelId, int value) {
        String hexString = "0x%s".formatted(Integer.toHexString(value));
        Optional<Panel> optional = panelRepository.findById(panelId);
        optional.ifPresent(panel -> {
            panel.setBrightness(value);
            Panel savedPanel = panelRepository.saveAndFlush(panel);
            System.out.println("New Brightness : " + savedPanel.getBrightness());
            setPanelBrightness(serialCommunication.getIndexFromDevice(panel.getDevice()), hexString);
        });
    }
    public void setSingleWarm(Long panelId, int value) {
        String hexString = "0x%s".formatted(Integer.toHexString(value));
        Optional<Panel> optional = panelRepository.findById(panelId);
        optional.ifPresent(panel -> {
            panel.setBw(value);
            panelRepository.saveAndFlush(panel);
            setPanelWarm(serialCommunication.getIndexFromDevice(panel.getDevice()), hexString);
        });
    }
    public void setSingleCool(Long panelId, int value) {
        String hexString = "0x%s".formatted(Integer.toHexString(value));
        Optional<Panel> optional = panelRepository.findById(panelId);
        optional.ifPresent(panel -> {
            panel.setBc(value);
            panelRepository.saveAndFlush(panel);
            setPanelCool(serialCommunication.getIndexFromDevice(panel.getDevice()), hexString);
        });
    }
}
