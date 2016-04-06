package com.iotracks.api.client;

import com.iotracks.api.handler.IOContainerWSAPIHandler;
import io.netty.channel.Channel;

import java.util.logging.Logger;

/**
 * Creates a WebSocket connection to ioFabric in a separate thread.
 *
 * @author ilaryionava
 */
public class IOWebSocketConnector implements Runnable {

    private static final Logger log = Logger.getLogger(IOWebSocketConnector.class.getName());

    private IOContainerWSAPIHandler handler;
    private boolean ssl;
    private String host;
    private int port;

    public IOWebSocketConnector(IOContainerWSAPIHandler handler, boolean ssl, String host, int port) {
        this.handler = handler;
        this.ssl = ssl;
        this.host = host;
        this.port = port;
    }

    @Override
    public void run() {
        IOFabricAPIConnector ioFabricAPIConnector = new IOFabricAPIConnector(handler, ssl);
        Channel channel = ioFabricAPIConnector.initConnection(host, port);
        try {
            handler.handshakeFuture().sync();
        } catch (InterruptedException e) {
            log.warning("Error synchronizing channel for WebSocket connection.");
        }
    }
}
