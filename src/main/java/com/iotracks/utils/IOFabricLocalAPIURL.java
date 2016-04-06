package com.iotracks.utils;

/**
 * Enum for all ioFabric's Local API requests URLs
 */
public enum IOFabricLocalAPIURL {

    GET_CONFIG_REST_LOCAL_API ("/v2/config/get") ,
    GET_NEXT_MSG_REST_LOCAL_API ("/v2/messages/next"),
    POST_MSG_REST_LOCAL_API ("/v2/messages/new"),
    GET_MSGS_QUERY_REST_LOCAL_API ("/v2/messages/query"),
    GET_CONTROL_WEB_SOCKET_LOCAL_API ("/v2/control/socket/id/"),
    GET_MSG_WEB_SOCKET_LOCAL_API ("/v2/message/socket/id/");

    private String url;

    IOFabricLocalAPIURL(String url){
       this.url = url;
    }

    public String getURL(){
        return this.url;
    }

}
