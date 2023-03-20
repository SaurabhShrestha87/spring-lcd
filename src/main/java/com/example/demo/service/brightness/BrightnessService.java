package com.example.demo.service.brightness;

import com.example.demo.model.Panel;
import com.example.demo.repository.PanelRepository;
import com.example.demo.service.SerialCommunication;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BrightnessService {
    @Autowired
    SerialCommunication serialCommunication;
    @Autowired
    PanelRepository panelRepository;

    @PostConstruct
    public String init() {
        String log = "";
        for (int i = 0; i < serialCommunication.getSize(); i++) {
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
        serialCommunication.runSerial("B %s".formatted(hexaValue), panelByIndex);
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
            Panel panel1 = new Panel();
            BeanUtils.copyProperties(panel, panel1);
            panel1.setBrightness(value);
            panelRepository.save(panel1);
            setPanelBrightness(serialCommunication.getIndexFromDevice(panel1.getDevice()), hexString);
        });
    }
    public void setSingleWarm(Long panelId, int value) {
        String hexString = "0x%s".formatted(Integer.toHexString(value));
        Optional<Panel> optional = panelRepository.findById(panelId);
        optional.ifPresent(panel -> {
            Panel panel1 = new Panel();
            BeanUtils.copyProperties(panel, panel1);
            panel1.setBw(value);
            panelRepository.save(panel1);
            setPanelWarm(serialCommunication.getIndexFromDevice(panel1.getDevice()), hexString);
        });
    }
    public void setSingleCool(Long panelId, int value) {
        String hexString = "0x%s".formatted(Integer.toHexString(value));
        Optional<Panel> optional = panelRepository.findById(panelId);
        optional.ifPresent(panel -> {
            Panel panel1 = new Panel();
            BeanUtils.copyProperties(panel, panel1);
            panel1.setBc(value);
            panelRepository.save(panel1);
            setPanelCool(serialCommunication.getIndexFromDevice(panel1.getDevice()), hexString);
        });
    }
}
