package org.example.lufthansaurlsystem.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class Base62Encoder {
    private static final String ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int BASE = ALPHABET.length();

    public static String encode(long numericId) {

        if (numericId == 0) return "0";
        StringBuilder sb = new StringBuilder();
        while (numericId > 0) {
            sb.append(ALPHABET.charAt((int) (numericId % BASE)));
            numericId /= BASE;
        }

        return sb.reverse().toString();
    }

    public static long decode(String encodedString) {
        long result = 0;
        for (int i = 0; i < encodedString.length(); i++) {
            result = result * BASE + ALPHABET.indexOf(encodedString.charAt(i));
        }
        return result;
    }
}