/*
 * *******************************************************************************
 *   Copyright (c) 2018 Edgeworx, Inc.
 *
 *   This program and the accompanying materials are made available under the
 *   terms of the Eclipse Public License v. 2.0 which is available at
 *   http://www.eclipse.org/legal/epl-2.0
 *
 *   SPDX-License-Identifier: EPL-2.0
 * *******************************************************************************
 */

package org.eclipse.iofog.utils;

import javax.json.JsonObject;

/**
 * Util class for all response field names that Container can get from ioFog.
 *
 * @author ilaryionava
 */
public class IOFogResponseUtils {

    public static final String ID_FIELD_NAME = "id";
    public static final String STATUS_FIELD_NAME = "status";
    public static final String COUNT_FIELD_NAME = "count";
    public static final String CONFIG_FIELD_NAME = "config";
    public static final String MESSAGES_FIELD_NAME = "messages";
    public static final String TIMESTAMP_FIELD_NAME = "timestamp";
    public static final String TIMEFRAME_START_FIELD_NAME = "timeframestart";
    public static final String TIMEFRAME_END_FIELD_NAME = "timeframeend";

    public static boolean isMessageQuery(JsonObject json) {
        return json.containsKey(TIMEFRAME_START_FIELD_NAME) &&
                json.containsKey(TIMEFRAME_END_FIELD_NAME);
    }

    public static boolean isMessageReceipt(JsonObject json) {
        return json.containsKey(ID_FIELD_NAME) && json.containsKey(TIMESTAMP_FIELD_NAME);
    }

    public static boolean isNewMessage(JsonObject json) {
        return json.containsKey(MESSAGES_FIELD_NAME);
    }

    public static boolean isNewConfig(JsonObject json) {
        return json.containsKey(CONFIG_FIELD_NAME);
    }

}
