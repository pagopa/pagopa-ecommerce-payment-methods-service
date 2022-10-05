package it.pagopa.ecommerce.payment.methods.domain.valueobjects;

import org.springframework.lang.NonNull;

import java.io.Serializable;
import java.util.Objects;

public class PspPaymentMethodType implements Serializable {

    private final String paymentmethodType;

    public PspPaymentMethodType(@NonNull String paymentmethodType) {

        this.paymentmethodType = Objects.requireNonNull(paymentmethodType);
    }

    public @NonNull String value() {
        return paymentmethodType;
    }
}
