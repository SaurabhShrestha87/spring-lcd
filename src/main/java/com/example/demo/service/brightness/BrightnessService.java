package com.example.demo.service.brightness;

import com.example.demo.model.Panel;
import com.example.demo.repository.PanelRepository;
import com.example.demo.service.SerialCommunication;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Optional;

/**
 * This class represents a service for controlling the brightness of panels using serial communication.
 * <p>
 * It is annotated with @Service to indicate that it is a service component in the application.
 * <p>
 * It is also annotated with @RequiredArgsConstructor to automatically generate a constructor
 * <p>
 * that injects the dependencies marked with @Autowired.
 */
@Service
@RequiredArgsConstructor
public class BrightnessService {
    private static final Logger logger = LoggerFactory.getLogger(BrightnessService.class);

    @Autowired
    SerialCommunication serialCommunication; // Dependency for serial communication
    @Autowired
    PanelRepository panelRepository; // Dependency for panel data access

    /**
     * Initializes the brightness service.
     * Sets the brightness of all panels to a default value of 0x1f (31 in decimal).
     * Returns a log message with the results of the initialization.
     *
     * @return A log message indicating the results of the initialization.
     */
    @PostConstruct
    public String init() {
        String log = "";
        for (int i = 0; i < serialCommunication.getSize(); i++) {
            setPanelBrightness(i, "0x1f");
        }
        return log;
    }

    /**
     * Sets the brightness of all panels to the specified value.
     *
     * @param value The brightness value to set for all panels.
     */
    public void setBrightness(int value) {
        String hexString = "0x%s".formatted(Integer.toHexString(value)); // Convert value to hex string ("1f")
        for (int i = 0; i < serialCommunication.getSize(); i++) {
            setPanelBrightness(i, hexString);
        }
    }

    /**
     * Sets the brightness of a panel identified by its index.
     *
     * @param panelByIndex The index of the panel to set the brightness for.
     * @param hexaValue    The brightness value in hexadecimal format (e.g., "0x1f").
     */
    void setPanelBrightness(int panelByIndex, String hexaValue) {
        try {
            serialCommunication.runSerial("B %s".formatted(hexaValue), panelByIndex);
        } catch (Exception e) {
            throw new RuntimeException("runSerial ERROR : " + e);
        }
    }

    /**
     * Sets the cool value of a panel identified by its index.
     *
     * @param panelByIndex The index of the panel to set the cool value for.
     * @param hexaValue    The cool value in hexadecimal format (e.g., "0x1f").
     */
    void setPanelCool(int panelByIndex, String hexaValue) {
        serialCommunication.runSerial("B %s".formatted(hexaValue), panelByIndex);
    }

    /**
     * Sets the warm value of a panel identified by its index.
     *
     * @param panelByIndex The index of the panel to set the warm value for.
     * @param hexaValue    The warm value in hexadecimal format (e.g., "0x1f").
     */
    void setPanelWarm(int panelByIndex, String hexaValue) {
        serialCommunication.runSerial("B %s".formatted(hexaValue), panelByIndex);
    }

    /**
     * Sets the brightness of a single panel identified by its ID.
     *
     * @param panelId The ID of the panel to set the brightness for.
     * @param value   The brightness value to set for the panel.
     */
    public void setSingleBrightness(Long panelId, int value) {
        String hexString = "0x%s".formatted(Integer.toHexString(value));
        Optional<Panel> optional = panelRepository.findById(panelId);
        optional.ifPresent(panel -> setPanelBrightness(serialCommunication.getIndexFromDevice(panel.getDevice()), hexString));
    }

    /**
     * Sets the warm value of a single panel identified by its ID.
     *
     * @param panelId The ID of the panel to set the warm value for.
     * @param value   The warm value to set for the panel.
     */
    public void setSingleWarm(Long panelId, int value) {
        String hexString = "0x%s".formatted(Integer.toHexString(value));
        Optional<Panel> optional = panelRepository.findById(panelId);
        optional.ifPresent(panel -> setPanelWarm(serialCommunication.getIndexFromDevice(panel.getDevice()), hexString));
    }

    /**
     * Sets the cool value of a single panel identified by its ID.
     *
     * @param panelId The ID of the panel to set the cool value for.
     * @param value   The cool value to set for the panel.
     */
    public void setSingleCool(Long panelId, int value) {
        String hexString = "0x%s".formatted(Integer.toHexString(value));
        Optional<Panel> optional = panelRepository.findById(panelId);
        optional.ifPresent(panel -> setPanelCool(serialCommunication.getIndexFromDevice(panel.getDevice()), hexString));
    }
}