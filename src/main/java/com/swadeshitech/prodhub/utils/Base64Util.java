package com.swadeshitech.prodhub.utils;

import com.swadeshitech.prodhub.enums.ErrorCode;
import com.swadeshitech.prodhub.exception.CustomException;
import lombok.extern.slf4j.Slf4j;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Locale;

@Slf4j
public class Base64Util {
 
    public static String generateBase64Encoded(String input) {
        byte[] encodedBytes = Base64.getEncoder().encode(input.getBytes());
        return new String(encodedBytes);
    }

    public static String convertToPlainText(String base64EncodedString) {
        byte[] decodedBytes = Base64.getDecoder().decode(base64EncodedString);
        return new String(decodedBytes);
    }

    public static String generate7DigitHash(String jsonString) {

        if (jsonString == null || jsonString.isEmpty()) {
            return "0000000"; // Return a default value for empty input
        }

        try {
            // 1. Get the SHA-256 MessageDigest instance
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            // 2. Compute the hash digest (byte array)
            byte[] hashBytes = digest.digest(jsonString.getBytes());

            // 3. Take the first 4 bytes of the hash (32 bits)
            // This is a convenient chunk to convert directly into an int.
            int intValue = 0;
            for (int i = 0; i < 4; i++) {
                // Combine the bytes into a single integer
                intValue = (intValue << 8) | (hashBytes[i] & 0xFF);
            }

            // 4. Ensure the number is positive (hash can be negative)
            // Math.abs handles the sign flip for the final modulus operation.
            long positiveValue = Math.abs((long) intValue);

            // 5. Truncate using modulus of 10,000,000 (10^7)
            // This guarantees the result is between 0 and 9,999,999.
            long sevenDigitHash = positiveValue % 10000000;

            // 6. Format the number with leading zeros to guarantee 7 digits
            return String.format(Locale.ROOT, "%07d", sevenDigitHash);

        } catch (NoSuchAlgorithmException e) {
            // This should not happen with SHA-256
            log.error("Failed to generate the hash", e);
            throw new CustomException(ErrorCode.BAD_REQUEST);
        }
    }
}
