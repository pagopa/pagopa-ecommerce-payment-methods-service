package it.pagopa.ecommerce.payment.methods.domain.aggregates;

import it.pagopa.ecommerce.payment.methods.exception.PaymentMethodAlreadyInUseException;
import it.pagopa.ecommerce.payment.methods.infrastructure.PaymentMethodDocument;
import it.pagopa.ecommerce.payment.methods.infrastructure.PaymentMethodRepository;
import it.pagopa.ecommerce.payment.methods.utils.TestUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.util.Pair;
import org.springframework.test.context.TestPropertySource;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@TestPropertySource(locations = "classpath:application.test.properties")
@ExtendWith(MockitoExtension.class)
class PaymentMethodFactoryTests {

    @Mock
    private PaymentMethodRepository paymentMethodRepository;

    @InjectMocks
    private PaymentMethodFactory paymentMethodFactory;

    @Test
    void shouldCreateNewmethod() {

        PaymentMethod paymentMethod = TestUtil.getPaymentMethod();
        Mockito.when(
                paymentMethodRepository.findByPaymentMethodNameOrPaymentMethodTypeCode(
                        paymentMethod.getPaymentMethodName().value(),
                        paymentMethod.getPaymentMethodTypeCode().value()
                )
        ).thenReturn(Mono.empty());

        PaymentMethod paymentMethodProduct = paymentMethodFactory.newPaymentMethod(
                paymentMethod.getPaymentMethodID(),
                paymentMethod.getPaymentMethodName(),
                paymentMethod.getPaymentMethodDescription(),
                paymentMethod.getPaymentMethodStatus(),
                paymentMethod.getPaymentMethodRanges(),
                paymentMethod.getPaymentMethodTypeCode(),
                paymentMethod.getPaymentMethodAsset()
        ).block();

        assertNotNull(paymentMethodProduct);
    }

    @Test
    void shouldThrowDuplicatedMethodException() {
        PaymentMethod paymentMethod = TestUtil.getPaymentMethod();

        Mockito.when(
                paymentMethodRepository.findByPaymentMethodNameOrPaymentMethodTypeCode(
                        paymentMethod.getPaymentMethodName().value(),
                        paymentMethod.getPaymentMethodTypeCode().value()
                )
        ).thenReturn(
                Mono.just(
                        new PaymentMethodDocument(
                                paymentMethod.getPaymentMethodID().value().toString(),
                                paymentMethod.getPaymentMethodName().value(),
                                paymentMethod.getPaymentMethodDescription().value(),
                                paymentMethod.getPaymentMethodStatus().value().toString(),
                                paymentMethod.getPaymentMethodAsset().value(),
                                paymentMethod.getPaymentMethodRanges().stream()
                                        .map(r -> Pair.of(r.min(), r.max()))
                                        .collect(Collectors.toList()),
                                paymentMethod.getPaymentMethodTypeCode().value()
                        )
                )
        );

        assertThrows(
                PaymentMethodAlreadyInUseException.class,
                () -> paymentMethodFactory.newPaymentMethod(
                        paymentMethod.getPaymentMethodID(),
                        paymentMethod.getPaymentMethodName(),
                        paymentMethod.getPaymentMethodDescription(),
                        paymentMethod.getPaymentMethodStatus(),
                        paymentMethod.getPaymentMethodRanges(),
                        paymentMethod.getPaymentMethodTypeCode(),
                        paymentMethod.getPaymentMethodAsset()
                ).block()
        );
    }
}
