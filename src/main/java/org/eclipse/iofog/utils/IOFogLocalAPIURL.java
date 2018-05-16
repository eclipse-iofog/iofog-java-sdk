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

/**
 * Enum for all ioFog's Local API requests URLs
 */
public enum IOFogLocalAPIURL {

    GET_CONFIG_REST_LOCAL_API ("/v2/config/get") ,
    GET_NEXT_MSG_REST_LOCAL_API ("/v2/messages/next"),
    POST_MSG_REST_LOCAL_API ("/v2/messages/new"),
    GET_MSGS_QUERY_REST_LOCAL_API ("/v2/messages/query"),
    GET_CONTROL_WEB_SOCKET_LOCAL_API ("/v2/control/socket/id/"),
    GET_MSG_WEB_SOCKET_LOCAL_API ("/v2/message/socket/id/");

    private String url;

    IOFogLocalAPIURL(String url){
       this.url = url;
    }

    public String getURL(){
        return this.url;
    }

}
