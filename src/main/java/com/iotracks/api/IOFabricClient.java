package com.iotracks.api;

import com.iotracks.api.client.*;
import com.iotracks.api.listener.*;
import com.iotracks.utils.IOFabricLocalAPIURL;
import com.iotracks.elements.IOMessage;
import com.iotracks.api.handler.*;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.*;
import io.netty.util.internal.StringUtil;

import javax.json.Json;
import javax.json.JsonObject;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Set;
import java.util.logging.Logger;

/**
 * IOFabricClient implements all methods to communicate with ioFabric (via local API).
 *
 * @author ilaryionava
 */
public class IOFabricClient {

    private static final Logger log = Logger.getLogger(IOFabricClient.class.getName());

    private final String ID_PARAM_NAME = "id";
    private final String TIMEFRAME_START_PARAM_NAME = "timeframestart";
    private final String TIMEFRAME_END_PARAM_NAME = "timeframeend";
    private final String PUBLISHERS_PARAM_NAME = "publishers";

    private String server;
    private int port;
    private boolean ssl;
    private String elementID = "UNKNOWN_IO_TRACKS_CONTEINER_UIID";
    private IOContainerWSAPIHandler handler;
    private IOFabricLocalAPIURL wsType;

    /**
     * @param host - the server name or ip address (by default "router")
     * @param port - the listening port (bye default 54321)
     * @param containerId - container's ID that will be used for all requests
     */
    public IOFabricClient(String host, int port, String containerId) {
        if (!StringUtil.isNullOrEmpty(host)) {
            this.server = host;
        } else {
            this.server = "iofabric";
            if(!isHostReachable()){
                log.warning("Host: " + server + " - is not reachable. Changing to default value: 127.0.0.1.");
                this.server = "127.0.0.1";
            }
        }
        this.port = port != 0 ? port : 54321;
        this.ssl = System.getProperty("ssl") != null;
        String selfname = System.getProperty("SELFNAME");
        if(!StringUtil.isNullOrEmpty(containerId)){
            this.elementID = containerId;
        } else if(!StringUtil.isNullOrEmpty(selfname)) {
            this.elementID = selfname;
        }
    }

    /**
     * Method sends REST request to ioFabric based on parameters.
     *
     * @param url - request url
     * @param content - json representation of request's content
     * @param listener - listener for REST communication with ioFabric
     *
     */
    private void sendRequest(IOFabricLocalAPIURL url, JsonObject content, IOFabricAPIListener listener){
        IOContainerRESTAPIHandler handler = new IOContainerRESTAPIHandler(listener);
        IOFabricAPIConnector localAPIConnector = new IOFabricAPIConnector(handler, ssl);
        Channel channel = localAPIConnector.initConnection(server, port);
        if(channel != null){
            channel.writeAndFlush(getRequest(url, HttpMethod.POST, content.toString().getBytes()));
            try {
                channel.closeFuture().sync();
            } catch (InterruptedException e) {
                log.warning("Error closing and synchronizing request channel.");
            }
        }
    }

    /**
     * Method creates request with necessary headers.
     *
     * @param url - request url
     * @param httpMethod - HTTP method type for request
     *
     * @return HttpRequest
     */
    private FullHttpRequest getRequest(IOFabricLocalAPIURL url, HttpMethod httpMethod, byte[] content){
        ByteBuf contentBuf = Unpooled.copiedBuffer(content);
        FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, httpMethod, getURI(url, false).getRawPath(), contentBuf);
        request.headers().set(HttpHeaders.Names.CONTENT_LENGTH, contentBuf.readableBytes());
        request.headers().set(HttpHeaders.Names.CONTENT_TYPE, "application/json");
        request.headers().set(HttpHeaders.Names.HOST, server);
        return request;
    }

    /**
     * Method opens a WebSocket connection to ioFabric in a separate thread.
     *
     * @param wsType - WebSocket type for connection
     * @param url - url for request
     * @param listener - listener for communication with ioFabric
     *
     */
    private void openWebSocketConnection(IOFabricLocalAPIURL wsType, IOFabricLocalAPIURL url, IOFabricAPIListener listener){
        this.wsType = wsType;
        handler = new IOContainerWSAPIHandler(listener, getURI(url, true), elementID, wsType);
        new Thread(new IOWebSocketConnector(handler, ssl, server, port)).start();
    }

    /**
     * Method sends IOMessage to ioFabric in case Message WebSocket connection is open.
     *
     * @param message - IOMessage to send
     *
     */
    public void sendMessageToWebSocket(IOMessage message){
        if(message != null) {
            message.setPublisher(elementID);
            if(handler != null && wsType == IOFabricLocalAPIURL.GET_MSG_WEB_SOCKET_LOCAL_API) {
                handler.sendMessage(elementID, message);
            } else {
                log.warning("Message can be sent to ioFabric only if MessageWebSocket connection is established.");
            }
        }
    }

    /**
     * Method sends request for current Container's configurations.
     *
     * @param listener - listener for communication with ioFabric
     *
     */
    public void fetchContainerConfig(IOFabricAPIListener listener){
        sendRequest(IOFabricLocalAPIURL.GET_CONFIG_REST_LOCAL_API, Json.createObjectBuilder().add(ID_PARAM_NAME, elementID).build(), listener);
    }

    /**
     * Method sends request for all Container's unread messages.
     *
     * @param listener - listener for communication with ioFabric
     *
     */
    public void fetchNextMessage(IOFabricAPIListener listener){
        sendRequest(IOFabricLocalAPIURL.GET_NEXT_MSG_REST_LOCAL_API, Json.createObjectBuilder().add(ID_PARAM_NAME, elementID).build(), listener);
    }

    /**
     * Method sends request to post Container's new IOMessage to the system.
     *
     * @param message - new IOMessage
     * @param listener - listener for communication with ioFabric
     *
     */
    public void pushNewMessage(IOMessage message , IOFabricAPIListener listener){
        if(message != null) {
            message.setPublisher(elementID);
            sendRequest(IOFabricLocalAPIURL.POST_MSG_REST_LOCAL_API, message.getJson(), listener);
        }
    }

    /**
     * Method sends request for all Container's messages for specified publishers and period.
     *
     * @param startDate - start date of period
     * @param endDate - end date of period
     * @param publishers - set of publisher's IDs
     * @param listener - listener for communication with ioFabric
     *
     */
    public void fetchMessagesByQuery(Date startDate, Date endDate,
                                                Set<String> publishers, IOFabricAPIListener listener){
        JsonObject json = Json.createObjectBuilder().add(ID_PARAM_NAME, elementID)
                .add(TIMEFRAME_START_PARAM_NAME, startDate.getTime())
                .add(TIMEFRAME_END_PARAM_NAME, endDate.getTime())
                .add(PUBLISHERS_PARAM_NAME, publishers.toString())
                .build();
        sendRequest(IOFabricLocalAPIURL.GET_MSGS_QUERY_REST_LOCAL_API, json, listener);
    }

    /**
     * Method opens a Control WebSocket connection to ioFabric in a separate thread.
     *
     * @param listener - listener for communication with ioFabric
     *
     */
    public void openControlWebSocket(IOFabricAPIListener listener){
        openWebSocketConnection(IOFabricLocalAPIURL.GET_CONTROL_WEB_SOCKET_LOCAL_API, IOFabricLocalAPIURL.GET_CONTROL_WEB_SOCKET_LOCAL_API, listener);
    }

    /**
     * Method opens a Message WebSocket connection to ioFabric in a separate thread.
     *
     * @param listener - listener for communication with ioFabric
     *
     */
    public void openMessageWebSocket(IOFabricAPIListener listener){
        openWebSocketConnection(IOFabricLocalAPIURL.GET_MSG_WEB_SOCKET_LOCAL_API, IOFabricLocalAPIURL.GET_MSG_WEB_SOCKET_LOCAL_API, listener);
    }

    /**
     * Method constructs a URL for request.
     *
     * @param url - url for request
     * @param isWS - weather url os for WS request<
     *
     * @return URI
     */
    private URI getURI(IOFabricLocalAPIURL url, boolean isWS){
        StringBuilder urlBuilder = new StringBuilder();
        String protocol = isWS ? "ws" : "http";
        urlBuilder.append(protocol);
        if (ssl) {
            urlBuilder.append("s");
        }
        urlBuilder.append("://").append(server).append(":").append(port).append(url.getURL());
        if(isWS) { urlBuilder.append(elementID); }
        try {
            return new URI(urlBuilder.toString());
        } catch (URISyntaxException e){
            log.warning("Error constructing URL for request.");
            return null;
        }
    }

    /**
     * Method checks if the host of IOFabricClient is reachable.
     *
     * @return boolean
     */
    private boolean isHostReachable(){
        try {
            return InetAddress.getByName(server).isReachable(1000);
        } catch (UnknownHostException e){
            return false;
        } catch (IOException e) {
            return false;
        }
    }


}
