package it.pagopa.ecommerce.payment.instruments.domain.valueobjects;

import java.io.Serializable;
import java.util.Objects;

import org.springframework.lang.NonNull;

import lombok.EqualsAndHashCode;

@ValueObjects
@EqualsAndHashCode
public class PaymentInstrumentName implements Serializable {

    private final String name;

    public PaymentInstrumentName(@NonNull String name) {

        this.name = Objects.requireNonNull(name);
    }

    public @NonNull String value() {

        return name;
    }
}
