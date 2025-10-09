package com.swadeshitech.prodhub.utils;

import java.util.UUID;

public final class UuidUtil {

    // Private constructor to prevent instantiation
    private UuidUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Generate a random (version 4) UUID.
     *
     * @return A randomly generated UUID string.
     */
    public static String generateRandomUuid() {
        return UUID.randomUUID().toString();
    }

    /**
     * Generate a name-based (version 3) UUID using MD5 hashing.
     *
     * @param namespace The namespace UUID.
     * @param name The name from which to generate UUID.
     * @return The generated UUID string.
     */
    public static String generateNameBasedUuid(UUID namespace, String name) {
        return UUID.nameUUIDFromBytes((namespace.toString() + name).getBytes()).toString();
    }

    /**
     * Generate a UUID from given string input.
     * Useful if you want deterministic UUID from string.
     *
     * @param input Input string.
     * @return UUID string.
     */
    public static String generateFromString(String input) {
        return UUID.nameUUIDFromBytes(input.getBytes()).toString();
    }
}
