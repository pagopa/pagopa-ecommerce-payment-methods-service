package it.pagopa.ecommerce.payment.instruments.domain.aggregates;

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
public class PaymentMethodFactoryTests {

    @Mock
    private PaymentMethodRepository paymentMethodRepository;

    @InjectMocks
    private PaymentMethodFactory paymentInstrumentFactory;

    // TODO: fix tests
    /*
    @Test
    void shouldCreateNewInstrument(){
        PaymentMethodID paymentMethodID = new PaymentMethodID(UUID.randomUUID());
        PaymentMethodName paymentMethodName = new PaymentMethodName("Test name");
        PaymentMethodDescription paymentMethodDescription = new PaymentMethodDescription("Test desc");
        PaymentMethodStatus paymentMethodStatus = new PaymentMethodStatus(
                PaymentMethodStatusEnum.ENABLED);
        PaymentInstrumentCategoryID paymentInstrumentCategoryID = new PaymentInstrumentCategoryID(UUID.randomUUID());
        PaymentMethodType paymentMethodTypeCode = new PaymentMethodType("testCode");

        Mockito.when(paymentMethodRepository.findByPaymentMethodName(paymentMethodName.value()))
                .thenReturn(Flux.empty());

        Mockito.when(paymentInstrumentCategoryRepository.findById(paymentInstrumentCategoryID.value().toString()))
                .thenReturn(Mono.just(
                        new PaymentInstrumentCategoryDocument(
                                paymentInstrumentCategoryID.value().toString(),
                                "test name",
                                List.of("test")
                        )
                ));

        PaymentMethod newPaymentInstrument = paymentInstrumentFactory.newPaymentMethod(
                paymentMethodID,
                paymentMethodName,
                paymentMethodDescription,
                paymentMethodStatus,
                paymentInstrumentCategoryID,
                paymentMethodTypeCode
        ).block();

        assertNotNull(newPaymentInstrument);
    }

    @Test
    void shouldThrowInvalidCategory(){
        PaymentMethodID paymentMethodID = new PaymentMethodID(UUID.randomUUID());
        PaymentMethodName paymentMethodName = new PaymentMethodName("Test name");
        PaymentMethodDescription paymentMethodDescription = new PaymentMethodDescription("Test desc");
        PaymentMethodStatus paymentMethodStatus = new PaymentMethodStatus(
                PaymentMethodStatusEnum.ENABLED);
        PaymentInstrumentCategoryID paymentInstrumentCategoryID = new PaymentInstrumentCategoryID(UUID.randomUUID());
        PaymentMethodType paymentMethodTypeCode = new PaymentMethodType("testCode");

        Mockito.when(paymentMethodRepository.findByPaymentMethodName(paymentMethodName.value()))
                .thenReturn(Flux.empty());

        Mockito.when(paymentInstrumentCategoryRepository.findById(paymentInstrumentCategoryID.value().toString()))
                .thenReturn(Mono.empty());

        assertThrows(CategoryNotFoundException.class,
                () -> paymentInstrumentFactory.newPaymentMethod(
                        paymentMethodID,
                        paymentMethodName,
                        paymentMethodDescription,
                        paymentMethodStatus,
                        paymentInstrumentCategoryID,
                        paymentMethodTypeCode
                ).block());
    }

    @Test
    void shouldThrowDuplicatedInstrumentException(){
        PaymentMethodID paymentMethodID = new PaymentMethodID(UUID.randomUUID());
        PaymentMethodName paymentMethodName = new PaymentMethodName("Test name");
        PaymentMethodDescription paymentMethodDescription = new PaymentMethodDescription("Test desc");
        PaymentMethodStatus paymentMethodStatus = new PaymentMethodStatus(
                PaymentMethodStatusEnum.ENABLED);
        PaymentInstrumentCategoryID paymentInstrumentCategoryID = new PaymentInstrumentCategoryID(UUID.randomUUID());
        PaymentMethodType paymentMethodTypeCode = new PaymentMethodType("testCode");

        Mockito.when(paymentMethodRepository.findByPaymentMethodName(paymentMethodName.value()))
                .thenReturn(Flux.just(
                        new PaymentMethodDocument(
                                paymentMethodID.value().toString(),
                                paymentMethodName.value(),
                                paymentMethodDescription.value(),
                                paymentMethodStatus.value().toString(),
                                paymentInstrumentCategoryID.value().toString(),
                                "test name",
                                List.of("test"),
                                paymentMethodTypeCode.value()
                                )
                ));

        Mockito.when(paymentInstrumentCategoryRepository.findById(paymentInstrumentCategoryID.value().toString()))
                .thenReturn(Mono.just(
                        new PaymentInstrumentCategoryDocument(
                                paymentInstrumentCategoryID.value().toString(),
                                "test name",
                                List.of("test")
                        )
                ));

        assertThrows(PaymentInstrumentAlreadyInUseException.class,
                () -> paymentInstrumentFactory.newPaymentMethod(
                        paymentMethodID,
                        paymentMethodName,
                        paymentMethodDescription,
                        paymentMethodStatus,
                        paymentInstrumentCategoryID,
                        paymentMethodTypeCode
                ).block());
    }
     */
}
