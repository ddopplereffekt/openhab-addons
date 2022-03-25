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
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openhab.binding.customplantirrigationstation.internal.config.CustomPlantIrrigationStationConfiguration;
import org.openhab.binding.customplantirrigationstation.internal.communication.PicoCommunicator;

import java.util.HashMap;


/**
 * The {@link CustomPlantIrrigationBridgeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Philip Hirschle - Initial contribution
 */
@NonNullByDefault
public class CustomPlantIrrigationBridgeHandler extends BaseBridgeHandler {
    private class PlantInformations{
        float minReference;
        float maxReference;
        // TODO: water intervall also
        PlantInformations() {

        }
    }

    private final Logger logger = LoggerFactory.getLogger(CustomPlantIrrigationBridgeHandler.class);

    private @Nullable CustomPlantIrrigationStationConfiguration config;
    private @Nullable HashMap<Integer, PlantInformations> plantData;


    public CustomPlantIrrigationBridgeHandler(Bridge bridge) {
        super(bridge);
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
        this.config = getConfigAs(CustomPlantIrrigationStationConfiguration.class);

        // set the thing status to UNKNOWN temporarily and let the background task decide for the real status.
        // the framework is then able to reuse the resources from the thing handler initialization.
        // we set this upfront to reliably check status updates in unit tests.
        updateStatus(ThingStatus.UNKNOWN);

        this.plantData = new HashMap<>(5);
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


    /**
     *
     * @param thingHandler
     * @param thing
     */
    @Override
    public void childHandlerInitialized(ThingHandler thingHandler, Thing thing) {
        int location = (int) thing.getConfiguration().get("location");
        this.plantData.put(location, new PlantInformations());
        super.childHandlerInitialized(thingHandler, thing);
    }


    /**
     *
     * @param thingHandler
     * @param thing
     */
    @Override
    public void childHandlerDisposed(ThingHandler thingHandler, Thing thing) {
        int location = (int) thing.getConfiguration().get("location");
        this.plantData.remove(location);
        super.childHandlerDisposed(thingHandler, thing);
    }


    /**
     * For handler disposal.
     */
    @Override
    public void dispose() {
        scheduler.execute(PicoCommunicator::cleanup);
        super.dispose();
    }
}
