package com.iotracks.api.listener;

import com.iotracks.elements.IOMessage;

/**
 * Listener's Interface for REST calls to ioFabric.
 *
 * Created by forte on 3/28/16.
 *
 * @author ilaryionava
 */

public interface IOFabricWSAPIListener {

    /**
     * Method is triggered when Container receives receipt from ioFabric, which is the response on sent message.
     *
     * @param message - IOMessage that was sent to ioFabric
     *
     */
    void onSuccess(IOMessage message);

    /**
     * Method is triggered when Container catches an error.
     *
     * @param cause - cause of exception
     *
     */
    void onError(Throwable cause);

}
