package com.iotracks.elements;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * IOFabricResponseMessage represents response from ioFabric REST calls.
 *
 * Created by forte on 3/23/16.
 *
 * @author ilaryionava
 */
public class IOFabricResponseMessage {

    private final String ID_FIELD_NAME = "id";
    private final String STATUS_FIELD_NAME = "status";
    private final String COUNT_FIELD_NAME = "count";
    private final String CONFIG_FIELD_NAME = "config";
    private final String MESSAGES_FIELD_NAME = "messages";
    private final String TIMESTAMP_FIELD_NAME = "timestamp";

    private String id;
    private String status;
    private Integer count;
    private long timestamp;
    private Map<String, String> config;
    private List<IOMessage> messages;
    private HttpResponseStatus httpStatus;

    public IOFabricResponseMessage(FullHttpResponse httpResponse) {
        ByteBuf content = httpResponse.content();
        String responseBody = content.toString(io.netty.util.CharsetUtil.US_ASCII);
        JsonReader reader = Json.createReader(new StringReader(responseBody));
        JsonObject jsonObject = reader.readObject();
        init(jsonObject);
        setHttpStatus(httpResponse.getStatus());
    }

    private void init(JsonObject json){
        if (json.containsKey(ID_FIELD_NAME)){ setId(json.getString(ID_FIELD_NAME)); }
        if (json.containsKey(STATUS_FIELD_NAME)){ setStatus(json.getString(STATUS_FIELD_NAME)); }
        if (json.containsKey(COUNT_FIELD_NAME)){ setCount(json.getInt(COUNT_FIELD_NAME)); }
        if (json.containsKey(TIMESTAMP_FIELD_NAME)){ setTimestamp(json.getJsonNumber(TIMESTAMP_FIELD_NAME).longValue()); }
        if (json.containsKey(CONFIG_FIELD_NAME)){
            JsonObject configJSON = json.getJsonObject(CONFIG_FIELD_NAME);
            Map<String, String> configMap = new HashMap<>();
            configJSON.keySet().forEach(key -> configMap.put(key, configJSON.getString(key)));
            setConfig(configMap);
        }
        if (json.containsKey(MESSAGES_FIELD_NAME)){
            JsonArray messagesJSON = json.getJsonArray(MESSAGES_FIELD_NAME);
            List<IOMessage> messagesList = new ArrayList<>(messagesJSON.size());
            messagesJSON.forEach(message -> {
                if(message instanceof JsonObject) {
                    messagesList.add(new IOMessage((JsonObject) message));
                }
            });
            setMessages(messagesList);
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, String> getConfig() {
        return config;
    }

    public void setConfig(Map<String, String> config) {
        this.config = config;
    }

    public List<IOMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<IOMessage> messages) {
        this.messages = messages;
    }

    public HttpResponseStatus getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(HttpResponseStatus httpStatus) {
        this.httpStatus = httpStatus;
    }
}
