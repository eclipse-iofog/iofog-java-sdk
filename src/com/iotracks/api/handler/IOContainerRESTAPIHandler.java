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

import javax.json.*;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;


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
                listener.onBadRequest(json.toString());
                channelHandlerContext.close();
            } else {
                if (json.containsKey(IOFabricResponseUtils.CONFIG_FIELD_NAME)) {
                    JsonString configString = json.getJsonString(IOFabricResponseUtils.CONFIG_FIELD_NAME);
                    JsonReader configReader = Json.createReader(new StringReader(configString.getString()));
                    listener.onNewConfig(configReader.readObject());
                    channelHandlerContext.close();
                    return;
                }
                if (json.containsKey(IOFabricResponseUtils.MESSAGES_FIELD_NAME)) {
                    JsonArray messagesJSON = json.getJsonArray(IOFabricResponseUtils.MESSAGES_FIELD_NAME);
                    List<IOMessage> messagesList = new ArrayList<>(messagesJSON.size());
                    messagesJSON.forEach(message -> {
                        if (message instanceof JsonObject) {
                            messagesList.add(new IOMessage((JsonObject) message));
                        }
                    });
                    if(json.containsKey(IOFabricResponseUtils.TIMEFRAME_START_FIELD_NAME) &&
                            json.containsKey(IOFabricResponseUtils.TIMEFRAME_END_FIELD_NAME)) {
                        listener.onMessagesQuery(Long.valueOf(json.getJsonNumber(IOFabricResponseUtils.TIMEFRAME_START_FIELD_NAME).toString()),
                                Long.valueOf(json.getJsonNumber(IOFabricResponseUtils.TIMEFRAME_END_FIELD_NAME).toString()),
                                messagesList);
                    } else {
                        listener.onMessages(messagesList);
                    }
                    channelHandlerContext.close();
                    return;
                }
                if(json.containsKey(IOFabricResponseUtils.ID_FIELD_NAME) && json.containsKey(IOFabricResponseUtils.TIMESTAMP_FIELD_NAME)) {
                    listener.onMessageReceipt(json.getString(IOFabricResponseUtils.ID_FIELD_NAME), Long.valueOf(json.getString(IOFabricResponseUtils.TIMESTAMP_FIELD_NAME)));
                    channelHandlerContext.close();
                    return;
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
