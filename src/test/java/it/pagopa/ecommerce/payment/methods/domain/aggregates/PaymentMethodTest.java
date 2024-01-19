package it.pagopa.ecommerce.payment.methods.domain.aggregates;

import it.pagopa.ecommerce.payment.methods.domain.valueobjects.*;
import it.pagopa.ecommerce.payment.methods.server.model.PaymentMethodManagementTypeDto;
import it.pagopa.ecommerce.payment.methods.server.model.PaymentMethodRequestDto;
import it.pagopa.ecommerce.payment.methods.utils.PaymentMethodStatusEnum;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class PaymentMethodTest {
    public static Stream<Arguments> getRedirectMismatchCases() {
        return Stream.of(
                Arguments.of(
                        new PaymentMethodType("CP"),
                        new PaymentMethodManagement(PaymentMethodManagementTypeDto.REDIRECT)
                ),
                Arguments.of(
                        new PaymentMethodType("REDIRECT"),
                        new PaymentMethodManagement(PaymentMethodManagementTypeDto.ONBOARDABLE)
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getRedirectMismatchCases")
    void shouldThrowOnRedirectMismatch(
                                       PaymentMethodType paymentMethodTypeCode,
                                       PaymentMethodManagement paymentMethodManagement
    ) {
        assertThrows(IllegalArgumentException.class, () -> {
            new PaymentMethod(
                    new PaymentMethodID(UUID.randomUUID()),
                    new PaymentMethodName("TEST_NAME"),
                    new PaymentMethodDescription("TEST_DESCRIPTION"),
                    new PaymentMethodStatus(
                            PaymentMethodStatusEnum.DISABLED
                    ),
                    paymentMethodTypeCode,
                    List.of(),
                    new PaymentMethodAsset("ASSET_URL"),
                    PaymentMethodRequestDto.ClientIdEnum.CHECKOUT,
                    paymentMethodManagement
            );
        });
    }
}
