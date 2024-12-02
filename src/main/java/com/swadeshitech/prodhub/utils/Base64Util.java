package com.swadeshitech.prodhub.utils;

import java.util.Base64;

public class Base64Util {
 
    public static String generateBase64Encoded(String input) {
        byte[] encodedBytes = Base64.getEncoder().encode(input.getBytes());
        String base64EncodedString = new String(encodedBytes);
        return base64EncodedString;
    }

    public static String convertToPlainText(String base64EncodedString) {
        byte[] decodedBytes = Base64.getDecoder().decode(base64EncodedString);
        String decodedText = new String(decodedBytes);
        return decodedText;
    }
}
