package it.pagopa.ecommerce.payment.instruments.domain.aggregates;

import it.pagopa.ecommerce.payment.instruments.exception.PaymentMethodAlreadyInUseException;
import it.pagopa.ecommerce.payment.instruments.infrastructure.PaymentMethodDocument;
import it.pagopa.ecommerce.payment.instruments.infrastructure.PaymentMethodRepository;
import it.pagopa.ecommerce.payment.instruments.utils.TestUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.util.Pair;
import org.springframework.test.context.TestPropertySource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@TestPropertySource(locations = "classpath:application.test.properties")
@ExtendWith(MockitoExtension.class)
public class PaymentMethodFactoryTests {

    @Mock
    private PaymentMethodRepository paymentMethodRepository;

    @InjectMocks
    private PaymentMethodFactory paymentInstrumentFactory;

    @Test
    void shouldCreateNewInstrument(){

        PaymentMethod paymentMethod = TestUtil.getPaymentMethod();
        Mockito.when(paymentMethodRepository.findByPaymentMethodNameOrPaymentMethodTypeCode(
                paymentMethod.getPaymentMethodName().value(), paymentMethod.getPaymentMethodTypeCode().value()))
                .thenReturn(Mono.empty());


        PaymentMethod paymentMethodProduct = paymentInstrumentFactory.newPaymentMethod(
                paymentMethod.getPaymentMethodID(),
                paymentMethod.getPaymentMethodName(),
                paymentMethod.getPaymentMethodDescription(),
                paymentMethod.getPaymentMethodStatus(),
                paymentMethod.getPaymentMethodRanges(),
                paymentMethod.getPaymentMethodTypeCode()
        ).block();

        assertNotNull(paymentMethodProduct);
    }

    @Test
    void shouldThrowDuplicatedInstrumentException(){
        PaymentMethod paymentMethod = TestUtil.getPaymentMethod();

        Mockito.when(paymentMethodRepository.findByPaymentMethodNameOrPaymentMethodTypeCode(
                paymentMethod.getPaymentMethodName().value(), paymentMethod.getPaymentMethodTypeCode().value()))
                .thenReturn(Mono.just(
                        new PaymentMethodDocument(
                                paymentMethod.getPaymentMethodID().value().toString(),
                                paymentMethod.getPaymentMethodName().value(),
                                paymentMethod.getPaymentMethodDescription().value(),
                                paymentMethod.getPaymentMethodStatus().value().toString(),
                                paymentMethod.getPaymentMethodRanges().stream().map(r -> Pair.of(r.min(), r.max()))
                                        .collect(Collectors.toList()),
                                paymentMethod.getPaymentMethodTypeCode().value()
                                )
                ));

        assertThrows(PaymentMethodAlreadyInUseException.class,
                () -> paymentInstrumentFactory.newPaymentMethod(
                        paymentMethod.getPaymentMethodID(),
                        paymentMethod.getPaymentMethodName(),
                        paymentMethod.getPaymentMethodDescription(),
                        paymentMethod.getPaymentMethodStatus(),
                        paymentMethod.getPaymentMethodRanges(),
                        paymentMethod.getPaymentMethodTypeCode()
                ).block());
    }
}
