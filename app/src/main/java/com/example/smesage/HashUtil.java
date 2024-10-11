package com.example.smesage;

import org.bouncycastle.jcajce.provider.digest.SHA3;

public class HashUtil {
    public static String hashWithSHA3(String input) {
        SHA3.DigestSHA3 sha3 = new SHA3.Digest512(); // Используем SHA3-512
        byte[] hash = sha3.digest(input.getBytes());
        return bytesToHex(hash);
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
