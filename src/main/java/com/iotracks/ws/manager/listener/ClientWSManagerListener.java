package com.iotracks.ws.manager.listener;

import com.iotracks.api.listener.IOFogAPIListener;
import com.iotracks.elements.IOMessage;
import com.iotracks.utils.ByteUtils;
import com.iotracks.utils.IOFogLocalAPIURL;
import com.iotracks.ws.manager.WebSocketManager;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;

import java.util.Arrays;
import java.util.Collections;
import java.util.logging.Logger;

/**
 * Implementation of {@link WebSocketManagerListener}.
 * According to specification handles next transmissions' codes:
 * 1. If Control WebSocket Connection is handles ->
 *    - In case of receiving NEW_CONFIGURATION_SIGNAL from ioFog, Container responds with ACKNOWLEDGE response.
 * 2. If Message WebSocket Connection is handled ->
 *    - In case of receiving MESSAGE from ioFog, Container responds with ACKNOWLEDGE response.
 *    - In case of receiving MESSAGE_RECEIPT from ioFog, Container responds with ACKNOWLEDGE response.
 *
 * @author ilaryionava
 */
public class ClientWSManagerListener implements WebSocketManagerListener {

    private static final Logger log = Logger.getLogger(ClientWSManagerListener.class.getName());

    private IOFogAPIListener wsListener;
    private IOFogLocalAPIURL wsType;

    public ClientWSManagerListener(IOFogAPIListener listener, IOFogLocalAPIURL wsType){
        this.wsListener = listener;
        this.wsType = wsType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handle(WebSocketManager wsManager, BinaryWebSocketFrame frame, ChannelHandlerContext ctx) {
        ByteBuf content = frame.content();
        if(content.isReadable()){
            byte[] byteArray = new byte[content.readableBytes()];
            int readerIndex = content.readerIndex();
            content.getBytes(readerIndex, byteArray);
            byte opcode = byteArray[0];
            if (opcode == WebSocketManager.OPCODE_CONTROL_SIGNAL.intValue() && wsType == IOFogLocalAPIURL.GET_CONTROL_WEB_SOCKET_LOCAL_API
                /*&& byteArray.length == 1*/) {
                wsListener.onNewConfigSignal();
                wsManager.sendAck(ctx);
            } else if (opcode == WebSocketManager.OPCODE_MSG.intValue() && wsType == IOFogLocalAPIURL.GET_MSG_WEB_SOCKET_LOCAL_API) {
                int totalMsgLength = ByteUtils.bytesToInteger(Arrays.copyOfRange(byteArray, 1, 5));
                int msgLength = totalMsgLength + 5;
                IOMessage message = new IOMessage(Arrays.copyOfRange(byteArray, 5, msgLength));
                wsListener.onMessages(Collections.singletonList(message));
                wsManager.sendAck(ctx);
            } else if (opcode == WebSocketManager.OPCODE_RECEIPT.intValue() && wsType == IOFogLocalAPIURL.GET_MSG_WEB_SOCKET_LOCAL_API) {
                int size = byteArray[1];
                int pos = 3;
                String messageId = "";
                if (size > 0) {
                    messageId = ByteUtils.bytesToString(Arrays.copyOfRange(byteArray, pos, pos + size));
                    pos += size;
                }
                size = byteArray[2];
                long timestamp = 0L;
                if (size > 0) {
                    timestamp = ByteUtils.bytesToLong(Arrays.copyOfRange(byteArray, pos, pos + size));
                }
                IOMessage message = new IOMessage(wsManager.getMessage(ctx));
                message.setId(messageId);
                message.setTimestamp(timestamp);
                wsListener.onMessageReceipt(messageId, timestamp);
                wsManager.sendAck(ctx);
            }
        }
    }

}
