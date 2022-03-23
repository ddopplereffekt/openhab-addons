package org.openhab.binding.customplantirrigationstation.internal;

import java.nio.ByteBuffer;

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


/**
 * This is a singleton class which provides the methods for UART communication with the raspberry pico. The singleton
 * access and creation is thread safe. As there will always be only one raspberry pico included in the system it is
 * suitable that this class is a singleton.
 *
 * @author Philip Hirschle - Initial Contribution
 */
final class UARTConnection {
    // the singleton instance
    private volatile static UARTConnection instance;


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


    private final Context pi4j;
    private final SerialConfig configuration;
    private final SerialProvider ioProvider;


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
    @SuppressWarnings("usy-waiting")
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
     * This method has to be called befor the programm closes to shut down and clean up the serial connection.
     */
    void closeConnection() {
        try {
            this.pi4j.shutdown();
        } catch (ShutdownException e) {
            // TODO: some logging of fatal error
        }
    }
}


/**
 * This class provides Methods for sending and retrieving messages to the raspberry pico.
 *
 * @author Philip Hirschle - Initial Contribution
 */
public final class PicoCommunicator {


    /**
     * This function will return the message in a suitable format for sending to the raspberry pico via UART. The
     * message is generated based on the given parameter.
     * @param nachrichtenTyp: A integer which encodes the kind of message which should be generated.
     * @return The message which can be sent to the raspberry pico.
     */
    private static char getCommunicationMessage(int nachrichtenTyp) {
        char res;
        switch (nachrichtenTyp) {
            case 0:
                res = 'B';
                break;
            case 1:
                res = 'C';
                break;
            default:            // test case
                res = 'T';
                break;
        }
        return res;
    }


    ByteBuffer sendReceive (int nachrichtTyp) throws UARTConnectionBuildupFailure {
        return UARTConnection.getInstance().communicate((getCommunicationMessage(nachrichtTyp)));
    }


    void cleanup() throws UARTConnectionBuildupFailure {
        UARTConnection.getInstance().closeConnection();
    }
}


/*
Pinlayout of Raspberry Pi 4b:

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
