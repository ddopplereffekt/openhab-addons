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
package org.openhab.binding.customplantirrigationstation.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.customplantirrigationstation.internal.communication.CommunicationParser;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.*;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openhab.binding.customplantirrigationstation.internal.configuration.CustomPlantIrrigationBridgeConfiguration;
import org.openhab.binding.customplantirrigationstation.internal.communication.PicoCommunicator;

import static org.openhab.binding.customplantirrigationstation.internal.CustomPlantIrrigationStationBindingConstants.*;
import static org.openhab.binding.customplantirrigationstation.internal.communication.CommunicationMessages.*;


/**
 * The {@link CustomPlantIrrigationBridgeHandler} is responsible for handling commands,
 * which are sent to one of the channels.
 *
 * @author Philip Hirschle - Initial contribution
 */
@NonNullByDefault
public class CustomPlantIrrigationBridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(CustomPlantIrrigationBridgeHandler.class);

    private @Nullable CustomPlantIrrigationBridgeConfiguration config;


    public CustomPlantIrrigationBridgeHandler(Bridge bridge) {
        super(bridge);
    }


    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        try {
            if (channelUID.getId().equals(CHANNEL_WATER_LEVEL)) {
                logger.debug("handeling\nchannel:" + channelUID.getAsString() + "\ncommand: " + command
                        + "\nwill therefore check the water level");
                updateState(CHANNEL_WATER_LEVEL,
                        new DecimalType(
                                CommunicationParser.parseReceiveMessage(
                                        PicoCommunicator.sendReceive(
                                                CommunicationParser.parseSendMessage(GET_WATER_LEVEL, -1, -1)))));

            }
        } catch (Exception e) {
            logger.error("During handling of the channelUID " + channelUID.getAsString()
                    + " and the command " + command.toFullString(), e);
            this.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }


    /**
     * This function will make a humidity measurement of the given plant and return the humidity value.
     * @param location this is a unique identifier for the plant.
     */
    double measureMoisture(int location) {
        logger.debug("send request for the plant at location" + location + " to meassure the soil moisture");
        return CommunicationParser.parseReceiveMessage(
                PicoCommunicator.sendReceive(
                        CommunicationParser.parseSendMessage(GET_HUMIDITY, location, -1)));
    }


    /**
     * This function will irrigate a given plant.
     * @param location this is a unique identifier for the plant.
     * @param wateringTime this is the time in seconds that the given plant should be irrigated.
     */
    void waterPlant(int location, int wateringTime) {
        logger.debug("send request for the plant at location" + location + " to irrigate " + wateringTime + " s");
        updateState(CHANNEL_WATER_LEVEL,
                new DecimalType(
                        CommunicationParser.parseReceiveMessage(
                                PicoCommunicator.sendReceive(
                                        CommunicationParser.parseSendMessage(IRRIGATE, location, wateringTime)))));
    }


    @Override
    public void initialize() {
        this.config = getConfigAs(CustomPlantIrrigationBridgeConfiguration.class);

        // set the thing status to UNKNOWN temporarily and let the background task decide for the real status.
        // the framework is then able to reuse the resources from the thing handler initialization.
        // we set this upfront to reliably check status updates in unit tests.
        updateStatus(ThingStatus.UNKNOWN);

        // for background initialization
        scheduler.execute(() -> {
            boolean thingReachable = PicoCommunicator.initialize();

            if (thingReachable) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR, "UART connection failed");
            }
        });

        // These logging types should be primarily used by bindings
        // logger.trace("Example trace message");
        // logger.debug("Example debug message");
        // logger.warn("Example warn message");
        //
        // Logging to INFO should be avoided normally.
        // See https://www.openhab.org/docs/developer/guidelines.html#f-logging
    }


    @Override
    public void childHandlerInitialized(ThingHandler thingHandler, Thing thing) {
        super.childHandlerInitialized(thingHandler, thing);
    }


    @Override
    public void childHandlerDisposed(ThingHandler thingHandler, Thing thing) {
        super.childHandlerDisposed(thingHandler, thing);
    }


    @Override
    public void dispose() {
        PicoCommunicator.cleanup(CommunicationParser.parseSendMessage(DISPOSE, -1, -1));
        super.dispose();
    }
}
