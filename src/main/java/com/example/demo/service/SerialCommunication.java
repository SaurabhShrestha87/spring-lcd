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

/*
 * #%L
 * **********************************************************************
 * ORGANIZATION  :  Pi4J
 * PROJECT       :  Pi4J :: Java Examples
 * FILENAME      :  SerialExample.java
 *
 * This file is part of the Pi4J project. More information about
 * this project can be found here:  https://pi4j.com/
 * **********************************************************************
 * %%
 * Copyright (C) 2012 - 2021 Pi4J
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

/**
 * This example code demonstrates how to perform serial communications using the Raspberry Pi.
 *
 * @author Robert Savage
 */

@Service
@RequiredArgsConstructor
public class SerialCommunication implements PriorityOrdered {
    @Autowired
    private RepositoryService repositoryService;

    public Console console = new Console();
    private static final Logger logger = LoggerFactory.getLogger(SerialCommunication.class);
    private final HashMap<String, Integer> panelIndexByDevice = new HashMap<>();
    private final HashMap<Integer, Long> panelIdByIndex = new HashMap<>();
    private Serial[] serialList;

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE; // Set the order to highest precedence
    }

    @PostConstruct
    public void init() {
        logger.info("SERIAL COMMUNICATION INIT RUNNING");
        configurePanels();
        configureSerials();
        logger.info("SERIAL COMMUNICATION INIT FINISHED");
    }

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
                    availablePanels[i] = new Panel(0L, i + 1,i + 1, currentActivePanelName, "30x118", 400, 600, 31, PanelStatus.ACTIVE, null);
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

    public int getSize() {
        return serialList.length;
    }

    public int getIndexFromDevice(String deviceName) {
        return panelIndexByDevice.get(deviceName);
    }

    public Long panelIdFromIndex(int panelByIndex) {
        return panelIdByIndex.get(panelByIndex);
    }

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

    public void clearPanelAtIndex(int panelByIndex) throws IOException {
        serialList[panelByIndex].write("Q/n");
        serialList[panelByIndex].write("Q/n");
        serialList[panelByIndex].write("Q/n");
        serialList[panelByIndex].write("Q/n");
    }
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

// END SNIPPET: serial-snippet
