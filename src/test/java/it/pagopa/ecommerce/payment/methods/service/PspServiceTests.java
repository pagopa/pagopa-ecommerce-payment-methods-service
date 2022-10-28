package it.pagopa.ecommerce.payment.methods.service;

import it.pagopa.ecommerce.payment.methods.application.PspService;
import it.pagopa.ecommerce.payment.methods.domain.aggregates.Psp;
import it.pagopa.ecommerce.payment.methods.domain.valueobjects.PspAmount;
import it.pagopa.ecommerce.payment.methods.domain.valueobjects.PspBrokerName;
import it.pagopa.ecommerce.payment.methods.domain.valueobjects.PspBusinessName;
import it.pagopa.ecommerce.payment.methods.domain.valueobjects.PspChannelCode;
import it.pagopa.ecommerce.payment.methods.domain.valueobjects.PspCode;
import it.pagopa.ecommerce.payment.methods.domain.valueobjects.PspDescription;
import it.pagopa.ecommerce.payment.methods.domain.valueobjects.PspFee;
import it.pagopa.ecommerce.payment.methods.domain.valueobjects.PspLanguage;
import it.pagopa.ecommerce.payment.methods.domain.valueobjects.PspPaymentMethodType;
import it.pagopa.ecommerce.payment.methods.domain.valueobjects.PspStatus;
import it.pagopa.ecommerce.payment.methods.infrastructure.PspDocument;
import it.pagopa.ecommerce.payment.methods.infrastructure.PspDocumentKey;
import it.pagopa.ecommerce.payment.methods.infrastructure.rule.FilterRuleEngine;
import it.pagopa.ecommerce.payment.methods.server.model.PspDto;
import it.pagopa.ecommerce.payment.methods.utils.LanguageEnum;
import it.pagopa.ecommerce.payment.methods.utils.PaymentMethodStatusEnum;
import it.pagopa.ecommerce.payment.methods.utils.TestUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.math.BigInteger;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@TestPropertySource(locations = "classpath:application.test.properties")
@ExtendWith(MockitoExtension.class)
class PspServiceTests {

    @Mock
    private FilterRuleEngine filterRuleEngine;

    @InjectMocks
    private PspService pspService;

    @Test
    void shouldReturnPsp() {

        PspDocument pspDocument = TestUtil.getTestPspDoc(TestUtil.getTestPsp());

        // Precondition
        Mockito.when(filterRuleEngine.applyFilter(null, null, null))
                .thenReturn(Flux.just(pspDocument));

        // Test execution
        List<PspDto> services = pspService.retrievePsps(null, null, null)
                .collectList().block();

        // Asserts
        assertEquals(1, services.size());
        assertEquals(pspDocument.getPspDocumentKey().getPspCode(), services.get(0).getCode());
    }

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
    void shouldThrowInvalidRangeException() {
        assertThrows(IllegalArgumentException.class,
                () -> new Psp(
                        new PspCode("PSP_TEST_CODE"),
                        new PspPaymentMethodType("PO"),
                        new PspStatus(PaymentMethodStatusEnum.ENABLED),
                        new PspBusinessName(""),
                        new PspBrokerName(""),
                        new PspDescription(""),
                        new PspLanguage(LanguageEnum.IT),
                        new PspAmount(BigInteger.valueOf(10)),
                        new PspAmount(BigInteger.valueOf(1)),
                        new PspChannelCode("AB0"),
                        new PspFee(BigInteger.valueOf(0))));

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
                BigInteger.valueOf(0),
                        BigInteger.valueOf(100),
                BigInteger.valueOf(100));

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
                BigInteger.valueOf(0),
                BigInteger.valueOf(100),
                BigInteger.valueOf(100));

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
