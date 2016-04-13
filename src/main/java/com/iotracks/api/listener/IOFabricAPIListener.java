package com.iotracks.api.listener;

import com.iotracks.elements.IOMessage;

import javax.json.JsonObject;
import java.util.List;
import java.util.Map;

/**
 * Listener's Interface for requests to ioFabric.
 *
 * @author ilaryionava
 */
public interface IOFabricAPIListener {

    /**
     * Method is triggered when Container receives messages.
     *
     * @param messages - list of received messages
     */
    void onMessages(List<IOMessage> messages);

    /**
     * Method is triggered when Container receives messages from Query request.
     *
     * @param timeframestart - time-frame start date of returned messages
     * @param timeframeend - time-frame end date of returned messages
     * @param messages - list of received messages
     */
    void onMessagesQuery(long timeframestart, long timeframeend, List<IOMessage> messages);

    /**
     * Method is triggered when Container catches an error.
     *
     * @param cause - cause of exception
     */
    void onError(Throwable cause);

    /**
     * Method is triggered when Container receives BAD_REQUEST response from ioFabric.
     *
     * @param error - error messages
     */
    void onBadRequest(String error);

    /**
     * Method is triggered when Container receives message's receipt.
     *
     * @param messageId - generated id of sent message
     * @param timestamp - timestamp generated when message was received by ioFabric
     */
    void onMessageReceipt(String messageId, long timestamp);

    /**
     * Method is triggered when Container receives new configuration.
     *
     *  @param config - new configuration JSON
     */
    void onNewConfig(JsonObject config);

    /**
     * Method is triggered when Container receives a signal about new configuration.
     */
    void onNewConfigSignal();
}
