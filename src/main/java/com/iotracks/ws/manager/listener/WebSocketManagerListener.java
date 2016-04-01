package com.iotracks.ws.manager.listener;

import com.iotracks.ws.manager.WebSocketManager;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;

/**
 * Interface of listener for WebSocket Manager.
 *
 * Created by forte on 3/30/16.
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
