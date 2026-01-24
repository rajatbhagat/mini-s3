package com.rajat.minis3.utils;

import java.util.Base64;

public class Base64Utils {

    public static String convertToBase64String(byte[] content) {
        return Base64.getEncoder().encodeToString(content);
    }

    public static byte[] getByteContentFromBase64String(String base64EncodedString) {
        return Base64.getDecoder().decode(base64EncodedString);
    }
}
