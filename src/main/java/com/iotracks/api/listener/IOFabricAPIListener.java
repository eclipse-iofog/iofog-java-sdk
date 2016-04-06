package com.iotracks.api.listener;

import com.iotracks.elements.IOMessage;

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
     * Method is triggered when Container catches an error.
     *
     * @param cause - cause of exception
     */
    void onError(Throwable cause);

    /**
     * Method is triggered when Container receives BAD_REQUEST response from ioFabric.
     *
     * @param errors - arrays of error messages
     */
    void onError(String[] errors);

    /**
     * Method is triggered when Container receives message's receipt.
     *
     */
    void onMessageReceipt();

    /**
     * Method is triggered when Container receives new configuration.
     *
     *  @param config - new configuration map
     */
    void onNewConfig(Map<String, String> config);

    /**
     * Method is triggered when Container receives a signal about new configuration.
     */
    void onNewConfig();
}
