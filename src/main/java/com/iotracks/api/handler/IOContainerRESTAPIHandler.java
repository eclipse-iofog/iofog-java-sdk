package com.iotracks.api.handler;

import com.iotracks.elements.IOFabricResponseMessage;
import com.iotracks.api.listener.IOFabricRESTAPIListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpObject;


/**
 * Containers's Handler for WebSocket transmissions with ioFabric.
 *
 * Created by ilaryionava on 3/24/16.
 *
 * @author ilaryionava
 */
public class IOContainerRESTAPIHandler extends SimpleChannelInboundHandler<HttpObject> {

    private IOFabricRESTAPIListener listener;

    public IOContainerRESTAPIHandler(IOFabricRESTAPIListener listener){
        this.listener = listener;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, HttpObject msg){
        if (msg instanceof FullHttpResponse) {
            IOFabricResponseMessage responseMsg = new IOFabricResponseMessage((FullHttpResponse) msg);
            listener.onSuccess(responseMsg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        listener.onError(cause);
        super.exceptionCaught(ctx, cause);
    }
}
