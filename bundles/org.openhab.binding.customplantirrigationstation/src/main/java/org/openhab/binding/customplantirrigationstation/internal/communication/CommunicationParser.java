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

import org.openhab.binding.customplantirrigationstation.internal.CustomPlantIrrigationBridgeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.jdt.annotation.NonNullByDefault;

import static org.openhab.binding.customplantirrigationstation.internal.communication.CommunicationMessages.*;
import static org.openhab.binding.customplantirrigationstation.internal.communication.CommunicationMessages.DISPOSE;


/**
 * The {@link CommunicationParser} class is responsible for parsing the answer of the raspberry pico.
 *
 * @author Philip Hirschle - Initial Contribution
 */
@NonNullByDefault
public final class CommunicationParser {

    private static final Logger logger = LoggerFactory.getLogger(CommunicationParser.class);

    private final static EnumMap<CommunicationMessages, Character> messageMap = new EnumMap<>(Map.of(
            GET_HUMIDITY,               'c',
            IRRIGATE,                   'c',
            INITIALIZE,                 'c',
            DISPOSE,                    'c'
    ));


    private CommunicationParser() {}


    /**
     * This method parses the message received from the raspberry pico to a double which can be used, given the context
     * of the message which the given answer relates to.
     * @param answer the answer which is to be parsed.
     * @return the answer as a double.
     */
    public static double parseReceiveMessage(ByteBuffer answer) {
        double res = 1.0;
        logger.debug("parsed the answer:\n" + answer + "\n\ninto:\n" + res);
        return res;
    }


    /**
     * This method parses the given parameter to a message suitable for the {@link PicoCommunicator} class for
     * communication with the raspberry pico.
     * @param type specifies which kind of message is wanted.
     * @param location this is a unique identifier for the plant.
     * @param wateringTime this is the time in seconds that the given plant should be irrigated.
     * @return the parsed message which can be used by the {@link PicoCommunicator} class.
     */
    public static ByteBuffer parseSendMessage(CommunicationMessages type, int location, int wateringTime) {
        ByteBuffer res = null;
        logger.debug("parsedthe parameter\ntype: " + type + "\nlocation: " + location
                + "\nwateringTime: " + wateringTime + "\n\ninto:\n" + res);
        return res;
    }
}
