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

import java.time.ZonedDateTime;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openhab.core.thing.*;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;

import org.openhab.binding.customplantirrigationstation.internal.config.CustomPlantIrrigationPlantConfiguration;
import static org.openhab.binding.customplantirrigationstation.internal.CustomPlantIrrigationStationBindingConstants.*;


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

    private @Nullable ScheduledFuture<?> moistureMeasurementJob;

    private @Nullable Double referenceDry;
    private @Nullable Double referenceWet;


    public CustomPlantIrrigationPlantHandler(Thing thing) {
        super(thing);
    }


    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        try {
            switch (channelUID.getId()) {
                case CHANNEL_IRRIGATION:
                    if (command instanceof RefreshType) {
                        break;          // only irrigate when the command is coming
                    }

                    if (command instanceof StringType) {
                        String c = command.toFullString();
                        if (c.equals("irrigate")) {
                            CustomPlantIrrigationBridgeHandler bridge = (CustomPlantIrrigationBridgeHandler) this.getBridge().getHandler();
                            assert bridge != null;
                            assert this.config != null;
                            bridge.waterPlant(this.config.location, this.config.waterQuantity);
                            updateState(CHANNEL_IRRIGATION, OnOffType.OFF);
                            // updateState(CHANNEL_IRRIGATION, new DateTimeType(ZonedDateTime.now()));
                        }
                    }
                    break;
                case CHANNEL_EARTH_MOISTURE:
                    if (command instanceof RefreshType) {
                        this.measureMoisture('0');
                    }

                    if (command instanceof StringType) {
                        String c = command.toFullString();
                        if (c.equals("meassure_soil_moisture")) {
                            this.measureMoisture('0');
                        }
                    }
                    break;
                default:                        // CHANNEL_REFERENCE_EARTH_MOISTURE
                    if (command instanceof RefreshType) {
                        if (this.referenceDry == null | this.referenceWet == null) {
                            updateState(CHANNEL_REFERENCE_EARTH_MOISTURE, new StringType("not-referenced"));
                        } else {
                            updateState(CHANNEL_REFERENCE_EARTH_MOISTURE, new StringType("referenced"));
                        }
                    }

                    if (command instanceof StringType) {
                        String c = command.toFullString();
                        if (c.equals("meassure_reference_4_dry_soil")) {
                            this.measureMoisture('d');
                        } else if (c.equals("meassure_reference_4_wet_soil")) {
                            this.measureMoisture('w');
                        }
                        if (this.referenceDry != null & this.referenceWet != null) {
                            updateState(CHANNEL_REFERENCE_EARTH_MOISTURE, new StringType("referenced"));
                        }
                    }
            }
        } catch (Exception e) {         // TODO: Exception type
            this.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "");
        }
    }


    /**
     *
     */
    private void measureMoisture(char c) {
        CustomPlantIrrigationBridgeHandler bridge = (CustomPlantIrrigationBridgeHandler) this.getBridge().getHandler();
        assert bridge != null;
        assert this.config != null;
        double res = bridge.measureMoisture(this.config.location);
        switch (c) {
            case 'd':
                this.referenceDry = res;
            case 'w':
                this.referenceWet = res;
            default:        // regular humidity measurement
                updateState(CHANNEL_EARTH_MOISTURE, new DecimalType(res));
                if (this.referenceDry != null & this.referenceWet != null) {
                    if (res <= this.referenceDry) {         // TODO Toleranz einabuen
                        updateState(CHANNEL_IRRIGATION, OnOffType.ON);
                    }
                }
        }
        // TODO
        // TODO no exception!
    }


    @Override
    public void initialize() {
        this.config = getConfigAs(CustomPlantIrrigationPlantConfiguration.class);

        // set the thing status to UNKNOWN temporarily and let the background task decide for the real status.
        // the framework is then able to reuse the resources from the thing handler initialization.
        // we set this upfront to reliably check status updates in unit tests.
        updateStatus(ThingStatus.UNKNOWN);

        if (this.config != null) {
            boolean locationTaken = getBridge().getThings().stream()
                    .anyMatch(thing -> thing.getConfiguration().get("location").equals(this.config.location));
            if (locationTaken) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                        "The location is already taken. Every location can only be used for one plant.");
            }

            this.moistureMeasurementJob = scheduler.scheduleWithFixedDelay(() -> this.measureMoisture('0'), 0, this.config.measurementInterval, TimeUnit.SECONDS);
            updateStatus(ThingStatus.ONLINE);
        }
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
                "Could not get the configuration, which is neccesary for the initialization.");

        // These logging types should be primarily used by bindings
        // logger.trace("Example trace message");
        // logger.debug("Example debug message");
        // logger.warn("Example warn message");
        //
        // Logging to INFO should be avoided normally.
        // See https://www.openhab.org/docs/developer/guidelines.html#f-logging
    }


    /**
     * This method must be implemented because the {@link CustomPlantIrrigationPlantHandler} needs a bridge
     * ({@link CustomPlantIrrigationBridgeHandler}).
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


    @Override
    public void dispose() {
        if (moistureMeasurementJob != null) {
            moistureMeasurementJob.cancel(true);
            moistureMeasurementJob = null;
        }
        super.dispose();
    }
}
