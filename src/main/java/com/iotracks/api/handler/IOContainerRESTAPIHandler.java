package com.iotracks.api.handler;

import com.iotracks.api.listener.IOFabricAPIListener;
import com.iotracks.elements.IOMessage;
import com.iotracks.utils.IOFabricResponseUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpObject;
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
 * Containers's Handler for WebSocket transmissions with ioFabric.
 *
 * @author ilaryionava
 */
public class IOContainerRESTAPIHandler extends SimpleChannelInboundHandler<HttpObject> {

    private IOFabricAPIListener listener;

    public IOContainerRESTAPIHandler(IOFabricAPIListener listener){
        this.listener = listener;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, HttpObject msg){
        if (msg instanceof FullHttpResponse) {
            FullHttpResponse response = (FullHttpResponse) msg;
            ByteBuf content = response.content();
            String responseBody = content.toString(io.netty.util.CharsetUtil.US_ASCII);
            JsonReader reader = Json.createReader(new StringReader(responseBody));
            JsonObject json = reader.readObject();
            if(response.getStatus() == HttpResponseStatus.BAD_REQUEST) {
                listener.onError(json.toString());
            } else {
                if (json.containsKey(IOFabricResponseUtils.CONFIG_FIELD_NAME)) {
                    JsonObject configJSON = json.getJsonObject(IOFabricResponseUtils.CONFIG_FIELD_NAME);
                    Map<String, String> configMap = new HashMap<>();
                    configJSON.keySet().forEach(key -> configMap.put(key, configJSON.getString(key)));
                    listener.onNewConfig(configMap);
                }
                if (json.containsKey(IOFabricResponseUtils.MESSAGES_FIELD_NAME)) {
                    JsonArray messagesJSON = json.getJsonArray(IOFabricResponseUtils.MESSAGES_FIELD_NAME);
                    List<IOMessage> messagesList = new ArrayList<>(messagesJSON.size());
                    messagesJSON.forEach(message -> {
                        if (message instanceof JsonObject) {
                            messagesList.add(new IOMessage((JsonObject) message));
                        }
                    });
                    listener.onMessages(messagesList);
                }
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        listener.onError(cause);
        super.exceptionCaught(ctx, cause);
    }
}
