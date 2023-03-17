package com.example.demo.service;

import com.example.demo.model.Panel;
import com.example.demo.model.PanelStatus;
import com.example.demo.repository.PanelRepository;
import com.example.demo.utils.OSValidator;
import com.pi4j.io.serial.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
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
// START SNIPPET: serial-snippet

@Service
@RequiredArgsConstructor
public class SerialCommunication {
    private static final Logger logger = LoggerFactory.getLogger(SerialCommunication.class);
    private final HashMap<String, Integer> panelIndexByDevice = new HashMap<>();
    private final HashMap<Integer, Long> panelIdByIndex = new HashMap<>();
    @Autowired
    PanelRepository panelRepository;
    private Serial[] serialList;

    @PostConstruct
    public void init() {
        List<Panel> panels = panelRepository.findAll();
        panels.removeIf(currentActivePanel -> currentActivePanel.getStatus().equals(PanelStatus.UNAVAILABLE));
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
                }
            });
            if (!OSValidator.isWindows()) {
                try {
                    SerialConfig config = new SerialConfig();
                    config.device(panel.getDevice())
                            .baud(Baud._9600)
                            .dataBits(DataBits._8)
                            .parity(Parity.NONE)
                            .stopBits(StopBits._1)
                            .flowControl(FlowControl.NONE);
                    if (OSValidator.isWindows()) {

                    } else {
                        serial.open(config);
                    }
                } catch (IOException ex) {
                    //console.println(" ==>> SERIAL SETUP FAILED : " + ex.getMessage());
                }
            }
            serialList[i] = serial;
            panelIndexByDevice.put(panel.getDevice(), i);
            panelIdByIndex.put(i, panel.getId());
            logger.info("\n\nCreated Serial : " + i);
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
            logger.info("\nSERIAL RAN!\n");
        }
    }

    public void runSerial(String serialData, int panelByIndex) {
        if (panelByIndex > getSize()) return;
        if (!OSValidator.isWindows()) {
            try {
                serialList[panelByIndex].write(serialData);
                logger.info(serialData);
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
}

// END SNIPPET: serial-snippet
