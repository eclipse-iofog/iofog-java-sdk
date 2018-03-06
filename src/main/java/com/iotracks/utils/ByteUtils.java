package com.iotracks.utils;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Utils class for convenient byte transformations.
 */

public class ByteUtils {

    public static byte[] copyOfRange(byte[] src, int from, int to) {
        return Arrays.copyOfRange(src, from, to);
    }

    public static byte[] longToBytes(long x) {
        byte[] b = new byte[8];
        for (int i = 0; i < 8; ++i) {
            b[i] = (byte) (x >> (8 - i - 1 << 3));
        }
        return b;
    }

    public static long bytesToLong(byte[] bytes) {
        long result = 0;
        for (byte aByte : bytes) {
            result = (result << 8) + (aByte & 0xff);
        }
        return result;
    }

    public static byte[] integerToBytes(int x) {
        byte[] b = new byte[4];
        for (int i = 0; i < 4; ++i) {
            b[i] = (byte) (x >> (4 - i - 1 << 3));
        }
        return b;
    }

    public static int bytesToInteger(byte[] bytes) {
        int result = 0;
        for (byte aByte : bytes) {
            result = (result << 8) + (aByte & 0xff);
        }
        return result;
    }

    public static byte[] shortToBytes(short x) {
        byte[] b = new byte[2];
        for (int i = 0; i < 2; ++i) {
            b[i] = (byte) (x >> (2 - i - 1 << 3));
        }
        return b;
    }

    public static short bytesToShort(byte[] bytes) {
        short result = 0;
        for (byte aByte : bytes) {
            result = (short) ((result << 8) + (aByte & 0xff));
        }
        return result;
    }

    public static byte[] stringToBytes(String s) {
        if (s == null)
            return new byte[] {};
        else
            return s.getBytes();
    }

    public static String bytesToString(byte[] bytes) {
        return new String(bytes);
    }

    public static String byteArrayToString(byte[] bytes) {
        StringBuilder result = new StringBuilder();

        result.append("[");
        for (byte b : bytes) {
            if (result.length() > 1)
                result.append(", ");
            result.append(b);
        }
        result.append("]");

        return result.toString();
    }

    public static byte[] decimalToBytes(double x){
        return ByteBuffer.allocate(8).putDouble(x).array();
    }

    public static int getLength(String s){
        return s!=null ? s.length() : 0;
    }
}
