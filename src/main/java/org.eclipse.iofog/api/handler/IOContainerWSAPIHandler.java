package org.eclipse.iofog.api.handler;

import io.netty.channel.*;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.*;
import org.eclipse.iofog.api.IOFogClient;
import org.eclipse.iofog.api.listener.IOFogAPIListener;
import org.eclipse.iofog.elements.IOMessage;
import org.eclipse.iofog.utils.IOFogLocalAPIURL;
import org.eclipse.iofog.ws.manager.WebSocketManager;
import org.eclipse.iofog.ws.manager.listener.ClientWSManagerListener;

import java.net.URI;
import java.util.logging.Logger;

/**
 * Containers's Handler for WebSocket transmissions with ioFog.
 *
 * @author ilaryionava
 */
public class IOContainerWSAPIHandler extends SimpleChannelInboundHandler {

    private static final Logger log = Logger.getLogger(IOContainerWSAPIHandler.class.getName());

    private WebSocketClientHandshaker handshaker;
    private ChannelPromise handshakeFuture;
    private WebSocketManager wsManager;
    private String containerId;
    private IOFogLocalAPIURL wsType;
    private IOFogAPIListener wsListener;
    private IOFogClient ioFogClient;

    public IOContainerWSAPIHandler(IOFogAPIListener wsListener,
                                   URI uri, String containerId,
                                   IOFogLocalAPIURL wsType,
                                   IOFogClient ioFogClient) {
        this.handshaker = WebSocketClientHandshakerFactory.newHandshaker(uri, WebSocketVersion.V13, null,
                false, new DefaultHttpHeaders(), Integer.MAX_VALUE);
        this.containerId = containerId;
        this.wsType = wsType;
        this.wsManager = new WebSocketManager(new ClientWSManagerListener(wsListener, wsType));
        this.wsListener = wsListener;
        this.ioFogClient = ioFogClient;
    }

    public ChannelFuture handshakeFuture() {
        return handshakeFuture;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        handshakeFuture = ctx.newPromise();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        handshaker.handshake(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        ioFogClient.reconnect(wsType, wsListener);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object o){
        Channel ch = channelHandlerContext.channel();
        if (!handshaker.isHandshakeComplete()) {
            handshaker.finishHandshake(ch, (FullHttpResponse) o);
            handshakeFuture.setSuccess();
            switch (wsType){
                case GET_CONTROL_WEB_SOCKET_LOCAL_API:
                    ioFogClient.wsReconnectControlSocketAttempts = 0;
                    wsManager.addControlContext(channelHandlerContext, containerId);
                    break;
                case GET_MSG_WEB_SOCKET_LOCAL_API:
                    ioFogClient.wsReconnectMessageSocketAttempts = 0;
                    wsManager.addMessageContext(channelHandlerContext, containerId);
                    break;
            }
        }
        if (o instanceof WebSocketFrame) {
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
