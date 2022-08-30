package it.pagopa.ecommerce.payment.instruments.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import it.pagopa.ecommerce.payment.instruments.application.PspService;
import it.pagopa.ecommerce.payment.instruments.domain.valueobjects.PspStatus;
import it.pagopa.ecommerce.payment.instruments.infrastructure.PspDocument;
import it.pagopa.ecommerce.payment.instruments.infrastructure.PspDocumentKey;
import it.pagopa.ecommerce.payment.instruments.infrastructure.rule.FilterRuleEngine;
import it.pagopa.ecommerce.payment.instruments.utils.PaymentMethodStatusEnum;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@SpringBootTest
@TestPropertySource(locations = "classpath:application.test.properties")
@ExtendWith(MockitoExtension.class)
class PspServiceTests {

    @Mock
    private FilterRuleEngine filterRuleEngine;

    @InjectMocks
    private PspService pspService;

    @Test
    void shouldReturnEmptyResultWithNullFilter() {

        // Precondition
        Mockito.when(filterRuleEngine.applyFilter(null, null, null)).thenReturn(Flux.empty());

        // Test execution
        Flux<PspDocument> services = pspService.getPspByFilter( null, null, null);

        // Asserts
        assertEquals(services, Flux.empty());
    }

    @Test
    void shouldReturnEmptyResultWithEmptyFilter() {

        // Precondition
        Mockito.when(filterRuleEngine.applyFilter(100, "", "")).thenReturn(Flux.empty());

        // Test execution
        Flux<PspDocument> services = pspService.getPspByFilter(100, "", "");

        // Asserts
        assertEquals(services, Flux.empty());
    }

    @Test
    void shouldReturnEmptyResultWithAmountEmptyFilter() {

        // Precondition
        Mockito.when(filterRuleEngine.applyFilter(null, "", "")).thenReturn(Flux.empty());

        // Test execution
        Flux<PspDocument> services = pspService.getPspByFilter(null, "", "");

        // Asserts
        assertEquals(services, Flux.empty());
    }

    @Test
    void shouldReturnFluxResultGivenAmountLangTypeCode() {

        // Precondition
        Integer amount = 1000;
        String language = "IT";
        String paymentTypeCode = "PO";

        PspDocument pspDocument_1 = new PspDocument(
                new PspDocumentKey(
                        "PSP_CODE",
                        paymentTypeCode,
                        "CHANNEL_0",
                        language),
                new PspStatus(PaymentMethodStatusEnum.ENABLED).value().getCode(),
                "Test",
                "Test broker",
                "Test description",
                0.0,
                100.0,
                100.0);

        PspDocument pspDocument_2 = new PspDocument(
                new PspDocumentKey(
                        "PSP_CODE_2",
                        paymentTypeCode,
                        "CHANNEL_0_2",
                        language),
                new PspStatus(PaymentMethodStatusEnum.ENABLED).value().getCode(),
                "Test_2",
                "Test broker",
                "Test description",
                0.0,
                100.0,
                100.0);

        Mockito.when(filterRuleEngine.applyFilter( amount, language, paymentTypeCode))
                .thenReturn(Flux.just(pspDocument_1, pspDocument_2));
        // Test execution

        Flux<PspDocument> services = pspService.getPspByFilter(amount, language, paymentTypeCode);

        // Asserts
        StepVerifier.create(services)
                .expectNext(pspDocument_1)
                .expectNext(pspDocument_2)
                .verifyComplete();
    }
}
