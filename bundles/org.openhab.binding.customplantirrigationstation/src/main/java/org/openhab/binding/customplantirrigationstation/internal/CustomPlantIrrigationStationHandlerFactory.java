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

import static org.openhab.binding.customplantirrigationstation.internal.CustomPlantIrrigationStationBindingConstants.*;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link CustomPlantIrrigationStationHandlerFactory} is responsible for creating things and thing  handlers.
 *
 * @author Philip Hirschle - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.customplantirrigationstation", service = ThingHandlerFactory.class)
public class CustomPlantIrrigationStationHandlerFactory extends BaseThingHandlerFactory {

    private static final List<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = List.of(
            THING_TYPE_CUSTOM_IRRIGATION_SYSTEM, THING_TYPE_SUPERVISED_PLANT);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_SUPERVISED_PLANT.equals(thingTypeUID)) {
            return new CustomPlantIrrigationBridgeHandler((Bridge) thing);
        } else if (THING_TYPE_CUSTOM_IRRIGATION_SYSTEM.equals(thingTypeUID)) {
            return new CustomPlantIrrigationPlantHandler(thing);
        }
        return null;
    }
}
