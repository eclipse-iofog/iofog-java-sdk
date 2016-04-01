package com.iotracks.api.handler;

import com.iotracks.utils.WebSocketType;
import com.iotracks.ws.manager.WebSocketManager;
import com.iotracks.elements.IOMessage;
import com.iotracks.api.listener.IOFabricWSAPIListener;
import com.iotracks.ws.manager.listener.ClientWSManagerListener;
import io.netty.channel.*;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.*;

import java.net.URI;
import java.util.logging.Logger;

/**
 * Containers's Handler for WebSocket transmissions with ioFabric.
 *
 * Created by forte on 3/28/16.
 *
 * @author ilaryionava
 */
public class IOContainerWSAPIHandler extends SimpleChannelInboundHandler {

    private static final Logger log = Logger.getLogger(IOContainerWSAPIHandler.class.getName());

    private WebSocketClientHandshaker handshaker;
    private ChannelPromise handshakeFuture;
    private WebSocketManager wsManager;
    private String containerId;
    private WebSocketType wsType;
    private IOFabricWSAPIListener wsListener;

    public IOContainerWSAPIHandler(IOFabricWSAPIListener listener, URI uri, String containerId, WebSocketType wsType){
        this.handshaker = WebSocketClientHandshakerFactory.newHandshaker(uri, WebSocketVersion.V13, null, false, new DefaultHttpHeaders());
        this.containerId = containerId;
        this.wsType = wsType;
        wsManager = new WebSocketManager(new ClientWSManagerListener(listener, wsType));
        wsListener = listener;
    }

    public ChannelFuture handshakeFuture() {
        return handshakeFuture;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        handshakeFuture = ctx.newPromise();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) { handshaker.handshake(ctx.channel()); }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        wsManager.closeSocket(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object o){
        Channel ch = channelHandlerContext.channel();
        if (!handshaker.isHandshakeComplete()) {
            handshaker.finishHandshake(ch, (FullHttpResponse) o);
            handshakeFuture.setSuccess();
            switch (wsType){
                case CONTROL_WEB_SOCKET:
                    wsManager.addControlContext(channelHandlerContext, containerId);
                    break;
                case MESSAGE_WEB_SOCKET:
                    wsManager.addMessageContext(channelHandlerContext, containerId);
                    break;
            }
            log.info("Handshake completed. " + wsType.toString() + " established.");
        }
        if (o instanceof WebSocketFrame ){
            wsManager.eatFrame(channelHandlerContext, (WebSocketFrame) o);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        wsListener.onError(cause);
        cause.printStackTrace();
        if (!handshakeFuture.isDone()) {
            handshakeFuture.setFailure(cause);
        }
        ctx.close();
        wsManager.closeSocket(ctx);
    }

    public void sendMessage(String containerId, IOMessage message){
        wsManager.sendMessage(containerId, message.getBytes());
    }

}
