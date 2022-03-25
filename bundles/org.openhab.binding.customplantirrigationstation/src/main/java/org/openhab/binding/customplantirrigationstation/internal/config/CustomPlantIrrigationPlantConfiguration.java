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
package org.openhab.binding.customplantirrigationstation.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link CustomPlantIrrigationPlantConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Philip Hirschle - Initial contribution
 */
@NonNullByDefault
public class CustomPlantIrrigationPlantConfiguration {

    public @Nullable String name;
    public int location;
    public int waterQuantity = 5;           // pumping time in seconds; minimum is 1 second
    public int measurementInterval = 60;    // soil moisture measurement interval in seconds; minimum is one second
}
