package org.eclipse.iofog.api.listener;

import org.eclipse.iofog.elements.IOMessage;

import javax.json.JsonObject;
import java.util.List;

/**
 * Listener's Interface for requests to ioFog.
 *
 * @author ilaryionava
 */
public interface IOFogAPIListener {

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
     * Method is triggered when Container receives BAD_REQUEST response from ioFog.
     *
     * @param error - error messages
     */
    void onBadRequest(String error);

    /**
     * Method is triggered when Container receives message's receipt.
     *
     * @param messageId - generated id of sent message
     * @param timestamp - timestamp generated when message was received by ioFog
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
