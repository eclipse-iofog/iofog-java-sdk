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

package org.eclipse.iofog.api.client;

import org.eclipse.iofog.api.handler.IOContainerWSAPIHandler;

import java.util.logging.Logger;

/**
 * Creates a WebSocket connection to ioFog in a separate thread.
 *
 * @author ilaryionava
 */
public class IOWebSocketConnector implements Runnable {

    private static final Logger log = Logger.getLogger(IOWebSocketConnector.class.getName());

    private IOFogAPIConnector ioFogAPIConnector;
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
            ioFogAPIConnector = new IOFogAPIConnector(handler, ssl);
            try {
                ioFogAPIConnector.initConnection(host, port);
                handler.handshakeFuture().sync();
            } catch (InterruptedException e) {
                log.warning("Error synchronizing channel for WebSocket connection.");
                caughtException = true;
            } catch (Exception e) {
                log.warning("Connection exception. Probably ioFog is not reachable.");
                caughtException = true;
            } finally {
                lock.notifyAll();
            }
        }
    }

    public void terminate(){
        caughtException = false;
        if(ioFogAPIConnector != null){
            ioFogAPIConnector.destroyConnection();
        }
    }

    public Boolean isCaughtException() {
        return caughtException;
    }

}
