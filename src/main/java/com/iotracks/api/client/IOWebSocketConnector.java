package com.iotracks.api.client;

import com.iotracks.api.handler.IOContainerWSAPIHandler;

import java.net.ConnectException;
import java.util.logging.Logger;

/**
 * Creates a WebSocket connection to ioFabric in a separate thread.
 *
 * @author ilaryionava
 */
public class IOWebSocketConnector implements Runnable {

    private static final Logger log = Logger.getLogger(IOWebSocketConnector.class.getName());

    private IOFabricAPIConnector ioFabricAPIConnector;
    private IOContainerWSAPIHandler handler;
    private boolean ssl;
    private String host;
    private int port;
    public final Boolean lock = true;
    private static Boolean caughtException = false;

    public IOWebSocketConnector(IOContainerWSAPIHandler handler, boolean ssl, String host, int port) {
        this.handler = handler;
        this.ssl = ssl;
        this.host = host;
        this.port = port;
    }

    @Override
    public void run() {
        synchronized (lock) {
            ioFabricAPIConnector = new IOFabricAPIConnector(handler, ssl);
            try {
                ioFabricAPIConnector.initConnection(host, port);
                handler.handshakeFuture().sync();
            } catch (InterruptedException e) {
                log.warning("Error synchronizing channel for WebSocket connection.");
                caughtException = true;
            } catch (ConnectException e) {
                log.warning("Socket Connection Error.");
                caughtException = true;
            } finally {
                lock.notifyAll();
            }
        }
    }

    public void terminate(){
        caughtException = false;
        if(ioFabricAPIConnector != null){
            ioFabricAPIConnector.destroyConnection();
        }
    }

    public Boolean isCaughtException() {
        return caughtException;
    }

}
