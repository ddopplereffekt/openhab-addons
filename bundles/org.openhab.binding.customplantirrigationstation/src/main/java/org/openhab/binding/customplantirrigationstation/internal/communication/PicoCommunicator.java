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

import java.nio.ByteBuffer;
import java.util.EnumMap;
import java.util.Map;

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

import org.openhab.binding.customplantirrigationstation.internal.exceptions.UARTConnectionBuildupFailure;

import static org.openhab.binding.customplantirrigationstation.internal.communication.CommunicationMessages.*;


/**
 * The {@link UARTConnection} is a singleton class which provides the methods for UART communication with the raspberry
 * pico. The singleton access and creation is thread safe. As there will always be only one raspberry pico included in
 * the system it is suitable that this class is a singleton.
 *
 * @author Philip Hirschle - Initial Contribution
 */
@NonNullByDefault
final class UARTConnection {
    // the singleton instance
    private @Nullable volatile static UARTConnection instance;


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
        assert this.pi4j != null;
        assert this.configuration != null;
        try {
            this.ioProvider = pi4j.provider("pigpio-serial");
        } catch (ProviderNotFoundException e) {
            // TODO: some logging and other stuff to get the error away
            throw new UARTConnectionBuildupFailure(e.getMessage());
        }
    }


    /**
     * This method sends a message over a serial (UART) connection and returns the answer.
     * @param data This is the message to be sent.
     * @return The message which was received.
     */
    ByteBuffer communicate(char data) {
        ByteBuffer result;
        try (Serial serialConnection = this.ioProvider.create(configuration)) {
            //sending the message
            serialConnection.write(data);

            // waiting for the answer
            while (serialConnection.available() < 1) {          // TODO is one byte enough?
                try {
                    //noinspection BusyWait
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    // TODO: some sort of logging
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
        try {
            this.pi4j.shutdown();
        } catch (NullPointerException e) {
            // TODO: some logging: this should not happen but could very well be okay since there is nothing to clean up
        } catch (ShutdownException e) {
            // TODO: some logging of fatal error
        }
    }


    /**
     * This method has to be called before the program closes to shut down and clean up the serial connection.
     */
    static void cleanUp() {
        assert UARTConnection.instance != null;
        UARTConnection.instance.closeConnection();
        UARTConnection.instance = null;
    }
}


/**
 * The {@link PicoCommunicator} provides Methods for sending and retrieving messages to the raspberry pico.
 *
 * @author Philip Hirschle - Initial Contribution
 */
@NonNullByDefault
public final class PicoCommunicator {

    private final static EnumMap<CommunicationMessages, Character> messageMap = new EnumMap<>(Map.of(
            GET_HUMIDITY,               'c',
            IRRIGATE,                   'c',
            INITIALIZE,                 'c',
            DISPOSE,                    'c'
    ));


    /**
     * This function will return the message in a suitable format for sending to the raspberry pico via UART. The
     * message is generated based on the given parameter.
     * @param message: A member of the {@link CommunicationMessages} enum which represents the kind of message which
     *               should be generated.
     * @return The message which can be sent to the raspberry pico.
     */
    public static double sendReceive (CommunicationMessages message) {
        // TODO: thread safety guaranty here (probably a lock)
        try {
            return CommunicationParser.parseCommunciation(UARTConnection.getInstance().communicate(messageMap.get(message)));
        } catch (UARTConnectionBuildupFailure e) {
            // TODO: logging , handling
        }
        return 0.0;
    }


    public static boolean initialize() {
        try {
            UARTConnection.getInstance();
        } catch (UARTConnectionBuildupFailure e) {
            // TODO: Some logging
            return false;
        }
        return true;
    }


    /**
     * This method has to be called on program end or binding disposal.
     */
    public static void cleanup() {
        PicoCommunicator.sendReceive(DISPOSE);
        UARTConnection.cleanUp();
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
