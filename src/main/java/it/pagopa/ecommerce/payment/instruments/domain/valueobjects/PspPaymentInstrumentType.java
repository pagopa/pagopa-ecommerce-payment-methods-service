package it.pagopa.ecommerce.payment.instruments.domain.valueobjects;

import org.springframework.lang.NonNull;

import java.io.Serializable;
import java.util.Objects;

public class PspPaymentInstrumentType implements Serializable {

    private final String paymentInstrumentType;

    public PspPaymentInstrumentType(@NonNull String paymentInstrumentType) {

        this.paymentInstrumentType = Objects.requireNonNull(paymentInstrumentType);
    }

    public @NonNull String value() {
        return paymentInstrumentType;
    }
}
