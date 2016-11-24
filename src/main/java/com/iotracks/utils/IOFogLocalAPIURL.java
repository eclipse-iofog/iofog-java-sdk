package com.iotracks.utils;

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
