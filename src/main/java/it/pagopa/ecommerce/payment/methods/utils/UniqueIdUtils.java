package it.pagopa.ecommerce.payment.methods.utils;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class UniqueIdUtils {
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final String ALPHANUMERICS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz-._";
    private static final int MAX_LENGTH = 18;

    public String generateUniqueId() {
        String timestampToString = String.valueOf(System.currentTimeMillis());
        int randomStringLength = MAX_LENGTH - timestampToString.length();
        return timestampToString + generateRandomString(randomStringLength);
    }

    private static String generateRandomString(int length) {
        StringBuilder stringBuilder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            stringBuilder.append(ALPHANUMERICS.charAt(secureRandom.nextInt(ALPHANUMERICS.length())));
        }
        return stringBuilder.toString();
    }

}
