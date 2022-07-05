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

import static it.pagopa.ecommerce.payment.instruments.exception.CategoryNotFoundException.categoryNotFoundException;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:application.test.properties")
@ExtendWith(MockitoExtension.class)
public class PaymentInstrumentFactoryTests {

    @Mock
    private PaymentInstrumentCategoryRepository paymentInstrumentCategoryRepository;

    @Mock
    private PaymentInstrumentRepository paymentInstrumentRepository;

    @InjectMocks
    private PaymentInstrumentFactory paymentInstrumentFactory;

    @Test
    void shouldCreateNewInstrument(){
        PaymentInstrumentID paymentInstrumentID = new PaymentInstrumentID(UUID.randomUUID());
        PaymentInstrumentName paymentInstrumentName = new PaymentInstrumentName("Test name");
        PaymentInstrumentDescription paymentInstrumentDescription = new PaymentInstrumentDescription("Test desc");
        PaymentInstrumentStatus paymentInstrumentStatus = new PaymentInstrumentStatus(
                PaymentInstrumentStatusEnum.ENABLED);
        PaymentInstrumentCategoryID paymentInstrumentCategoryID = new PaymentInstrumentCategoryID(UUID.randomUUID());

        Mockito.when(paymentInstrumentRepository.findByPaymentInstrumentName(paymentInstrumentName.value()))
                .thenReturn(Flux.empty());

        Mockito.when(paymentInstrumentCategoryRepository.findById(paymentInstrumentCategoryID.value().toString()))
                .thenReturn(Mono.just(
                        new PaymentInstrumentCategoryDocument(
                                paymentInstrumentCategoryID.value().toString(),
                                "test name",
                                List.of("test")
                        )
                ));

        PaymentInstrument newPaymentInstrument = paymentInstrumentFactory.newPaymentInstrument(
                paymentInstrumentID,
                paymentInstrumentName,
                paymentInstrumentDescription,
                paymentInstrumentStatus,
                paymentInstrumentCategoryID
        ).block();

        assertNotNull(newPaymentInstrument);
    }

    @Test
    void shouldThrowInvalidCategory(){
        PaymentInstrumentID paymentInstrumentID = new PaymentInstrumentID(UUID.randomUUID());
        PaymentInstrumentName paymentInstrumentName = new PaymentInstrumentName("Test name");
        PaymentInstrumentDescription paymentInstrumentDescription = new PaymentInstrumentDescription("Test desc");
        PaymentInstrumentStatus paymentInstrumentStatus = new PaymentInstrumentStatus(
                PaymentInstrumentStatusEnum.ENABLED);
        PaymentInstrumentCategoryID paymentInstrumentCategoryID = new PaymentInstrumentCategoryID(UUID.randomUUID());

        Mockito.when(paymentInstrumentRepository.findByPaymentInstrumentName(paymentInstrumentName.value()))
                .thenReturn(Flux.empty());

        Mockito.when(paymentInstrumentCategoryRepository.findById(paymentInstrumentCategoryID.value().toString()))
                .thenReturn(Mono.empty());

        assertThrows(CategoryNotFoundException.class,
                () -> paymentInstrumentFactory.newPaymentInstrument(
                        paymentInstrumentID,
                        paymentInstrumentName,
                        paymentInstrumentDescription,
                        paymentInstrumentStatus,
                        paymentInstrumentCategoryID
                ).block());
    }

    @Test
    void shouldThrowDuplicatedInstrumentException(){
        PaymentInstrumentID paymentInstrumentID = new PaymentInstrumentID(UUID.randomUUID());
        PaymentInstrumentName paymentInstrumentName = new PaymentInstrumentName("Test name");
        PaymentInstrumentDescription paymentInstrumentDescription = new PaymentInstrumentDescription("Test desc");
        PaymentInstrumentStatus paymentInstrumentStatus = new PaymentInstrumentStatus(
                PaymentInstrumentStatusEnum.ENABLED);
        PaymentInstrumentCategoryID paymentInstrumentCategoryID = new PaymentInstrumentCategoryID(UUID.randomUUID());

        Mockito.when(paymentInstrumentRepository.findByPaymentInstrumentName(paymentInstrumentName.value()))
                .thenReturn(Flux.just(
                        new PaymentInstrumentDocument(
                                paymentInstrumentID.value().toString(),
                                paymentInstrumentName.value(),
                                paymentInstrumentDescription.value(),
                                paymentInstrumentStatus.value().toString(),
                                paymentInstrumentCategoryID.value().toString(),
                                "test name",
                                List.of("test")
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
                        paymentInstrumentID,
                        paymentInstrumentName,
                        paymentInstrumentDescription,
                        paymentInstrumentStatus,
                        paymentInstrumentCategoryID
                ).block());
    }

}
