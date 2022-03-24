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
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link CustomPlantIrrigationStationBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Philip Hirschle - Initial contribution
 */
@NonNullByDefault
public class CustomPlantIrrigationStationBindingConstants {

    private static final String BINDING_ID = "customplantirrigationstation";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_CUSTOM_IRRIGATION_SYSTEM = new ThingTypeUID(BINDING_ID, "custom-irrigation-system");
    public static final ThingTypeUID THING_TYPE_SUPERVISED_PLANT = new ThingTypeUID(BINDING_ID, "supervised-plant");

    // List of all Channel ids
    public static final String CHANNEL_WATER_LEVEL = "water-level";
    public static final String CHANNEL_REFERENCE_EARTH_MOISTURE = "reference-earth-moisture";
    public static final String CHANNEL_EARTH_MOISTURE = "earth-moisture";
    public static final String CHANNEL_IRRIGATION = "irrigation";
    public static final String CHANNEL_PLANT_LOCATION = "plant-location";
}
