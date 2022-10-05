package it.pagopa.ecommerce.payment.methods.domain.valueobjects;

import org.springframework.lang.NonNull;

import java.io.Serializable;
import java.util.Objects;

public class PspPaymentMethodType implements Serializable {

    private final String paymentMethodType;

    public PspPaymentMethodType(@NonNull String paymentMethodType) {

        this.paymentMethodType = Objects.requireNonNull(paymentMethodType);
    }

    public @NonNull String value() {
        return paymentMethodType;
    }
}
