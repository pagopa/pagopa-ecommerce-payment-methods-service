package it.pagopa.ecommerce.payment.instruments.service;

import it.pagopa.ecommerce.payment.instruments.domain.aggregates.PaymentMethodFactory;
import it.pagopa.ecommerce.payment.instruments.infrastructure.PaymentMethodRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(locations = "classpath:application.test.properties")
@ExtendWith(MockitoExtension.class)
class PaymentMethodServiceTests {

    @Mock
    private PaymentMethodRepository paymentMethodRepository;

    @Mock
    private PaymentMethodFactory paymentInstrumentFactory;


    // TODO: fix tests
    /*
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
                PaymentMethodStatusEnum.ENABLED);
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

        PaymentMethodDocument paymentMethodDocument = new PaymentMethodDocument(
                paymentInstrument.getPaymentMethodID().value().toString(),
                paymentInstrument.getPaymentMethodName().value(),
                paymentInstrument.getPaymentMethodDescription().value(),
                paymentInstrument.getPaymentMethodStatus().value().toString(),
                paymentInstrument.getPaymentInstrumentCategoryID().value().toString(),
                paymentInstrument.getPaymentInstrumentCategoryName().value(),
                paymentInstrument.getPaymentInstrumentCategoryTypes().stream()
                        .map(PaymentMethodType::value).collect(Collectors.toList()),
                paymentInstrument.getPaymentMethodTypeCode().value());

        Mockito.when(paymentInstrumentFactory.newPaymentMethod(
                        any(), any(), any(), any(), any(), any())
                )
                .thenReturn(Mono.just(paymentInstrument));

        Mockito.when(paymentMethodRepository.save(
                        paymentMethodDocument))
                .thenReturn(Mono.just(paymentMethodDocument));

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
                PaymentMethodStatusEnum.ENABLED);
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

        PaymentMethodDocument paymentMethodDocument = new PaymentMethodDocument(
                paymentInstrument.getPaymentMethodID().value().toString(),
                paymentInstrument.getPaymentMethodName().value(),
                paymentInstrument.getPaymentMethodDescription().value(),
                paymentInstrument.getPaymentMethodStatus().value().toString(),
                paymentInstrument.getPaymentInstrumentCategoryID().value().toString(),
                paymentInstrument.getPaymentInstrumentCategoryName().value(),
                paymentInstrument.getPaymentInstrumentCategoryTypes().stream()
                        .map(PaymentMethodType::value).collect(Collectors.toList()),
                paymentMethodTypeCode.value());
        Mockito.when(paymentMethodRepository.findAll())
                .thenReturn(Flux.just(paymentMethodDocument));

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
                PaymentMethodStatusEnum.ENABLED);
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

        PaymentMethodDocument paymentMethodDocument = new PaymentMethodDocument(
                paymentInstrument.getPaymentMethodID().value().toString(),
                paymentInstrument.getPaymentMethodName().value(),
                paymentInstrument.getPaymentMethodDescription().value(),
                paymentInstrument.getPaymentMethodStatus().value().toString(),
                paymentInstrument.getPaymentInstrumentCategoryID().value().toString(),
                paymentInstrument.getPaymentInstrumentCategoryName().value(),
                paymentInstrument.getPaymentInstrumentCategoryTypes().stream()
                        .map(PaymentMethodType::value).collect(Collectors.toList()),
                paymentMethodTypeCode.value());
        Mockito.when(paymentMethodRepository.findById(paymentMethodID.value().toString()))
                .thenReturn(Mono.just(paymentMethodDocument));

        paymentMethodDocument.setPaymentMethodStatus(PaymentMethodStatusEnum.DISABLED.getCode());
        Mockito.when(paymentMethodRepository.save(
                        paymentMethodDocument))
                .thenReturn(Mono.just(paymentMethodDocument));

        PaymentMethod paymentInstrumentPatched = paymentInstrumentService
                .patchPaymentInstrument(paymentMethodID.value().toString(), PaymentMethodStatusEnum.DISABLED)
                .block();

        assertEquals(paymentInstrumentPatched.getPaymentInstrumentCategoryID().value(),
                paymentInstrumentCategoryID.value());

        assertEquals(paymentInstrumentPatched.getPaymentMethodStatus().value(),
                PaymentMethodStatusEnum.DISABLED);
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
                PaymentMethodStatusEnum.ENABLED);
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

        PaymentMethodDocument paymentMethodDocument = new PaymentMethodDocument(
                paymentInstrument.getPaymentMethodID().value().toString(),
                paymentInstrument.getPaymentMethodName().value(),
                paymentInstrument.getPaymentMethodDescription().value(),
                paymentInstrument.getPaymentMethodStatus().value().toString(),
                paymentInstrument.getPaymentInstrumentCategoryID().value().toString(),
                paymentInstrument.getPaymentInstrumentCategoryName().value(),
                paymentInstrument.getPaymentInstrumentCategoryTypes().stream()
                        .map(PaymentMethodType::value).collect(Collectors.toList()),
                paymentMethodTypeCode.value());

        Mockito.when(paymentMethodRepository.findById(paymentMethodID.value().toString()))
                .thenReturn(Mono.just(paymentMethodDocument));

        PaymentMethod paymentInstrumentCreated = paymentInstrumentService
                .retrivePaymentInstrumentById(paymentMethodID.value().toString()).block();

        assertEquals(paymentInstrumentCreated.getPaymentInstrumentCategoryID().value(),
                paymentInstrumentCategoryID.value());
    }
     */
}
