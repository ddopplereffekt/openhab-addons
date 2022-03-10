package org.openhab.binding.customplantirrigationstation.internal;



/**
 * This class provides Methods for handeling the communication with the raspberry pico.
 *
 * @author Philip Hirschle - Initial Contribution
 */
public class PicoCommunicator {
    /*
    Pinlayout of Raspberry Pi 4b:

            3V3                 (1)  (2)               5V
            GPIO2 ; I2C Data    (3)  (4)               5V
            GPIO3 ; I2C CLK     (5)  (6)               GND
            GPIO4               (7)  (8)    UART TXD ; GPIO14
            GND                 (9)  (10)   UART RXD ; GPIO15
            GPIO17              (11) (12)              GPIO18
            GPIO27              (13) (14)              GND
            GPIO22              (15) (16)              GPIO23
            3V3                 (17) (18)              GPIO24
            GPIO10              (19) (20)              GND
            GPIO9               (21) (22)              GPIO25
            GPIO11              (23) (24)              GPIO8
            GND                 (25) (26)              GPIO7
            GPIO0 ; I2C EEPROM  (27) (28) I2C EEPROM ; GPIO1
            GPIO5               (29) (30)              GND
            GPIO6               (31) (32)              GPIO12
            GPIO13              (33) (34)              GND
            GPIO19              (35) (36)              GPIO16
            GPIO26              (37) (38)              GPIO20
            GND                 (39) (40)              GPIO21
    */

    /**
     * This function will return the fitting message which will be sent to the raspberry pico based on the given
     * parameter.
     * @param nachrichtenTyp: A integer which encodes the kind of message which should be generated.
     * @return The message which can be sent to the raspberry pico.
     */
    private static String getCommunicationMessage(int nachrichtenTyp) {
        String res;
        switch (nachrichtenTyp) {
            case 0:
                res = "Beispiel";
                break;
            case 1:
                res = "Beispiel";
                break;
            default:
                res = "Test Verbindung Nachricht";
                break;
        }
        return res;
    }


    private static void testCommunication() {
        PicoCommunicator.sendMessage(PicoCommunicator.getCommunicationMessage(-1));
    }


    private static void sendMessage(String m) {

    }
}
