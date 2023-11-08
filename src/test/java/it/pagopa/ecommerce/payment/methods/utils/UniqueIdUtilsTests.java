package it.pagopa.ecommerce.payment.methods.utils;

import it.pagopa.ecommerce.payment.methods.exception.UniqueIdGenerationException;
import it.pagopa.ecommerce.payment.methods.infrastructure.UniqueIdTemplateWrapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.test.StepVerifier;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;

class UniqueIdUtilsTests {
    private final UniqueIdTemplateWrapper uniqueIdTemplateWrapper = mock(UniqueIdTemplateWrapper.class);

    private final UniqueIdUtils uniqueIdUtils = new UniqueIdUtils(uniqueIdTemplateWrapper);

    private static final String PRODUCT_PREFIX = "E";

    @Test
    void shouldGenerateUniqueIdGenerateException() {
        Mockito.when(uniqueIdTemplateWrapper.saveIfAbsent(any(), any())).thenReturn(false);
        StepVerifier.create(uniqueIdUtils.generateUniqueId())
                .expectErrorMatches(e -> e instanceof UniqueIdGenerationException)
                .verify();
    }

    @Test
    void shouldGenerateUniqueIdWithRetry() {
        Mockito.when(uniqueIdTemplateWrapper.saveIfAbsent(any(), any())).thenReturn(false, false, true);
        StepVerifier.create(uniqueIdUtils.generateUniqueId())
                .expectNextMatches(
                        response -> response.length() == 18 && response.startsWith(PRODUCT_PREFIX)
                )
                .verifyComplete();
    }

    @Test
    void shouldGenerateUniqueIdNoRetry() {
        Mockito.when(uniqueIdTemplateWrapper.saveIfAbsent(any(), any())).thenReturn(true);
        StepVerifier.create(uniqueIdUtils.generateUniqueId())
                .expectNextMatches(
                        response -> response.length() == 18 && response.startsWith(PRODUCT_PREFIX)
                )
                .verifyComplete();
    }
}
