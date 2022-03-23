package org.openhab.binding.customplantirrigationstation.internal.exceptions;


/**
 * Special exception for the case: The serial (UART) to the raspberry pico could not be achieved.
 *
 * @author Philip Hirschle - Initial Contribution
 */
public class UARTConnectionBuildupFailure extends Exception {
    public UARTConnectionBuildupFailure() {
        super();
    }

    public UARTConnectionBuildupFailure(String message) {
        super(message);
    }

    public UARTConnectionBuildupFailure(String message, Throwable e) {
        super(message, e);
    }
}
