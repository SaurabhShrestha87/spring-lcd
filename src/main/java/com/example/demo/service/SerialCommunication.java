package com.example.demo.service;

import com.example.demo.model.DeviceType;
import com.example.demo.utils.OSValidator;
import com.pi4j.io.serial.*;
import com.pi4j.util.Console;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

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
@Service
@NoArgsConstructor
public class SerialCommunication {
    private static final Logger logger = LoggerFactory.getLogger(SerialCommunication.class);
    final Console console = new Console();
    public Serial serial = SerialFactory.createInstance();
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
        logger.info("SerialCommunication : " + deviceType.toString());
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
        console.title("<-- The Pi4J Project -->", "Serial Communication");

        // allow for user to exit program using CTRL-C
        console.promptForExit();

        // create an instance of the serial communications class

        // create and register the serial data listener
        serial.addListener(event -> {
            console.println("\n[SERIAL EVENT TRIGGERED]");
            // NOTE! - It is extremely important to read the data received from the
            // serial port.  If it does not get read from the receive buffer, the
            // buffer will continue to grow and consume memory.
            // print out the data received to the console
            try {
                if (event.getReader().available() != -1) {
                    SerialDataEvent event1 = event;
                    console.println("\n[SERIAL DATA]   " + Arrays.toString(event.getReader().read(event.getReader().available())));
                    console.println("\n[SERIAL DATA]   " + event1.getSerial().toString());
                    console.println("\n[HEX DATA]   " + event1.getHexByteString());
                    console.println("\n[ASCII DATA] " + event1.getAsciiString());
                } else {
                    console.println("\n[available() ERROR SERIAL] " + event.getReader().available());
                }
            } catch (IOException e) {
                console.println("\n[ERROR SERIAL] " + e);
            }
        });

        if (!OSValidator.isWindows()) {
            try {
                // create serial config object
                SerialConfig config = new SerialConfig();
                /*
                 *set default serial settings (device, baud rate, flow control, etc)
                 *by default, use the DEFAULT com port on the Raspberry Pi (exposed on GPIO header)
                 *NOTE: this utility method will determine the default serial port for the
                 *      detected platform and board/model.  For all Raspberry Pi models
                 *      except the 3B, it will return "/dev/ttyAMA0".  For Raspberry Pi
                 *      model 3B may return "/dev/ttyS0" or "/dev/ttyAMA0" depending on
                 *      environment configuration.
                 */
                config.device(deviceType.toString()).baud(Baud._9600).dataBits(DataBits._8).parity(Parity.NONE).stopBits(StopBits._1).flowControl(FlowControl.NONE);

                // display connection details
                console.box(" Connecting to: " + config, " We are sending ASCII data on the serial port every 1 second.", " Data received on serial port will be displayed below. (EDIT: REMOVED THIS, TODO: MAYBE ADD RECT CODE DIPLAY TO SERIAL?)");
                // open the default serial device/port with the configuration settings
                if (OSValidator.isWindows()) {

                } else {
                    serial.open(config);
                }
                // continuous loop to keep the program running until the user terminates the program
//            while (console.isRunning()) {
//                try {
//                    // write a formatted string to the serial transmit buffer
//                    serial.write("CURRENT TIME: " + new Date());
//                    // write a individual bytes to the serial transmit buffer
//                    serial.write((byte) 13);
//                    serial.write((byte) 10);
//                    // write a simple string to the serial transmit buffer
//                    serial.write("Second Line");
//                    // write a individual characters to the serial transmit buffer
//                    serial.write('\r');
//                    serial.write('\n');
//                    // write a string terminating with CR+LF to the serial transmit buffer
//                    serial.writeln("Third Line");
//                } catch (IllegalStateException ex) {
//                    ex.printStackTrace();
//                }
//                // wait 1 second before continuing
//               Thread.sleep(1000);
//            }
                // we are done; close serial port
//            serial.close();
            } catch (IOException ex) {
                console.println(" ==>> SERIAL SETUP FAILED : " + ex.getMessage());
            }
        }
    }

    public void clearScreen() {
        if (!OSValidator.isWindows()) {
            try {
                serial.write("Q/n");
                serial.write("Q/n");
                serial.write("Q/n");
                serial.write("Q/n");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            logger.info("clearScreen() RAN \n");
        }
    }

    public void runSerial(InputStream inputStream) {
        if (!OSValidator.isWindows()) {
            try {
                serial.write(inputStream);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            logger.info("runSerial() RAN \n");
        }
    }
}

// END SNIPPET: serial-snippet
