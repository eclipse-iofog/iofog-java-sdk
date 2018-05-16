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

package org.eclipse.iofog.ws.manager.listener;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import org.eclipse.iofog.ws.manager.WebSocketManager;

/**
 * Interface of listener for WebSocket Manager.
 *
 * @author ilaryionava
 */
public interface WebSocketManagerListener {

    /**
     * Method that handles binary data calls (BinaryWebSocketFrame).
     *
     * @param wsManager - WebSocket manager
     * @param frame - binary frame to process
     * @param ctx - current channel handler context
     *
     */
    void handle(WebSocketManager wsManager, BinaryWebSocketFrame frame, ChannelHandlerContext ctx);
}
