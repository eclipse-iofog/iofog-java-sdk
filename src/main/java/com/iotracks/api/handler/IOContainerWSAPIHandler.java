package com.iotracks.api.handler;

import com.iotracks.api.listener.IOFabricAPIListener;
import com.iotracks.utils.IOFabricLocalAPIURL;
import com.iotracks.ws.manager.WebSocketManager;
import com.iotracks.elements.IOMessage;
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
 * @author ilaryionava
 */
public class IOContainerWSAPIHandler extends SimpleChannelInboundHandler {

    private static final Logger log = Logger.getLogger(IOContainerWSAPIHandler.class.getName());

    private WebSocketClientHandshaker handshaker;
    private ChannelPromise handshakeFuture;
    private WebSocketManager wsManager;
    private String containerId;
    private IOFabricLocalAPIURL wsType;
    private IOFabricAPIListener wsListener;

    public IOContainerWSAPIHandler(IOFabricAPIListener listener, URI uri, String containerId, IOFabricLocalAPIURL wsType){
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
                case GET_CONTROL_WEB_SOCKET_LOCAL_API:
                    wsManager.addControlContext(channelHandlerContext, containerId);
                    break;
                case GET_MSG_WEB_SOCKET_LOCAL_API:
                    wsManager.addMessageContext(channelHandlerContext, containerId);
                    break;
            }
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
