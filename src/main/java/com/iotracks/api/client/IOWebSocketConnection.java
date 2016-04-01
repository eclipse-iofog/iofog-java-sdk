package com.iotracks.api.client;

import com.iotracks.api.handler.IOContainerWSAPIHandler;
import io.netty.channel.Channel;

import java.util.logging.Logger;

/**
 * Creates a WebSocket connection to ioFabric in a separate thread.
 *
 * Created by forte on 3/31/16.
 *
 * @author ilaryionava
 */
public class IOWebSocketConnection implements Runnable {

    private static final Logger log = Logger.getLogger(IOWebSocketConnection.class.getName());

    private IOContainerWSAPIHandler handler;
    private boolean ssl;
    private String host;
    private int port;

    public IOWebSocketConnection(IOContainerWSAPIHandler handler, boolean ssl, String host, int port) {
        this.handler = handler;
        this.ssl = ssl;
        this.host = host;
        this.port = port;
    }

    @Override
    public void run() {
        IOFabricAPIClient ioFabricAPIClient = new IOFabricAPIClient(handler, ssl);
        Channel channel = ioFabricAPIClient.connect(host, port);
        try {
            handler.handshakeFuture().sync();
        } catch (InterruptedException e) {
            log.warning("Error synchronizing channel for WebSocket connection.");
        }
    }
}
