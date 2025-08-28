package it.pagopa.ecommerce.payment.methods.domain.aggregates;

import it.pagopa.ecommerce.payment.methods.domain.valueobjects.*;
import it.pagopa.ecommerce.payment.methods.exception.PaymentMethodAlreadyInUseException;
import it.pagopa.ecommerce.payment.methods.infrastructure.PaymentMethodDocument;
import it.pagopa.ecommerce.payment.methods.infrastructure.PaymentMethodRepository;
import it.pagopa.ecommerce.payment.methods.server.model.PaymentMethodManagementTypeDto;
import it.pagopa.ecommerce.payment.methods.server.model.PaymentMethodRequestDto;
import it.pagopa.ecommerce.payment.methods.utils.PaymentMethodStatusEnum;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:application.test.properties")
@ExtendWith(MockitoExtension.class)
class PaymentMethodFactoryTests {

    @Mock
    private PaymentMethodRepository paymentMethodRepository;

    @InjectMocks
    private PaymentMethodFactory paymentMethodFactory;

    @Test
    void shouldCreateNewNPGMethod() {

        PaymentMethod paymentMethod = TestUtil.getNPGPaymentMethod();
        PaymentMethodRequestDto.ClientIdEnum clientIdCheckout = TestUtil.getClientIdCheckout();

        Mockito.when(
                paymentMethodRepository.findByPaymentMethodNameAndPaymentMethodTypeCodeAndClientId(
                        paymentMethod.getPaymentMethodName().value(),
                        paymentMethod.getPaymentMethodTypeCode().value(),
                        clientIdCheckout.getValue()
                )
        ).thenReturn(Mono.empty());

        PaymentMethod paymentMethodProduct = paymentMethodFactory.newPaymentMethod(
                paymentMethod.getPaymentMethodID(),
                paymentMethod.getPaymentMethodName(),
                paymentMethod.getPaymentMethodDescription(),
                paymentMethod.getPaymentMethodStatus(),
                paymentMethod.getPaymentMethodRanges(),
                paymentMethod.getPaymentMethodTypeCode(),
                paymentMethod.getPaymentMethodAsset(),
                paymentMethod.getClientIdEnum(),
                paymentMethod.getPaymentMethodManagement(),
                paymentMethod.getPaymentMethodBrandAsset()
        ).block();

        assertNotNull(paymentMethodProduct);
    }

    @Test
    void shouldCreateNewRedirectMethod() {
        PaymentMethod paymentMethod = TestUtil.getRedirectPaymentMethod();
        PaymentMethodRequestDto.ClientIdEnum clientIdCheckout = TestUtil.getClientIdCheckout();

        Mockito.when(
                paymentMethodRepository.findByPaymentMethodNameAndPaymentMethodTypeCodeAndClientId(
                        paymentMethod.getPaymentMethodName().value(),
                        paymentMethod.getPaymentMethodTypeCode().value(),
                        clientIdCheckout.getValue()
                )
        ).thenReturn(Mono.empty());

        PaymentMethod paymentMethodProduct = paymentMethodFactory.newPaymentMethod(
                paymentMethod.getPaymentMethodID(),
                paymentMethod.getPaymentMethodName(),
                paymentMethod.getPaymentMethodDescription(),
                paymentMethod.getPaymentMethodStatus(),
                paymentMethod.getPaymentMethodRanges(),
                paymentMethod.getPaymentMethodTypeCode(),
                paymentMethod.getPaymentMethodAsset(),
                paymentMethod.getClientIdEnum(),
                paymentMethod.getPaymentMethodManagement(),
                paymentMethod.getPaymentMethodBrandAsset()
        ).block();

        assertNotNull(paymentMethodProduct);
    }

    @Test
    void shouldThrowDuplicatedMethodException() {
        PaymentMethod paymentMethod = TestUtil.getNPGPaymentMethod();
        PaymentMethodRequestDto.ClientIdEnum clientIdCheckout = TestUtil.getClientIdCheckout();

        Mockito.when(
                paymentMethodRepository.findByPaymentMethodNameAndPaymentMethodTypeCodeAndClientId(
                        paymentMethod.getPaymentMethodName().value(),
                        paymentMethod.getPaymentMethodTypeCode().value(),
                        clientIdCheckout.getValue()
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
                                paymentMethod.getPaymentMethodTypeCode().value(),
                                clientIdCheckout.getValue(),
                                paymentMethod.getPaymentMethodManagement().value().getValue(),
                                paymentMethod.getPaymentMethodBrandAsset().brandAssets().orElse(null)
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
                        paymentMethod.getPaymentMethodAsset(),
                        paymentMethod.getClientIdEnum(),
                        paymentMethod.getPaymentMethodManagement(),
                        paymentMethod.getPaymentMethodBrandAsset()
                ).block()
        );
    }

    @Test
    void shouldThrowExceptionForUnhandledPaymentMethodTypeCodeCreatingRedirectPaymentMethod() {

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new PaymentMethod(
                        new PaymentMethodID(UUID.randomUUID()),
                        new PaymentMethodName("TEST_NAME"),
                        new PaymentMethodDescription("payment method description"),
                        new PaymentMethodStatus(PaymentMethodStatusEnum.ENABLED),
                        new PaymentMethodType("unmanaged"),
                        List.of(new PaymentMethodRange(0L, 100L)),
                        new PaymentMethodAsset("asset"),
                        PaymentMethodRequestDto.ClientIdEnum.CHECKOUT,
                        new PaymentMethodManagement(PaymentMethodManagementTypeDto.REDIRECT),
                        new PaymentMethodBrandAssets(Optional.empty())
                )
        );

        assertEquals(
                "Payment method type code: [unmanaged] not managed for payment method management type REDIRECT! Allowed type codes: [RBPR, RBPB, RBPS, RICO, RBPP, RPIC, KLRN]",
                exception.getMessage()
        );
    }
}
