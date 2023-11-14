package it.pagopa.ecommerce.payment.methods.utils;

import it.pagopa.ecommerce.payment.methods.exception.UniqueIdGenerationException;
import it.pagopa.ecommerce.payment.methods.infrastructure.UniqueIdDocument;
import it.pagopa.ecommerce.payment.methods.infrastructure.UniqueIdTemplateWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.security.SecureRandom;
import java.time.Duration;

@Component
public class UniqueIdUtils {
    private final UniqueIdTemplateWrapper uniqueIdTemplateWrapper;
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final String ALPHANUMERICS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz-._";
    private static final int MAX_LENGTH = 18;
    private static final int MAX_NUMBER_ATTEMPTS = 3;
    private static final String PRODUCT_PREFIX = "E";

    @Autowired
    public UniqueIdUtils(UniqueIdTemplateWrapper uniqueIdTemplateWrapper) {
        this.uniqueIdTemplateWrapper = uniqueIdTemplateWrapper;
    }

    public Mono<String> generateUniqueId() {
        boolean isSuccessfullySaved = false;
        int attempt = 0;
        String uniqueId = generateRandomIdentifier();
        while (attempt < MAX_NUMBER_ATTEMPTS && !isSuccessfullySaved) {
            isSuccessfullySaved = uniqueIdTemplateWrapper
                    .saveIfAbsent(new UniqueIdDocument(uniqueId), Duration.ofSeconds(60));
            attempt++;
            if (!isSuccessfullySaved) {
                uniqueId = generateRandomIdentifier();
            }
        }
        return !isSuccessfullySaved ? Mono.error(new UniqueIdGenerationException()) : Mono.just(uniqueId);
    }

    private static String generateRandomIdentifier() {
        StringBuilder uniqueId = new StringBuilder(PRODUCT_PREFIX);
        uniqueId.append(System.currentTimeMillis());
        int randomStringLength = MAX_LENGTH - uniqueId.length();
        return uniqueId.append(generateRandomString(randomStringLength)).toString();
    }

    private static String generateRandomString(int length) {
        StringBuilder stringBuilder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            stringBuilder.append(ALPHANUMERICS.charAt(secureRandom.nextInt(ALPHANUMERICS.length())));
        }
        return stringBuilder.toString();
    }

}