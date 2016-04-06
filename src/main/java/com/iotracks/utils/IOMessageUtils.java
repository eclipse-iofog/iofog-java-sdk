package com.iotracks.utils;

import com.iotracks.elements.IOMessage;

import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utils class for convenient encoding and decoding for IOMessage
 *
 * @author ilaryionava
 */
public class IOMessageUtils {

    private static final Logger log = Logger.getLogger(IOMessageUtils.class.getName());

    /**
     * Method to encode IOMessage to bytes in base64 format.
     *
     * @param message - IOMessage to be encoded
     *
     * @return byte[]
     */
    public byte[] encodeBase64(IOMessage message) {
        try {
            return Base64.getEncoder().encode(message.getBytes());
        } catch (Exception e) {
            log.log(Level.WARNING, "Error encoding IOMessage to base64 format.");
            return null;
        }
    }

    /**
     * Method to decode byte array in base64 format to IOMessage.
     *
     * @param bytes - array of bytes to be decoded into IOMessage
     *
     * @return IOMessage
     */
    public IOMessage decodeBase64(byte[] bytes) {
        try {
            return new IOMessage(Base64.getDecoder().decode(bytes));
        } catch (Exception e) {
            log.log(Level.WARNING, "Error decoding bytes from base64 format to IOMessage.");
            return null;
        }
    }
}
