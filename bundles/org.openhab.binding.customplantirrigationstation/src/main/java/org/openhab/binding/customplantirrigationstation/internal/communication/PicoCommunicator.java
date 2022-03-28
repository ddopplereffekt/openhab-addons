/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.customplantirrigationstation.internal.communication;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.exception.ShutdownException;
import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialConfig;
import com.pi4j.io.serial.Parity;
import com.pi4j.io.serial.FlowControl;
import com.pi4j.io.serial.StopBits;
import com.pi4j.io.serial.SerialProvider;
import com.pi4j.provider.exception.ProviderNotFoundException;

import org.openhab.binding.customplantirrigationstation.internal.CustomPlantIrrigationBridgeHandler;
import org.openhab.binding.customplantirrigationstation.internal.exceptions.UARTConnectionBuildupFailure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The {@link UARTConnection} is a singleton class which provides the methods for UART communication with the raspberry
 * pico. The singleton access and creation is thread safe. As there will always be only one raspberry pico included in
 * the system it is suitable that this class is a singleton.
 *
 * @author Philip Hirschle - Initial Contribution
 */
@NonNullByDefault
final class UARTConnection {

    private static final Logger logger = LoggerFactory.getLogger(UARTConnection.class);


    // the singleton instance
    private static volatile @Nullable UARTConnection instance;


    /**
     * This method is used to get the singleton object. This is a thread safe implementation.
     * @return the Pico singleton instance.
     */
    static UARTConnection getInstance() throws UARTConnectionBuildupFailure {
        UARTConnection localRef = UARTConnection.instance;
        if(localRef == null) {
            synchronized (UARTConnection.class) {
                localRef = UARTConnection.instance;
                if (localRef == null) {
                    UARTConnection.instance = localRef = new UARTConnection();
                }
            }
        }
        return localRef;
    }


    private final @Nullable Context pi4j;
    private final @Nullable SerialConfig configuration;
    private final @Nullable SerialProvider ioProvider;


    /**
     * The constructor of the class will initialize everything necessary for the UART communication.
     */
    private UARTConnection() throws UARTConnectionBuildupFailure {
        // Initialize Pi4J with an auto context
        this.pi4j = Pi4J.newAutoContext();
        if (this.pi4j == null) {
            logger.error("Pi4J object for UART connection could not be created");
            throw new UARTConnectionBuildupFailure("Pi4J object for UART connection could not be created");
        }

        this.configuration =  Serial.newConfigBuilder(pi4j)
                .provider("pigpio-serial")
                .id("uart-port-to-pico")
                .name("Serial (UART) port to raspberry pico")
                .use_9600_N81()
                .flowControl(FlowControl.NONE)
                .dataBits_7()
                .parity(Parity.ODD)
                .stopBits(StopBits._1)
                .device("/dev/ttyS0")
                .build();
        if (this.configuration == null) {
            logger.error("Configuration object for UART connection could not be created");
            throw new UARTConnectionBuildupFailure("Configuration object for UART connection could not be created");
        }

        try {
            this.ioProvider = pi4j.provider("pigpio-serial");
        } catch (ProviderNotFoundException e) {
            logger.error("pigpio-serial provider is neccesary for this class to function", e);
            throw new UARTConnectionBuildupFailure(e.getMessage());
        }
    }


    /**
     * This method sends a message over a serial (UART) connection and returns the answer.
     * @param data This is the message to be sent.
     * @return The message which was received.
     */
    ByteBuffer communicate(ByteBuffer data) {
        ByteBuffer result;
        assert this.ioProvider != null;
        try (Serial serialConnection = this.ioProvider.create(configuration)) {
            //sending the message
            serialConnection.write(data);

            // waiting for the answer
            while (serialConnection.available() < 1) {          // TODO is one byte enough?
                try {
                    //noinspection BusyWait
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    logger.debug("This thread got interrupted while waiting for the answer of the raspberry pico via the serial connection.");
                    Thread.yield();
                }
            }

            // receiving the answer
            result = serialConnection.readByteBuffer(7);        // TODO: wie lang ist? Paritätsbit -> 7?

        } catch (Exception e) {
            // TODO: logging or handle information otherwise
            int i;
            throw e;
        }
        return result;
    }


    /**
     * This method has to be called before the program closes to shut down and clean up the serial connection.
     */
    private void closeConnection() {
        assert this.pi4j != null;
        try {
            this.pi4j.shutdown();
        } catch (ShutdownException e) {
            logger.error("encountered while trying to close the Connection to the raspberry pico", e);
        }
    }


    /**
     * This method has to be called before the program closes to shut down and clean up the serial connection.
     */
    static void cleanUp() {
        try {
            UARTConnection.instance.closeConnection();
            UARTConnection.instance = null;
        } catch (NullPointerException e) {
            logger.error("Nullpointer Exception while trying to clean up the connection to the raspberry pico.\n" +
                    "This indicates that the call to PicoCommunicator.initialize was not successful.\n" +
                    "This error can be ignored if the communication to the raspberrry pico is not needed anymore.");
        }
    }
}


/**
 * The {@link PicoCommunicator} provides Methods for sending and retrieving messages to the raspberry pico. The
 * communication is thread safe by locking a {@link ReentrantLock} object.
 *
 * @author Philip Hirschle - Initial Contribution
 */
@NonNullByDefault
public final class PicoCommunicator {

    private static final Logger logger = LoggerFactory.getLogger(PicoCommunicator.class);

    private static final ReentrantLock lock = new ReentrantLock(true);


    /**
     * This function will send a message to the raspberry pico and return the answer.
     * @param message: The message to be sent.
     * @return The message which can be sent to the raspberry pico.
     */
    @Nullable
    public static ByteBuffer sendReceive (ByteBuffer message) {
        PicoCommunicator.lock.lock();
        ByteBuffer res;
        try {
            res = UARTConnection.getInstance().communicate(message);
        } catch (UARTConnectionBuildupFailure e) {
            logger.error("The message could not be sent or could not be received", e);
        }
        PicoCommunicator.lock.unlock();
        return res;            // TODO leeren Buffer!? -> check auf leeren Buffer in CommunicationParser
    }


    /**
     * This method initializes the connection to the raspberry pico.
     * @return true if the connection is built up successful otherwise false.
     */
    public static boolean initialize() {
        try {
            UARTConnection.getInstance();
        } catch (UARTConnectionBuildupFailure e) {
            return false;
        }
        logger.debug("Initializing of the serial connection to the raspberry pico was successful.");
        return true;
    }


    /**
     * This method has to be called on program end or binding disposal.
     */
    public static void cleanup(ByteBuffer message) {
        PicoCommunicator.lock.lock();
        PicoCommunicator.sendReceive(message);
        UARTConnection.cleanUp();
        logger.debug("Cleaning up serial connection to raspberry pico was successful.");
        PicoCommunicator.lock.unlock();
    }
}


/*
Pin layout of Raspberry Pi 4b:

        3V3                  (1)  (2)                5V
        GPIO2 ; I2C Data     (3)  (4)                5V
        GPIO3 ; I2C CLK      (5)  (6)                GND
        GPIO4                (7)  (8)     UART TXD ; GPIO14
        GND                  (9)  (10)    UART RXD ; GPIO15
        GPIO17               (11) (12)               GPIO18
        GPIO27               (13) (14)               GND
        GPIO22               (15) (16)               GPIO23
        3V3                  (17) (18)               GPIO24
        GPIO10               (19) (20)               GND
        GPIO9                (21) (22)               GPIO25
        GPIO11               (23) (24)               GPIO8
        GND                  (25) (26)               GPIO7
        GPIO0 ; I2C EEPROM   (27) (28)  I2C EEPROM ; GPIO1
        GPIO5                (29) (30)               GND
        GPIO6                (31) (32)               GPIO12
        GPIO13               (33) (34)               GND
        GPIO19               (35) (36)               GPIO16
        GPIO26               (37) (38)               GPIO20
        GND                  (39) (40)               GPIO21
*/
