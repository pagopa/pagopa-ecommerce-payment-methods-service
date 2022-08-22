package it.pagopa.ecommerce.payment.instruments.service;

import it.pagopa.ecommerce.payment.instruments.application.PaymentInstrumentService;
import it.pagopa.ecommerce.payment.instruments.domain.aggregates.PaymentMethod;
import it.pagopa.ecommerce.payment.instruments.domain.aggregates.PaymentMethodFactory;
import it.pagopa.ecommerce.payment.instruments.domain.valueobjects.*;
import it.pagopa.ecommerce.payment.instruments.infrastructure.PaymentInstrumentCategoryRepository;
import it.pagopa.ecommerce.payment.instruments.infrastructure.PaymentInstrumentDocument;
import it.pagopa.ecommerce.payment.instruments.infrastructure.PaymentInstrumentRepository;
import it.pagopa.ecommerce.payment.instruments.utils.PaymentInstrumentStatusEnum;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@SpringBootTest
@TestPropertySource(locations = "classpath:application.test.properties")
@ExtendWith(MockitoExtension.class)
class PaymentMethodServiceTests {

    @Mock
    private PaymentInstrumentRepository paymentInstrumentRepository;

    @Mock
    private PaymentMethodFactory paymentInstrumentFactory;

    @InjectMocks
    private PaymentInstrumentService paymentInstrumentService;

    @Mock
    private PaymentInstrumentCategoryRepository paymentInstrumentCategoryRepository;

    @Test
    void shouldCreatePaymentInstrument() {

        String paymentInstrumentNameAsString = "paymentInstrumentName";
        String paymentInstrumentDescriptionAsString = "paymentInstrumentDescription";

        PaymentMethodName paymentMethodName = new PaymentMethodName(paymentInstrumentNameAsString);
        PaymentMethodDescription paymentMethodDescription = new PaymentMethodDescription(
                paymentInstrumentDescriptionAsString);
        PaymentInstrumentCategoryID paymentInstrumentCategoryID = new PaymentInstrumentCategoryID(
                UUID.fromString(UUID.randomUUID().toString()));
        PaymentMethodID paymentMethodID = new PaymentMethodID(UUID.randomUUID());
        PaymentMethodStatus paymentMethodStatus = new PaymentMethodStatus(
                PaymentInstrumentStatusEnum.ENABLED);
        PaymentInstrumentCategoryName paymentInstrumentCategoryName =
                new PaymentInstrumentCategoryName("paymentInstrumentCategoryName");
        List<PaymentMethodType> paymentMethodType = List.of(new PaymentMethodType("PO"));
        PaymentMethodType paymentMethodTypeCode = new PaymentMethodType("test");

        PaymentMethod paymentInstrument = new PaymentMethod(paymentMethodID,
                paymentMethodName,
                paymentMethodDescription,
                paymentMethodStatus,
                paymentInstrumentCategoryID,
                paymentInstrumentCategoryName,
                paymentMethodType,
                paymentMethodTypeCode);

        PaymentInstrumentDocument paymentInstrumentDocument = new PaymentInstrumentDocument(
                paymentInstrument.getPaymentMethodID().value().toString(),
                paymentInstrument.getPaymentMethodName().value(),
                paymentInstrument.getPaymentMethodDescription().value(),
                paymentInstrument.getPaymentMethodStatus().value().toString(),
                paymentInstrument.getPaymentInstrumentCategoryID().value().toString(),
                paymentInstrument.getPaymentInstrumentCategoryName().value(),
                paymentInstrument.getPaymentInstrumentCategoryTypes().stream()
                        .map(PaymentMethodType::value).collect(Collectors.toList()),
                paymentInstrument.getPaymentMethodTypeCode().value());

        Mockito.when(paymentInstrumentFactory.newPaymentInstrument(
                        any(), any(), any(), any(), any(), any())
                )
                .thenReturn(Mono.just(paymentInstrument));

        Mockito.when(paymentInstrumentRepository.save(
                        paymentInstrumentDocument))
                .thenReturn(Mono.just(paymentInstrumentDocument));

        PaymentMethod paymentInstrumentCreated = paymentInstrumentService.createPaymentInstrument(
                paymentInstrumentNameAsString,
                paymentInstrumentDescriptionAsString,
                paymentInstrumentCategoryID.value().toString(),
                paymentMethodTypeCode.value()).block();

        assertEquals(paymentInstrumentCreated.getPaymentInstrumentCategoryID().value(),
                paymentInstrumentCategoryID.value());
    }

    @Test
    void shouldRetrievePaymentInstruments() {

        String paymentInstrumentNameAsString = "paymentInstrumentName";
        String paymentInstrumentDescriptionAsString = "paymentInstrumentDescription";

        PaymentMethodName paymentMethodName = new PaymentMethodName(paymentInstrumentNameAsString);
        PaymentMethodDescription paymentMethodDescription = new PaymentMethodDescription(
                paymentInstrumentDescriptionAsString);
        PaymentInstrumentCategoryID paymentInstrumentCategoryID = new PaymentInstrumentCategoryID(
                UUID.fromString(UUID.randomUUID().toString()));
        PaymentMethodID paymentMethodID = new PaymentMethodID(UUID.randomUUID());
        PaymentMethodStatus paymentMethodStatus = new PaymentMethodStatus(
                PaymentInstrumentStatusEnum.ENABLED);
        PaymentInstrumentCategoryName paymentInstrumentCategoryName =
                new PaymentInstrumentCategoryName("paymentInstrumentCategoryName");
        List<PaymentMethodType> paymentMethodType = List.of(new PaymentMethodType("PO"));
        PaymentMethodType paymentMethodTypeCode = new PaymentMethodType("test");

        PaymentMethod paymentInstrument = new PaymentMethod(paymentMethodID,
                paymentMethodName,
                paymentMethodDescription,
                paymentMethodStatus,
                paymentInstrumentCategoryID,
                paymentInstrumentCategoryName,
                paymentMethodType,
                paymentMethodTypeCode);

        PaymentInstrumentDocument paymentInstrumentDocument = new PaymentInstrumentDocument(
                paymentInstrument.getPaymentMethodID().value().toString(),
                paymentInstrument.getPaymentMethodName().value(),
                paymentInstrument.getPaymentMethodDescription().value(),
                paymentInstrument.getPaymentMethodStatus().value().toString(),
                paymentInstrument.getPaymentInstrumentCategoryID().value().toString(),
                paymentInstrument.getPaymentInstrumentCategoryName().value(),
                paymentInstrument.getPaymentInstrumentCategoryTypes().stream()
                        .map(PaymentMethodType::value).collect(Collectors.toList()),
                paymentMethodTypeCode.value());
        Mockito.when(paymentInstrumentRepository.findAll())
                .thenReturn(Flux.just(paymentInstrumentDocument));

        PaymentMethod paymentInstrumentCreated = paymentInstrumentService.retrivePaymentInstruments(null).blockFirst();

        assertEquals(paymentInstrumentCreated.getPaymentInstrumentCategoryID().value(),
                paymentInstrumentCategoryID.value());
    }

    @Test
    void shouldPatchPaymentInstrument() {
        String paymentInstrumentNameAsString = "paymentInstrumentName";
        String paymentInstrumentDescriptionAsString = "paymentInstrumentDescription";

        PaymentMethodName paymentMethodName = new PaymentMethodName(paymentInstrumentNameAsString);
        PaymentMethodDescription paymentMethodDescription = new PaymentMethodDescription(
                paymentInstrumentDescriptionAsString);
        PaymentInstrumentCategoryID paymentInstrumentCategoryID = new PaymentInstrumentCategoryID(
                UUID.fromString(UUID.randomUUID().toString()));
        PaymentMethodID paymentMethodID = new PaymentMethodID(UUID.randomUUID());
        PaymentMethodStatus paymentMethodStatus = new PaymentMethodStatus(
                PaymentInstrumentStatusEnum.ENABLED);
        PaymentInstrumentCategoryName paymentInstrumentCategoryName =
                new PaymentInstrumentCategoryName("paymentInstrumentCategoryName");
        List<PaymentMethodType> paymentMethodType = List.of(new PaymentMethodType("PO"));
        PaymentMethodType paymentMethodTypeCode = new PaymentMethodType("test");

        PaymentMethod paymentInstrument = new PaymentMethod(paymentMethodID,
                paymentMethodName,
                paymentMethodDescription,
                paymentMethodStatus,
                paymentInstrumentCategoryID,
                paymentInstrumentCategoryName,
                paymentMethodType,
                paymentMethodTypeCode
                );

        PaymentInstrumentDocument paymentInstrumentDocument = new PaymentInstrumentDocument(
                paymentInstrument.getPaymentMethodID().value().toString(),
                paymentInstrument.getPaymentMethodName().value(),
                paymentInstrument.getPaymentMethodDescription().value(),
                paymentInstrument.getPaymentMethodStatus().value().toString(),
                paymentInstrument.getPaymentInstrumentCategoryID().value().toString(),
                paymentInstrument.getPaymentInstrumentCategoryName().value(),
                paymentInstrument.getPaymentInstrumentCategoryTypes().stream()
                        .map(PaymentMethodType::value).collect(Collectors.toList()),
                paymentMethodTypeCode.value());
        Mockito.when(paymentInstrumentRepository.findById(paymentMethodID.value().toString()))
                .thenReturn(Mono.just(paymentInstrumentDocument));

        paymentInstrumentDocument.setPaymentInstrumentStatus(PaymentInstrumentStatusEnum.DISABLED.getCode());
        Mockito.when(paymentInstrumentRepository.save(
                paymentInstrumentDocument))
                .thenReturn(Mono.just(paymentInstrumentDocument));

        PaymentMethod paymentInstrumentPatched = paymentInstrumentService
                .patchPaymentInstrument(paymentMethodID.value().toString(), PaymentInstrumentStatusEnum.DISABLED)
                .block();

        assertEquals(paymentInstrumentPatched.getPaymentInstrumentCategoryID().value(),
                paymentInstrumentCategoryID.value());

        assertEquals(paymentInstrumentPatched.getPaymentMethodStatus().value(),
                PaymentInstrumentStatusEnum.DISABLED);
    }

    @Test
    void shouldRetrivePaymentInstrumentById() {
        String paymentInstrumentNameAsString = "paymentInstrumentName";
        String paymentInstrumentDescriptionAsString = "paymentInstrumentDescription";

        PaymentMethodName paymentMethodName = new PaymentMethodName(paymentInstrumentNameAsString);
        PaymentMethodDescription paymentMethodDescription = new PaymentMethodDescription(
                paymentInstrumentDescriptionAsString);
        PaymentInstrumentCategoryID paymentInstrumentCategoryID = new PaymentInstrumentCategoryID(
                UUID.fromString(UUID.randomUUID().toString()));
        PaymentMethodID paymentMethodID = new PaymentMethodID(UUID.randomUUID());
        PaymentMethodStatus paymentMethodStatus = new PaymentMethodStatus(
                PaymentInstrumentStatusEnum.ENABLED);
        PaymentInstrumentCategoryName paymentInstrumentCategoryName =
                new PaymentInstrumentCategoryName("paymentInstrumentCategoryName");
        List<PaymentMethodType> paymentMethodType = List.of(new PaymentMethodType("PO"));
        PaymentMethodType paymentMethodTypeCode = new PaymentMethodType("test");

        PaymentMethod paymentInstrument = new PaymentMethod(paymentMethodID,
                paymentMethodName,
                paymentMethodDescription,
                paymentMethodStatus,
                paymentInstrumentCategoryID,
                paymentInstrumentCategoryName,
                paymentMethodType,
                paymentMethodTypeCode);

        PaymentInstrumentDocument paymentInstrumentDocument = new PaymentInstrumentDocument(
                paymentInstrument.getPaymentMethodID().value().toString(),
                paymentInstrument.getPaymentMethodName().value(),
                paymentInstrument.getPaymentMethodDescription().value(),
                paymentInstrument.getPaymentMethodStatus().value().toString(),
                paymentInstrument.getPaymentInstrumentCategoryID().value().toString(),
                paymentInstrument.getPaymentInstrumentCategoryName().value(),
                paymentInstrument.getPaymentInstrumentCategoryTypes().stream()
                        .map(PaymentMethodType::value).collect(Collectors.toList()),
                paymentMethodTypeCode.value());

        Mockito.when(paymentInstrumentRepository.findById(paymentMethodID.value().toString()))
                .thenReturn(Mono.just(paymentInstrumentDocument));

        PaymentMethod paymentInstrumentCreated = paymentInstrumentService
                .retrivePaymentInstrumentById(paymentMethodID.value().toString()).block();

        assertEquals(paymentInstrumentCreated.getPaymentInstrumentCategoryID().value(),
                paymentInstrumentCategoryID.value());
    }
}
