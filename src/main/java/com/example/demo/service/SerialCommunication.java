package com.example.demo.service;

import com.example.demo.model.Panel;
import com.example.demo.model.PanelStatus;
import com.example.demo.utils.FileUtils;
import com.example.demo.utils.OSValidator;
import com.pi4j.io.serial.*;
import com.pi4j.util.Console;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

/**
 * The SerialCommunication class handles serial communication with panels.
 * It initializes and configures panels and serial connections, and provides methods to send data to the panels.
 */
@Service
@RequiredArgsConstructor
public class SerialCommunication implements PriorityOrdered {
    private static final Logger logger = LoggerFactory.getLogger(SerialCommunication.class);
    private final HashMap<String, Integer> panelIndexByDevice = new HashMap<>();
    private final HashMap<Integer, Long> panelIdByIndex = new HashMap<>();
    public Console console = new Console();
    @Autowired
    private RepositoryService repositoryService;
    private Serial[] serialList;

    /**
     * Returns the order of the SerialCommunication bean.
     * This ensures that the bean has the highest precedence.
     *
     * @return the order value
     */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE; // Set the order to highest precedence
    }

    /**
     * Initializes the SerialCommunication bean after construction.
     * It configures panels and serial connections.
     */
    @PostConstruct
    public void init() {
        logger.info("SERIAL COMMUNICATION INIT RUNNING");
        configurePanels();
        configureSerials();
        logger.info("SERIAL COMMUNICATION INIT FINISHED");
    }

    /**
     * Configures the panels by updating the connected panels in the database.
     * <p>
     * It retrieves the connected panel names, checks if they exist in the database,
     * <p>
     * and updates or creates new panels accordingly.
     */
    private void configurePanels() {
        try {
            /**  Get connected panels and update it into db.
             */
            String[] currentActivePanelNames = FileUtils.getPanelsList();
            Panel[] availablePanels = new Panel[currentActivePanelNames.length];
            console.title("CONFIGURING PANELS");
            for (int i = 0; i < currentActivePanelNames.length; i++) {
                String currentActivePanelName = currentActivePanelNames[i];
                console.box(2, "Found connected panel : " + currentActivePanelName);
                boolean panelFound = false;
                for (Panel panel : repositoryService.getPanels()) {
                    if (panel.getName().equalsIgnoreCase(currentActivePanelName)) {
                        availablePanels[i] = panel;
                        panelFound = true;
                        break;
                    }
                }
                if (!panelFound) {
                    console.println("Panel is not in db : " + currentActivePanelName);
                    availablePanels[i] = new Panel(0L, i + 1, i + 1, currentActivePanelName, "30x118", 400, 600, 31, PanelStatus.ACTIVE, null);
                } else {
                    console.println("Panel is in db : " + currentActivePanelName);
                }
            }

            console.box(10, "Making all panels in db UNAVAILABLE...");
            for (Panel panel : repositoryService.getPanels()) {
                panel.setStatus(PanelStatus.UNAVAILABLE);
                repositoryService.updatePanel(panel);
            }
            console.box(10, "Updating connected panels ...");
            for (Panel availablePanel : availablePanels) {
                if (availablePanel.getStatus().equals(PanelStatus.UNAVAILABLE)) {
                    availablePanel.setStatus(PanelStatus.ACTIVE);
                }
                repositoryService.updateElseCreatePanel(availablePanel);
            }
            console.exiting();
        } catch (Exception e) {
            throw new RuntimeException("Error configuring panels " + e);
        }
    }

    /**
     * Configures the serial connections for the active panels.
     * <p>
     * It creates Serial instances and sets up listeners for data reception.
     */
    private void configureSerials() {
        console.title("CONFIGURING SERIAL");
        List<Panel> panels = repositoryService.getPanelsByStatusOrderBySnAsc(PanelStatus.ACTIVE);
        serialList = new Serial[panels.size()];
        for (int i = 0, panelsSize = panels.size(); i < panelsSize; i++) {
            Panel panel = panels.get(i);
            Serial serial = SerialFactory.createInstance();
            serial.addListener(event -> {
                try {
                    while (event.getReader().available() != -1) {
                        event.getReader().read();
                    }
                } catch (IOException e) {
                    logger.error("\nSERIAL Error : " + e);
                    throw new RuntimeException("\nSERIAL Error : " + e);
                }
            });
            if (!OSValidator.isWindows()) {
                SerialConfig config = new SerialConfig();
                config.device(panel.getDevice())
                        .baud(Baud._9600)
                        .dataBits(DataBits._8)
                        .parity(Parity.NONE)
                        .stopBits(StopBits._1)
                        .flowControl(FlowControl.NONE);
                if (!OSValidator.isWindows()) {
                    try {
                        serial.open(config);
                    } catch (IOException e) {
                        logger.error("SERIAL OPEN ERROR : " + e.getMessage());
                        throw new RuntimeException(e);
                    }
                }
            }
            serialList[i] = serial;
            panelIndexByDevice.put(panel.getDevice(), i);
            panelIdByIndex.put(i, panel.getId());
            console.box(2, "Serial connected for panel : " + panel.getDevice());
        }
    }

    /**
     * Runs the serial communication for the specified panel using the given input stream.
     * <p>
     * It writes the input stream data to the serial connection associated with the panel.
     *
     * @param inputStream  the input stream containing the data to be sent
     * @param panelByIndex the index of the panel in the serialList
     */
    public void runSerial(InputStream inputStream, int panelByIndex) {
        if (panelByIndex > getSize()) return;
        if (!OSValidator.isWindows()) {
            try {
                serialList[panelByIndex].write(inputStream);
            } catch (IOException e) {
                logger.error("runSerial Error: " + e);
            }
        } else {
            logger.info("\n\n\n\n\n");
            logger.error("runSerial Error: " + inputStream);
            logger.info("\n\n\n\n\n");
        }
    }

    /**
     * Runs the serial communication for the specified panel using the given serial data.
     * <p>
     * It writes the serial data to the serial connection associated with the panel.
     *
     * @param serialData   the serial data to be sent
     * @param panelByIndex the index of the panel in the serialList
     */
    public void runSerial(String serialData, int panelByIndex) {
        if (panelByIndex > getSize()) return;
        if (!OSValidator.isWindows()) {
            try {
                serialList[panelByIndex].write(serialData);
            } catch (IOException e) {
                logger.error("runSerial : " + e);
            }
        } else {
            logger.info("\nSERIAL RAN! (serialData) at " + panelByIndex + "\n");
        }
    }

    /**
     * Returns the number of serial connections in the serialList.
     *
     * @return the number of serial connections
     */
    public int getSize() {
        return serialList.length;
    }

    /**
     * Returns the panel index associated with the given device name.
     *
     * @param deviceName the name of the device
     * @return the panel index
     */
    public int getIndexFromDevice(String deviceName) {
        return panelIndexByDevice.get(deviceName);
    }

    /**
     * Returns the panel ID associated with the given index in the serialList.
     *
     * @param panelByIndex the index of the panel in the serialList
     * @return the panel ID
     */
    public Long panelIdFromIndex(int panelByIndex) {
        return panelIdByIndex.get(panelByIndex);
    }

    /**
     * Clears all panels by sending "Q/n" commands through the serial connections.
     *
     * @throws IOException if an I/O error occurs
     */
    public void clearAll() throws IOException {
        if (!OSValidator.isWindows()) {
            for (Serial serial : serialList) {
                serial.write("Q/n");
                serial.write("Q/n");
                serial.write("Q/n");
                serial.write("Q/n");
            }
        } else {
            logger.info("Serial RAN! clearALLLLL!!!!");
        }
    }

    /**
     * Clears the panel at the specified index by sending "Q/n" commands through the serial connection.
     *
     * @param panelByIndex the index of the panel in the serialList
     * @throws IOException if an I/O error occurs
     */
    public void clearPanelAtIndex(int panelByIndex) throws IOException {
        serialList[panelByIndex].write("Q/n");
        serialList[panelByIndex].write("Q/n");
        serialList[panelByIndex].write("Q/n");
        serialList[panelByIndex].write("Q/n");
    }

    /**
     * Resets the serial connections by closing them and reinitializing the SerialCommunication bean.
     */
    public void resetSerial() {
        if (!OSValidator.isWindows()) {
            for (Serial serial : serialList) {
                try {
                    serial.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        init();
    }
}

