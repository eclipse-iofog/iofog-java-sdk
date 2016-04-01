package com.iotracks.api.listener;

import com.iotracks.elements.IOFabricResponseMessage;

/**
 * Listener's Interface for REST calls to ioFabric.
 *
 * Created by forte on 3/24/16.
 *
 * @author ilaryionava
 */
public interface IOFabricRESTAPIListener {

    /**
     * Method is triggered when Container receives response on request from ioFabric.
     *
     * @param responseMsg - representation of response message from ioFabric
     *
     */
    void onSuccess(IOFabricResponseMessage responseMsg);

    /**
     * Method is triggered when Container catches an error.
     *
     * @param cause - cause of exception
     *
     */
    void onError(Throwable cause);
}
