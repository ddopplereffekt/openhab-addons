package org.openhab.binding.customplantirrigationstation.internal;


/**
 * This class provides Methods for handeling the communication with the raspberry pico.
 *
 * @author Philip Hirschle - Initial Contribution
 */
public class PicoCommunicator {

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

}
