package it.pagopa.ecommerce.payment.instruments.service;

import it.pagopa.ecommerce.payment.instruments.application.PaymentInstrumentService;
import it.pagopa.ecommerce.payment.instruments.domain.aggregates.PaymentInstrument;
import it.pagopa.ecommerce.payment.instruments.domain.aggregates.PaymentInstrumentFactory;
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
class PaymentInstrumentServiceTests {

    @Mock
    private PaymentInstrumentRepository paymentInstrumentRepository;

    @Mock
    private PaymentInstrumentFactory paymentInstrumentFactory;

    @InjectMocks
    private PaymentInstrumentService paymentInstrumentService;

    @Mock
    private PaymentInstrumentCategoryRepository paymentInstrumentCategoryRepository;

    @Test
    void shouldCreatePaymentInstrument() {

        String paymentInstrumentNameAsString = "paymentInstrumentName";
        String paymentInstrumentDescriptionAsString = "paymentInstrumentDescription";

        PaymentInstrumentName paymentInstrumentName = new PaymentInstrumentName(paymentInstrumentNameAsString);
        PaymentInstrumentDescription paymentInstrumentDescription = new PaymentInstrumentDescription(
                paymentInstrumentDescriptionAsString);
        PaymentInstrumentCategoryID paymentInstrumentCategoryID = new PaymentInstrumentCategoryID(
                UUID.fromString(UUID.randomUUID().toString()));
        PaymentInstrumentID paymentInstrumentID = new PaymentInstrumentID(UUID.randomUUID());
        PaymentInstrumentStatus paymentInstrumentStatus = new PaymentInstrumentStatus(
                PaymentInstrumentStatusEnum.ENABLED);
        PaymentInstrumentCategoryName paymentInstrumentCategoryName =
                new PaymentInstrumentCategoryName("paymentInstrumentCategoryName");
        List<PaymentInstrumentType> paymentInstrumentType = List.of(new PaymentInstrumentType("PO"));

        PaymentInstrument paymentInstrument = new PaymentInstrument(paymentInstrumentID,
                paymentInstrumentName,
                paymentInstrumentDescription,
                paymentInstrumentStatus,
                paymentInstrumentCategoryID,
                paymentInstrumentCategoryName,
                paymentInstrumentType);

        PaymentInstrumentDocument paymentInstrumentDocument = new PaymentInstrumentDocument(
                paymentInstrument.getPaymentInstrumentID().value().toString(),
                paymentInstrument.getPaymentInstrumentName().value(),
                paymentInstrument.getPaymentInstrumentDescription().value(),
                paymentInstrument.getPaymentInstrumentStatus().value().toString(),
                paymentInstrument.getPaymentInstrumentCategoryID().value().toString(),
                paymentInstrument.getPaymentInstrumentCategoryName().value(),
                paymentInstrument.getPaymentInstrumentCategoryTypes().stream()
                        .map(PaymentInstrumentType::value).collect(Collectors.toList()));

        Mockito.when(paymentInstrumentFactory.newPaymentInstrument(
                any(),
                eq(paymentInstrumentName),
                eq(paymentInstrumentDescription),
                eq(paymentInstrumentStatus),
                eq(paymentInstrumentCategoryID)))
                .thenReturn(Mono.just(paymentInstrument));

        Mockito.when(paymentInstrumentRepository.save(
                        paymentInstrumentDocument))
                .thenReturn(Mono.just(paymentInstrumentDocument));

        PaymentInstrument paymentInstrumentCreated = paymentInstrumentService.createPaymentInstrument(
                paymentInstrumentNameAsString,
                paymentInstrumentDescriptionAsString,
                paymentInstrumentCategoryID.value().toString()).block();
        assertEquals(paymentInstrumentCreated.getPaymentInstrumentCategoryID().value(),
                paymentInstrumentCategoryID.value());
    }

    @Test
    void shouldRetrievePaymentInstruments() {

        String paymentInstrumentNameAsString = "paymentInstrumentName";
        String paymentInstrumentDescriptionAsString = "paymentInstrumentDescription";

        PaymentInstrumentName paymentInstrumentName = new PaymentInstrumentName(paymentInstrumentNameAsString);
        PaymentInstrumentDescription paymentInstrumentDescription = new PaymentInstrumentDescription(
                paymentInstrumentDescriptionAsString);
        PaymentInstrumentCategoryID paymentInstrumentCategoryID = new PaymentInstrumentCategoryID(
                UUID.fromString(UUID.randomUUID().toString()));
        PaymentInstrumentID paymentInstrumentID = new PaymentInstrumentID(UUID.randomUUID());
        PaymentInstrumentStatus paymentInstrumentStatus = new PaymentInstrumentStatus(
                PaymentInstrumentStatusEnum.ENABLED);
        PaymentInstrumentCategoryName paymentInstrumentCategoryName =
                new PaymentInstrumentCategoryName("paymentInstrumentCategoryName");
        List<PaymentInstrumentType> paymentInstrumentType = List.of(new PaymentInstrumentType("PO"));

        PaymentInstrument paymentInstrument = new PaymentInstrument(paymentInstrumentID,
                paymentInstrumentName,
                paymentInstrumentDescription,
                paymentInstrumentStatus,
                paymentInstrumentCategoryID,
                paymentInstrumentCategoryName,
                paymentInstrumentType);

        PaymentInstrumentDocument paymentInstrumentDocument = new PaymentInstrumentDocument(
                paymentInstrument.getPaymentInstrumentID().value().toString(),
                paymentInstrument.getPaymentInstrumentName().value(),
                paymentInstrument.getPaymentInstrumentDescription().value(),
                paymentInstrument.getPaymentInstrumentStatus().value().toString(),
                paymentInstrument.getPaymentInstrumentCategoryID().value().toString(),
                paymentInstrument.getPaymentInstrumentCategoryName().value(),
                paymentInstrument.getPaymentInstrumentCategoryTypes().stream()
                        .map(PaymentInstrumentType::value).collect(Collectors.toList()));
        Mockito.when(paymentInstrumentRepository.findAll())
                .thenReturn(Flux.just(paymentInstrumentDocument));

        PaymentInstrument paymentInstrumentCreated = paymentInstrumentService.retrivePaymentInstruments().blockFirst();

        assertEquals(paymentInstrumentCreated.getPaymentInstrumentCategoryID().value(),
                paymentInstrumentCategoryID.value());
    }

    @Test
    void shouldPatchPaymentInstrument() {
        String paymentInstrumentNameAsString = "paymentInstrumentName";
        String paymentInstrumentDescriptionAsString = "paymentInstrumentDescription";

        PaymentInstrumentName paymentInstrumentName = new PaymentInstrumentName(paymentInstrumentNameAsString);
        PaymentInstrumentDescription paymentInstrumentDescription = new PaymentInstrumentDescription(
                paymentInstrumentDescriptionAsString);
        PaymentInstrumentCategoryID paymentInstrumentCategoryID = new PaymentInstrumentCategoryID(
                UUID.fromString(UUID.randomUUID().toString()));
        PaymentInstrumentID paymentInstrumentID = new PaymentInstrumentID(UUID.randomUUID());
        PaymentInstrumentStatus paymentInstrumentStatus = new PaymentInstrumentStatus(
                PaymentInstrumentStatusEnum.ENABLED);
        PaymentInstrumentCategoryName paymentInstrumentCategoryName =
                new PaymentInstrumentCategoryName("paymentInstrumentCategoryName");
        List<PaymentInstrumentType> paymentInstrumentType = List.of(new PaymentInstrumentType("PO"));

        PaymentInstrument paymentInstrument = new PaymentInstrument(paymentInstrumentID,
                paymentInstrumentName,
                paymentInstrumentDescription,
                paymentInstrumentStatus,
                paymentInstrumentCategoryID,
                paymentInstrumentCategoryName,
                paymentInstrumentType);

        PaymentInstrumentDocument paymentInstrumentDocument = new PaymentInstrumentDocument(
                paymentInstrument.getPaymentInstrumentID().value().toString(),
                paymentInstrument.getPaymentInstrumentName().value(),
                paymentInstrument.getPaymentInstrumentDescription().value(),
                paymentInstrument.getPaymentInstrumentStatus().value().toString(),
                paymentInstrument.getPaymentInstrumentCategoryID().value().toString(),
                paymentInstrument.getPaymentInstrumentCategoryName().value(),
                paymentInstrument.getPaymentInstrumentCategoryTypes().stream()
                        .map(PaymentInstrumentType::value).collect(Collectors.toList()));
        Mockito.when(paymentInstrumentRepository.findById(paymentInstrumentID.value().toString()))
                .thenReturn(Mono.just(paymentInstrumentDocument));

        paymentInstrumentDocument.setPaymentInstrumentStatus(PaymentInstrumentStatusEnum.DISABLED.getCode());
        Mockito.when(paymentInstrumentRepository.save(
                paymentInstrumentDocument))
                .thenReturn(Mono.just(paymentInstrumentDocument));

        PaymentInstrument paymentInstrumentPatched = paymentInstrumentService
                .patchPaymentInstrument(paymentInstrumentID.value().toString(), PaymentInstrumentStatusEnum.DISABLED)
                .block();

        assertEquals(paymentInstrumentPatched.getPaymentInstrumentCategoryID().value(),
                paymentInstrumentCategoryID.value());

        assertEquals(paymentInstrumentPatched.getPaymentInstrumentStatus().value(),
                PaymentInstrumentStatusEnum.DISABLED);
    }

    @Test
    void shouldRetrivePaymentInstrumentById() {
        String paymentInstrumentNameAsString = "paymentInstrumentName";
        String paymentInstrumentDescriptionAsString = "paymentInstrumentDescription";

        PaymentInstrumentName paymentInstrumentName = new PaymentInstrumentName(paymentInstrumentNameAsString);
        PaymentInstrumentDescription paymentInstrumentDescription = new PaymentInstrumentDescription(
                paymentInstrumentDescriptionAsString);
        PaymentInstrumentCategoryID paymentInstrumentCategoryID = new PaymentInstrumentCategoryID(
                UUID.fromString(UUID.randomUUID().toString()));
        PaymentInstrumentID paymentInstrumentID = new PaymentInstrumentID(UUID.randomUUID());
        PaymentInstrumentStatus paymentInstrumentStatus = new PaymentInstrumentStatus(
                PaymentInstrumentStatusEnum.ENABLED);
        PaymentInstrumentCategoryName paymentInstrumentCategoryName =
                new PaymentInstrumentCategoryName("paymentInstrumentCategoryName");
        List<PaymentInstrumentType> paymentInstrumentType = List.of(new PaymentInstrumentType("PO"));

        PaymentInstrument paymentInstrument = new PaymentInstrument(paymentInstrumentID,
                paymentInstrumentName,
                paymentInstrumentDescription,
                paymentInstrumentStatus,
                paymentInstrumentCategoryID,
                paymentInstrumentCategoryName,
                paymentInstrumentType);

        PaymentInstrumentDocument paymentInstrumentDocument = new PaymentInstrumentDocument(
                paymentInstrument.getPaymentInstrumentID().value().toString(),
                paymentInstrument.getPaymentInstrumentName().value(),
                paymentInstrument.getPaymentInstrumentDescription().value(),
                paymentInstrument.getPaymentInstrumentStatus().value().toString(),
                paymentInstrument.getPaymentInstrumentCategoryID().value().toString(),
                paymentInstrument.getPaymentInstrumentCategoryName().value(),
                paymentInstrument.getPaymentInstrumentCategoryTypes().stream()
                        .map(PaymentInstrumentType::value).collect(Collectors.toList()));

        Mockito.when(paymentInstrumentRepository.findById(paymentInstrumentID.value().toString()))
                .thenReturn(Mono.just(paymentInstrumentDocument));

        PaymentInstrument paymentInstrumentCreated = paymentInstrumentService
                .retrivePaymentInstrumentById(paymentInstrumentID.value().toString()).block();

        assertEquals(paymentInstrumentCreated.getPaymentInstrumentCategoryID().value(),
                paymentInstrumentCategoryID.value());
    }
}
