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


/**
 * The {@link CommunicationParser} class is responsible for parsing the answer of the raspberry pico.
 *
 * @author Philip Hirschle - Initial Contribution
 */
public class CommunicationParser {


    static double parseCommunciation(ByteBuffer answer) {
        // TODO
        return 1.0;
    }
}
