package com.livevote.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashUtils {

    /**
     * Hashes a given input string using SHA-256.
     *
     * @param input The string to be hashed.
     * @return The hashed string in hexadecimal format.
     * @throws RuntimeException If the hashing algorithm is not available.
     */
    public static String hashString(String input) {
        try {
            // Create a MessageDigest instance for SHA-256
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            // Perform the hashing
            byte[] hashBytes = digest.digest(input.getBytes());

            // Convert the byte array into a hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            // Rethrow as a RuntimeException if SHA-256 is unavailable
            throw new RuntimeException("Error hashing string: SHA-256 algorithm not found", e);
        }
    }
}
