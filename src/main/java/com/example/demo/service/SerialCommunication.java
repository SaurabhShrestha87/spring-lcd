package com.example.demo.service;

import com.example.demo.model.DeviceType;
import com.example.demo.utils.FileUtils;
import com.example.demo.utils.OSValidator;
import com.pi4j.io.serial.*;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

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
@NoArgsConstructor
public class SerialCommunication {
    private static final Logger logger = LoggerFactory.getLogger(SerialCommunication.class);
    //    final Console console = new Console();
    public Serial serial = SerialFactory.createInstance();
    int i = 1;
    /**
     * This example program supports the following optional command arguments/options:
     * "--device (device-path)"                   [DEFAULT: /dev/ttyAMA0]
     * "--baud (baud-rate)"                       [DEFAULT: 38400]
     * "--data-bits (5|6|7|8)"                    [DEFAULT: 8]
     * "--parity (none|odd|even)"                 [DEFAULT: none]
     * "--stop-bits (1|2)"                        [DEFAULT: 1]
     * "--flow-control (none|hardware|software)"  [DEFAULT: none]
     *
     * @param args
     * @throws InterruptedException
     * @throws IOException
     */
    DeviceType deviceType;

    public SerialCommunication(DeviceType device) {
        this.deviceType = device;
        init();
    }

    public void init() {
        /** !! ATTENTION !!
         *By default, the serial port is configured as a console port
         *for interacting with the Linux OS shell.  If you want to use
         *the serial port in a software program, you must disable the
         *OS from using this port.
         *Please see this blog article for instructions on how to disable
         *the OS console for this port:
         *https://www.cube-controls.com/2015/11/02/disable-serial-port-terminal-output-on-raspbian/
         *create Pi4J console wrapper/helper
         *(This is a utility class to abstract some of the boilerplate code)
         **/

        // print program title/header
        //console.title("<-- The Pi4J Project -->", "Serial Communication");

        // allow for user to exit program using CTRL-C
        //console.promptForExit();

        // create an instance of the serial communications class

        // create and register the serial data listener
        serial.addListener(event -> {
            //console.println("\n[SERIAL EVENT TRIGGERED]");
            // NOTE! - It is extremely important to read the data received from the
            // serial port.  If it does not get read from the receive buffer, the
            // buffer will continue to grow and consume memory.
            // print out the data received to the console
            try {
                while (event.getReader().available() != -1) {
                    logger.info("\n\nReading event : \n");
                    logger.info(Arrays.toString(event.getReader().read()));
//                    console.println("\n[SERIAL DATA]   " + Arrays.toString(event.getReader().read(event.getReader().available())));
//                    console.println("\n[SERIAL DATA]   " + event.getSerial().toString());
//                    console.println("\n[HEX DATA]   " + event.getHexByteString());
//                    console.println("\n[ASCII DATA] " + event.getAsciiString());
                }
                logger.info("\n\n[available() ERROR SERIAL] : " + event.getReader().available());
            } catch (IOException e) {
                logger.error("\nSERIAL Error : " + e);
            }
        });

        if (!OSValidator.isWindows()) {
            try {
                // create serial config object
                SerialConfig config = new SerialConfig();
                config.device(deviceType.toString())
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
    }

    public void runSerial(InputStream inputStream) {
        if (!OSValidator.isWindows()) {
            try {
                serial.write(inputStream);
            } catch (IOException e) {
                logger.error("runSerial Error: " + e);
            }
        } else {
            try {
                FileUtils.saveInputStreamAsImages(inputStream, "D:\\upload\\split", "out_"+ i );
                i++;
            } catch (IOException e) {
                // Handle the IOException gracefully instead of throwing a RuntimeException
                logger.error("runSerial Error: " + e);
                throw new RuntimeException(e);
            }
        }
    }

    public void runSerial(String serialData) {
        if (!OSValidator.isWindows()) {
            try {
                serial.write(serialData);
                logger.info(serialData);
            } catch (IOException e) {
                logger.error("runSerial : " + e);
            }
        } else {
            logger.error("runSerial : RAN");
        }
    }
}

// END SNIPPET: serial-snippet
