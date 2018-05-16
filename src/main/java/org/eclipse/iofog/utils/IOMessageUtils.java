/*
 * *******************************************************************************
 *   Copyright (c) 2018 Edgeworx, Inc.
 *
 *   This program and the accompanying materials are made available under the
 *   terms of the Eclipse Public License v. 2.0 which is available at
 *   http://www.eclipse.org/legal/epl-2.0
 *
 *   SPDX-License-Identifier: EPL-2.0
 * *******************************************************************************
 */

package org.eclipse.iofog.utils;

import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utils class for convenient encoding and decoding for byte arrays
 *
 * @author ilaryionava
 */
public class IOMessageUtils {

    private static final Logger log = Logger.getLogger(IOMessageUtils.class.getName());

    /**
     * Method to encode byte array to base64 format.
     *
     * @param data - array of bytes to be encoded
     *
     * @return byte[]
     */
    public static byte[] encodeBase64(byte[] data) {
        try {
            return Base64.getEncoder().encode(data);
        } catch (Exception e) {
            log.log(Level.WARNING, "Error encoding bytes to base64 format.");
            return null;
        }
    }

    /**
     * Method to decode byte array from base64 format.
     *
     * @param data - array of bytes to be decoded
     *
     * @return byte[]
     */
    public static byte[] decodeBase64(byte[] data) {
        try {
            return Base64.getDecoder().decode(data);
        } catch (Exception e) {
            log.log(Level.WARNING, "Error decoding bytes from base64 format.");
            return null;
        }
    }
}
