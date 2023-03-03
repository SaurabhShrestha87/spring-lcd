package com.example.demo.service.brightness;

import com.example.demo.model.*;
import com.example.demo.repository.PanelRepository;
import com.example.demo.service.SerialCommunication;
import com.example.demo.service.mirror.MirrorLedService;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;

@Service
@NoArgsConstructor
public class BrightnessService {
    @Autowired
    PanelRepository panelRepository;

    @PostConstruct
    public String init() {
        StringBuilder log = new StringBuilder();
        for (Panel panel : panelRepository.findAllByStatus(PanelStatus.ACTIVE)) {
            setPanelBrightness(panel, "0x1f");
        }
        return log.toString();
    }

    public void setBrightness(int value) {
        String hexString = "0x%s".formatted(Integer.toHexString(value)); // "1f"
        for (Panel panel : panelRepository.findAllByStatus(PanelStatus.ACTIVE)) {
            setPanelBrightness(panel, hexString);
        }
    }
    void setPanelBrightness(Panel panel, String hexaValue) {
        SerialCommunication serialCommunication = new SerialCommunication(DeviceType.fromString(panel.getDevice()));
        serialCommunication.runSerial("B %s".formatted(hexaValue));
    }

    public void setSingleBrightness(Long panelId, int value) {
        String hexString = "0x%s".formatted(Integer.toHexString(value));
        Optional<Panel> optional = panelRepository.findById(panelId);
        optional.ifPresent(panel -> {
            Panel panel1 = new Panel();
            BeanUtils.copyProperties(panel, panel1);
            panel1.setBrightness(value);
            panelRepository.save(panel1);
            setPanelBrightness(panel, hexString);
        });
    }
}
