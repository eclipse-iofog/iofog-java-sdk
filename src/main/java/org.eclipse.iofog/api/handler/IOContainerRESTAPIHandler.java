package org.eclipse.iofog.api.handler;

import org.eclipse.iofog.api.listener.IOFogAPIListener;
import org.eclipse.iofog.elements.IOMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpResponseStatus;

import javax.json.*;
import java.io.StringReader;
import java.util.List;

import static org.eclipse.iofog.utils.IOFogResponseUtils.*;
import static java.util.stream.Collectors.toList;


/**
 * Containers's Handler for WebSocket transmissions with ioFog.
 *
 * @author ilaryionava
 */
public class IOContainerRESTAPIHandler extends SimpleChannelInboundHandler<HttpObject> {

    private IOFogAPIListener listener;

    public IOContainerRESTAPIHandler(IOFogAPIListener listener){
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
            if (response.getStatus() == HttpResponseStatus.BAD_REQUEST) {
                handleBadRequest(json, channelHandlerContext);
            } else {
                if (isNewConfig(json)) {
                    handleNewConfig(json, channelHandlerContext);
                } else if (isNewMessage(json)) {
                    handleNewMessage(json, channelHandlerContext);
                } else if (isMessageReceipt(json)) {
                    handleMessageReceipt(json, channelHandlerContext);
                }
            }
        }
    }

    private void handleNewConfig(JsonObject json, ChannelHandlerContext channelHandlerContext) {
        JsonString configString = json.getJsonString(CONFIG_FIELD_NAME);
        JsonReader configReader = Json.createReader(new StringReader(configString.getString()));
        listener.onNewConfig(configReader.readObject());
        channelHandlerContext.close();
    }

    private void handleNewMessage(JsonObject json, ChannelHandlerContext channelHandlerContext) {
        JsonArray messagesJSON = json.getJsonArray(MESSAGES_FIELD_NAME);

        List<IOMessage> messagesList = messagesJSON.stream()
                .filter(message -> message instanceof JsonObject)
                .map(message -> new IOMessage((JsonObject) message, true))
                .collect(toList());

        if(isMessageQuery(json)) {
            listener.onMessagesQuery(Long.valueOf(json.getJsonNumber(TIMEFRAME_START_FIELD_NAME).toString()),
                    Long.valueOf(json.getJsonNumber(TIMEFRAME_END_FIELD_NAME).toString()),
                    messagesList);
        } else {
            listener.onMessages(messagesList);
        }
        channelHandlerContext.close();
    }

    private void handleBadRequest(JsonObject json, ChannelHandlerContext channelHandlerContext) {
        listener.onBadRequest(json.toString());
        channelHandlerContext.close();
    }

    private void handleMessageReceipt(JsonObject json, ChannelHandlerContext channelHandlerContext) {
        listener.onMessageReceipt(json.getString(ID_FIELD_NAME),
                Long.valueOf(json.getString(TIMESTAMP_FIELD_NAME)));
        channelHandlerContext.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        listener.onError(cause);
        super.exceptionCaught(ctx, cause);
    }
}
