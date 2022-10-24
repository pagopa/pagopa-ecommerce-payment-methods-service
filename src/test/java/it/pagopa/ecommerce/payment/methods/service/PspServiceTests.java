package it.pagopa.ecommerce.payment.methods.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import it.pagopa.ecommerce.payment.methods.domain.aggregates.Psp;
import it.pagopa.ecommerce.payment.methods.domain.valueobjects.*;
import it.pagopa.ecommerce.payment.methods.infrastructure.PspRepository;
import it.pagopa.ecommerce.payment.methods.server.model.LanguageDto;
import it.pagopa.ecommerce.payment.methods.server.model.PspDto;
import it.pagopa.ecommerce.payment.methods.utils.LanguageEnum;
import it.pagopa.ecommerce.payment.methods.utils.TestUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import it.pagopa.ecommerce.payment.methods.application.PspService;
import it.pagopa.ecommerce.payment.methods.infrastructure.PspDocument;
import it.pagopa.ecommerce.payment.methods.infrastructure.PspDocumentKey;
import it.pagopa.ecommerce.payment.methods.infrastructure.rule.FilterRuleEngine;
import it.pagopa.ecommerce.payment.methods.utils.PaymentMethodStatusEnum;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

@SpringBootTest
@TestPropertySource(locations = "classpath:application.test.properties")
@ExtendWith(MockitoExtension.class)
class PspServiceTests {

    @Mock
    private FilterRuleEngine filterRuleEngine;

    @InjectMocks
    private PspService pspService;

    @Mock
    private PspRepository pspRepository;

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
                        new PspAmount(10.0),
                        new PspAmount(1.0),
                        new PspChannelCode("AB0"),
                        new PspFee(0.0)));

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

    @Test
    void shouldRetrievePspByKey() {
        Psp psp = TestUtil.getTestPsp();
        PspDocument pspDocument = TestUtil.getTestPspDoc(psp);

        PspDto expected = new PspDto()
                .code(pspDocument.getPspDocumentKey().getPspCode())
                .paymentTypeCode(pspDocument.getPspDocumentKey().getPspPaymentTypeCode())
                .description(pspDocument.getPspDescription())
                .businessName(pspDocument.getPspBusinessName())
                .status(PspDto.StatusEnum.fromValue(pspDocument.getPspStatus()))
                .brokerName(pspDocument.getPspBrokerName())
                .language(LanguageDto.fromValue(pspDocument.getPspDocumentKey().getPspLanguageCode()))
                .minAmount(pspDocument.getPspMinAmount())
                .maxAmount(pspDocument.getPspMaxAmount())
                .fixedCost(pspDocument.getPspFixedCost());

        PspDocumentKey searchKey = pspDocument.getPspDocumentKey();

        Mockito.when(pspRepository.findPspByKey(searchKey)).thenReturn(Mono.just(pspDocument));

        StepVerifier.create(pspService.findPsp(searchKey))
                .expectNext(expected)
                .verifyComplete();
    }

    @Test
    void shouldReturnEmptyMonoIfNotMatch() {
        Psp psp = TestUtil.getTestPsp();
        PspDocument pspDocument = TestUtil.getTestPspDoc(psp);

        PspDocumentKey searchKey = pspDocument.getPspDocumentKey();

        Mockito.when(pspRepository.findPspByKey(searchKey)).thenReturn(Mono.empty());

        StepVerifier.create(pspService.findPsp(searchKey))
                .verifyComplete();

    }
}
