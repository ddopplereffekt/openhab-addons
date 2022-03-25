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
import org.openhab.core.thing.*;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openhab.binding.customplantirrigationstation.internal.config.CustomPlantIrrigationPlantConfiguration;


/**
 * The {@link CustomPlantIrrigationPlantHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Philip Hirschle - Initial contribution
 */
@NonNullByDefault
public class CustomPlantIrrigationPlantHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(CustomPlantIrrigationPlantHandler.class);

    private @Nullable CustomPlantIrrigationPlantConfiguration config;

    public CustomPlantIrrigationPlantHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (CHANNEL_1.equals(channelUID.getId())) {
            if (command instanceof RefreshType) {
                // TODO: handle data refresh
            }

            // TODO: handle command

            // Note: if communication with thing fails for some reason,
            // indicate that by setting the status with detail information:
            // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
            // "Could not control device at IP address x.x.x.x");
        }
    }

    @Override
    public void initialize() {
        this.config = getConfigAs(CustomPlantIrrigationPlantConfiguration.class);

        // set the thing status to UNKNOWN temporarily and let the background task decide for the real status.
        // the framework is then able to reuse the resources from the thing handler initialization.
        // we set this upfront to reliably check status updates in unit tests.
        updateStatus(ThingStatus.UNKNOWN);

        boolean locationTaken = getBridge().getThings().stream()
                .anyMatch(thing -> thing.getConfiguration().get("location").equals(config.location));
        if (locationTaken) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
                    "The location was taken. The bridge will try to automaticly localize the plant or use a default value.");
            // TODO: location automatic finding or to default changing
        }
        updateStatus(ThingStatus.ONLINE);

        // These logging types should be primarily used by bindings
        // logger.trace("Example trace message");
        // logger.debug("Example debug message");
        // logger.warn("Example warn message");
        //
        // Logging to INFO should be avoided normally.
        // See https://www.openhab.org/docs/developer/guidelines.html#f-logging
    }


    /**
     * This method must be implemented because the {@link CustomPlantIrrigationPlantHandler} needs a bridge (the {@link CustomPlantIrrigationBridgeHandler}).
     * @param statusInfo The {@link ThingStatusInfo} regarding the change of the {@link ThingStatus} of the bridge.
     */
    @Override
    public void bridgeStatusChanged(ThingStatusInfo statusInfo) {
        if (statusInfo.getStatus().equals(ThingStatus.OFFLINE)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        } else if (statusInfo.getStatus().equals(ThingStatus.ONLINE)) {
            updateStatus(ThingStatus.ONLINE);           // TODO: no check required that the connection to the pico is good?
        }
    }
}
