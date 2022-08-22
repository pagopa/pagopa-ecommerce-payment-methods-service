package it.pagopa.ecommerce.payment.instruments.domain.aggregates;

import it.pagopa.ecommerce.payment.instruments.domain.valueobjects.*;
import it.pagopa.ecommerce.payment.instruments.exception.CategoryNotFoundException;
import it.pagopa.ecommerce.payment.instruments.exception.PaymentInstrumentAlreadyInUseException;
import it.pagopa.ecommerce.payment.instruments.infrastructure.PaymentInstrumentCategoryDocument;
import it.pagopa.ecommerce.payment.instruments.infrastructure.PaymentInstrumentCategoryRepository;
import it.pagopa.ecommerce.payment.instruments.infrastructure.PaymentInstrumentDocument;
import it.pagopa.ecommerce.payment.instruments.infrastructure.PaymentInstrumentRepository;
import it.pagopa.ecommerce.payment.instruments.utils.PaymentInstrumentStatusEnum;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:application.test.properties")
@ExtendWith(MockitoExtension.class)
public class PaymentMethodFactoryTests {

    @Mock
    private PaymentInstrumentCategoryRepository paymentInstrumentCategoryRepository;

    @Mock
    private PaymentInstrumentRepository paymentInstrumentRepository;

    @InjectMocks
    private PaymentMethodFactory paymentInstrumentFactory;

    @Test
    void shouldCreateNewInstrument(){
        PaymentMethodID paymentMethodID = new PaymentMethodID(UUID.randomUUID());
        PaymentMethodName paymentMethodName = new PaymentMethodName("Test name");
        PaymentMethodDescription paymentMethodDescription = new PaymentMethodDescription("Test desc");
        PaymentMethodStatus paymentMethodStatus = new PaymentMethodStatus(
                PaymentInstrumentStatusEnum.ENABLED);
        PaymentInstrumentCategoryID paymentInstrumentCategoryID = new PaymentInstrumentCategoryID(UUID.randomUUID());
        PaymentMethodType paymentMethodTypeCode = new PaymentMethodType("testCode");

        Mockito.when(paymentInstrumentRepository.findByPaymentInstrumentName(paymentMethodName.value()))
                .thenReturn(Flux.empty());

        Mockito.when(paymentInstrumentCategoryRepository.findById(paymentInstrumentCategoryID.value().toString()))
                .thenReturn(Mono.just(
                        new PaymentInstrumentCategoryDocument(
                                paymentInstrumentCategoryID.value().toString(),
                                "test name",
                                List.of("test")
                        )
                ));

        PaymentMethod newPaymentInstrument = paymentInstrumentFactory.newPaymentInstrument(
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
                PaymentInstrumentStatusEnum.ENABLED);
        PaymentInstrumentCategoryID paymentInstrumentCategoryID = new PaymentInstrumentCategoryID(UUID.randomUUID());
        PaymentMethodType paymentMethodTypeCode = new PaymentMethodType("testCode");

        Mockito.when(paymentInstrumentRepository.findByPaymentInstrumentName(paymentMethodName.value()))
                .thenReturn(Flux.empty());

        Mockito.when(paymentInstrumentCategoryRepository.findById(paymentInstrumentCategoryID.value().toString()))
                .thenReturn(Mono.empty());

        assertThrows(CategoryNotFoundException.class,
                () -> paymentInstrumentFactory.newPaymentInstrument(
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
                PaymentInstrumentStatusEnum.ENABLED);
        PaymentInstrumentCategoryID paymentInstrumentCategoryID = new PaymentInstrumentCategoryID(UUID.randomUUID());
        PaymentMethodType paymentMethodTypeCode = new PaymentMethodType("testCode");

        Mockito.when(paymentInstrumentRepository.findByPaymentInstrumentName(paymentMethodName.value()))
                .thenReturn(Flux.just(
                        new PaymentInstrumentDocument(
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
                () -> paymentInstrumentFactory.newPaymentInstrument(
                        paymentMethodID,
                        paymentMethodName,
                        paymentMethodDescription,
                        paymentMethodStatus,
                        paymentInstrumentCategoryID,
                        paymentMethodTypeCode
                ).block());
    }

}
