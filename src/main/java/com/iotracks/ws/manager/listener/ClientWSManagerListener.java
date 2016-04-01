package com.iotracks.ws.manager.listener;

import com.iotracks.api.listener.IOFabricWSAPIListener;
import com.iotracks.elements.IOMessage;
import com.iotracks.utils.ByteUtils;
import com.iotracks.utils.WebSocketType;
import com.iotracks.ws.manager.WebSocketManager;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;

import java.util.Arrays;
import java.util.logging.Logger;

/**
 * Implementation of {@link WebSocketManagerListener}.
 * According to specification handles next transmissions' codes:
 * 1. If Control WebSocket Connection is handles ->
 *    - In case of receiving NEW_CONFIGURATION_SIGNAL from ioFabric, Container responds with ACKNOWLEDGE response.
 * 2. If Message WebSocket Connection is handled ->
 *    - In case of receiving MESSAGE from ioFabric, Container responds with ACKNOWLEDGE response.
 *    - In case of receiving MESSAGE_RECEIPT from ioFabric, Container responds with ACKNOWLEDGE response.
 *
 * Created by forte on 3/30/16.
 *
 * @author ilaryionava
 */
public class ClientWSManagerListener implements WebSocketManagerListener {

    private static final Logger log = Logger.getLogger(ClientWSManagerListener.class.getName());

    private IOFabricWSAPIListener wsListener;
    private WebSocketType wsType;

    public ClientWSManagerListener(IOFabricWSAPIListener listener, WebSocketType wsType){
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
            if (opcode == WebSocketManager.OPCODE_CONTROL_SIGNAL.intValue() && wsType == WebSocketType.CONTROL_WEB_SOCKET
                /*&& byteArray.length == 1*/) {
                log.info("RECEIVED CONTROL SIGNAL from ioFabric: new Configuration is available. Sending ACKNOWLEDGE response");
                wsManager.sendAck(ctx);
            } else if (opcode == WebSocketManager.OPCODE_MSG.intValue() && wsType == WebSocketType.MESSAGE_WEB_SOCKET) {
                int totalMsgLength = ByteUtils.bytesToInteger(Arrays.copyOfRange(byteArray, 1, 5));
                IOMessage message = new IOMessage(Arrays.copyOfRange(byteArray, 5, totalMsgLength));
                // TODO: wsListener on Message Received ???
                log.info("RECEIVED MESSAGE from ioFabric: sending ACKNOWLEDGE response.");
                wsManager.sendAck(ctx);
            } else if (opcode == WebSocketManager.OPCODE_RECEIPT.intValue() && wsType == WebSocketType.MESSAGE_WEB_SOCKET) {
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
                wsListener.onSuccess(message);
                log.info("RECEIVED MESSAGE RECEIPT from ioFabric: sending ACKNOWLEDGE response.");
                wsManager.sendAck(ctx);
            }
        }
    }

}
