package com.iotracks.ws.manager;

import com.iotracks.utils.ByteUtils;
import com.iotracks.ws.manager.listener.WebSocketManagerListener;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static io.netty.handler.codec.http.HttpHeaders.Names.HOST;


/**
 * Manager for all WebSocket communications between Container and ioFabric.
 */
public class WebSocketManager {

    public static final Byte OPCODE_PING = 0x9;
    public static final Byte OPCODE_PONG = 0xA;
    public static final Byte OPCODE_ACK = 0xB;
    public static final Byte OPCODE_CONTROL_SIGNAL = 0xC;
    public static final Byte OPCODE_MSG = 0xD;
    public static final Byte OPCODE_RECEIPT = 0xE; //TODO

    private Map<String, ChannelHandlerContext> mControlWebsocketMap;
    private Map<String, ChannelHandlerContext> mMessageWebsocketMap;
    private Map<ChannelHandlerContext, AckMarker> mMessageSendContextMap;
    private Map<ChannelHandlerContext, Integer> mControlSignalSendContextMap;
    private Set<ChannelHandlerContext> mPingSendMap;

    private WebSocketManagerListener wsListener;

    private ScheduledExecutorService mScheduler;

    public WebSocketManager(WebSocketManagerListener wsListener){
        mControlWebsocketMap = new ConcurrentHashMap<>();
        mMessageWebsocketMap = new ConcurrentHashMap<>();
        mMessageSendContextMap = new ConcurrentHashMap<>();
        mControlSignalSendContextMap = new ConcurrentHashMap<>();
        mPingSendMap = Collections.synchronizedSet(new HashSet<>());

        mScheduler = Executors.newScheduledThreadPool(4);
        mScheduler.scheduleWithFixedDelay(new MessageWatcher(mMessageSendContextMap, this), 0, 5, TimeUnit.SECONDS);
        mScheduler.scheduleWithFixedDelay(new ControlWatcher(mControlSignalSendContextMap, this), 0, 5, TimeUnit.SECONDS);
        mScheduler.scheduleWithFixedDelay(new PingWatcher(mPingSendMap, mControlWebsocketMap, this), 0, 10, TimeUnit.SECONDS);
        mScheduler.scheduleWithFixedDelay(new PingWatcher(mPingSendMap, mMessageWebsocketMap, this), 0, 10, TimeUnit.SECONDS);
        this.wsListener = wsListener;
    }

    public void sendMessage(String publisherId, byte[] pData){
        ChannelHandlerContext ctx = mMessageWebsocketMap.get(publisherId);
        if (ctx != null){
            sendMessage(ctx, pData);
        }else{
            new IllegalArgumentException("Context not found.");
        }
    }

    public void sendMessage(ChannelHandlerContext pCtx, byte[] pData){
        byte[] header = new byte[]{OPCODE_MSG, (byte)pData.length};
        byte[] msg = new byte[header.length + pData.length];
        int i = 0;
        for(byte b : header){
            msg[i] = b;
            i++;
        }
        for(byte b : pData){
            msg[i] = b;
            i++;
        }
        sendBinaryFrame(pCtx, msg);

        AckMarker marker = mMessageSendContextMap.get(pCtx);
        if(marker == null){
            marker = new AckMarker(10, pData);
            mMessageSendContextMap.put(pCtx, marker);
        }
    }

    public void sendControl(String publisherId){
        ChannelHandlerContext ctx = mControlWebsocketMap.get(publisherId);
        if (ctx != null){
            sendControl(ctx);
        }
        else{
            new IllegalArgumentException("Context not found.");
        }
    }

    public void sendControl(ChannelHandlerContext pCtx){
        //sendBinaryFrame(pCtx, new byte[]{OPCODE_CONTROL_SIGNAL});
        ByteBuf buffer1 = pCtx.alloc().buffer();
        buffer1.writeByte(OPCODE_ACK);
        buffer1.writeByte(Byte.SIZE);
        pCtx.channel().writeAndFlush(new BinaryWebSocketFrame(buffer1));
        Integer cnt = mControlSignalSendContextMap.get(pCtx);
        if(cnt == null){
            cnt = 10;
            mControlSignalSendContextMap.put(pCtx, 10);
        }
    }

    public void sendReceipt(ChannelHandlerContext pCtx, String pMessageId, Long pMessageTimestamp){
        ByteBuf buffer1 = pCtx.alloc().buffer();
        buffer1.writeByte(OPCODE_RECEIPT.intValue());
        //send Length
        int msgIdLength = pMessageId.length();
        buffer1.writeByte(msgIdLength);
        buffer1.writeByte(Long.BYTES);

        //Send opcode, id and timestamp
        buffer1.writeBytes(pMessageId.getBytes());
        buffer1.writeBytes(ByteUtils.longToBytes(pMessageTimestamp));
        pCtx.channel().writeAndFlush(new BinaryWebSocketFrame(buffer1));
    }

    public void sendAck(ChannelHandlerContext pCtx) {
        ByteBuf buffer1 = pCtx.alloc().buffer();
        buffer1.writeByte(OPCODE_ACK);
        pCtx.channel().writeAndFlush(new BinaryWebSocketFrame(buffer1));
    }

    public void sendPing(ChannelHandlerContext pCtx){
        ByteBuf buffer1 = pCtx.alloc().buffer();
        buffer1.writeByte(OPCODE_PING.intValue());
        pCtx.channel().writeAndFlush(new PingWebSocketFrame(buffer1));
    }

    private void sendBinaryFrame(ChannelHandlerContext pCtx, byte[] pData){
        if(!isCtxActual(pCtx)){
            new IllegalArgumentException("Context not found.");
        }
        ByteBuf buffer1 = pCtx.alloc().buffer();
        buffer1.writeBytes(pData);
        pCtx.channel().writeAndFlush(new BinaryWebSocketFrame(buffer1));
    }

    public void sendFrame(ChannelHandlerContext pCtx,WebSocketFrame pFrame){
        pCtx.channel().writeAndFlush(pFrame);
    }

    public void closeSocket(ChannelHandlerContext pCtx){
        sendFrame(pCtx, new CloseWebSocketFrame());
        pCtx.channel().close();
        invalidateCtx(pCtx);
    }

    public void eatFrame(ChannelHandlerContext pCtx, WebSocketFrame pFrame){
        boolean handled = false;
        if(!handled){
            handled = handleClose(pCtx, pFrame);
        }
        if(!handled){
            handled = handlePing(pCtx, pFrame);
        }
        if(!handled){
            handled = handleAck(pCtx, pFrame);
        }
        if(!handled){
            handled = handlePong(pCtx, pFrame);
        }
        if(!handled) {
            handleData(pCtx, pFrame);
        }
    }

    private void handleData(ChannelHandlerContext pCtx, WebSocketFrame pFrame){
        if (pFrame instanceof BinaryWebSocketFrame) {
            wsListener.handle(this, (BinaryWebSocketFrame)pFrame, pCtx);
        }
    }


    private boolean handleAck(ChannelHandlerContext pCtx, WebSocketFrame pFrame){
        if (pFrame instanceof BinaryWebSocketFrame) {
            ByteBuf buffer2 = pFrame.content();
            if (buffer2.readableBytes() == 1) {
                Byte opcode = buffer2.readByte();
                if(opcode == OPCODE_ACK.intValue()){
                    invalidateAck(pCtx);
                    return true;
                }
            }
        }
        return false;
    }

    private boolean handleClose(ChannelHandlerContext pCtx, WebSocketFrame pFrame){
        if (pFrame instanceof CloseWebSocketFrame) {
            pCtx.channel().close();
            invalidateCtx(pCtx);
            return true;
        }
        return false;
    }

    private boolean handlePong(ChannelHandlerContext pCtx, WebSocketFrame pFrame){
        if (pFrame instanceof PongWebSocketFrame) {
            ByteBuf buffer = pFrame.content();
            if (buffer.readableBytes() == 1) {
                Byte opcode = buffer.readByte();
                if (opcode == OPCODE_PONG.intValue()) {
                    if (isCtxActual(pCtx)) {
                        mPingSendMap.remove(pCtx);
                        return true;
                    }
                }
            }
        }
        return false;
    }
    private boolean handlePing(ChannelHandlerContext pCtx, WebSocketFrame pFrame) {
        if (pFrame instanceof PingWebSocketFrame) {
            ByteBuf buffer = pFrame.content();
            if (buffer.readableBytes() == 1) {
                Byte opcode = buffer.readByte();
                if (opcode == OPCODE_PING.intValue()) {
                    if (isCtxActual(pCtx)) {
                        ByteBuf buffer1 = pCtx.alloc().buffer();
                        buffer1.writeByte(OPCODE_PONG.intValue());
                        sendFrame(pCtx, new PongWebSocketFrame(buffer1));
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void invalidateAck(ChannelHandlerContext pCtx){
        mControlWebsocketMap.remove(pCtx);
        mMessageSendContextMap.remove(pCtx);
    }

    private void invalidateCtx(ChannelHandlerContext pCtx){
        removeCtxFromMap(pCtx, mControlWebsocketMap);
        removeCtxFromMap(pCtx, mMessageWebsocketMap);
    }

    private static void removeCtxFromMap(ChannelHandlerContext pCtx, Map<String, ChannelHandlerContext> pCtxMap){

        for(String id : pCtxMap.keySet()){
            ChannelHandlerContext curCtx = pCtxMap.get(id);
            if(curCtx.equals(pCtxMap)){
                pCtxMap.remove(id);
            }
        }
    }

    private boolean isCtxActual(ChannelHandlerContext pCtx){
        return isControlCtx(pCtx) || isMessageCtx(pCtx);
    }

    private boolean isControlCtx(ChannelHandlerContext pCtx){
        return isCtxInMap(pCtx, mControlWebsocketMap);
    }

    private boolean isMessageCtx(ChannelHandlerContext pCtx){
        return isCtxInMap(pCtx, mMessageWebsocketMap);
    }

    private static boolean isCtxInMap(ChannelHandlerContext pCtx, Map<String, ChannelHandlerContext> pCtxMap){
        return pCtxMap.values().contains(pCtx);
    }

    public void initMessageSocket(ChannelHandlerContext pCtx, String pContainerId, boolean pSsl, String pUrl, FullHttpRequest pReq){
        initSocket(pCtx, pContainerId, pSsl, pUrl, pReq, mMessageWebsocketMap);
    }

    public void initControlSocket(ChannelHandlerContext pCtx, String pContainerId, boolean pSsl, String pUrl, FullHttpRequest pReq){
        initSocket(pCtx, pContainerId, pSsl, pUrl, pReq, mControlWebsocketMap);
    }

    public void addControlContext(ChannelHandlerContext pCtx, String containerId) {
        mControlWebsocketMap.put(containerId, pCtx);
    }

    public void addMessageContext(ChannelHandlerContext pCtx, String containerId) {
        mMessageWebsocketMap.put(containerId, pCtx);
    }

    public byte[] getMessage(ChannelHandlerContext pCtx) {
        return mMessageSendContextMap.get(pCtx).getData();
    }

    private static void initSocket(ChannelHandlerContext pCtx, String pContainerId, boolean pSsl, String pUrl, FullHttpRequest pReq, Map<String, ChannelHandlerContext> pSocketMap){
        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(buildWebSocketLocation(pSsl, pUrl, pReq), null, true);
        WebSocketServerHandshaker handshaker = wsFactory.newHandshaker(pReq);
        if (handshaker == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(pCtx.channel());
        } else {
            handshaker.handshake(pCtx.channel(), pReq);
        }
        pSocketMap.put(pContainerId, pCtx);
    }



    private static String buildWebSocketLocation(boolean pSsl, String wsPath, FullHttpRequest pReq) {
        String location =  pReq.headers().get(HOST) + wsPath;
        if (pSsl) {
            return "wss://" + location;
        } else {
            return "ws://" + location;
        }
    }

    private static class AckMarker{
        private int mSendCnt;
        private byte[] mData;

        public AckMarker(int pSendCnt, byte[] pData){
            mSendCnt = pSendCnt;
            mData = pData;
        }

        public int getSendCnt(){
            return mSendCnt;
        }

        public byte[] getData(){
            return mData;
        }

        public void trying(){
            mSendCnt--;
        }
    }

    private class MessageWatcher implements Runnable{

        private Map<ChannelHandlerContext, AckMarker> mMessageSendContextMap;
        private WebSocketManager mSocketManager;

        public MessageWatcher(Map<ChannelHandlerContext, AckMarker> pMessageSendContextMap, WebSocketManager pSocketManager){
            mMessageSendContextMap = pMessageSendContextMap;
            mSocketManager = pSocketManager;
        }
        @Override
        public void run() {
            for(ChannelHandlerContext ctx: mMessageSendContextMap.keySet()){
                AckMarker marker = mMessageSendContextMap.get(ctx);
                if(marker.getSendCnt() > 0){
                    marker.trying();
                    mSocketManager.sendMessage(ctx, marker.getData());
                }
                else{
                    mMessageSendContextMap.remove(ctx);
                    mSocketManager.closeSocket(ctx);
                }
            }
        }
    }

    private class ControlWatcher implements Runnable{

        private Map<ChannelHandlerContext, Integer> mControlSignalSendContextMap;
        private WebSocketManager mSocketManager;

        public ControlWatcher(Map<ChannelHandlerContext, Integer> pControlSignalSendContextMap,  WebSocketManager pSocketManager){
            mControlSignalSendContextMap = pControlSignalSendContextMap;
            mSocketManager = pSocketManager;
        }

        @Override
        public void run() {
            for(ChannelHandlerContext ctx : mControlSignalSendContextMap.keySet()){
                int cnt = mControlSignalSendContextMap.get(ctx);
                if(cnt > 0){
                    cnt--;
                    mControlSignalSendContextMap.put(ctx, cnt);
                    mSocketManager.sendControl(ctx);
                }
                else{
                    mControlSignalSendContextMap.remove(ctx);
                    mSocketManager.closeSocket(ctx);
                }
            }
        }
    }

    private class PingWatcher implements Runnable{

        private WebSocketManager mSocketManager;
        private Set<ChannelHandlerContext> mPingSendMap;
        private Map<String, ChannelHandlerContext> mWebsocketMap;

        public PingWatcher(Set<ChannelHandlerContext> pPingSendMap, Map<String, ChannelHandlerContext> pWebsocketMap, WebSocketManager pSocketManager){
            mSocketManager = pSocketManager;
            mPingSendMap = pPingSendMap;
            mWebsocketMap = pWebsocketMap;
        }

        @Override
        public void run() {
            for(ChannelHandlerContext ctx : mPingSendMap){
                mSocketManager.closeSocket(ctx);
                mPingSendMap.remove(ctx);
            }

            for(String id : mWebsocketMap.keySet()){
                ChannelHandlerContext ctx = mWebsocketMap.get(id);
                mPingSendMap.add(ctx);
                mSocketManager.sendPing(ctx);
            }
        }
    }
}
