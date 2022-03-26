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

import java.util.HashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.*;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openhab.binding.customplantirrigationstation.internal.config.CustomPlantIrrigationStationConfiguration;
import org.openhab.binding.customplantirrigationstation.internal.communication.PicoCommunicator;
import static org.openhab.binding.customplantirrigationstation.internal.CustomPlantIrrigationStationBindingConstants.*;
import static org.openhab.binding.customplantirrigationstation.internal.communication.CommunicationMessages.*;


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
    private @Nullable HashMap<Integer, PlantInformations> location2PlantData;


    public CustomPlantIrrigationBridgeHandler(Bridge bridge) {
        super(bridge);
    }


    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (channelUID.getId().equals(CHANNEL_WATER_LEVEL)) {
            // TODO wie kommt Ergebnis zurück?
            scheduler.execute(() -> PicoCommunicator.sendReceive(GET_WATER_LEVEL));
            updateState(CHANNEL_WATER_LEVEL, new DecimalType(1.0));
        }
    }


    /**
     *
     * @param location
     */
    double measureMoisture(int location) {
        // TODO wie kommt das Ergebnis wieder zur Pflanze zurück?
        scheduler.execute(() -> PicoCommunicator.sendReceive(GET_HUMIDITY));
        return 1.0;
    }


    /**
     *
     * @param location
     */
    void waterPlant(int location, int wateringTime) {
        // TODO irgendwie die Informationen für die Bewässerung mitgeben
        scheduler.execute(() -> updateState(CHANNEL_WATER_LEVEL, new DecimalType(PicoCommunicator.sendReceive(IRRIGATE))));
        updateState(CHANNEL_WATER_LEVEL, new DecimalType(1.0));
    }


    @Override
    public void initialize() {
        this.config = getConfigAs(CustomPlantIrrigationStationConfiguration.class);

        // set the thing status to UNKNOWN temporarily and let the background task decide for the real status.
        // the framework is then able to reuse the resources from the thing handler initialization.
        // we set this upfront to reliably check status updates in unit tests.
        updateStatus(ThingStatus.UNKNOWN);

        this.location2PlantData = new HashMap<>(5);
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
        this.location2PlantData.put(location, new PlantInformations());
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
        this.location2PlantData.remove(location);
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
