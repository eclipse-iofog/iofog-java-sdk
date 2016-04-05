package com.iotracks.api;

import com.iotracks.api.client.*;
import com.iotracks.api.listener.*;
import com.iotracks.elements.IOMessage;
import com.iotracks.api.handler.*;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;

import com.iotracks.utils.WebSocketType;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.*;
import io.netty.util.internal.StringUtil;

import javax.json.Json;
import javax.json.JsonObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.Set;
import java.util.logging.Logger;

/**
 * IOContainer represents container with all methods to communicate with ioFabric (via local API).
 *
 * Created by forte on 3/23/16.
 *
 * @author ilaryionava
 */
public class IOContainer {

    private static final Logger log = Logger.getLogger(IOContainer.class.getName());

    private final String ID_PARAM_NAME = "id";
    private final String TIMEFRAME_START_PARAM_NAME = "timeframestart";
    private final String TIMEFRAME_END_PARAM_NAME = "timeframeend";
    private final String PUBLISHERS_PARAM_NAME = "publishers";

    private final String GET_CONFIG_REST_LOCAL_API = "/v2/config/get" ;
    private final String GET_NEXT_MSG_REST_LOCAL_API = "/v2/messages/next" ;
    private final String POST_MSG_REST_LOCAL_API = "/v2/messages/new" ;
    private final String GET_MSGS_QUERY_REST_LOCAL_API = "/v2/messages/query" ;
    private final String GET_CONTROL_WEB_SOCKET_LOCAL_API = "/v2/control/socket/id/" ;
    private final String GET_MSG_WEB_SOCKET_LOCAL_API = "/v2/message/socket/id/" ;

    private String server;
    private int port;
    private boolean ssl;
    private String elementID;
    private IOContainerWSAPIHandler handler;
    private WebSocketType wsType;

    /**
     * @param host - the server name or ip address (by default "router")
     * @param port - the listening port (bye default 54321)
     */
    public IOContainer(String host, int port){
        if(!StringUtil.isNullOrEmpty(host)) {
            this.server = host;
        } else {
            this.server = "iofabric";
        }
        this.port = port!=0 ? port : 54321;
        this.ssl = System.getProperty("ssl") != null;
        this.elementID = System.getProperty("SELFNAME");
    }

    /**
     * Method sends REST request to ioFabric based on parameters.
     *
     * @param url - request url
     * @param content - json representation of request's content
     * @param listener - listener for REST communication with ioFabric
     *
     */
    private void sendRequest(String url, JsonObject content, IOFabricRESTAPIListener listener){
        IOContainerRESTAPIHandler handler = new IOContainerRESTAPIHandler(listener);
        IOFabricAPIClient localAPIClient = new IOFabricAPIClient(handler, ssl);
        Channel channel = localAPIClient.connect(server, port);
        if(channel != null){
            channel.write(getRequest(url, HttpMethod.POST));
            channel.write(content);
            channel.flush();
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
    private HttpRequest getRequest(String url, HttpMethod httpMethod){
        HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, httpMethod, url);
        request.headers().set(CONTENT_TYPE, "application/json");
        return request;
    }

    /**
     * Method opens a WebSocket connection to ioFabric in a separate thread.
     *
     * @param wsType - WebSocket type for connection
     * @param url - url for request
     * @param containerId - Container's ID
     * @param listener - listener for WebSocket communication with ioFabric
     *
     */
    private void openWebSocketConnection(WebSocketType wsType, String url, String containerId, IOFabricWSAPIListener listener){
        this.wsType = wsType;
        handler = new IOContainerWSAPIHandler(listener, getURI(url, containerId), containerId, wsType);
        new Thread(new IOWebSocketConnection(handler, ssl, server, port)).start();
    }

    /**
     * Method sends IOMessage to ioFabric in case Message WebSocket connection is open.
     *
     * @param containerId - Container's ID
     * @param message - IOMessage to send
     *
     */
    public void sendMessageToWebSocket(String containerId, IOMessage message){
        if(message != null) {
            message.setPublisher(elementID);
            if(handler != null && wsType == WebSocketType.MESSAGE_WEB_SOCKET) {
                handler.sendMessage(containerId, message);
            } else {
                log.warning("Message can be sent to ioFabric only if MessageWebSocket connection is established.");
            }
        }
    }

    /**
     * Method sends request for current Container's configurations.
     *
     * @param containerId - Container's ID
     * @param listener - listener for REST communication with ioFabric
     *
     */
    public void fetchContainerConfig(String containerId, IOFabricRESTAPIListener listener){
        sendRequest(GET_CONFIG_REST_LOCAL_API, Json.createObjectBuilder().add(ID_PARAM_NAME, containerId).build(), listener);
    }

    /**
     * Method sends request for all Container's unread messages.
     *
     * @param containerId - Container's ID
     * @param listener - listener for REST communication with ioFabric
     *
     */
    public void fetchNextMessage(String containerId , IOFabricRESTAPIListener listener){
        sendRequest(GET_NEXT_MSG_REST_LOCAL_API, Json.createObjectBuilder().add(ID_PARAM_NAME, containerId).build(), listener);
    }

    /**
     * Method sends request to post Container's new IOMessage to the system.
     *
     * @param message - new IOMessage
     * @param listener - listener for REST communication with ioFabric
     *
     */
    public void pushNewMessage(IOMessage message , IOFabricRESTAPIListener listener){
        if(message != null) {
            message.setPublisher(elementID);
            sendRequest(POST_MSG_REST_LOCAL_API, message.getJson(), listener);
        }
    }

    /**
     * Method sends request for all Container's messages for specified publishers and period.
     *
     * @param containerId - Container's ID
     * @param startDate - start date of period
     * @param endDate - end date of period
     * @param publishers - set of publisher's IDs
     * @param listener - listener for REST communication with ioFabric
     *
     */
    public void fetchMessagesByQuery(String containerId, Date startDate, Date endDate,
                                                Set<String> publishers, IOFabricRESTAPIListener listener){
        JsonObject json = Json.createObjectBuilder().add(ID_PARAM_NAME, containerId)
                .add(TIMEFRAME_START_PARAM_NAME, startDate.getTime())
                .add(TIMEFRAME_END_PARAM_NAME, endDate.getTime())
                .add(PUBLISHERS_PARAM_NAME, publishers.toString())
                .build();
        sendRequest(GET_MSGS_QUERY_REST_LOCAL_API, json, listener);
    }

    /**
     * Method opens a Control WebSocket connection to ioFabric in a separate thread.
     *
     * @param containerId - Container's ID
     * @param listener - listener for WebSocket communication with ioFabric
     *
     */
    public void openControlWebSocket(String containerId, IOFabricWSAPIListener listener){
        openWebSocketConnection(WebSocketType.CONTROL_WEB_SOCKET, GET_CONTROL_WEB_SOCKET_LOCAL_API, containerId, listener);
    }

    /**
     * Method opens a Message WebSocket connection to ioFabric in a separate thread.
     *
     * @param containerId - Container's ID
     * @param listener - listener for WebSocket communication with ioFabric
     *
     */
    public void openMessageWebSocket(String containerId, IOFabricWSAPIListener listener){
        openWebSocketConnection(WebSocketType.MESSAGE_WEB_SOCKET, GET_MSG_WEB_SOCKET_LOCAL_API, containerId, listener);
    }

    /**
     * Method constructs a URL for request.
     *
     * @param url - url for request
     * @param containerId - Container's ID
     *
     * @return URI
     */
    private URI getURI(String url, String containerId){
        StringBuilder urlBuilder = new StringBuilder();
        if (ssl) {
            urlBuilder.append("wss://");
        } else {
            urlBuilder.append("ws://");
        }
        urlBuilder.append(server).append(":").append(port).append(url).append(containerId);
        try {
            return new URI(urlBuilder.toString());
        } catch (URISyntaxException e){
            log.warning("Error constructing URL for request.");
            return null;
        }
    }

}
