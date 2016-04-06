package com.iotracks.api.client;

import com.iotracks.api.handler.*;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import javax.net.ssl.SSLException;
import java.net.InetSocketAddress;
import java.util.logging.Logger;

/**
 * Client to establish connection to ioFabric API.
 *
 * @author ilaryionava
 */
public class IOFabricAPIConnector {

    private static final Logger log = Logger.getLogger(IOFabricAPIConnector.class.getName());

    protected Bootstrap bootstrap;
    protected EventLoopGroup workerGroup;

    private Bootstrap init(){
        workerGroup = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(workerGroup);
        bootstrap.channel(NioSocketChannel.class);
        return bootstrap;
    }

    /**
     * Creates a new IOFabricAPIConnector for REST calls.
     * @param handler - instance of {@link IOContainerRESTAPIHandler}
     * @param ssl
     */
    public IOFabricAPIConnector(IOContainerRESTAPIHandler handler, boolean ssl){
        bootstrap = init();
        bootstrap.handler(new ChannelInitializer() {
            @Override
            protected void initChannel(Channel channel){
                addSSLHandler(ssl, channel);
                channel.pipeline().addLast(handler);
            }
        });
    }

    /**
     * Creates a new IOFabricAPIConnector for WebSocket transmissions.
     * @param handler - instance of {@link IOContainerWSAPIHandler}
     * @param ssl - indicates if connection should be established through secured protocol
     */
    public IOFabricAPIConnector(IOContainerWSAPIHandler handler, boolean ssl){
        bootstrap = init();
        bootstrap.handler(new ChannelInitializer() {
            @Override
            protected void initChannel(Channel channel){
                addSSLHandler(ssl, channel);
                channel.pipeline().addLast(
                        new HttpClientCodec(),
                        new HttpObjectAggregator(8192),
                        handler);
            }
        });
    }

    /**
     * Returns a channel bound to the specified server
     *
     * @param server - the server name or ip address
     * @param port - the listening port
     *
     * @return a channel bound to the specified server
     */
    public Channel initConnection(String server, int port)  {
        InetSocketAddress socketAddress = new InetSocketAddress(server, port);
        try {
            return bootstrap.connect(socketAddress).sync().channel();
        } catch (InterruptedException e) {
            log.warning("Error connection to specified address : " + socketAddress.toString());
            return null;
        }
    }

    private void addSSLHandler(boolean ssl, Channel channel) {
        if(ssl) {
           try {
               /* SelfSignedCertificate ssc = new SelfSignedCertificate();
                SslContext sslCtx =  SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();*/
                SslContext sslCtx = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
                channel.pipeline().addLast(sslCtx.newHandler(channel.alloc()));
           } catch (SSLException e) {
               log.warning("Error building SSL context.");
           }
        }
    }

    /**
     * Shuts down connection.
     */
    public void destroyConnection(){
        workerGroup.shutdownGracefully();
    }

}